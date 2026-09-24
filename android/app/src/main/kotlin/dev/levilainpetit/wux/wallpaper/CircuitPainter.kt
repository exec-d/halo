package dev.levilainpetit.wux.wallpaper

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import dev.levilainpetit.wux.R
import kotlin.math.PI
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.sin

/** Couleurs du fond d'écran : celles des widgets, donc du téléphone (Material You). */
data class CircuitPalette(val core: Int, val line: Int, val glow: Int) {
    companion object {
        fun of(context: Context) = CircuitPalette(
            core = context.getColor(R.color.clock_core),
            line = context.getColor(R.color.clock_line),
            glow = context.getColor(R.color.clock_glow),
        )
    }
}

/** Une impulsion en route : [progress] de 0 à 1 le long de [route]. */
class Pulse(val route: CircuitScene.Route, val speed: Float, var progress: Float = 0f)

/** Tout ce qui change d'une image à l'autre. */
class FrameState {
    /** Inclinaison, de -1 à 1 sur chaque axe. */
    var tiltX = 0f
    var tiltY = 0f

    /** Allumage, de 0 (éteint) à 1 (tout allumé). */
    var ignition = 1f
    var batteryLevel = 0.8f
    var charging = false

    /** Force du signal, de 0 à 1. */
    var signal = 0.75f
    var timeMillis = 0L

    /** Écran de verrouillage : le haut est assombri pour l'horloge. */
    var locked = false
    val pulses = mutableListOf<Pulse>()
}

/**
 * Dessine une image du fond d'écran : les plans de [CircuitScene] décalés
 * selon l'inclinaison, puis ce qui vit (niveau de batterie, antennes,
 * impulsions, reflet du verre).
 */
class CircuitPainter(private val scene: CircuitScene) {

    private val density = scene.density
    private val maxShift = 16f * density

