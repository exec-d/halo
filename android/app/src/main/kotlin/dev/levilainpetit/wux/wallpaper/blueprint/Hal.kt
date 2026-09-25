package dev.levilainpetit.wux.wallpaper.blueprint

import android.graphics.Paint
import kotlin.math.sin

/**
 * 2001, l'odyssée de l'espace — HAL 9000. Le panneau de face et la coupe de
 * son objectif ; l'œil respire, la lumière traverse les lentilles.
 */
class Hal : Subject {
    override val title = "HAL 9000"
    override val reference = "2001: A SPACE ODYSSEY (1968) · DISCOVERY ONE"
    override val scale = "1:4"
    override val sheet = 4

    override fun draw(pen: Pen, width: Float, top: Float, bottom: Float, time: Float, tiltX: Float, tiltY: Float) {
        val d = pen.density
        val shift = tiltX * 8f * d
        pen.fit(900f, 2700f, 24 * d + shift, top, width * 0.46f + shift, bottom)
        panel(pen, time)
        pen.fit(1400f, 2700f, width * 0.5f - shift, top, width - 24 * d - shift, bottom)
        section(pen, time)
    }

    private fun panel(pen: Pen, time: Float) {
        val cx = 450f
        pen.rect(100f, 100f, 800f, 2500f, Weight.THICK)
        pen.rect(130f, 130f, 770f, 2470f, Weight.HAIR)
        // La plaque du nom.
        pen.rect(200f, 250f, 700f, 400f, Weight.MAIN)
        pen.text(cx, 350f, "HAL 9000", 9f, Paint.Align.CENTER, bold = true)
        // L'objectif : bague, lentille, œil.
        val eye = 1000f
        pen.circle(cx, eye, 300f, Weight.THICK)
        pen.circle(cx, eye, 250f, Weight.MAIN)
        pen.circle(cx, eye, 170f, Weight.THIN)
        pen.circle(cx, eye, 90f, Weight.HAIR)
        pen.axis(cx - 360f, eye, cx + 360f, eye)
        pen.axis(cx, eye - 360f, cx, eye + 360f)
        val breath = 0.75f + 0.25f * sin(time * 1.4f)
        pen.glowDot(cx, eye, 40f * breath, breath)
        pen.glowArc(cx, eye, 170f, -90f, 360f, 0.35f * breath, 1f)
        // La grille du haut-parleur.
        pen.rect(250f, 1700f, 650f, 2300f, Weight.MAIN)
        for (row in 0 until 12) {
            for (col in 0 until 8) pen.dot(290f + col * 46f, 1740f + row * 46f, 1.1f)
        }
        pen.dim(100f, 2500f, 800f, 2500f, -120f, "240")
        pen.dim(100f, 2500f, 100f, 100f, 100f, "810")
        pen.callout(cx + 200f, eye - 180f, 760f, 600f, 1)
        pen.callout(600f, 2000f, 820f, 2200f, 2)
        pen.caption("FRONT VIEW", "1 LENS  2 SPEAKER")
    }

    /** La coupe de l'objectif : les lentilles, l'axe, les rayons. */
    private fun section(pen: Pen, time: Float) {
        val axis = 1000f
        pen.axis(0f, axis, 1400f, axis)
        // Le fût, en coupe hachurée.
        pen.rect(250f, axis - 320f, 1250f, axis - 260f, Weight.MAIN)
        pen.rect(250f, axis + 260f, 1250f, axis + 320f, Weight.MAIN)
        pen.hatch(250f, axis - 320f, 1250f, axis - 260f, 5f)
        pen.hatch(250f, axis + 260f, 1250f, axis + 320f, 5f)
        // Les lentilles : une convergente, une divergente, une convergente.
        for ((x, r, bulge) in listOf(Triple(380f, 250f, 60f), Triple(650f, 200f, -40f), Triple(900f, 170f, 50f))) {
            pen.arc(x - bulge * 4f, axis, if (bulge > 0) r + bulge * 3f else r - bulge * 3f, -30f, 60f, Weight.MAIN)
            pen.arc(x + bulge * 4f, axis, if (bulge > 0) r + bulge * 3f else r - bulge * 3f, 150f, 60f, Weight.MAIN)
            pen.line(x - 20f, axis - r, x + 20f, axis - r, Weight.THIN)
            pen.line(x - 20f, axis + r, x + 20f, axis + r, Weight.THIN)
        }
        // Le capteur, au fond, qui s'allume.
        pen.rect(1150f, axis - 90f, 1210f, axis + 90f, Weight.MAIN)
        val breath = 0.75f + 0.25f * sin(time * 1.4f)
        pen.glowDot(1180f, axis, 26f, breath)
        // Des rayons qui convergent, en marche.
        val phase = (time * 0.6f) % 1f
        for (h in floatArrayOf(-200f, -110f, 110f, 200f)) {
            pen.glowLine(0f, axis + h, 380f, axis + h, 0.35f, 0.8f)
            pen.glowLine(380f, axis + h, 1180f, axis, 0.35f, 0.8f)
            val t = phase
            pen.glowDot(380f + (1180f - 380f) * t, axis + h * (1 - t), 8f, 0.9f)
        }
        pen.dim(250f, axis + 320f, 1250f, axis + 320f, -140f, "150")
        pen.callout(650f, axis - 200f, 650f, axis - 520f, 3)
        pen.callout(1180f, axis + 90f, 1250f, axis + 520f, 4)
        pen.caption("SECTION A-A", "3 OPTICS  4 SENSOR")
    }
}
