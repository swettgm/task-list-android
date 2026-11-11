"""Exercise the actual bundled Windows executable without touching a user's data."""
import argparse
import json
import os
from pathlib import Path
import subprocess
import tempfile
import time
import urllib.error
import urllib.request


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--exe', required=True, type=Path)
    args = parser.parse_args()
    with tempfile.TemporaryDirectory(prefix='tasks-pc-smoke-') as temporary:
        directory = Path(temporary)
        env = dict(os.environ, TASKS_DATA_DIR=str(directory))
        process = subprocess.Popen([str(args.exe.resolve()), '--no-browser', '--host', '127.0.0.1', '--port', '0'], env=env)
        try:
            config = None
            for _ in range(300):
                if process.poll() is not None:
                    raise AssertionError('Tasks executable stopped before opening its local endpoint')
                file = directory / 'companion.json'
                try:
                    config = json.loads(file.read_text('utf-8'))
                    if config.get('port') and config.get('ready'):
                        break
                except (OSError, ValueError):
                    pass
                time.sleep(.1)
            assert config and config.get('port'), 'Packaged server did not start'
            origin = 'http://127.0.0.1:' + str(config['port'])
            def request(path, value=None, auth=True):
                headers = {'Authorization': 'Bearer ' + config['token']} if auth else {}
                payload = None if value is None else json.dumps(value).encode()
                req = urllib.request.Request(origin + path, data=payload, headers=headers)
                with urllib.request.urlopen(req, timeout=8) as response:
                    return response.read()
            initial = json.loads(request('/api/state'))
            assert initial['state'] == {} and not initial['phoneLinked'] and initial['revision'] >= 1
            try:
                request('/api/state', auth=False)
                raise AssertionError('Unauthenticated state request was accepted')
            except urllib.error.HTTPError as failure:
                assert failure.code == 401
            next_state = dict(initial['state'])
            next_state['completed'] = dict(next_state.get('completed', {}), **{'pc-smoke:2099-01-01': True})
            merged = json.loads(request('/api/sync', {'base': initial['state'], 'state': next_state}))
            assert merged['state']['completed']['pc-smoke:2099-01-01'] is True
            qr = json.loads(request('/api/pair'))
            assert '<svg' in qr['qrSvg'] and '#'+config['token'] in qr['url']
            html = request('/').decode('utf-8')
            assert 'window.__TASKS_DESKTOP_BOOT__=' in html and 'Tasks desktop startup' in html
            assert html.index('window.__TASKS_DESKTOP_BOOT__=') < html.index('global.TaskListStorage')
            assert 'pc-smoke:2099-01-01' in html
            public_landing = request('/pair', auth=False).decode()
            assert config['token'] not in public_landing
            request('/api/quit', {})
            process.wait(timeout=15)
            assert process.returncode == 0
            print('Packaged Tasks PC: startup, shared UI, auth, saved sync, QR, and clean shutdown passed')
        finally:
            if process.poll() is None:
                process.terminate()
                process.wait(timeout=10)


if __name__ == '__main__':
    main()
