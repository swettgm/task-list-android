#!/usr/bin/env python3
"""Rebuild pitch-preserving 20-second preset decays. Needs FFmpeg + rubberband."""
from pathlib import Path
from array import array
import subprocess, json, hashlib, sys, argparse
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
 tail_end=1.35 if name=='zen_tone' else duration
 fade_start=6 if name=='zen_tone' else 2
 filters=(f'[0:a]asplit=2[attack][ring];[attack]atrim=end=0.5,asetpts=PTS-STARTPTS[head];'
          f'[ring]atrim=start=0.35:end={tail_end},asetpts=PTS-STARTPTS,rubberband=tempo={(tail_end-.35)/19.65:.10f}:pitch=1:transients=smooth:detector=soft:phase=laminar:window=long:smoothing=on:channels=together,apad=whole_dur=19.65,atrim=duration=19.65[tail];'
          f'[head][tail]acrossfade=d=0.15:c1=qsin:c2=qsin,apad=whole_dur=20,atrim=duration=20,afade=t=in:st=0:d=0.005:curve=hsin,afade=t=out:st={fade_start}:d={19.8-fade_start}:curve=hsin,alimiter=limit=0.8:level=false:latency=true[out]')
 raw=subprocess.check_output(['ffmpeg','-v','error','-i',str(source),'-filter_complex',filters,'-map','[out]','-ar',str(rate),'-ac',str(channels),'-f','f32le','-'])
 samples=array('f');samples.frombytes(raw)
 if sys.byteorder!='little':samples.byteswap()
 peak=max(abs(v) for v in samples);gain=.5/peak
 scaled=array('f',(v*gain for v in samples))
 if sys.byteorder!='little':scaled.byteswap()
 target=ROOT/'app/src/main/res/raw'/f'{name}.ogg'
 subprocess.run(['ffmpeg','-y','-v','error','-f','f32le','-ar',str(rate),'-ac',str(channels),'-i','-','-c:a','libvorbis','-q:a','6',str(target)],input=scaled.tobytes(),check=True)
 report.append({'name':name,'source':filename,'sourceSha256':hashlib.sha256(source.read_bytes()).hexdigest(),'originalSeconds':duration,'seconds':20,'fadeStartSeconds':fade_start,'tailSourceStartSeconds':.35,'tailSourceEndSeconds':tail_end,'fadeEndSeconds':19.8,'sampleRate':rate,'channels':channels,'sha256':hashlib.sha256(target.read_bytes()).hexdigest(),'bytes':target.stat().st_size})
(ROOT/'audio-source/processing.json').write_text(json.dumps(report,indent=2)+'\n')
print(json.dumps(report,indent=2))
