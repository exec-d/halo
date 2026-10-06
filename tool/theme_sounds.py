"""Génère les sons des thèmes Halo (res/raw/theme_*.wav).

Des sons originaux, synthétisés : les mêmes recettes que la maquette de
l'écran Thèmes (oscillateurs, bruit filtré, enveloppes exponentielles, comme
WebAudio), rendues en WAV 16 bits mono à 44,1 kHz. Une sonnerie et une
alarme se répètent : elles finissent par un court silence.

    python3 tool/theme_sounds.py
"""
import math
import wave

import numpy as np

OUT = 'android/app/src/main/res/raw/'
RATE = 44100
rng = np.random.default_rng(1985)


class Track:
    def __init__(self, seconds):
        self.buf = np.zeros(int(seconds * RATE))

    def add(self, signal, t):
        start = int(t * RATE)
        end = min(len(self.buf), start + len(signal))
        if end > start:
            self.buf[start:end] += signal[: end - start]


def envelope(n, d, v, attack):
    """Montée exponentielle jusqu'à v en `attack`, puis descente jusqu'à presque rien à `d`."""
    t = np.arange(n) / RATE
    floor = 1e-4
    rise = floor * (v / floor) ** np.clip(t / attack, 0, 1)
    fall = v * (floor / v) ** np.clip((t - attack) / max(d - attack, 1e-6), 0, 1)
    return np.where(t < attack, rise, fall)


def wave_of(kind, phase):
    if kind == 'sine':
        return np.sin(2 * math.pi * phase)
    if kind == 'square':
        return np.sign(np.sin(2 * math.pi * phase)) * 0.6
    if kind == 'sawtooth':
        return (2 * (phase % 1) - 1) * 0.6
    if kind == 'triangle':
        return 2 * np.abs(2 * (phase % 1) - 1) - 1
    raise ValueError(kind)


def tone(track, t, d, f0, f1=None, kind='sine', v=0.4, attack=0.005, vibrato=None):
    f1 = f0 if f1 is None else f1
    n = int((d + 0.05) * RATE)
    time = np.arange(n) / RATE
    # Fréquence en rampe exponentielle sur d, puis tenue.
    freq = f0 * (f1 / f0) ** np.clip(time / d, 0, 1)
    if vibrato:
        rate, depth = vibrato
        freq = freq + depth * np.sin(2 * math.pi * rate * time)
    phase = np.cumsum(freq) / RATE
    track.add(wave_of(kind, phase) * envelope(n, d, v, attack), t)


def biquad(x, kind, f, q):
    w = 2 * math.pi * f / RATE
    alpha = math.sin(w) / (2 * q)
    cos = math.cos(w)
    if kind == 'bandpass':
        b = [alpha, 0, -alpha]
    else:  # lowpass
        b = [(1 - cos) / 2, 1 - cos, (1 - cos) / 2]
    a = [1 + alpha, -2 * cos, 1 - alpha]
    b = [c / a[0] for c in b]
    a = [c / a[0] for c in a]
    y = np.zeros_like(x)
    x1 = x2 = y1 = y2 = 0.0
    for i, xi in enumerate(x):
        yi = b[0] * xi + b[1] * x1 + b[2] * x2 - a[1] * y1 - a[2] * y2
        x2, x1, y2, y1 = x1, xi, y1, yi
        y[i] = yi
    return y


def noise(track, t, d, v=0.3, f=2000, q=1.0, kind='bandpass'):
    n = int(d * RATE)
    filtered = biquad(rng.uniform(-1, 1, n), kind, f, q)
    time = np.arange(n) / RATE
    gain = v * (1e-4 / v) ** (time / d)
    track.add(filtered * gain, t)


# ——— Circuit ———

def bus():
    s = Track(2.9)
    seq = [880, 1320, 1760, 1320, 990, 1480, 1980, 1480]
    for r in range(2):
        for i, f in enumerate(seq):
            tone(s, r * 1.1 + i * 0.09, 0.07, f, kind='square', v=0.12)
    return s


