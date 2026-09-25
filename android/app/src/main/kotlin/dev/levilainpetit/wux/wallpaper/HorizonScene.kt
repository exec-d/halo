package dev.levilainpetit.wux.wallpaper

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import dev.levilainpetit.wux.weather.Weather
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

/**
 * Horizon : un paysage néon qui suit la vraie journée et la vraie météo. Le
 * soleil (rayé, façon années 80) ou la lune et les étoiles selon l'heure,
 * deux crêtes de montagnes et un sol quadrillé en perspective ; nuages,
 * pluie, neige, orage ou brume selon le temps qu'il fait au lieu choisi.
 */
class HorizonScene(private val context: Context) : LiveScene {

    private enum class Sky { CLEAR, CLOUDS, FOG, RAIN, SNOW, STORM }

    private var width = 1
    private var height = 1
    private var density = 1f
    private var horizon = 0f

    private var sky = Sky.CLEAR
    private var sunrise = LocalTime.of(7, 30)
    private var sunset = LocalTime.of(19, 30)

    private val far = Path()
    private val near = Path()
    private var stars = FloatArray(0)
    private var drops = FloatArray(0)

    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val skyPaint = Paint()
    private var skyColor = 0
    private val lockShade = Paint()

    override fun resize(width: Int, height: Int, density: Float) {
        this.width = width
        this.height = height
        this.density = density
        horizon = height * 0.62f
        val random = Random(11)
        ridge(far, random, horizon - height * 0.06f, height * 0.05f)
        ridge(near, random, horizon - height * 0.02f, height * 0.08f)
        stars = FloatArray(90 * 3) { i ->
            when (i % 3) {
                0 -> random.nextFloat() * width
                1 -> random.nextFloat() * horizon * 0.9f
                else -> random.nextFloat()
            }
        }
        drops = FloatArray(140 * 2) { random.nextFloat() }
        lockShade.shader = LinearGradient(0f, 0f, 0f, height * 0.34f, Color.argb(170, 0, 0, 0), Color.TRANSPARENT, Shader.TileMode.CLAMP)
        skyColor = 0
    }

    override fun refresh() {
        val forecast = Weather.forecast(context)
        sky = when (forecast?.code) {
            null, 0 -> Sky.CLEAR
            1, 2, 3 -> Sky.CLOUDS
            45, 48 -> Sky.FOG
            51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82 -> Sky.RAIN
            71, 73, 75, 77, 85, 86 -> Sky.SNOW
            95, 96, 99 -> Sky.STORM
            else -> Sky.CLOUDS
        }
        val today = forecast?.days?.firstOrNull { it.date == LocalDate.now() }
        today?.sunrise?.let { sunrise = it }
        today?.sunset?.let { sunset = it }
    }

    override fun draw(canvas: Canvas, frame: LiveFrame) {
        val palette = frame.palette
        val strength = 0.35f + 0.65f * frame.intensity
        val now = LocalTime.now()
        val day = now.isAfter(sunrise) && now.isBefore(sunset)
        val shift = 24f * density

        // Ciel : un dégradé vers une lueur à l'horizon, plus vive le jour.
        val glowAlpha = ((if (day) 90 else 45) * strength).toInt()
        val wanted = Color.argb(glowAlpha, Color.red(palette.glow), Color.green(palette.glow), Color.blue(palette.glow))
        if (wanted != skyColor) {
            skyColor = wanted
            skyPaint.shader = LinearGradient(0f, 0f, 0f, horizon, CircuitPainter.BACKGROUND, wanted, Shader.TileMode.CLAMP)
        }
        canvas.drawColor(CircuitPainter.BACKGROUND)
        canvas.drawRect(0f, 0f, width.toFloat(), horizon, skyPaint)

        val skyX = -frame.tiltX * shift * 0.3f
        if (!day && sky != Sky.RAIN && sky != Sky.STORM) drawStars(canvas, frame, skyX, strength)
        if (day) drawSun(canvas, now, skyX, strength, palette) else drawMoon(canvas, now, skyX, strength, palette)
        if (sky == Sky.CLOUDS || sky == Sky.RAIN || sky == Sky.STORM || sky == Sky.SNOW) drawClouds(canvas, frame, skyX, strength, palette)

        // Montagnes, la plus proche bougeant le plus.
        drawRidge(canvas, far, -frame.tiltX * shift * 0.5f, palette.line, (110 * strength).toInt())
        drawRidge(canvas, near, -frame.tiltX * shift, palette.line, (190 * strength).toInt())

        drawGrid(canvas, frame, shift, strength, palette)
        when (sky) {
            Sky.RAIN, Sky.STORM -> drawRain(canvas, frame, strength, palette)
            Sky.SNOW -> drawSnow(canvas, frame, strength, palette)
            Sky.FOG -> drawFog(canvas, strength, palette)
            else -> Unit
        }
        if (sky == Sky.STORM) drawLightning(canvas, frame, palette)
        if (frame.locked) canvas.drawRect(0f, 0f, width.toFloat(), height * 0.34f, lockShade)
    }

