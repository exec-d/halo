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
 * Mégapole : une ville immense sous la pluie, la nuit, dans un smog teinté
 * des couleurs du téléphone. Nuages bas qui dérivent, projecteurs qui
 * balaient le ciel, une pyramide au loin, quatre plans de tours (les plus
 * proches bougent le plus quand on penche le téléphone), des torchères qui
 * crachent leur flamme, des voitures volantes, deux enseignes lumineuses,
 * du brouillard et la pluie.
 */
class MegacityScene : LiveScene {

    override val colors: IntArray? get() = own

    private var own: IntArray? = null
    private var width = 1
    private var height = 1
    private var density = 1f
    private var last = 0L
    private var time = 0f
    private val random = Random(19)
    private var ground = 0f

    private var layers = emptyList<Skyline>()
    private val pyramid = Path()
    private var pyramidTop = 0f
    private var pyramidBase = 0f
    private var clouds = FloatArray(0)
    private var flares = emptyList<Flare>()
    private var cars = emptyList<Car>()
    private var rain = FloatArray(0)
    private var billboard = FloatArray(4)
    private var holo = FloatArray(4)
    private var glyphs = emptyList<FloatArray>()
    private val sign = RectF()

    private val fill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val sky = Paint()
    private val haze = Paint()
    private val fog = Paint()
    private val beam = Path()
    private val beamPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var lockShade = Paint()
    private var shaded = 0

    override fun resize(width: Int, height: Int, density: Float) {
        this.width = width
        this.height = height
        this.density = density
        ground = height * 0.8f
        layers = listOf(
            Skyline(depth = 0.2f, base = ground - height * 0.1f, tall = height * 0.26f, wide = width * 0.045f, shade = 0.16f, lit = 0.1f, windowAlpha = 70),
            Skyline(depth = 0.4f, base = ground - height * 0.03f, tall = height * 0.36f, wide = width * 0.07f, shade = 0.1f, lit = 0.15f, windowAlpha = 120),
            Skyline(depth = 0.65f, base = ground + height * 0.05f, tall = height * 0.44f, wide = width * 0.1f, shade = 0.06f, lit = 0.17f, windowAlpha = 170),
            Skyline(depth = 1f, base = height * 1.05f, tall = height * 0.5f, wide = width * 0.18f, shade = 0.025f, lit = 0.07f, windowAlpha = 230),
        )
        // La pyramide, au loin.
        val cx = width * 0.64f
        pyramidBase = ground - height * 0.09f
        val half = width * 0.42f
        pyramidTop = pyramidBase - height * 0.38f
        pyramid.rewind()
        pyramid.moveTo(cx - half, pyramidBase)
        pyramid.lineTo(cx - half * 0.1f, pyramidTop)
        pyramid.lineTo(cx + half * 0.1f, pyramidTop)
        pyramid.lineTo(cx + half, pyramidBase)
        pyramid.close()
        clouds = FloatArray(5 * 4) { i ->
            when (i % 4) {
                0 -> random.nextFloat() * width
                1 -> height * (0.08f + random.nextFloat() * 0.3f)
                2 -> width * (0.5f + random.nextFloat() * 0.6f)
                else -> 0.4f + random.nextFloat() * 0.6f
            }
        }
        // Une enseigne verticale fixée à sa tour, et un panneau holographique.
        billboard = floatArrayOf(width * 0.12f, height * 0.33f, width * 0.2f, height * 0.53f)
        holo = floatArrayOf(width * 0.7f, height * 0.5f, width * 0.93f, height * 0.585f)
        glyphs = List(5) {
            val strokes = STROKES.shuffled(random).take(2 + random.nextInt(2))
            FloatArray(strokes.size * 4) { i -> strokes[i / 4][i % 4] }
        }
        flares = List(3) { i ->
            Flare(x = width * (0.18f + 0.32f * i) + random.nextFloat() * width * 0.08f, y = ground - height * (0.2f + random.nextFloat() * 0.06f), next = 0.5f + i * 2.4f)
        }
        cars = List(6) { i ->
            Car(
                y = height * (0.16f + 0.08f * i),
                speed = (30f + random.nextFloat() * 70f) * density * (if (i % 2 == 0) 1f else -1f),
                x = random.nextFloat() * width,
                size = 0.7f + (i / 6f) * 0.8f,
            )
        }
        rain = FloatArray(220 * 3) { i ->
            when (i % 3) {
                0 -> random.nextFloat() * width * 1.3f
                1 -> random.nextFloat() * height
                else -> 0.4f + random.nextFloat() * 0.6f
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
        val shift = 34f * density
        val accent = SceneKit.neon(frame.palette)
        val second = SceneKit.second(frame.palette)
        val third = SceneKit.third(frame.palette)
        val core = frame.palette.core
        val night = SceneKit.night(frame.palette, 0.035f)
        val smog = SceneKit.mix(SceneKit.night(frame.palette, 0.16f), frame.palette.glow, 0.22f)
        own = intArrayOf(accent, second, night)
        prepare(night, smog)

        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), sky)
        drawClouds(canvas, dt, frame, strength, smog)
        drawBeams(canvas, strength, SceneKit.mix(second, core, 0.3f))

        // La pyramide, au plus loin.
        canvas.save()
        canvas.translate(-frame.tiltX * shift * 0.12f, -frame.tiltY * shift * 0.08f)
        drawPyramid(canvas, strength, accent, night)
        canvas.restore()

        for ((i, layer) in layers.withIndex()) {
            canvas.save()
            canvas.translate(-frame.tiltX * shift * layer.depth, -frame.tiltY * shift * 0.4f * layer.depth)
            layer.draw(canvas, strength, night, smog, accent, second, core)
            if (i == 0) drawFlares(canvas, dt, strength, accent, core)
            if (i == 2) {
                drawBillboard(canvas, strength, accent, third, night)
                drawHolo(canvas, strength, second, core)
                drawCars(canvas, dt, strength, accent, core)
            }
            canvas.restore()
        }
        canvas.drawRect(0f, height * 0.45f, width.toFloat(), height.toFloat(), haze)
        drawFog(canvas, frame, strength)
        drawRain(canvas, dt, frame, strength, core)
        if (frame.locked) canvas.drawRect(0f, 0f, width.toFloat(), height * 0.34f, lockShade)
    }

