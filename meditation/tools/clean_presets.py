#!/usr/bin/env python3
"""Original clean struck modes: no noise, distortion, phase vocoder or lossy encoding."""
from pathlib import Path
import numpy as np,subprocess,hashlib,json,argparse
ROOT=Path(__file__).resolve().parents[1]
def rebuild(name):
 source=ROOT/'audio-source/clean-bell-recipes.json';recipe=json.loads(source.read_text());rate=recipe['sampleRate'];t=np.arange(round(recipe['seconds']*rate))/rate;y=np.zeros(len(t))
 for frequency,amplitude,decay,attack in recipe['presets'][name]:
  gate=.5-.5*np.cos(np.pi*np.minimum(1,t/attack));y+=amplitude*np.sin(2*np.pi*frequency*t)*np.exp(-t/decay)*gate
 start=recipe['fadeStartSeconds'];end=recipe['fadeEndSeconds'];y*=np.where(t<=start,1,np.where(t<end,.5+.5*np.cos(np.pi*np.clip((t-start)/(end-start),0,1)),0));y*=recipe['targetPeak']/np.max(np.abs(y));target=ROOT/'app/src/main/res/raw'/f'{name}.flac'
 subprocess.run(['ffmpeg','-y','-v','error','-f','f32le','-ar',str(rate),'-ac','1','-i','pipe:0','-c:a','flac','-sample_fmt','s16','-compression_level','8',str(target)],input=y.astype('<f4').tobytes(),check=True)
 target.with_suffix('.ogg').unlink(missing_ok=True)
 return {'name':name,'file':target.name,'source':source.name,'sourceSha256':hashlib.sha256(source.read_bytes()).hexdigest(),'originalSeconds':recipe['seconds'],'seconds':recipe['seconds'],'fadeStartSeconds':start,'fadeEndSeconds':end,'sampleRate':rate,'channels':1,'sha256':hashlib.sha256(target.read_bytes()).hexdigest(),'lossless':True,'bytes':target.stat().st_size,'method':'original analytic damped inharmonic modes; no noise or stretching','modeFrequenciesHz':[mode[0]for mode in recipe['presets'][name]]}
if __name__=='__main__':
 parser=argparse.ArgumentParser();parser.add_argument('--only',choices=['soft_bell','gentle_chime','temple_gong']);args=parser.parse_args();path=ROOT/'audio-source/processing.json';records=json.loads(path.read_text());protected={name:hashlib.sha256((ROOT/'app/src/main/res/raw'/name).read_bytes()).hexdigest()for name in ['warm_bowl.flac','zen_tone.flac']}
 for i,item in enumerate(records):
  if item['name']in ['soft_bell','gentle_chime','temple_gong']and(not args.only or item['name']==args.only):records[i]=rebuild(item['name'])
 for name,expected in protected.items():assert hashlib.sha256((ROOT/'app/src/main/res/raw'/name).read_bytes()).hexdigest()==expected
 path.write_text(json.dumps(records,indent=2)+'\n');print(json.dumps(records,indent=2))
