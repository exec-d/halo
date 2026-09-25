package dev.levilainpetit.wux.tiles

import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import dev.levilainpetit.wux.MainActivity
import dev.levilainpetit.wux.R
import dev.levilainpetit.wux.system.BatteryHistory
import dev.levilainpetit.wux.wallpaper.WallpaperPreview
import dev.levilainpetit.wux.wallpaper.WallpaperSettings
import dev.levilainpetit.wux.weather.Weather
import dev.levilainpetit.wux.weather.WeatherRefresh
import dev.levilainpetit.wux.widgets.SystemGraphics
import kotlin.concurrent.thread
import kotlin.math.roundToInt

/** Ce qui est commun aux tuiles Halo : ouvrir l'application sur un écran. */
abstract class HaloTile : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        qsTile?.let {
            update(it)
            it.updateTile()
        }
    }

    abstract fun update(tile: Tile)

    /** Ouvre Halo à l'adresse halo://[target] et replie le volet. */
    protected fun open(target: String) {
        val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("halo://$target"), this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startActivityAndCollapse(PendingIntent.getActivity(this, target.hashCode(), intent, PendingIntent.FLAG_IMMUTABLE))
        } else {
            @Suppress("DEPRECATION", "StartActivityAndCollapseDeprecated")
            startActivityAndCollapse(intent)
        }
    }

    protected fun Tile.subtitleCompat(text: CharSequence?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) subtitle = text
    }
}

/**
 * Météo express : la température et le temps qu'il fait au lieu choisi.
 * Toucher retélécharge les prévisions ; un appui long ouvre les réglages.
 */
class WeatherTile : HaloTile() {

    override fun update(tile: Tile) {
        val forecast = Weather.forecast(this)
        if (forecast == null) {
            tile.state = Tile.STATE_INACTIVE
            tile.label = getString(R.string.tile_weather)
            tile.subtitleCompat(getString(R.string.tile_weather_pick))
            tile.icon = Icon.createWithResource(this, R.drawable.icon_partly_cloudy)
            return
        }
        tile.state = Tile.STATE_ACTIVE
        tile.label = "${forecast.temperature.roundToInt()}°"
        tile.subtitleCompat(getString(Weather.label(forecast.code)))
        tile.icon = Icon.createWithResource(this, Weather.icon(forecast.code, forecast.isDay))
    }

    override fun onClick() {
        super.onClick()
        if (Weather.place(this) == null) {
            open("weather")
            return
        }
        val app = applicationContext
        thread(name = "halo-tile-weather") {
            if (Weather.refresh(app)) WeatherRefresh.redraw(app)
            qsTile?.let {
                update(it)
                it.updateTile()
            }
        }
    }
}

/**
 * Fond Halo : l'intensité en un toucher (Discret → Normal → Vif). Active
 * quand un fond Halo (Circuit, Horizon, Ciel) est appliqué ; sinon, toucher
 * ouvre l'écran des fonds.
 */
class WallpaperTile : HaloTile() {

    override fun update(tile: Tile) {
        val active = WallpaperPreview.activeKind(this) != null
        tile.state = if (active) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = getString(R.string.tile_wallpaper)
        tile.subtitleCompat(
            if (active) getString(intensityLabel(WallpaperSettings.intensityName(this))) else getString(R.string.tile_wallpaper_off),
        )
        tile.icon = Icon.createWithResource(this, R.drawable.tile_wallpaper)
    }

    override fun onClick() {
        super.onClick()
        if (WallpaperPreview.activeKind(this) == null) {
            open("wallpaper")
            return
        }
        val next = when (WallpaperSettings.intensityName(this)) {
            "discreet" -> "normal"
            "normal" -> "vivid"
            else -> "discreet"
        }
        // Le fond relit l'intensité dès qu'il redevient visible, volet replié.
        WallpaperSettings.setIntensityName(this, next)
        qsTile?.let {
            update(it)
            it.updateTile()
        }
    }

    private fun intensityLabel(name: String) = when (name) {
        "vivid" -> R.string.tile_intensity_vivid
        "normal" -> R.string.tile_intensity_normal
        else -> R.string.tile_intensity_discreet
    }
}

/** Batterie : le niveau et ce qu'il annonce ; toucher ouvre l'utilisation de la batterie. */
class BatteryTile : HaloTile() {

    override fun update(tile: Tile) {
        val info = BatteryHistory.read(this)
        tile.state = if (info.charging) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = "${info.level} %"
        val minutes = info.minutesLeft
        tile.subtitleCompat(
            when {
                info.charging && info.level >= 100 -> getString(R.string.battery_full)
                info.charging && minutes != null -> getString(R.string.battery_full_in, SystemGraphics.formatDuration(minutes * 60_000L))
                info.charging -> getString(R.string.battery_estimating_charge)
                minutes != null -> getString(R.string.battery_left, SystemGraphics.formatDuration(minutes * 60_000L))
                else -> getString(R.string.tile_battery)
            },
        )
        tile.icon = Icon.createWithResource(this, R.drawable.tile_battery)
    }

    override fun onClick() {
        super.onClick()
        val intent = Intent(Intent.ACTION_POWER_USAGE_SUMMARY).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startActivityAndCollapse(PendingIntent.getActivity(this, 90, intent, PendingIntent.FLAG_IMMUTABLE))
        } else {
            @Suppress("DEPRECATION", "StartActivityAndCollapseDeprecated")
            startActivityAndCollapse(intent)
        }
    }
}
