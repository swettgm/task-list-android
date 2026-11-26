#!/usr/bin/env python3
"""Render original steady noise, a bass hum, and a looped field recording."""
from pathlib import Path
import hashlib
import json
import subprocess
import wave

import numpy as np

ROOT = Path(__file__).resolve().parents[1]
RATE, SECONDS = 24000, 24
FRAMES = RATE * SECONDS
FREQUENCY = np.fft.rfftfreq(FRAMES, 1 / RATE)
SEED = 2026112605
random = np.random.default_rng(SEED)


def level(audio, target, window=.08):
    frequency = FREQUENCY.reshape((-1,) + (1,) * (audio.ndim - 1))
    smoothing = np.exp(-.5 * (2 * np.pi * frequency * window) ** 2)
    # Circular smoothing applies the same gain rule at the loop boundary.
    for _ in range(3):
        energy = np.fft.irfft(np.fft.rfft(audio**2, axis=0) * smoothing, n=FRAMES, axis=0)
        audio /= np.sqrt(np.maximum(energy.mean(axis=-1, keepdims=True) if audio.ndim == 2 else energy, 1e-8))
    return audio * (target / np.sqrt(np.mean(audio**2)))


def noise(power, low, target):
    spectrum = random.normal(size=len(FREQUENCY)) + 1j * random.normal(size=len(FREQUENCY))
    weights = np.maximum(FREQUENCY, low)**(-power / 2)
    weights *= np.minimum(1, FREQUENCY / low)**3
    weights[0] = 0
    return level(np.fft.irfft(spectrum * weights, n=FRAMES), target)


def record_rain():
    source = ROOT / 'audio-source/soft-rain-barkenov.mp3'
    info = json.loads((source.parent / 'rain-recording.json').read_text())
    assert hashlib.sha256(source.read_bytes()).hexdigest() == info['sha256']
    overlap = 3 * RATE
    raw = subprocess.check_output([
        'ffmpeg', '-v', 'error', '-ss', '29', '-i', str(source), '-t', '27',
        '-af', 'highpass=f=120,lowpass=f=9500', '-ar', str(RATE), '-ac', '2', '-f', 'f32le', '-',
    ])
    recording = np.frombuffer(raw, dtype='<f4').astype(float).reshape(-1, 2)
    assert len(recording) == FRAMES + overlap
    audio = recording[overlap:].copy()
    phase = np.linspace(0, np.pi / 2, overlap)[:, None]
    audio[-overlap:] = audio[-overlap:] * np.cos(phase) + recording[:overlap] * np.sin(phase)
    audio = level(audio, .072, .44)
    # Soften isolated close drops while keeping the stereo rainfall texture.
    audio = .23 * np.tanh(audio / .23)
    return audio, dict(sourceFile=source.name, sourceSha256=info['sha256'],
                       sourcePage=info['sourcePage'], license=info['license'], crossfadeSeconds=3)


def encode(name, audio, description, **extra):
    channels = 1 if audio.ndim == 1 else audio.shape[1]
    path = ROOT / 'app/src/main/res/raw' / f'{name}.wav'
    with wave.open(str(path), 'wb') as stream:
        stream.setparams((channels, 2, RATE, FRAMES, 'NONE', 'not compressed'))
        stream.writeframes(np.rint(np.clip(audio, -1, 1) * 32767).astype('<i2').tobytes())
    return dict(name=name, file=path.name, sha256=hashlib.sha256(path.read_bytes()).hexdigest(),
                seconds=SECONDS, sampleRate=RATE, channels=channels, source=description, **extra)


if __name__ == '__main__':
    rain, attribution = record_rain()
    phase = 2 * np.pi * 55 * np.arange(FRAMES) / RATE
    hum = .14 * np.sin(phase) + .018 * np.sin(2 * phase) + .004 * np.sin(3 * phase)
    items = [
        encode('brown_noise', noise(2, 65, .14), 'Original circular, level-corrected brown noise', seed=SEED),
        encode('white_noise', noise(0, 25, .12), 'Original circular, level-corrected white noise', seed=SEED),
        encode('rain', rain, 'Natural garden rainfall by barkenov, CC0', **attribution),
        encode('bass_hum', hum, 'Original continuous 55 Hz hum with soft harmonics'),
    ]
    (ROOT / 'audio-source/ambient-processing.json').write_text(json.dumps(items, indent=2) + '\n')
