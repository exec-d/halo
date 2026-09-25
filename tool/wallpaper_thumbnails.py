"""Génère les miniatures des trois fonds d'écran animés
(res/drawable-nodpi/wallpaper_thumb_*.png), montrées par le sélecteur de
fonds d'écran d'Android. Couleurs fixes : une miniature n'est pas teintée.

    python3 tool/wallpaper_thumbnails.py
"""
import math
import random

from PIL import Image, ImageDraw, ImageFilter

OUT = 'android/app/src/main/res/drawable-nodpi/'
W, H = 360, 640
BG = (5, 7, 13)
LINE = (242, 193, 120)
CORE = (255, 236, 205)


def glow(img):
    blur = img.filter(ImageFilter.GaussianBlur(6))
    return Image.blend(Image.new('RGB', img.size, BG), Image.composite(blur, img, Image.new('L', img.size, 90)), 1.0)


def circuit():
    img = Image.new('RGB', (W, H), BG)
    d = ImageDraw.Draw(img)
    d.rounded_rectangle((10, 10, W - 10, H - 10), radius=40, outline=LINE, width=2)
    d.rounded_rectangle((25, 30, W - 25, 230), radius=12, outline=LINE, width=2)
    d.rounded_rectangle((40, 260, W - 70, 530), radius=16, outline=LINE, width=2)
    d.rectangle((52, 420, W - 82, 518), fill=(60, 45, 25))
    for r in range(90, 20, -12):
        d.ellipse((W / 2 - 15 - r, 395 - r, W / 2 - 15 + r, 395 + r), outline=(90, 70, 40), width=1)
    d.rectangle((150, 80, 230, 160), outline=CORE, width=3)
    d.rectangle((170, 100, 210, 140), fill=(90, 70, 40))
    for cx, r in ((280, 32), (230, 22)):
        d.ellipse((cx - r, 70 - r, cx + r, 70 + r), outline=CORE, width=2)
    for i in range(4):
        y = 95 + i * 10
        d.line((150, y, 60 + i * 8, y, 60 + i * 8, 200), fill=LINE, width=1)
        d.line((230, y + 5, 320 - i * 6, y + 5, 320 - i * 6, 590), fill=LINE, width=1)
    d.rounded_rectangle((25, 560, W - 25, 620), radius=12, outline=LINE, width=2)
    return glow(img)


def horizon():
    img = Image.new('RGB', (W, H), BG)
    d = ImageDraw.Draw(img)
    hy = 400
    for y in range(hy):
        t = y / hy
        d.line((0, y, W, y), fill=tuple(int(BG[i] + (c - BG[i]) * t * 0.35) for i, c in enumerate(LINE)))
    r = 80
    # Le soleil rayé, les bandes ne débordant pas du disque.
    mask = Image.new('L', (W, H), 0)
    m = ImageDraw.Draw(mask)
    m.ellipse((W / 2 - r, 250 - r, W / 2 + r, 250 + r), fill=255)
    for k in range(6):
        y = 250 + r * (0.1 + k * 0.16)
        m.rectangle((W / 2 - r, y, W / 2 + r, y + r * (0.03 + k * 0.02)), fill=0)
    img.paste(Image.new('RGB', (W, H), LINE), (0, 0), mask)
    rnd = random.Random(3)
    for base, amp in ((hy - 30, 45), (hy - 10, 60)):
        pts = [(-20, hy)]
        x = -20
        while x < W + 40:
            pts.append((x, base - rnd.random() * amp))
            x += rnd.randint(30, 60)
        pts.append((W + 40, hy))
        d.polygon(pts, fill=BG, outline=LINE)
    for k in range(1, 12):
        y = hy + (H - hy) * (k / 11) ** 2
        d.line((0, y, W, y), fill=LINE, width=1)
    for k in range(-8, 9):
        d.line((W / 2, hy, W / 2 + k * 55, H), fill=LINE, width=1)
    d.line((0, hy, W, hy), fill=CORE, width=2)
    return glow(img)


def sky():
    img = Image.new('RGB', (W, H), BG)
    d = ImageDraw.Draw(img)
    rnd = random.Random(8)
    for _ in range(120):
        x, y, s = rnd.random() * W, rnd.random() * 480, rnd.random()
        d.ellipse((x - s, y - s, x + s, y + s), fill=tuple(int(c * (0.4 + 0.6 * s)) for c in CORE))
    # La Grande Ourse, pour qu'on la reconnaisse.
    dipper = [(60, 150), (110, 130), (150, 150), (190, 165), (240, 150), (270, 200), (225, 215), (190, 165)]
    d.line(dipper, fill=LINE, width=2)
    for x, y in dipper:
        d.ellipse((x - 4, y - 4, x + 4, y + 4), fill=CORE)
    orion = [(120, 330), (170, 320), (150, 380), (160, 382), (170, 384), (130, 440), (190, 445)]
    for a, b in ((0, 2), (1, 4), (2, 3), (3, 4), (2, 5), (4, 6)):
        d.line((orion[a], orion[b]), fill=LINE, width=2)
    for x, y in orion:
        d.ellipse((x - 3, y - 3, x + 3, y + 3), fill=CORE)
    d.ellipse((260, 60, 300, 100), fill=CORE)
    d.ellipse((248, 56, 288, 96), fill=BG)
    d.rectangle((0, 520, W, H), fill=BG)
    d.line((0, 540, W, 505), fill=LINE, width=2)
    return glow(img)


if __name__ == '__main__':
    import os
    os.makedirs(OUT, exist_ok=True)
    circuit().save(OUT + 'wallpaper_thumb_circuit.png', optimize=True)
    horizon().save(OUT + 'wallpaper_thumb_horizon.png', optimize=True)
    sky().save(OUT + 'wallpaper_thumb_sky.png', optimize=True)
