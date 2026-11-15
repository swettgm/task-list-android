import base64,copy,hashlib,io,json,os,sys,tempfile,threading,unittest,urllib.error,urllib.parse,urllib.request
from pathlib import Path
from unittest.mock import patch
sys.path.insert(0,str(Path(__file__).resolve().parents[1]))
import google_sync as google,cloud_state,tasks_pc

class Vault:
 def __init__(self):self.account={'sub':'subject','email':'test@example.com','refresh_token':'private'}
 def load(self):return self.account
 def save(self,value):self.account=value
 def clear(self):self.account=None

class Drive:
 def __init__(self,seed=None):
  self.values={};self.names={};self.writes=0;self.before_write=None
  if seed is not None:self.values['seed']={'schema':2,'application':'personal.tasks','kind':'seed','epoch':'epoch-1','state':seed};self.names['seed']=google.NotebookDrive.SEED
 def files(self):return[{'id':k,'name':self.names[k],'createdTime':str(i).zfill(8)}for i,k in enumerate(self.values)]
 def read(self,key):return copy.deepcopy(self.values[key])
 def create(self,name,value):
  key='doc-'+str(len(self.values));self.values[key]=copy.deepcopy(value);self.names[key]=name;self.writes+=1;return key
 def write(self,key,value):
  if self.before_write:self.before_write()
  self.values[key]=copy.deepcopy(value);self.writes+=1

class DriveTests(unittest.TestCase):
 def test_reads_and_writes_never_send_a_metadata_etag_to_media(self):
  calls=[]
  def transport(method,url,body,headers):calls.append((method,url,body,headers));return {'schema':2,'application':'personal.tasks','state':{}},{}
  drive=google.NotebookDrive('private',transport);drive.read('id/part');drive.write('id/part',{'schema':2})
  self.assertTrue(calls[0][1].endswith('id%2Fpart?alt=media'));self.assertEqual(calls[1][0],'PUT')
  self.assertTrue(all('If-Match'not in call[3]for call in calls))
 def test_creation_is_private_app_data_only(self):
  calls=[]
  def transport(*args):calls.append(args);return {'id':'created'},{}
  self.assertEqual(google.NotebookDrive('private',transport).create(google.NotebookDrive.SEED,{'schema':2}),'created')
  self.assertIn(b'appDataFolder',calls[0][2])
 def test_windows_client_configuration_requires_an_installed_client_in_same_project(self):
  good={'client_id':google.CLIENT_ID,'client_secret':'fake-client-secret'}
  self.assertEqual(google.ClientVault.validate(good),good)
  for bad in [{},{'client_id':'other-project.apps.googleusercontent.com','client_secret':'fake-client-secret'},{'client_id':google.CLIENT_ID}]:
   with self.assertRaises(google.CloudError):google.ClientVault.validate(bad)
 def test_missing_client_secret_is_not_misreported_as_expired_authorization(self):
  payload=json.dumps({'error':'invalid_request','error_description':'client_secret is missing.'}).encode()
  error=urllib.error.HTTPError('https://oauth2.googleapis.com/token',400,'bad',{},io.BytesIO(payload))
  with patch.object(urllib.request.OpenerDirector,'open',side_effect=error):
   with self.assertRaises(google.CloudError)as caught:google.form_request({'client_id':google.CLIENT_ID})
  self.assertIn('client JSON',str(caught.exception));self.assertNotIn('expired',str(caught.exception))
 def test_tokens_cannot_be_sent_to_arbitrary_hosts_or_cleartext(self):
  for url in ['http://www.googleapis.com/drive/v3/files','https://example.com/steal']:
   with self.assertRaises(google.CloudError):google.request('GET',url)
 @unittest.skipUnless(os.name=='nt','DPAPI requires Windows')
 def test_dpapi_encrypts_both_refresh_token_and_desktop_client_secret(self):
  with tempfile.TemporaryDirectory()as folder:
   for cls,value in [(google.TokenVault,{'sub':'subject','email':'test@example.com','refresh_token':'private-refresh'}),(google.ClientVault,{'client_id':google.CLIENT_ID,'client_secret':'private-client-secret'})]:
    path=Path(folder)/cls.__name__;vault=cls(path);vault.save(value);self.assertEqual(vault.load(),value);self.assertNotIn(b'private-',path.read_bytes())

