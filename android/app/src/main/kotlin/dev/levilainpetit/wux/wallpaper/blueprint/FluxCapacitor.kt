package dev.levilainpetit.wux.wallpaper.blueprint

import android.graphics.Paint
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.sin

/**
 * Retour vers le futur — le convecteur temporel : le boîtier à hublot, le Y
 * des trois chambres de flux, les câbles d'alimentation. Les impulsions
 * courent le long des trois bras vers le centre, qui s'illumine.
 * En haut, la DeLorean de profil et la place du convecteur ; au milieu, la
 * face et le côté du boîtier ; en bas, la puissance à fournir selon la
 * vitesse, que le point de fonctionnement remonte jusqu'à 88 mph.
 */
class FluxCapacitor : Subject {
    override val title = "FLUX CAPACITOR"
    override val reference = "BACK TO THE FUTURE (1985) · DMC-12"
    override val scale = "1:5"
    override val sheet = 2
    override val frame = 50L

    override fun draw(pen: Pen, width: Float, top: Float, bottom: Float, time: Float, tiltX: Float, tiltY: Float) {
        val d = pen.density
        val zone = bottom - top
        val shift = tiltX * 8f * d
        val l = 18 * d
        val r = width - 18 * d
        pen.fit(4600f, 1450f, l + shift, top, r + shift, top + zone * 0.26f, x0 = -200f, y0 = -150f)
        car(pen, time)
        pen.caption("INSTALLATION", "DMC-12 · BEHIND THE SEATS")
        pen.fit(800f, 1080f, l - shift, top + zone * 0.26f, width * 0.62f - shift, top + zone * 0.7f, x0 = -100f, y0 = -250f)
        front(pen, time)
        pen.caption("FRONT VIEW", "DOOR CLOSED · CHAMBER LIVE")
        pen.fit(480f, 1080f, width * 0.62f - shift, top + zone * 0.26f, r - shift, top + zone * 0.7f, x0 = -110f, y0 = -250f)
        side(pen)
        pen.caption("SIDE VIEW")
        pen.fit(1200f, 760f, l + shift, top + zone * 0.7f, r + shift, bottom, x0 = -150f, y0 = -60f)
        curve(pen, time)
        pen.caption("POWER REQUIREMENT", "1.21 GW AT 88 MPH")
    }

    // ——— Le boîtier, de face : 600 × 760, le Y centré sur (300, 400) ———

    private val center = 300f to 400f
    private val ends = listOf(140f to 185f, 460f to 185f, 300f to 650f)

    /** L'éclat de la lampe [i] (0 au bout, 4 près du centre) : les impulsions courent vers le centre. */
    private fun chase(time: Float, i: Int): Float {
        val p = (time * 0.9f) % 1f * 7f
        return max(0f, 1f - abs(p - i) * 1.4f)
    }

    private fun flash(time: Float): Float {
        val p = (time * 0.9f) % 1f * 7f
        return max(0f, 1f - abs(p - 5.3f) * 0.9f)
    }

