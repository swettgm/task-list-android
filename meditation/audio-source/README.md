# Sound sources

The four WAVs are the original Meditation 1.0.4 synthesized presets, retained for rebuilding. `zen_tone_original.mp3` is the user-supplied `alex_jauk-zen-tone-mid-high-202557.mp3` (Alex Jauk, as credited in the supplied filename).

`../tools/extend-sounds.py` preserves the initial half-second strike, stretches the ringing tail without shifting pitch, crossfades the join and applies a raised-cosine fade from 2 to 19.8 seconds followed by 0.2 seconds of silence. A limiter catches processing peaks. Outputs are Ogg Vorbis; Zen tone keeps the original recording’s stereo channels. These source files are not packaged in the app. `processing.json` records source/output hashes and processing parameters.
