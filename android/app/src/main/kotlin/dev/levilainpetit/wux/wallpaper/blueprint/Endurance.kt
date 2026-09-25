package dev.levilainpetit.wux.wallpaper.blueprint

import android.graphics.Paint
import kotlin.math.sin

/**
 * Interstellar — l'Endurance, la station en anneau : douze modules autour du
 * poste de commande. L'anneau tourne lentement, comme pour la gravité.
 */
class Endurance : Subject {
    override val title = "ENDURANCE"
    override val reference = "INTERSTELLAR (2014) · NASA"
    override val scale = "1:400"
    override val sheet = 6

    override fun draw(pen: Pen, width: Float, top: Float, bottom: Float, time: Float, tiltX: Float, tiltY: Float) {
        val d = pen.density
        val zone = bottom - top
        val shift = tiltX * 8f * d
        pen.fit(2600f, 2600f, 22 * d + shift, top, width - 22 * d + shift, top + zone * 0.6f)
        plan(pen, time)
        pen.fit(2600f, 600f, 22 * d - shift, top + zone * 0.6f, width - 22 * d - shift, top + zone * 0.75f)
        side(pen, time)
        pen.fit(1300f, 800f, 22 * d - shift, top + zone * 0.75f, width - 22 * d - shift, bottom)
        module(pen, time)
    }

    private fun plan(pen: Pen, time: Float) {
        val c = 1300f
        val ring = 950f
        val spin = time * 6f
        pen.axis(c - 1250f, c, c + 1250f, c)
        pen.axis(c, c - 1250f, c, c + 1250f)
        pen.circle(c, c, ring, Weight.HAIR)
        // Les douze modules, tangents à l'anneau.
        for (i in 0 until 12) {
            val a = spin + i * 30f
            val corners = FloatArray(8)
            val shapes = listOf(-110f to -170f, 110f to -170f, 110f to 170f, -110f to 170f)
            for ((k, s) in shapes.withIndex()) {
                val (x, y) = pen.around(c, c, ring + s.first, a + s.second / (ring + s.first) * 57.3f)
                corners[k * 2] = x
                corners[k * 2 + 1] = y
            }
            pen.shade(corners, 16)
            pen.poly(corners, closed = true, weight = Weight.MAIN)
            // Le tube vers le module suivant.
            val (x0, y0) = pen.around(c, c, ring, a + 11f)
            val (x1, y1) = pen.around(c, c, ring, a + 19f)
            pen.line(x0, y0, x1, y1, Weight.THICK)
            if (i % 3 == 0) {
                val (hx, hy) = pen.around(c, c, ring - 120f, a)
                val (sx, sy) = pen.around(c, c, 240f, a)
                pen.line(hx, hy, sx, sy, Weight.THIN)
            }
            val (lx, ly) = pen.around(c, c, ring, a)
            pen.glowDot(lx, ly, 14f, 0.5f + 0.5f * sin(time * 2f + i))
        }
        // Le poste de commande, et le Ranger amarré.
        pen.circle(c, c, 240f, Weight.THICK)
        pen.circle(c, c, 160f, Weight.THIN)
        val (rx, ry) = pen.around(c, c, 380f, spin - 90f)
        pen.circle(rx, ry, 90f, Weight.MAIN)
        pen.glowArc(c, c, 200f, spin * 3f, 90f, 0.7f)
        pen.dim(c - ring - 110f, c + ring + 200f, c + ring + 110f, c + ring + 200f, -60f, "Ø 64 m")
        pen.callout(c, c, c + 500f, c - 300f, 1)
        val (mx, my) = pen.around(c, c, ring, spin + 60f)
        pen.callout(mx, my, c + 1150f, c - 1150f, 2)
        pen.caption("PLAN VIEW", "1 COMMAND MODULE  2 HABITAT")
    }

    private fun side(pen: Pen, time: Float) {
        val cy = 260f
        pen.axis(0f, cy, 2600f, cy)
        pen.rect(250f, cy - 110f, 2350f, cy + 110f, Weight.MAIN)
        for (i in 0..6) {
            val x = 250f + i * 350f
            pen.line(x, cy - 110f, x, cy + 110f, Weight.HAIR)
        }
        pen.rect(1150f, cy - 200f, 1450f, cy + 200f, Weight.THICK)
        pen.glowLine(1180f, cy, 1420f, cy, 0.6f + 0.3f * sin(time * 2f))
        pen.caption("SIDE ELEVATION")
    }

    /** Détail : un module d'habitation, ses hublots, ses sas. */
    private fun module(pen: Pen, time: Float) {
        pen.rect(150f, 150f, 1150f, 650f, Weight.THICK)
        pen.rect(190f, 190f, 1110f, 610f, Weight.HAIR)
        for (i in 0 until 5) {
            val x = 260f + i * 170f
            pen.rect(x, 300f, x + 100f, 380f, Weight.THIN)
            pen.glowLine(x + 10f, 340f, x + 90f, 340f, 0.3f + 0.3f * sin(time * 1.5f + i), 1f)
        }
        for (x in floatArrayOf(150f, 1150f)) {
            pen.circle(x, 400f, 90f, Weight.MAIN)
            pen.circle(x, 400f, 50f, Weight.HAIR)
        }
        pen.line(150f, 500f, 1150f, 500f, Weight.HAIR)
        pen.hatch(150f, 560f, 1150f, 650f, 7f)
        pen.dim(150f, 650f, 1150f, 650f, -90f, "12 m")
        pen.dim(1150f, 650f, 1150f, 150f, -60f, "6 m")
        pen.caption("DETAIL C · HABITAT MODULE")
    }
}