    private fun front(pen: Pen, time: Float) {
        // Les trois câbles d'alimentation, qui descendent dans le boîtier.
        for (x in floatArrayOf(150f, 300f, 450f)) {
            val bend = (x - 300f) * 0.25f
            pen.poly(floatArrayOf(x - 16f, 0f, x - 16f, -70f, x - 16f + bend, -170f, x - 16f + bend * 1.6f, -240f), weight = Weight.MAIN)
            pen.poly(floatArrayOf(x + 16f, 0f, x + 16f, -70f, x + 16f + bend, -170f, x + 16f + bend * 1.6f, -240f), weight = Weight.MAIN)
            for (j in 1..5) {
                val y = -j * 40f
                val o = if (y < -70f) bend * ((-y - 70f) / 100f).coerceAtMost(1.6f) else 0f
                pen.line(x - 16f + o, y, x + 16f + o, y, Weight.HAIR)
            }
            pen.rect(x - 30f, -22f, x + 30f, 0f, Weight.THIN)
        }
        // Le boîtier, la porte et le hublot à pans coupés.
        val box = floatArrayOf(0f, 0f, 600f, 0f, 600f, 760f, 0f, 760f)
        pen.shade(box, 14)
        pen.rect(0f, 0f, 600f, 760f, Weight.THICK)
        pen.rect(22f, 22f, 578f, 738f, Weight.THIN)
        val window = chamfer(72f, 92f, 528f, 690f, 34f)
        pen.poly(window, closed = true, weight = Weight.MAIN)
        pen.poly(chamfer(86f, 106f, 514f, 676f, 28f), closed = true, weight = Weight.HAIR)
        for (y in floatArrayOf(120f, 600f)) pen.rect(-14f, y, 0f, y + 80f, Weight.THIN)
        pen.rect(578f, 330f, 606f, 470f, Weight.THIN)
        pen.rect(592f, 360f, 620f, 440f, Weight.THIN)
        // L'étiquette d'avertissement, sous le hublot.
        pen.rect(300f, 700f, 548f, 730f, Weight.HAIR)
        pen.line(316f, 710f, 532f, 710f, Weight.HAIR)
        pen.line(316f, 720f, 480f, 720f, Weight.HAIR)

        // Les trois bras : une électrode au bout, un tube, les éclateurs.
        val (cx, cy) = center
        for ((a, e) in ends.withIndex()) {
            val (ex, ey) = e
            val len = hypot(cx - ex, cy - ey)
            val ux = (cx - ex) / len
            val uy = (cy - ey) / len
            val nx = -uy
            val ny = ux
            // L'électrode : un cylindre bagué.
            val h = 34f
            val body = floatArrayOf(
                ex + nx * h, ey + ny * h, ex + nx * h + ux * 90f, ey + ny * h + uy * 90f,
                ex - nx * h + ux * 90f, ey - ny * h + uy * 90f, ex - nx * h, ey - ny * h,
            )
            pen.shade(body, 24)
            pen.poly(body, closed = true, weight = Weight.MAIN)
            for (t in floatArrayOf(22f, 45f, 68f)) pen.line(ex + nx * h + ux * t, ey + ny * h + uy * t, ex - nx * h + ux * t, ey - ny * h + uy * t, Weight.HAIR)
            // Le tube, jusqu'au centre.
            val w = 14f
            val s = 90f
            val f = len - 44f
            pen.line(ex + nx * w + ux * s, ey + ny * w + uy * s, ex + nx * w + ux * f, ey + ny * w + uy * f, Weight.THIN)
            pen.line(ex - nx * w + ux * s, ey - ny * w + uy * s, ex - nx * w + ux * f, ey - ny * w + uy * f, Weight.THIN)
            pen.glowLine(ex + ux * s, ey + uy * s, ex + ux * f, ey + uy * f, level = 0.35f, width = 1.2f)
            // Les éclateurs, et l'impulsion qui court vers le centre.
            for (i in 0 until 5) {
                val t = s + 20f + (f - s - 40f) * i / 4f
                val x = ex + ux * t
                val y = ey + uy * t
                pen.line(x + nx * 22f, y + ny * 22f, x - nx * 22f, y - ny * 22f, Weight.THIN)
                pen.glowDot(x, y, 15f, 0.2f + 0.8f * chase(time, i))
                if (i < 4) {
                    val t2 = s + 20f + (f - s - 40f) * (i + 1) / 4f
                    pen.glowLine(x, y, ex + ux * t2, ey + uy * t2, level = chase(time, i), width = 2f)
                }
            }
            // Le fil qui part de l'électrode vers son câble.
            val feed = floatArrayOf(150f, 300f, 450f)[a]
            if (a < 2) pen.poly(floatArrayOf(ex - ux * 10f, ey - uy * 10f, feed, 110f, feed, 22f), weight = Weight.HAIR)
            else pen.poly(floatArrayOf(ex + 40f, ey + 40f, 470f, 690f, 548f, 690f, 548f, 22f, 450f, 22f), weight = Weight.HAIR)
        }
        // Le cœur, où les trois flux se rejoignent.
        pen.circle(cx, cy, 46f, Weight.MAIN)
        pen.circle(cx, cy, 26f, Weight.THIN)
        pen.glowDot(cx, cy, 22f, 0.25f + 0.75f * flash(time))
        pen.axis(300f, 70f, 300f, 720f)

        pen.dim(600f, 0f, 600f, 760f, -80f, "760")
        pen.note(140f, 190f, -60f, -80f, "FLUX|DISPERSAL")
        pen.note(424f, 730f, 40f, 800f, "SHIELD EYES FROM LIGHT")
    }