class AuthorizationTests(unittest.TestCase):
 def test_system_browser_uses_pkce_state_and_configured_client_secret(self):
  urls=[];exchanges=[];statuses=[];threads=[]
  def browser(url):
   urls.append(url);query=urllib.parse.parse_qs(urllib.parse.urlsplit(url).query)
   def respond():
    for state in ['wrong',query['state'][0]]:
     try:
      with urllib.request.urlopen(query['redirect_uri'][0]+'?'+urllib.parse.urlencode({'code':'private-code','state':state}),timeout=5)as response:statuses.append(response.status)
     except urllib.error.HTTPError as error:statuses.append(error.code)
   thread=threading.Thread(target=respond);thread.start();threads.append(thread);return True
  def exchange(fields):exchanges.append(fields);return {'access_token':'private-access','refresh_token':'private-refresh'}
  with patch.object(google,'request',return_value=({'sub':'account','email':'me@example.com','email_verified':True},{})):
   result=google.authorize(threading.Event(),browser,exchange,client_secret='fake-client-secret')
  for thread in threads:thread.join(5)
  self.assertEqual(statuses,[400,200]);self.assertEqual(exchanges[0]['client_secret'],'fake-client-secret')
  q=urllib.parse.parse_qs(urllib.parse.urlsplit(urls[0]).query);self.assertEqual(q['code_challenge'],[base64.urlsafe_b64encode(hashlib.sha256(exchanges[0]['code_verifier'].encode()).digest()).rstrip(b'=').decode()])
  self.assertNotIn('fake-client-secret',urls[0]);self.assertEqual(result['sub'],'account')
 def test_missing_configuration_does_not_start_a_doomed_browser_flow(self):
  with self.assertRaises(google.CloudError):google.authorize(threading.Event(),lambda url:self.fail('Browser should not open'))

class RegisterTests(unittest.TestCase):
 def test_shared_cross_platform_cases_converge_in_any_order(self):
  fixture=Path(__file__).resolve().parents[2]/'app/src/test/resources/cloud-v2-fixtures.json'
  for case in json.loads(fixture.read_text()):
   self.assertEqual(cloud_state.resolve(case['seed'],case['documents'])[0],case['expected'])
   self.assertEqual(cloud_state.resolve(case['seed'],list(reversed(case['documents'])))[0],case['expected'])
 def test_offline_deletions_are_tombstones_and_retry_does_not_reclock(self):
  old={'customTasks':[{'id':'a','title':'Delete'}],'inProgressTasks':['a:date']};new={'customTasks':[],'inProgressTasks':[]}
  book={'counter':0,'registers':{},'localHead':old};book,changed=cloud_state.capture(book,new);again,changedAgain=cloud_state.capture(book,new)
  self.assertTrue(changed);self.assertFalse(changedAgain);self.assertEqual(again['counter'],1)
  result,_=cloud_state.resolve(old,[{'owner':'phone','registers':book['registers']}]);self.assertEqual(result['customTasks'],[]);self.assertEqual(result['inProgressTasks'],[])
 def test_independent_first_projects_are_merged_instead_of_overwriting_map(self):
  docs=[]
  for owner in ['phone','pc']:
   book,_=cloud_state.capture({'counter':0,'registers':{},'localHead':{}},{'projects':{owner:{'id':owner,'title':owner}}});docs.append({'owner':owner,'registers':book['registers']})
  state,_=cloud_state.resolve({},docs);self.assertEqual(set(state['projects']),{'phone','pc'})

