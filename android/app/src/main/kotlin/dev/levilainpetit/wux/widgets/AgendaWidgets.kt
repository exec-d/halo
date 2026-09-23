package dev.levilainpetit.wux.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import dev.levilainpetit.wux.calendar.AgendaBuilder
import dev.levilainpetit.wux.calendar.CalendarRepository
import es.antonborri.home_widget.HomeWidgetPlugin

/**
 * Widget agenda : les événements groupés par jour, sur [days] jours.
 *
 * [widgetId] est l'identifiant partagé avec Flutter (`lib/src/home_widgets/catalog.dart`)
 * pour les réglages ; [withClock] ajoute le bloc horloge en tête.
 */
abstract class AgendaWidgetProvider(
    private val widgetId: String,
    private val days: Int,
    private val withClock: Boolean = false,
) : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (withClock && intent.action in ClockViews.REFRESH_ACTIONS) renderAll(context)
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { render(context, appWidgetManager, it) }
        AgendaRefresh.schedule(context)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle,
    ) {
        // Redimensionné : ce qui tient dans les colonnes a changé.
        render(context, appWidgetManager, appWidgetId)
    }

    fun renderAll(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        manager.getAppWidgetIds(ComponentName(context, javaClass)).forEach { render(context, manager, it) }
    }

    private fun render(context: Context, manager: AppWidgetManager, appWidgetId: Int) {
        val settings = AgendaSettings.read(HomeWidgetPlugin.getData(context), widgetId)
        val repository = CalendarRepository(context)
        val agenda = if (repository.hasPermission()) {
            AgendaBuilder.build(context, repository, days, settings.calendarIds)
        } else {
            null
        }
        manager.updateAppWidget(
            appWidgetId,
            AgendaRenderer.views(context, agenda, manager.getAppWidgetOptions(appWidgetId), withClock),
        )
    }
}

class TodayAgendaWidgetReceiver : AgendaWidgetProvider(widgetId = "agenda_today", days = 1)

class TwoDayAgendaWidgetReceiver : AgendaWidgetProvider(widgetId = "agenda_two_days", days = 2)

class ClockAgendaWidgetProvider :
    AgendaWidgetProvider(widgetId = "clock_agenda", days = 2, withClock = true)
