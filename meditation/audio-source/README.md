# Sound sources

The four WAVs are the original Meditation 1.0.4 synthesized presets, retained for rebuilding. `zen_tone_original.mp3` is the user-supplied `alex_jauk-zen-tone-mid-high-202557.mp3` (Alex Jauk, as credited in the supplied filename).

`../tools/extend-sounds.py` preserves the initial half-second strike, stretches the ringing tail without shifting pitch, crossfades the join and applies a raised-cosine fade from 2 to 19.8 seconds followed by 0.2 seconds of silence. A limiter catches processing peaks. Outputs are Ogg Vorbis; Zen tone keeps the original recording’s stereo channels. These source files are not packaged in the app. `processing.json` records source/output hashes and processing parameters.

Zen tone is stretched continuously from the cleaned original recording, with no separately spliced attack. The previous attack/tail crossfade caused phase cancellation and a short loudness dip around 400ms; filtering sample spikes alone did not remove it. A zero-phase 3kHz source cleanup suppresses sharp attack noise before time stretching. The first 160ms of source noise is omitted, and the output has 140ms of silence followed by a 650ms raised-cosine fade-in. The twenty-second output still fades from 2 to 19.8 seconds, then ends in silence.

Zen tone is packaged as lossless 16-bit stereo FLAC to avoid introducing another lossy encoding stage. Android supports this format natively; its resource name and saved sound selection are unchanged. The other four Ogg presets remain byte-for-byte unchanged. `audio-check.py` checks the decoded full-rate stereo envelope as well as sample jumps, so the previous loudness dip fails verification. Rebuild only Zen tone with `python3 tools/extend-sounds.py --only zen_tone`.
