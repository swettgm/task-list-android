"""Retain real-time bell attacks; extend only their ringing modes.

Rebuilding uses numpy/scipy, in addition to FFmpeg. They are development-only;
neither the APK nor its audio verification needs these Python packages.
"""
import hashlib,json,math,subprocess,sys
from pathlib import Path
import numpy as np
from scipy.optimize import least_squares
from scipy.signal import correlate

ROOT=Path(__file__).resolve().parents[1]
def decode(path,channels):
 return np.frombuffer(subprocess.check_output(['ffmpeg','-v','error','-i',str(path),'-ar','48000','-ac',str(channels),'-f','f32le','-']),np.float32).reshape(-1,channels).astype(float)

def rebuild(name):
 rate=48000;channels=2 if name=='zen_tone'else 1
 filename='zen_tone_original.mp3'if name=='zen_tone'else'warm_bowl.wav'
 source=ROOT/'audio-source'/filename;x=decode(source,channels);times=np.arange(20*rate)/rate
 y=np.zeros((len(times),channels));extra={}
 if name=='zen_tone':
  # No time-stretch processing or low-pass on the strike. Fit the recording's
  # bell modes to extend the late ring without a phase-vocoder attack/artifact.
  anchor=1.4;join_end=2.;indices=np.arange(round(1.2*rate),round(1.65*rate),4);t=indices/rate
  def matrix(freqs,t):return np.column_stack([fn(2*np.pi*f*(t-anchor))for f in freqs for fn in[np.cos,np.sin]])
  def residual(freqs):
   m=matrix(freqs,t);c=np.linalg.lstsq(m,x[indices],rcond=None)[0];return(m@c-x[indices]).ravel()
  fit=least_squares(residual,[548.,1410.,2635.],bounds=([544,1406,2610],[552,1414,2660]),diff_step=1e-5,ftol=1e-10,xtol=1e-10,gtol=1e-10)
  frequencies=fit.x;coefficients=np.linalg.lstsq(matrix(frequencies,t),x[indices],rcond=None)[0]
  tail=np.zeros_like(y)
  for k,f in enumerate(frequencies):
   envelope=np.exp(-np.maximum(0,times-anchor)/(6.5 if k<2 else 1.0));phase=2*np.pi*f*(times-anchor)
   tail+=(np.cos(phase)[:,None]*coefficients[2*k]+np.sin(phase)[:,None]*coefficients[2*k+1])*envelope[:,None]
  start=round(anchor*rate);end=round(join_end*rate);y[:start]=x[:start];y[start:]=tail[start:]
  weight=(.5-.5*np.cos(np.pi*np.arange(end-start)/(end-start)))[:,None]
  y[start:end]=x[start:end]*(1-weight)+tail[start:end]*weight
  lead=round(.14*rate);fade=round(.01*rate);y[:lead]=0;y[lead:lead+fade]*=(.5-.5*np.cos(np.pi*np.arange(fade)/fade))[:,None]
  extra={'tailMethod':'source-fitted bell modes','tailModeFrequenciesHz':frequencies.tolist(),'attackStartSeconds':.14,'attackFadeSeconds':.01,'attackPreservedUntilSeconds':anchor,'tailJoinEndSeconds':join_end}
 else:
  start=1.2;overlap=.6;search=.5;n=round(overlap*rate);duration=20-start
  segment=x[round(start*rate):];target=duration+search
  filters=f'rubberband=tempo={len(segment)/rate/target:.12f}:pitch=1:transients=smooth:detector=soft:phase=laminar:window=long:smoothing=on:channels=together,apad=whole_dur={target},atrim=duration={target}'
  raw=subprocess.check_output(['ffmpeg','-v','error','-f','f32le','-ar',str(rate),'-ac',str(channels),'-i','-','-af',filters,'-f','f32le','-'],input=segment.astype(np.float32).tobytes())
  tail=np.frombuffer(raw,np.float32).reshape(-1,channels).astype(float)
  reference=x[round(start*rate):round(start*rate)+n];candidates=tail[:round(search*rate)+n]
  dots=sum(correlate(candidates[:,c],reference[:,c],mode='valid',method='fft')for c in range(channels))
  power=np.concatenate(([0.],np.cumsum((candidates*candidates).sum(axis=1))));norm=power[n:]-power[:-n]
  scores=dots/np.sqrt(norm*(reference*reference).sum());offset=int(np.argmax(scores));gain=dots[offset]/norm[offset]
  s=round(start*rate);tail=tail[offset:offset+len(y)-s]*gain;y[:s]=x[:s];y[s:s+len(tail)]=tail
  weight=(.5-.5*np.cos(np.pi*np.arange(n)/n))[:,None];y[s:s+n]=x[s:s+n]*(1-weight)+tail[:n]*weight
  # A short, deterministic mallet strike: damped inharmonic modes, no hiss,
  # random noise, distortion or time-stretch on the attack.
  strike_time=np.maximum(0,times-.008);strike_gate=(times>=.008)*(.5-.5*np.cos(np.pi*np.minimum(1,strike_time/.002)))
  strike=np.zeros(len(times))
  for frequency,amplitude,decay in[(1315,.035,.04),(1978,.024,.03),(2817,.016,.02),(4098,.008,.012)]:
   strike+=amplitude*np.sin(2*np.pi*frequency*strike_time)*np.exp(-strike_time/decay)*strike_gate
  y[:,0]+=strike
  fade=round(.005*rate);y[:fade]*=(.5-.5*np.cos(np.pi*np.arange(fade)/fade))[:,None]
  extra={'tailMethod':'phase-aligned stretched ring','tailJoinCorrelation':float(scores[offset]),'attackStartSeconds':0,'attackFadeSeconds':.005,'attackPreservedUntilSeconds':start,'tailJoinEndSeconds':start+overlap,'strike':'short damped inharmonic mallet modes; no noise'}
 i=round(2*rate);j=round(19.8*rate);y[i:j]*=(.5+.5*np.cos(np.pi*np.arange(j-i)/(j-i)))[:,None];y[j:]=0
 y*=.5/np.max(abs(y));target=ROOT/'app/src/main/res/raw'/f'{name}.flac'
 subprocess.run(['ffmpeg','-y','-v','error','-f','f32le','-ar',str(rate),'-ac',str(channels),'-i','-','-c:a','flac','-sample_fmt','s16','-compression_level','8',str(target)],input=y.astype(np.float32).tobytes(),check=True)
 target.with_suffix('.ogg').unlink(missing_ok=True)
 return {'name':name,'file':target.name,'source':filename,'sourceSha256':hashlib.sha256(source.read_bytes()).hexdigest(),'originalSeconds':len(x)/rate,'seconds':20,'fadeStartSeconds':2,'fadeEndSeconds':19.8,'sampleRate':rate,'channels':channels,'sha256':hashlib.sha256(target.read_bytes()).hexdigest(),'lossless':True,'bytes':target.stat().st_size,**extra}
