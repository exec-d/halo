"""Génère les éléments lumineux des widgets (res/drawable-xxhdpi).

Tout est dessiné en blanc avec son halo : le widget les teinte à la couleur
d'accent du téléphone (android:tint), ce qui teinte le halo avec.

    pip install pillow && python3 tool/clock_assets.py
"""
from PIL import Image, ImageDraw, ImageFilter

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


def bars(count: int = 7):
    """Pile de barres, puis hachures en biais, comme à droite de la maquette."""
    pad = 6
    img, d = canvas(16 + 2 * pad, count * 7 + 27 + 2 * pad)
    x0, x1 = pad * DP, (pad + 16) * DP
    y = pad * DP
    for _ in range(count):
        d.rectangle((x0, y, x1, y + 4 * DP), fill='white')
        y += 7 * DP
    y += 4 * DP
    for i in range(3):
        x = x0 + i * 6 * DP
        d.polygon([(x, y + 14 * DP), (x + 3 * DP, y + 14 * DP), (x + 8 * DP, y), (x + 5 * DP, y)], fill='white')
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


if __name__ == '__main__':
    divider(True).save(OUT + 'clock_divider_vertical.png')
    divider(False).save(OUT + 'clock_divider_horizontal.png')
    bars().save(OUT + 'clock_bars.png')
    bars(20).save(OUT + 'agenda_bars.png')
    event_bar().save(OUT + 'agenda_event_bar.png')
    dotted_divider().save(OUT + 'agenda_divider_dotted.png')
