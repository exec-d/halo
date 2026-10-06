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


# ——— Physique quantique ———

def lcg(seed):
    """Le même hasard que la maquette (seeded en JavaScript)."""
    state = [seed]

    def next_value():
        state[0] = (state[0] * 1103515245 + 12345) % 2147483648
        return state[0] / 2147483648
    return next_value


def superposition():
    s = Track(2.9)
    tone(s, 0, 2.4, 660, v=0.18, attack=0.2)
    tone(s, 0, 2.4, 666, v=0.18, attack=0.2)
    for i, f in enumerate([880, 1320, 1760, 2200, 1760, 1320, 880]):
        tone(s, 0.4 + i * 0.17, 0.35, f, v=0.14)
    return s


def entangle():
    s = Track(0.8)
    for k, start in enumerate([0, 0.2]):
        tone(s, start, 0.45, 1046, v=0.25 / (k + 1))
        tone(s, start, 0.45, 1568, v=0.25 / (k + 1))
    return s


def collapse():
    s = Track(3.0)
    for r in range(2):
        b = r * 1.4
        for i, f in enumerate([4000, 2800, 1900, 1300, 900]):
            noise(s, b + i * 0.12, 0.16, v=0.3, f=f, q=2 + i * 2)
        tone(s, b + 0.62, 0.7, 880, v=0.3)
    return s


# ——— Intelligence artificielle ———

def inference():
    s = Track(2.7)
    r = lcg(7)
    scale = [523, 587, 659, 784, 880, 1047, 1175, 1319]
    for i in range(24):
        tone(s, i * 0.085 + (0.25 if i >= 12 else 0), 0.12, scale[int(r() * len(scale))], kind='triangle', v=0.16)
    return s


def token():
    s = Track(0.35)
    tone(s, 0, 0.06, 1200, 1500, v=0.3)
    tone(s, 0.1, 0.07, 1500, 1800, v=0.3)
    return s


def awaken():
    s = Track(3.0)
    for i, f in enumerate([220, 330, 440, 554, 660]):
        tone(s, i * 0.28, 2.4 - i * 0.28, f, kind='triangle', v=0.12, attack=0.15)
        noise(s, i * 0.28, 0.03, v=0.25, f=6000, q=4)
    return s


# ——— Énergie atomique ———

def chain():
    s = Track(3.0)
    t, gap = 0.0, 0.22
    while t < 1.7:
        noise(s, t, 0.012, v=0.6, f=3500, q=1.2)
        t += gap
        # Les clics se resserrent, sans descendre sous 12 ms d'écart.
        gap = max(gap * 0.86, 0.012)
    tone(s, 1.7, 0.8, 60, 45, kind='sawtooth', v=0.3, attack=0.05)
    noise(s, 1.7, 0.8, v=0.5, f=200, q=0.7, kind='lowpass')
    return s


def neutron():
    s = Track(0.7)
    noise(s, 0, 0.015, v=0.7, f=3500, q=1.2)
    tone(s, 0.05, 0.5, 1760, v=0.25)
    return s


def critical():
    s = Track(3.0)
    for i in range(6):
        tone(s, i * 0.42, 0.4, 554 if i % 2 else 440, kind='square', v=0.12, attack=0.02)
    return s


# ——— Fallout ———

def vaultdoor():
    s = Track(3.0)
    for r in range(2):
        tone(s, r * 0.55, 0.5, 330, 260, kind='sawtooth', v=0.16, attack=0.03)
    noise(s, 1.15, 0.9, v=0.35, f=5000, q=0.5)
    tone(s, 2.05, 0.35, 110, 45, v=0.5)
    noise(s, 2.05, 0.25, v=0.5, f=300, q=0.7, kind='lowpass')
    return s


def terminal():
    s = Track(0.5)
    for start in [0, 0.07, 0.15]:
        noise(s, start, 0.02, v=0.5, f=2500, q=2)
    tone(s, 0.26, 0.12, 880, kind='square', v=0.12, attack=0.002)
    return s


def geiger():
    s = Track(3.3)
    r = lcg(3)
    t = 0.0
    while t < 3:
        noise(s, t, 0.01, v=0.6, f=3500, q=1.2)
        t += 0.015 + r() * r() * 0.18
    return s


# ——— Ghost in the Shell ———

def dive():
    s = Track(2.8)
    for start in [0, 0.45, 0.9, 1.15, 1.6]:
        tone(s, start, 0.4, 95, 48, v=0.5)
        noise(s, start, 0.08, v=0.3, f=400, q=0.8, kind='lowpass')
    for f in [587, 698, 880]:
        tone(s, 0.3, 1.9, f, v=0.06, attack=0.6)
        tone(s, 0.3, 1.9, f * 1.006, v=0.06, attack=0.6)
    return s


def ghost():
    s = Track(1.0)
    noise(s, 0, 0.5, v=0.16, f=3000, q=1.5)
    tone(s, 0.18, 0.7, 1318, v=0.2, attack=0.08)
    return s


def sync():
    s = Track(2.9)
    for start in [0, 0.5, 1.0, 1.5, 2.0]:
        tone(s, start, 0.35, 90, 50, v=0.45)
    for i, f in enumerate([587, 659, 784, 880, 988, 1175, 1319, 1568]):
        tone(s, 0.25 + i * 0.25, 0.09, f, kind='square', v=0.08, attack=0.003)
    return s


SOUNDS = {
    'theme_circuit_ring': bus, 'theme_circuit_notification': pulse, 'theme_circuit_alarm': boot,
    'theme_flux_ring': jump, 'theme_flux_notification': flux, 'theme_flux_alarm': timer,
    'theme_arc_ring': repulsor, 'theme_arc_notification': hud, 'theme_arc_alarm': reactor,
    'theme_quantum_ring': superposition, 'theme_quantum_notification': entangle, 'theme_quantum_alarm': collapse,
    'theme_neural_ring': inference, 'theme_neural_notification': token, 'theme_neural_alarm': awaken,
    'theme_atom_ring': chain, 'theme_atom_notification': neutron, 'theme_atom_alarm': critical,
    'theme_vault_ring': vaultdoor, 'theme_vault_notification': terminal, 'theme_vault_alarm': geiger,
    'theme_ghost_ring': dive, 'theme_ghost_notification': ghost, 'theme_ghost_alarm': sync,
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
