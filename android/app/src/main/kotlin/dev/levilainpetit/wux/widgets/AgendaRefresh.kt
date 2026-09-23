package dev.levilainpetit.wux.widgets

import android.content.Context
import android.provider.CalendarContract
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.time.Duration
import java.time.LocalDate
import java.time.ZonedDateTime

/**
 * Tient les widgets agenda à jour.
 *
 * Trois déclencheurs : une modification du calendrier (déclencheur de contenu
 * WorkManager), le passage à minuit, et un changement de réglage dans l'app.
 * Le système ajoute sa mise à jour périodique (`updatePeriodMillis`).
 */
object AgendaRefresh {

    private const val WORK_CALENDAR_CHANGE = "agenda-calendar-change"
    private const val WORK_MIDNIGHT = "agenda-midnight"

    suspend fun refreshAll(context: Context) {
        val manager = GlanceAppWidgetManager(context)
        val stamp = System.currentTimeMillis()
        listOf(TodayAgendaWidget(), TwoDayAgendaWidget()).forEach { widget ->
            manager.getGlanceIds(widget.javaClass).forEach { id ->
                updateAppWidgetState(context, id) { it[AgendaWidget.REFRESH_KEY] = stamp }
                widget.update(context, id)
            }
        }
    }

    /** (Re)programme les deux déclencheurs, qui ne servent qu'une fois chacun. */
    fun schedule(context: Context) {
        val work = WorkManager.getInstance(context)

        val onCalendarChange = OneTimeWorkRequestBuilder<AgendaRefreshWorker>()
            .setConstraints(
                Constraints.Builder()
                    .addContentUriTrigger(CalendarContract.CONTENT_URI, true)
                    .setTriggerContentUpdateDelay(Duration.ofSeconds(5))
                    .build(),
            )
            .build()
        work.enqueueUniqueWork(WORK_CALENDAR_CHANGE, ExistingWorkPolicy.REPLACE, onCalendarChange)

        val now = ZonedDateTime.now()
        val midnight = LocalDate.now().plusDays(1).atStartOfDay(now.zone).plusMinutes(1)
        val atMidnight = OneTimeWorkRequestBuilder<AgendaRefreshWorker>()
            .setInitialDelay(Duration.between(now, midnight))
            .build()
        work.enqueueUniqueWork(WORK_MIDNIGHT, ExistingWorkPolicy.REPLACE, atMidnight)
    }
}

class AgendaRefreshWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        AgendaRefresh.refreshAll(applicationContext)
        AgendaRefresh.schedule(applicationContext)
        return Result.success()
    }
}
