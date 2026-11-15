"""Google account authorization and private Drive sync for Tasks PC.

Tokens never enter the WebView. Windows DPAPI protects the refresh token; task
state remains in the existing local store and sync uses Drive ETag preconditions.
"""
from __future__ import annotations
import base64
import ctypes
from ctypes import wintypes
import hashlib
import hmac
import json
import os
from pathlib import Path
import secrets
import threading
import time
import urllib.error
import urllib.parse
import urllib.request
import webbrowser
import uuid
import cloud_state
from http.server import BaseHTTPRequestHandler, HTTPServer

CLIENT_ID = '277712575915-aum3u79p81j3e83676mme4sgcf2l7ffg.apps.googleusercontent.com'
SCOPES = 'openid email https://www.googleapis.com/auth/drive.appdata'
FILE_NAME = 'tasks-state-v1.json'
MAX_BYTES = 2 * 1024 * 1024

class CloudError(Exception):
    def __init__(self, message, status=0, kind=""):
        super().__init__(message)
        self.status = status
        self.kind=kind

class NoRedirect(urllib.request.HTTPRedirectHandler):
    def redirect_request(self, req, fp, code, msg, headers, newurl):
        return None

def request(method, url, body=None, headers=None):
    parsed = urllib.parse.urlsplit(url)
    if parsed.scheme != 'https' or parsed.hostname not in {'www.googleapis.com', 'oauth2.googleapis.com', 'openidconnect.googleapis.com'}:
        raise CloudError('Invalid Google endpoint')
    req = urllib.request.Request(url, data=body, headers=headers or {}, method=method)
    try:
        with urllib.request.build_opener(NoRedirect).open(req, timeout=25) as response:
            data = response.read(MAX_BYTES + 8193)
            if len(data) > MAX_BYTES + 8192:
                raise CloudError('Google sync data is too large')
            return json.loads(data or b'{}'), dict(response.headers)
    except urllib.error.HTTPError as error:
        detail={}
        try:detail=json.loads(error.read(8192))
        except Exception:pass
        kind=detail.get('error');description=str(detail.get('error_description',''))
        if parsed.hostname=='oauth2.googleapis.com':
            if 'client_secret' in description or kind=='invalid_client':message='Import the Windows Google client JSON in Set up Google. The client configuration is missing or incorrect.'
            elif kind=='invalid_grant':message='Google sign-in is no longer valid. Sign in again.'
            else:message='Google could not authorize this desktop client. Check the Desktop app configuration.'
        elif error.code==401:message='Google access expired. Sign in again.'
        elif error.code==403:message='Google Drive access was denied. Check that Drive API is enabled for Tasks.'
        else:message='Google Drive is unavailable. Your local tasks are kept.'
        raise CloudError(message,error.code,kind if isinstance(kind,str)else'') from None
    except (OSError, ValueError):
        raise CloudError('Google is unavailable. Your tasks are saved on this PC.') from None

def form_request(fields):
    return request('POST', 'https://oauth2.googleapis.com/token',
                   urllib.parse.urlencode(fields).encode(), {'Content-Type': 'application/x-www-form-urlencoded'})[0]

