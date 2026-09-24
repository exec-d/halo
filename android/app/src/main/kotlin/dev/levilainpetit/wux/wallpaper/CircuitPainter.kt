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
import dev.levilainpetit.wux.R
import kotlin.math.PI
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

    /**
     * Intensité du décor, de 0 à 1 : discret par défaut, pour que les
     * widgets et les icônes posés dessus restent lisibles.
     */
    var intensity = WallpaperSettings.DISCREET

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
    private val point = PointF()

    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG)
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


    fun draw(canvas: Canvas, state: FrameState, palette: CircuitPalette) {
        canvas.drawColor(BACKGROUND)

        val igniting = state.ignition < 1f

        scene.layers.forEachIndexed { index, layer ->
            val dx = state.tiltX * maxShift * layer.depth
            val dy = state.tiltY * maxShift * layer.depth
            canvas.save()
            canvas.translate(dx, dy)
            maskPaint.color = palette.glow
            maskPaint.alpha = (layer.alpha * 0.35f * state.intensity).toInt()
            glowRect.set(
                layer.glowOffsetX,
                layer.glowOffsetY,
                layer.glowOffsetX + layer.glow.width * CircuitScene.GLOW_SCALE,
                layer.glowOffsetY + layer.glow.height * CircuitScene.GLOW_SCALE,
            )
            canvas.drawBitmap(layer.glow, null, glowRect, maskPaint)
            maskPaint.color = palette.line
            maskPaint.alpha = (layer.alpha * state.intensity).toInt()
            canvas.drawBitmap(layer.core, 0f, 0f, maskPaint)
            if (igniting) ignite(canvas, index, state, palette)
            when (index) {
                CircuitScene.BATTERY -> battery(canvas, state, palette)
                CircuitScene.BOARD -> {
                    antennas(canvas, state, palette)
                    pulses(canvas, state, palette)
                }
            }
            canvas.restore()
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
        fill.alpha = ((22 + 30 * breath) * state.intensity).toInt()
        canvas.drawRect(cell.left, top, cell.right, cell.bottom, fill)
        stroke.color = palette.core
        stroke.alpha = (200 * state.intensity).toInt()
        stroke.strokeWidth = 1.4f * density
        canvas.drawLine(cell.left + 1.5f * density, top, cell.right - 1.5f * density, top, stroke)
    }

    private fun antennas(canvas: Canvas, state: FrameState, palette: CircuitPalette) {
        val strength = 0.25f + 0.75f * state.signal.coerceIn(0f, 1f)
        stroke.color = palette.glow
        stroke.alpha = (60 * strength * state.intensity).toInt()
        stroke.strokeWidth = 7f * density
        canvas.drawPath(scene.antennas, stroke)
        stroke.color = palette.core
        stroke.alpha = ((70 + 150 * strength) * state.intensity).toInt()
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
        val strength = 0.4f + 0.6f * state.intensity
        for (pulse in state.pulses) {
            // Une traînée de quelques points qui s'estompent.
            for (k in 3 downTo 0) {
                val t = pulse.progress - k * 0.018f
                if (t < 0f) continue
                pulse.route.at(t, point)
                val fade = 1f - k / 4f
                pulseMatrix.setTranslate(point.x, point.y)
                shader.setLocalMatrix(pulseMatrix)
                pulseGlow.alpha = (130 * fade * strength).toInt()
                canvas.drawCircle(point.x, point.y, 9f * density * fade, pulseGlow)
                fill.color = palette.core
                fill.alpha = (255 * fade * strength).toInt()
                canvas.drawCircle(point.x, point.y, max(1f, 2.2f * density * fade), fill)
            }
        }
    }

    /**
     * L'allumage : le décor est déjà là, et chaque composant ou piste
     * s'illumine à son tour, du processeur vers les bords, puis retombe.
     */
    private fun ignite(canvas: Canvas, layer: Int, state: FrameState, palette: CircuitPalette) {
        val strength = 0.5f + 0.5f * state.intensity
        for (part in scene.parts) {
            if (part.layer != layer) continue
            // Chaque éclat dure FLASH ; le dernier finit à la fin de l'allumage.
            val t = (state.ignition - part.delay * (1f - FLASH)) / FLASH
            if (t <= 0f || t >= 1f) continue
            // Montée rapide, descente douce.
            val a = if (t < 0.25f) t / 0.25f else 1f - (t - 0.25f) / 0.75f
            stroke.color = palette.glow
            stroke.alpha = (110 * a * strength).toInt()
            stroke.strokeWidth = 6f * density
            canvas.drawPath(part.path, stroke)
            stroke.color = palette.core
            stroke.alpha = (255 * a * strength).toInt()
            stroke.strokeWidth = 1.6f * density
            canvas.drawPath(part.path, stroke)
        }
    }

    companion object {
        /** Le fond de la nuit des widgets (`splash_background`). */
        val BACKGROUND = Color.rgb(5, 7, 13)

        /** Durée d'un éclat, en part de l'allumage. */
        const val FLASH = 0.3f
    }
}
