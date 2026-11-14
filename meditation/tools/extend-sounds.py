#!/usr/bin/env python3
"""Rebuild pitch-preserving 20-second preset decays. Needs FFmpeg + rubberband."""
from pathlib import Path
from array import array
import subprocess, json, hashlib, sys, argparse, math
ROOT=Path(__file__).resolve().parents[1]
SOURCES={'soft_bell':'soft_bell.wav','warm_bowl':'warm_bowl.wav','gentle_chime':'gentle_chime.wav','temple_gong':'temple_gong.wav','zen_tone':'zen_tone_original.mp3'}
parser=argparse.ArgumentParser();parser.add_argument('--only',choices=list(SOURCES));args=parser.parse_args()
existing={v['name']:v for v in json.loads((ROOT/'audio-source/processing.json').read_text())} if (ROOT/'audio-source/processing.json').exists() else {}
report=[]
for name,filename in SOURCES.items():
 if args.only and name!=args.only:
  report.append(existing[name]);continue
 source=ROOT/'audio-source'/filename
 meta=json.loads(subprocess.check_output(['ffprobe','-v','error','-show_entries','format=duration:stream=sample_rate,channels','-of','json',str(source)]))
 duration=float(meta['format']['duration']);stream=meta['streams'][0];rate=int(stream['sample_rate']);channels=stream['channels']
 tail_end=duration
 fade_start=2
 attack_start=.14 if name=='zen_tone' else 0
 attack_fade=.10 if name=='zen_tone' else .005
 quality=8 if name=='zen_tone' else 6
 filters=(f'[0:a]asplit=2[attack][ring];[attack]atrim=end=0.5,asetpts=PTS-STARTPTS[head];'
          f'[ring]atrim=start=0.35:end={tail_end},asetpts=PTS-STARTPTS,rubberband=tempo={(tail_end-.35)/19.65:.10f}:pitch=1:transients=smooth:detector=soft:phase=laminar:window=long:smoothing=on:channels=together,apad=whole_dur=19.65,atrim=duration=19.65[tail];'
          f'[head][tail]acrossfade=d=0.15:c1=qsin:c2=qsin,apad=whole_dur=20,atrim=duration=20,afade=t=in:st={attack_start}:d={attack_fade}:curve=hsin,afade=t=out:st={fade_start}:d={19.8-fade_start}:curve=hsin,alimiter=limit=0.8:level=false:latency=true[out]')
 raw=subprocess.check_output(['ffmpeg','-v','error','-i',str(source),'-filter_complex',filters,'-map','[out]','-ar',str(rate),'-ac',str(channels),'-f','f32le','-'])
 samples=array('f');samples.frombytes(raw)
 if sys.byteorder!='little':samples.byteswap()
 peak=max(abs(v) for v in samples);gain=.5/peak
 scaled=array('f',(v*gain for v in samples))
 if name=='zen_tone':
  # Zero-phase FIR removes the opening's isolated high-frequency spikes.
  # Blend back to the unmodified ring between 600 and 800ms.
  taps=129;half=taps//2;cutoff=3000/rate
  kernel=[]
  for k in range(taps):
   offset=k-half
   sinc=2*cutoff if offset==0 else math.sin(2*math.pi*cutoff*offset)/(math.pi*offset)
   kernel.append(sinc*(.5-.5*math.cos(2*math.pi*k/(taps-1))))
  total=sum(kernel);kernel=[v/total for v in kernel]
  original=list(scaled);frames=len(original)//channels
  for frame in range(min(frames,int(.8*rate))):
   t=frame/rate;weight=1 if t<=.6 else .5*(1+math.cos(math.pi*(t-.6)/.2))
   for channel in range(channels):
    smooth=sum(coef*original[(frame+k-half)*channels+channel] for k,coef in enumerate(kernel) if 0<=frame+k-half<frames)
    index=frame*channels+channel;scaled[index]=smooth*weight+original[index]*(1-weight)
 if sys.byteorder!='little':scaled.byteswap()
 target=ROOT/'app/src/main/res/raw'/f'{name}.ogg'
 subprocess.run(['ffmpeg','-y','-v','error','-f','f32le','-ar',str(rate),'-ac',str(channels),'-i','-','-c:a','libvorbis','-q:a',str(quality),str(target)],input=scaled.tobytes(),check=True)
 report.append({'name':name,'source':filename,'sourceSha256':hashlib.sha256(source.read_bytes()).hexdigest(),'originalSeconds':duration,'seconds':20,'fadeStartSeconds':fade_start,'tailSourceStartSeconds':.35,'tailSourceEndSeconds':tail_end,'fadeEndSeconds':19.8,'sampleRate':rate,'channels':channels,'sha256':hashlib.sha256(target.read_bytes()).hexdigest(),'attackStartSeconds':attack_start,'attackFadeSeconds':attack_fade,'vorbisQuality':quality,'openingLowPassHz':3000 if name=='zen_tone' else None,'openingBlendEndSeconds':.8 if name=='zen_tone' else None,'bytes':target.stat().st_size})
(ROOT/'audio-source/processing.json').write_text(json.dumps(report,indent=2)+'\n')
print(json.dumps(report,indent=2))
