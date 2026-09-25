package dev.levilainpetit.wux.wallpaper

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import kotlin.random.Random

/**
 * Code : une pluie de caractères verts, en deux plans. Chaque colonne tombe
 * à sa vitesse, sa tête blanche et sa traîne qui s'éteint ; les caractères
 * changent au passage. La pluie penche avec le téléphone, comme sous
 * l'effet de la gravité.
 */
class CodeScene : LiveScene {

    override val colors = intArrayOf(GREEN, HEAD, BACKGROUND)

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
    private var lockShade = Paint()

    override fun resize(width: Int, height: Int, density: Float) {
        this.width = width
        this.height = height
        this.density = density
        // Plus large que l'écran : la pluie penchée couvre encore les coins.
        layers = listOf(Layer(11f * density, 0.35f, 0.5f), Layer(17f * density, 1f, 1f))
        lockShade = SceneKit.lockShade(height)
    }

    override fun draw(canvas: Canvas, frame: LiveFrame) {
        val dt = SceneKit.step(last, frame.timeMillis)
        last = frame.timeMillis
        val strength = SceneKit.strength(frame)
        canvas.drawColor(BACKGROUND)
        canvas.save()
        canvas.rotate(frame.tiltX * 16f, width / 2f, height / 2f)
        for (layer in layers) {
            layer.advance(dt)
            canvas.save()
            canvas.translate(-frame.tiltX * 20f * density * layer.depth, 0f)
            layer.draw(canvas, strength)
            canvas.restore()
        }
        canvas.restore()
        if (frame.locked) canvas.drawRect(0f, 0f, width.toFloat(), height * 0.34f, lockShade)
    }

    override fun nextFrameIn(frame: LiveFrame): Long = 50L

    /** Un plan de colonnes, de taille de caractère [size], d'éclat [bright]. */
    private inner class Layer(val size: Float, val bright: Float, val depth: Float) {
        private val cell = size * 1.15f
        private val margin = width * 0.3f
        private val columns = ((width + margin * 2) / (size * 0.95f)).toInt()
        private val rows = ((height * 1.2f) / cell).toInt() + 1
        private val glyphs = Array(columns) { CharArray(rows) { pick() } }
        private val head = FloatArray(columns) { random.nextFloat() * rows * 1.5f - rows * 0.3f }
        private val speed = FloatArray(columns) { newSpeed() }
        private val length = IntArray(columns) { newLength() }

        private fun newSpeed() = (6f + random.nextFloat() * 14f) * bright.coerceAtLeast(0.6f)
        private fun newLength() = 6 + random.nextInt(22)

        fun advance(dt: Float) {
            for (c in 0 until columns) {
                head[c] += speed[c] * dt
                if (head[c] - length[c] > rows) {
                    head[c] = -random.nextFloat() * rows * 0.4f
                    speed[c] = newSpeed()
                    length[c] = newLength()
                }
            }
            // Quelques caractères changent à chaque image.
            repeat((columns * rows * 0.01f).toInt().coerceAtLeast(1)) {
                glyphs[random.nextInt(columns)][random.nextInt(rows)] = pick()
            }
        }

        fun draw(canvas: Canvas, strength: Float) {
            paint.textSize = size
            val top = -height * 0.1f
            for (c in 0 until columns) {
                val x = -margin + c * size * 0.95f + size / 2
                val h = head[c].toInt()
                for (i in 0..length[c]) {
                    val row = h - i
                    if (row < 0 || row >= rows) continue
                    val fade = 1f - i / (length[c] + 1f)
                    if (i == 0) {
                        paint.color = HEAD
                        paint.setShadowLayer(6f * density, 0f, 0f, GREEN)
                    } else {
                        paint.color = GREEN
                        paint.clearShadowLayer()
                    }
                    paint.alpha = (255 * fade * bright * strength).toInt()
                    glyph[0] = glyphs[c][row]
                    canvas.drawText(glyph, 0, 1, x, top + (row + 1) * cell, paint)
                }
            }
            paint.clearShadowLayer()
        }
    }

    private fun pick(): Char = GLYPHS[random.nextInt(GLYPHS.length)]

    private companion object {
        val GREEN = Color.rgb(0, 255, 70)
        val HEAD = Color.rgb(215, 255, 225)
        val BACKGROUND = Color.rgb(0, 6, 2)
        const val GLYPHS = "ｱｲｳｴｵｶｷｸｹｺｻｼｽｾｿﾀﾁﾂﾃﾄﾅﾆﾇﾈﾉﾊﾋﾌﾍﾎﾏﾐﾑﾒﾓﾔﾕﾖﾗﾘﾙﾚﾛﾜﾝ0123456789Z:.=*+<>|"
    }
}
