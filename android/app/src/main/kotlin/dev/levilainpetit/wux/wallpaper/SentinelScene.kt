package dev.levilainpetit.wux.wallpaper

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.SweepGradient
import kotlin.math.sin

/**
 * Sentinelle : l'œil rouge d'une intelligence artificielle, dans son boîtier
 * de métal. Son cœur respire lentement et suit l'inclinaison du téléphone,
 * comme s'il vous regardait ; un anneau de balayage part du centre de temps
 * en temps, et l'œil s'avive quand on déverrouille.
 */
class SentinelScene : LiveScene {

    override val colors = intArrayOf(RED, Color.rgb(150, 150, 160), Color.BLACK)

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

    private val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val halo = Paint(Paint.ANTI_ALIAS_FLAG)
    private val body = Paint(Paint.ANTI_ALIAS_FLAG)
    private val glass = Paint(Paint.ANTI_ALIAS_FLAG)
    private val heart = Paint(Paint.ANTI_ALIAS_FLAG)
    private val shine = Paint(Paint.ANTI_ALIAS_FLAG)
    private var lockShade = Paint()

    override fun resize(width: Int, height: Int, density: Float) {
        this.width = width
        this.height = height
        this.density = density
        housing = width * 0.36f
        lens = housing * 0.66f
        core = lens * 0.24f
        // Dégradés dessinés autour de l'origine : on se place au centre avant de peindre.
        halo.shader = RadialGradient(
            0f, 0f, housing * 1.6f,
            intArrayOf(SceneKit.alpha(RED, 70), SceneKit.alpha(RED, 18), Color.TRANSPARENT),
            floatArrayOf(0f, 0.5f, 1f), Shader.TileMode.CLAMP,
        )
        body.shader = RadialGradient(
            0f, -housing * 0.3f, housing * 1.2f,
            Color.rgb(46, 46, 52), Color.rgb(8, 8, 10), Shader.TileMode.CLAMP,
        )
        ring.shader = SweepGradient(
            0f, 0f,
            intArrayOf(Color.rgb(90, 90, 98), Color.rgb(230, 230, 236), Color.rgb(70, 70, 78), Color.rgb(190, 190, 200), Color.rgb(90, 90, 98)),
            null,
        )
        glass.shader = RadialGradient(
            0f, 0f, lens,
            intArrayOf(Color.rgb(120, 8, 4), Color.rgb(40, 2, 2), Color.rgb(4, 0, 0)),
            floatArrayOf(0f, 0.55f, 1f), Shader.TileMode.CLAMP,
        )
        heart.shader = RadialGradient(
            0f, 0f, core,
            intArrayOf(Color.rgb(255, 250, 215), Color.rgb(255, 170, 60), RED, SceneKit.alpha(RED, 0)),
            floatArrayOf(0f, 0.18f, 0.5f, 1f), Shader.TileMode.CLAMP,
        )
        shine.shader = RadialGradient(
            0f, 0f, lens * 0.22f,
            Color.argb(90, 255, 255, 255), Color.TRANSPARENT, Shader.TileMode.CLAMP,
        )
        lockShade = SceneKit.lockShade(height)
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

        canvas.drawColor(Color.BLACK)
        val cx = width / 2f - frame.tiltX * 10f * density
        val cy = height * 0.5f - frame.tiltY * 10f * density
        canvas.save()
        canvas.translate(cx, cy)

        halo.alpha = (255 * strength * (0.8f + 0.4f * wake)).toInt().coerceAtMost(255)
        canvas.drawCircle(0f, 0f, housing * 1.6f, halo)
        canvas.drawCircle(0f, 0f, housing, body)
        ring.strokeWidth = housing * 0.07f
        canvas.drawCircle(0f, 0f, housing * 0.93f, ring)
        ring.strokeWidth = housing * 0.025f
        canvas.drawCircle(0f, 0f, lens * 1.06f, ring)
        canvas.drawCircle(0f, 0f, lens, glass)

        // Les lentilles, cercles fins à l'intérieur du verre.
        stroke.color = RED
        stroke.strokeWidth = 1f * density
        for (i in 1..4) {
            stroke.alpha = (28 * strength).toInt()
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

        // Un reflet sur le verre, qui glisse à l'opposé.
        canvas.save()
        canvas.translate(-lens * 0.38f - frame.tiltX * look * 0.6f, -lens * 0.42f - frame.tiltY * look * 0.6f)
        canvas.scale(1.4f, 0.8f)
        canvas.drawCircle(0f, 0f, lens * 0.22f, shine)
        canvas.restore()

        canvas.restore()
        if (frame.locked) canvas.drawRect(0f, 0f, width.toFloat(), height * 0.34f, lockShade)
    }

    override fun nextFrameIn(frame: LiveFrame): Long = 50L

    private companion object {
        val RED = Color.rgb(255, 36, 20)
    }
}