class TokenVault:
    """DPAPI CurrentUser, with no plaintext credential fallback on other systems."""
    def __init__(self, path):
        self.path = Path(path)

    @staticmethod
    def crypt(data, decrypt=False):
        if os.name != 'nt':
            raise CloudError('Google sign-in requires the Windows app.')
        class Blob(ctypes.Structure):
            _fields_ = [('size', wintypes.DWORD), ('data', ctypes.POINTER(ctypes.c_byte))]
        buffer = ctypes.create_string_buffer(data)
        source = Blob(len(data), ctypes.cast(buffer, ctypes.POINTER(ctypes.c_byte)))
        result = Blob()
        library = ctypes.WinDLL('crypt32', use_last_error=True)
        function = library.CryptUnprotectData if decrypt else library.CryptProtectData
        function.argtypes = [ctypes.POINTER(Blob), ctypes.c_void_p, ctypes.c_void_p, ctypes.c_void_p,
                             ctypes.c_void_p, wintypes.DWORD, ctypes.POINTER(Blob)]
        function.restype = wintypes.BOOL
        if not function(ctypes.byref(source), None, None, None, None, 1, ctypes.byref(result)):
            raise CloudError('Windows could not unlock the Google account. Sign in again.')
        try:
            return ctypes.string_at(result.data, result.size)
        finally:
            local_free = ctypes.WinDLL('kernel32').LocalFree
            local_free.argtypes = [ctypes.c_void_p]
            local_free.restype = ctypes.c_void_p
            local_free(result.data)

    def load(self):
        if not self.path.exists():
            return None
        value = json.loads(self.crypt(self.path.read_bytes(), decrypt=True))
        if not isinstance(value, dict) or not all(isinstance(value.get(k), str) and value[k] for k in ['sub', 'email', 'refresh_token']):
            raise CloudError('The Google account could not be read. Sign in again.')
        return value

    def save(self, value):
        encrypted = self.crypt(json.dumps(value, separators=(',', ':')).encode())
        self.path.parent.mkdir(parents=True, exist_ok=True)
        temporary = self.path.with_suffix('.tmp')
        try:
            temporary.write_bytes(encrypted)
            os.replace(temporary, self.path)
        finally:
            temporary.unlink(missing_ok=True)

    def clear(self):
        self.path.unlink(missing_ok=True)


def authorize(cancel, open_browser=webbrowser.open, exchange=form_request, client_id=CLIENT_ID, client_secret=None):
    if not client_secret:raise CloudError("Import the Windows Google client JSON using Set up Google before signing in.")
    verifier = secrets.token_urlsafe(64)
    state = secrets.token_urlsafe(32)
    challenge = base64.urlsafe_b64encode(hashlib.sha256(verifier.encode()).digest()).rstrip(b'=').decode()
    outcome = {}
    class Callback(BaseHTTPRequestHandler):
        def log_message(self, *_):
            pass
        def do_GET(self):
            split = urllib.parse.urlsplit(self.path)
            params = urllib.parse.parse_qs(split.query, keep_blank_values=True)
            valid = (split.path == '/oauth/callback' and self.headers.get('Host') == '127.0.0.1:' + str(self.server.server_port)
                     and len(params.get('state', [])) == 1 and hmac.compare_digest(params['state'][0], state)
                     and all(len(v) == 1 for v in params.values()))
            if not valid:
                self.send_error(400, 'Invalid authorization response')
                return
            if 'error' in params:
                outcome['error'] = True
            elif params.get('code', [''])[0]:
                outcome['code'] = params['code'][0]
            else:
                self.send_error(400, 'Missing authorization response')
                return
            payload = b'<html><body>Google authorization received. You can close this page and return to Tasks.</body></html>'
            self.send_response(200)
            self.send_header('Content-Type', 'text/html; charset=utf-8')
            self.send_header('Content-Length', str(len(payload)))
            self.send_header('Cache-Control', 'no-store')
            self.send_header('Referrer-Policy', 'no-referrer')
            self.send_header('Content-Security-Policy', "default-src 'none'")
            self.end_headers()
            self.wfile.write(payload)
    with HTTPServer(('127.0.0.1', 0), Callback) as server:
        server.timeout = .5
        redirect = f'http://127.0.0.1:{server.server_port}/oauth/callback'
        query = urllib.parse.urlencode(dict(client_id=client_id, redirect_uri=redirect, response_type='code', scope=SCOPES,
                                            code_challenge=challenge, code_challenge_method='S256', state=state,
                                            access_type='offline', prompt='consent select_account'))
        if not open_browser('https://accounts.google.com/o/oauth2/v2/auth?' + query):
            raise CloudError('Open a default browser to sign in with Google.')
        deadline = time.monotonic() + 180
        while not outcome and not cancel.is_set() and time.monotonic() < deadline:
            server.handle_request()
        if cancel.is_set() or not outcome or outcome.get('error'):
            raise CloudError('Google sign-in was cancelled or timed out.')
        tokens = exchange(dict(client_id=client_id, client_secret=client_secret, code=outcome['code'], code_verifier=verifier,
                               redirect_uri=redirect, grant_type='authorization_code'))
        if not isinstance(tokens.get('access_token'), str) or not tokens.get('refresh_token'):
            raise CloudError('Google did not grant offline access. Sign in again.')
        identity = request('GET', 'https://openidconnect.googleapis.com/v1/userinfo',
                           headers={'Authorization': 'Bearer ' + tokens['access_token']})[0]
        if not identity.get('sub') or not identity.get('email') or identity.get('email_verified') is not True:
            raise CloudError('Google did not return a verified account.')
        return {'sub': identity['sub'], 'email': identity['email'], 'refresh_token': tokens['refresh_token']}

