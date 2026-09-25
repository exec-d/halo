package dev.levilainpetit.wux.wallpaper

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.SweepGradient
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Sentinelle : l'œil d'une intelligence artificielle, aux couleurs du
 * téléphone, dans son panneau de métal brossé : plaque d'identification,
 * voyants, grille de haut-parleur. Son cœur respire lentement et suit
 * l'inclinaison du téléphone, comme s'il vous regardait ; un anneau de
 * balayage part du centre de temps en temps, et l'œil s'avive quand on
 * déverrouille.
 */
class SentinelScene : LiveScene {

    override val colors: IntArray? get() = own

    private var own: IntArray? = null
    private var width = 1
    private var height = 1
    private var density = 1f
    private var last = 0L
    private var time = 0f
    private var wasLocked = false
    private var wake = 1f
    private var scan = 0f

    private var housing = 0f
    private var lens = 0f
    private var core = 0f
    private var eyeY = 0f
    private val panel = RectF()
    private val plate = RectF()
    private val grille = RectF()
    private var brush = FloatArray(0)

    private val fill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val metal = Paint(Paint.ANTI_ALIAS_FLAG)
    private val halo = Paint(Paint.ANTI_ALIAS_FLAG)
    private val body = Paint(Paint.ANTI_ALIAS_FLAG)
    private val glass = Paint(Paint.ANTI_ALIAS_FLAG)
    private val heart = Paint(Paint.ANTI_ALIAS_FLAG)
    private val shine = Paint(Paint.ANTI_ALIAS_FLAG)
    private var lockShade = Paint()
    private var shaded = 0

    override fun resize(width: Int, height: Int, density: Float) {
        this.width = width
        this.height = height
        this.density = density
        housing = width * 0.3f
        lens = housing * 0.68f
        core = lens * 0.24f
        eyeY = height * 0.44f
        panel.set(width * 0.14f, height * 0.12f, width * 0.86f, height * 0.9f)
        plate.set(width * 0.3f, height * 0.17f, width * 0.7f, height * 0.205f)
        grille.set(width * 0.27f, height * 0.64f, width * 0.73f, height * 0.84f)
        val r = Random(4)
        brush = FloatArray(160 * 2) { i -> if (i % 2 == 0) panel.top + r.nextFloat() * panel.height() else r.nextFloat() }
        body.shader = RadialGradient(
            0f, -housing * 0.3f, housing * 1.2f,
            Color.rgb(50, 50, 56), Color.rgb(8, 8, 10), Shader.TileMode.CLAMP,
        )
        ring.shader = SweepGradient(
            0f, 0f,
            intArrayOf(Color.rgb(90, 90, 98), Color.rgb(235, 235, 240), Color.rgb(70, 70, 78), Color.rgb(200, 200, 210), Color.rgb(90, 90, 98)),
            null,
        )
        metal.shader = LinearGradient(
            panel.left, 0f, panel.right, 0f,
            intArrayOf(Color.rgb(24, 25, 28), Color.rgb(46, 47, 52), Color.rgb(30, 31, 35), Color.rgb(20, 21, 24)),
            floatArrayOf(0f, 0.35f, 0.7f, 1f), Shader.TileMode.CLAMP,
        )
        shine.shader = RadialGradient(
            0f, 0f, lens * 0.22f,
            Color.argb(90, 255, 255, 255), Color.TRANSPARENT, Shader.TileMode.CLAMP,
        )
        lockShade = SceneKit.lockShade(height)
        shaded = 0
    }

    /** Les dégradés de l'œil, à la couleur du téléphone. */
    private fun prepare(eye: Int, hot: Int) {
        if (shaded == eye) return
        shaded = eye
        halo.shader = RadialGradient(
            0f, 0f, housing * 1.7f,
            intArrayOf(SceneKit.alpha(eye, 70), SceneKit.alpha(eye, 16), Color.TRANSPARENT),
            floatArrayOf(0f, 0.5f, 1f), Shader.TileMode.CLAMP,
        )
        glass.shader = RadialGradient(
            0f, 0f, lens,
            intArrayOf(SceneKit.hue(eye, 0f, 1f, 0.5f), SceneKit.hue(eye, 0f, 1f, 0.16f), Color.rgb(3, 2, 2)),
            floatArrayOf(0f, 0.55f, 1f), Shader.TileMode.CLAMP,
        )
        heart.shader = RadialGradient(
            0f, 0f, core,
            intArrayOf(Color.rgb(255, 252, 235), hot, eye, SceneKit.alpha(eye, 0)),
            floatArrayOf(0f, 0.18f, 0.5f, 1f), Shader.TileMode.CLAMP,
        )
    }

