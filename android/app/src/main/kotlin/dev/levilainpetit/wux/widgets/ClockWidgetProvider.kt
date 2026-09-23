package dev.levilainpetit.wux.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.SizeF
import android.widget.RemoteViews
import dev.levilainpetit.wux.R

/**
 * Widget horloge : heure, date et prochaine alarme.
 *
 * L'heure et la date sont des `TextClock`, qui avancent seuls sans réveiller
 * l'application. La prochaine alarme est redessinée quand le
 * système annonce qu'elle a changé (voir le `<receiver>` dans
 * `AndroidManifest.xml`).
 *
 * Deux mises en page, d'après la maquette : la grande (`widget_clock`, date
 * sur deux lignes) et, quand le widget est réduit à une rangée, la compacte
 * (`widget_clock_compact`).
 */
class ClockWidgetProvider : AppWidgetProvider(), PreviewableWidget {

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action in ClockViews.REFRESH_ACTIONS) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, ClockWidgetProvider::class.java))
            if (ids.isNotEmpty()) onUpdate(context, manager, ids)
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        appWidgetIds.forEach { id ->
            appWidgetManager.updateAppWidget(id, views(context, appWidgetManager.getAppWidgetOptions(id)))
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle,
    ) {
        // Avant Android 12, c'est ici que l'on apprend le redimensionnement.
        appWidgetManager.updateAppWidget(appWidgetId, views(context, newOptions))
    }

    private fun views(context: Context, options: Bundle): RemoteViews {
        val compact = build(context, R.layout.widget_clock_compact)
        val full = build(context, R.layout.widget_clock)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Le lanceur choisit lui-même la plus grande qui tient.
            return RemoteViews(mapOf(SizeF(100f, 40f) to compact, SizeF(160f, FULL_MIN_HEIGHT) to full))
        }
        val height = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, FULL_MIN_HEIGHT.toInt())
        return if (height < FULL_MIN_HEIGHT) compact else full
    }

    override fun preview(context: Context, size: SizeF): RemoteViews =
        build(context, if (size.height < FULL_MIN_HEIGHT) R.layout.widget_clock_compact else R.layout.widget_clock)

    private fun build(context: Context, layout: Int): RemoteViews =
        RemoteViews(context.packageName, layout).also { ClockViews.bind(context, it) }

    private companion object {
        /** Hauteur (dp) à partir de laquelle l'heure, la date et l'alarme s'empilent. */
        const val FULL_MIN_HEIGHT = 100f
    }
}
