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
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import kotlin.math.sin
import kotlin.random.Random

/**
 * Néon : une rue de ville du futur, la nuit, aux couleurs du téléphone.
 * Façades aux fenêtres allumées, enseignes verticales qui grésillent,
 * banderoles et câbles au-dessus de la rue, un anneau holographique au bout,
 * le sol mouillé qui reflète tout, de la vapeur qui monte. De temps en temps,
 * l'image « bugue ». La rue suit l'inclinaison du téléphone, les plans
 * proches bougeant plus que le fond.
 */
class NeonScene : LiveScene {

    override val colors: IntArray? get() = own

    private var own: IntArray? = null
    private var width = 1
    private var height = 1
    private var density = 1f
    private var last = 0L
    private var time = 0f
    private var nextGlitch = 3f
    private var glitchUntil = -1f
    private val random = Random(77)

    private var signs = emptyList<Sign>()
    private var windows = FloatArray(0)
    private var banners = FloatArray(0)
    private var steam = FloatArray(0)
    private var camX = 0f
    private var camY = 0f
    private var vx = 0f
    private var vy = 0f
    private var focal = 1f

    private val fill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val path = Path()
    private val rect = RectF()
    private val sky = Paint()
    private val scanlines = Paint()
    private val tints = listOf(tint(1f, 0.25f, 0.35f), tint(0.25f, 1f, 1f))
    private var lockShade = Paint()
    private var shaded = 0
    private var palette = IntArray(4)

