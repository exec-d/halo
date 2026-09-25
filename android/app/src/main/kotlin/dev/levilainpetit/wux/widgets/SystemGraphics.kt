package dev.levilainpetit.wux.widgets

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import dev.levilainpetit.wux.system.BatterySample
import dev.levilainpetit.wux.system.ConnectedDevice
import dev.levilainpetit.wux.system.MobileData
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Dessins des widgets système, en `ALPHA_8` comme ceux de la météo : blancs,
 * teints par la mise en page aux couleurs du téléphone, nuances par l'opacité.
 */
object SystemGraphics {

    private fun canvas(width: Int, height: Int): Pair<Bitmap, Canvas> {
        val bitmap = Bitmap.createBitmap(width.coerceAtLeast(1), height.coerceAtLeast(1), Bitmap.Config.ALPHA_8)
        return bitmap to Canvas(bitmap)
    }

    private fun stroke(width: Float) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = width
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private fun label(density: Float) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.MONOSPACE
        textSize = 8.5f * density
        alpha = 190
    }

    // ——— Batterie : les 24 dernières heures, et la suite prévue ———

    /**
     * La courbe du niveau sur 24 heures (80 % de la largeur), les périodes de
     * charge remplies plus fort, le point « maintenant », puis sur 6 heures la
     * suite prévue en pointillés : jusqu'à la charge complète, ou la descente
     * au rythme actuel.
     */
    fun batteryChart(
        history: List<BatterySample>,
        level: Int,
        charging: Boolean,
        minutesLeft: Int?,
        now: Long,
        /** L'étiquette de « maintenant » sous la courbe. */
        nowLabel: String,
        widthPx: Int,
        heightPx: Int,
        density: Float,
    ): Bitmap {
        val (bitmap, canvas) = canvas(widthPx, heightPx)
        val text = label(density)
        val top = 4 * density
        val bottom = heightPx - text.textSize - 5 * density
        val nowX = widthPx * 0.8f
        val hour = 3_600_000f
        fun x(time: Long) = nowX - (now - time) / (24 * hour) * nowX
        fun y(value: Float) = bottom - (bottom - top) * value / 100f

        // Repères 0, 50 et 100 %.
        val grid = stroke(0.8f * density).apply {
            alpha = 60
            pathEffect = DashPathEffect(floatArrayOf(2 * density, 3 * density), 0f)
        }
        for (v in listOf(0f, 50f, 100f)) canvas.drawLine(0f, y(v), widthPx.toFloat(), y(v), grid)

        val points = (history.filter { now - it.time <= 24 * hour } + BatterySample(now, level, charging))
            .sortedBy { it.time }
        if (points.size >= 2) {
            // Remplissage, plus fort pendant les charges.
            val fill = Paint(Paint.ANTI_ALIAS_FLAG)
            for (i in 1 until points.size) {
                val a = points[i - 1]
                val b = points[i]
                val area = Path().apply {
                    moveTo(x(a.time), bottom)
                    lineTo(x(a.time), y(a.level.toFloat()))
                    lineTo(x(b.time), y(b.level.toFloat()))
                    lineTo(x(b.time), bottom)
                    close()
                }
                fill.alpha = if (a.charging) 120 else 40
                canvas.drawPath(area, fill)
            }
            val line = Path().apply {
                moveTo(x(points[0].time), y(points[0].level.toFloat()))
                points.drop(1).forEach { lineTo(x(it.time), y(it.level.toFloat())) }
            }
            canvas.drawPath(line, stroke(1.6f * density))
        }

        // La suite prévue.
        val future = stroke(1.4f * density).apply {
            alpha = 170
            pathEffect = DashPathEffect(floatArrayOf(3 * density, 3 * density), 0f)
        }
        if (minutesLeft != null && minutesLeft > 0) {
            val target = if (charging) 100f else 0f
            val horizon = 6 * 60f
            val reach = min(1f, horizon / minutesLeft)
            val endLevel = level + (target - level) * reach
            val endX = nowX + (widthPx - nowX) * min(1f, minutesLeft / horizon)
            canvas.drawLine(nowX, y(level.toFloat()), endX, y(endLevel), future)
        }

        // Maintenant.
        val nowY = y(level.toFloat())
        canvas.drawCircle(nowX, nowY, 6 * density, Paint(Paint.ANTI_ALIAS_FLAG).apply { alpha = 70 })
        canvas.drawCircle(nowX, nowY, 3 * density, Paint(Paint.ANTI_ALIAS_FLAG))
        canvas.drawLine(nowX, top, nowX, bottom, stroke(0.8f * density).apply { alpha = 90 })

        // Échelle de temps.
        val baseline = heightPx - 2 * density
        text.textAlign = Paint.Align.LEFT
        canvas.drawText("-24 h", 0f, baseline, text)
        text.textAlign = Paint.Align.CENTER
        canvas.drawText("-12 h", x(now - 12 * hour.toLong()), baseline, text)
        canvas.drawText(nowLabel, nowX, baseline, text)
        text.textAlign = Paint.Align.RIGHT
        canvas.drawText("+6 h", widthPx.toFloat(), baseline, text)
        return bitmap
    }

    // ——— Appareils Bluetooth ———

    /** Une pile de 5 cellules, allumées de bas en haut selon [level]. */
    fun cells(level: Int?, widthPx: Int, heightPx: Int, density: Float): Bitmap {
        val (bitmap, canvas) = canvas(widthPx, heightPx)
        val count = 5
        val gap = 2.5f * density
        val cap = 3 * density
        val body = RectF(0f, cap + gap, widthPx.toFloat(), heightPx.toFloat())
        // Borne du haut.
        canvas.drawRoundRect(RectF(widthPx * 0.3f, 0f, widthPx * 0.7f, cap), cap / 2, cap / 2, Paint(Paint.ANTI_ALIAS_FLAG).apply { alpha = 150 })
        val cell = (body.height() - gap * (count - 1)) / count
        val lit = if (level == null) 0 else ((level + 10) / 20).coerceIn(0, count)
        for (i in 0 until count) {
            val bottom = body.bottom - i * (cell + gap)
            val rect = RectF(body.left, bottom - cell, body.right, bottom)
            if (i < lit) {
                canvas.drawRoundRect(rect, 1.5f * density, 1.5f * density, Paint(Paint.ANTI_ALIAS_FLAG))
            } else {
                canvas.drawRoundRect(rect, 1.5f * density, 1.5f * density, stroke(1f * density).apply { alpha = 110 })
            }
        }
        return bitmap
    }

    /** Silhouette d'un type d'appareil, tracée au trait. */
    fun deviceGlyph(kind: ConnectedDevice.Kind, sizePx: Int, density: Float): Bitmap {
        val (bitmap, canvas) = canvas(sizePx, sizePx)
        val s = sizePx.toFloat()
        val line = stroke(max(1.6f * density, s * 0.07f))
        val fill = Paint(Paint.ANTI_ALIAS_FLAG)
        when (kind) {
            ConnectedDevice.Kind.HEADPHONES -> {
                canvas.drawArc(RectF(s * 0.15f, s * 0.12f, s * 0.85f, s * 0.82f), 180f, 180f, false, line)
                canvas.drawRoundRect(RectF(s * 0.1f, s * 0.48f, s * 0.32f, s * 0.88f), s * 0.08f, s * 0.08f, fill)
                canvas.drawRoundRect(RectF(s * 0.68f, s * 0.48f, s * 0.9f, s * 0.88f), s * 0.08f, s * 0.08f, fill)
            }
            ConnectedDevice.Kind.WATCH -> {
                canvas.drawRoundRect(RectF(s * 0.36f, s * 0.04f, s * 0.64f, s * 0.24f), s * 0.04f, s * 0.04f, fill)
                canvas.drawRoundRect(RectF(s * 0.36f, s * 0.76f, s * 0.64f, s * 0.96f), s * 0.04f, s * 0.04f, fill)
                canvas.drawCircle(s * 0.5f, s * 0.5f, s * 0.3f, line)
                canvas.drawLine(s * 0.5f, s * 0.5f, s * 0.5f, s * 0.34f, line)
                canvas.drawLine(s * 0.5f, s * 0.5f, s * 0.62f, s * 0.56f, line)
            }
            ConnectedDevice.Kind.SPEAKER -> {
                canvas.drawRoundRect(RectF(s * 0.22f, s * 0.08f, s * 0.78f, s * 0.92f), s * 0.1f, s * 0.1f, line)
                canvas.drawCircle(s * 0.5f, s * 0.62f, s * 0.16f, line)
                canvas.drawCircle(s * 0.5f, s * 0.27f, s * 0.06f, fill)
            }
            ConnectedDevice.Kind.CAR -> {
                val body = Path().apply {
                    moveTo(s * 0.08f, s * 0.7f)
                    lineTo(s * 0.08f, s * 0.52f)
                    lineTo(s * 0.26f, s * 0.26f)
                    lineTo(s * 0.74f, s * 0.26f)
                    lineTo(s * 0.92f, s * 0.52f)
                    lineTo(s * 0.92f, s * 0.7f)
                    close()
                }
                canvas.drawPath(body, line)
                canvas.drawCircle(s * 0.28f, s * 0.74f, s * 0.09f, fill)
                canvas.drawCircle(s * 0.72f, s * 0.74f, s * 0.09f, fill)
            }
            ConnectedDevice.Kind.INPUT -> {
                canvas.drawRoundRect(RectF(s * 0.06f, s * 0.28f, s * 0.94f, s * 0.72f), s * 0.08f, s * 0.08f, line)
                for (row in 0..1) for (col in 0..4) {
                    val cx = s * (0.2f + col * 0.15f)
                    val cy = s * (0.42f + row * 0.16f)
                    canvas.drawCircle(cx, cy, s * 0.035f, fill)
                }
            }
            ConnectedDevice.Kind.OTHER -> {
                // La rune Bluetooth.
                val rune = Path().apply {
                    moveTo(s * 0.3f, s * 0.3f)
                    lineTo(s * 0.7f, s * 0.68f)
                    lineTo(s * 0.5f, s * 0.86f)
                    lineTo(s * 0.5f, s * 0.14f)
                    lineTo(s * 0.7f, s * 0.32f)
                    lineTo(s * 0.3f, s * 0.7f)
                }
                canvas.drawPath(rune, line)
            }
        }
        return bitmap
    }

    // ——— Temps d'écran : un cadran de 24 heures ———

    /**
     * La journée en cadran : minuit en haut, midi en bas, un arc épais pour
     * chaque période écran allumé, et une aiguille pour l'heure actuelle.
     */
    fun dial(sessions: List<Pair<Long, Long>>, dayStart: Long, now: Long, sizePx: Int, density: Float): Bitmap {
        val (bitmap, canvas) = canvas(sizePx, sizePx)
        val c = sizePx / 2f
        val ring = c - 12 * density
        val day = 24 * 3_600_000f
        fun angle(time: Long) = (time - dayStart) / day * 360f - 90f

        // Graduations : une par heure, plus longues toutes les 6 heures.
        val tick = stroke(1f * density)
        for (h in 0 until 24) {
            val a = (h * 15f - 90f) * PI.toFloat() / 180f
            val long = h % 6 == 0
            tick.alpha = if (long) 220 else 90
            val inner = c - (if (long) 7f else 4f) * density
            val outer = c - 1.5f * density
            canvas.drawLine(c + cos(a) * inner, c + sin(a) * inner, c + cos(a) * outer, c + sin(a) * outer, tick)
        }
        val text = label(density).apply { textAlign = Paint.Align.CENTER }
        val labelRadius = ring - 11 * density
        for ((h, name) in listOf(0 to "0", 6 to "6", 12 to "12", 18 to "18")) {
            val a = (h * 15f - 90f) * PI.toFloat() / 180f
            canvas.drawText(name, c + cos(a) * labelRadius, c + sin(a) * labelRadius + text.textSize * 0.35f, text)
        }

        // Piste, puis les séances.
        val oval = RectF(c - ring, c - ring, c + ring, c + ring)
        canvas.drawArc(oval, 0f, 360f, false, stroke(5 * density).apply { alpha = 45 })
        val arc = stroke(5 * density).apply { strokeCap = Paint.Cap.BUTT }
        for ((from, to) in sessions) {
            val start = angle(from)
            val sweep = max(1.5f, (to - from) / day * 360f)
            canvas.drawArc(oval, start, sweep, false, arc)
        }

        // Aiguille de l'heure actuelle.
        val a = angle(now) * PI.toFloat() / 180f
        canvas.drawLine(c + cos(a) * (ring - 8 * density), c + sin(a) * (ring - 8 * density), c + cos(a) * (ring + 8 * density), c + sin(a) * (ring + 8 * density), stroke(1.6f * density))
        canvas.drawCircle(c + cos(a) * ring, c + sin(a) * ring, 3.5f * density, Paint(Paint.ANTI_ALIAS_FLAG))
        return bitmap
    }

    /**
     * Silhouette d'une icône d'appli, pour la teindre : sa couche monochrome
     * (Android 13+), sinon son premier plan, sinon l'icône entière.
     */
    fun silhouette(icon: Drawable?, sizePx: Int): Bitmap? {
        icon ?: return null
        val argb = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(argb)
        val layer: Drawable = if (icon is AdaptiveIconDrawable) {
            (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) icon.monochrome else null) ?: icon.foreground ?: icon
        } else {
            icon
        }
        if (layer !== icon) {
            // Les couches adaptatives débordent de 18/108 de chaque côté.
            val extra = (sizePx * 18f / 72f).roundToInt()
            layer.setBounds(-extra, -extra, sizePx + extra, sizePx + extra)
        } else {
            layer.setBounds(0, 0, sizePx, sizePx)
        }
        layer.draw(canvas)
        val alpha = argb.extractAlpha()
        argb.recycle()
        return alpha
    }

    // ——— Données mobiles : la période en cours ———

    /**
     * Le cumul de la période jour après jour (trait plein, rempli), le rythme
     * qui mènerait pile au forfait (pointillés fins), la projection au rythme
     * actuel (pointillés), et en pied une barre par jour.
     */
    fun dataChart(data: MobileData, widthPx: Int, heightPx: Int, density: Float): Bitmap {
        val (bitmap, canvas) = canvas(widthPx, heightPx)
        val text = label(density)
        val days = data.length.coerceAtLeast(1)
        val barsBand = heightPx * 0.22f
        val top = text.textSize + 4 * density
        val bottom = heightPx - barsBand - 4 * density
        val quota = data.quota
        // L'échelle suit la consommation ; un forfait bien au-dessus est
        // seulement signalé en haut, pour ne pas écraser la courbe.
        val reach = max(data.projected, data.used).coerceAtLeast(1L)
        val quotaShown = quota != null && quota <= reach * 1.6f
        val scale = (if (quotaShown) max(reach, quota!!) else reach) * 1.12f
        fun x(day: Float) = widthPx * day / days
        fun y(bytes: Long) = bottom - (bottom - top) * (bytes / scale)

        // Forfait : un plafond, et le rythme qui y mène.
        if (quota != null && !quotaShown) {
            text.textAlign = Paint.Align.RIGHT
            canvas.drawText("${formatBytes(quota)} ↑", widthPx.toFloat(), text.textSize, text)
        }
        if (quota != null && quotaShown) {
            val cap = stroke(1f * density).apply { alpha = 150 }
            canvas.drawLine(0f, y(quota), widthPx.toFloat(), y(quota), cap)
            text.textAlign = Paint.Align.RIGHT
            canvas.drawText(formatBytes(quota), widthPx.toFloat(), y(quota) - 3 * density, text)
            val pace = stroke(1f * density).apply {
                alpha = 90
                pathEffect = DashPathEffect(floatArrayOf(1.5f * density, 3 * density), 0f)
            }
            canvas.drawLine(0f, bottom, widthPx.toFloat(), y(quota), pace)
        }

        // Le cumul.
        var total = 0L
        val line = Path().apply { moveTo(0f, bottom) }
        val area = Path().apply { moveTo(0f, bottom) }
        data.days.forEachIndexed { i, bytes ->
            total += bytes
            line.lineTo(x(i + 1f), y(total))
            area.lineTo(x(i + 1f), y(total))
        }
        area.lineTo(x(data.days.size.toFloat()), bottom)
        area.close()
        canvas.drawPath(area, Paint(Paint.ANTI_ALIAS_FLAG).apply { alpha = 45 })
        canvas.drawPath(line, stroke(1.8f * density))
        val todayX = x(data.days.size.toFloat())
        canvas.drawCircle(todayX, y(total), 3 * density, Paint(Paint.ANTI_ALIAS_FLAG))

        // La projection jusqu'à la fin de la période.
        if (data.days.size < days) {
            val projection = stroke(1.4f * density).apply {
                alpha = 170
                pathEffect = DashPathEffect(floatArrayOf(3 * density, 3 * density), 0f)
            }
            canvas.drawLine(todayX, y(total), widthPx.toFloat(), y(data.projected), projection)
        }

        // Une barre par jour ; points pour les jours à venir.
        val maxDay = (data.days.maxOrNull() ?: 0L).coerceAtLeast(1L)
        val slot = widthPx.toFloat() / days
        val barWidth = max(1.5f * density, slot * 0.55f)
        val base = heightPx.toFloat()
        val fill = Paint(Paint.ANTI_ALIAS_FLAG)
        for (i in 0 until days) {
            val cx = slot * i + slot / 2
            if (i < data.days.size) {
                val h = max(1.5f * density, (barsBand - 2 * density) * data.days[i] / maxDay)
                fill.alpha = if (i == data.days.size - 1) 255 else 150
                canvas.drawRect(cx - barWidth / 2, base - h, cx + barWidth / 2, base, fill)
            } else {
                fill.alpha = 90
                canvas.drawCircle(cx, base - 1.2f * density, 0.9f * density, fill)
            }
        }
        return bitmap
    }

    /** 320 Mo, 12,4 Go, 150 Go — ou MB et GB hors du français. */
    fun formatBytes(bytes: Long): String {
        val locale = java.util.Locale.getDefault()
        val french = locale.language == "fr"
        val mbUnit = if (french) "Mo" else "MB"
        val gbUnit = if (french) "Go" else "GB"
        val mb = bytes / 1_000_000.0
        return when {
            mb < 1000 -> "${mb.roundToInt()} $mbUnit"
            mb < 100_000 -> String.format(locale, "%.1f %s", mb / 1000, gbUnit)
            else -> "${(mb / 1000).roundToInt()} $gbUnit"
        }
    }

    /** 3 h 12, 42 min. */
    fun formatDuration(millis: Long): String {
        val minutes = millis / 60_000
        return if (minutes < 60) "$minutes min" else "${minutes / 60} h ${"%02d".format(minutes % 60)}"
    }
}
