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
from http.server import BaseHTTPRequestHandler, HTTPServer

CLIENT_ID = '277712575915-aum3u79p81j3e83676mme4sgcf2l7ffg.apps.googleusercontent.com'
SCOPES = 'openid email https://www.googleapis.com/auth/drive.appdata'
FILE_NAME = 'tasks-state-v1.json'
MAX_BYTES = 2 * 1024 * 1024

class CloudError(Exception):
    def __init__(self, message, status=0):
        super().__init__(message)
        self.status = status

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
        # Never return token responses or callback codes in UI errors/logs.
        raise CloudError('Google authorization expired. Sign in again.' if error.code in (400, 401) else
                         'Google Drive rejected this sync request.', error.code) from None
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


def authorize(cancel, open_browser=webbrowser.open, exchange=form_request):
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
        query = urllib.parse.urlencode(dict(client_id=CLIENT_ID, redirect_uri=redirect, response_type='code', scope=SCOPES,
                                            code_challenge=challenge, code_challenge_method='S256', state=state,
                                            access_type='offline', prompt='consent select_account'))
        if not open_browser('https://accounts.google.com/o/oauth2/v2/auth?' + query):
            raise CloudError('Open a default browser to sign in with Google.')
        deadline = time.monotonic() + 180
        while not outcome and not cancel.is_set() and time.monotonic() < deadline:
            server.handle_request()
        if cancel.is_set() or not outcome or outcome.get('error'):
            raise CloudError('Google sign-in was cancelled or timed out.')
        tokens = exchange(dict(client_id=CLIENT_ID, code=outcome['code'], code_verifier=verifier,
                               redirect_uri=redirect, grant_type='authorization_code'))
        if not isinstance(tokens.get('access_token'), str) or not tokens.get('refresh_token'):
            raise CloudError('Google did not grant offline access. Sign in again.')
        identity = request('GET', 'https://openidconnect.googleapis.com/v1/userinfo',
                           headers={'Authorization': 'Bearer ' + tokens['access_token']})[0]
        if not identity.get('sub') or not identity.get('email') or identity.get('email_verified') is not True:
            raise CloudError('Google did not return a verified account.')
        return {'sub': identity['sub'], 'email': identity['email'], 'refresh_token': tokens['refresh_token']}

class DriveState:
    def __init__(self, token, transport=request):
        self.token, self.transport = token, transport
    def call(self, method, path, value=None, headers=None):
        payload = json.dumps(value, ensure_ascii=False, separators=(',', ':'), allow_nan=False).encode() if value is not None else None
        if payload is not None and len(payload) > MAX_BYTES:
            raise CloudError('Tasks sync data is too large')
        return self.transport(method, 'https://www.googleapis.com' + path, payload,
                              {'Authorization': 'Bearer ' + self.token, 'Content-Type': 'application/json', **(headers or {})})
    def find(self):
        page = ''
        pages = 0
        candidates = []
        while True:
            pages += 1
            if pages > 20:
                raise CloudError('Too many Tasks sync pages')
            args = {'spaces': 'appDataFolder', 'q': "title = '" + FILE_NAME + "' and trashed = false",
                    'fields': 'items(id,createdDate),nextPageToken', 'maxResults': '100'}
            if page:
                args['pageToken'] = page
            data, _ = self.call('GET', '/drive/v2/files?' + urllib.parse.urlencode(args))
            candidates += [(f.get('createdDate', ''), f['id']) for f in data.get('items', []) if isinstance(f.get('id'), str)]
            page = data.get('nextPageToken')
            if not page:
                break
            if len(candidates) > 1000:
                raise CloudError('Too many Tasks sync files')
        return min(candidates)[1] if candidates else None
    @staticmethod
    def envelope(state):
        return {'schema': 1, 'application': 'personal.tasks', 'state': state}
    def create(self, state):
        boundary = 'tasks-' + secrets.token_hex(16)
        meta = json.dumps({'name': FILE_NAME, 'parents': ['appDataFolder'], 'mimeType': 'application/json'}).encode()
        body = json.dumps(self.envelope(state), ensure_ascii=False, separators=(',', ':'), allow_nan=False).encode()
        if len(body) > MAX_BYTES:
            raise CloudError('Tasks sync data is too large')
        payload = b'--' + boundary.encode() + b'\r\nContent-Type: application/json; charset=UTF-8\r\n\r\n' + meta
        payload += b'\r\n--' + boundary.encode() + b'\r\nContent-Type: application/json\r\n\r\n' + body + b'\r\n--' + boundary.encode() + b'--\r\n'
        data, _ = self.transport('POST', 'https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart&fields=id', payload,
                                {'Authorization': 'Bearer ' + self.token, 'Content-Type': 'multipart/related; boundary=' + boundary})
        if not isinstance(data.get('id'), str):
            raise CloudError('Google did not create the Tasks sync file')
        return data['id']
    def read(self, file_id):
        path = '/drive/v2/files/' + urllib.parse.quote(file_id, safe='')
        metadata, _ = self.call('GET', path + '?fields=id,etag')
        etag = metadata.get('etag')
        if not isinstance(etag, str) or not etag:
            raise CloudError('Google did not provide a safe sync version')
        data, _ = self.call('GET', path + '?alt=media', headers={'If-Match': etag})
        if data.get('schema') != 1 or data.get('application') != 'personal.tasks' or not isinstance(data.get('state'), dict):
            raise CloudError('The Google Tasks sync file could not be read. It was not overwritten.')
        return data['state'], etag
    def write(self, file_id, state, etag):
        self.call('PUT', '/upload/drive/v2/files/' + urllib.parse.quote(file_id, safe='') + '?uploadType=media&fields=id,etag',
                  self.envelope(state), {'If-Match': etag})

