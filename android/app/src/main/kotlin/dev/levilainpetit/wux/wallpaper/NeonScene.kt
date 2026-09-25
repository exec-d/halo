package dev.levilainpetit.wux.wallpaper

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import kotlin.math.sin
import kotlin.random.Random

/**
 * Néon : une rue de ville du futur, la nuit. Enseignes verticales jaunes,
 * cyan, magenta et rouges de chaque côté, qui grésillent parfois, et leur
 * reflet sur le sol mouillé. De temps en temps, l'image « bugue » : bandes
 * décalées, couleurs séparées. La rue suit l'inclinaison du téléphone, les
 * enseignes proches bougeant plus que les lointaines.
 */
class NeonScene : LiveScene {

    override val colors = intArrayOf(YELLOW, CYAN, BACKGROUND)

    private var width = 1
    private var height = 1
    private var density = 1f
    private var last = 0L
    private var time = 0f
    private var nextGlitch = 3f
    private var glitchUntil = -1f
    private val random = Random(77)

    private var signs = emptyList<Sign>()
    private var camX = 0f
    private var camY = 0f
    private var vx = 0f
    private var vy = 0f
    private var focal = 1f

    private val fill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.SQUARE
    }
    private val path = Path()
    private val rect = RectF()
    private val sky = Paint()
    private val scanlines = Paint()
    private val tints = listOf(tint(1f, 0.2f, 0.3f), tint(0.2f, 1f, 1f))
    private var lockShade = Paint()

    override fun resize(width: Int, height: Int, density: Float) {
        this.width = width
        this.height = height
        this.density = density
        focal = width * 0.55f
        sky.shader = LinearGradient(
            0f, 0f, 0f, height * 0.5f,
            BACKGROUND, Color.rgb(40, 8, 44), Shader.TileMode.CLAMP,
        )
        val line = (3 * density).toInt().coerceAtLeast(2)
        val tile = Bitmap.createBitmap(1, line, Bitmap.Config.ARGB_8888)
        tile.setPixel(0, 0, Color.argb(40, 0, 0, 0))
        scanlines.shader = BitmapShader(tile, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
        // Des enseignes des deux côtés, de près jusqu'au fond de la rue.
        val palette = listOf(YELLOW, CYAN, MAGENTA, RED)
        signs = List(26) { i ->
            val left = i % 2 == 0
            val z = 1.4f + (i / 2) * 0.9f + random.nextFloat() * 0.4f
            val bottom = -0.1f + random.nextFloat() * 1.2f
            Sign(
                left = left,
                z = z,
                bottom = bottom,
                top = bottom + 0.6f + random.nextFloat() * 1.6f,
                color = palette[random.nextInt(palette.size)],
                marks = List(4 + random.nextInt(4)) { random.nextFloat() },
                flicker = random.nextFloat() < 0.25f,
            )
        }.sortedByDescending { it.z }
        lockShade = SceneKit.lockShade(height)
    }

    override fun draw(canvas: Canvas, frame: LiveFrame) {
        val dt = SceneKit.step(last, frame.timeMillis)
        last = frame.timeMillis
        time += dt
        val strength = SceneKit.strength(frame)
        camX = frame.tiltX * 0.35f
        camY = frame.tiltY * 0.25f
        vx = width / 2f
        vy = height * 0.46f

        drawStreet(canvas, strength)
        nextGlitch -= dt
        if (nextGlitch <= 0f) {
            glitchUntil = time + 0.18f + random.nextFloat() * 0.2f
            nextGlitch = 4f + random.nextFloat() * 6f
        }
        if (time < glitchUntil) drawGlitch(canvas, strength)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), scanlines)
        if (frame.locked) canvas.drawRect(0f, 0f, width.toFloat(), height * 0.34f, lockShade)
    }

    override fun nextFrameIn(frame: LiveFrame): Long = if (time < glitchUntil) 33L else 60L

    private fun sx(x: Float, z: Float) = vx + (x - camX) * focal / z
    private fun sy(y: Float, z: Float) = vy - (y - camY) * focal / z

    private fun drawStreet(canvas: Canvas, strength: Float) {
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), sky)
        // La lueur tout au fond de la rue.
        fill.shader = null
        fill.color = MAGENTA
        fill.alpha = (40 * strength).toInt()
        canvas.drawCircle(sx(0f, FAR), sy(1f, FAR), width * 0.35f, fill)

        // Façades, sol et arêtes, en perspective.
        drawFacade(canvas, -STREET)
        drawFacade(canvas, STREET)
        path.rewind()
        path.moveTo(sx(-STREET, NEAR), sy(GROUND, NEAR))
        path.lineTo(sx(STREET, NEAR), sy(GROUND, NEAR))
        path.lineTo(sx(STREET, FAR), sy(GROUND, FAR))
        path.lineTo(sx(-STREET, FAR), sy(GROUND, FAR))
        path.close()
        fill.color = Color.rgb(10, 8, 16)
        canvas.drawPath(path, fill)
        stroke.color = Color.rgb(70, 40, 90)
        stroke.alpha = (180 * strength).toInt()
        stroke.strokeWidth = 1f * density
        var z = NEAR
        while (z < FAR) {
            for (side in floatArrayOf(-STREET, STREET)) canvas.drawLine(sx(side, z), sy(GROUND, z), sx(side, z), sy(ROOF, z), stroke)
            z += 1.5f
        }

        // Les enseignes, du fond vers l'avant, et leur reflet sur le sol.
        for (sign in signs) drawSign(canvas, sign, strength)
    }

    private fun drawFacade(canvas: Canvas, x: Float) {
        path.rewind()
        path.moveTo(sx(x, NEAR), sy(GROUND, NEAR))
        path.lineTo(sx(x, NEAR), sy(ROOF, NEAR))
        path.lineTo(sx(x, FAR), sy(ROOF, FAR))
        path.lineTo(sx(x, FAR), sy(GROUND, FAR))
        path.close()
        fill.color = Color.rgb(14, 10, 22)
        fill.alpha = 255
        canvas.drawPath(path, fill)
    }

    private fun drawSign(canvas: Canvas, sign: Sign, strength: Float) {
        val inner = if (sign.left) -STREET + 0.05f else STREET - 0.05f
        val outer = if (sign.left) inner + 0.45f else inner - 0.45f
        val x0 = sx(inner, sign.z)
        val x1 = sx(outer, sign.z)
        val top = sy(sign.top, sign.z)
        val bottom = sy(sign.bottom, sign.z)
        rect.set(minOf(x0, x1), top, maxOf(x0, x1), bottom)
        val off = sign.flicker && sin(time * 17f + sign.z * 3f) > 0.6f
        val lit = if (off) 0.25f else 1f
        val fade = (1.3f - sign.z / FAR).coerceIn(0.2f, 1f)
        val alpha = strength * lit * fade

        fill.color = sign.color
        fill.alpha = (35 * alpha).toInt()
        canvas.drawRect(rect.left - rect.width() * 0.3f, rect.top - rect.width() * 0.3f, rect.right + rect.width() * 0.3f, rect.bottom + rect.width() * 0.3f, fill)
        fill.color = Color.rgb(8, 6, 12)
        fill.alpha = 255
        canvas.drawRect(rect, fill)
        stroke.color = sign.color
        stroke.alpha = (90 * alpha).toInt()
        stroke.strokeWidth = rect.width() * 0.12f + 2f * density
        canvas.drawRect(rect, stroke)
        stroke.alpha = (255 * alpha).toInt()
        stroke.strokeWidth = (rect.width() * 0.04f).coerceAtLeast(1f * density)
        canvas.drawRect(rect, stroke)
        // Des signes lumineux à l'intérieur, comme des caractères.
        val mark = rect.height() / (sign.marks.size + 1)
        for ((i, m) in sign.marks.withIndex()) {
            val y = rect.top + mark * (i + 0.6f)
            val w = rect.width() * (0.3f + m * 0.5f)
            canvas.drawLine(rect.centerX() - w / 2, y, rect.centerX() + w / 2, y + mark * 0.25f * m, stroke)
        }
        // Le reflet sur le sol mouillé.
        val ground = sy(GROUND, sign.z)
        val depth = bottom - top
        fill.color = sign.color
        fill.alpha = (45 * alpha).toInt()
        canvas.drawRect(rect.left, ground + (ground - bottom), rect.right, ground + (ground - bottom) + depth * 0.7f, fill)
    }

    /** Quelques bandes de l'image, décalées et teintées. */
    private fun drawGlitch(canvas: Canvas, strength: Float) {
        repeat(3 + random.nextInt(3)) {
            val top = random.nextFloat() * height
            val band = height * (0.01f + random.nextFloat() * 0.05f)
            rect.set(0f, top, width.toFloat(), top + band)
            canvas.saveLayer(rect, tints[random.nextInt(tints.size)])
            canvas.clipRect(rect)
            canvas.translate((random.nextFloat() - 0.5f) * width * 0.12f, 0f)
            drawStreet(canvas, strength)
            canvas.restore()
        }
        // Une barre jaune, comme une interface qui décroche.
        fill.color = YELLOW
        fill.alpha = (200 * strength).toInt()
        val y = random.nextFloat() * height
        canvas.drawRect(0f, y, width * random.nextFloat(), y + 3f * density, fill)
    }

    private fun tint(r: Float, g: Float, b: Float) = Paint().apply {
        colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setScale(r, g, b, 1f) })
    }

    private class Sign(
        val left: Boolean,
        val z: Float,
        val bottom: Float,
        val top: Float,
        val color: Int,
        val marks: List<Float>,
        val flicker: Boolean,
    )

    private companion object {
        val YELLOW = Color.rgb(252, 238, 10)
        val CYAN = Color.rgb(0, 240, 255)
        val MAGENTA = Color.rgb(255, 43, 214)
        val RED = Color.rgb(255, 0, 60)
        val BACKGROUND = Color.rgb(7, 6, 11)
        const val STREET = 1.6f
        const val GROUND = -1f
        const val ROOF = 6f
        const val NEAR = 0.8f
        const val FAR = 16f
    }
}
