#!/usr/bin/env python3
"""Rebuild only brown noise and rain; keep every bell and white noise byte-for-byte."""
from pathlib import Path
import hashlib,json,subprocess
import numpy as np
ROOT=Path(__file__).resolve().parents[1]
RATE=24000;SECONDS=24;N=RATE*SECONDS
rng=np.random.default_rng(20261118)
freq=np.fft.rfftfreq(N,1/RATE)
def noise(power,low,high):
 spectrum=rng.normal(size=len(freq))+1j*rng.normal(size=len(freq))
 weights=np.maximum(freq,low)**(-power/2)*np.minimum(1,freq/low)**3*np.exp(-(freq/high)**8);weights[0]=0
 x=np.fft.irfft(spectrum*weights,n=N);return x/np.std(x)
def encode(name,data,description,**extra):
 data-=data.mean(axis=0);path=ROOT/'app/src/main/res/raw'/f'{name}.ogg'
 subprocess.run(['ffmpeg','-y','-v','error','-f','f32le','-ar',str(RATE),'-ac',str(1 if data.ndim==1 else data.shape[1]),'-i','pipe:0','-c:a','libvorbis','-q:a','7',str(path)],input=data.astype('<f4').tobytes(),check=True)
 return dict(name=name,file=path.name,sha256=hashlib.sha256(path.read_bytes()).hexdigest(),seconds=SECONDS,sampleRate=RATE,channels=1 if data.ndim==1 else data.shape[1],seed=20261118,source=description,**extra)
def brown():
 data=noise(2,35,3500)
 # Circular, smooth local-energy correction: no fade-in, fade-out, or loop reset.
 smooth=np.exp(-.5*(2*np.pi*freq*.10)**2)
 for _ in range(3):
  energy=np.maximum(np.fft.irfft(np.fft.rfft(data*data)*smooth,n=N),1e-10)
  data/=np.sqrt(energy)
 data*=.14/np.sqrt(np.mean(data**2))
 return encode('brown_noise',data,'Original periodic brown noise with smooth circular level stabilization',targetRms=.14,levelWindowSeconds=.10)
def rain():
 common=noise(.9,100,2600)
 bed=np.column_stack((.8*common+.3*noise(.9,100,2600),.8*common+.3*noise(.9,100,2600)))
 bed*=.065/np.sqrt(np.mean(bed**2));drops=np.zeros((N,2))
 for _ in range(420):
  length=int(RATE*rng.uniform(.09,.24));t=np.arange(length)/RATE
  splash=rng.normal(size=length);kernel=np.exp(-.5*(np.arange(-12,13)/3.5)**2);kernel/=kernel.sum();splash=np.convolve(splash,kernel,'same');splash/=np.std(splash)
  f0=rng.uniform(650,1700);phase=2*np.pi*f0*(.5*t+.5*.025*(1-np.exp(-t/.025)))
  water=np.sin(phase)+.12*np.sin(phase*1.48+.8)
  envelope=(1-np.exp(-t/.009))**2*np.exp(-t/rng.uniform(.025,.065));envelope*=np.minimum(1,(length/RATE-t)/.025)**2
  drop=(.78*splash+.22*water)*envelope*rng.uniform(.6,1.1)
  pos=(int(rng.integers(N))+np.arange(length))%N;pan=rng.uniform(.15,.85)
  drops[pos,0]+=drop*np.sqrt(1-pan);drops[pos,1]+=drop*np.sqrt(pan)
 drops*=.035/np.sqrt(np.mean(drops**2));data=bed+drops
 for c in range(2):data[:,c]=np.fft.irfft(np.fft.rfft(data[:,c])*np.exp(-(freq/2700)**8),n=N)
 data*=min(.075/np.sqrt(np.mean(data**2)),.32/np.max(np.abs(data)))
 return encode('rain',data,'Original soft stereo rainfall with rounded water droplets and diffuse warm wash',targetRms=.075,peakCeiling=.32,highFrequencyRolloffHz=2700,droplets=420)
if __name__=='__main__':
 path=ROOT/'audio-source/ambient-processing.json';items=json.loads(path.read_text());updates={x['name']:x for x in [brown(),rain()]};items=[updates.get(x['name'],x)for x in items];path.write_text(json.dumps(items,indent=2)+'\n');print(json.dumps(list(updates.values()),indent=2))
