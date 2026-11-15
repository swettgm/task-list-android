#!/usr/bin/env python3
"""Generate original periodic noise/rain loops. Requires NumPy and FFmpeg to rebuild."""
from pathlib import Path
import numpy as np,subprocess,json,hashlib
r=Path(__file__).resolve().parents[1];rate=24000;seconds=24;n=rate*seconds;rng=np.random.default_rng(20261115);freq=np.fft.rfftfreq(n,1/rate)
def noise(power,high):
 spectrum=rng.normal(size=len(freq))+1j*rng.normal(size=len(freq))
 weight=np.maximum(freq,35.)**(-power/2);weight*=np.minimum(1,freq/35.)**3;weight*=np.exp(-(freq/high)**6);weight[0]=0
 return np.fft.irfft(spectrum*weight,n=n)
reports=[]
for name in ['brown_noise','white_noise','rain']:
 if name=='brown_noise':data=noise(2,3500)
 elif name=='white_noise':data=noise(0,8500)
 else:
  data=noise(.7,6000);data/=np.std(data);data*=.28
  for _ in range(300):
   length=int(rate*rng.uniform(.035,.17));t=np.arange(length)/rate;drop=rng.normal(size=length)*np.exp(-t/rng.uniform(.009,.04))*(1-np.exp(-t/.001));drop*=rng.uniform(.15,.65);start=int(rng.integers(n));indices=(start+np.arange(length))%n;data[indices]+=drop
  # Periodic FFT filtering avoids artificial silence or fades at loop boundaries.
  spectrum=np.fft.rfft(data);spectrum*=np.exp(-(freq/7200)**6);data=np.fft.irfft(spectrum,n=n)
 data-=np.mean(data);data*=min(.16/np.std(data),.68/np.max(np.abs(data)))
 path=r/'app/src/main/res/raw'/f'{name}.ogg'
 subprocess.run(['ffmpeg','-y','-v','error','-f','f32le','-ar',str(rate),'-ac','1','-i','pipe:0','-c:a','libvorbis','-q:a','4',str(path)],input=data.astype('<f4').tobytes(),check=True)
 reports.append({'name':name,'file':path.name,'sha256':hashlib.sha256(path.read_bytes()).hexdigest(),'seconds':seconds,'sampleRate':rate,'seed':20261115,'source':'Original synthesized periodic noise and procedural rain'})
(r/'audio-source/ambient-processing.json').write_text(json.dumps(reports,indent=2)+'\n');print(json.dumps(reports,indent=2))
