package dev.levilainpetit.wux.wallpaper.blueprint

import android.graphics.Paint
import kotlin.math.sin

/**
 * Matrix — le Nebuchadnezzar, d'après le plan de décor du pont : la passerelle
 * qui mène à la salle du cœur, la mezzanine, le détail du fût et celui de la
 * passerelle courbe. Une impulsion parcourt la passerelle, le cœur tourne.
 */
class Hovercraft : Subject {
    override val title = "NEBUCHADNEZZAR"
    override val reference = "THE MATRIX (1999) · CATWALK / MEZZANINE"
    override val scale = "1:50"
    override val sheet = 3

    override fun draw(pen: Pen, width: Float, top: Float, bottom: Float, time: Float, tiltX: Float, tiltY: Float) {
        val d = pen.density
        val zone = bottom - top
        val shift = tiltX * 8f * d
        pen.fit(9300f, 3000f, 22 * d + shift, top, width * 0.58f + shift, bottom, turn = true)
        catwalk(pen, time)
        pen.fit(4300f, 2700f, width * 0.6f - shift, top, width - 22 * d - shift, top + zone * 0.44f, turn = true)
        mezzanine(pen)
        pen.fit(1300f, 1300f, width * 0.6f - shift, top + zone * 0.44f, width - 22 * d - shift, top + zone * 0.68f)
        barrel(pen, time)
        pen.fit(1400f, 1400f, width * 0.6f - shift, top + zone * 0.68f, width - 22 * d - shift, bottom)
        curve(pen)
    }

    /** Les files repérées du plan : un axe et sa bulle. */
    private fun grid(pen: Pen, x: Float, top: Float, bottom: Float, letter: String) {
        pen.axis(x, top, x, bottom)
        pen.circle(x, top - 110f, 90f, Weight.THIN)
        pen.text(x, top - 80f, letter, 7f, Paint.Align.CENTER)
    }

    /** L'entrée : la tôle en C, les marches, les boulons. */
    private fun entry(pen: Pen, x0: Float, cy: Float) {
        pen.poly(floatArrayOf(x0 + 420f, cy - 1400f, x0 + 300f, cy - 1400f, x0 + 300f, cy - 700f, x0 + 180f, cy - 700f, x0 + 180f, cy + 700f, x0 + 300f, cy + 700f, x0 + 300f, cy + 1400f, x0 + 420f, cy + 1400f), weight = Weight.THICK)
        pen.poly(floatArrayOf(x0 + 420f, cy - 1400f, x0 + 420f, cy - 800f, x0 + 900f, cy - 800f), weight = Weight.MAIN)
        pen.poly(floatArrayOf(x0 + 420f, cy + 1400f, x0 + 420f, cy + 800f, x0 + 900f, cy + 800f), weight = Weight.MAIN)
        pen.rect(x0, cy - 350f, x0 + 180f, cy + 350f, Weight.MAIN)
        for (i in 0 until 5) pen.line(x0 + 30f + i * 30f, cy - 350f, x0 + 30f + i * 30f, cy + 350f, Weight.HAIR)
        pen.hatch(x0 + 300f, cy - 1400f, x0 + 420f, cy - 700f, 5f)
        pen.hatch(x0 + 300f, cy + 700f, x0 + 420f, cy + 1400f, 5f)
    }

