#!/usr/bin/env python3
"""Rebuild only brown noise and rain; keep every bell and white noise byte-for-byte."""
from pathlib import Path
import hashlib,json,subprocess
import numpy as np
ROOT=Path(__file__).resolve().parents[1]
RATE=24000;SECONDS=24;N=RATE*SECONDS
rng=np.random.default_rng(2026111802)
freq=np.fft.rfftfreq(N,1/RATE)
def noise(power,low,high):
 spectrum=rng.normal(size=len(freq))+1j*rng.normal(size=len(freq))
 weights=np.maximum(freq,low)**(-power/2)*np.minimum(1,freq/low)**3*np.exp(-(freq/high)**8);weights[0]=0
 x=np.fft.irfft(spectrum*weights,n=N);return x/np.std(x)
def encode(name,data,description,**extra):
 data-=data.mean(axis=0);path=ROOT/'app/src/main/res/raw'/f'{name}.ogg'
 subprocess.run(['ffmpeg','-y','-v','error','-f','f32le','-ar',str(RATE),'-ac',str(1 if data.ndim==1 else data.shape[1]),'-i','pipe:0','-c:a','libvorbis','-q:a','7',str(path)],input=data.astype('<f4').tobytes(),check=True)
 return dict(name=name,file=path.name,sha256=hashlib.sha256(path.read_bytes()).hexdigest(),seconds=SECONDS,sampleRate=RATE,channels=1 if data.ndim==1 else data.shape[1],seed=2026111802,source=description,**extra)
def brown():
 data=noise(2,95,6000)
 # Circular, smooth local-energy correction: no fade-in, fade-out, or loop reset.
 smooth=np.exp(-.5*(2*np.pi*freq*.10)**2)
 for _ in range(3):
  energy=np.maximum(np.fft.irfft(np.fft.rfft(data*data)*smooth,n=N),1e-10)
  data/=np.sqrt(energy)
 data*=.16/np.sqrt(np.mean(data**2))
 return encode('brown_noise',data,'Original steady brown noise with audible warm low-mid energy and reduced sub-bass',targetRms=.16,levelWindowSeconds=.10)
def rain():
 source=ROOT/'audio-source/soft-rain-barkenov.mp3'
 metadata=json.loads((ROOT/'audio-source/rain-recording.json').read_text())
 assert hashlib.sha256(source.read_bytes()).hexdigest()==metadata['sha256']
 raw=subprocess.check_output(['ffmpeg','-v','error','-ss','8','-i',str(source),'-t','26','-ar',str(RATE),'-ac','2','-f','f32le','-'])
 recorded=np.frombuffer(raw,dtype='<f4').reshape(-1,2).copy();assert len(recorded)>=N+2*RATE
 # Equal-power tail-to-head overlap creates a continuous loop without
 # dulling the recording's real rain patter or adding synthetic droplets.
 overlap=2*RATE;data=recorded[overlap:overlap+N].copy();phase=np.linspace(0,np.pi/2,overlap)[:,None]
 data[-overlap:]=data[-overlap:]*np.cos(phase)+recorded[:overlap]*np.sin(phase)
 data-=data.mean(axis=0)
 # Lift the very quiet field recording, rounding isolated loud droplets
 # instead of turning down the entire rain bed to accommodate one peak.
 data*=.085/np.sqrt(np.mean(data.mean(axis=1)**2));data=.42*np.tanh(data/.42)
 data*=.075/np.sqrt(np.mean(data.mean(axis=1)**2))
 return encode('rain',data,'Soft rain falling in a garden by barkenov, CC0; natural recording with a seamless crossfade',targetRms=.075,sourceFile=source.name,sourceSha256=metadata['sha256'],sourcePage=metadata['sourcePage'],license='CC0-1.0',crossfadeSeconds=2)
if __name__=='__main__':
 path=ROOT/'audio-source/ambient-processing.json';items=json.loads(path.read_text());updates={x['name']:x for x in [brown(),rain()]};items=[updates.get(x['name'],x)for x in items];path.write_text(json.dumps(items,indent=2)+'\n');print(json.dumps(list(updates.values()),indent=2))
