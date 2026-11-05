"""Publish an explicitly selected, successful Windows build with known checksums."""
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import sys
import urllib.request
import zipfile


def sha(path):return hashlib.sha256(path.read_bytes()).hexdigest()


def manifest():
    data=json.loads(Path('release/windows.json').read_text())
    assert re.fullmatch(r'\d+\.\d+\.\d+',data['version'])
    assert isinstance(data['run_id'],int) and data['run_id']>0
    assert re.fullmatch('[0-9a-f]{40}',data['source_sha'])
    for key in ['setup_sha256','portable_sha256']:assert re.fullmatch('[0-9a-f]{64}',data[key])
    return data


def validate(data):
    req=urllib.request.Request('https://api.github.com/repos/'+os.environ['GITHUB_REPOSITORY']+'/actions/runs/'+str(data['run_id']),headers={'Authorization':'Bearer '+os.environ['GH_TOKEN'],'Accept':'application/vnd.github+json'})
    with urllib.request.urlopen(req,timeout=30) as response:run=json.load(response)
    assert run['conclusion']=='success' and run['status']=='completed'
    assert run['head_sha']==data['source_sha'] and run['name']=='Build Tasks PC'
    assert run['repository']['full_name']==os.environ['GITHUB_REPOSITORY']
    with open(os.environ['GITHUB_OUTPUT'],'a') as output:output.write('run_id='+str(data['run_id'])+'\n')
    print('Selected Windows build passed and matches the requested source.')


def prepare(data):
    version=data['version'];setup=Path('publish-input/setup')/('Tasks-Setup-v'+version+'.exe');portable=Path('publish-input/desktop/Tasks-PC.exe')
    assert sha(setup)==data['setup_sha256'] and sha(portable)==data['portable_sha256']
    assert (setup.with_suffix('.exe.sha256')).read_text().split()[0]==data['setup_sha256']
    assert (portable.with_suffix('.exe.sha256')).read_text().split()[0]==data['portable_sha256']
    dest=Path('downloads');dest.mkdir(exist_ok=True);result={'version':version,'source_sha':data['source_sha'],'run_id':data['run_id'],'files':{}}
    def record(path):
        digest=sha(path);path.with_name(path.name+'.sha256').write_text(digest+'  '+path.name+'\n')
        result['files'][path.name]={'sha256':digest,'bytes':path.stat().st_size}
    for source,names in [(setup,['Tasks-Setup-v'+version+'.exe','Tasks-Setup.exe']),(portable,['Tasks-PC-v'+version+'.exe','Tasks-PC.exe'])]:
        for name in names:target=dest/name;shutil.copyfile(source,target);record(target)
    instructions=Path('desktop/USING-TASKS-PC.txt').read_text();(dest/'START-HERE-WINDOWS.txt').write_text(instructions)
    archive=dest/('Tasks-PC-Windows-v'+version+'.zip')
    with zipfile.ZipFile(archive,'w',zipfile.ZIP_DEFLATED) as z:
        z.write(setup,'Tasks-Setup.exe');z.writestr('Tasks-Setup.exe.sha256',data['setup_sha256']+'  Tasks-Setup.exe\n');z.writestr('START-HERE.txt',instructions)
    record(archive);shutil.copyfile(archive,dest/'Tasks-PC-Windows.zip');record(dest/'Tasks-PC-Windows.zip')
    (dest/'windows-published.json').write_text(json.dumps(result,indent=2)+'\n')
    print(json.dumps(result,indent=2))


if __name__=='__main__':
    data=manifest()
    if sys.argv[1]=='validate':validate(data)
    elif sys.argv[1]=='prepare':prepare(data)
    else:raise ValueError('Unknown publish operation')
