package dev.levilainpetit.wux.wallpaper.blueprint

import kotlin.math.sin

/**
 * Iron Man — le réacteur ARK, de face et en coupe. Le cœur pulse, les
 * bobines s'éclairent l'une après l'autre.
 */
class ArcReactor : Subject {
    override val title = "ARC REACTOR MK I"
    override val reference = "IRON MAN (2008) · STARK INDUSTRIES"
    override val scale = "2:1"
    override val sheet = 10
    override val frame = 50L

    override fun draw(pen: Pen, width: Float, top: Float, bottom: Float, time: Float, tiltX: Float, tiltY: Float) {
        val d = pen.density
        val zone = bottom - top
        val shift = tiltX * 8f * d
        pen.fit(1300f, 1300f, 24 * d + shift, top, width - 24 * d + shift, top + zone * 0.68f)
        front(pen, time)
        pen.fit(1300f, 500f, 24 * d - shift, top + zone * 0.72f, width - 24 * d - shift, bottom)
        section(pen, time)
    }

    private fun front(pen: Pen, time: Float) {
        val c = 650f
        pen.axis(c - 620f, c, c + 620f, c)
        pen.axis(c, c - 620f, c, c + 620f)
        pen.circle(c, c, 560f, Weight.THICK)
        pen.circle(c, c, 500f, Weight.MAIN)
        pen.circle(c, c, 300f, Weight.MAIN)
        pen.circle(c, c, 240f, Weight.THIN)
        // Les dix bobines, et leur enroulement.
        val lit = ((time * 4f) % 10).toInt()
        for (i in 0 until 10) {
            val a = i * 36f - 90f
            val corners = FloatArray(8)
            val pts = listOf(310f to -14f, 490f to -12f, 490f to 12f, 310f to 14f)
            for ((k, p) in pts.withIndex()) {
                val (x, y) = pen.around(c, c, p.first, a + p.second)
                corners[k * 2] = x
                corners[k * 2 + 1] = y
            }
            pen.shade(corners, 20)
            pen.poly(corners, closed = true, weight = Weight.MAIN)
            for (r in 340..460 step 30) {
                val (x0, y0) = pen.around(c, c, r.toFloat(), a - 12f)
                val (x1, y1) = pen.around(c, c, r.toFloat(), a + 12f)
                pen.line(x0, y0, x1, y1, Weight.HAIR)
            }
            val (gx, gy) = pen.around(c, c, 400f, a)
            pen.glowDot(gx, gy, 16f, if (i == lit) 1f else 0.3f)
        }
        // Le cœur.
        val pulse = 0.7f + 0.3f * sin(time * 2.6f)
        pen.circle(c, c, 150f, Weight.THICK)
        pen.glowArc(c, c, 200f, 0f, 360f, 0.5f * pulse, 1.4f)
        pen.glowDot(c, c, 70f * pulse, pulse)
        for (k in 0 until 3) {
            val (x, y) = pen.around(c, c, 560f, k * 120f + 30f)
            pen.circle(x, y, 28f, Weight.THIN)
        }
        pen.dim(c - 560f, c + 560f, c + 560f, c + 560f, -80f, "Ø 76")
        pen.callout(c, c, c + 450f, c - 650f + 90f, 1)
        val (bx, by) = pen.around(c, c, 400f, 54f)
        pen.callout(bx, by, c + 620f, c + 400f, 2)
        pen.text(0f, 1290f, "FRONT VIEW · 1 PALLADIUM CORE  2 COIL", 7f, bold = true)
    }

    private fun section(pen: Pen, time: Float) {
        val cy = 220f
        pen.axis(0f, cy, 1300f, cy)
        pen.rect(90f, cy - 110f, 1210f, cy + 110f, Weight.THICK)
        pen.rect(150f, cy - 70f, 350f, cy + 70f, Weight.MAIN)
        pen.hatch(150f, cy - 70f, 350f, cy + 70f, 6f)
        pen.rect(950f, cy - 70f, 1150f, cy + 70f, Weight.MAIN)
        pen.hatch(950f, cy - 70f, 1150f, cy + 70f, 6f)
        pen.rect(500f, cy - 110f, 800f, cy + 110f, Weight.MAIN)
        pen.glowLine(520f, cy, 780f, cy, 0.7f + 0.3f * sin(time * 2.6f), 3f)
        pen.dim(90f, cy + 110f, 1210f, cy + 110f, -80f, "Ø 76")
        pen.dim(1210f, cy + 110f, 1210f, cy - 110f, -40f, "22")
        pen.text(0f, 480f, "SECTION A-A", 7.5f, bold = true)
    }
}
