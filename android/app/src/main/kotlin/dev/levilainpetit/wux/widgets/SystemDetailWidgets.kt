package dev.levilainpetit.wux.widgets

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.SizeF
import android.view.View
import android.widget.RemoteViews
import dev.levilainpetit.wux.MainActivity
import dev.levilainpetit.wux.R
import dev.levilainpetit.wux.system.BatteryHistory
import dev.levilainpetit.wux.system.BatteryInfo
import dev.levilainpetit.wux.system.BatterySample
import dev.levilainpetit.wux.system.BluetoothDevices
import dev.levilainpetit.wux.system.DeviceInfo
import dev.levilainpetit.wux.system.MobileData
import dev.levilainpetit.wux.system.ScreenTime
import dev.levilainpetit.wux.system.SystemRefresh
import java.util.Locale
import kotlin.math.roundToInt

/** Ouvre l'application, pour accorder une autorisation qui manque. */
private fun openApp(context: Context, code: Int) =
    NeonWidget.activity(context, Intent(context, MainActivity::class.java), code)

private fun density(context: Context) = context.resources.displayMetrics.density
private fun fontScale(context: Context) = context.resources.configuration.fontScale

// ——— Batterie détaillée ———

/**
 * Batterie détaillée : le niveau et ce qu'il annonce (« Pleine dans 42 min »,
 * « Encore environ 9 h 30 »), les relevés, et la courbe des 24 dernières
 * heures prolongée de la suite prévue.
 */
class BatteryWidget : NeonWidget() {

    override val refreshActions = setOf(Intent.ACTION_POWER_CONNECTED, Intent.ACTION_POWER_DISCONNECTED)

    override fun build(context: Context, size: SizeF, sample: Boolean): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_battery)
        views.setOnClickPendingIntent(R.id.battery_root, activity(context, Intent(Intent.ACTION_POWER_USAGE_SUMMARY), 71))
        val info = if (sample) sample() else BatteryHistory.read(context)

        views.setTextViewText(R.id.battery_level, "${info.level}")
        val minutes = info.minutesLeft
        views.setTextViewText(
            R.id.battery_estimate,
            when {
                info.charging && info.level >= 100 -> context.getString(R.string.battery_full)
                info.charging && minutes != null -> context.getString(R.string.battery_full_in, SystemGraphics.formatDuration(minutes * 60_000L))
                info.charging -> context.getString(R.string.battery_estimating_charge)
                minutes != null -> context.getString(R.string.battery_left, SystemGraphics.formatDuration(minutes * 60_000L))
                else -> context.getString(R.string.battery_estimating)
            },
        )
        views.setTextViewText(
            R.id.battery_temperature,
            info.temperatureTenths?.let { String.format(Locale.FRANCE, "%.1f °C", it / 10.0) } ?: "—",
        )
        views.setTextViewText(
            R.id.battery_voltage,
            info.millivolts?.let { String.format(Locale.FRANCE, "%.2f V", it / 1000.0) } ?: "—",
        )
        views.setTextViewText(R.id.battery_cycles, info.cycles?.toString() ?: "—")
        val health = context.resources.getStringArray(R.array.battery_health_names)
        views.setTextViewText(R.id.battery_health, health.getOrElse(info.health) { "—" })

        // Assez haut : la courbe ; sinon, seulement les chiffres.
        val scale = fontScale(context)
        val chartHeight = size.height - 16 - 92 * scale
        val showChart = chartHeight >= 36
        views.setViewVisibility(R.id.battery_chart, if (showChart) View.VISIBLE else View.GONE)
        if (showChart) {
            val d = density(context)
            views.setImageViewBitmap(
                R.id.battery_chart,
                SystemGraphics.batteryChart(
                    info.history, info.level, info.charging, info.minutesLeft, System.currentTimeMillis(),
                    context.getString(R.string.battery_chart_now),
                    ((size.width - 24) * d).roundToInt(), (chartHeight * d).roundToInt(), d,
                ),
            )
        }
        return views
    }

    override fun onRendered(context: Context) = SystemRefresh.schedule(context)

    private fun sample(): BatteryInfo {
        val now = System.currentTimeMillis()
        val hour = 3_600_000L
        // Une nuit en charge, puis une journée d'utilisation.
        val levels = listOf(
            -24.0 to 64, -22.0 to 55, -20.0 to 47, -18.0 to 40, -16.5 to 34, -15.9 to 34,
            -14.5 to 62, -13.2 to 88, -12.0 to 100, -9.0 to 100, -7.0 to 94, -5.0 to 88, -3.0 to 85, -1.5 to 83, 0.0 to 82,
        )
        val history = levels.mapIndexed { i, (h, level) ->
            BatterySample(now + (h * hour).toLong(), level, i in 5..8)
        }
        return BatteryInfo(82, false, 312, 4_210, 412, 2, 575, history)
    }
}

