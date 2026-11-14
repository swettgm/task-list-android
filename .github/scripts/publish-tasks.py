#!/usr/bin/env python3
"""Publish checked binaries without sending large installers through a chat tool.

The Android APK is already signed locally with the retained private key; this
runner fetches its immutable Git blob. No app signing key is uploaded or used.
"""
from pathlib import Path
import base64
import hashlib
import json
import os
import re
import shutil
import subprocess
import sys
import urllib.request

REPO='swettgm/task-list-android'
ROOT=Path(__file__).resolve().parents[2]
PLAN=ROOT/'release/publish-tasks.json'
TEXT_PATHS={'README.md','VALIDATION.md','docs/WINDOWS-INSTALLER-VALIDATION.md','docs/WINDOWS-TASKBAR-VALIDATION.md'}
HEX64=re.compile(r'[0-9a-f]{64}\Z')
HEX40=re.compile(r'[0-9a-f]{40}\Z')

def api(path):
    token=os.environ['GH_TOKEN']
    request=urllib.request.Request('https://api.github.com/repos/'+REPO+path,headers={
        'Authorization':'Bearer '+token,'Accept':'application/vnd.github+json','X-GitHub-Api-Version':'2022-11-28'})
    # These JSON endpoints never require redirects. Never forward a token elsewhere.
    class NoRedirect(urllib.request.HTTPRedirectHandler):
        def redirect_request(self,*args):return None
    with urllib.request.build_opener(NoRedirect).open(request,timeout=40)as response:
        return json.loads(response.read(24*1024*1024))

def plan():
    data=json.loads(PLAN.read_text())
    assert data['repository']==REPO and re.fullmatch(r'0\.\d+\.\d+',data['version'])
    assert HEX40.fullmatch(data['source'])
    assert all(isinstance(data[k],int)and data[k]>0 for k in ['androidRun','windowsRun','windowsArtifact'])
    assert HEX64.fullmatch(data['windowsArtifactDigest'])
    android=data['android'];assert HEX40.fullmatch(android['blob']) and HEX64.fullmatch(android['sha256'])
    assert 0<android['bytes']<16*1024*1024
    assert set(android['targets'])=={'downloads/Tasks-v'+data['version']+'.apk','downloads/Task-List.apk','downloads/Task-List-Dark.apk'}
    expected={'Tasks-PC.exe':{'downloads/Tasks-PC.exe','downloads/Tasks-PC-v'+data['version']+'.exe'},
              'Tasks-Setup-v'+data['version']+'.exe':{'downloads/Tasks-Setup-v'+data['version']+'.exe','downloads/Tasks-Setup.exe'}}
    assert {item['name']for item in data['windows']}==set(expected)
    for item in data['windows']:
        assert set(item['targets'])==expected[item['name']] and HEX64.fullmatch(item['sha256'])
        assert 0<item['bytes']<100*1024*1024
    assert isinstance(data['textUpdates'],dict) and set(data['textUpdates'])<=TEXT_PATHS
    assert all(isinstance(text,str)and len(text)<500000 for text in data['textUpdates'].values())
    return data

def preflight(data):
    for run_id,expected_name in [(data['androidRun'],'Build Android APK'),(data['windowsRun'],'Build Tasks PC')]:
        run=api('/actions/runs/'+str(run_id))
        assert run['head_sha']==data['source'] and run['conclusion']=='success' and run['status']=='completed'
        assert run['name']==expected_name and run['head_repository']['full_name']==REPO
    artifact=api('/actions/artifacts/'+str(data['windowsArtifact']))
    assert artifact['name']=='Tasks-PC-Windows' and not artifact['expired']
    assert artifact['workflow_run']['id']==data['windowsRun'] and artifact['workflow_run']['head_sha']==data['source']
    assert artifact['digest']=='sha256:'+data['windowsArtifactDigest']
    subprocess.run(['git','merge-base','--is-ancestor',data['source'],'HEAD'],check=True,cwd=ROOT)
    protected=['app','desktop','.github/workflows/build-apk.yml','.github/workflows/build-desktop.yml','build.gradle','settings.gradle','gradle.properties']
    difference=subprocess.check_output(['git','diff','--name-only',data['source'],'HEAD','--',*protected],cwd=ROOT)
    assert not difference,'App sources changed after the tested build'
    if os.environ.get('GITHUB_OUTPUT'):
        with open(os.environ['GITHUB_OUTPUT'],'a')as out:
            out.write('artifact_id='+str(data['windowsArtifact'])+'\nwindows_run='+str(data['windowsRun'])+'\n')
    print('Verified successful Android/Windows runs, artifact provenance and unchanged tested sources.')

def write_verified(raw,item):
    assert len(raw)==item['bytes'] and hashlib.sha256(raw).hexdigest()==item['sha256'],'Binary checksum mismatch'
    if item.get('name','').endswith('.exe'):assert raw[:2]==b'MZ'
    else:assert raw[:2]==b'PK'
    for filename in item['targets']:
        path=ROOT/filename;path.parent.mkdir(parents=True,exist_ok=True);path.write_bytes(raw)
        path.with_suffix(path.suffix+'.sha256').write_text(item['sha256']+'  '+path.name+'\n')
        print(path.name,str(len(raw))+' bytes, SHA-256 verified')

def prepare(data):
    preflight(data)
    blob=api('/git/blobs/'+data['android']['blob'])
    assert blob['sha']==data['android']['blob'] and blob['encoding']=='base64'
    apk=base64.b64decode(blob['content']);write_verified(apk,data['android'])
    for item in data['windows']:
        path=ROOT/'release-input/windows'/item['name'];write_verified(path.read_bytes(),item)
    for filename,text in data['textUpdates'].items():(ROOT/filename).write_text(text)

def commit(data):
    files=list(data['textUpdates'])
    for item in [data['android'],*data['windows']]:
        for filename in item['targets']:
            path=ROOT/filename
            assert path.stat().st_size==item['bytes'] and hashlib.sha256(path.read_bytes()).hexdigest()==item['sha256']
            files.extend([filename,filename+'.sha256'])
    subprocess.run(['git','add','--',*files],check=True,cwd=ROOT)
    if not subprocess.check_output(['git','diff','--cached','--name-only'],cwd=ROOT).strip():
        print('These verified downloads are already published.');return
    subprocess.run(['git','commit','-m','Publish Tasks '+data['version']+' for Android and Windows'],check=True,cwd=ROOT)
    subprocess.run(['git','push','origin','HEAD:main'],check=True,cwd=ROOT)
    print('Published Tasks '+data['version']+' downloads atomically.')

if __name__=='__main__':
    operation=sys.argv[1];data=plan()
    {'preflight':preflight,'prepare':prepare,'commit':commit}[operation](data)
