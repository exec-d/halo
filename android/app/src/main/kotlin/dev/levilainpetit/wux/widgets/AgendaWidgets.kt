package dev.levilainpetit.wux.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.util.SizeF
import dev.levilainpetit.wux.calendar.AgendaDay
import dev.levilainpetit.wux.calendar.AgendaBuilder
import dev.levilainpetit.wux.calendar.CalendarRepository
import es.antonborri.home_widget.HomeWidgetPlugin

/**
 * Widget agenda : les événements groupés par jour, en [columns] colonnes. Le
 * nombre de jours (aujourd'hui, ou aujourd'hui et demain) est un réglage.
 *
 * [widgetId] est l'identifiant partagé avec Flutter (`lib/src/home_widgets/catalog.dart`)
 * pour les réglages.
 */
abstract class AgendaWidgetProvider(
    private val widgetId: String,
    private val columns: Int,
) : AppWidgetProvider(), PreviewableWidget {

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
        manager.updateAppWidget(
            appWidgetId,
            AgendaRenderer.views(context, agenda(context), manager.getAppWidgetOptions(appWidgetId), columns),
        )
    }

    override fun preview(context: Context, size: SizeF, sample: Boolean) =
        AgendaRenderer.render(
            context,
            if (sample) SampleData.agenda(context, settings(context).showAllDay) else agenda(context),
            size,
            columns,
        )

    private fun settings(context: Context) = AgendaSettings.read(HomeWidgetPlugin.getData(context), widgetId)

    /** `null` : l'accès au calendrier n'est pas accordé. */
    private fun agenda(context: Context): List<AgendaDay>? {
        val settings = settings(context)
        val repository = CalendarRepository(context)
        if (!repository.hasPermission()) return null
        return AgendaBuilder.build(context, repository, settings.days, settings.calendarIds, settings.showAllDay)
    }
}

class OneColumnAgendaWidget : AgendaWidgetProvider(widgetId = "agenda_one_column", columns = 1)

class TwoColumnAgendaWidget : AgendaWidgetProvider(widgetId = "agenda_two_columns", columns = 2)