// ——— Appareil ———

/**
 * Appareil, façon console : la durée depuis le démarrage (un chronomètre, qui
 * défile seul sans réveiller l'application), le système et le matériel.
 */
class DeviceWidget : NeonWidget() {

    override val refreshActions = setOf(Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED) + DATE_ACTIONS

    override fun build(context: Context, size: SizeF, sample: Boolean): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_device)
        views.setOnClickPendingIntent(R.id.device_root, activity(context, Intent(Settings.ACTION_DEVICE_INFO_SETTINGS), 72))
        val info = if (sample) DeviceInfo.sample() else DeviceInfo.read(context)
        fun prompt(command: String) = context.getString(R.string.device_prompt, "halo", info.host, command)

        views.setTextViewText(R.id.device_prompt_uptime, prompt("uptime"))
        // Le chronomètre compte depuis sa base, en temps écoulé depuis le
        // démarrage : 0, c'est le démarrage, y compris après un redémarrage.
        val base = if (sample) android.os.SystemClock.elapsedRealtime() - info.uptimeMillis else 0L
        views.setChronometer(R.id.device_uptime, base, null, !sample)
        val hours = info.uptimeMillis / 3_600_000L
        views.setTextViewText(
            R.id.device_uptime_days,
            if (hours >= 24) context.getString(R.string.device_days, (hours / 24).toInt(), (hours % 24).toInt())
            else context.getString(R.string.device_hours),
        )
        views.setTextViewText(R.id.device_prompt_os, prompt("os"))
        views.setTextViewText(
            R.id.device_os,
            listOfNotNull(info.android, info.patch?.let { context.getString(R.string.device_patch, it) }).joinToString(" · "),
        )
        views.setTextViewText(R.id.device_prompt_hw, prompt("hw"))
        views.setTextViewText(
            R.id.device_hw,
            "${info.model} · ${info.chip} · ${context.getString(R.string.device_memory, info.memoryGb)}",
        )
        views.setTextViewText(R.id.device_cursor, prompt("█"))

        // Peu de hauteur : les résultats seuls, sans les commandes.
        val compact = size.height < 150 * fontScale(context)
        val prompts = if (compact) View.GONE else View.VISIBLE
        views.setViewVisibility(R.id.device_prompt_os, prompts)
        views.setViewVisibility(R.id.device_prompt_hw, prompts)
        views.setViewVisibility(R.id.device_cursor, prompts)
        return views
    }

    override fun onRendered(context: Context) = refreshAfterMidnight(context)
}

// ——— Écouteurs et montre ———

/** La batterie des appareils Bluetooth connectés : silhouette, cellules, niveau. */
class BluetoothDevicesWidget : NeonWidget() {

    override val refreshActions = setOf(
        BluetoothDevice.ACTION_ACL_CONNECTED,
        BluetoothDevice.ACTION_ACL_DISCONNECTED,
        "android.bluetooth.device.action.BATTERY_LEVEL_CHANGED",
    )