class ClientVault(TokenVault):
    def load(self):
        if not self.path.exists():return None
        value=json.loads(self.crypt(self.path.read_bytes(),decrypt=True))
        return self.validate(value)
    @staticmethod
    def validate(value):
        if not isinstance(value,dict) or not isinstance(value.get('client_id'),str) or not value['client_id'].startswith('277712575915-') or not value['client_id'].endswith('.apps.googleusercontent.com') or not isinstance(value.get('client_secret'),str) or not 6<len(value['client_secret'])<1000:
            raise CloudError('Choose the Desktop app client JSON from the Tasks Google project.')
        return {'client_id':value['client_id'],'client_secret':value['client_secret']}

class NotebookDrive:
    SEED='tasks-cloud-v2-seed.json'
    PREFIX='tasks-cloud-v2-device-'
    def __init__(self,token,transport=request):self.token,self.transport=token,transport
    def call(self,method,path,value=None):
        body=json.dumps(value,ensure_ascii=False,separators=(',',':'),allow_nan=False).encode()if value is not None else None
        if body is not None and len(body)>MAX_BYTES:raise CloudError('Tasks sync data is too large')
        return self.transport(method,'https://www.googleapis.com'+path,body,{'Authorization':'Bearer '+self.token,'Content-Type':'application/json'})[0]
    def files(self):
        result=[];page=''
        for _ in range(20):
            query={'spaces':'appDataFolder','q':"trashed = false and (name = '"+self.SEED+"' or name contains '"+self.PREFIX+"')",'fields':'files(id,name,createdTime),nextPageToken','pageSize':'1000'}
            if page:query['pageToken']=page
            value=self.call('GET','/drive/v3/files?'+urllib.parse.urlencode(query));result.extend(value.get('files',[]));page=value.get('nextPageToken')
            if not page:return sorted(result,key=lambda f:(f.get('createdTime',''),f['id']))
        raise CloudError('Too many Tasks cloud documents')
    def read(self,file_id):
        value=self.call('GET','/drive/v3/files/'+urllib.parse.quote(file_id,safe='')+'?alt=media')
        if value.get('schema')!=2 or value.get('application')!='personal.tasks':raise CloudError('The cloud data is invalid. It has not been overwritten.')
        return value
    def create(self,name,value):
        boundary='tasks-'+secrets.token_hex(16);metadata=json.dumps({'name':name,'parents':['appDataFolder'],'mimeType':'application/json'}).encode()
        body=json.dumps(value,ensure_ascii=False,separators=(',',':'),allow_nan=False).encode()
        if len(body)>MAX_BYTES:raise CloudError('Tasks cloud data is too large')
        payload=b'--'+boundary.encode()+b'\r\nContent-Type: application/json\r\n\r\n'+metadata+b'\r\n--'+boundary.encode()+b'\r\nContent-Type: application/json\r\n\r\n'+body+b'\r\n--'+boundary.encode()+b'--\r\n'
        result=self.transport('POST','https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart&fields=id',payload,{'Authorization':'Bearer '+self.token,'Content-Type':'multipart/related; boundary='+boundary})[0]
        if not result.get('id'):raise CloudError('Google did not save the Tasks cloud document')
        return result['id']
    def write(self,file_id,value):
        # Only this installation writes its device document. No representation-
        # dependent ETag is sent to a media endpoint.
        self.call('PUT','/upload/drive/v2/files/'+urllib.parse.quote(file_id,safe='')+'?uploadType=media',value)

