"""Génère les images d'exemple des aperçus du lanceur des widgets système
détaillés (res/drawable-xxhdpi/sample_*.png).

Le lanceur montre ces aperçus sans lancer Halo : ils reprennent, en blanc,
les dessins de SystemGraphics.kt, que la mise en page teinte.

    python3 tool/sample_assets.py
"""
import math

from PIL import Image, ImageDraw

OUT = 'android/app/src/main/res/drawable-xxhdpi/'
DP = 3


def canvas(w, h):
    img = Image.new('RGBA', (int(w * DP), int(h * DP)), (255, 255, 255, 0))
    return img, ImageDraw.Draw(img)


def white(alpha=255):
    return (255, 255, 255, alpha)


def battery_chart():
    w, h = 300, 64
    img, d = canvas(w, h)
    top, bottom, now_x = 4 * DP, (h - 12) * DP, w * 0.8 * DP
    y = lambda v: bottom - (bottom - top) * v / 100
    x = lambda hours: now_x + hours / 24 * now_x
    for v in (0, 50, 100):
        for sx in range(0, w * DP, 5 * DP):
            d.line((sx, y(v), sx + 2 * DP, y(v)), fill=white(60), width=2)
    pts = [(-24, 64), (-22, 55), (-20, 47), (-18, 40), (-16.5, 34), (-15.9, 34), (-14.5, 62),
           (-13.2, 88), (-12, 100), (-9, 100), (-7, 94), (-5, 88), (-3, 85), (-1.5, 83), (0, 82)]
    for i in range(1, len(pts)):
        (h0, l0), (h1, l1) = pts[i - 1], pts[i]
        charging = 5 <= i - 1 <= 8
        d.polygon([(x(h0), bottom), (x(h0), y(l0)), (x(h1), y(l1)), (x(h1), bottom)], fill=white(120 if charging else 40))
    d.line([(x(a), y(b)) for a, b in pts], fill=white(), width=int(1.6 * DP), joint='curve')
    end = (w * DP, y(82 - 82 * min(1, 360 / 575)))
    steps = 8
    for k in range(steps):
        if k % 2 == 0:
            a = (now_x + (end[0] - now_x) * k / steps, y(82) + (end[1] - y(82)) * k / steps)
            b = (now_x + (end[0] - now_x) * (k + 1) / steps, y(82) + (end[1] - y(82)) * (k + 1) / steps)
            d.line((a, b), fill=white(170), width=int(1.4 * DP))
    r = 3 * DP
    d.ellipse((now_x - 2 * r, y(82) - 2 * r, now_x + 2 * r, y(82) + 2 * r), fill=white(70))
    d.ellipse((now_x - r, y(82) - r, now_x + r, y(82) + r), fill=white())
    return img


def dial():
    s = 110
    img, d = canvas(s, s)
    c = s * DP / 2
    ring = c - 12 * DP
    for hour in range(24):
        a = math.radians(hour * 15 - 90)
        long = hour % 6 == 0
        inner = c - (7 if long else 4) * DP
        outer = c - 1.5 * DP
        d.line((c + math.cos(a) * inner, c + math.sin(a) * inner, c + math.cos(a) * outer, c + math.sin(a) * outer),
               fill=white(220 if long else 90), width=DP)
    box = (c - ring, c - ring, c + ring, c + ring)
    d.arc(box, 0, 360, fill=white(45), width=5 * DP)
    for a, b in ((7.1, 7.6), (8.2, 8.5), (9.9, 10.3), (12.4, 13.2), (15.0, 15.2), (18.3, 19.4)):
        d.arc(box, a * 15 - 90, b * 15 - 90, fill=white(), width=5 * DP)
    a = math.radians(19.6 * 15 - 90)
    d.line((c + math.cos(a) * (ring - 8 * DP), c + math.sin(a) * (ring - 8 * DP),
            c + math.cos(a) * (ring + 8 * DP), c + math.sin(a) * (ring + 8 * DP)), fill=white(), width=int(1.6 * DP))
    return img