    override fun nextFrameIn(frame: LiveFrame): Long? = when (sky) {
        Sky.RAIN, Sky.STORM, Sky.SNOW -> 40L
        // Nuages qui dérivent, étoiles qui scintillent : sans hâte.
        else -> 120L
    }

    // ——— Éléments ———

    private fun ridge(path: Path, random: Random, base: Float, amplitude: Float) {
        path.rewind()
        val margin = width * 0.2f
        path.moveTo(-margin, horizon)
        var x = -margin
        while (x <= width + margin) {
            path.lineTo(x, base - random.nextFloat() * amplitude)
            x += width / 9f * (0.6f + random.nextFloat() * 0.8f)
        }
        path.lineTo(width + margin, horizon)
        path.close()
    }

    private fun drawRidge(canvas: Canvas, path: Path, dx: Float, color: Int, alpha: Int) {
        canvas.save()
        canvas.translate(dx, 0f)
        fill.color = CircuitPainter.BACKGROUND
        fill.alpha = 255
        canvas.drawPath(path, fill)
        stroke.color = color
        stroke.alpha = alpha
        stroke.strokeWidth = 1.6f * density
        canvas.drawPath(path, stroke)
        canvas.restore()
    }

    private fun drawGrid(canvas: Canvas, frame: LiveFrame, shift: Float, strength: Float, palette: CircuitPalette) {
        stroke.color = palette.line
        stroke.strokeWidth = 1.2f * density
        val depth = height - horizon
        // Lignes horizontales, de plus en plus serrées vers l'horizon.
        for (k in 1..12) {
            val t = k / 12f
            val y = horizon + depth * t * t
            stroke.alpha = (40 + 150 * t * strength).toInt()
            canvas.drawLine(0f, y, width.toFloat(), y, stroke)
        }
        // Lignes de fuite vers un point qui suit l'inclinaison.
        val vanish = width / 2f - frame.tiltX * shift * 1.5f
        for (k in -10..10) {
            stroke.alpha = (110 * strength).toInt()
            val bottomX = width / 2f + k * width / 7f
            canvas.drawLine(vanish, horizon, bottomX, height.toFloat(), stroke)
        }
        stroke.alpha = (230 * strength).toInt()
        stroke.strokeWidth = 1.8f * density
        canvas.drawLine(0f, horizon, width.toFloat(), horizon, stroke)
    }

