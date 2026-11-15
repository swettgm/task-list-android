#!/usr/bin/env python3
from pathlib import Path
from array import array
import subprocess,json,hashlib,math,sys
root=Path(__file__).resolve().parents[1];report=[]
for item in json.loads((root/'audio-source/ambient-processing.json').read_text()):
 path=root/'app/src/main/res/raw'/item['file'];assert hashlib.sha256(path.read_bytes()).hexdigest()==item['sha256']
 raw=subprocess.check_output(['ffmpeg','-v','error','-i',str(path),'-ar','24000','-ac','1','-f','f32le','-']);samples=array('f');samples.frombytes(raw)
 if sys.byteorder!='little':samples.byteswap()
 assert abs(len(samples)/24000-24)<.01
 peak=max(abs(v)for v in samples);rms=math.sqrt(sum(v*v for v in samples)/len(samples));assert .05<rms<.25 and peak<.95,(item['name'],rms,peak)
 steps=sorted(abs(samples[i]-samples[i-1])for i in range(1,len(samples)));seam=abs(samples[-1]-samples[0]);assert seam<max(.03,steps[int(len(steps)*.95)]*1.5),(item['name'],seam)
 chunks=[math.sqrt(sum(v*v for v in samples[i:i+24000])/24000)for i in range(0,len(samples),24000)];assert min(chunks)>.03
 report.append({'name':item['name'],'seconds':len(samples)/24000,'rms':rms,'peak':peak,'loopSeamStep':seam,'typicalStep':steps[int(len(steps)*.95)]})
assert len(report)==3
out=root/'build/audio-qa';out.mkdir(exist_ok=True,parents=True);(out/'ambient-report.json').write_text(json.dumps(report,indent=2)+'\n');print(json.dumps({'passed':len(report),'ambientChecks':report},indent=2))
