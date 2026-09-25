package dev.levilainpetit.wux.wallpaper

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import dev.levilainpetit.wux.weather.Weather
import java.time.Duration
import java.time.Instant
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

/**
 * Ciel : les vraies étoiles et la vraie Lune au-dessus du lieu choisi, dans
 * la direction où l'on tient le téléphone (boussole). Lever le téléphone
 * monte vers le zénith ; le tenir comme d'habitude montre le ciel au-dessus
 * de l'horizon. Le jour, le ciel s'éclaircit et les étoiles pâlissent.
 */
class SkyScene(private val context: Context) : LiveScene {

    override val usesOrientation = true

    private var width = 1
    private var height = 1
    private var density = 1f
    private var latitude = 48.85
    private var longitude = 2.35

    /** Les tracés, en indices dans `Stars.ALL`. */
    private val lines = Stars.ALL.withIndex().associate { it.value.name to it.index }.let { index ->
        Stars.LINES.mapNotNull { (a, b) -> index[a]?.let { i -> index[b]?.let { j -> i to j } } }
    }
    private val points = FloatArray(Stars.ALL.size * 2)
    private val visible = BooleanArray(Stars.ALL.size)

    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val lockShade = Paint()

    override fun resize(width: Int, height: Int, density: Float) {
        this.width = width
        this.height = height
        this.density = density
        lockShade.shader = LinearGradient(0f, 0f, 0f, height * 0.34f, Color.argb(170, 0, 0, 0), Color.TRANSPARENT, Shader.TileMode.CLAMP)
    }

    override fun refresh() {
        Weather.place(context)?.let {
            latitude = it.latitude
            longitude = it.longitude
        }
    }