    private val maskPaint = Paint(Paint.FILTER_BITMAP_FLAG)
    private val glowRect = RectF()
    private val clip = Path()
    private val point = PointF()

    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val text = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.MONOSPACE
        textSize = 15f * density
        isFakeBoldText = true
    }
    private val pulseGlow = Paint(Paint.ANTI_ALIAS_FLAG)
    /** Le halo d'une impulsion, recréé quand la couleur du téléphone change. */
    private var pulseShader: RadialGradient? = null
    private var pulseColor = 0
    private val pulseMatrix = Matrix()

    private val glass = Paint().apply {
        shader = LinearGradient(
            0f, 0f, scene.width * 0.6f, scene.height * 0.35f,
            intArrayOf(Color.TRANSPARENT, Color.argb(16, 255, 255, 255), Color.TRANSPARENT),
            floatArrayOf(0.35f, 0.5f, 0.65f),
            Shader.TileMode.CLAMP,
        )
    }
    private val glassMatrix = Matrix()

    private val lockShade = Paint().apply {
        shader = LinearGradient(
            0f, 0f, 0f, scene.height * 0.34f,
            Color.argb(190, 0, 0, 0), Color.TRANSPARENT, Shader.TileMode.CLAMP,
        )
    }

    /** Portée de l'allumage : la distance du processeur au coin le plus loin. */
    private val reach = listOf(0f to 0f, scene.width.toFloat() to 0f, 0f to scene.height.toFloat(), scene.width.toFloat() to scene.height.toFloat())
        .maxOf { (x, y) -> hypot(x - scene.origin.x, y - scene.origin.y) }

    fun draw(canvas: Canvas, state: FrameState, palette: CircuitPalette) {
        canvas.drawColor(BACKGROUND)

        val ignition = ease(state.ignition)
        val radius = ignition * reach
        val igniting = state.ignition < 1f
        if (igniting) {
            canvas.save()
            clip.rewind()
            clip.addCircle(scene.origin.x, scene.origin.y, radius, Path.Direction.CW)
            canvas.clipPath(clip)
        }

        scene.layers.forEachIndexed { index, layer ->
            val dx = state.tiltX * maxShift * layer.depth
            val dy = state.tiltY * maxShift * layer.depth
            canvas.save()
            canvas.translate(dx, dy)
            maskPaint.color = palette.glow
            maskPaint.alpha = layer.alpha * 3 / 5
            glowRect.set(
                layer.glowOffsetX,
                layer.glowOffsetY,
                layer.glowOffsetX + layer.glow.width * CircuitScene.GLOW_SCALE,
                layer.glowOffsetY + layer.glow.height * CircuitScene.GLOW_SCALE,
            )
            canvas.drawBitmap(layer.glow, null, glowRect, maskPaint)
            maskPaint.color = if (index == CircuitScene.BOARD) palette.core else palette.line
            maskPaint.alpha = layer.alpha
            canvas.drawBitmap(layer.core, 0f, 0f, maskPaint)
            when (index) {
                CircuitScene.BATTERY -> battery(canvas, state, palette)
                CircuitScene.BOARD -> {
                    antennas(canvas, state, palette)
                    pulses(canvas, state, palette)
                }
            }
            canvas.restore()
        }

        if (igniting) {
            canvas.restore()
            // Le front de l'allumage.
            stroke.color = palette.core
            stroke.alpha = ((1f - ignition) * 220).toInt()
            stroke.strokeWidth = 2f * density
            canvas.drawCircle(scene.origin.x, scene.origin.y, radius, stroke)
        }

        glassMatrix.setTranslate(-state.tiltX * maxShift * 6f, -state.tiltY * maxShift * 6f)
        glass.shader.setLocalMatrix(glassMatrix)
        canvas.drawRect(0f, 0f, scene.width.toFloat(), scene.height.toFloat(), glass)

        if (state.locked) canvas.drawRect(0f, 0f, scene.width.toFloat(), scene.height * 0.34f, lockShade)
    }

    private fun battery(canvas: Canvas, state: FrameState, palette: CircuitPalette) {
        val cell = scene.batteryCell
        val level = state.batteryLevel.coerceIn(0f, 1f)
        val top = cell.bottom - cell.height() * level
        // En charge, le niveau respire.
        val breath = if (state.charging) (sin(state.timeMillis / 700.0 * PI).toFloat() + 1f) / 2f else 0f
        fill.color = palette.glow
        fill.alpha = (34 + 40 * breath).toInt()
        canvas.drawRect(cell.left, top, cell.right, cell.bottom, fill)
        stroke.color = palette.core
        stroke.alpha = 220
        stroke.strokeWidth = 1.4f * density
        canvas.drawLine(cell.left + 1.5f * density, top, cell.right - 1.5f * density, top, stroke)

        text.color = palette.core
        text.alpha = 230
        val label = "${(level * 100).toInt()} %" + if (state.charging) "  ⚡" else ""
        canvas.drawText(label, cell.left + 2f * density + cell.width() * 0.04f, cell.top + text.textSize * 1.6f, text)
    }

    private fun antennas(canvas: Canvas, state: FrameState, palette: CircuitPalette) {
        val strength = 0.25f + 0.75f * state.signal.coerceIn(0f, 1f)
        stroke.color = palette.glow
        stroke.alpha = (70 * strength).toInt()
        stroke.strokeWidth = 7f * density
        canvas.drawPath(scene.antennas, stroke)
        stroke.color = palette.core
        stroke.alpha = (90 + 150 * strength).toInt()
        stroke.strokeWidth = 1.6f * density
        canvas.drawPath(scene.antennas, stroke)
    }

    private fun pulses(canvas: Canvas, state: FrameState, palette: CircuitPalette) {
        if (state.pulses.isEmpty()) return
        if (pulseShader == null || pulseColor != palette.glow) {
            pulseColor = palette.glow
            pulseShader = RadialGradient(
                0f, 0f, 9f * density,
                palette.glow, palette.glow and 0x00FFFFFF, Shader.TileMode.CLAMP,
            )
        }
        val shader = pulseShader!!
        pulseGlow.shader = shader
        for (pulse in state.pulses) {
            // Une traînée de quelques points qui s'estompent.
            for (k in 3 downTo 0) {
                val t = pulse.progress - k * 0.018f
                if (t < 0f) continue
                pulse.route.at(t, point)
                val fade = 1f - k / 4f
                pulseMatrix.setTranslate(point.x, point.y)
                shader.setLocalMatrix(pulseMatrix)
                pulseGlow.alpha = (150 * fade).toInt()
                canvas.drawCircle(point.x, point.y, 9f * density * fade, pulseGlow)
                fill.color = palette.core
                fill.alpha = (255 * fade).toInt()
                canvas.drawCircle(point.x, point.y, max(1f, 2.2f * density * fade), fill)
            }
        }
    }

    private fun ease(t: Float): Float {
        val c = t.coerceIn(0f, 1f)
        return 1f - (1f - c) * (1f - c) * (1f - c)
    }

    companion object {
        /** Le fond de la nuit des widgets (`splash_background`). */
        val BACKGROUND = Color.rgb(5, 7, 13)
    }
}