    override fun nextFrameIn(frame: LiveFrame): Long = 40L

    private fun prepare(night: Int, smog: Int) {
        if (shaded == smog) return
        shaded = smog
        sky.shader = LinearGradient(
            0f, 0f, 0f, ground,
            intArrayOf(night, SceneKit.mix(night, smog, 0.45f), smog),
            floatArrayOf(0f, 0.55f, 1f), Shader.TileMode.CLAMP,
        )
        haze.shader = LinearGradient(
            0f, height * 0.45f, 0f, height.toFloat(),
            SceneKit.alpha(smog, 0), SceneKit.alpha(smog, 60), Shader.TileMode.CLAMP,
        )
        fog.shader = LinearGradient(
            0f, height * 0.72f, 0f, height.toFloat(),
            SceneKit.alpha(smog, 0), SceneKit.alpha(SceneKit.mix(smog, Color.WHITE, 0.1f), 80), Shader.TileMode.CLAMP,
        )
    }

    /** Des nuages bas, éclairés par-dessous par la ville. */
    private fun drawClouds(canvas: Canvas, dt: Float, frame: LiveFrame, strength: Float, smog: Int) {
        fill.shader = null
        for (i in clouds.indices step 4) {
            clouds[i] += dt * 4f * density * clouds[i + 3]
            if (clouds[i] - clouds[i + 2] > width) clouds[i] = -clouds[i + 2]
            val x = clouds[i] - frame.tiltX * 6f * density
            val y = clouds[i + 1]
            val w = clouds[i + 2]
            fill.color = SceneKit.mix(smog, Color.WHITE, 0.08f)
            fill.alpha = (22 * clouds[i + 3] * strength).toInt()
            canvas.drawOval(RectF(x - w / 2, y - w * 0.07f, x + w / 2, y + w * 0.07f), fill)
            fill.alpha = (16 * clouds[i + 3] * strength).toInt()
            canvas.drawOval(RectF(x - w * 0.3f, y - w * 0.11f, x + w * 0.35f, y + w * 0.04f), fill)
        }
    }

