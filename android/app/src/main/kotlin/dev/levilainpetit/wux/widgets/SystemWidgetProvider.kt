package dev.levilainpetit.wux.widgets

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkRequest
import android.os.Build
import android.provider.Settings
import android.view.View
import android.widget.RemoteViews
import dev.levilainpetit.wux.R

/**
 * Widget système 4x1 : batterie, réseau, stockage.
 *
 * Fraîcheur : le réseau prévient lui-même quand il change (rappel réseau à
 * `PendingIntent`) ; la batterie et le stockage n'ont pas d'annonce qu'un
 * widget puisse écouter, ils sont relus toutes les 5 minutes par une alarme
 * non réveillante, donc seulement quand l'écran est allumé.
 */
class SystemWidgetProvider : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) renderAll(context)
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val views = views(context)
        appWidgetIds.forEach { appWidgetManager.updateAppWidget(it, views) }
        schedule(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        context.getSystemService(AlarmManager::class.java)?.cancel(refresh(context, 0))
        runCatching {
            context.getSystemService(ConnectivityManager::class.java)?.unregisterNetworkCallback(refresh(context, 1))
        }
    }

    private fun renderAll(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(ComponentName(context, SystemWidgetProvider::class.java))
        if (ids.isNotEmpty()) {
            val views = views(context)
            ids.forEach { manager.updateAppWidget(it, views) }
        }
    }

    private fun schedule(context: Context) {
        val interval = 5 * 60_000L
        context.getSystemService(AlarmManager::class.java)?.setInexactRepeating(
            AlarmManager.RTC,
            System.currentTimeMillis() + interval,
            interval,
            refresh(context, 0),
        )
        // Le même PendingIntent remplace l'inscription précédente.
        runCatching {
            context.getSystemService(ConnectivityManager::class.java)
                ?.registerNetworkCallback(NetworkRequest.Builder().build(), refresh(context, 1))
        }
    }

    /** Diffusion vers ce widget ; [code] 1 : modifiable, le système y joint le réseau. */
    private fun refresh(context: Context, code: Int): PendingIntent {
        val mutability = if (code == 1 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_IMMUTABLE
        }
        return PendingIntent.getBroadcast(
            context,
            code,
            Intent(context, SystemWidgetProvider::class.java).setAction(ACTION_REFRESH),
            mutability or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    private fun views(context: Context) = RemoteViews(context.packageName, R.layout.widget_system).apply {
        tile(this, SystemStatus.battery(context), R.id.system_battery, R.id.system_battery_label,
            R.id.system_battery_value, R.id.system_battery_detail, R.id.system_battery_gauge)
        tile(this, SystemStatus.network(context), R.id.system_network, R.id.system_network_label,
            R.id.system_network_value, R.id.system_network_detail, R.id.system_network_gauge)
        tile(this, SystemStatus.storage(context), R.id.system_storage, R.id.system_storage_label,
            R.id.system_storage_value, R.id.system_storage_detail, R.id.system_storage_gauge)

        val network = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY)
        } else {
            Intent(Settings.ACTION_WIRELESS_SETTINGS)
        }
        open(context, this, R.id.system_battery, Intent(Intent.ACTION_POWER_USAGE_SUMMARY), 10)
        open(context, this, R.id.system_network, network, 11)
        open(context, this, R.id.system_storage, Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS), 12)
    }

    private fun tile(views: RemoteViews, tile: SystemTile, root: Int, label: Int, value: Int, detail: Int, gauge: Int) {
        views.setTextViewText(label, tile.label)
        views.setTextViewText(value, tile.value)
        views.setTextViewText(detail, tile.detail)
        views.setViewVisibility(gauge, if (tile.progress == null) View.INVISIBLE else View.VISIBLE)
        views.setProgressBar(gauge, 100, tile.progress ?: 0, false)
        views.setContentDescription(root, "${tile.label} ${tile.value}, ${tile.detail}")
    }

    private fun open(context: Context, views: RemoteViews, id: Int, intent: Intent, code: Int) {
        views.setOnClickPendingIntent(
            id,
            PendingIntent.getActivity(
                context,
                code,
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            ),
        )
    }

    companion object {
        const val ACTION_REFRESH = "dev.levilainpetit.wux.action.SYSTEM_REFRESH"
    }
}