    override fun draw(canvas: Canvas, frame: LiveFrame) {
        val palette = frame.palette
        val strength = 0.35f + 0.65f * frame.intensity
        val d = Stars.days(frame.timeMillis)

        // Sans boussole, ou le téléphone tenu vers le sol : on regarde quand
        // même le ciel, vers le sud, un peu au-dessus de l'horizon.
        val azimuth = if (frame.hasOrientation) frame.azimuth.toDouble() else 180.0 + frame.tiltX * 25
        val altitude = if (frame.hasOrientation) (28.0 + (frame.altitude + 30) * 0.6).coerceIn(12.0, 80.0) else 30.0 - frame.tiltY * 12
        val view = View(azimuth, altitude, frame.roll.toDouble(), width, height)

        // Le jour, selon la hauteur du Soleil.
        val (sunRa, sunDec) = Stars.sun(d)
        val sunAltitude = Stars.horizontal(sunRa, sunDec, latitude, longitude, d).first
        val daylight = ((sunAltitude + 12) / 18).coerceIn(0.0, 1.0).toFloat()
        canvas.drawColor(CircuitPainter.BACKGROUND)
        fill.color = palette.glow
        fill.alpha = (70 * daylight * strength).toInt()
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), fill)

        // Positions à l'écran.
        Stars.ALL.forEachIndexed { i, star ->
            val (alt, az) = Stars.horizontal(star.ra, star.dec, latitude, longitude, d)
            val p = if (alt > -1) view.project(az, alt) else null
            visible[i] = p != null
            if (p != null) {
                points[i * 2] = p.first
                points[i * 2 + 1] = p.second
            }
        }
        val starStrength = strength * (1f - 0.8f * daylight)

        // Constellations.
        stroke.color = palette.line
        stroke.strokeWidth = 1f * density
        stroke.alpha = (80 * starStrength).toInt()
        for ((i, j) in lines) {
            if (visible[i] && visible[j]) canvas.drawLine(points[i * 2], points[i * 2 + 1], points[j * 2], points[j * 2 + 1], stroke)
        }

        // Étoiles, taille et éclat selon la magnitude, un léger scintillement.
        Stars.ALL.forEachIndexed { i, star ->
            if (!visible[i]) return@forEachIndexed
            val size = (2.8 - 0.55 * star.magnitude).coerceIn(0.7, 3.4).toFloat() * density
            val twinkle = 0.8f + 0.2f * sin(frame.timeMillis / 700.0 + i * 1.7).toFloat()
            fill.color = palette.glow
            fill.alpha = (70 * twinkle * starStrength).toInt()
            canvas.drawCircle(points[i * 2], points[i * 2 + 1], size * 2.6f, fill)
            fill.color = palette.core
            fill.alpha = (255 * twinkle * starStrength).toInt()
            canvas.drawCircle(points[i * 2], points[i * 2 + 1], size, fill)
        }

        drawMoon(canvas, view, d, palette, strength)
        drawHorizon(canvas, view, palette, strength)
        if (frame.locked) canvas.drawRect(0f, 0f, width.toFloat(), height * 0.34f, lockShade)
    }

    override fun nextFrameIn(frame: LiveFrame): Long = 150L

    private fun drawMoon(canvas: Canvas, view: View, d: Double, palette: CircuitPalette, strength: Float) {
        val (ra, dec) = Stars.moon(d)
        val (alt, az) = Stars.horizontal(ra, dec, latitude, longitude, d)
        if (alt < -1) return
        val (x, y) = view.project(az, alt) ?: return
        val radius = width * 0.045f
        val age = moonAge()
        stroke.color = palette.core
        stroke.alpha = (230 * strength).toInt()
        stroke.strokeWidth = 1.4f * density
        canvas.drawCircle(x, y, radius, stroke)
        val litFraction = ((1 - cos(2 * PI * age)) / 2).toFloat()
        if (litFraction < 0.03f) return
        fill.color = palette.core
        fill.alpha = (210 * strength).toInt()
        if (litFraction > 0.97f) {
            canvas.drawCircle(x, y, radius, fill)
            return
        }
        val offset = radius * 2f * litFraction
        val lit = Path().apply { addCircle(x, y, radius, Path.Direction.CW) }
        lit.op(Path().apply { addCircle(if (age < 0.5) x - offset else x + offset, y, radius, Path.Direction.CW) }, Path.Op.DIFFERENCE)
        canvas.drawPath(lit, fill)
    }

    /** Le sol, sous l'horizon réel, et un repère aux quatre points cardinaux. */
    private fun drawHorizon(canvas: Canvas, view: View, palette: CircuitPalette, strength: Float) {
        val line = Path()
        var started = false
        var az = view.azimuth - 90
        while (az <= view.azimuth + 90) {
            val p = view.project(az, 0.0)
            if (p != null) {
                if (!started) line.moveTo(p.first, p.second) else line.lineTo(p.first, p.second)
                started = true
            }
            az += 3
        }
        if (!started) return
        val ground = Path(line).apply {
            lineTo(width * 2f, height * 2f)
            lineTo(-width.toFloat(), height * 2f)
            close()
        }
        fill.color = CircuitPainter.BACKGROUND
        fill.alpha = 245
        canvas.drawPath(ground, fill)
        stroke.color = palette.line
        stroke.alpha = (220 * strength).toInt()
        stroke.strokeWidth = 1.6f * density
        canvas.drawPath(line, stroke)
        for (cardinal in 0 until 360 step 90) {
            val p = view.project(cardinal.toDouble(), 0.0) ?: continue
            val length = (if (cardinal == 0) 14f else 8f) * density
            canvas.drawLine(p.first, p.second, p.first, p.second + length, stroke)
        }
    }

    private fun moonAge(): Double {
        val days = Duration.between(Instant.parse("2000-01-06T18:14:00Z"), Instant.now()).toMinutes() / 1440.0
        val synodic = 29.530588853
        return (((days % synodic) + synodic) % synodic) / synodic
    }

    /**
     * Une vue en perspective (gnomonique) centrée sur ([azimuth], [altitude]),
     * tournée de [roll], 70° de champ en largeur.
     */
    private class View(val azimuth: Double, altitude: Double, roll: Double, private val width: Int, private val height: Int) {
        private val forward = vector(azimuth, altitude)
        private val right: DoubleArray
        private val up: DoubleArray
        private val scale = width / 2.0 / tan(35 * PI / 180)
        private val cosRoll = cos(-roll * PI / 180)
        private val sinRoll = sin(-roll * PI / 180)

        init {
            // droite = avant × zénith ; haut = droite × avant.
            val r = doubleArrayOf(forward[1], -forward[0], 0.0)
            val n = sqrt(r[0] * r[0] + r[1] * r[1]).coerceAtLeast(1e-6)
            right = doubleArrayOf(r[0] / n, r[1] / n, 0.0)
            up = doubleArrayOf(
                right[1] * forward[2] - right[2] * forward[1],
                right[2] * forward[0] - right[0] * forward[2],
                right[0] * forward[1] - right[1] * forward[0],
            )
        }

        fun project(azimuth: Double, altitude: Double): Pair<Float, Float>? {
            val v = vector(azimuth, altitude)
            val z = dot(v, forward)
            if (z <= 0.05) return null
            val x = dot(v, right) / z * scale
            val y = dot(v, up) / z * scale
            val rx = x * cosRoll - y * sinRoll
            val ry = x * sinRoll + y * cosRoll
            return (width / 2.0 + rx).toFloat() to (height / 2.0 - ry).toFloat()
        }

        private fun vector(azimuth: Double, altitude: Double): DoubleArray {
            val az = azimuth * PI / 180
            val alt = altitude * PI / 180
            return doubleArrayOf(sin(az) * cos(alt), cos(az) * cos(alt), sin(alt))
        }

        private fun dot(a: DoubleArray, b: DoubleArray) = a[0] * b[0] + a[1] * b[1] + a[2] * b[2]
    }
}
