package dev.levilainpetit.wux.widgets

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.text.format.DateFormat
import android.util.SizeF
import android.view.View
import android.widget.RemoteViews
import dev.levilainpetit.wux.MainActivity
import dev.levilainpetit.wux.R
import dev.levilainpetit.wux.sky.Body
import dev.levilainpetit.wux.sky.Sighting
import dev.levilainpetit.wux.sky.Sky
import dev.levilainpetit.wux.sky.SkyChart
import dev.levilainpetit.wux.weather.Weather
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Ciel de ce soir : depuis le lieu de Météo, la Lune et les planètes visibles
 * de la tombée de la nuit à 1 h du matin (ou jusqu'à l'aube, après minuit),
 * où les chercher et quand. Tout est calculé sur le téléphone (sky/Sky.kt).
 */
class SkyWidget : NeonWidget() {

    override val refreshActions = DATE_ACTIONS

    override fun build(context: Context, size: SizeF, sample: Boolean): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_sky)
        views.setOnClickPendingIntent(R.id.sky_root, activity(context, Intent(context, MainActivity::class.java), 65))
        val place = if (sample) SampleData.place else Weather.place(context)
        if (place == null) {
            views.setTextViewText(R.id.sky_sunset, "")
            views.setTextViewText(R.id.sky_empty, context.getString(R.string.weather_pick_place))
            views.setViewVisibility(R.id.sky_empty, View.VISIBLE)
            views.setViewVisibility(R.id.sky_chart, View.GONE)
            ROWS.forEach { views.setViewVisibility(it[0], View.GONE) }
            return views
        }
        val now = if (sample) ZonedDateTime.of(2026, 9, 30, 18, 0, 0, 0, ZoneId.of("Europe/Paris")) else ZonedDateTime.now()
        val night = Sky.night(now, place.latitude, place.longitude)
        val format = DateTimeFormatter.ofPattern(if (DateFormat.is24HourFormat(context)) "HH:mm" else "h:mm a", Locale.getDefault())
        views.setTextViewText(R.id.sky_title, context.getString(if (night.from.hour < 5) R.string.sky_title_late else R.string.sky_title))
        views.setTextViewText(R.id.sky_sunset, night.sunset?.let { context.getString(R.string.sky_sunset, format.format(it)) }.orEmpty())

        val sightings = night.sightings.take(ROWS.size)
        ROWS.forEachIndexed { i, (rowId, nameId, whereId, timeId) ->
            val s = sightings.getOrNull(i)
            views.setViewVisibility(rowId, if (s == null) View.GONE else View.VISIBLE)
            if (s == null) return@forEachIndexed
            views.setTextViewText(nameId, context.getString(NAMES.getValue(s.body)))
            views.setTextViewText(whereId, where(context, s))
            views.setTextViewText(
                timeId,
                when {
                    !s.atStart -> format.format(s.from)
                    s.atEnd -> context.getString(R.string.sky_all_night)
                    else -> context.getString(R.string.sky_until, format.format(s.until))
                },
            )
        }
        views.setViewVisibility(R.id.sky_empty, if (sightings.isEmpty()) View.VISIBLE else View.GONE)
        views.setTextViewText(R.id.sky_empty, context.getString(R.string.sky_nothing))

        val showChart = size.height >= 150f && sightings.isNotEmpty()
        views.setViewVisibility(R.id.sky_chart, if (showChart) View.VISIBLE else View.GONE)
        if (showChart) {
            val density = context.resources.displayMetrics.density
            val hours = DateTimeFormatter.ofPattern(
                when {
                    !DateFormat.is24HourFormat(context) -> "h a"
                    Locale.getDefault().language == "fr" -> "H'h'"
                    else -> "HH"
                },
                Locale.getDefault(),
            )
            views.setImageViewBitmap(
                R.id.sky_chart,
                SkyChart.draw(
                    night, place.latitude, place.longitude, sightings,
                    name = { context.getString(NAMES.getValue(it)).uppercase(Locale.getDefault()) },
                    hour = { hours.format(it) },
                    now = now,
                    width = ((size.width - 48) * density).roundToInt(),
                    height = (64 * density).roundToInt(),
                    density = density,
                ),
            )
        }
        return views
    }

    override fun onRendered(context: Context) {
        val next = System.currentTimeMillis() + 30 * 60_000L
        context.getSystemService(AlarmManager::class.java)?.set(AlarmManager.RTC, next, refreshIntent(context, 2))
    }

    /** « Sud-est, haute » s'il est déjà là ; « Se lève à l'est » sinon. */
    private fun where(context: Context, s: Sighting): String {
        val sector = sector(s.start.azimuth)
        return if (s.atStart) {
            context.getString(
                R.string.sky_where,
                context.resources.getStringArray(R.array.sky_directions)[sector],
                context.getString(if (s.highest >= 35) R.string.sky_high else R.string.sky_low),
            )
        } else {
            context.getString(R.string.sky_rises, context.resources.getStringArray(R.array.sky_rising)[sector])
        }
    }

    private companion object {
        /** Chaque rangée : la rangée, le nom, où regarder, quand. */
        val ROWS = listOf(
            intArrayOf(R.id.sky_row_0, R.id.sky_name_0, R.id.sky_where_0, R.id.sky_time_0),
            intArrayOf(R.id.sky_row_1, R.id.sky_name_1, R.id.sky_where_1, R.id.sky_time_1),
            intArrayOf(R.id.sky_row_2, R.id.sky_name_2, R.id.sky_where_2, R.id.sky_time_2),
        )

        val NAMES = mapOf(
            Body.MOON to R.string.sky_moon,
            Body.VENUS to R.string.sky_venus,
            Body.JUPITER to R.string.sky_jupiter,
            Body.MARS to R.string.sky_mars,
            Body.SATURN to R.string.sky_saturn,
            Body.MERCURY to R.string.sky_mercury,
        )

        fun sector(azimuth: Double) = (((azimuth % 360 + 360) % 360 + 22.5) / 45).toInt() % 8
    }
}
