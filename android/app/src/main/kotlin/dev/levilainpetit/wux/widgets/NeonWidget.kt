package dev.levilainpetit.wux.widgets

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.SizeF
import android.widget.RemoteViews
import es.antonborri.home_widget.HomeWidgetPlugin
import java.time.ZonedDateTime

/**
 * Base des widgets simples : une mise en page par taille annoncée par le
 * lanceur (Android 12+), redessinée à la pose, au redimensionnement, sur les
 * diffusions de [refreshActions] et sur [ACTION_REFRESH].
 */
abstract class NeonWidget : AppWidgetProvider(), PreviewableWidget {

    /** La mise en page pour une taille donnée (dp). */
    abstract fun build(context: Context, size: SizeF): RemoteViews

    /** Diffusions système qui changent ce que le widget affiche. */
    open val refreshActions: Set<String> = emptySet()

    /** Après chaque dessin : (re)programmer ce qui doit l'être. */
    open fun onRendered(context: Context) {}

    override fun preview(context: Context, size: SizeF) = build(context, size)

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH || intent.action in refreshActions) renderAll(context)
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { render(context, appWidgetManager, it) }
        onRendered(context)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle,
    ) = render(context, appWidgetManager, appWidgetId)

    fun renderAll(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(ComponentName(context, javaClass))
        ids.forEach { render(context, manager, it) }
        if (ids.isNotEmpty()) onRendered(context)
    }

    private fun render(context: Context, manager: AppWidgetManager, id: Int) {
        val options = manager.getAppWidgetOptions(id)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            @Suppress("DEPRECATION")
            val sizes = options.getParcelableArrayList<SizeF>(AppWidgetManager.OPTION_APPWIDGET_SIZES)
            if (!sizes.isNullOrEmpty()) {
                manager.updateAppWidget(id, RemoteViews(sizes.associateWith { build(context, it) }))
                return
            }
        }
        val width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 250)
        val height = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 110)
        manager.updateAppWidget(id, build(context, SizeF(width.toFloat(), height.toFloat())))
    }

    /** Redessine ce widget peu après minuit (alarme non réveillante). */
    protected fun refreshAfterMidnight(context: Context) {
        val now = ZonedDateTime.now()
        val next = now.toLocalDate().plusDays(1).atStartOfDay(now.zone).plusMinutes(1)
        context.getSystemService(AlarmManager::class.java)
            ?.set(AlarmManager.RTC, next.toInstant().toEpochMilli(), refreshIntent(context, MIDNIGHT))
    }

    /** Diffusion [ACTION_REFRESH] vers ce widget. */
    protected fun refreshIntent(context: Context, code: Int = 0): PendingIntent = PendingIntent.getBroadcast(
        context,
        code,
        Intent(context, javaClass).setAction(ACTION_REFRESH),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    companion object {
        const val ACTION_REFRESH = "dev.levilainpetit.wux.action.REFRESH"
        private const val MIDNIGHT = 1000

        /** Diffusions après lesquelles la date affichée peut avoir changé. */
        val DATE_ACTIONS = setOf(Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED, Intent.ACTION_LOCALE_CHANGED)

        /** Réglages écrits par Flutter (`<id>.<nom>`). */
        fun settings(context: Context) = HomeWidgetPlugin.getData(context)

        fun activity(context: Context, intent: Intent, code: Int): PendingIntent = PendingIntent.getActivity(
            context,
            code,
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }
}
