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
import dev.levilainpetit.wux.weather.Weather
import dev.levilainpetit.wux.weather.WeatherRefresh

/**
 * Allergies : niveau de six pollens au lieu de la météo, et indice européen
 * de qualité de l'air (Open-Meteo, Europe seulement pour les pollens).
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
        val highest = air.pollens.maxByOrNull { (pollen, value) -> pollen.level(value) }
        views.setTextViewText(
            R.id.allergy_summary,
            if (air.pollens.isEmpty()) {
                context.getString(R.string.pollen_unavailable)
            } else {
                context.getString(R.string.pollen_highest, levels[highest!!.key.level(highest.value)])
            },
        )
        views.setTextViewText(R.id.allergy_detail, aqi(context, air))
        val tiles = Pollen.entries.map { pollen ->
            val value = air.pollens[pollen]
            val level = value?.let { pollen.level(it) }
            SystemTile(
                label = context.getString(pollen.label),
                value = level?.let { levels[it] } ?: "—",
                detail = value?.let { context.getString(R.string.pollen_grains, it.toInt()) }.orEmpty(),
                progress = level?.times(25),
                action = Intent(),
            )
        }
        val showTiles = size.height >= 100f
        views.setViewVisibility(R.id.system_row1, if (showTiles) View.VISIBLE else View.GONE)
        views.setViewVisibility(R.id.system_row2, if (showTiles) View.VISIBLE else View.GONE)
        if (showTiles) {
            tiles.chunked(3).forEachIndexed { r, row ->
                val id = if (r == 0) R.id.system_row1 else R.id.system_row2
                row.forEachIndexed { i, tile ->
                    if (i > 0) views.addView(id, RemoteViews(context.packageName, R.layout.system_divider))
                    views.addView(id, tileView(context, tile))
                }
            }
        }
        return views
    }

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