    override fun resize(width: Int, height: Int, density: Float) {
        this.width = width
        this.height = height
        this.density = density
        focal = width * 0.55f
        val line = (3 * density).toInt().coerceAtLeast(2)
        val tile = Bitmap.createBitmap(1, line, Bitmap.Config.ARGB_8888)
        tile.setPixel(0, 0, Color.argb(34, 0, 0, 0))
        scanlines.shader = BitmapShader(tile, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
        // Des enseignes des deux côtés, de près jusqu'au fond de la rue.
        signs = List(28) { i ->
            val left = i % 2 == 0
            val z = 2f + (i / 2) * 0.8f + random.nextFloat() * 0.4f
            val bottom = 0.1f + random.nextFloat() * 1.6f
            Sign(
                left = left,
                z = z,
                bottom = bottom,
                top = bottom + 0.8f + random.nextFloat() * 1.8f,
                hue = random.nextInt(4),
                glyphs = List(3 + random.nextInt(3)) {
                    val strokes = STROKES.shuffled(random).take(2 + random.nextInt(2))
                    FloatArray(strokes.size * 4) { k -> strokes[k / 4][k % 4] }
                },
                flicker = random.nextFloat() < 0.25f,
            )
        }.sortedByDescending { it.z }
        // Fenêtres des façades : côté, profondeur, étage, teinte.
        windows = FloatArray(260 * 4) { i ->
            when (i % 4) {
                0 -> if (random.nextBoolean()) -1f else 1f
                1 -> 2.2f + random.nextFloat() * (FAR - 2.2f)
                2 -> GROUND + 0.4f + random.nextFloat() * (ROOF - GROUND - 0.6f)
                else -> random.nextInt(4).toFloat()
            }
        }
        // Banderoles au-dessus de la rue : profondeur, hauteur, teinte.
        banners = floatArrayOf(3.2f, 3.6f, 1f, 6.5f, 4.4f, 2f, 10f, 3.9f, 0f)
        steam = FloatArray(6 * 3) { i ->
            when (i % 3) {
                0 -> (random.nextFloat() - 0.5f) * STREET * 1.6f
                1 -> 1.6f + random.nextFloat() * 8f
                else -> random.nextFloat() * 5f
            }
        }
        lockShade = SceneKit.lockShade(height)
        shaded = 0
    }

    override fun draw(canvas: Canvas, frame: LiveFrame) {
        val dt = SceneKit.step(last, frame.timeMillis)
        last = frame.timeMillis
        time += dt
        val strength = SceneKit.strength(frame)
        palette = intArrayOf(
            SceneKit.neon(frame.palette),
            SceneKit.second(frame.palette),
            SceneKit.third(frame.palette),
            frame.palette.core,
        )
        val night = SceneKit.night(frame.palette, 0.04f)
        own = intArrayOf(palette[0], palette[1], night)
        prepare(night)
        camX = frame.tiltX * 0.35f
        camY = frame.tiltY * 0.25f
        vx = width / 2f
        vy = height * 0.44f

        drawStreet(canvas, strength, night, dt)
        nextGlitch -= dt
        if (nextGlitch <= 0f) {
            glitchUntil = time + 0.18f + random.nextFloat() * 0.2f
            nextGlitch = 5f + random.nextFloat() * 7f
        }
        if (time < glitchUntil) drawGlitch(canvas, strength, night)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), scanlines)
        if (frame.locked) canvas.drawRect(0f, 0f, width.toFloat(), height * 0.34f, lockShade)
    }

    override fun nextFrameIn(frame: LiveFrame): Long = if (time < glitchUntil) 33L else 50L

    private fun prepare(night: Int) {
        if (shaded == night) return
        shaded = night
        sky.shader = LinearGradient(
            0f, 0f, 0f, height * 0.5f,
            night, SceneKit.mix(night, palette[2], 0.3f), Shader.TileMode.CLAMP,
        )
    }

    private fun sx(x: Float, z: Float) = vx + (x - camX) * focal / z
    private fun sy(y: Float, z: Float) = vy - (y - camY) * focal / z

    private fun drawStreet(canvas: Canvas, strength: Float, night: Int, dt: Float) {
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), sky)
        drawSkyline(canvas, strength, night)
        drawFacade(canvas, -STREET, night)
        drawFacade(canvas, STREET, night)
        drawWindows(canvas, strength)
        drawGround(canvas, strength, night)
        for ((i, sign) in signs.withIndex()) {
            drawSign(canvas, sign, strength)
            if (i % 7 == 3) drawCable(canvas, sign.z + 0.3f, strength)
        }
        drawBanners(canvas, strength)
        drawSteam(canvas, strength, dt)
    }

    /** Au bout de la rue : des tours lointaines et un anneau holographique. */
    private fun drawSkyline(canvas: Canvas, strength: Float, night: Int) {
        val x = sx(0f, FAR)
        val y = sy(2.5f, FAR)
        fill.shader = RadialGradient(
            x, y, width * 0.45f,
            SceneKit.alpha(palette[0], (70 * strength).toInt()), Color.TRANSPARENT, Shader.TileMode.CLAMP,
        )
        canvas.drawCircle(x, y, width * 0.45f, fill)
        fill.shader = null
        fill.color = SceneKit.mix(night, Color.BLACK, 0.2f)
        for (k in -4..4) {
            val tw = width * 0.035f
            val th = height * (0.1f + ((k * 7 + 11) % 5) * 0.03f)
            val bx = x + k * tw * 1.1f
            canvas.drawRect(bx - tw / 2, sy(GROUND, FAR) - th, bx + tw / 2, sy(GROUND, FAR), fill)
        }
        val radius = width * 0.09f
        val spin = sin(time * 0.4f)
        stroke.color = palette[0]
        for ((size, alpha) in listOf(10f to 30, 2f to 200)) {
            stroke.strokeWidth = size * density
            stroke.alpha = (alpha * strength).toInt()
            canvas.drawOval(RectF(x - radius, y - radius * (0.35f + 0.1f * spin), x + radius, y + radius * (0.35f + 0.1f * spin)), stroke)
        }
        stroke.color = palette[1]
        stroke.strokeWidth = 1.5f * density
        stroke.alpha = (170 * strength).toInt()
        canvas.drawOval(RectF(x - radius * 0.6f, y - radius * 0.7f, x + radius * 0.6f, y + radius * 0.7f), stroke)
    }

    private fun drawFacade(canvas: Canvas, x: Float, night: Int) {
        path.rewind()
        path.moveTo(sx(x, NEAR), sy(GROUND, NEAR))
        path.lineTo(sx(x, NEAR), sy(ROOF, NEAR))
        path.lineTo(sx(x, FAR), sy(ROOF, FAR))
        path.lineTo(sx(x, FAR), sy(GROUND, FAR))
        path.close()
        fill.shader = null
        fill.color = SceneKit.mix(night, Color.BLACK, 0.15f)
        canvas.drawPath(path, fill)
        // Étages et arêtes des immeubles.
        stroke.color = SceneKit.mix(night, palette[2], 0.35f)
        stroke.strokeWidth = 1f * density
        stroke.alpha = 170
        var z = NEAR
        while (z < FAR) {
            canvas.drawLine(sx(x, z), sy(GROUND, z), sx(x, z), sy(ROOF, z), stroke)
            z += 1.4f
        }
        var y = GROUND + 1f
        stroke.alpha = 90
        while (y < ROOF) {
            canvas.drawLine(sx(x, NEAR), sy(y, NEAR), sx(x, FAR), sy(y, FAR), stroke)
            y += 0.8f
        }
    }

    private fun drawWindows(canvas: Canvas, strength: Float) {
        fill.shader = null
        for (i in windows.indices step 4) {
            val side = windows[i] * STREET
            val z = windows[i + 1]
            val y = windows[i + 2]
            val depth = 0.25f
            fill.color = palette[windows[i + 3].toInt()]
            val flicker = if (((i / 4) * 31 + (time * 2).toInt()) % 97 == 0) 0.2f else 1f
            fill.alpha = ((150 - z * 6f).coerceAtLeast(40f) * strength * flicker).toInt()
            path.rewind()
            path.moveTo(sx(side, z), sy(y, z))
            path.lineTo(sx(side, z + depth), sy(y, z + depth))
            path.lineTo(sx(side, z + depth), sy(y + 0.28f, z + depth))
            path.lineTo(sx(side, z), sy(y + 0.28f, z))
            path.close()
            canvas.drawPath(path, fill)
        }
    }

    /** Le sol mouillé : bitume, marquage, reflets. */
    private fun drawGround(canvas: Canvas, strength: Float, night: Int) {
        path.rewind()
        path.moveTo(sx(-STREET, NEAR), sy(GROUND, NEAR))
        path.lineTo(sx(STREET, NEAR), sy(GROUND, NEAR))
        path.lineTo(sx(STREET, FAR), sy(GROUND, FAR))
        path.lineTo(sx(-STREET, FAR), sy(GROUND, FAR))
        path.close()
        fill.shader = null
        fill.color = SceneKit.mix(night, Color.BLACK, 0.35f)
        canvas.drawPath(path, fill)
        // Le reflet du ciel au bout de la rue.
        fill.shader = LinearGradient(
            0f, sy(GROUND, FAR), 0f, height.toFloat(),
            SceneKit.alpha(palette[0], (60 * strength).toInt()), Color.TRANSPARENT, Shader.TileMode.CLAMP,
        )
        canvas.drawPath(path, fill)
        fill.shader = null
        // Les trottoirs, le long des façades.
        for (side in floatArrayOf(-1f, 1f)) {
            val curb = side * (STREET - 0.45f)
            stroke.color = SceneKit.mix(night, palette[3], 0.25f)
            stroke.strokeWidth = 1.2f * density
            stroke.alpha = (150 * strength).toInt()
            canvas.drawLine(sx(curb, NEAR), sy(GROUND, NEAR), sx(curb, FAR), sy(GROUND, FAR), stroke)
        }
        // Des flaques, qui reflètent les couleurs de la rue.
        for (k in 0 until 5) {
            val z = 1.2f + k * 1.7f
            val x = ((k * 37) % 7 - 3) * 0.28f
            val cx = sx(x, z)
            val cy = sy(GROUND, z)
            val rx = focal / z * 0.45f
            val ry = rx * 0.12f
            val color = palette[k % 4]
            fill.shader = RadialGradient(
                cx, cy, rx,
                SceneKit.alpha(color, (60 * strength).toInt()), Color.TRANSPARENT, Shader.TileMode.CLAMP,
            )
            canvas.drawOval(RectF(cx - rx, cy - ry, cx + rx, cy + ry), fill)
        }
        fill.shader = null
        // Le marquage au milieu, en pointillés.
        stroke.color = palette[3]
        stroke.strokeWidth = 2f * density
        var z = NEAR + (time * 1.2f) % 1f
        while (z < FAR) {
            stroke.alpha = ((110 - z * 6f).coerceAtLeast(20f) * strength).toInt()
            canvas.drawLine(sx(0f, z), sy(GROUND, z), sx(0f, z + 0.4f), sy(GROUND, z + 0.4f), stroke)
            z += 1f
        }
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
        val lit = if (off) 0.2f else 1f
        val fade = (1.25f - sign.z / FAR).coerceIn(0.25f, 1f)
        val alpha = strength * lit * fade
        val color = palette[sign.hue]

        // Le reflet sur le sol mouillé, étiré vers le bas.
        val ground = sy(GROUND, sign.z)
        val reach = ((bottom - top) * 1.6f).coerceAtMost(height * 0.3f)
        fill.shader = LinearGradient(
            0f, ground, 0f, ground + reach,
            SceneKit.alpha(color, (70 * alpha).toInt()), Color.TRANSPARENT, Shader.TileMode.CLAMP,
        )
        canvas.drawRect(rect.left, ground, rect.right, ground + reach, fill)
        fill.shader = null

        // Le halo, le caisson, le cadre.
        stroke.color = color
        val w = rect.width()
        for ((size, a) in listOf(0.5f to 22, 0.25f to 45)) {
            stroke.strokeWidth = w * size + 2f * density
            stroke.alpha = (a * alpha).toInt()
            canvas.drawRect(rect, stroke)
        }
        fill.color = Color.rgb(8, 6, 12)
        canvas.drawRect(rect, fill)
        stroke.strokeWidth = (w * 0.05f).coerceAtLeast(1f * density)
        stroke.alpha = (255 * alpha).toInt()
        canvas.drawRect(rect, stroke)

        // Les caractères, empilés.
        val cell = w * 0.6f
        val step = (rect.height() - cell * 0.3f) / sign.glyphs.size
        if (cell < 2f) return
        stroke.strokeWidth = (w * 0.07f).coerceAtLeast(0.8f * density)
        for ((i, glyph) in sign.glyphs.withIndex()) {
            val gx = rect.centerX() - cell / 2
            val gy = rect.top + cell * 0.2f + i * step
            for (k in glyph.indices step 4) {
                canvas.drawLine(gx + glyph[k] * cell, gy + glyph[k + 1] * cell, gx + glyph[k + 2] * cell, gy + glyph[k + 3] * cell, stroke)
            }
        }
    }

    /** Un câble qui pend d'une façade à l'autre. */
    private fun drawCable(canvas: Canvas, z: Float, strength: Float) {
        path.rewind()
        val y = ROOF - 1.2f - (z % 1.5f)
        path.moveTo(sx(-STREET, z), sy(y, z))
        path.quadTo(sx(0f, z), sy(y - 0.9f, z), sx(STREET, z), sy(y, z))
        stroke.color = Color.rgb(4, 3, 6)
        stroke.strokeWidth = 1.6f * density
        stroke.alpha = (220 * strength).toInt()
        canvas.drawPath(path, stroke)
    }

    /** Des banderoles lumineuses tendues au-dessus de la rue. */
    private fun drawBanners(canvas: Canvas, strength: Float) {
        for (i in banners.indices step 3) {
            val z = banners[i]
            val y = banners[i + 1]
            val color = palette[banners[i + 2].toInt()]
            val left = sx(-STREET * 0.8f, z)
            val right = sx(STREET * 0.8f, z)
            val top = sy(y + 0.35f, z)
            val bottom = sy(y, z)
            rect.set(left, top, right, bottom)
            stroke.color = color
            stroke.strokeWidth = rect.height() * 0.5f
            stroke.alpha = (28 * strength).toInt()
            canvas.drawRect(rect, stroke)
            fill.shader = null
            fill.color = Color.rgb(8, 6, 12)
            canvas.drawRect(rect, fill)
            stroke.strokeWidth = (rect.height() * 0.08f).coerceAtLeast(1f * density)
            stroke.alpha = (230 * strength).toInt()
            canvas.drawRect(rect, stroke)
            // Un texte lumineux qui défile.
            canvas.save()
            canvas.clipRect(rect)
            val unit = rect.height()
            val offset = (time * unit * 1.2f) % (unit * 3)
            fill.color = color
            fill.alpha = (210 * strength).toInt()
            var x = rect.left - offset
            var k = 0
            while (x < rect.right) {
                val wide = unit * (0.4f + ((k * 5 + i) % 4) * 0.25f)
                canvas.drawRect(x, rect.top + unit * 0.3f, x + wide, rect.bottom - unit * 0.3f, fill)
                x += wide + unit * 0.35f
                k++
            }
            canvas.restore()
        }
    }

    /** De la vapeur qui monte des bouches d'égout. */
    private fun drawSteam(canvas: Canvas, strength: Float, dt: Float) {
        for (i in steam.indices step 3) {
            steam[i + 2] += dt
            val life = (steam[i + 2] % 5f) / 5f
            val x = sx(steam[i], steam[i + 1])
            val y = sy(GROUND + life * 2.2f, steam[i + 1])
            val r = focal / steam[i + 1] * (0.3f + life * 0.8f)
            fill.shader = RadialGradient(
                x, y, r,
                Color.argb((60 * (1 - life) * strength).toInt(), 220, 220, 235), Color.TRANSPARENT, Shader.TileMode.CLAMP,
            )
            canvas.drawCircle(x, y, r, fill)
        }
        fill.shader = null
    }

    /** Quelques bandes de l'image, décalées et teintées. */
    private fun drawGlitch(canvas: Canvas, strength: Float, night: Int) {
        repeat(3 + random.nextInt(3)) {
            val top = random.nextFloat() * height
            val band = height * (0.01f + random.nextFloat() * 0.05f)
            rect.set(0f, top, width.toFloat(), top + band)
            canvas.saveLayer(rect, tints[random.nextInt(tints.size)])
            canvas.clipRect(rect)
            canvas.translate((random.nextFloat() - 0.5f) * width * 0.12f, 0f)
            drawStreet(canvas, strength, night, 0f)
            canvas.restore()
        }
        // Une barre d'interface qui décroche.
        fill.shader = null
        fill.color = palette[0]
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
        val hue: Int,
        val glyphs: List<FloatArray>,
        val flicker: Boolean,
    )

    private companion object {
        const val STREET = 1.6f
        const val GROUND = -1f
        const val ROOF = 6f
        const val NEAR = 0.38f
        const val FAR = 16f

        val STROKES = listOf(
            floatArrayOf(0.15f, 0.2f, 0.85f, 0.2f),
            floatArrayOf(0.2f, 0.5f, 0.8f, 0.5f),
            floatArrayOf(0.1f, 0.85f, 0.9f, 0.85f),
            floatArrayOf(0.5f, 0.1f, 0.5f, 0.9f),
            floatArrayOf(0.25f, 0.2f, 0.25f, 0.8f),
            floatArrayOf(0.8f, 0.25f, 0.3f, 0.9f),
            floatArrayOf(0.3f, 0.35f, 0.7f, 0.65f),
            floatArrayOf(0.7f, 0.1f, 0.85f, 0.3f),
        )
    }
}