    override fun draw(canvas: Canvas, frame: LiveFrame) {
        val dt = SceneKit.step(last, frame.timeMillis)
        last = frame.timeMillis
        time += dt
        if (wasLocked && !frame.locked) {
            wake = 1f
            scan = 0f
        }
        wasLocked = frame.locked
        wake = (wake - dt / 1.6f).coerceAtLeast(0f)
        scan += dt / 7f
        if (scan > 1f) scan = 0f
        val strength = SceneKit.strength(frame)
        val eye = SceneKit.neon(frame.palette)
        val hot = SceneKit.mix(eye, frame.palette.core, 0.6f)
        own = intArrayOf(eye, Color.rgb(150, 150, 160), Color.BLACK)
        prepare(eye, hot)

        canvas.drawColor(Color.rgb(2, 2, 3))
        canvas.save()
        canvas.translate(-frame.tiltX * 6f * density, -frame.tiltY * 6f * density)
        drawPanel(canvas, strength, eye)
        canvas.restore()

        val cx = width / 2f - frame.tiltX * 10f * density
        val cy = eyeY - frame.tiltY * 10f * density
        canvas.save()
        canvas.translate(cx, cy)
        drawEye(canvas, frame, strength, eye)
        canvas.restore()
        if (frame.locked) canvas.drawRect(0f, 0f, width.toFloat(), height * 0.34f, lockShade)
    }

    override fun nextFrameIn(frame: LiveFrame): Long = 50L

    /** Le panneau de métal brossé, sa plaque, ses vis, ses voyants, sa grille. */
    private fun drawPanel(canvas: Canvas, strength: Float, eye: Int) {
        val radius = 18f * density
        canvas.drawRoundRect(panel, radius, radius, metal)
        // Le brossage : de fines lignes horizontales.
        stroke.color = Color.WHITE
        stroke.strokeWidth = 0.8f * density
        for (i in brush.indices step 2) {
            stroke.alpha = (6 + brush[i + 1] * 12).toInt()
            canvas.drawLine(panel.left + radius / 2, brush[i], panel.right - radius / 2, brush[i], stroke)
        }
        // Le biseau.
        stroke.strokeWidth = 1.5f * density
        stroke.alpha = 60
        canvas.drawRoundRect(panel, radius, radius, stroke)
        stroke.color = Color.BLACK
        stroke.alpha = 200
        stroke.strokeWidth = 3f * density
        val inset = RectF(panel.left - 2 * density, panel.top - 2 * density, panel.right + 2 * density, panel.bottom + 2 * density)
        canvas.drawRoundRect(inset, radius + 2 * density, radius + 2 * density, stroke)
        // Les vis, aux quatre coins.
        for ((x, y) in listOf(
            panel.left + 18 * density to panel.top + 18 * density,
            panel.right - 18 * density to panel.top + 18 * density,
            panel.left + 18 * density to panel.bottom - 18 * density,
            panel.right - 18 * density to panel.bottom - 18 * density,
        )) {
            fill.shader = null
            fill.color = Color.rgb(70, 70, 76)
            canvas.drawCircle(x, y, 5f * density, fill)
            stroke.color = Color.rgb(20, 20, 22)
            stroke.alpha = 255
            stroke.strokeWidth = 1.2f * density
            canvas.drawLine(x - 3.5f * density, y, x + 3.5f * density, y, stroke)
        }
        // La plaque d'identification, et un voyant qui clignote.
        fill.color = Color.rgb(10, 10, 12)
        canvas.drawRoundRect(plate, 4f * density, 4f * density, fill)
        stroke.color = Color.rgb(120, 120, 130)
        stroke.strokeWidth = 1f * density
        stroke.alpha = 120
        canvas.drawRoundRect(plate, 4f * density, 4f * density, stroke)
        fill.color = Color.rgb(200, 200, 210)
        fill.alpha = 170
        val bar = plate.height() * 0.28f
        var x = plate.left + plate.height() * 0.6f
        for (w in floatArrayOf(0.1f, 0.06f, 0.14f, 0.04f, 0.09f)) {
            val len = plate.width() * w
            canvas.drawRect(x, plate.centerY() - bar / 2, x + len, plate.centerY() + bar / 2, fill)
            x += len + plate.width() * 0.035f
        }
        fill.color = eye
        fill.alpha = ((if (sin(time * 3f) > 0f) 230 else 70) * strength).toInt()
        canvas.drawCircle(plate.right - plate.height() * 0.5f, plate.centerY(), plate.height() * 0.16f, fill)
        // Les voyants sur le côté, qui battent comme un calcul en cours.
        for (k in 0 until 8) {
            val y = panel.top + panel.height() * (0.3f + k * 0.035f)
            val level = 0.5f + 0.5f * sin(time * (2f + k * 0.7f) + k)
            fill.color = eye
            fill.alpha = ((30 + 170 * level) * strength).toInt()
            canvas.drawRoundRect(panel.right - 30 * density, y, panel.right - 18 * density, y + 3.5f * density, 1.5f * density, 1.5f * density, fill)
        }
        // La grille du haut-parleur.
        fill.color = Color.rgb(14, 14, 16)
        canvas.drawRoundRect(grille, 10f * density, 10f * density, fill)
        fill.color = Color.rgb(2, 2, 3)
        val hole = 2.2f * density
        val pitch = 8f * density
        var gy = grille.top + pitch
        var row = 0
        while (gy < grille.bottom - pitch / 2) {
            var gx = grille.left + pitch + (if (row % 2 == 0) 0f else pitch / 2)
            while (gx < grille.right - pitch / 2) {
                canvas.drawCircle(gx, gy, hole, fill)
                gx += pitch
            }
            gy += pitch * 0.87f
            row++
        }
        stroke.color = Color.WHITE
        stroke.alpha = 40
        stroke.strokeWidth = 1f * density
        canvas.drawRoundRect(grille, 10f * density, 10f * density, stroke)
    }

