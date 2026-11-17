#!/usr/bin/env python3
"""Rebuild the three unapproved presets while retaining approved bell files exactly."""
from pathlib import Path
import argparse,json,hashlib
from clean_presets import rebuild
ROOT=Path(__file__).resolve().parents[1]
parser=argparse.ArgumentParser();parser.add_argument('--only',choices=['soft_bell','gentle_chime','temple_gong']);args=parser.parse_args();path=ROOT/'audio-source/processing.json';records=json.loads(path.read_text());locked=json.loads((ROOT/'audio-source/protected-sounds.json').read_text())
for name,expected in locked.items():assert hashlib.sha256((ROOT/'app/src/main/res/raw'/name).read_bytes()).hexdigest()==expected
for i,item in enumerate(records):
 if item['name']in ['soft_bell','gentle_chime','temple_gong']and(not args.only or args.only==item['name']):records[i]=rebuild(item['name'])
for name,expected in locked.items():assert hashlib.sha256((ROOT/'app/src/main/res/raw'/name).read_bytes()).hexdigest()==expected
path.write_text(json.dumps(records,indent=2)+'\n');print(json.dumps(records,indent=2))