    override fun build(context: Context, size: SizeF, sample: Boolean): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_bt_devices)
        views.removeAllViews(R.id.bt_list)
        val devices = if (sample) BluetoothDevices.sample() else BluetoothDevices.connected(context)
        val settings = activity(context, Intent(Settings.ACTION_BLUETOOTH_SETTINGS), 73)
        val message = when {
            devices == null -> context.getString(R.string.bt_permission)
            devices.isEmpty() -> context.getString(R.string.bt_none)
            else -> null
        }
        views.setViewVisibility(R.id.bt_empty, if (message != null) View.VISIBLE else View.GONE)
        views.setViewVisibility(R.id.bt_list, if (message != null) View.GONE else View.VISIBLE)
        if (message != null) {
            views.setTextViewText(R.id.bt_empty, message)
            views.setOnClickPendingIntent(R.id.bt_root, if (devices == null) openApp(context, 74) else settings)
            return views
        }
        views.setOnClickPendingIntent(R.id.bt_root, settings)
        val d = density(context)
        // Environ 105 dp par appareil.
        val room = (size.width / 105f).toInt().coerceIn(1, 4)
        devices!!.take(room).forEach { device ->
            val item = RemoteViews(context.packageName, R.layout.bt_device)
            item.setImageViewBitmap(R.id.bt_glyph, SystemGraphics.deviceGlyph(device.kind, (28 * d).roundToInt(), d))
            item.setImageViewBitmap(R.id.bt_cells, SystemGraphics.cells(device.level, (11 * d).roundToInt(), (36 * d).roundToInt(), d))
            item.setTextViewText(R.id.bt_level, device.level?.let { "$it %" } ?: context.getString(R.string.bt_unknown_level))
            item.setTextViewText(R.id.bt_name, device.name)
            views.addView(R.id.bt_list, item)
        }
        return views
    }

    override fun onRendered(context: Context) = SystemRefresh.schedule(context)
}

// ——— Temps d'écran ———

/**
 * Temps d'écran : un cadran de 24 heures où chaque période écran allumé est
 * un arc, le total et les déverrouillages au centre, et les trois applis les
 * plus utilisées, leur icône en silhouette.
 */
class ScreenTimeWidget : NeonWidget() {

    override fun build(context: Context, size: SizeF, sample: Boolean): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_screen_time)
        val now = System.currentTimeMillis()
        val data = if (sample) ScreenTime.sample(now) else ScreenTime.today(context, now)
        val d = density(context)
        val dialSize = ((size.height - 16).coerceIn(60f, 180f) * d).roundToInt()
        views.removeAllViews(R.id.st_apps)

        if (data == null) {
            views.setImageViewBitmap(R.id.st_dial, SystemGraphics.dial(emptyList(), now, now, dialSize, d))
            views.setTextViewText(R.id.st_total, "—")
            views.setTextViewText(R.id.st_unlocks, "")
            views.setViewVisibility(R.id.st_empty, View.VISIBLE)
            views.setTextViewText(R.id.st_empty, context.getString(R.string.usage_permission))
            views.setOnClickPendingIntent(R.id.st_root, openApp(context, 75))
            return views
        }
        views.setOnClickPendingIntent(R.id.st_root, wellbeing(context))
        views.setImageViewBitmap(R.id.st_dial, SystemGraphics.dial(data.sessions, data.dayStart, now, dialSize, d))
        views.setTextViewText(R.id.st_total, SystemGraphics.formatDuration(data.totalMillis))
        views.setTextViewText(R.id.st_unlocks, context.getString(R.string.screen_time_unlocks, data.unlocks))
        views.setViewVisibility(R.id.st_empty, if (data.apps.isEmpty()) View.VISIBLE else View.GONE)
        views.setTextViewText(R.id.st_empty, context.getString(R.string.screen_time_no_apps))

        // Autant d'applis que la hauteur le permet (≈ 28 dp chacune).
        val room = ((size.height - 24 * fontScale(context)) / (28 * fontScale(context))).toInt().coerceIn(1, 3)
        val top = data.apps.firstOrNull()?.millis?.coerceAtLeast(1L) ?: 1L
        data.apps.take(room).forEachIndexed { i, app ->
            val row = RemoteViews(context.packageName, R.layout.st_app)
            val icon = SystemGraphics.silhouette(app.icon, (18 * d).roundToInt())
            if (icon != null) {
                row.setImageViewBitmap(R.id.st_app_icon, icon)
            } else {
                row.setImageViewBitmap(R.id.st_app_icon, SystemGraphics.deviceGlyph(SAMPLE_GLYPHS[i % SAMPLE_GLYPHS.size], (18 * d).roundToInt(), d))
            }
            row.setTextViewText(R.id.st_app_name, app.label)
            row.setTextViewText(R.id.st_app_time, SystemGraphics.formatDuration(app.millis))
            row.setProgressBar(R.id.st_app_share, 100, (app.millis * 100 / top).toInt(), false)
            views.addView(R.id.st_apps, row)
        }
        return views
    }

    override fun onRendered(context: Context) = SystemRefresh.schedule(context)

    /** Bien-être numérique s'il est installé, sinon les réglages. */
    private fun wellbeing(context: Context) =
        activity(
            context,
            context.packageManager.getLaunchIntentForPackage("com.google.android.apps.wellbeing")
                ?: Intent(Settings.ACTION_SETTINGS),
            76,
        )

    private companion object {
        /** Pour l'aperçu, qui n'a pas de vraies icônes. */
        val SAMPLE_GLYPHS = listOf(
            dev.levilainpetit.wux.system.ConnectedDevice.Kind.OTHER,
            dev.levilainpetit.wux.system.ConnectedDevice.Kind.INPUT,
            dev.levilainpetit.wux.system.ConnectedDevice.Kind.CAR,
        )
    }
}

