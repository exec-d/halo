"""Génère les éléments lumineux des widgets (res/drawable-xxhdpi).

Tout est dessiné en blanc avec son halo : le widget les teinte à la couleur
d'accent du téléphone (android:tint), ce qui teinte le halo avec.

    pip install pillow
    python3 tool/clock_assets.py <flutter>/bin/cache/artifacts/material_fonts/MaterialIcons-Regular.otf
"""
from PIL import Image, ImageDraw, ImageFilter, ImageFont

OUT = 'android/app/src/main/res/drawable-xxhdpi/'
DP = 3  # xxhdpi


def glow(core: Image.Image, radius_dp: float) -> Image.Image:
    """Superpose le tracé à deux halos flous de son propre alpha."""
    alpha = core.split()[3]
    halo = alpha.filter(ImageFilter.GaussianBlur(radius_dp * DP))
    wide = alpha.filter(ImageFilter.GaussianBlur(radius_dp * DP * 2))
    out = Image.new('RGBA', core.size, (255, 255, 255, 0))
    for layer, strength in ((wide, 0.5), (halo, 0.9)):
        a = layer.point(lambda v: int(v * strength))
        out.alpha_composite(Image.merge('RGBA', (*[Image.new('L', core.size, 255)] * 3, a)))
    out.alpha_composite(core)
    return out


def canvas(w_dp: float, h_dp: float):
    img = Image.new('RGBA', (int(w_dp * DP), int(h_dp * DP)), (255, 255, 255, 0))
    return img, ImageDraw.Draw(img)


def divider(vertical: bool):
    long, thick, pad = 120, 2, 6
    w, h = (thick + 2 * pad, long) if vertical else (long, thick + 2 * pad)
    img, d = canvas(w, h)
    if vertical:
        d.rectangle((pad * DP, 0, (pad + thick) * DP - 1, h * DP), fill='white')
    else:
        d.rectangle((0, pad * DP, w * DP, (pad + thick) * DP - 1), fill='white')
    return glow(img, 2)


def event_bar():
    """Trait vertical d'un événement, teint à la couleur de son agenda."""
    pad = 5
    img, d = canvas(3 + 2 * pad, 40)
    d.rectangle((pad * DP, pad * DP, (pad + 3) * DP - 1, (40 - pad) * DP), fill='white')
    return glow(img, 1.5)


def dotted_divider():
    """Séparateur de colonnes en pointillé."""
    pad = 5
    img, d = canvas(2 + 2 * pad, 120)
    y = 0
    while y < 120 * DP:
        d.rectangle((pad * DP, y, (pad + 2) * DP - 1, y + 2 * DP - 1), fill='white')
        y += 6 * DP
    return glow(img, 1.5)


def icon(font_path: str, codepoint: int):
    """Une icône Material Icons (codes de la police du SDK Flutter), avec son halo."""
    size, pad = 24, 5
    img, d = canvas(size + 2 * pad, size + 2 * pad)
    font = ImageFont.truetype(font_path, size * DP)
    d.text((pad * DP, pad * DP), chr(codepoint), font=font, fill='white')
    return glow(img, 1.5)


# Nom du fichier (icon_<nom>.png) -> code dans MaterialIcons-Regular.otf.
ICONS = {
    'sunny': 0xE6D9, 'night': 0xE430, 'partly_cloudy': 0xE6D5, 'cloud': 0xE16F,
    'fog': 0xF0505, 'drizzle': 0xE2E2, 'rain': 0xF05A2, 'showers': 0xE68A,
    'snow': 0xE037, 'storm': 0xF07CB, 'wind': 0xE064,
    'flashlight_on': 0xE295, 'flashlight_off': 0xE294, 'wifi': 0xE6E7,
    'bluetooth': 0xE0E4, 'volume': 0xE6C5, 'camera': 0xE4B6,
    'sunrise': 0xE6DB, 'moon': 0xE1B0, 'hourglass': 0xE323, 'event': 0xE23E,
}


def moon(phase: int):
    """Lune à la phase [phase] sur 8 (0 : nouvelle, 4 : pleine), vue de
    l'hémisphère nord : elle croît par la droite."""
    import math
    size, pad = 24, 5
    img, d = canvas(size + 2 * pad, size + 2 * pad)
    r = size * DP / 2
    c = (pad + size / 2) * DP
    d.ellipse((c - r, c - r, c + r, c + r), outline='white', width=int(1.2 * DP))
    k = math.cos(2 * math.pi * phase / 8)
    px = img.load()
    for yy in range(img.size[1]):
        for xx in range(img.size[0]):
            x, y = (xx - c) / r, (yy - c) / r
            if x * x + y * y > 1:
                continue
            w = math.sqrt(1 - y * y)
            # Terminateur en x = w·cos(2π·phase) : éclairé à droite en croissant.
            lit = x > w * k if phase <= 4 else x < -w * k
            if lit:
                px[xx, yy] = (255, 255, 255, 255)
    return glow(img, 1.5)


if __name__ == '__main__':
    import sys
    divider(True).save(OUT + 'clock_divider_vertical.png')
    divider(False).save(OUT + 'clock_divider_horizontal.png')
    event_bar().save(OUT + 'agenda_event_bar.png')
    dotted_divider().save(OUT + 'agenda_divider_dotted.png')
    # MaterialIcons-Regular.otf : livrée avec le SDK Flutter
    # (bin/cache/artifacts/material_fonts/), licence Apache 2.0.
    icon(sys.argv[1], 0xE072).save(OUT + 'clock_alarm.png')
    for name, codepoint in ICONS.items():
        icon(sys.argv[1], codepoint).save(OUT + f'icon_{name}.png')
    for phase in range(8):
        moon(phase).save(OUT + f'moon_{phase}.png')
