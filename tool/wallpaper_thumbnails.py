"""Génère la miniature du fond d'écran animé Circuit
(res/drawable-nodpi/wallpaper_thumb_circuit.png) ; celles des autres fonds
sont dessinées par les scènes elles-mêmes (tool/scenes/render.sh --thumbs), montrées par le sélecteur de
fonds d'écran d'Android. Couleurs fixes : une miniature n'est pas teintée.

    python3 tool/wallpaper_thumbnails.py
"""
import math

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


if __name__ == '__main__':
    import os
    os.makedirs(OUT, exist_ok=True)
    circuit().save(OUT + 'wallpaper_thumb_circuit.png', optimize=True)
