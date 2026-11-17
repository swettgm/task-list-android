#!/usr/bin/env python3
"""Warm diffuse rain with rounded droplets; no sharp high-frequency ticks."""
from pathlib import Path
import numpy as np,subprocess,hashlib,json
ROOT=Path(__file__).resolve().parents[1]
def rebuild():
 rate=24000;seconds=24;n=rate*seconds;rng=np.random.default_rng(20261117);frequencies=np.fft.rfftfreq(n,1/rate)
 def bed():
  spectrum=rng.normal(size=len(frequencies))+1j*rng.normal(size=len(frequencies));weights=np.maximum(frequencies,90.)**(-.45);weights*=np.minimum(1,frequencies/90.)**3;weights*=np.exp(-(frequencies/2500.)**6);weights[0]=0;signal=np.fft.irfft(spectrum*weights,n=n);return signal/np.std(signal)
 common=bed();left=.8*common+.2*bed();right=.8*common+.2*bed();data=np.column_stack((left,right))*.7
 for _ in range(180):
  length=int(rate*rng.uniform(.10,.25));t=np.arange(length)/rate;drop=rng.normal(size=length);kernel=np.exp(-.5*(np.arange(-18,19)/6.)**2);kernel/=kernel.sum();drop=np.convolve(drop,kernel,'same');drop*=((.5-.5*np.cos(np.pi*np.minimum(1,t/.014)))*np.exp(-t/rng.uniform(.045,.1)))*rng.uniform(.25,.6)
  start=int(rng.integers(n));pan=rng.uniform(.15,.85);positions=(start+np.arange(length))%n;data[positions,0]+=drop*np.sqrt(1-pan);data[positions,1]+=drop*np.sqrt(pan)
 t=np.arange(n)/rate;data*=(.94+.04*np.sin(2*np.pi*t/seconds)+.02*np.sin(4*np.pi*t/seconds+.7))[:,None]
 for channel in range(2):
  spectrum=np.fft.rfft(data[:,channel]);spectrum*=np.exp(-(frequencies/2700.)**8);data[:,channel]=np.fft.irfft(spectrum,n=n)
 data-=data.mean(axis=0);data*=min(.075/np.sqrt(np.mean(data**2)),.32/np.max(np.abs(data)));target=ROOT/'app/src/main/res/raw/rain.ogg';subprocess.run(['ffmpeg','-y','-v','error','-f','f32le','-ar',str(rate),'-ac','2','-i','pipe:0','-c:a','libvorbis','-q:a','7',str(target)],input=data.astype('<f4').tobytes(),check=True)
 return {'name':'rain','file':target.name,'sha256':hashlib.sha256(target.read_bytes()).hexdigest(),'seconds':seconds,'sampleRate':rate,'channels':2,'seed':20261117,'source':'Original warm diffuse stereo rain with rounded filtered droplets','highFrequencyRolloffHz':2700,'targetRms':.075,'peakCeiling':.32}
if __name__=='__main__':
 path=ROOT/'audio-source/ambient-processing.json';items=json.loads(path.read_text());result=rebuild();items=[result if item['name']=='rain'else item for item in items];path.write_text(json.dumps(items,indent=2)+'\n');print(json.dumps(result,indent=2))
