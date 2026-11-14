import importlib.util
import json
import os
from pathlib import Path
import sys
import tempfile
import threading
import unittest
from unittest.mock import patch
sys.path.insert(0,str(Path(__file__).resolve().parents[1]))
import google_sync as google
import tasks_pc

class Vault:
    def __init__(self):self.account={'sub':'subject','email':'test@example.com','refresh_token':'private'}
    def load(self):return self.account
    def save(self,value):self.account=value
    def clear(self):self.account=None

class Drive:
    def __init__(self):self.state={'completed':{'remote':True}};self.etag=1;self.conflict=False;self.writes=0;self.before_write=None
    def find(self):return 'file'
    def read(self,file):return tasks_pc.clone(self.state),str(self.etag)
    def write(self,file,state,etag):
        if self.conflict:
            self.conflict=False;self.state['completed']['newRemote']=True;self.etag+=1
            raise google.CloudError('Conflict',412)
        if self.before_write:self.before_write()
        self.writes+=1;self.state=state;self.etag+=1

class DriveTests(unittest.TestCase):
    def test_reads_and_writes_require_the_same_etag(self):
        calls=[]
        def transport(method,url,body,headers):
            calls.append((method,url,body,headers))
            if 'fields=id,etag' in url and method=='GET':return {'etag':'"version-7"'},{}
            if 'alt=media' in url:return google.DriveState.envelope({'customTasks':[]}),{}
            return {},{}
        drive=google.DriveState('private-token',transport)
        state,etag=drive.read('file/id');drive.write('file/id',state,etag)
        self.assertEqual(calls[1][3]['If-Match'],'"version-7"')
        self.assertEqual(calls[2][3]['If-Match'],'"version-7"')
        self.assertIn('file%2Fid',calls[0][1])
        self.assertEqual(json.loads(calls[2][2])['application'],'personal.tasks')
    def test_missing_etag_and_invalid_documents_are_never_overwritten(self):
        for response in [{},{'etag':'v'}]:
            calls=[]
            def transport(method,url,body,headers):calls.append(method);return response,{}
            with self.assertRaises(google.CloudError):google.DriveState('private',transport).read('file')
            self.assertNotIn('PUT',calls)
    def test_oldest_duplicate_is_canonical(self):
        def transport(*args):return {'items':[{'id':'new','createdDate':'2026-11-03'},{'id':'old','createdDate':'2026-11-01'}]},{}
        self.assertEqual(google.DriveState('private',transport).find(),'old')
    def test_creation_uses_private_app_data_folder_and_schema(self):
        calls=[]
        def transport(method,url,body,headers):calls.append((method,url,body,headers));return {'id':'created'},{}
        self.assertEqual(google.DriveState('private',transport).create({'completed':{}}),'created')
        self.assertIn(b'"parents": ["appDataFolder"]',calls[0][2])
        self.assertIn(b'personal.tasks',calls[0][2])
    def test_tokens_cannot_be_sent_to_arbitrary_hosts_or_cleartext(self):
        for url in ['http://www.googleapis.com/drive/v2/files','https://example.com/steal','https://www.googleapis.com.evil.test/']:
            with self.assertRaises(google.CloudError):google.request('GET',url,headers={'Authorization':'Bearer private'})
    def test_no_plaintext_vault_fallback(self):
        if os.name=='nt':self.skipTest('Windows encryption tested separately')
        with tempfile.TemporaryDirectory()as folder:
            path=Path(folder)/'account';vault=google.TokenVault(path)
            with self.assertRaises(google.CloudError):vault.save({'refresh_token':'secret'})
            self.assertFalse(path.exists())
    @unittest.skipUnless(os.name=='nt','DPAPI requires Windows')
    def test_dpapi_roundtrip_is_encrypted_and_bound_to_current_user(self):
        with tempfile.TemporaryDirectory()as folder:
            path=Path(folder)/'account';vault=google.TokenVault(path)
            value={'sub':'subject','email':'test@example.com','refresh_token':'a-private-refresh-token'}
            vault.save(value);self.assertNotIn(b'a-private-refresh-token',path.read_bytes());self.assertEqual(vault.load(),value)
            vault.clear();self.assertIsNone(vault.load())

class AuthorizationTests(unittest.TestCase):
    def test_system_browser_flow_validates_state_and_uses_pkce(self):
        import urllib.request,urllib.parse,base64,hashlib
        urls=[];exchanges=[];statuses=[];threads=[]
        def browser(url):
            urls.append(url);query=urllib.parse.parse_qs(urllib.parse.urlsplit(url).query)
            callback=query['redirect_uri'][0]
            def respond():
                for state in ['wrong-state',query['state'][0]]:
                    try:
                        with urllib.request.urlopen(callback+'?'+urllib.parse.urlencode({'code':'private-code','state':state}),timeout=5)as response:statuses.append(response.status)
                    except urllib.error.HTTPError as error:statuses.append(error.code)
            thread=threading.Thread(target=respond);thread.start();threads.append(thread);return True
        def exchange(fields):exchanges.append(fields);return {'access_token':'access-private','refresh_token':'refresh-private'}
        with patch.object(google,'request',return_value=({'sub':'account','email':'me@example.com','email_verified':True},{})):
            account=google.authorize(threading.Event(),browser,exchange)
        for thread in threads:thread.join(5)
        self.assertEqual(statuses,[400,200]);self.assertEqual(account['refresh_token'],'refresh-private')
        query=urllib.parse.parse_qs(urllib.parse.urlsplit(urls[0]).query)
        challenge=base64.urlsafe_b64encode(hashlib.sha256(exchanges[0]['code_verifier'].encode()).digest()).rstrip(b'=').decode()
        self.assertEqual(query['code_challenge'],[challenge]);self.assertEqual(query['code_challenge_method'],['S256'])
        self.assertNotIn('private-code',urls[0]);self.assertNotIn('client_secret',exchanges[0])

