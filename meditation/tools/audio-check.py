#!/usr/bin/env python3
"""Decode packaged presets and check real duration, headroom and fade-to-silence."""
from pathlib import Path
from array import array
import subprocess, json, hashlib, math, sys
ROOT=Path(__file__).resolve().parents[1]

def decode(path):
 raw=subprocess.check_output(['ffmpeg','-v','error','-i',str(path),'-ar','24000','-ac','1','-f','f32le','-'])
 samples=array('f');samples.frombytes(raw)
 if sys.byteorder!='little':samples.byteswap()
 return samples

def rms(samples):return math.sqrt(sum(v*v for v in samples)/len(samples))
report=[]
for entry in json.loads((ROOT/'audio-source/processing.json').read_text()):
 path=ROOT/'app/src/main/res/raw'/f"{entry['name']}.ogg"
 assert hashlib.sha256(path.read_bytes()).hexdigest()==entry['sha256'],path.name
 source=ROOT/'audio-source'/entry['source'];assert hashlib.sha256(source.read_bytes()).hexdigest()==entry['sourceSha256']
 samples=decode(path);duration=len(samples)/24000;peak=max(abs(v) for v in samples)
 first=rms(samples[:24000]);middle=rms(samples[8*24000:10*24000]);late=rms(samples[14*24000:16*24000]);last=rms(samples[-24000:]);end=max(abs(v) for v in samples[-2400:])
 assert abs(duration-20)<.01,(path.name,duration)
 assert 0<peak<.8,(path.name,peak)
 assert first>.01 and middle>.0001 and late>.0001,(path.name,first,middle,late)
 assert last<first*.01 and end<.00003,(path.name,last,end)
 assert first>middle>late>last,(path.name,'gradual decay',first,middle,late,last)
 if entry['name']=='zen_tone':
  assert max(abs(v) for v in samples[:3120])<.00003 and rms(samples[:4800])<rms(samples[4800:9600])*.3,(path.name,'quiet lead-in and gentle onset')
  # Check full-rate stereo too: downsampling can mask very short HF clicks.
  raw=subprocess.check_output(['ffmpeg','-v','error','-i',str(path),'-ar','48000','-ac','2','-f','f32le','-'])
  native=array('f');native.frombytes(raw)
  if sys.byteorder!='little':native.byteswap()
  jump=max(abs(native[i]-native[i-2]) for i in range(2,int(.6*48000)*2))
  assert jump<.06,(path.name,'opening sample spike',jump)

 report.append({'name':entry['name'],'seconds':duration,'peak':peak,'firstSecondRms':first,'middleRms':middle,'lateRms':late,'lastSecondRms':last,'last100msPeak':end})
assert len(report)==5
out=ROOT/'build/audio-qa';out.mkdir(parents=True,exist_ok=True);(out/'report.json').write_text(json.dumps(report,indent=2)+'\n')
print(json.dumps({'passed':len(report),'audioChecks':report},indent=2))