    /** Un rectangle aux coins coupés. */
    private fun chamfer(l: Float, t: Float, r: Float, b: Float, c: Float) = floatArrayOf(
        l + c, t, r - c, t, r, t + c, r, b - c, r - c, b, l + c, b, l, b - c, l, t + c,
    )

    // ——— De côté : 260 de profondeur ———

    private fun side(pen: Pen) {
        for (x in floatArrayOf(130f)) {
            pen.poly(floatArrayOf(x - 16f, 0f, x - 16f, -240f), weight = Weight.MAIN)
            pen.poly(floatArrayOf(x + 16f, 0f, x + 16f, -240f), weight = Weight.MAIN)
            pen.rect(x - 30f, -22f, x + 30f, 0f, Weight.THIN)
        }
        val box = floatArrayOf(0f, 0f, 260f, 0f, 260f, 760f, 0f, 760f)
        pen.shade(box, 14)
        pen.rect(0f, 0f, 260f, 760f, Weight.THICK)
        pen.rect(0f, 22f, 24f, 738f, Weight.THIN)
        pen.hidden(60f, 92f, 60f, 690f)
        pen.hidden(210f, 92f, 210f, 690f)
        pen.hidden(60f, 92f, 210f, 92f)
        pen.hidden(60f, 690f, 210f, 690f)
        // Les pattes de fixation, à l'arrière.
        for (y in floatArrayOf(80f, 620f)) {
            pen.rect(260f, y, 300f, y + 60f, Weight.THIN)
            pen.circle(280f, y + 30f, 9f, Weight.HAIR)
        }
        pen.axis(-40f, 400f, 320f, 400f)
        pen.dim(0f, 760f, 260f, 760f, -60f, "260")
    }

    // ——— La DeLorean, de profil, et la place du convecteur ———

    private fun arcPoints(cx: Float, cy: Float, r: Float, from: Float, to: Float, steps: Int = 12): FloatArray {
        val out = FloatArray((steps + 1) * 2)
        for (i in 0..steps) {
            val a = Math.toRadians((from + (to - from) * i / steps).toDouble())
            out[i * 2] = cx + cos(a).toFloat() * r
            out[i * 2 + 1] = cy + sin(a).toFloat() * r
        }
        return out
    }