def pulse():
    s = Track(0.5)
    tone(s, 0, 0.12, 900, 2600, v=0.35)
    tone(s, 0.16, 0.12, 900, 2600, v=0.12)
    return s


def boot():
    s = Track(2.6)
    for i, f in enumerate([523, 659, 784, 1047, 1319]):
        tone(s, i * 0.16, 0.5, f, kind='triangle', v=0.22)
    tone(s, 0.9, 0.9, 1568, v=0.18)
    return s


# ——— Retour vers le futur ———

def jump():
    s = Track(3.0)
    tone(s, 0, 1.5, 90, 1400, kind='sawtooth', v=0.16, attack=0.3)
    for i in range(10):
        noise(s, 0.5 + i * 0.09 + rng.uniform(0, 0.04), 0.05, v=0.25, f=4000, q=2)
    noise(s, 1.55, 0.7, v=0.6, f=300, q=0.7, kind='lowpass')
    tone(s, 1.55, 0.6, 120, 40, v=0.5)
    return s


def flux():
    s = Track(0.8)
    for i, start in enumerate([0, 0.11, 0.22]):
        tone(s, start, 0.09, 2400 - i * 300, 700, kind='sawtooth', v=0.12)
        noise(s, start, 0.06, v=0.15, f=5000, q=3)
    tone(s, 0.34, 0.35, 660, v=0.25)
    return s


def timer():
    s = Track(3.2)
    for r in range(3):
        for i in range(4):
            tone(s, r * 0.9 + i * 0.13, 0.08, 2048, kind='square', v=0.1, attack=0.002)
    return s


# ——— Iron Man ———

def repulsor():
    s = Track(2.4)
    tone(s, 0, 1.1, 300, 3200, v=0.18, attack=0.5, vibrato=(18, 40))
    noise(s, 1.1, 0.55, v=0.7, f=1200, q=0.6)
    tone(s, 1.1, 0.45, 220, 60, kind='triangle', v=0.4)
    return s


def hud():
    s = Track(0.9)
    for f, start in [(1568, 0), (2349, 0.14)]:
        tone(s, start, 0.6, f, v=0.25)
        tone(s, start, 0.25, f * 2.76, v=0.05)
    return s


def reactor():
    s = Track(3.2)
    tone(s, 0, 2.6, 55, kind='triangle', v=0.25, attack=0.4)
    for i in range(4):
        v = 0.25 + i * 0.12
        tone(s, i * 0.62, 0.18, 90, 50, v=v)
        tone(s, i * 0.62 + 0.2, 0.16, 80, 45, v=v * 0.7)
    tone(s, 0.3, 2.2, 2637, v=0.04, attack=0.8)
    return s


SOUNDS = {
    'theme_circuit_ring': bus, 'theme_circuit_notification': pulse, 'theme_circuit_alarm': boot,
    'theme_flux_ring': jump, 'theme_flux_notification': flux, 'theme_flux_alarm': timer,
    'theme_arc_ring': repulsor, 'theme_arc_notification': hud, 'theme_arc_alarm': reactor,
}


def write(name, track):
    x = track.buf
    # Comme le compresseur de la maquette : un écrêtage doux, puis -1 dB crête.
    x = np.tanh(x * 1.6)
    x = x / max(1e-9, np.abs(x).max()) * 0.89
    # Pas de clic au début ni à la fin.
    fade = int(0.004 * RATE)
    x[:fade] *= np.linspace(0, 1, fade)
    x[-fade:] *= np.linspace(1, 0, fade)
    with wave.open(OUT + name + '.wav', 'wb') as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(RATE)
        w.writeframes((x * 32767).astype('<i2').tobytes())


if __name__ == '__main__':
    for name, make in SOUNDS.items():
        write(name, make())
        print(name)