    private fun drawSun(canvas: Canvas, now: LocalTime, dx: Float, strength: Float, palette: CircuitPalette) {
        val span = Duration.between(sunrise, sunset).seconds.coerceAtLeast(1)
        val t = (Duration.between(sunrise, now).seconds / span.toFloat()).coerceIn(0f, 1f)
        val radius = width * 0.17f
        val cx = width * (0.15f + 0.7f * t) + dx
        val cy = horizon - sin(PI * t).toFloat() * height * 0.3f
        fill.shader = LinearGradient(0f, cy - radius, 0f, cy + radius, palette.core, palette.glow, Shader.TileMode.CLAMP)
        fill.alpha = (255 * strength).toInt()
        canvas.save()
        canvas.clipRect(0f, 0f, width.toFloat(), horizon)
        canvas.drawCircle(cx, cy, radius, fill)
        fill.shader = null
        // Les bandes du bas, de plus en plus larges.
        fill.color = CircuitPainter.BACKGROUND
        for (k in 0 until 6) {
            val y = cy + radius * (0.1f + k * 0.16f)
            val h = radius * (0.02f + k * 0.018f)
            canvas.drawRect(cx - radius, y, cx + radius, y + h, fill)
        }
        canvas.restore()
    }

    private fun drawMoon(canvas: Canvas, now: LocalTime, dx: Float, strength: Float, palette: CircuitPalette) {
        // La nuit va du coucher au lever ; la lune la traverse.
        val night = Duration.ofHours(24).minus(Duration.between(sunrise, sunset)).seconds.coerceAtLeast(1)
        val since = Duration.between(sunset, now).seconds.let { if (it < 0) it + 86_400 else it }
        val t = (since / night.toFloat()).coerceIn(0f, 1f)
        val radius = width * 0.07f
        val cx = width * (0.2f + 0.6f * t) + dx
        val cy = horizon - sin(PI * t).toFloat() * height * 0.32f - radius
        val age = moonAge()
        stroke.color = palette.core
        stroke.alpha = (230 * strength).toInt()
        stroke.strokeWidth = 1.6f * density
        canvas.drawCircle(cx, cy, radius, stroke)
        // La partie éclairée : le disque, moins un disque d'ombre qui s'écarte
        // à mesure que la lune se remplit (par la droite, puis se vide par la droite).
        val litFraction = ((1 - cos(2 * PI * age)) / 2).toFloat()
        if (litFraction < 0.03f) return
        fill.color = palette.core
        fill.alpha = (200 * strength).toInt()
        if (litFraction > 0.97f) {
            canvas.drawCircle(cx, cy, radius, fill)
            return
        }
        val offset = radius * 2f * litFraction
        val lit = Path().apply { addCircle(cx, cy, radius, Path.Direction.CW) }
        val shade = Path().apply { addCircle(if (age < 0.5) cx - offset else cx + offset, cy, radius, Path.Direction.CW) }
        lit.op(shade, Path.Op.DIFFERENCE)
        canvas.drawPath(lit, fill)
    }

    private fun drawStars(canvas: Canvas, frame: LiveFrame, dx: Float, strength: Float) {
        fill.color = frame.palette.core
        for (i in stars.indices step 3) {
            val twinkle = 0.55f + 0.45f * sin(frame.timeMillis / 900.0 + stars[i + 2] * 20).toFloat()
            fill.alpha = (200 * twinkle * strength * (0.4f + 0.6f * stars[i + 2])).toInt()
            canvas.drawCircle(stars[i] + dx, stars[i + 1], (0.6f + stars[i + 2]) * density, fill)
        }
    }

    private fun drawClouds(canvas: Canvas, frame: LiveFrame, dx: Float, strength: Float, palette: CircuitPalette) {
        val heavy = sky != Sky.CLOUDS
        val count = if (heavy) 5 else 3
        stroke.color = palette.line
        stroke.strokeWidth = 1.5f * density
        fill.color = CircuitPainter.BACKGROUND
        for (i in 0 until count) {
            // Chaque nuage dérive à sa vitesse, et revient par l'autre bord.
            val speed = (8f + i * 3f) * density
            val span = width * 1.6f
            val x = ((i * 0.37f * span + frame.timeMillis / 1000f * speed) % span) - width * 0.3f + dx
            val y = horizon * (0.18f + 0.13f * i)
            val w = width * (0.28f + 0.06f * (i % 2))
            val cloud = Path().apply {
                addRoundRect(RectF(x, y, x + w, y + w * 0.22f), w * 0.11f, w * 0.11f, Path.Direction.CW)
                addCircle(x + w * 0.35f, y, w * 0.16f, Path.Direction.CW)
                addCircle(x + w * 0.62f, y + w * 0.02f, w * 0.12f, Path.Direction.CW)
            }
            cloud.op(Path(), Path.Op.UNION)
            fill.alpha = 230
            canvas.drawPath(cloud, fill)
            stroke.alpha = ((if (heavy) 190 else 140) * strength).toInt()
            canvas.drawPath(cloud, stroke)
        }
    }

