package dev.levilainpetit.wux.wallpaper.blueprint

import android.graphics.Paint
import kotlin.math.sin

/**
 * Blade Runner — le Spinner, la voiture volante de la police. Profil en vol
 * et vue de face ; le véhicule flotte, la rampe du toit clignote, la turbine
 * arrière tourne.
 */
class Spinner : Subject {
    override val title = "POLICE SPINNER"
    override val reference = "BLADE RUNNER (1982) · LAPD 44"
    override val scale = "1:30"
    override val sheet = 2
    override val frame = 50L

    override fun draw(pen: Pen, width: Float, top: Float, bottom: Float, time: Float, tiltX: Float, tiltY: Float) {
        val d = pen.density
        val zone = bottom - top
        val shift = tiltX * 8f * d
        pen.fit(4900f, 1900f, 24 * d + shift, top, width - 24 * d + shift, top + zone * 0.56f)
        side(pen, time)
        pen.fit(2600f, 1900f, 24 * d - shift, top + zone * 0.6f, width * 0.55f - shift, bottom)
        front(pen, time)
        pen.fit(1500f, 1500f, width * 0.58f - shift, top + zone * 0.62f, width - 24 * d - shift, bottom)
        turbine(pen, time)
    }

    private fun side(pen: Pen, time: Float) {
        val ground = 1700f
        val lift = 300f + sin(time * 1.3f) * 40f
        val y = ground - 1450f - lift + 300f
        pen.line(0f, ground, 4900f, ground, Weight.THIN)
        for (x in 0..48) pen.line(40f + x * 100f, ground, -20f + x * 100f, ground + 50f, Weight.HAIR)
        // La caisse.
        val body = floatArrayOf(
            80f, 900f, 150f, 700f, 400f, 560f, 900f, 380f, 1700f, 300f, 2500f, 320f, 2900f, 420f,
            3600f, 480f, 4300f, 560f, 4550f, 700f, 4600f, 900f, 4480f, 1080f, 3900f, 1150f, 800f, 1150f, 250f, 1080f,
        ).offset(0f, y)
        pen.shade(body, 14)
        pen.poly(body, closed = true, weight = Weight.THICK)
        // La verrière et ses montants.
        pen.poly(floatArrayOf(430f, 610f, 900f, 430f, 1700f, 350f, 2500f, 370f, 2800f, 470f, 2760f, 730f, 600f, 750f).offset(0f, y), closed = true, weight = Weight.MAIN)
        pen.line(1300f, 390f + y, 1250f, 745f + y, Weight.THIN)
        pen.line(2100f, 355f + y, 2080f, 740f + y, Weight.THIN)
        // Lignes de caisse, portière, inscriptions.
        pen.poly(floatArrayOf(300f, 900f, 4450f, 900f).offset(0f, y), weight = Weight.HAIR)
        pen.poly(floatArrayOf(2950f, 470f, 3050f, 1130f).offset(0f, y), weight = Weight.HAIR)
        pen.text(2350f, 1030f + y, "POLICE", 8f, Paint.Align.CENTER, bold = true)
        pen.text(3450f, 1030f + y, "44", 8f, Paint.Align.CENTER, bold = true)
        // Les roues, rentrées sous la caisse pour le vol.
        for (cx in floatArrayOf(900f, 3700f)) {
            pen.arc(cx, 1150f + y, 230f, 180f, 180f, Weight.THIN)
            pen.circle(cx, 1170f + y, 150f, Weight.MAIN)
            pen.circle(cx, 1170f + y, 60f, Weight.HAIR)
        }
        // La rampe du toit, qui clignote d'un côté puis de l'autre.
        pen.rect(1550f, 250f + y, 2350f, 300f + y, Weight.MAIN)
        val blink = sin(time * 6f) > 0f
        pen.glowLine(1580f, 275f + y, 1930f, 275f + y, if (blink) 1f else 0.2f, 2.4f)
        pen.glowLine(1970f, 275f + y, 2320f, 275f + y, if (blink) 0.2f else 1f, 2.4f)
        // Phares et feux.
        pen.glowDot(150f, 880f + y, 40f, 0.9f)
        pen.glowLine(4500f, 820f + y, 4580f, 960f + y, 0.8f)
        // Les poussées sous la caisse, vers le sol.
        val thrust = 0.5f + 0.3f * sin(time * 9f)
        for (x in floatArrayOf(1500f, 3100f)) pen.glowLine(x, 1150f + y, x, 1150f + y + lift * 0.5f, thrust, 1.2f)
        pen.hidden(0f, 1150f + y, 4900f, 1150f + y)
        // Cotes et repères.
        pen.dim(80f, 1150f + y, 4600f, 1150f + y, -(ground - 1150f - y) - 140f, "4 520")
        pen.dim(4750f, ground, 4750f, 1150f + y, 0f - 60f, "HOVER %d".format(lift.toInt()))
        pen.callout(1900f, 275f + y, 1900f, 60f + y - 120f, 1)
        pen.callout(1000f, 500f + y, 700f, 200f + y - 60f, 2)
        pen.callout(3700f, 1170f + y, 3950f, 1500f, 3)
        pen.text(0f, 1880f, "SIDE ELEVATION · IN FLIGHT", 7.5f, bold = true)
    }

