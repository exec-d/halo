"""Génère le cadran et les aiguilles néon de l'horloge analogique
(res/drawable-xxhdpi/analog_*.png), en blanc avec leur halo : le widget les
teinte aux couleurs du téléphone (Android 12+).

Les trois images ont la même taille et le même centre : AnalogClock les
superpose, et fait tourner les aiguilles autour du centre.

    python3 tool/analog_assets.py
"""
import math

from PIL import Image, ImageDraw, ImageFilter

OUT = 'android/app/src/main/res/drawable-xxhdpi/'
DP = 3
SIZE = 160  # dp


def glow(core, radius_dp):
    alpha = core.split()[3]
    out = Image.new('RGBA', core.size, (255, 255, 255, 0))
    for blur, strength in ((radius_dp * 2, 0.45), (radius_dp, 0.85)):
        a = alpha.filter(ImageFilter.GaussianBlur(blur * DP)).point(lambda v: int(v * strength))
        out.alpha_composite(Image.merge('RGBA', (*[Image.new('L', core.size, 255)] * 3, a)))
    out.alpha_composite(core)
    return out


def canvas():
    img = Image.new('RGBA', (SIZE * DP, SIZE * DP), (255, 255, 255, 0))
    return img, ImageDraw.Draw(img), SIZE * DP / 2


def dial():
    img, d, c = canvas()
    r = c - 8 * DP
    d.ellipse((c - r, c - r, c + r, c + r), outline='white', width=int(1.2 * DP))
    for tick in range(60):
        a = math.radians(tick * 6 - 90)
        hour = tick % 5 == 0
        inner = r - (9 if tick % 15 == 0 else 6 if hour else 3) * DP
        outer = r - 1.5 * DP
        width = int((2.2 if hour else 1) * DP)
        alpha = 255 if hour else 120
        d.line((c + math.cos(a) * inner, c + math.sin(a) * inner, c + math.cos(a) * outer, c + math.sin(a) * outer),
               fill=(255, 255, 255, alpha), width=width)
    return glow(img, 2)


def hand(length_dp, width_dp, tail_dp, center=False):
    img, d, c = canvas()
    w = width_dp * DP
    d.rounded_rectangle((c - w / 2, c - length_dp * DP, c + w / 2, c + tail_dp * DP), radius=w / 2, fill='white')
    if center:
        r = 4 * DP
        d.ellipse((c - r, c - r, c + r, c + r), fill='white')
    return glow(img, 1.5)


if __name__ == '__main__':
    dial().save(OUT + 'analog_dial.png')
    hand(40, 5, 8).save(OUT + 'analog_hour.png')
    hand(60, 3, 10, center=True).save(OUT + 'analog_minute.png')