    private fun drawEye(canvas: Canvas, frame: LiveFrame, strength: Float, eye: Int) {
        halo.alpha = (255 * strength * (0.8f + 0.4f * wake)).toInt().coerceAtMost(255)
        canvas.drawCircle(0f, 0f, housing * 1.7f, halo)
        canvas.drawCircle(0f, 0f, housing, body)
        ring.strokeWidth = housing * 0.08f
        canvas.drawCircle(0f, 0f, housing * 0.92f, ring)
        ring.strokeWidth = housing * 0.025f
        canvas.drawCircle(0f, 0f, lens * 1.07f, ring)
        canvas.drawCircle(0f, 0f, lens, glass)

        // L'iris : des rayons fins qui tournent lentement.
        stroke.color = eye
        stroke.strokeWidth = 0.8f * density
        val spin = time * 0.08f
        for (k in 0 until 72) {
            val a = spin + k * (Math.PI * 2 / 72).toFloat()
            val inner = core * 1.1f
            val outer = lens * (0.55f + 0.1f * ((k * 7) % 5) / 4f)
            stroke.alpha = ((18 + (k % 3) * 10) * strength).toInt()
            canvas.drawLine(cos(a) * inner, sin(a) * inner, cos(a) * outer, sin(a) * outer, stroke)
        }
        // Les lentilles, cercles fins à l'intérieur du verre.
        for (i in 1..4) {
            stroke.alpha = (30 * strength).toInt()
            canvas.drawCircle(0f, 0f, lens * (0.3f + 0.17f * i), stroke)
        }

        // Le balayage, un anneau qui s'élargit en s'éteignant.
        if (scan < 0.35f) {
            val progress = scan / 0.35f
            stroke.strokeWidth = 2f * density
            stroke.alpha = (170 * (1 - progress) * strength).toInt()
            canvas.drawCircle(0f, 0f, core * 0.6f + (lens - core * 0.6f) * progress, stroke)
        }

        // Le cœur : il respire, s'avive au réveil, et suit l'inclinaison.
        val breath = 0.92f + 0.08f * sin(time * 1.6f)
        val look = lens * 0.18f
        canvas.save()
        canvas.translate(frame.tiltX * look, frame.tiltY * look)
        val scale = breath * (1f + 0.35f * wake)
        canvas.scale(scale, scale)
        heart.alpha = (255 * (0.55f + 0.45f * strength)).toInt()
        canvas.drawCircle(0f, 0f, core, heart)
        canvas.restore()

        // Des reflets sur la bague et sur le verre.
        stroke.color = Color.WHITE
        stroke.strokeWidth = 2f * density
        stroke.alpha = 120
        canvas.drawArc(RectF(-housing * 0.92f, -housing * 0.92f, housing * 0.92f, housing * 0.92f), 200f, 50f, false, stroke)
        stroke.alpha = 60
        canvas.drawArc(RectF(-lens * 1.07f, -lens * 1.07f, lens * 1.07f, lens * 1.07f), 20f, 35f, false, stroke)
        canvas.save()
        canvas.translate(-lens * 0.38f - frame.tiltX * look * 0.6f, -lens * 0.42f - frame.tiltY * look * 0.6f)
        canvas.scale(1.4f, 0.8f)
        canvas.drawCircle(0f, 0f, lens * 0.22f, shine)
        canvas.restore()
    }
}
