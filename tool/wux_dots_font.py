"""Génère la police à points de l'horloge WUX.

Chaque chiffre est dessiné à la main sur une grille de 8 x 11, traits de deux
points, d'après la maquette. Lancer depuis la racine du dépôt :

    pip install fonttools && python3 tool/wux_dots_font.py
"""
import math
from fontTools.fontBuilder import FontBuilder
from fontTools.pens.ttGlyphPen import TTGlyphPen

G = {
'0': """
..####..
.######.
##....##
##....##
##....##
##....##
##....##
##....##
##....##
.######.
..####..""",
'1': """
...##...
..###...
.####...
...##...
...##...
...##...
...##...
...##...
...##...
...##...
.######.""",
'2': """
..####..
.######.
##....##
......##
.....##.
....##..
...##...
..##....
.##.....
########
########""",
'3': """
.######.
########
......##
.....##.
...###..
...####.
......##
......##
##....##
.######.
..####..""",
'4': """
.....##.
....###.
...####.
..##.##.
.##..##.
##...##.
########
########
.....##.
.....##.
.....##.""",
'5': """
########
########
##......
##......
######..
#######.
......##
......##
##....##
.######.
..####..""",
'6': """
..####..
.######.
##....##
##......
######..
#######.
##....##
##....##
##....##
.######.
..####..""",
'7': """
########
########
......##
.....##.
....##..
...##...
...##...
..##....
..##....
..##....
..##....""",
'8': """
..####..
.######.
##....##
##....##
.######.
.######.
##....##
##....##
##....##
.######.
..####..""",
'9': """
..####..
.######.
##....##
##....##
##....##
.#######
..######
......##
##....##
.######.
..####..""",
':': """
..
..
..
##
##
..
..
##
##
..
..""",
}
PITCH = 80          # pas de la grille, en unités (1000 par em)
DOT = 0.74 * PITCH  # diamètre d'un point
ROWS = 11

def circle(pen, cx, cy, r):
    n = 8
    k = r / math.cos(math.pi / n)
    pts = []
    for i in range(n):
        a = 2 * math.pi * i / n
        b = a + math.pi / n
        pts.append(((cx + r * math.cos(a)), (cy + r * math.sin(a))))
        pts.append(((cx + k * math.cos(b)), (cy + k * math.sin(b))))
    # contour quadratique fermé : on-curve / off-curve alternés
    pen.moveTo((round(pts[0][0]), round(pts[0][1])))
    for i in range(n):
        off = pts[2 * i + 1]
        on = pts[(2 * i + 2) % (2 * n)]
        pen.qCurveTo((round(off[0]), round(off[1])), (round(on[0]), round(on[1])))
    pen.closePath()

def build(path):
    names = {'0':'zero','1':'one','2':'two','3':'three','4':'four','5':'five','6':'six','7':'seven','8':'eight','9':'nine',':':'colon'}
    order = ['.notdef', 'space'] + [names[c] for c in G]
    fb = FontBuilder(1000, isTTF=True)
    fb.setupGlyphOrder(order)
    cmap = {ord(' '): 'space'}
    glyphs, metrics = {}, {}
    empty = TTGlyphPen(None); glyphs['.notdef'] = empty.glyph(); metrics['.notdef'] = (500, 0)
    empty = TTGlyphPen(None); glyphs['space'] = empty.glyph(); metrics['space'] = (4 * PITCH, 0)
    for ch, art in G.items():
        rows = art.strip('\n').split('\n')
        assert len(rows) == ROWS, ch
        cols = len(rows[0])
        pen = TTGlyphPen(None)
        for r, line in enumerate(rows):
            for c, cell in enumerate(line):
                if cell == '#':
                    cx = PITCH * (c + 1) - PITCH / 2 + PITCH / 2   # une demi-colonne de marge à gauche
                    cy = PITCH * (ROWS - r) - PITCH / 2
                    circle(pen, cx, cy, DOT / 2)
        glyphs[names[ch]] = pen.glyph()
        metrics[names[ch]] = ((cols + 1) * PITCH, 0)
        cmap[ord(ch)] = names[ch]
    fb.setupCharacterMap(cmap)
    fb.setupGlyf(glyphs)
    fb.setupHorizontalMetrics(metrics)
    fb.setupHorizontalHeader(ascent=ROWS * PITCH + 60, descent=-60)
    fb.setupNameTable({'familyName': 'WUX Dots', 'styleName': 'Regular'})
    fb.setupOS2(sTypoAscender=ROWS * PITCH + 60, sTypoDescender=-60, usWinAscent=ROWS * PITCH + 60, usWinDescent=60)
    fb.setupPost()
    fb.save(path)

if __name__ == '__main__':
    # Même fichier pour le widget (res/font) et l'aperçu Flutter (assets/fonts).
    build('android/app/src/main/res/font/wux_dots.ttf')
    build('assets/fonts/WuxDots.ttf')
