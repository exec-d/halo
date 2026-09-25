package dev.levilainpetit.wux.wallpaper

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Mégapole : une ville immense sous la pluie, la nuit, dans un smog orangé.
 * Trois plans de tours (les plus proches bougent le plus quand on penche le
 * téléphone), une pyramide au loin, des torchères qui crachent leur flamme,
 * des voitures volantes, deux projecteurs qui balaient le ciel et un grand
 * panneau lumineux qui change de couleur.
 */
class MegacityScene : LiveScene {

    override val colors = intArrayOf(AMBER, TEAL, BACKGROUND)

    private var width = 1
    private var height = 1
    private var density = 1f
    private var last = 0L
    private var time = 0f
    private val random = Random(19)

    private var layers = emptyList<Skyline>()
    private val pyramid = Path()
    private var pyramidBands = FloatArray(0)
    private var flares = emptyList<Flare>()
    private var cars = emptyList<Car>()
    private var rain = FloatArray(0)
    private var billboard = FloatArray(4)
    private var glyphs = emptyList<FloatArray>()
    private val sign = RectF()

    private val fill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val sky = Paint()
    private val haze = Paint()
    private val beam = Path()
    private val beamPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var lockShade = Paint()

    override fun resize(width: Int, height: Int, density: Float) {
        this.width = width
        this.height = height
        this.density = density
        val ground = height * 0.78f
        sky.shader = LinearGradient(
            0f, 0f, 0f, ground,
            intArrayOf(BACKGROUND, Color.rgb(22, 14, 24), Color.rgb(64, 30, 14)),
            floatArrayOf(0f, 0.55f, 1f), Shader.TileMode.CLAMP,
        )
        haze.shader = LinearGradient(
            0f, height * 0.45f, 0f, height.toFloat(),
            SceneKit.alpha(Color.rgb(120, 60, 25), 0), SceneKit.alpha(Color.rgb(120, 60, 25), 110), Shader.TileMode.CLAMP,
        )
        layers = listOf(
            Skyline(depth = 0.25f, base = ground - height * 0.08f, tall = height * 0.30f, wide = width * 0.05f, color = Color.rgb(28, 18, 26), lit = 0.12f, windowAlpha = 70),
            Skyline(depth = 0.55f, base = ground, tall = height * 0.42f, wide = width * 0.09f, color = Color.rgb(15, 11, 18), lit = 0.18f, windowAlpha = 150),
            Skyline(depth = 1f, base = height * 1.02f, tall = height * 0.55f, wide = width * 0.16f, color = Color.rgb(6, 5, 9), lit = 0.08f, windowAlpha = 220),
        )
        // La pyramide, au loin, et ses bandes de lumière.
        val cx = width * 0.62f
        val base = ground - height * 0.06f
        val half = width * 0.34f
        val top = base - height * 0.24f
        pyramid.rewind()
        pyramid.moveTo(cx - half, base)
        pyramid.lineTo(cx - half * 0.12f, top)
        pyramid.lineTo(cx + half * 0.12f, top)
        pyramid.lineTo(cx + half, base)
        pyramid.close()
        pyramidBands = FloatArray(7) { i -> top + (base - top) * (i + 1) / 8f }
        // Une enseigne verticale, fixée à une tour : cinq caractères lumineux.
        billboard = floatArrayOf(width * 0.12f, height * 0.33f, width * 0.2f, height * 0.53f)
        glyphs = List(5) {
            val strokes = STROKES.shuffled(random).take(2 + random.nextInt(2))
            FloatArray(strokes.size * 4) { i -> strokes[i / 4][i % 4] }
        }
        flares = List(3) { i ->
            Flare(x = width * (0.2f + 0.3f * i) + random.nextFloat() * width * 0.1f, y = ground - height * (0.1f + random.nextFloat() * 0.05f), next = 1f + i * 2.7f)
        }
        cars = List(4) { i ->
            Car(y = height * (0.2f + 0.12f * i), speed = (40f + random.nextFloat() * 60f) * density * (if (i % 2 == 0) 1f else -1f), x = random.nextFloat() * width)
        }
        rain = FloatArray(180 * 3) { i ->
            when (i % 3) {
                0 -> random.nextFloat() * width * 1.3f
                1 -> random.nextFloat() * height
                else -> 0.5f + random.nextFloat() * 0.5f
            }
        }
        lockShade = SceneKit.lockShade(height)
    }

