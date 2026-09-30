package dev.levilainpetit.wux.widgets

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.text.format.DateFormat
import android.util.SizeF
import android.view.View
import android.widget.RemoteViews
import dev.levilainpetit.wux.MainActivity
import dev.levilainpetit.wux.R
import dev.levilainpetit.wux.system.Health
import dev.levilainpetit.wux.system.HealthDay
import dev.levilainpetit.wux.system.HealthSnapshot
import dev.levilainpetit.wux.system.Sleep
import dev.levilainpetit.wux.system.SleepStage
import dev.levilainpetit.wux.system.SystemRefresh
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Pas et sommeil : l'anneau des pas du jour vers l'objectif, la dernière
 * nuit (durée, coucher et lever, phases sur une échelle d'heures) et les pas
 * des sept derniers jours, chaque jour sous sa barre et son total au-dessus.
 * Les données viennent de Santé Connect (system/Health.kt).
 */
class HealthWidget : NeonWidget() {

    override fun build(context: Context, size: SizeF, sample: Boolean): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_health)
        views.setOnClickPendingIntent(R.id.health_root, activity(context, Intent(context, MainActivity::class.java), 68))
        val snapshot = if (sample) sample() else Health.snapshot(context)
        val goal = goal(context)
        val density = context.resources.displayMetrics.density
        if (snapshot == null) {
            views.setTextViewText(R.id.health_steps, "--")
            views.setTextViewText(R.id.health_steps_share, "")
            views.setTextViewText(R.id.health_sleep, "--")
            views.setTextViewText(R.id.health_sleep_range, context.getString(R.string.health_connect))
            views.setViewVisibility(R.id.health_hypno, View.GONE)
            views.setViewVisibility(R.id.health_legend, View.GONE)
            views.setViewVisibility(R.id.health_week, View.GONE)
            views.setViewVisibility(R.id.health_footer, View.GONE)
            views.setImageViewBitmap(R.id.health_ring, ring(0f, (104 * density).roundToInt(), density))
            return views
        }
        val steps = snapshot.today
        views.setTextViewText(R.id.health_steps, grouped(steps))
        views.setTextViewText(R.id.health_steps_share, context.getString(R.string.health_steps_share, (steps * 100 / goal.coerceAtLeast(1)).toInt()))
        views.setImageViewBitmap(R.id.health_ring, ring((steps.toFloat() / goal).coerceIn(0f, 1f), (104 * density).roundToInt(), density))

        val time = DateTimeFormatter.ofPattern(if (DateFormat.is24HourFormat(context)) "HH:mm" else "h:mm a", Locale.getDefault())
        val zone = ZoneId.systemDefault()
        val sleep = snapshot.sleep
        if (sleep == null) {
            views.setTextViewText(R.id.health_sleep, "--")
            views.setTextViewText(R.id.health_sleep_range, context.getString(R.string.health_no_sleep))
            views.setViewVisibility(R.id.health_hypno, View.GONE)
            views.setViewVisibility(R.id.health_legend, View.GONE)
        } else {
            views.setTextViewText(R.id.health_sleep, duration(context, sleep.end - sleep.start - awake(sleep)))
            views.setTextViewText(
                R.id.health_sleep_range,
                "${time.format(Instant.ofEpochMilli(sleep.start).atZone(zone))} → ${time.format(Instant.ofEpochMilli(sleep.end).atZone(zone))}",
            )
            val staged = sleep.stages.any { it.kind != SleepStage.Kind.SLEEPING && it.kind != SleepStage.Kind.AWAKE }
            views.setViewVisibility(R.id.health_hypno, if (staged) View.VISIBLE else View.GONE)
            views.setViewVisibility(R.id.health_legend, if (staged) View.VISIBLE else View.GONE)
            if (staged) {
                val width = ((size.width - 48 - 104 - 14) * density).roundToInt()
                views.setImageViewBitmap(R.id.health_hypno, hypnogram(sleep, zone, width, (40 * density).roundToInt(), density))
                fun total(kind: SleepStage.Kind) = sleep.stages.filter { it.kind == kind }.sumOf { it.end - it.start }
                views.setTextViewText(
                    R.id.health_legend,
                    context.getString(
                        R.string.health_legend,
                        duration(context, total(SleepStage.Kind.DEEP)),
                        duration(context, total(SleepStage.Kind.LIGHT)),
                        duration(context, total(SleepStage.Kind.REM)),
                    ),
                )
            }
        }

        val showWeek = size.height >= 190f
        views.setViewVisibility(R.id.health_week, if (showWeek) View.VISIBLE else View.GONE)
        views.setViewVisibility(R.id.health_footer, if (showWeek) View.VISIBLE else View.GONE)
        if (showWeek) {
            views.setImageViewBitmap(R.id.health_week, week(snapshot.days, ((size.width - 48) * density).roundToInt(), (58 * density).roundToInt(), density))
            val average = snapshot.days.map { it.steps }.average()
            views.setTextViewText(R.id.health_footer, context.getString(R.string.health_footer, grouped(goal.toLong()), thousands(average)))
        }
        return views
    }

    override fun onRendered(context: Context) = SystemRefresh.schedule(context)

    /** Temps éveillé pendant la nuit, retiré de sa durée. */
    private fun awake(sleep: Sleep) = sleep.stages.filter { it.kind == SleepStage.Kind.AWAKE }.sumOf { it.end - it.start }

    private fun duration(context: Context, millis: Long): String {
        val minutes = (millis / 60_000).toInt().coerceAtLeast(0)
        return context.getString(R.string.health_duration, minutes / 60, minutes % 60)
    }

    private fun grouped(value: Long) = String.format(Locale.getDefault(), "%,d", value).replace(',', ' ').replace(' ', ' ')

    /** 7,2k, 10,4k… */
    private fun thousands(value: Double) =
        if (value < 1000) value.roundToInt().toString() else String.format(Locale.getDefault(), "%.1fk", value / 1000)

    /** L'anneau des pas (en blanc, teinté par la mise en page). */
    private fun ring(share: Float, size: Int, density: Float): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ALPHA_8)
        val canvas = Canvas(bitmap)
        val stroke = 8 * density
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = stroke
            strokeCap = Paint.Cap.ROUND
        }
        val box = RectF(stroke / 2 + density, stroke / 2 + density, size - stroke / 2 - density, size - stroke / 2 - density)
        paint.alpha = 55
        canvas.drawArc(box, 0f, 360f, false, paint)
        paint.alpha = 255
        if (share > 0f) canvas.drawArc(box, -90f, 360f * share, false, paint)
        return bitmap
    }

    /** Les phases de la nuit, hauteur selon la profondeur, et les heures dessous. */
    private fun hypnogram(sleep: Sleep, zone: ZoneId, width: Int, height: Int, density: Float): Bitmap {
        val bitmap = Bitmap.createBitmap(width.coerceAtLeast(1), height.coerceAtLeast(1), Bitmap.Config.ALPHA_8)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val label = 8.5f * density
        val base = height - label - 4 * density
        val span = (sleep.end - sleep.start).coerceAtLeast(1).toFloat()
        fun x(t: Long) = (t - sleep.start) / span * width
        for (stage in sleep.stages) {
            val (ratio, alpha) = when (stage.kind) {
                SleepStage.Kind.DEEP -> 1f to 255
                SleepStage.Kind.REM -> 0.75f to 230
                SleepStage.Kind.LIGHT -> 0.5f to 150
                SleepStage.Kind.SLEEPING -> 0.5f to 150
                SleepStage.Kind.AWAKE -> 0.2f to 80
            }
            paint.alpha = alpha
            canvas.drawRoundRect(RectF(x(stage.start), base - base * ratio, x(stage.end).coerceAtLeast(x(stage.start) + density), base), density, density, paint)
        }
        // Une heure pleine sur deux, sous l'échelle.
        paint.alpha = 190
        paint.textSize = label
        paint.textAlign = Paint.Align.CENTER
        var hour = Instant.ofEpochMilli(sleep.start).atZone(zone).withMinute(0).withSecond(0).withNano(0).plusHours(1)
        while (hour.toInstant().toEpochMilli() < sleep.end) {
            if (hour.hour % 2 == 1) {
                val px = x(hour.toInstant().toEpochMilli()).coerceIn(10 * density, width - 10 * density)
                canvas.drawText("${hour.hour}h", px, height - 1.5f * density, paint)
            }
            hour = hour.plusHours(1)
        }
        return bitmap
    }

    /** Sept barres, le jour dessous (initiale), le total au-dessus ; aujourd'hui en plein. */
    private fun week(days: List<HealthDay>, width: Int, height: Int, density: Float): Bitmap {
        val bitmap = Bitmap.createBitmap(width.coerceAtLeast(1), height.coerceAtLeast(1), Bitmap.Config.ALPHA_8)
        if (days.isEmpty()) return bitmap
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }
        val label = 8.5f * density
        val top = label + 4 * density
        val base = height - label - 4 * density
        val max = days.maxOf { it.steps }.coerceAtLeast(1)
        val slot = width / days.size.toFloat()
        paint.textSize = label
        days.forEachIndexed { i, day ->
            val cx = slot * i + slot / 2
            val barTop = base - (base - top) * day.steps / max
            val today = i == days.size - 1
            paint.alpha = if (today) 255 else 130
            canvas.drawRoundRect(RectF(cx - slot * 0.3f, barTop.coerceAtMost(base - density), cx + slot * 0.3f, base), 2 * density, 2 * density, paint)
            paint.alpha = if (today) 255 else 190
            canvas.drawText(thousands(day.steps.toDouble()), cx, barTop - 3 * density, paint)
            val letter = day.date.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault())
            canvas.drawText(letter, cx, height - 1.5f * density, paint)
        }
        return bitmap
    }

    private fun sample(): HealthSnapshot {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val steps = longArrayOf(6200, 8800, 4500, 10400, 7100, 5400, 7842)
        val night = today.atTime(0, 0).atZone(zone).minusMinutes(20).toInstant().toEpochMilli()
        fun m(minutes: Int) = night + minutes * 60_000L
        val plan = listOf(
            40 to SleepStage.Kind.LIGHT, 55 to SleepStage.Kind.DEEP, 45 to SleepStage.Kind.LIGHT, 25 to SleepStage.Kind.REM,
            40 to SleepStage.Kind.DEEP, 60 to SleepStage.Kind.LIGHT, 35 to SleepStage.Kind.REM, 8 to SleepStage.Kind.AWAKE,
            55 to SleepStage.Kind.LIGHT, 40 to SleepStage.Kind.REM, 29 to SleepStage.Kind.LIGHT,
        )
        var at = 0
        val stages = plan.map { (length, kind) -> SleepStage(m(at), m(at + length), kind).also { at += length } }
        return HealthSnapshot(
            days = steps.mapIndexed { i, s -> HealthDay(today.minusDays(6L - i), s) },
            sleep = Sleep(m(0), m(at), stages),
            updatedAt = System.currentTimeMillis(),
        )
    }

    private companion object {
        fun goal(context: Context) = settings(context).getString("health.goal", null)?.toIntOrNull() ?: 10_000
    }
}
