package dev.levilainpetit.wux.widgets

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.provider.CalendarContract
import android.util.SizeF
import android.view.View
import android.widget.RemoteViews
import dev.levilainpetit.wux.R
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.IsoFields
import kotlin.math.roundToInt

/**
 * Éphéméride : la fête du jour, le numéro de la semaine et du jour, l'année
 * en douze mois qui se remplissent, et le prochain jour férié (France).
 */
class EphemerisWidget : NeonWidget() {

    override val refreshActions = DATE_ACTIONS

    override fun build(context: Context, size: SizeF, sample: Boolean): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_ephemeris)
        views.setOnClickPendingIntent(
            R.id.ephemeris_root,
            activity(context, Intent(Intent.ACTION_VIEW, CalendarContract.CONTENT_URI.buildUpon().appendPath("time").build()), 83),
        )
        val today = LocalDate.now()
        val saint = Saints.of(today)
        views.setTextViewText(
            R.id.ephemeris_saint,
            if (Saints.isFeast(saint)) saint else context.getString(R.string.ephemeris_saint, saint),
        )
        views.setTextViewText(
            R.id.ephemeris_numbers,
            context.getString(R.string.ephemeris_numbers, today.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR), today.dayOfYear, today.lengthOfYear()),
        )
        val (index, date) = nextHoliday(today)
        val holiday = context.resources.getStringArray(R.array.holidays)[index]
        val days = ChronoUnit.DAYS.between(today, date)
        views.setTextViewText(
            R.id.ephemeris_holiday,
            when (days) {
                0L -> context.getString(R.string.ephemeris_holiday_today, holiday)
                1L -> context.getString(R.string.ephemeris_holiday_tomorrow, holiday)
                else -> context.getString(R.string.ephemeris_holiday, holiday, days)
            },
        )

        // L'année ne tient qu'à partir de deux rangées.
        val showYear = size.height >= 100
        views.setViewVisibility(R.id.ephemeris_year, if (showYear) View.VISIBLE else View.GONE)
        if (showYear) {
            val d = context.resources.displayMetrics.density
            views.setImageViewBitmap(R.id.ephemeris_year, year(today, ((size.width - 24) * d).roundToInt(), (22 * d).roundToInt(), d))
        }
        return views
    }

    override fun onRendered(context: Context) = refreshAfterMidnight(context)

    /** Douze barres, une par mois, remplies des jours passés ; aujourd'hui en point. */
    private fun year(today: LocalDate, width: Int, height: Int, density: Float): Bitmap {
        val bitmap = Bitmap.createBitmap(width.coerceAtLeast(1), height.coerceAtLeast(1), Bitmap.Config.ALPHA_8)
        val canvas = Canvas(bitmap)
        val gap = 3 * density
        val bar = (width - gap * 11) / 12f
        val top = height * 0.3f
        val bottom = height * 0.75f
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        for (m in 1..12) {
            val left = (m - 1) * (bar + gap)
            val month = today.withDayOfMonth(1).withMonth(m)
            val filled = when {
                m < today.monthValue -> 1f
                m > today.monthValue -> 0f
                else -> today.dayOfMonth / month.lengthOfMonth().toFloat()
            }
            paint.alpha = 60
            canvas.drawRoundRect(RectF(left, top, left + bar, bottom), 1.5f * density, 1.5f * density, paint)
            if (filled > 0f) {
                paint.alpha = if (m == today.monthValue) 255 else 170
                canvas.drawRoundRect(RectF(left, top, left + bar * filled, bottom), 1.5f * density, 1.5f * density, paint)
            }
            if (m == today.monthValue) {
                paint.alpha = 255
                canvas.drawCircle(left + bar * filled, (top + bottom) / 2, 3 * density, paint)
            }
        }
        return bitmap
    }

    companion object {
        /** Le prochain jour férié en France, aujourd'hui compris. */
        fun nextHoliday(today: LocalDate): Pair<Int, LocalDate> =
            (holidays(today.year) + holidays(today.year + 1))
                .filter { !it.second.isBefore(today) }
                .minBy { it.second }

        /** (index dans `R.array.holidays`, date). */
        private fun holidays(year: Int): List<Pair<Int, LocalDate>> {
            val easter = easter(year)
            return listOf(
                0 to LocalDate.of(year, 1, 1),
                1 to easter.plusDays(1),
                2 to LocalDate.of(year, 5, 1),
                3 to LocalDate.of(year, 5, 8),
                4 to easter.plusDays(39),
                5 to easter.plusDays(50),
                6 to LocalDate.of(year, 7, 14),
                7 to LocalDate.of(year, 8, 15),
                8 to LocalDate.of(year, 11, 1),
                9 to LocalDate.of(year, 11, 11),
                10 to LocalDate.of(year, 12, 25),
            )
        }

        /** Dimanche de Pâques (calendrier grégorien, algorithme de Butcher). */
        fun easter(year: Int): LocalDate {
            val a = year % 19
            val b = year / 100
            val c = year % 100
            val d = b / 4
            val e = b % 4
            val f = (b + 8) / 25
            val g = (b - f + 1) / 3
            val h = (19 * a + b - d - g + 15) % 30
            val i = c / 4
            val k = c % 4
            val l = (32 + 2 * e + 2 * i - h - k) % 7
            val m = (a + 11 * h + 22 * l) / 451
            val month = (h + l - 7 * m + 114) / 31
            val day = (h + l - 7 * m + 114) % 31 + 1
            return LocalDate.of(year, month, day)
        }
    }
}
