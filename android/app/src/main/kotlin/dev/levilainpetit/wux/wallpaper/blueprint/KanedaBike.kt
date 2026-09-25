package dev.levilainpetit.wux.wallpaper.blueprint

import android.graphics.Paint
import kotlin.math.sin

/**
 * Akira — la moto de Kaneda. Profil et vue de dessus ; les jantes pleines
 * tournent, le feu arrière laisse sa traînée.
 */
class KanedaBike : Subject {
    override val title = "KANEDA'S BIKE"
    override val reference = "AKIRA (1988) · NEO-TOKYO 2019"
    override val scale = "1:20"
    override val sheet = 8
    override val frame = 50L

    override fun draw(pen: Pen, width: Float, top: Float, bottom: Float, time: Float, tiltX: Float, tiltY: Float) {
        val d = pen.density
        val zone = bottom - top
        val shift = tiltX * 8f * d
        pen.fit(3500f, 1500f, 24 * d + shift, top, width - 24 * d + shift, top + zone * 0.6f)
        side(pen, time)
        pen.fit(3500f, 800f, 24 * d - shift, top + zone * 0.66f, width - 24 * d - shift, bottom)
        plan(pen, time)
    }

    private fun side(pen: Pen, time: Float) {
        val ground = 1150f
        pen.line(-50f, ground, 3450f, ground, Weight.THIN)
        for (x in 0..34) pen.line(-30f + x * 100f, ground, -90f + x * 100f, ground + 50f, Weight.HAIR)
        val front = 560f
        val rear = 2500f
        // Le carénage : le grand nez arrondi, la selle basse, la poupe.
        val body = floatArrayOf(
            160f, 860f, 150f, 600f, 260f, 400f, 500f, 250f, 850f, 190f, 1150f, 230f, 1350f, 380f,
            1550f, 520f, 2050f, 520f, 2350f, 430f, 2700f, 410f, 2950f, 470f, 3060f, 620f, 2950f, 760f,
            2750f, 830f, 2200f, 930f, 1200f, 960f, 850f, 900f,
        )
        pen.shade(body, 14)
        pen.poly(body, closed = true, weight = Weight.THICK)
        // La bulle, la selle, les autocollants.
        pen.poly(floatArrayOf(820f, 200f, 1100f, 160f, 1330f, 300f, 1150f, 290f), closed = true, weight = Weight.THIN)
        pen.poly(floatArrayOf(1500f, 520f, 1600f, 460f, 2100f, 460f, 2200f, 500f), weight = Weight.MAIN)
        pen.rect(700f, 450f, 1000f, 560f, Weight.HAIR)
        pen.text(850f, 525f, "CAPSULE", 5f, Paint.Align.CENTER, bold = true)
        pen.rect(2450f, 560f, 2800f, 650f, Weight.HAIR)
        pen.text(2625f, 625f, "GOOD LUCK", 5f, Paint.Align.CENTER)
        pen.poly(floatArrayOf(450f, 700f, 900f, 640f, 1400f, 700f), weight = Weight.HAIR)
        // Les roues, jantes pleines, avec un repère qui tourne.
        for ((cx, r) in listOf(front to 330f, rear to 360f)) {
            val cy = ground - r
            pen.circle(cx, cy, r, Weight.THICK)
            pen.circle(cx, cy, r - 70f, Weight.MAIN)
            pen.circle(cx, cy, 60f, Weight.THIN)
            pen.axis(cx - r - 60f, cy, cx + r + 60f, cy)
            val spin = time * 300f
            for (k in 0 until 3) {
                val (x0, y0) = pen.around(cx, cy, 80f, spin + k * 120f)
                val (x1, y1) = pen.around(cx, cy, r - 90f, spin + k * 120f + 25f)
                pen.line(x0, y0, x1, y1, Weight.THIN)
            }
        }
        // Le phare, et le feu arrière qui laisse sa traînée.
        pen.glowDot(170f, 700f, 40f, 0.9f)
        val wave = 12f * sin(time * 4f)
        pen.glowPoly(floatArrayOf(3060f, 620f, 3250f, 630f + wave, 3500f, 625f - wave), level = 0.9f, width = 2.2f)
        pen.glowLine(3000f, 560f, 3050f, 680f, 1f, 2f)
        pen.dim(150f, ground, 3060f, ground, -150f, "2 910")
        pen.dim(front, ground - 330f, rear, ground - 360f, -520f, "1 940")
        pen.callout(500f, 300f, 300f, 80f, 1)
        pen.callout(rear, ground - 360f, 2750f, 1320f, 2)
        pen.callout(1800f, 470f, 1900f, 180f, 3)
        pen.text(0f, 1330f, "SIDE ELEVATION · 1 FAIRING  2 HUB COVER  3 SEAT", 7f, bold = true)
    }

    private fun plan(pen: Pen, time: Float) {
        val cy = 330f
        val outline = floatArrayOf(
            150f, cy, 300f, cy - 230f, 900f, cy - 300f, 1400f, cy - 240f, 2100f, cy - 230f, 2800f, cy - 200f, 3060f, cy,
            2800f, cy + 200f, 2100f, cy + 230f, 1400f, cy + 240f, 900f, cy + 300f, 300f, cy + 230f,
        )
        pen.shade(outline, 12)
        pen.poly(outline, closed = true, weight = Weight.THICK)
        pen.axis(-50f, cy, 3450f, cy)
        pen.rect(230f, cy - 80f, 890f, cy + 80f, Weight.MAIN)
        pen.rect(2140f, cy - 100f, 2860f, cy + 100f, Weight.MAIN)
        // Le guidon, sous la bulle.
        pen.line(1100f, cy - 380f, 1100f, cy + 380f, Weight.MAIN)
        pen.glowDot(1100f, cy - 380f, 12f, 0.6f + 0.4f * sin(time * 3f))
        pen.glowDot(1100f, cy + 380f, 12f, 0.6f + 0.4f * sin(time * 3f))
        pen.dim(150f, cy + 300f, 3060f, cy + 300f, -120f, "2 910")
        pen.text(0f, 780f, "PLAN VIEW", 7.5f, bold = true)
    }
}
