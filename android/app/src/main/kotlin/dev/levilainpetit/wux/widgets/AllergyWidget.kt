package dev.levilainpetit.wux.widgets

import android.content.Context
import android.content.Intent
import android.util.SizeF
import android.view.View
import android.widget.RemoteViews
import dev.levilainpetit.wux.MainActivity
import dev.levilainpetit.wux.R
import dev.levilainpetit.wux.weather.Air
import dev.levilainpetit.wux.weather.Pollen
import dev.levilainpetit.wux.weather.PollenGroup
import dev.levilainpetit.wux.weather.Weather
import dev.levilainpetit.wux.weather.WeatherRefresh
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Allergies : les pollens regroupés comme dans l'application Météo de Google
 * (herbe, arbres, herbacées), sur trois jours, au lieu de la météo, et
 * l'indice européen de qualité de l'air (Open-Meteo, pollens en Europe
 * seulement). Chaque niveau est le maximum de la journée.
 */
class AllergyWidget : NeonWidget() {

    override fun build(context: Context, size: SizeF, sample: Boolean): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_allergy)
        views.setOnClickPendingIntent(R.id.allergy_root, activity(context, Intent(context, MainActivity::class.java), 63))
        val air = if (sample) SampleData.air() else Weather.air(context)
        views.removeAllViews(R.id.system_row1)
        views.removeAllViews(R.id.system_row2)
        if (air == null) {
            views.setTextViewText(R.id.allergy_summary, "--")
            views.setTextViewText(
                R.id.allergy_detail,
                context.getString(if (Weather.place(context) == null) R.string.weather_pick_place else R.string.weather_loading),
            )
            return views
        }
        val levels = context.resources.getStringArray(R.array.pollen_levels)
        val today = air.days.firstOrNull()
        val highest = today?.highest
        views.setTextViewText(
            R.id.allergy_summary,
            if (today == null || today.values.isEmpty()) {
                context.getString(R.string.pollen_unavailable)
            } else {
                context.getString(R.string.pollen_highest, levels[today.overall ?: 0])
            },
        )
        views.setTextViewText(
            R.id.allergy_detail,
            listOfNotNull(
                highest?.let { context.getString(R.string.pollen_most_present, context.getString(it.label)) },
                aqi(context, air).ifBlank { null },
            ).joinToString(" · "),
        )
        val showTiles = size.height >= 100f && today != null
        views.setViewVisibility(R.id.system_row1, if (showTiles) View.VISIBLE else View.GONE)
        views.setViewVisibility(R.id.system_row2, if (showTiles) View.VISIBLE else View.GONE)
        if (!showTiles || today == null) return views

        // Rangée 1 : les trois familles, aujourd'hui.
        val groups = PollenGroup.entries.map { group ->
            val level = today.level(group)
            val top = Pollen.entries.filter { it.group == group }
                .maxByOrNull { today.values[it] ?: -1.0 }
                ?.takeIf { (today.level(it) ?: 0) > 0 }
            SystemTile(
                label = context.getString(group.label),
                value = level?.let { levels[it] } ?: "—",
                detail = top?.let { context.getString(it.label) }.orEmpty(),
                progress = level?.times(25),
                action = Intent(),
            )
        }
        // Rangée 2 : aujourd'hui et les deux jours suivants.
        val dayFormat = DateTimeFormatter.ofPattern("EEE d", Locale.getDefault())
        val days = air.days.take(3).mapIndexed { i, day ->
            SystemTile(
                label = if (i == 0) context.getString(R.string.agenda_today) else dayFormat.format(day.date),
                value = day.overall?.let { levels[it] } ?: "—",
                detail = day.highest?.let { context.getString(it.label) }.orEmpty(),
                progress = day.overall?.times(25),
                action = Intent(),
            )
        }
        listOf(R.id.system_row1 to groups, R.id.system_row2 to days).forEach { (id, row) ->
            row.forEachIndexed { i, tile ->
                if (i > 0) views.addView(id, RemoteViews(context.packageName, R.layout.system_divider))
                views.addView(id, tileView(context, tile))
            }
        }
        return views
    }

    override fun onSystemUpdate(context: Context, goAsync: () -> PendingResult) =
        WeatherRefresh.refreshIfStale(context, goAsync)

    override fun onRendered(context: Context) {
        WeatherRefresh.schedule(context)
    }

    private fun tileView(context: Context, tile: SystemTile) =
        RemoteViews(context.packageName, R.layout.system_tile).apply {
            setTextViewText(R.id.system_tile_label, tile.label)
            setTextViewText(R.id.system_tile_value, tile.value)
            setTextViewText(R.id.system_tile_detail, tile.detail)
            setViewVisibility(R.id.system_tile_gauge, if (tile.progress == null) View.INVISIBLE else View.VISIBLE)
            setProgressBar(R.id.system_tile_gauge, 100, tile.progress ?: 0, false)
            setContentDescription(R.id.system_tile, "${tile.label} ${tile.value}")
        }

    private fun aqi(context: Context, air: Air): String {
        val aqi = air.aqi ?: return ""
        val quality = context.resources.getStringArray(R.array.aqi_levels)[
            when {
                aqi <= 20 -> 0
                aqi <= 40 -> 1
                aqi <= 60 -> 2
                aqi <= 80 -> 3
                aqi <= 100 -> 4
                else -> 5
            },
        ]
        return context.getString(R.string.aqi, quality, aqi)
    }
}