    override fun draw(canvas: Canvas, frame: LiveFrame) {
        val dt = SceneKit.step(last, frame.timeMillis)
        last = frame.timeMillis
        time += dt
        val strength = SceneKit.strength(frame)
        val shift = 34f * density

        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), sky)
        drawBeams(canvas, strength)

        // Pyramide, au plus loin.
        canvas.save()
        canvas.translate(-frame.tiltX * shift * 0.15f, -frame.tiltY * shift * 0.1f)
        fill.color = Color.rgb(20, 13, 16)
        canvas.drawPath(pyramid, fill)
        canvas.save()
        canvas.clipPath(pyramid)
        fill.color = AMBER
        for ((i, y) in pyramidBands.withIndex()) {
            fill.alpha = ((50 + 30 * sin(time * 0.7f + i)) * strength).toInt()
            canvas.drawRect(0f, y, width.toFloat(), y + 1.6f * density, fill)
        }
        canvas.restore()
        canvas.restore()

        for ((i, layer) in layers.withIndex()) {
            canvas.save()
            canvas.translate(-frame.tiltX * shift * layer.depth, -frame.tiltY * shift * 0.4f * layer.depth)
            layer.draw(canvas, strength)
            if (i == 0) drawFlares(canvas, dt, strength)
            if (i == 1) {
                drawBillboard(canvas, strength)
                drawCars(canvas, dt, strength)
            }
            canvas.restore()
        }
        canvas.drawRect(0f, height * 0.45f, width.toFloat(), height.toFloat(), haze)
        drawRain(canvas, dt, frame, strength)
        if (frame.locked) canvas.drawRect(0f, 0f, width.toFloat(), height * 0.34f, lockShade)
    }

    override fun nextFrameIn(frame: LiveFrame): Long = 40L

    private fun drawBeams(canvas: Canvas, strength: Float) {
        for (i in 0..1) {
            val originX = width * (0.3f + 0.45f * i)
            val originY = height * 0.8f
            val angle = (-PI / 2 + sin(time * 0.18f + i * 2.1f) * 0.55f).toFloat()
            val reach = height * 0.9f
            val spread = 0.06f
            beam.rewind()
            beam.moveTo(originX, originY)
            beam.lineTo(originX + cos(angle - spread) * reach, originY + sin(angle - spread) * reach)
            beam.lineTo(originX + cos(angle + spread) * reach, originY + sin(angle + spread) * reach)
            beam.close()
            beamPaint.shader = LinearGradient(
                originX, originY, originX + cos(angle) * reach, originY + sin(angle) * reach,
                SceneKit.alpha(TEAL, (70 * strength).toInt()), SceneKit.alpha(TEAL, 0), Shader.TileMode.CLAMP,
            )
            canvas.drawPath(beam, beamPaint)
        }
    }

    private fun drawFlares(canvas: Canvas, dt: Float, strength: Float) {
        for (flare in flares) {
            // Le brûleur, toujours là.
            fill.shader = null
            fill.color = AMBER
            fill.alpha = (150 * strength).toInt()
            canvas.drawCircle(flare.x, flare.y, 1.8f * density, fill)
            flare.next -= dt
            if (flare.next <= 0f) {
                flare.age = 0f
                flare.next = 5f + random.nextFloat() * 7f
            }
            if (flare.age < 0f) continue
            flare.age += dt
            val life = flare.age / 1.4f
            if (life >= 1f) {
                flare.age = -1f
                continue
            }
            val radius = density * (10f + 38f * life)
            val y = flare.y - density * 30f * life
            fill.shader = RadialGradient(
                flare.x, y, radius,
                intArrayOf(Color.argb((230 * (1 - life) * strength).toInt(), 255, 220, 150), SceneKit.alpha(Color.rgb(255, 110, 20), (160 * (1 - life) * strength).toInt()), Color.TRANSPARENT),
                floatArrayOf(0f, 0.45f, 1f), Shader.TileMode.CLAMP,
            )
            canvas.drawCircle(flare.x, y, radius, fill)
            fill.shader = null
        }
    }

    private fun drawBillboard(canvas: Canvas, strength: Float) {
        val (left, top, right, bottom) = billboard.toList()
        // Du magenta au cyan, lentement ; un grésillement de temps en temps.
        val mix = (0.5f + 0.5f * sin(time * 0.6f)).coerceIn(0f, 1f)
        val color = blend(PINK, TEAL, mix)
        val flicker = if (sin(time * 23f) > 0.97f || sin(time * 7.3f) > 0.995f) 0.3f else 1f
        val glow = strength * flicker
        val radius = 3f * density

        // La tour qui porte l'enseigne, avec quelques fenêtres.
        fill.shader = null
        val towerRight = left - 10 * density
        val towerLeft = towerRight - width * 0.11f
        val towerTop = top - height * 0.035f
        fill.color = Color.rgb(15, 11, 18)
        canvas.drawRect(towerLeft, towerTop, towerRight, height.toFloat(), fill)
        val window = 2.2f * density
        var wy = towerTop + 8 * density
        var row = 0
        while (wy < height) {
            var wx = towerLeft + 5 * density
            var column = 0
            while (wx < towerRight - 5 * density) {
                if ((row * 7 + column * 13) % 11 == 0) {
                    fill.color = if ((row + column) % 3 == 0) TEAL else AMBER
                    fill.alpha = (150 * strength).toInt()
                    canvas.drawRect(wx, wy, wx + window, wy + window * 0.8f, fill)
                }
                wx += 7 * density
                column++
            }
            wy += 10 * density
            row++
        }

        // Les attaches, puis le halo autour du caisson.
        fill.color = Color.rgb(10, 8, 14)
        canvas.drawRect(left - 10 * density, top + 12 * density, left, top + 16 * density, fill)
        canvas.drawRect(left - 10 * density, bottom - 16 * density, left, bottom - 12 * density, fill)
        sign.set(left, top, right, bottom)
        stroke.color = color
        for ((size, alpha) in listOf(14f to 18, 8f to 34, 4f to 70)) {
            stroke.strokeWidth = size * density
            stroke.alpha = (alpha * glow).toInt()
            canvas.drawRoundRect(sign, radius, radius, stroke)
        }
        fill.color = Color.rgb(11, 7, 16)
        canvas.drawRoundRect(sign, radius, radius, fill)
        stroke.strokeWidth = 1.6f * density
        stroke.alpha = (230 * glow).toInt()
        canvas.drawRoundRect(sign, radius, radius, stroke)

        // Les caractères, allumés un à un de haut en bas, puis tous ensemble.
        val cell = (right - left) * 0.62f
        val step = (bottom - top - cell * 0.3f) / glyphs.size
        val chase = ((time * 1.6f) % (glyphs.size + 3)).toInt()
        for ((i, glyph) in glyphs.withIndex()) {
            val x0 = (left + right) / 2 - cell / 2
            val y0 = top + cell * 0.25f + i * step
            val on = if (chase < glyphs.size) i <= chase else true
            val level = if (on) 1f else 0.25f
            for ((size, alpha) in listOf(6f to 40, 2.2f to 255)) {
                stroke.strokeWidth = size * density
                stroke.alpha = (alpha * glow * level).toInt()
                for (k in glyph.indices step 4) {
                    canvas.drawLine(
                        x0 + glyph[k] * cell, y0 + glyph[k + 1] * cell,
                        x0 + glyph[k + 2] * cell, y0 + glyph[k + 3] * cell,
                        stroke,
                    )
                }
            }
        }
    }

    /** Entre [a] (0) et [b] (1). */
    private fun blend(a: Int, b: Int, t: Float): Int = Color.rgb(
        (Color.red(a) + (Color.red(b) - Color.red(a)) * t).toInt(),
        (Color.green(a) + (Color.green(b) - Color.green(a)) * t).toInt(),
        (Color.blue(a) + (Color.blue(b) - Color.blue(a)) * t).toInt(),
    )

    private fun drawCars(canvas: Canvas, dt: Float, strength: Float) {
        for (car in cars) {
            car.x += car.speed * dt
            if (car.x > width * 1.2f) car.x = -width * 0.2f
            if (car.x < -width * 0.2f) car.x = width * 1.2f
            val direction = if (car.speed > 0) 1f else -1f
            // Le faisceau des phares, puis la carrosserie lumineuse.
            stroke.color = Color.rgb(255, 240, 210)
            stroke.alpha = (40 * strength).toInt()
            stroke.strokeWidth = 6f * density
            canvas.drawLine(car.x, car.y, car.x + direction * 40f * density, car.y + 6f * density, stroke)
            fill.color = Color.rgb(255, 245, 225)
            fill.alpha = (230 * strength).toInt()
            canvas.drawCircle(car.x, car.y, 2.2f * density, fill)
            if (sin(time * 9f + car.y) > 0.3f) {
                fill.color = Color.rgb(255, 50, 40)
                canvas.drawCircle(car.x - direction * 7f * density, car.y, 1.6f * density, fill)
            }
        }
    }

    private fun drawRain(canvas: Canvas, dt: Float, frame: LiveFrame, strength: Float) {
        stroke.color = Color.rgb(170, 190, 210)
        stroke.strokeWidth = 1f * density
        val slant = 0.18f + frame.tiltX * 0.2f
        val length = 22f * density
        for (i in rain.indices step 3) {
            val speed = rain[i + 2]
            rain[i + 1] += speed * 1400f * density * dt / 2.6f
            rain[i] -= slant * speed * 1400f * density * dt / 2.6f
            if (rain[i + 1] > height) {
                rain[i + 1] -= height + length
                rain[i] = random.nextFloat() * width * 1.3f
            }
            val x = rain[i]
            val y = rain[i + 1]
            stroke.alpha = (70 * speed * strength).toInt()
            canvas.drawLine(x, y, x - slant * length * speed, y + length * speed, stroke)
        }
    }

    /** Une rangée de tours et leurs fenêtres, au plan [depth] (1 : le plus proche). */
    private inner class Skyline(
        val depth: Float,
        base: Float,
        tall: Float,
        wide: Float,
        val color: Int,
        lit: Float,
        val windowAlpha: Int,
    ) {
        private val towers: FloatArray
        private val windows: FloatArray
        private val lights: FloatArray

        init {
            val towers = mutableListOf<Float>()
            val windows = mutableListOf<Float>()
            val lights = mutableListOf<Float>()
            var x = -width * 0.25f
            while (x < width * 1.25f) {
                val w = wide * (0.6f + random.nextFloat() * 0.8f)
                val top = base - tall * (0.35f + random.nextFloat() * 0.65f)
                towers += listOf(x, top, x + w, base)
                // Fenêtres allumées, ambrées ou bleutées.
                val step = 6f * density * (0.6f + depth * 0.6f)
                var wy = top + step
                while (wy < base - step) {
                    var wx = x + step * 0.6f
                    while (wx < x + w - step * 0.6f) {
                        if (random.nextFloat() < lit) windows += listOf(wx, wy, if (random.nextFloat() < 0.7f) 0f else 1f)
                        wx += step
                    }
                    wy += step * 1.4f
                }
                if (random.nextFloat() < 0.5f) lights += listOf(x + w / 2, top - 2 * density)
                x += w + wide * random.nextFloat() * 0.3f
            }
            this.towers = towers.toFloatArray()
            this.windows = windows.toFloatArray()
            this.lights = lights.toFloatArray()
        }

        fun draw(canvas: Canvas, strength: Float) {
            fill.shader = null
            fill.color = color
            for (i in towers.indices step 4) canvas.drawRect(towers[i], towers[i + 1], towers[i + 2], towers[i + 3], fill)
            val size = 2.2f * density * (0.6f + depth * 0.6f)
            for (i in windows.indices step 3) {
                fill.color = if (windows[i + 2] == 0f) AMBER else TEAL
                fill.alpha = (windowAlpha * strength).toInt()
                canvas.drawRect(windows[i], windows[i + 1], windows[i] + size, windows[i + 1] + size * 0.8f, fill)
            }
            // Feux d'obstacle, qui clignotent lentement.
            val on = sin(time * 2.2f + depth * 5f) > 0f
            if (on) {
                fill.color = Color.rgb(255, 40, 30)
                fill.alpha = (220 * strength).toInt()
                for (i in lights.indices step 2) canvas.drawCircle(lights[i], lights[i + 1], 1.8f * density, fill)
            }
        }
    }

    private class Flare(val x: Float, val y: Float, var next: Float) {
        var age = -1f
    }

    private class Car(val y: Float, val speed: Float, var x: Float)

    private companion object {
        val AMBER = Color.rgb(255, 170, 70)
        val TEAL = Color.rgb(80, 210, 230)
        val PINK = Color.rgb(255, 63, 164)

        /** Les traits dont sont faits les caractères de l'enseigne (x0, y0, x1, y1). */
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
        val BACKGROUND = Color.rgb(4, 5, 10)
    }
}
