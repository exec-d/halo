"""Génère les miniatures des fonds d'écran animés
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


def grid():
    bg, cyan, orange = (2, 4, 10), (41, 231, 255), (255, 138, 31)
    img = Image.new('RGB', (W, H), bg)
    d = ImageDraw.Draw(img)
    hy, cx, f = 280, W / 2, W * 0.9
    for y in range(hy - 120, hy):
        a = (y - hy + 120) / 120
        d.line((0, y, W, y), fill=tuple(int(bg[i] + (cyan[i] - bg[i]) * a * 0.25) for i in range(3)))
    for i in range(-24, 25):
        d.line((cx + i * f / 0.45, hy + f / 0.45, cx + i * f / 60, hy + f / 60), fill=(20, 110, 130), width=1)
    z = 1.0
    while z < 60:
        y = hy + f / z
        d.line((0, y, W, y), fill=(20, 110, 130), width=1)
        z += 1

    def p(x, z, h=0):
        return (cx + x * f / z, hy + (1 - h) * f / z)
    for color, pts in ((orange, [(2, 3), (2, 7), (-1.5, 7), (-1.5, 11)]), (cyan, [(-1, 1.6), (-1, 4), (1.2, 4), (1.2, 6.5)])):
        for (x0, z0), (x1, z1) in zip(pts, pts[1:]):
            d.polygon([p(x0, z0), p(x1, z1), p(x1, z1, 0.35), p(x0, z0, 0.35)], fill=tuple(c // 3 for c in color))
            d.line((p(x0, z0, 0.35), p(x1, z1, 0.35)), fill=color, width=3)
        x, z = pts[-1]
        hx, hy2 = p(x, z, 0.17)
        d.ellipse((hx - 5, hy2 - 5, hx + 5, hy2 + 5), fill=(255, 255, 255))
    d.line((0, hy, W, hy), fill=cyan, width=2)
    return img


def megacity():
    img = Image.new('RGB', (W, H), (4, 5, 10))
    d = ImageDraw.Draw(img)
    for y in range(H):
        a = min(1, y / (H * 0.78))
        d.line((0, y, W, y), fill=(int(4 + 60 * a * a), int(5 + 25 * a * a), int(10 + 4 * a)))
    d.polygon([(100, 480), (200, 330), (240, 330), (340, 480)], fill=(20, 13, 16))
    rnd = random.Random(19)
    for layer, (base, tall, wide, color, lit) in enumerate(((470, 180, 22, (28, 18, 26), 0.1), (520, 260, 36, (15, 11, 18), 0.18), (660, 330, 60, (6, 5, 9), 0.08))):
        x = -20
        while x < W:
            w = wide * (0.6 + rnd.random() * 0.8)
            top = base - tall * (0.35 + rnd.random() * 0.65)
            d.rectangle((x, top, x + w, base), fill=color)
            wy = top + 6
            while wy < base - 6:
                wx = x + 4
                while wx < x + w - 4:
                    if rnd.random() < lit:
                        d.rectangle((wx, wy, wx + 2, wy + 1), fill=(255, 170, 70) if rnd.random() < 0.7 else (80, 210, 230))
                    wx += 6
                wy += 8
            x += w + 3
    d.rectangle((30, 230, 90, 350), fill=(200, 40, 170))
    for y in range(230, 350, 4):
        d.line((30, y, 90, y), fill=(120, 20, 100))
    d.polygon([(110, 520), (40, 0), (90, 0)], fill=(20, 45, 55))
    d.ellipse((215, 400, 245, 430), fill=(255, 150, 50))
    for _ in range(90):
        x, y = rnd.random() * W, rnd.random() * H
        d.line((x, y, x - 4, y + 18), fill=(90, 100, 115))
    return img


def code():
    img = Image.new('RGB', (W, H), (0, 6, 2))
    d = ImageDraw.Draw(img)
    rnd = random.Random(4)
    for c in range(24):
        x = 6 + c * 15
        head = rnd.randint(4, 44)
        length = rnd.randint(6, 26)
        for i in range(length):
            row = head - i
            if row < 0:
                continue
            fade = 1 - i / (length + 1)
            color = (215, 255, 225) if i == 0 else (0, int(255 * fade), int(70 * fade))
            y = row * 15
            # Un caractère stylisé : deux ou trois traits.
            if rnd.random() < 0.5:
                d.line((x, y, x + 8, y), fill=color, width=2)
            d.line((x + rnd.randint(0, 8), y, x + rnd.randint(0, 8), y + 11), fill=color, width=2)
            d.line((x, y + 6, x + 8, y + 6 + rnd.randint(-3, 3)), fill=color, width=2)
    return img


def neon():
    img = Image.new('RGB', (W, H), (7, 6, 11))
    d = ImageDraw.Draw(img)
    vx, vy, f = W / 2, H * 0.46, W * 0.55

    def p(x, y, z):
        return (vx + x * f / z, vy - y * f / z)
    for x in (-1.6, 1.6):
        d.polygon([p(x, -1, 0.8), p(x, 6, 0.8), p(x, 6, 16), p(x, -1, 16)], fill=(14, 10, 22))
    d.polygon([p(-1.6, -1, 0.8), p(1.6, -1, 0.8), p(1.6, -1, 16), p(-1.6, -1, 16)], fill=(10, 8, 16))
    rnd = random.Random(77)
    colors = [(252, 238, 10), (0, 240, 255), (255, 43, 214), (255, 0, 60)]
    signs = []
    for i in range(26):
        z = 1.4 + (i // 2) * 0.9 + rnd.random() * 0.4
        bottom = -0.1 + rnd.random() * 1.2
        signs.append((z, i % 2 == 0, bottom, bottom + 0.6 + rnd.random() * 1.6, rnd.choice(colors)))
    for z, left, bottom, top, color in sorted(signs, reverse=True):
        inner = -1.55 if left else 1.55
        outer = inner + 0.45 if left else inner - 0.45
        x0, y0 = p(inner, top, z)
        x1, y1 = p(outer, bottom, z)
        box = (min(x0, x1), y0, max(x0, x1), y1)
        d.rectangle(box, fill=(8, 6, 12), outline=color, width=max(1, int((box[2] - box[0]) * 0.06)))
        g = p(0, -1, z)[1]
        d.rectangle((box[0], g + (g - y1), box[2], g + (g - y1) + (y1 - y0) * 0.5), fill=tuple(c // 5 for c in color))
    for y in range(0, H, 3):
        d.line((0, y, W, y), fill=(0, 0, 0))
    d.rectangle((0, 400, 220, 404), fill=(252, 238, 10))
    return img


def sentinel():
    img = Image.new('RGB', (W, H), (0, 0, 0))
    d = ImageDraw.Draw(img)
    cx, cy, r = W / 2, H / 2, W * 0.36
    for i in range(40, 0, -1):
        a = i / 40
        rr = r * (1 + a * 0.6)
        d.ellipse((cx - rr, cy - rr, cx + rr, cy + rr), fill=(int(60 * (1 - a)), 0, 0))
    d.ellipse((cx - r, cy - r, cx + r, cy + r), fill=(30, 30, 34), outline=(200, 200, 208), width=8)
    lens = r * 0.66
    for i in range(30, 0, -1):
        rr = lens * i / 30
        a = 1 - i / 30
        d.ellipse((cx - rr, cy - rr, cx + rr, cy + rr), fill=(int(4 + 116 * a), int(8 * a), int(4 * a)))
    core = lens * 0.24
    for i in range(20, 0, -1):
        rr = core * i / 20
        a = 1 - i / 20
        d.ellipse((cx - rr, cy - rr, cx + rr, cy + rr), fill=(255, int(40 + 210 * a * a), int(20 + 190 * a * a * a)))
    d.ellipse((cx - lens * 0.62, cy - lens * 0.58, cx - lens * 0.2, cy - lens * 0.32), fill=(70, 30, 30))
    return img


if __name__ == '__main__':
    import os
    os.makedirs(OUT, exist_ok=True)
    circuit().save(OUT + 'wallpaper_thumb_circuit.png', optimize=True)
    for name in ('grid', 'megacity', 'code', 'neon', 'sentinel'):
        globals()[name]().save(OUT + 'wallpaper_thumb_' + name + '.png', optimize=True)
