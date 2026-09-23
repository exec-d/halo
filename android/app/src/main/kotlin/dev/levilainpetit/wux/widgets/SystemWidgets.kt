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
import android.util.SizeF
import android.view.View
import android.widget.RemoteViews
import dev.levilainpetit.wux.R

/**
 * Widgets système : des rangées de cases ([SystemTile]) séparées de traits.
 *
 * Fraîcheur : le réseau prévient lui-même quand il change (rappel réseau à
 * `PendingIntent`) ; le reste n'a pas d'annonce qu'un widget puisse écouter
 * et est relu toutes les 5 minutes par une alarme non réveillante, donc
 * seulement écran allumé.
 */
abstract class SystemWidgetProvider(
    private val layout: Int,
    /** Code propre à chaque widget, pour que leurs alarmes ne se remplacent pas. */
    private val code: Int,
) : AppWidgetProvider(), PreviewableWidget {

    /** Une liste de cases par rangée (`system_row1`, `system_row2`). */
    abstract fun rows(context: Context): List<List<SystemTile>>

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
        context.getSystemService(AlarmManager::class.java)?.cancel(refresh(context, network = false))
        runCatching {
            context.getSystemService(ConnectivityManager::class.java)
                ?.unregisterNetworkCallback(refresh(context, network = true))
        }
    }

    private fun renderAll(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(ComponentName(context, javaClass))
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
            refresh(context, network = false),
        )
        // Le même PendingIntent remplace l'inscription précédente.
        runCatching {
            context.getSystemService(ConnectivityManager::class.java)
                ?.registerNetworkCallback(NetworkRequest.Builder().build(), refresh(context, network = true))
        }
    }

    /** Diffusion vers ce widget ; pour le rappel réseau, modifiable : le système y joint le réseau. */
    private fun refresh(context: Context, network: Boolean): PendingIntent {
        val mutability = if (network && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_IMMUTABLE
        }
        return PendingIntent.getBroadcast(
            context,
            code * 2 + if (network) 1 else 0,
            Intent(context, javaClass).setAction(ACTION_REFRESH),
            mutability or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    override fun preview(context: Context, size: SizeF) = views(context)

    private fun views(context: Context) = RemoteViews(context.packageName, layout).apply {
        rows(context).forEachIndexed { index, tiles ->
            val row = if (index == 0) R.id.system_row1 else R.id.system_row2
            removeAllViews(row)
            tiles.forEachIndexed { i, tile ->
                if (i > 0) addView(row, RemoteViews(context.packageName, R.layout.system_divider))
                addView(row, tileView(context, tile, code * 100 + index * 10 + i))
            }
        }
    }

    private fun tileView(context: Context, tile: SystemTile, requestCode: Int) =
        RemoteViews(context.packageName, R.layout.system_tile).apply {
            setTextViewText(R.id.system_tile_label, tile.label)
            setTextViewText(R.id.system_tile_value, tile.value)
            setTextViewText(R.id.system_tile_detail, tile.detail)
            setViewVisibility(R.id.system_tile_gauge, if (tile.progress == null) View.INVISIBLE else View.VISIBLE)
            setProgressBar(R.id.system_tile_gauge, 100, tile.progress ?: 0, false)
            setContentDescription(R.id.system_tile, "${tile.label} ${tile.value}, ${tile.detail}")
            setOnClickPendingIntent(
                R.id.system_tile,
                PendingIntent.getActivity(
                    context,
                    requestCode,
                    Intent(tile.action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                ),
            )
        }

    companion object {
        const val ACTION_REFRESH = "dev.levilainpetit.wux.action.SYSTEM_REFRESH"
    }
}

class SystemWidget : SystemWidgetProvider(R.layout.widget_system, code = 1) {
    override fun rows(context: Context) = listOf(SystemStatus.basic(context))
}

class AdvancedSystemWidget : SystemWidgetProvider(R.layout.widget_system_advanced, code = 2) {
    override fun rows(context: Context) = SystemStatus.advanced(context)
}