def data_chart():
    w, h = 300, 60
    img, d = canvas(w, h)
    days, quota = 30, 50.0
    bars = h * 0.22 * DP
    top, bottom = 12 * DP, h * DP - bars - 4 * DP
    usage = [0.42, 0.31, 0.65, 0.28, 0.19, 0.82, 0.54, 0.36, 0.30, 0.71, 0.48, 0.26, 0.39, 0.58, 0.44, 0.61, 0.33, 0.52]
    used = sum(usage)
    projected = used / len(usage) * days
    reach = max(projected, used)
    shown = quota <= reach * 1.6
    scale = (max(reach, quota) if shown else reach) * 1.12
    x = lambda day: w * DP * day / days
    y = lambda gb: bottom - (bottom - top) * gb / scale
    if shown:
        d.line((0, y(quota), w * DP, y(quota)), fill=white(150), width=DP)
        d.line((0, bottom, w * DP, y(quota)), fill=white(90), width=DP)
    total, pts = 0, [(0, bottom)]
    for i, gb in enumerate(usage):
        total += gb
        pts.append((x(i + 1), y(total)))
    d.polygon(pts + [(x(len(usage)), bottom)], fill=white(45))
    d.line(pts, fill=white(), width=int(1.8 * DP))
    d.line((x(len(usage)), y(total), w * DP, y(projected)), fill=white(170), width=int(1.4 * DP))
    slot = w * DP / days
    for i in range(days):
        cx = slot * i + slot / 2
        if i < len(usage):
            bh = max(1.5 * DP, (bars - 2 * DP) * usage[i] / max(usage))
            d.rectangle((cx - slot * 0.27, h * DP - bh, cx + slot * 0.27, h * DP), fill=white(255 if i == len(usage) - 1 else 150))
        else:
            d.ellipse((cx - DP, h * DP - 2.2 * DP, cx + DP, h * DP - 0.2 * DP), fill=white(90))
    return img


def cells(level):
    w, h = 11, 36
    img, d = canvas(w, h)
    cap, gap = 3 * DP, 2.5 * DP
    d.rounded_rectangle((w * 0.3 * DP, 0, w * 0.7 * DP, cap), radius=cap / 2, fill=white(150))
    top = cap + gap
    cell = (h * DP - top - gap * 4) / 5
    lit = (level + 10) // 20
    for i in range(5):
        b = h * DP - i * (cell + gap)
        box = (0, b - cell, w * DP - 1, b - 1)
        if i < lit:
            d.rounded_rectangle(box, radius=1.5 * DP, fill=white())
        else:
            d.rounded_rectangle(box, radius=1.5 * DP, outline=white(110), width=DP)
    return img


def glyph(kind):
    s = 28
    img, d = canvas(s, s)
    S = s * DP
    line = int(max(1.6 * DP, S * 0.07))
    if kind == 'headphones':
        d.arc((S * 0.15, S * 0.12, S * 0.85, S * 0.82), 180, 360, fill=white(), width=line)
        d.rounded_rectangle((S * 0.1, S * 0.48, S * 0.32, S * 0.88), radius=S * 0.08, fill=white())
        d.rounded_rectangle((S * 0.68, S * 0.48, S * 0.9, S * 0.88), radius=S * 0.08, fill=white())
    else:
        d.rounded_rectangle((S * 0.36, S * 0.04, S * 0.64, S * 0.24), radius=S * 0.04, fill=white())
        d.rounded_rectangle((S * 0.36, S * 0.76, S * 0.64, S * 0.96), radius=S * 0.04, fill=white())
        d.ellipse((S * 0.2, S * 0.2, S * 0.8, S * 0.8), outline=white(), width=line)
        d.line((S * 0.5, S * 0.5, S * 0.5, S * 0.34), fill=white(), width=line)
        d.line((S * 0.5, S * 0.5, S * 0.62, S * 0.56), fill=white(), width=line)
    return img


if __name__ == '__main__':
    battery_chart().save(OUT + 'sample_battery_chart.png')
    dial().save(OUT + 'sample_screen_dial.png')
    data_chart().save(OUT + 'sample_data_chart.png')
    cells(70).save(OUT + 'sample_cells_70.png')
    cells(45).save(OUT + 'sample_cells_45.png')
    glyph('headphones').save(OUT + 'sample_glyph_headphones.png')
    glyph('watch').save(OUT + 'sample_glyph_watch.png')
