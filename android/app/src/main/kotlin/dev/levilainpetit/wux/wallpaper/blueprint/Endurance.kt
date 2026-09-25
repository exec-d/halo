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
        pen.fit(2600f, 2600f, 24 * d + shift, top, width - 24 * d + shift, top + zone * 0.72f)
        plan(pen, time)
        pen.fit(2600f, 600f, 24 * d - shift, top + zone * 0.76f, width - 24 * d - shift, bottom)
        side(pen, time)
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
        pen.text(0f, 2560f, "PLAN VIEW · 1 COMMAND MODULE  2 HABITAT", 7f, bold = true)
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
        pen.text(0f, 560f, "SIDE ELEVATION", 7f, bold = true)
    }
}