    private fun drawBeams(canvas: Canvas, strength: Float, color: Int) {
        for (i in 0..1) {
            val originX = width * (0.3f + 0.42f * i)
            val originY = height * 0.82f
            val angle = (-PI / 2 + sin(time * 0.16f + i * 2.1f) * 0.5f).toFloat()
            val reach = height * 0.95f
            val spread = 0.045f
            beam.rewind()
            beam.moveTo(originX, originY)
            beam.lineTo(originX + cos(angle - spread) * reach, originY + sin(angle - spread) * reach)
            beam.lineTo(originX + cos(angle + spread) * reach, originY + sin(angle + spread) * reach)
            beam.close()
            beamPaint.shader = LinearGradient(
                originX, originY, originX + cos(angle) * reach, originY + sin(angle) * reach,
                SceneKit.alpha(color, (38 * strength).toInt()), SceneKit.alpha(color, 0), Shader.TileMode.CLAMP,
            )
            canvas.drawPath(beam, beamPaint)
        }
    }

    /** La pyramide : sa masse, ses étages éclairés, son sommet qui brille. */
    private fun drawPyramid(canvas: Canvas, strength: Float, accent: Int, night: Int) {
        fill.shader = null
        fill.color = SceneKit.mix(night, Color.BLACK, 0.2f)
        canvas.drawPath(pyramid, fill)
        canvas.save()
        canvas.clipPath(pyramid)
        val floors = 16
        for (k in 1 until floors) {
            val y = pyramidTop + (pyramidBase - pyramidTop) * k / floors
            fill.color = accent
            fill.alpha = ((22 + 26 * (0.5f + 0.5f * sin(time * 0.6f + k * 0.9f))) * strength).toInt()
            canvas.drawRect(0f, y, width.toFloat(), y + 1.2f * density, fill)
            // Des fenêtres allumées sur l'étage.
            var x = (k * 37 % 23) * density
            while (x < width) {
                if ((x.toInt() / 7 + k * 3) % 5 == 0) {
                    fill.alpha = (110 * strength).toInt()
                    canvas.drawRect(x, y + 2 * density, x + 2.4f * density, y + 3.6f * density, fill)
                }
                x += 9f * density
            }
        }
        canvas.restore()
        stroke.color = accent
        stroke.strokeWidth = 1f * density
        stroke.alpha = (90 * strength).toInt()
        canvas.drawPath(pyramid, stroke)
        val cx = width * 0.64f
        fill.shader = RadialGradient(
            cx, pyramidTop, 40f * density,
            SceneKit.alpha(accent, (160 * strength).toInt()), Color.TRANSPARENT, Shader.TileMode.CLAMP,
        )
        canvas.drawCircle(cx, pyramidTop, 40f * density, fill)
        fill.shader = null
    }