    private fun car(pen: Pen, time: Float) {
        val g = 1200f
        fun y(h: Float) = g - h
        val front = 870f
        val rear = 3283f
        val axle = y(320f)
        pen.line(-150f, g, 4400f, g, Weight.THIN)
        // Le coin : le nez bas, le pare-brise couché, le toit, la poupe à persiennes.
        val upper = floatArrayOf(
            0f, y(330f), 0f, y(600f), 60f, y(650f), 900f, y(800f), 1500f, y(870f), 2050f, y(1140f),
            2600f, y(1130f), 3200f, y(960f), 4150f, y(905f), 4216f, y(860f), 4216f, y(380f), 4150f, y(300f),
        )
        val lower = floatArrayOf(3700f, y(300f)) + arcPoints(rear, axle, 380f, 0f, -180f) +
            floatArrayOf(2860f, y(250f), 1290f, y(250f)) + arcPoints(front, axle, 380f, 0f, -180f) +
            floatArrayOf(450f, y(300f), 0f, y(330f))
        val shell = upper + lower
        pen.shade(shell, 12)
        pen.poly(shell, closed = true, weight = Weight.THICK)
        pen.poly(floatArrayOf(1620f, y(880f), 2080f, y(1105f), 2560f, y(1095f), 2760f, y(900f)), closed = true, weight = Weight.THIN)
        pen.poly(floatArrayOf(1520f, y(860f), 1500f, y(330f), 2760f, y(330f), 2800f, y(930f)), weight = Weight.HAIR)
        pen.line(20f, y(560f), 4200f, y(560f), Weight.HAIR)
        for (i in 0 until 7) {
            val x = 2850f + i * 110f
            pen.line(x, y(1000f - i * 12f), x + 80f, y(990f - i * 12f), Weight.HAIR)
        }
        for (x in floatArrayOf(front, rear)) {
            pen.circle(x, axle, 320f, Weight.MAIN)
            pen.circle(x, axle, 200f, Weight.THIN)
            pen.circle(x, axle, 40f, Weight.HAIR)
        }
        // Le convecteur, caché derrière les sièges.
        pen.hidden(2480f, y(900f), 2640f, y(900f))
        pen.hidden(2480f, y(420f), 2640f, y(420f))
        pen.hidden(2480f, y(420f), 2480f, y(900f))
        pen.hidden(2640f, y(420f), 2640f, y(900f))
        pen.glowDot(2560f, y(660f), 60f, 0.4f + 0.6f * flash(time))
        pen.note(2640f, y(760f), 3300f, -60f, "FLUX CAPACITOR")
        pen.dim(0f, g, 4216f, g, -140f, "4 216")
    }

    // ——— La puissance à fournir selon la vitesse ———

    private fun curve(pen: Pen, time: Float) {
        val w = 1000f
        val h = 600f
        pen.line(0f, h, w + 30f, h, Weight.MAIN)
        pen.line(0f, h, 0f, -30f, Weight.MAIN)
        for (i in 0..5) {
            val x = w * i / 5f
            pen.line(x, h, x, h + 14f, Weight.THIN)
            pen.text(x, h + 50f, "${i * 20}", 5f, Paint.Align.CENTER, alpha = 170)
            if (i > 0) pen.line(x, 0f, x, h, Weight.HAIR)
        }
        for (i in 0..3) {
            val y = h - h * i / 3f
            pen.text(-16f, y + 8f, listOf("0.0", "0.5", "1.0", "1.5")[i], 5f, Paint.Align.RIGHT, alpha = 170)
            if (i > 0) pen.line(0f, y, w, y, Weight.HAIR)
        }
        pen.text(w, h + 90f, "MPH", 5.5f, Paint.Align.RIGHT, bold = true)
        pen.text(10f, -40f, "GW", 5.5f, bold = true)
        fun px(v: Float) = w * v / 100f
        fun py(p: Float) = h - h * p / 1.5f
        val pts = FloatArray(42)
        for (i in 0..20) {
            val v = i * 5f
            pts[i * 2] = px(v)
            pts[i * 2 + 1] = py(1.21f * (v / 88f) * (v / 88f)).coerceAtLeast(-20f)
        }
        pen.poly(pts, weight = Weight.MAIN)
        pen.hidden(px(88f), h, px(88f), py(1.21f))
        pen.hidden(0f, py(1.21f), px(88f), py(1.21f))
        // Le point de fonctionnement grimpe jusqu'à 88 mph, puis l'éclair.
        val cycle = (time * 0.25f) % 1f
        val v = 88f * minOf(1f, cycle / 0.8f)
        val p = 1.21f * (v / 88f) * (v / 88f)
        val trail = FloatArray(2 * 12)
        for (i in 0 until 12) {
            val t = v * i / 11f
            trail[i * 2] = px(t)
            trail[i * 2 + 1] = py(1.21f * (t / 88f) * (t / 88f))
        }
        pen.glowPoly(trail, level = 0.8f, width = 1.4f)
        val hit = if (cycle > 0.8f) 1f - (cycle - 0.8f) / 0.2f else 0.5f
        pen.glowDot(px(v), py(p), 14f, hit)
        pen.note(px(88f), py(1.21f), 560f, -80f, "88 MPH · 1.21 GW", left = true)
    }
}