    private fun drawRain(canvas: Canvas, frame: LiveFrame, strength: Float, palette: CircuitPalette) {
        stroke.color = palette.line
        stroke.strokeWidth = 1.2f * density
        val length = 14f * density
        val t = frame.timeMillis / 1000f
        val count = if (sky == Sky.STORM) drops.size / 2 else drops.size / 3
        for (i in 0 until count) {
            val x = drops[i * 2] * (width + length) - frame.tiltX * 20f * density
            val speed = 0.9f + drops[i * 2 + 1] * 0.5f
            val y = ((drops[i * 2 + 1] + t * speed) % 1f) * height
            stroke.alpha = ((80 + 120 * drops[i * 2 + 1]) * strength).toInt()
            canvas.drawLine(x, y, x - length * 0.25f, y + length, stroke)
        }
    }

    private fun drawSnow(canvas: Canvas, frame: LiveFrame, strength: Float, palette: CircuitPalette) {
        fill.color = palette.core
        val t = frame.timeMillis / 1000f
        for (i in 0 until drops.size / 3) {
            val speed = 0.08f + drops[i * 2 + 1] * 0.08f
            val y = ((drops[i * 2 + 1] + t * speed) % 1f) * height
            val sway = sin(t * 1.3 + i).toFloat() * 10f * density
            val x = drops[i * 2] * width + sway
            fill.alpha = ((120 + 110 * drops[i * 2]) * strength).toInt()
            canvas.drawCircle(x, y, (1.2f + drops[i * 2] * 1.5f) * density, fill)
        }
    }

    private fun drawFog(canvas: Canvas, strength: Float, palette: CircuitPalette) {
        fill.color = palette.glow
        for (k in 0 until 4) {
            fill.alpha = ((30 - k * 5) * strength).toInt().coerceAtLeast(0)
            val y = horizon - height * 0.08f + k * height * 0.05f
            canvas.drawRect(0f, y, width.toFloat(), y + height * 0.04f, fill)
        }
    }

    /** Un éclair toutes les huit secondes environ, bref. */
    private fun drawLightning(canvas: Canvas, frame: LiveFrame, palette: CircuitPalette) {
        val cycle = frame.timeMillis % 8_000L
        if (cycle > 180L) return
        val flash = 1f - cycle / 180f
        fill.color = palette.core
        fill.alpha = (40 * flash).toInt()
        canvas.drawRect(0f, 0f, width.toFloat(), horizon, fill)
        val random = Random(frame.timeMillis / 8_000L)
        var x = width * (0.2f + random.nextFloat() * 0.6f)
        var y = horizon * 0.2f
        val bolt = Path().apply { moveTo(x, y) }
        while (y < horizon) {
            x += (random.nextFloat() - 0.5f) * 40f * density
            y += 25f * density
            bolt.lineTo(x, min(y, horizon))
        }
        stroke.color = palette.core
        stroke.alpha = (255 * flash).toInt()
        stroke.strokeWidth = 2f * density
        canvas.drawPath(bolt, stroke)
    }

    /** Âge de la lune, de 0 (nouvelle) à 1, 0,5 étant la pleine lune. */
    private fun moonAge(): Double {
        val days = Duration.between(Instant.parse("2000-01-06T18:14:00Z"), Instant.now()).toMinutes() / 1440.0
        val synodic = 29.530588853
        return (((days % synodic) + synodic) % synodic) / synodic
    }
}