class CloudTests(unittest.TestCase):
 def setup_cloud(self,folder,drive,seed=None):
  store=tasks_pc.StateStore(folder,seed or{})
  with patch.object(google.threading.Thread,'start'):
   cloud=google.CloudSync(store,tasks_pc.merge_state,tasks_pc.atomic_json,Vault(),lambda token:drive,config={'client_id':google.CLIENT_ID,'client_secret':'fake-client-secret'})
  cloud.token=lambda account:'private-access';self.addCleanup(cloud.close);return store,cloud
 def test_phone_list_is_restored_without_uploading_old_desktop_tasks(self):
  with tempfile.TemporaryDirectory()as folder:
   phone={'customTasks':[{'id':'phone','title':'Current phone list'}],'taskPurged':{'deleted':True},'completed':{}}
   drive=Drive(phone);store,cloud=self.setup_cloud(folder,drive,{'customTasks':[{'id':'deleted','title':'Old deleted task'}]});cloud.sync_once()
   self.assertEqual([t['id']for t in store.state['customTasks']],['phone']);self.assertEqual(drive.writes,0);self.assertTrue(store.state['taskPurged']['deleted']);self.assertTrue(list((Path(folder)/'cloud-backups').glob('*.json')))
 def test_desktop_never_initializes_the_cloud_from_its_old_list(self):
  with tempfile.TemporaryDirectory()as folder:
   drive=Drive();store,cloud=self.setup_cloud(folder,drive,{'customTasks':[{'id':'old','title':'Old'}]});before=store.snapshot();cloud.sync_once();self.assertEqual(store.snapshot(),before);self.assertEqual(drive.writes,0);self.assertIn('phone',cloud.message)
 def test_changes_and_deletions_sync_across_clients_and_survive_restart(self):
  with tempfile.TemporaryDirectory()as a,tempfile.TemporaryDirectory()as b:
   drive=Drive({'customTasks':[{'id':'a','title':'Keep'},{'id':'delete','title':'Delete'}],'completed':{}});sa,ca=self.setup_cloud(a,drive);sb,cb=self.setup_cloud(b,drive);ca.sync_once();cb.sync_once()
   before=sa.snapshot()['state'];changed=copy.deepcopy(before);changed['customTasks']=[t for t in changed['customTasks']if t['id']!='delete'];changed['completed']['a:date']=True;sa.sync(before,changed,user_edited=True);ca.sync_once();cb.sync_once()
   self.assertEqual([t['id']for t in sb.state['customTasks']],['a']);self.assertTrue(sb.state['completed']['a:date'])
   reopened=tasks_pc.StateStore(b,{});self.assertEqual(reopened.state,sb.state)
 def test_stale_browser_write_after_restore_cannot_reintroduce_deleted_tasks(self):
  with tempfile.TemporaryDirectory()as folder:
   drive=Drive({'customTasks':[]});old={'customTasks':[{'id':'old','title':'Old'}]};store,cloud=self.setup_cloud(folder,drive,old);cloud.sync_once();reply=store.sync({},old,cloud_session='')
   self.assertEqual(reply['state']['customTasks'],[])
 def test_signout_keeps_local_tasks_and_retains_their_offline_journal(self):
  with tempfile.TemporaryDirectory()as folder:
   drive=Drive({'customTasks':[]});store,cloud=self.setup_cloud(folder,drive);cloud.sync_once();before=store.snapshot();cloud.sign_out();self.assertEqual(store.snapshot(),before);self.assertTrue(cloud.base_path.exists());self.assertFalse(cloud.status()['signedIn'])
 def test_switching_accounts_restores_instead_of_uploading_previous_account(self):
  with tempfile.TemporaryDirectory()as folder:
   drive=Drive({'customTasks':[{'id':'a','title':'Account A'}]});store,cloud=self.setup_cloud(folder,drive);cloud.sync_once();cloud.account={'sub':'different','email':'other@example.com','refresh_token':'private'};drive.values['seed']['state']={'customTasks':[{'id':'b','title':'Account B'}]};cloud.sync_once();self.assertEqual([t['id']for t in store.state['customTasks']],['b']);self.assertEqual(drive.writes,0)

if __name__=='__main__':unittest.main()