class CloudTests(unittest.TestCase):
    def setup_cloud(self,folder,drive):
        store=tasks_pc.StateStore(folder,{'completed':{'local':True}})
        # Drive the worker deterministically; no sockets or real OAuth credentials.
        with patch.object(google.threading.Thread,'start'):
            cloud=google.CloudSync(store,tasks_pc.merge_state,tasks_pc.atomic_json,Vault(),lambda token:drive)
        cloud.token=lambda account:'private-token'
        self.addCleanup(cloud.close)
        return store,cloud
    def test_simultaneous_first_sync_merges_both_devices(self):
        with tempfile.TemporaryDirectory()as folder:
            drive=Drive();store,cloud=self.setup_cloud(folder,drive);cloud.sync_once()
            self.assertEqual(store.snapshot()['state']['completed'],{'local':True,'remote':True})
            self.assertEqual(drive.state,store.snapshot()['state'])
            self.assertNotIn('private-token',json.dumps(cloud.status()))
    def test_conflicting_etag_retries_against_new_remote_version(self):
        with tempfile.TemporaryDirectory()as folder:
            drive=Drive();drive.conflict=True;store,cloud=self.setup_cloud(folder,drive);cloud.sync_once()
            self.assertTrue(store.snapshot()['state']['completed']['newRemote'])
            self.assertEqual(drive.writes,1)
    def test_edits_during_network_request_win_locally_and_upload_next_time(self):
        with tempfile.TemporaryDirectory()as folder:
            drive=Drive();store,cloud=self.setup_cloud(folder,drive)
            def edit():
                snap=store.snapshot()['state'];changed=tasks_pc.clone(snap);changed['completed']['during']=True
                store.sync(snap,changed,user_edited=True)
            drive.before_write=edit;cloud.sync_once()
            self.assertTrue(store.snapshot()['state']['completed']['during'])
            drive.before_write=None;cloud.sync_once();self.assertTrue(drive.state['completed']['during'])
    def test_starting_new_sign_in_invalidates_a_previous_inflight_sync(self):
        with tempfile.TemporaryDirectory()as folder:
            drive=Drive();store,cloud=self.setup_cloud(folder,drive);before=store.snapshot()
            with patch.object(google.threading.Thread,'start'):
                drive.before_write=cloud.sign_in;cloud.sync_once()
            self.assertEqual(store.snapshot(),before)
            self.assertFalse(cloud.base_path.exists())
            self.assertTrue(cloud.status()['signingIn'])
    def test_old_account_refresh_cannot_be_cached_for_a_new_account(self):
        with tempfile.TemporaryDirectory()as folder:
            drive=Drive();store,cloud=self.setup_cloud(folder,drive);old=dict(cloud.account)
            def refresh(fields):
                cloud.sign_out();cloud.account={'sub':'different','email':'other@example.com','refresh_token':'other-private'}
                return {'access_token':'old-access','expires_in':3600}
            with patch.object(google,'form_request',side_effect=refresh):
                with self.assertRaises(google.CloudError):google.CloudSync.token(cloud,old)
            self.assertIsNone(cloud.access_token)

    def test_network_failure_keeps_local_data_and_baseline(self):
        with tempfile.TemporaryDirectory()as folder:
            drive=Drive();store,cloud=self.setup_cloud(folder,drive);before=store.snapshot()
            drive.read=lambda file:(_ for _ in ()).throw(google.CloudError('Offline'))
            cloud.sync_once();self.assertEqual(store.snapshot(),before);self.assertFalse(cloud.base_path.exists())
    def test_sign_out_preserves_tasks_and_clears_credentials_and_baseline(self):
        with tempfile.TemporaryDirectory()as folder:
            drive=Drive();store,cloud=self.setup_cloud(folder,drive);cloud.sync_once();before=store.snapshot()
            cloud.sign_out();self.assertEqual(store.snapshot(),before);self.assertIsNone(cloud.vault.load());self.assertFalse(cloud.base_path.exists());self.assertFalse(cloud.status()['signedIn'])
    def test_first_phone_link_does_not_delete_tasks_created_on_pc(self):
        with tempfile.TemporaryDirectory()as folder:
            store=tasks_pc.StateStore(folder,{})
            state={'customTasks':[{'id':'pc','title':'PC task'}]};store.sync({},state,user_edited=True)
            result=store.sync({}, {'customTasks':[{'id':'phone','title':'Phone task'}]},phone=True)
            self.assertEqual({v['id']for v in result['state']['customTasks']},{'pc','phone'})
    def test_cloud_import_survives_first_phone_link_and_restart(self):
        with tempfile.TemporaryDirectory()as folder:
            store=tasks_pc.StateStore(folder,{})
            store.apply_remote({}, {'customTasks':[{'id':'cloud','title':'Cloud task'}]})
            store=tasks_pc.StateStore(folder,{})
            result=store.sync({}, {'customTasks':[{'id':'phone','title':'Phone task'}]},phone=True)
            self.assertEqual({v['id']for v in result['state']['customTasks']},{'cloud','phone'})

if __name__=='__main__':unittest.main()