    private fun front(pen: Pen, time: Float) {
        val cx = 1300f
        val body = floatArrayOf(
            cx - 1100f, 1250f, cx - 1150f, 900f, cx - 950f, 600f, cx - 550f, 420f, cx, 380f,
            cx + 550f, 420f, cx + 950f, 600f, cx + 1150f, 900f, cx + 1100f, 1250f, cx + 800f, 1350f, cx - 800f, 1350f,
        )
        pen.shade(body, 14)
        pen.poly(body, closed = true, weight = Weight.THICK)
        pen.poly(floatArrayOf(cx - 850f, 880f, cx - 700f, 560f, cx, 470f, cx + 700f, 560f, cx + 850f, 880f), closed = true, weight = Weight.MAIN)
        pen.axis(cx, 250f, cx, 1650f)
        pen.rect(cx - 380f, 300f, cx + 380f, 380f, Weight.MAIN)
        val blink = sin(time * 6f) > 0f
        pen.glowLine(cx - 350f, 340f, cx - 30f, 340f, if (blink) 1f else 0.2f, 2.4f)
        pen.glowLine(cx + 30f, 340f, cx + 350f, 340f, if (blink) 0.2f else 1f, 2.4f)
        for (side in floatArrayOf(-1f, 1f)) {
            pen.glowDot(cx + side * 780f, 1060f, 50f, 0.9f)
            pen.rect(cx + side * 1050f - 120f, 1350f, cx + side * 1050f + 120f, 1520f, Weight.MAIN)
        }
        pen.dim(cx - 1150f, 1520f, cx + 1150f, 1520f, -140f, "2 300")
        pen.text(0f, 1870f, "FRONT VIEW", 7.5f, bold = true)
    }

    /** Détail : la turbine arrière, pales qui tournent. */
    private fun turbine(pen: Pen, time: Float) {
        val c = 700f
        pen.circle(c, c, 560f, Weight.THICK)
        pen.circle(c, c, 480f, Weight.THIN)
        pen.circle(c, c, 120f, Weight.MAIN)
        pen.axis(c - 640f, c, c + 640f, c)
        pen.axis(c, c - 640f, c, c + 640f)
        val spin = time * 200f
        for (b in 0 until 9) {
            val (x0, y0) = pen.around(c, c, 130f, spin + b * 40f)
            val (x1, y1) = pen.around(c, c, 470f, spin + b * 40f + 22f)
            pen.line(x0, y0, x1, y1, Weight.THIN)
        }
        pen.glowArc(c, c, 520f, spin * 0.3f, 60f, 0.6f)
        pen.text(0f, 1460f, "DETAIL A · TURBINE", 7.5f, bold = true)
    }
}

/** Décale les points x, y d'une ligne brisée. */
internal fun FloatArray.offset(dx: Float, dy: Float) = FloatArray(size) { if (it % 2 == 0) this[it] + dx else this[it] + dy }
