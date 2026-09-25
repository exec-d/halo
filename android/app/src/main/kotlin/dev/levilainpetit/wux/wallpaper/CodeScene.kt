package dev.levilainpetit.wux.wallpaper

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.Typeface
import kotlin.random.Random

/**
 * Code : une pluie de caractères aux couleurs du téléphone, en trois plans
 * de profondeur. Chaque colonne tombe à sa vitesse, tête blanche et
 * lumineuse, traîne qui s'éteint ; les caractères changent au passage, et
 * parfois une colonne s'embrase. La pluie tombe droit, sans suivre
 * l'inclinaison.
 */
class CodeScene : LiveScene {

    override val colors: IntArray? get() = own

    private var own: IntArray? = null
    private var width = 1
    private var height = 1
    private var density = 1f
    private var last = 0L
    private val random = Random(42)
    private var layers = emptyList<Layer>()
    private val glyph = CharArray(1)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.MONOSPACE
        textAlign = Paint.Align.CENTER
    }
    private val vignette = Paint()
    private val floor = Paint()
    private var lockShade = Paint()

    override fun resize(width: Int, height: Int, density: Float) {
        this.width = width
        this.height = height
        this.density = density
        // Du plus lointain (petit, sombre, lent) au plus proche.
        layers = listOf(
            Layer(size = 9f * density, bright = 0.35f, pace = 0.55f),
            Layer(size = 13f * density, bright = 0.62f, pace = 0.8f),
            Layer(size = 19f * density, bright = 1f, pace = 1.1f),
        )
        vignette.shader = RadialGradient(
            width / 2f, height * 0.45f, height * 0.75f,
            intArrayOf(Color.TRANSPARENT, Color.TRANSPARENT, Color.argb(200, 0, 0, 0)),
            floatArrayOf(0f, 0.55f, 1f), Shader.TileMode.CLAMP,
        )
        lockShade = SceneKit.lockShade(height)
        // Des colonnes déjà en route dès la première image.
        repeat(60) { for (layer in layers) layer.advance(0.1f) }
    }

    override fun draw(canvas: Canvas, frame: LiveFrame) {
        val dt = SceneKit.step(last, frame.timeMillis)
        last = frame.timeMillis
        val strength = SceneKit.strength(frame)
        val accent = SceneKit.neon(frame.palette)
        val head = frame.palette.core
        own = intArrayOf(accent, head, SceneKit.night(frame.palette, 0.02f))

        canvas.drawColor(SceneKit.night(frame.palette, 0.025f))
        // Une lueur au sol, comme si la pluie éclairait le bas de l'écran.
        floor.shader = LinearGradient(
            0f, height * 0.55f, 0f, height.toFloat(),
            Color.TRANSPARENT, SceneKit.alpha(accent, (40 * strength).toInt()), Shader.TileMode.CLAMP,
        )
        canvas.drawRect(0f, height * 0.55f, width.toFloat(), height.toFloat(), floor)
        for (layer in layers) {
            layer.advance(dt)
            layer.draw(canvas, strength, accent, head)
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), vignette)
        if (frame.locked) canvas.drawRect(0f, 0f, width.toFloat(), height * 0.34f, lockShade)
    }

    override fun nextFrameIn(frame: LiveFrame): Long = 50L

    /** Un plan de colonnes, de caractères de taille [size]. */
    private inner class Layer(val size: Float, val bright: Float, val pace: Float) {
        private val cell = size * 1.18f
        private val pitch = size * 1.02f
        private val columns = (width / pitch).toInt() + 2
        private val rows = (height / cell).toInt() + 2
        private val glyphs = Array(columns) { CharArray(rows) { pick() } }
        private val head = FloatArray(columns) { random.nextFloat() * rows * 1.6f - rows * 0.4f }
        private val speed = FloatArray(columns) { newSpeed() }
        private val length = IntArray(columns) { newLength() }

        /** Une colonne qui s'embrase : plus vive, jusqu'à la fin de sa chute. */
        private val burning = BooleanArray(columns)

        private fun newSpeed() = (6f + random.nextFloat() * 14f) * pace
        private fun newLength() = 12 + random.nextInt(30)

        fun advance(dt: Float) {
            for (c in 0 until columns) {
                head[c] += speed[c] * dt
                if (head[c] - length[c] > rows) {
                    head[c] = -random.nextFloat() * rows * 0.5f
                    speed[c] = newSpeed()
                    length[c] = newLength()
                    burning[c] = random.nextFloat() < 0.06f
                }
            }
            repeat((columns * rows * 0.012f).toInt().coerceAtLeast(1)) {
                glyphs[random.nextInt(columns)][random.nextInt(rows)] = pick()
            }
        }

        fun draw(canvas: Canvas, strength: Float, accent: Int, headColor: Int) {
            paint.textSize = size
            for (c in 0 until columns) {
                val x = c * pitch + pitch / 2
                val top = head[c].toInt()
                val boost = if (burning[c]) 1.6f else 1f
                for (i in 0..length[c]) {
                    val row = top - i
                    if (row < 0 || row >= rows) continue
                    val y = (row + 1) * cell
                    val fade = 1f - i / (length[c] + 1f)
                    glyph[0] = glyphs[c][row]
                    if (i == 0) {
                        // La tête : un halo, puis le caractère presque blanc.
                        paint.color = accent
                        paint.alpha = (90 * bright * strength).toInt()
                        paint.textSize = size * 1.35f
                        canvas.drawText(glyph, 0, 1, x, y + size * 0.12f, paint)
                        paint.textSize = size
                        paint.color = headColor
                        paint.alpha = (255 * bright * strength).toInt().coerceAtMost(255)
                    } else {
                        paint.color = if (i < 3) SceneKit.mix(headColor, accent, i / 3f) else accent
                        paint.alpha = (255 * Math.pow(fade.toDouble(), 1.4).toFloat() * bright * strength * boost).toInt().coerceAtMost(255)
                    }
                    canvas.drawText(glyph, 0, 1, x, y, paint)
                }
            }
        }
    }

    private fun pick(): Char = GLYPHS[random.nextInt(GLYPHS.length)]

    private companion object {
        const val GLYPHS = "ｱｲｳｴｵｶｷｸｹｺｻｼｽｾｿﾀﾁﾂﾃﾄﾅﾆﾇﾈﾉﾊﾋﾌﾍﾎﾏﾐﾑﾒﾓﾔﾕﾖﾗﾘﾙﾚﾛﾜﾝ0123456789Z:.=*+<>|"
    }
}
