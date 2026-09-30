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
import kotlin.math.abs
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
    /** Décalage du plan le plus profond à pleine inclinaison. */
    private val maxShift = 36f * density

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
            intArrayOf(Color.TRANSPARENT, Color.argb(30, 255, 255, 255), Color.TRANSPARENT),
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
                CircuitScene.BATTERY -> when {
                    scene.flux != null -> flux(canvas, scene.flux, state, palette)
                    scene.arc != null -> arc(canvas, scene.arc, state, palette)
                    else -> battery(canvas, state, palette)
                }
                CircuitScene.BOARD -> {
                    scene.flux?.let { timeCircuits(canvas, it, state, palette) }
                    antennas(canvas, state, palette)
                    pulses(canvas, state, palette)
                }
            }
            canvas.restore()
        }

        glassMatrix.setTranslate(-state.tiltX * maxShift * 4f, -state.tiltY * maxShift * 4f)
        glass.shader?.setLocalMatrix(glassMatrix)
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

    /**
     * Le convecteur temporel : une impulsion court le long des trois bras,
     * lampe après lampe, jusqu'au cœur qui s'illumine ; plus vite pendant la
     * charge. La jauge au-dessus du hublot montre le niveau de batterie.
     */
    private fun flux(canvas: Canvas, flux: CircuitScene.Flux, state: FrameState, palette: CircuitPalette) {
        val u = flux.unit
        // L'animation reste visible en « Discret », comme les impulsions.
        val strength = 0.4f + 0.6f * state.intensity
        val speed = if (state.charging) 1.6f else 0.9f
        val phase = (state.timeMillis / 1000f * speed) % 1f * 7f
        for (lamps in flux.arms) {
            lamps.forEachIndexed { i, lamp ->
                val level = max(0f, 1f - abs(phase - i) * 1.4f)
                if (i < lamps.size - 1 && level > 0f) {
                    val next = lamps[i + 1]
                    stroke.color = palette.glow
                    stroke.alpha = (90 * level * strength).toInt()
                    stroke.strokeWidth = 5f * density
                    canvas.drawLine(lamp.x, lamp.y, next.x, next.y, stroke)
                    stroke.color = palette.core
                    stroke.alpha = (230 * level * strength).toInt()
                    stroke.strokeWidth = 1.4f * density
                    canvas.drawLine(lamp.x, lamp.y, next.x, next.y, stroke)
                }
                val a = (0.2f + 0.8f * level) * strength
                halo(canvas, lamp, 2.6f * u, (150 * a).toInt(), palette)
                fill.color = palette.core
                fill.alpha = (255 * a).toInt()
                canvas.drawCircle(lamp.x, lamp.y, 0.7f * u, fill)
            }
        }
        // Le cœur s'illumine quand les trois impulsions s'y rejoignent.
        val flash = max(0f, 1f - abs(phase - 5.3f) * 0.9f)
        halo(canvas, flux.core, flux.coreRadius * (1.4f + flash), ((50 + 200 * flash) * strength).toInt(), palette)
        fill.color = palette.core
        fill.alpha = ((60 + 195 * flash) * strength).toInt()
        canvas.drawCircle(flux.core.x, flux.core.y, flux.coreRadius * 0.55f, fill)

        // La jauge : une case par dixième de batterie, la dernière respire en charge.
        val g = flux.gauge
        val cells = (state.batteryLevel.coerceIn(0f, 1f) * 10f).let { kotlin.math.ceil(it).toInt() }.coerceIn(1, 10)
        val breath = if (state.charging) (sin(state.timeMillis / 700.0 * PI).toFloat() + 1f) / 2f else 1f
        val cell = g.width() / 10f
        for (i in 0 until cells) {
            fill.color = palette.glow
            val a = if (i == cells - 1) 0.35f + 0.65f * breath else 1f
            fill.alpha = (150 * a * strength).toInt()
            val left = g.left + i * cell
            canvas.drawRect(left + 0.35f * u, g.top + 0.35f * u, left + cell - 0.35f * u, g.bottom - 0.35f * u, fill)
        }
    }

    /**
     * Les circuits temporels, en afficheurs à sept segments : la destination
     * (21 octobre 2015, 16 h 29), le présent (la vraie année et l'heure), le
     * dernier départ (26 octobre 1985, 1 h 21). Mr. Fusion s'éclaire en charge.
     */
    private fun timeCircuits(canvas: Canvas, flux: CircuitScene.Flux, state: FrameState, palette: CircuitPalette) {
        val strength = 0.4f + 0.6f * state.intensity
        val now = java.time.LocalDateTime.now()
        val rows = listOf(
            "20151629",
            "%04d%02d%02d".format(java.util.Locale.ROOT, now.year, now.hour, now.minute),
            "19850121",
        )
        val colon = (System.currentTimeMillis() / 500) % 2 == 0L
        flux.clocks.forEachIndexed { r, rect -> sevenSegments(canvas, rect, rows[r], if (r == 1) colon else true, strength, palette) }
        if (state.charging) {
            val breath = (sin(state.timeMillis / 700.0 * PI).toFloat() + 1f) / 2f
            halo(canvas, flux.fusion, flux.fusionRadius * 1.8f, ((40 + 120 * breath) * strength).toInt(), palette)
        }
    }

    /** Huit chiffres dans [rect] : quatre pour l'année, un deux-points, quatre pour l'heure. */
    private fun sevenSegments(canvas: Canvas, rect: RectF, digits: String, colon: Boolean, strength: Float, palette: CircuitPalette) {
        val pad = rect.height() * 0.24f
        val gap = rect.height() * 0.5f
        val space = rect.height() * 0.14f
        val w = (rect.width() - 2 * pad - gap - 7 * space) / 8f
        val h = rect.height() - 2 * pad
        stroke.strokeWidth = (w * 0.16f).coerceAtLeast(0.8f * density)
        var x = rect.left + pad
        digits.forEachIndexed { i, ch ->
            val lit = SEGMENTS[ch - '0']
            for (seg in 0 until 7) {
                val on = lit and (1 shl seg) != 0
                val (x0, y0, x1, y1) = SEGMENT_LINES[seg]
                stroke.color = palette.core
                stroke.alpha = ((if (on) 230 else 22) * strength).toInt()
                canvas.drawLine(x + x0 * w, rect.top + pad + y0 * h, x + x1 * w, rect.top + pad + y1 * h, stroke)
            }
            x += w + space
            if (i == 3) {
                if (colon) {
                    fill.color = palette.core
                    fill.alpha = (230 * strength).toInt()
                    val cx = x - space / 2f + gap / 2f
                    canvas.drawCircle(cx, rect.top + pad + h * 0.3f, stroke.strokeWidth * 0.7f, fill)
                    canvas.drawCircle(cx, rect.top + pad + h * 0.7f, stroke.strokeWidth * 0.7f, fill)
                }
                x += gap
            }
        }
    }

    /**
     * Le réacteur arc, vivant :
     * - à l'allumage de l'écran, les bobines s'allument une à une, puis le
     *   cœur s'embrase ;
     * - une bobine allumée par dixième de batterie ; chacune scintille à son
     *   rythme, la lumière passe entre ses spires, et une lueur fait le tour ;
     * - deux pistes d'énergie tournent en sens contraires ;
     * - le cœur bat (deux coups rapprochés), plus vite en charge, et ses
     *   rayons s'étirent à chaque battement ;
     * - en charge, des particules spiralent des câbles vers le cœur ;
     * - batterie faible : le cœur devient instable et vacille.
     */
    private fun arc(canvas: Canvas, arc: CircuitScene.Arc, state: FrameState, palette: CircuitPalette) {
        val strength = 0.4f + 0.6f * state.intensity
        val seconds = state.timeMillis / 1000f
        val battery = state.batteryLevel.coerceIn(0f, 1f)
        val lit = kotlin.math.ceil(battery * CircuitScene.COILS).toInt().coerceIn(1, CircuitScene.COILS)
        val ignition = state.ignition.coerceIn(0f, 1f)
        val cx = arc.core.x
        val cy = arc.core.y

        // Une lueur diffuse derrière tout le boîtier.
        halo(canvas, arc.core, arc.radius * 1.15f, (45 * strength * ignition).toInt(), palette)

        // Les pistes d'énergie.
        val spin = seconds * (if (state.charging) 90f else 28f)
        for ((track, angle) in listOf(arc.outerTrack to spin, arc.innerTrack to -spin * 1.6f)) {
            canvas.save()
            canvas.rotate(angle % 360f, cx, cy)
            stroke.color = palette.glow
            stroke.alpha = (55 * strength * ignition).toInt()
            stroke.strokeWidth = 5f * density
            canvas.drawPath(track, stroke)
            stroke.color = palette.core
            stroke.alpha = (170 * strength * ignition).toInt()
            stroke.strokeWidth = 1.3f * density
            canvas.drawPath(track, stroke)
            canvas.restore()
        }

        // Les bobines.
        val sweep = (seconds * (if (state.charging) 220f else 80f)) % 360f
        arc.coils.forEachIndexed { i, coil ->
            // À l'allumage, la bobine i s'allume à son tour.
            val on = ((ignition - 0.08f * i) / 0.18f).coerceIn(0f, 1f)
            val angle = ((arc.angles[i] % 360f) + 360f) % 360f
            val distance = abs(((angle - sweep + 540f) % 360f) - 180f)
            val shine = max(0f, 1f - distance / 60f)
            val flicker = 0.86f + 0.14f * sin(seconds * 7.3f + i * 1.7f) * sin(seconds * 3.1f + i * 0.9f)
            val a = on * strength * if (i < lit) (0.45f + 0.55f * shine) * flicker else 0.05f + 0.1f * shine
            fill.color = palette.glow
            fill.alpha = (130 * a).toInt()
            canvas.drawPath(coil, fill)
            // La lumière entre les spires.
            stroke.color = palette.core
            stroke.alpha = (235 * a).toInt()
            stroke.strokeWidth = 0.9f * density
            canvas.drawLines(arc.windings[i], stroke)
            stroke.alpha = (180 * a).toInt()
            stroke.strokeWidth = 1.3f * density
            canvas.drawPath(coil, stroke)
        }

        // En charge : des particules qui spiralent vers le cœur.
        if (state.charging) {
            for (k in 0 until 10) {
                val p = ((seconds * 0.45f + k / 10f) % 1f)
                val r = arc.radius * (0.95f - 0.72f * p)
                val t = (k * 36f + p * 300f) * PI.toFloat() / 180f
                val x = cx + kotlin.math.cos(t) * r
                val y = cy + kotlin.math.sin(t) * r
                fill.color = palette.core
                fill.alpha = (230 * sin(p * PI.toFloat()) * strength).toInt()
                canvas.drawCircle(x, y, 0.45f * arc.unit, fill)
            }
        }

        // Le cœur bat : deux coups rapprochés par période.
        val period = if (state.charging) 0.8f else 1.15f
        val p = (seconds / period) % 1f
        val second = if (p > 0.22f) 0.6f * kotlin.math.exp(-(p - 0.22f) * 10f) else 0f
        val beat = (kotlin.math.exp(-p * 10f) + second).coerceAtMost(1f)
        var level = (0.5f + 0.5f * beat) * max(0f, (ignition - 0.7f) / 0.3f)
        // L'allumage finit par un éclat.
        if (ignition in 0.75f..0.999f) level = max(level, 1f - (ignition - 0.75f) * 3f)
        // Batterie faible : le cœur vacille.
        if (battery < 0.15f) {
            val tick = (seconds * 14f).toInt()
            val noise = ((tick * 1103515245 + 12345) ushr 16 and 0xFF) / 255f
            level *= 0.35f + 0.65f * noise
        }
        halo(canvas, arc.core, arc.coreRadius * (2.1f + 0.9f * beat), (215 * level * strength).toInt(), palette)
        // Les rayons, qui s'étirent au battement.
        stroke.color = palette.core
        stroke.strokeWidth = 1.1f * density
        stroke.alpha = (140 * level * strength).toInt()
        val turn = seconds * 12f * PI.toFloat() / 180f
        for (k in 0 until 6) {
            val t = turn + k * PI.toFloat() / 3f
            val r0 = arc.coreRadius * 0.62f
            val r1 = arc.coreRadius * (1.25f + 0.55f * beat)
            canvas.drawLine(
                cx + kotlin.math.cos(t) * r0, cy + kotlin.math.sin(t) * r0,
                cx + kotlin.math.cos(t) * r1, cy + kotlin.math.sin(t) * r1,
                stroke,
            )
        }
        // Le noyau : un petit disque vif, et le triangle éclairé autour.
        fill.color = palette.core
        fill.alpha = (255 * level * strength).toInt()
        canvas.drawCircle(cx, cy, arc.coreRadius * (0.26f + 0.05f * beat), fill)
        stroke.alpha = (220 * level * strength).toInt()
        stroke.strokeWidth = 1.4f * density
        canvas.drawCircle(cx, cy, arc.coreRadius * 0.47f, stroke)
    }

    /** Un halo rond, du cœur vers rien. */
    private fun halo(canvas: Canvas, at: PointF, radius: Float, alpha: Int, palette: CircuitPalette) {
        val shader = glowShader(palette)
        pulseMatrix.setScale(radius / (9f * density), radius / (9f * density))
        pulseMatrix.postTranslate(at.x, at.y)
        shader.setLocalMatrix(pulseMatrix)
        pulseGlow.shader = shader
        pulseGlow.alpha = alpha.coerceIn(0, 255)
        canvas.drawCircle(at.x, at.y, radius, pulseGlow)
    }

    private fun glowShader(palette: CircuitPalette): RadialGradient {
        if (pulseShader == null || pulseColor != palette.glow) {
            pulseColor = palette.glow
            pulseShader = RadialGradient(
                0f, 0f, 9f * density,
                palette.glow, palette.glow and 0x00FFFFFF, Shader.TileMode.CLAMP,
            )
        }
        return pulseShader!!
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
        val shader = glowShader(palette)
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
        /** Les segments allumés de chaque chiffre (bit 0 : a, en haut, … bit 6 : g, au milieu). */
        private val SEGMENTS = intArrayOf(0x3F, 0x06, 0x5B, 0x4F, 0x66, 0x6D, 0x7D, 0x07, 0x7F, 0x6F)

        /** Les segments a à g, en fraction de la case du chiffre. */
        private val SEGMENT_LINES = listOf(
            floatArrayOf(0.1f, 0f, 0.9f, 0f),
            floatArrayOf(1f, 0.08f, 1f, 0.46f),
            floatArrayOf(1f, 0.54f, 1f, 0.92f),
            floatArrayOf(0.1f, 1f, 0.9f, 1f),
            floatArrayOf(0f, 0.54f, 0f, 0.92f),
            floatArrayOf(0f, 0.08f, 0f, 0.46f),
            floatArrayOf(0.1f, 0.5f, 0.9f, 0.5f),
        )

        /** Le fond de la nuit des widgets (`splash_background`). */
        val BACKGROUND = Color.rgb(5, 7, 13)

        /** Durée d'un éclat, en part de l'allumage. */
        const val FLASH = 0.3f
    }
}
