"""Run the real Windows setup, upgrade and uninstall with isolated test data."""
import argparse
import ctypes
import json
import os
from pathlib import Path
import subprocess
import tempfile
import time
import urllib.request
import winreg

APP_ID = '{D242BDF2-B929-4327-B6A9-ABF045127A35}_is1'
REGISTRY = r'Software\Microsoft\Windows\CurrentVersion\Uninstall' + '\\' + APP_ID


def registered():
    try:
        with winreg.OpenKey(winreg.HKEY_CURRENT_USER, REGISTRY) as key:
            return winreg.QueryValueEx(key, 'DisplayName')[0] == 'Tasks'
    except FileNotFoundError:
        return False


def uninstall_keys():
    result=[]
    for flag in [winreg.KEY_WOW64_64KEY,winreg.KEY_WOW64_32KEY]:
        try:
            with winreg.OpenKey(winreg.HKEY_CURRENT_USER,r'Software\Microsoft\Windows\CurrentVersion\Uninstall',0,winreg.KEY_READ|flag) as root:
                for i in range(winreg.QueryInfoKey(root)[0]):
                    name=winreg.EnumKey(root,i)
                    with winreg.OpenKey(root,name) as key:
                        try:
                            value=winreg.QueryValueEx(key,'DisplayName')[0]
                            if 'Tasks' in value:result.append({'view':flag,'key':name,'name':value})
                        except FileNotFoundError:pass
        except FileNotFoundError:pass
    return result


def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--setup',required=True,type=Path)
    args=parser.parse_args()
    with tempfile.TemporaryDirectory(prefix='tasks-setup-qa-') as folder:
        root=Path(folder);install=root/'Tasks App';data=root/'data';env=dict(os.environ,TASKS_DATA_DIR=str(data));group='Tasks QA '+root.name
        desktop_buffer=ctypes.create_unicode_buffer(260)
        assert ctypes.windll.shell32.SHGetFolderPathW(None,16|0x8000,None,0,desktop_buffer)==0
        desktop=Path(desktop_buffer.value)/'Tasks.lnk'
        shortcut=Path(os.environ['APPDATA'])/'Microsoft/Windows/Start Menu/Programs'/group/'Tasks.lnk'
        setup=[str(args.setup.resolve()),'/VERYSILENT','/SUPPRESSMSGBOXES','/NORESTART','/SP-',f'/DIR={install}',f'/GROUP={group}','/TASKS=desktopicon']
        processes=[]
        def execute(command):
            result=subprocess.run(command,env=env,timeout=120)
            assert result.returncode==0, (command[0],result.returncode)
        def request(config,path,body=None):
            req=urllib.request.Request('http://127.0.0.1:'+str(config['port'])+path,
                data=None if body is None else json.dumps(body).encode(),
                headers={'Authorization':'Bearer '+config['token'],'Content-Type':'application/json','X-Tasks-Client':'android'})
            with urllib.request.urlopen(req,timeout=8) as response:return json.loads(response.read())
        def start():
            process=subprocess.Popen([str(install/'Tasks-PC.exe'),'--no-browser','--host','127.0.0.1','--port','0'],env=env);processes.append(process)
            for _ in range(300):
                assert process.poll() is None,'Installed app exited early'
                try:
                    config=json.loads((data/'companion.json').read_text('utf-8'))
                    request(config,'/api/state')
                    return process,config
                except (OSError,ValueError):time.sleep(.1)
            raise AssertionError('Installed app did not start')
        try:
            execute(setup+['/LOG='+str(root/'install.log')])
            assert (install/'Tasks-PC.exe').is_file() and (install/'unins000.exe').is_file()
            assert registered() and shortcut.is_file() and desktop.is_file(), json.dumps({'registered':registered(),'expectedShortcut':str(shortcut),'shortcutExists':shortcut.is_file(),'expectedDesktop':str(desktop),'desktopExists':desktop.is_file(),'actualShortcuts':[str(p) for p in (Path(os.environ['APPDATA'])/'Microsoft/Windows/Start Menu/Programs').rglob('*.lnk') if 'Tasks' in str(p)],'uninstallKeys':uninstall_keys()})
            assert not data.exists(),'Installing must not create placeholder task data'
            process,config=start();token=config['token']
            state={'version':2026103022,'progressStateInitialized':True,'completed':{'setup:2026-11-05':True},'uiTheme':{'accent':'#80ddaa'},'customTasks':[{'id':'setup','title':'Preserve after upgrade'}]}
            request(config,'/api/sync',{'base':{},'state':state})
            before=(data/'tasks-state.json').read_bytes()
            # Installing over a live copy must close it and keep data and pairing intact.
            execute(setup+['/LOG='+str(root/'upgrade.log')]);process.wait(timeout=20)
            assert process.returncode==0
            assert (data/'tasks-state.json').read_bytes()==before
            assert json.loads((data/'companion.json').read_text())['token']==token
            assert registered() and shortcut.exists() and desktop.exists()
            process,config=start();assert request(config,'/api/state')['state']['completed']['setup:2026-11-05']
            execute([str(install/'unins000.exe'),'/VERYSILENT','/SUPPRESSMSGBOXES','/NORESTART'])
            process.wait(timeout=20)
            for _ in range(50):
                if not (install/'Tasks-PC.exe').exists():break
                time.sleep(.1)
            assert not (install/'Tasks-PC.exe').exists() and not registered()
            assert not shortcut.exists() and not desktop.exists()
            assert (data/'tasks-state.json').read_bytes()==before
            assert json.loads((data/'companion.json').read_text())['token']==token
            print(json.dumps({'passed':True,'checks':['per-user install','Start menu shortcut','desktop shortcut','Apps uninstall registration','installed executable startup','upgrade closes running companion','tasks and theme survive upgrade','pairing survives upgrade','uninstall closes companion','uninstall removes program and shortcuts','uninstall preserves personal data']}))
        finally:
            for process in processes:
                if process.poll() is None:process.terminate();process.wait(timeout=10)
            if (install/'unins000.exe').exists():
                subprocess.run([str(install/'unins000.exe'),'/VERYSILENT','/SUPPRESSMSGBOXES','/NORESTART'],env=env,timeout=60)


if __name__=='__main__':main()