class CloudSync:
    def __init__(self, store, merge, atomic, vault=None, drive_factory=DriveState):
        self.store, self.merge, self.atomic = store, merge, atomic
        self.vault = vault or TokenVault(store.directory / 'google-account.dpapi')
        self.base_path = store.directory / 'google-sync.json'
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
                    'message': self.message, 'signingIn': self.auth_busy, 'lastSyncAt': self.last_sync}
    def sign_in(self):
        with self.lock:
            if self.auth_busy:
                return
            self.auth_busy = True
            generation = self.generation
            self.cancel_auth = threading.Event()
            cancel = self.cancel_auth
            self.message = 'Finish signing in in your browser'
        def authenticate():
            try:
                account = authorize(cancel)
                with self.lock:
                    if generation != self.generation or self.closed.is_set() or cancel.is_set():
                        return
                    self.vault.save(account)
                    self.account, self.access_token, self.expiry = account, None, 0
                    self.base_path.unlink(missing_ok=True)
                    self.message = 'Signed in. Syncing…'
                    self.wake.set()
            except Exception as error:
                with self.lock:
                    if generation == self.generation:
                        self.message = str(error) if isinstance(error, CloudError) else 'Google sign-in could not be completed.'
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
            self.base_path.unlink(missing_ok=True)
            self.account, self.access_token, self.expiry = None, None, 0
            self.auth_busy = False
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
        reply = form_request({'client_id': CLIENT_ID, 'refresh_token': account['refresh_token'], 'grant_type': 'refresh_token'})
        if not isinstance(reply.get('access_token'), str):
            raise CloudError('Sign in to Google again')
        if self.account is None or self.account['sub'] != account['sub']:
            raise CloudError('Google account changed. Retrying with the current account.')
        self.access_token = reply['access_token']
        self.expiry = time.monotonic() + max(30, min(int(reply.get('expires_in', 300)), 3600) - 60)
        return self.access_token
    def sync_once(self):
        with self.lock:
            if not self.account or self.auth_busy or self.closed.is_set():
                return
            account, generation = dict(self.account), self.generation
        try:
            drive = self.drive_factory(self.token(account))
            baseline = {}
            if self.base_path.exists():
                baseline = json.loads(self.base_path.read_bytes())
                if baseline.get('account') != account['sub']:
                    baseline = {}
            sent = self.store.snapshot()['state']
            file_id = drive.find()
            if file_id is None:
                with self.lock:
                    if generation != self.generation or self.closed.is_set():
                        return
                drive.create(sent)
                file_id = drive.find()
                if file_id is None:
                    raise CloudError('Waiting for Google Drive to publish your sync file')
            for attempt in range(8):
                try:
                    remote, etag = drive.read(file_id)
                    base = baseline.get('base', {}) if baseline.get('fileId') == file_id else {}
                    merged = self.merge(base, sent, remote)
                    with self.lock:
                        if generation != self.generation or self.closed.is_set():
                            return
                    if merged != remote:
                        drive.write(file_id, merged, etag)
                    with self.lock:
                        if generation != self.generation or self.closed.is_set():
                            return
                        self.store.apply_remote(sent, merged)
                        self.atomic(self.base_path, {'account': account['sub'], 'fileId': file_id, 'base': merged})
                        self.message, self.last_sync = 'Synced with Google', time.time()
                    return
                except CloudError as error:
                    if error.status != 412:
                        raise
            raise CloudError('Tasks changed on another device. Retrying shortly; local edits are kept.')
        except Exception as error:
            with self.lock:
                if generation == self.generation:
                    self.message = str(error) if isinstance(error, CloudError) else 'Sync unavailable. Your tasks are saved on this PC.'
                    if isinstance(error, CloudError) and error.status == 401:
                        self.access_token = None
                        self.expiry = 0
    def run(self):
        while not self.closed.is_set():
            self.sync_once()
            self.wake.wait(10)
            self.wake.clear()