    private fun drawFlares(canvas: Canvas, dt: Float, strength: Float, accent: Int, core: Int) {
        for (flare in flares) {
            // La cheminée et son brûleur.
            fill.shader = null
            fill.color = Color.rgb(12, 10, 14)
            canvas.drawRect(flare.x - 3 * density, flare.y, flare.x + 3 * density, ground, fill)
            fill.color = accent
            fill.alpha = (170 * strength).toInt()
            canvas.drawCircle(flare.x, flare.y, 2f * density, fill)
            flare.next -= dt
            if (flare.next <= 0f) {
                flare.age = 0f
                flare.next = 4f + random.nextFloat() * 6f
            }
            if (flare.age < 0f) continue
            flare.age += dt
            val life = flare.age / 1.6f
            if (life >= 1f) {
                flare.age = -1f
                continue
            }
            val radius = density * (12f + 46f * life)
            val y = flare.y - density * 36f * life
            // Le ciel s'éclaire autour de la flamme.
            fill.shader = RadialGradient(
                flare.x, y, radius * 3.5f,
                SceneKit.alpha(accent, (70 * (1 - life) * strength).toInt()), Color.TRANSPARENT, Shader.TileMode.CLAMP,
            )
            canvas.drawCircle(flare.x, y, radius * 3.5f, fill)
            fill.shader = RadialGradient(
                flare.x, y, radius,
                intArrayOf(SceneKit.alpha(core, (240 * (1 - life) * strength).toInt()), SceneKit.alpha(accent, (170 * (1 - life) * strength).toInt()), Color.TRANSPARENT),
                floatArrayOf(0f, 0.45f, 1f), Shader.TileMode.CLAMP,
            )
            canvas.drawCircle(flare.x, y, radius, fill)
            fill.shader = null
        }
    }