// ——— Données mobiles ———

/**
 * Données mobiles : le consommé de la période face au forfait, la
 * consommation du jour, la projection en fin de période, et la courbe du
 * cumul avec le rythme qui mène pile au forfait.
 *
 * Réglages écrits par Flutter : `mobile_data.quota` (Go, 0 pour aucun) et
 * `mobile_data.cycleDay` (jour de reprise, 1 à 28).
 */
class MobileDataWidget : NeonWidget() {

    override fun build(context: Context, size: SizeF, sample: Boolean): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_mobile_data)
        val prefs = settings(context)
        val quota = prefs.getString("$ID.quota", null)?.replace(',', '.')?.toDoubleOrNull() ?: 0.0
        val cycleDay = prefs.getString("$ID.cycleDay", null)?.toIntOrNull() ?: 1
        val data = if (sample) MobileData.sample() else MobileData.current(context, cycleDay, quota)

        val missing = data == null
        views.setViewVisibility(R.id.md_empty, if (missing) View.VISIBLE else View.GONE)
        views.setViewVisibility(R.id.md_chart, if (missing) View.GONE else View.VISIBLE)
        views.setViewVisibility(R.id.md_readouts, if (missing) View.GONE else View.VISIBLE)
        if (data == null) {
            views.setTextViewText(R.id.md_used, "—")
            views.setTextViewText(R.id.md_quota, "")
            views.setTextViewText(R.id.md_detail, "")
            views.setTextViewText(R.id.md_empty, context.getString(R.string.usage_permission))
            views.setOnClickPendingIntent(R.id.md_root, openApp(context, 77))
            return views
        }
        views.setOnClickPendingIntent(R.id.md_root, activity(context, Intent(Settings.ACTION_DATA_USAGE_SETTINGS), 78))
        views.setTextViewText(R.id.md_used, SystemGraphics.formatBytes(data.used))
        views.setTextViewText(R.id.md_quota, data.quota?.let { context.getString(R.string.mobile_data_quota, SystemGraphics.formatBytes(it)) } ?: "")
        views.setTextViewText(R.id.md_detail, context.getString(R.string.mobile_data_detail, SystemGraphics.formatBytes(data.today), data.daysLeft))
        views.setTextViewText(R.id.md_projection, "≈ ${SystemGraphics.formatBytes(data.projected)}")
        views.setTextViewText(R.id.md_average, SystemGraphics.formatBytes(if (data.days.isEmpty()) 0L else data.used / data.days.size))

        val chartHeight = size.height - 16 - 72 * fontScale(context)
        val showChart = chartHeight >= 32
        views.setViewVisibility(R.id.md_chart, if (showChart) View.VISIBLE else View.GONE)
        if (showChart) {
            val d = density(context)
            views.setImageViewBitmap(
                R.id.md_chart,
                SystemGraphics.dataChart(data, ((size.width - 24) * d).roundToInt(), (chartHeight * d).roundToInt(), d),
            )
        }
        return views
    }

    override fun onRendered(context: Context) = SystemRefresh.schedule(context)

    companion object {
        const val ID = "mobile_data"
    }
}