class CloudSync:
    def __init__(self, store, merge, atomic, vault=None, drive_factory=NotebookDrive, config=None):
        self.store, self.merge, self.atomic = store, merge, atomic
        self.vault = vault or TokenVault(store.directory / 'google-account.dpapi')
        self.base_path = store.directory / 'google-sync-v2.json'
        self.client_vault=ClientVault(store.directory/'google-client.dpapi')
        self.configuration=config
        if self.configuration is None:
            try:self.configuration=self.client_vault.load()
            except Exception:self.configuration=None
        self.drive_factory = drive_factory
        self.lock = threading.RLock()
        self.account = None
        self.access_token = None
        self.expiry = 0
        self.generation = 0
        self.closed = threading.Event()
        self.wake = threading.Event()
        self.cancel_auth = threading.Event()
        self.auth_busy = False
        self.configuration_invalid=False
        self.needs_reauth=False
        self.message = 'Not signed in'
        self.last_sync = None
        try:
            self.account = self.vault.load()
            if self.account:
                self.message = 'Waiting to sync'
        except Exception:
            self.message = 'The saved Google account could not be unlocked. Sign in again.'
        self.thread = threading.Thread(target=self.run, name='Tasks Google sync', daemon=True)
        self.thread.start()
    def status(self):
        with self.lock:
            return {'signedIn': bool(self.account), 'email': self.account['email'] if self.account else '',
                    'message': self.message, 'signingIn': self.auth_busy, 'lastSyncAt': self.last_sync,'needsSetup':self.configuration is None or self.configuration_invalid,'needsReauth':self.needs_reauth}
    def sign_in(self):
        with self.lock:
            if self.auth_busy:
                return
            if not self.configuration or self.configuration_invalid:
                self.message='Use Set up Google to import the Windows client JSON.'
                return
            self.auth_busy = True
            self.generation += 1
            generation = self.generation
            self.cancel_auth = threading.Event()
            cancel = self.cancel_auth
            self.message = 'Finish signing in in your browser'
        def authenticate():
            try:
                account = authorize(cancel,client_id=self.configuration['client_id'],client_secret=self.configuration['client_secret'])
                with self.lock:
                    if generation != self.generation or self.closed.is_set() or cancel.is_set():
                        return
                    self.vault.save(account)
                    self.account, self.access_token, self.expiry = account, None, 0
                    self.needs_reauth=False
                    self.message = 'Signed in. Syncing…'
                    self.wake.set()
            except Exception as error:
                with self.lock:
                    if generation == self.generation:
                        self.message = str(error) if isinstance(error, CloudError) else 'Google sign-in could not be completed.'
                        if isinstance(error,CloudError)and error.kind in ('invalid_client','invalid_request'):self.configuration_invalid=True
            finally:
                with self.lock:
                    if generation == self.generation:
                        self.auth_busy = False
        threading.Thread(target=authenticate, name='Tasks Google sign in', daemon=True).start()
    def sign_out(self):
        with self.lock:
            self.generation += 1
            self.cancel_auth.set()
            self.vault.clear()
            self.account, self.access_token, self.expiry = None, None, 0
            self.auth_busy = False
            self.needs_reauth=False
            self.message, self.last_sync = 'Signed out. Tasks remain on this PC.', None
    def close(self):
        self.closed.set()
        self.cancel_auth.set()
        self.wake.set()
    def request_sync(self):
        self.wake.set()
    def token(self, account):
        if self.access_token and time.monotonic() < self.expiry:
            return self.access_token
        if not self.configuration:raise CloudError('Use Set up Google to import the Windows client JSON.')
        reply = form_request({'client_id':self.configuration['client_id'],'client_secret':self.configuration['client_secret'],'refresh_token':account['refresh_token'],'grant_type':'refresh_token'})
        if not isinstance(reply.get('access_token'), str):
            raise CloudError('Sign in to Google again')
        if self.account is None or self.account['sub'] != account['sub']:
            raise CloudError('Google account changed. Retrying with the current account.')
        self.access_token = reply['access_token']
        self.expiry = time.monotonic() + max(30, min(int(reply.get('expires_in', 300)), 3600) - 60)
        return self.access_token
    def configure(self,path):
        source=Path(path)
        if source.stat().st_size>65536:raise CloudError('Choose the downloaded Google Desktop client JSON.')
        value=json.loads(source.read_text(encoding='utf-8-sig'))
        config=ClientVault.validate(value.get('installed'))
        with self.lock:
            self.client_vault.save(config)
            if self.account and self.configuration and self.configuration['client_id']!=config['client_id']:self.sign_out()
            self.configuration=config;self.configuration_invalid=False;self.message='Google configured. Sign in to restore your cloud tasks.'
    def sync_once(self):
        with self.lock:
            if not self.account or self.auth_busy or self.closed.is_set():return
            account,generation=dict(self.account),self.generation
        try:
            drive=self.drive_factory(self.token(account));files=drive.files();seeds=[f for f in files if f['name']==NotebookDrive.SEED]
            if not seeds:
                with self.lock:self.message='Open Tasks on your current phone to initialize the cloud list.'
                return
            seed=drive.read(seeds[0]['id'])
            if seed.get('kind')!='seed' or not isinstance(seed.get('state'),dict)or not isinstance(seed.get('epoch'),str):raise CloudError('The cloud task list is invalid. Local tasks are kept.')
            epoch=seed['epoch'];sent=self.store.snapshot()['state']
            path=self.store.directory/('google-v2-'+hashlib.sha256(account['sub'].encode()).hexdigest()[:24]+'.json');self.base_path=path
            journal=json.loads(path.read_bytes())if path.exists()else None
            fresh=not journal or journal.get('epoch')!=epoch
            restore=fresh or self.store.cloud_account!=account['sub']
            if fresh:journal={'epoch':epoch,'owner':str(uuid.uuid4()),'counter':0,'registers':{},'localHead':{}}
            elif not restore:
                journal,_=cloud_state.capture(journal,sent);self.atomic(path,journal)
            documents=[];own_files=[];own=dict(journal['registers']);canonical_own=None
            for file in files:
                if not file['name'].startswith(NotebookDrive.PREFIX+epoch+'-'):continue
                doc=drive.read(file['id'])
                if doc.get('epoch')!=epoch or doc.get('kind')!='device':raise CloudError('Invalid cloud device document')
                if doc.get('owner')==journal['owner']:
                    own_files.append(file)
                    if canonical_own is None:canonical_own=doc['registers']
                    for key,op in doc['registers'].items():
                        if key not in own or op['clock']>own[key]['clock']:own[key]=op
                else:documents.append(doc)
            own_doc={'schema':2,'application':'personal.tasks','kind':'device','epoch':epoch,'owner':journal['owner'],'registers':own}
            documents.append(own_doc);resolved,counter=cloud_state.resolve(seed['state'],documents)
            with self.lock:
                if generation!=self.generation or self.closed.is_set():return
            if own and not fresh and own!=canonical_own:
                if own_files:drive.write(own_files[0]['id'],own_doc)
                else:drive.create(NotebookDrive.PREFIX+epoch+'-'+journal['owner']+'.json',own_doc)
            with self.lock:
                if generation!=self.generation or self.closed.is_set():return
                if restore:self.store.restore_cloud(sent,resolved,account['sub'])
                else:self.store.apply_remote(sent,resolved)
                journal.update(registers=own,counter=max(counter,journal['counter']),localHead=resolved)
                self.atomic(path,journal);self.message='Synced with Google';self.last_sync=time.time();self.needs_reauth=False
        except Exception as error:
            with self.lock:
                if generation==self.generation:
                    self.message=str(error)if isinstance(error,CloudError)else'Google sync is unavailable. Your tasks remain saved locally.'
                    if isinstance(error,CloudError)and(error.status==401 or error.kind=='invalid_grant'):self.access_token=None;self.expiry=0;self.needs_reauth=True
                    if isinstance(error,CloudError)and error.kind in ('invalid_client','invalid_request'):self.configuration_invalid=True
    def run(self):
        while not self.closed.is_set():
            self.sync_once()
            self.wake.wait(10)
            self.wake.clear()