    /** Le plan L : la passerelle et la salle du cœur. */
    private fun catwalk(pen: Pen, time: Float) {
        val cy = 1500f
        val start = 300f
        val end = 6700f
        val core = 7900f
        val room = 1150f
        grid(pen, 450f, 250f, 2800f, "M")
        grid(pen, 900f, 250f, 2800f, "E")
        grid(pen, 2200f, 250f, 2800f, "A")
        grid(pen, 3600f, 250f, 2800f, "B")
        grid(pen, 5000f, 250f, 2800f, "C")
        grid(pen, 6400f, 250f, 2800f, "D")
        entry(pen, start - 300f, cy)
        // Les rambardes, doublées, et le caillebotis.
        for (y in floatArrayOf(cy - 600f, cy - 560f, cy + 560f, cy + 600f)) pen.line(start + 600f, y, end + 200f, y, Weight.MAIN)
        pen.line(start + 600f, cy - 420f, end, cy - 420f, Weight.THIN)
        pen.line(start + 600f, cy + 420f, end, cy + 420f, Weight.THIN)
        pen.hatch(start + 700f, cy - 420f, start + 2600f, cy + 420f, 5f)
        pen.hatch(end - 1700f, cy - 420f, end, cy + 420f, 5f)
        pen.hidden(start + 600f, cy, end, cy)
        // Les boulons des poteaux, par paires.
        for (x in floatArrayOf(start + 900f, start + 1700f, start + 3400f, start + 5100f)) {
            for (side in floatArrayOf(-1f, 1f)) {
                pen.circle(x, cy + side * 500f, 55f, Weight.THIN)
                pen.dot(x, cy + side * 500f, 1f)
            }
        }
        // L'arc pointillé de l'escalier au-dessus.
        pen.arc(start + 600f, cy, 1000f, -60f, 120f, Weight.HAIR)
        // La salle du cœur : sa paroi double, ouverte vers la passerelle.
        pen.arc(core, cy, room, -160f, 320f, Weight.THICK)
        pen.arc(core, cy, room - 60f, -160f, 320f, Weight.MAIN)
        for (i in 0 until 10) {
            val a = -150f + i * 32f
            val (x0, y0) = pen.around(core, cy, room, a - 8f)
            val (x1, y1) = pen.around(core, cy, room + 180f, a - 8f)
            val (x2, y2) = pen.around(core, cy, room + 180f, a + 8f)
            val (x3, y3) = pen.around(core, cy, room, a + 8f)
            pen.poly(floatArrayOf(x0, y0, x1, y1, x2, y2, x3, y3), weight = Weight.MAIN)
        }
        // Les rayons du plancher, et le moyeu qui tourne.
        val spin = time * 12f
        for (i in 0 until 12) {
            val (x0, y0) = pen.around(core, cy, 180f, spin + i * 30f)
            val (x1, y1) = pen.around(core, cy, room - 80f, spin + i * 30f)
            pen.line(x0, y0, x1, y1, Weight.HAIR)
        }
        pen.circle(core, cy, 180f, Weight.MAIN)
        pen.glowArc(core, cy, 120f, spin * 4f, 120f, 0.8f, 1.4f)
        pen.glowDot(core, cy, 40f, 0.6f + 0.4f * sin(time * 2f))
        // Une impulsion qui parcourt la passerelle.
        val t = (time * 0.35f) % 1f
        pen.glowLine(start + 600f + (end - start - 600f) * t, cy - 500f, start + 600f + (end - start - 600f) * t, cy + 500f, 0.6f, 1.2f)
        pen.dim(start + 600f, cy + 600f, end + 200f, cy + 600f, -300f, "6 000")
        pen.dim(end + 200f, cy + 600f, end + 200f, cy - 600f, -250f, "1 200")
        pen.dim(core - room, cy + room, core + room, cy + room, -300f, "Ø 2 300")
        pen.text(start + 1300f, cy - 700f, "GRATING", 6f, alpha = 150)
        pen.text(core - 400f, cy - room - 280f, "HUB", 6f, alpha = 150)
        pen.caption("CATWALK PLAN L", "NOTE: LEVELS FROM STUDIO FLOOR")
    }

    /** Le plan K : la mezzanine. */
    private fun mezzanine(pen: Pen) {
        val cy = 1300f
        grid(pen, 450f, 250f, 2500f, "M")
        grid(pen, 900f, 250f, 2500f, "E")
        grid(pen, 3600f, 250f, 2500f, "A")
        entry(pen, 0f, cy)
        pen.rect(900f, cy - 450f, 2900f, cy + 450f, Weight.MAIN)
        pen.hatch(900f, cy - 450f, 2900f, cy + 450f, 4f)
        for (x in floatArrayOf(1200f, 2000f, 2700f)) {
            for (side in floatArrayOf(-1f, 1f)) pen.circle(x, cy + side * 600f, 60f, Weight.THIN)
        }
        pen.circle(1900f, cy, 110f, Weight.MAIN)
        pen.arc(2900f, cy, 800f, -60f, 120f, Weight.HAIR)
        pen.dim(900f, cy + 450f, 2900f, cy + 450f, -250f, "2 000")
        pen.caption("MEZZANINE PLAN K", "BESTOBELL 40 GRATING")
    }

    /** Le détail du fût : bagues et crans, qui tournent lentement. */
    private fun barrel(pen: Pen, time: Float) {
        val c = 650f
        pen.circle(c, c, 540f, Weight.THICK)
        pen.circle(c, c, 470f, Weight.MAIN)
        pen.circle(c, c, 380f, Weight.THIN)
        pen.circle(c, c, 120f, Weight.MAIN)
        pen.axis(c - 620f, c, c + 620f, c)
        pen.axis(c, c - 620f, c, c + 620f)
        val spin = time * 8f
        for (i in 0 until 36) {
            val (x0, y0) = pen.around(c, c, 470f, spin + i * 10f)
            val (x1, y1) = pen.around(c, c, 540f, spin + i * 10f)
            pen.line(x0, y0, x1, y1, Weight.HAIR)
        }
        pen.caption("BARREL DETAIL", "SCALE 1:20")
    }

    /** Le détail de la passerelle courbe : les barreaux du caillebotis. */
    private fun curve(pen: Pen) {
        val cx = 1400f
        val cy = 1400f
        pen.arc(cx, cy, 1250f, 180f, 90f, Weight.THICK)
        pen.arc(cx, cy, 950f, 180f, 90f, Weight.THICK)
        pen.arc(cx, cy, 1200f, 180f, 90f, Weight.HAIR)
        pen.arc(cx, cy, 1000f, 180f, 90f, Weight.HAIR)
        for (i in 0..30) {
            val (x0, y0) = pen.around(cx, cy, 1000f, 180f + i * 3f)
            val (x1, y1) = pen.around(cx, cy, 1200f, 180f + i * 3f)
            pen.line(x0, y0, x1, y1, Weight.HAIR)
        }
        pen.caption("CATWALK DETAIL", "SCALE 1:20")
    }
}