    private fun drawBillboard(canvas: Canvas, strength: Float, a: Int, b: Int, night: Int) {
        val (left, top, right, bottom) = billboard.toList()
        val mix = (0.5f + 0.5f * sin(time * 0.6f)).coerceIn(0f, 1f)
        val color = SceneKit.mix(a, b, mix)
        val flicker = if (sin(time * 23f) > 0.97f || sin(time * 7.3f) > 0.995f) 0.3f else 1f
        val glow = strength * flicker
        val radius = 3f * density

        // La tour qui porte l'enseigne, avec quelques fenêtres.
        fill.shader = null
        val towerRight = left - 10 * density
        val towerLeft = towerRight - width * 0.11f
        val towerTop = top - height * 0.035f
        fill.color = SceneKit.mix(night, Color.BLACK, 0.3f)
        canvas.drawRect(towerLeft, towerTop, towerRight, height.toFloat(), fill)
        stroke.color = a
        stroke.strokeWidth = 1f * density
        stroke.alpha = (90 * strength).toInt()
        canvas.drawLine(towerLeft, towerTop, towerRight, towerTop, stroke)
        val window = 2.2f * density
        var wy = towerTop + 8 * density
        var row = 0
        while (wy < height) {
            var wx = towerLeft + 5 * density
            var column = 0
            while (wx < towerRight - 5 * density) {
                if ((row * 7 + column * 13) % 11 == 0) {
                    fill.color = if ((row + column) % 3 == 0) b else a
                    fill.alpha = (150 * strength).toInt()
                    canvas.drawRect(wx, wy, wx + window, wy + window * 0.8f, fill)
                }
                wx += 7 * density
                column++
            }
            wy += 10 * density
            row++
        }

        // Les attaches, le halo, le caisson.
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

    /** Un panneau holographique : des bandes qui défilent, un peu transparentes. */
    private fun drawHolo(canvas: Canvas, strength: Float, color: Int, core: Int) {
        val (left, top, right, bottom) = holo.toList()
        fill.shader = null
        fill.color = Color.rgb(8, 7, 12)
        canvas.drawRect(right - width * 0.02f, top - height * 0.06f, right + width * 0.1f, height.toFloat(), fill)
        sign.set(left, top, right, bottom)
        stroke.color = color
        for ((size, alpha) in listOf(12f to 20, 5f to 45)) {
            stroke.strokeWidth = size * density
            stroke.alpha = (alpha * strength).toInt()
            canvas.drawRect(sign, stroke)
        }
        fill.color = color
        fill.alpha = (46 * strength).toInt()
        canvas.drawRect(sign, fill)
        canvas.save()
        canvas.clipRect(sign)
        val bar = (bottom - top) / 5
        val offset = (time * 18f * density) % (right - left)
        for (k in 0 until 4) {
            val y = top + bar * (k + 0.7f)
            val length = (right - left) * (0.3f + 0.15f * ((k * 7) % 4))
            for (copy in 0..1) {
                val x = left + ((k * 41 * density + offset + copy * (right - left)) % ((right - left) * 2)) - (right - left) * 0.5f
                fill.color = if (k == 0) core else color
                fill.alpha = ((if (k == 0) 220 else 160) * strength).toInt()
                canvas.drawRect(x, y, x + length, y + bar * 0.45f, fill)
            }
        }
        canvas.restore()
        stroke.strokeWidth = 1.4f * density
        stroke.alpha = (220 * strength).toInt()
        canvas.drawRect(sign, stroke)
    }

    private fun drawCars(canvas: Canvas, dt: Float, strength: Float, accent: Int, core: Int) {
        for (car in cars) {
            car.x += car.speed * dt
            if (car.x > width * 1.2f) car.x = -width * 0.2f
            if (car.x < -width * 0.2f) car.x = width * 1.2f
            val direction = if (car.speed > 0) 1f else -1f
            val s = car.size * density
            // La traînée, le faisceau, la carrosserie et les feux.
            stroke.color = accent
            stroke.alpha = (50 * strength).toInt()
            stroke.strokeWidth = 2.5f * s
            canvas.drawLine(car.x - direction * 34f * s, car.y + 1.5f * s, car.x, car.y, stroke)
            stroke.color = core
            stroke.alpha = (40 * strength).toInt()
            stroke.strokeWidth = 7f * s
            canvas.drawLine(car.x, car.y, car.x + direction * 46f * s, car.y + 8f * s, stroke)
            fill.shader = null
            fill.color = Color.rgb(20, 18, 24)
            canvas.drawRoundRect(car.x - 8f * s, car.y - 2.5f * s, car.x + 8f * s, car.y + 2.5f * s, 2f * s, 2f * s, fill)
            fill.color = core
            fill.alpha = (240 * strength).toInt()
            canvas.drawCircle(car.x + direction * 7f * s, car.y, 2f * s, fill)
            if (sin(time * 9f + car.y) > 0.3f) {
                fill.color = Color.rgb(255, 50, 40)
                canvas.drawCircle(car.x - direction * 7f * s, car.y, 1.5f * s, fill)
            }
        }
    }

    private fun drawFog(canvas: Canvas, frame: LiveFrame, strength: Float) {
        fog.alpha = (255 * strength).toInt()
        canvas.save()
        canvas.translate(sin(time * 0.1f) * 20f * density - frame.tiltX * 10f * density, 0f)
        canvas.drawRect(-width * 0.2f, height * 0.72f, width * 1.2f, height.toFloat(), fog)
        canvas.restore()
    }

    private fun drawRain(canvas: Canvas, dt: Float, frame: LiveFrame, strength: Float, core: Int) {
        stroke.color = SceneKit.mix(core, Color.rgb(160, 180, 200), 0.5f)
        stroke.strokeWidth = 1f * density
        val slant = 0.18f + frame.tiltX * 0.2f
        val length = 22f * density
        for (i in rain.indices step 3) {
            val speed = rain[i + 2]
            rain[i + 1] += speed * 540f * density * dt
            rain[i] -= slant * speed * 540f * density * dt
            if (rain[i + 1] > height) {
                rain[i + 1] -= height + length
                rain[i] = random.nextFloat() * width * 1.3f
            }
            val x = rain[i]
            val y = rain[i + 1]
            stroke.alpha = (75 * speed * strength).toInt()
            canvas.drawLine(x, y, x - slant * length * speed, y + length * speed, stroke)
        }
    }

    /** Une rangée de tours, au plan [depth] (1 : le plus proche). */
    private inner class Skyline(
        val depth: Float,
        base: Float,
        tall: Float,
        wide: Float,
        /** Clarté des façades, de 0 (noir) à 1. */
        val shade: Float,
        lit: Float,
        val windowAlpha: Int,
    ) {
        private val towers: FloatArray
        private val kinds: IntArray
        private val windows: FloatArray
        private val lights: FloatArray

        init {
            val towers = mutableListOf<Float>()
            val kinds = mutableListOf<Int>()
            val windows = mutableListOf<Float>()
            val lights = mutableListOf<Float>()
            var x = -width * 0.25f
            while (x < width * 1.25f) {
                val w = wide * (0.6f + random.nextFloat() * 0.8f)
                val top = base - tall * (0.35f + random.nextFloat() * 0.65f)
                towers += listOf(x, top, x + w, base)
                // 0 : toit plat ; 1 : gradins ; 2 : flèche.
                val kind = random.nextInt(3)
                kinds += kind
                val step = 6f * density * (0.6f + depth * 0.6f)
                var wy = top + step
                while (wy < base - step) {
                    var wx = x + step * 0.6f
                    while (wx < x + w - step * 0.6f) {
                        if (random.nextFloat() < lit) windows += listOf(wx, wy, random.nextInt(3).toFloat())
                        wx += step
                    }
                    wy += step * 1.4f
                }
                val spire = if (kind == 2) tall * 0.12f else if (kind == 1) tall * 0.06f else 0f
                if (random.nextFloat() < 0.6f) lights += listOf(x + w / 2, top - spire - 2 * density)
                x += w + wide * random.nextFloat() * 0.3f * (1f - depth * 0.8f)
            }
            this.towers = towers.toFloatArray()
            this.kinds = kinds.toIntArray()
            this.windows = windows.toFloatArray()
            this.lights = lights.toFloatArray()
        }

        fun draw(canvas: Canvas, strength: Float, night: Int, smog: Int, accent: Int, second: Int, core: Int) {
            fill.shader = null
            // Les plans lointains se fondent dans le smog.
            val body = SceneKit.mix(SceneKit.mix(night, Color.BLACK, 0.4f), smog, shade * 1.6f)
            for (i in towers.indices step 4) {
                val left = towers[i]
                val top = towers[i + 1]
                val right = towers[i + 2]
                val bottom = towers[i + 3]
                val w = right - left
                fill.color = body
                canvas.drawRect(left, top, right, bottom, fill)
                when (kinds[i / 4]) {
                    1 -> canvas.drawRect(left + w * 0.2f, top - w * 0.3f, right - w * 0.2f, top, fill)
                    2 -> {
                        canvas.drawRect(left + w * 0.35f, top - w * 0.2f, right - w * 0.35f, top, fill)
                        stroke.color = body
                        stroke.strokeWidth = 1.5f * density
                        stroke.alpha = 255
                        canvas.drawLine(left + w / 2, top - w * 0.2f, left + w / 2, top - w * 0.2f - (bottom - top) * 0.12f, stroke)
                    }
                }
                // Un liseré de lumière sur le bord du toit.
                stroke.color = accent
                stroke.strokeWidth = 1f * density
                stroke.alpha = (40 * depth * strength).toInt()
                canvas.drawLine(left, top, right, top, stroke)
            }
            val size = 2.2f * density * (0.6f + depth * 0.6f)
            for (i in windows.indices step 3) {
                fill.color = when (windows[i + 2].toInt()) {
                    0 -> accent
                    1 -> second
                    else -> core
                }
                fill.alpha = (windowAlpha * strength).toInt()
                canvas.drawRect(windows[i], windows[i + 1], windows[i] + size, windows[i + 1] + size * 0.8f, fill)
            }
            // Feux d'obstacle, qui clignotent lentement.
            if (sin(time * 2.2f + depth * 5f) > 0f) {
                fill.color = Color.rgb(255, 40, 30)
                fill.alpha = (220 * strength).toInt()
                for (i in lights.indices step 2) canvas.drawCircle(lights[i], lights[i + 1], 1.8f * density, fill)
            }
        }
    }

    private class Flare(val x: Float, val y: Float, var next: Float) {
        var age = -1f
    }

    private class Car(val y: Float, val speed: Float, var x: Float, val size: Float)

    private companion object {
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
    }
}
