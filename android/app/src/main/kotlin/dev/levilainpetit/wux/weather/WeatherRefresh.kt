package dev.levilainpetit.wux.weather

import android.content.BroadcastReceiver
import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dev.levilainpetit.wux.widgets.AllergyWidget
import dev.levilainpetit.wux.widgets.ForecastWidget
import dev.levilainpetit.wux.widgets.RainWidget
import dev.levilainpetit.wux.widgets.SunMoonWidget
import dev.levilainpetit.wux.widgets.WeatherWidget
import es.antonborri.home_widget.HomeWidgetPlugin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

/** Prévisions rafraîchies toutes les heures, seulement avec du réseau. */
object WeatherRefresh {

    private const val PERIODIC = "weather-hourly"
    private const val NOW = "weather-now"
    private const val STARTED = "weather.refresh_started_at"

    private val network = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

    fun schedule(context: Context) {
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<WeatherWorker>(1, TimeUnit.HOURS).setConstraints(network).build(),
        )
    }

    /** Tout de suite (lieu changé, widget posé), dès que le réseau le permet. */
    fun now(context: Context) {
        WorkManager.getInstance(context).enqueueUniqueWork(
            NOW,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<WeatherWorker>().setConstraints(network).build(),
        )
    }

    /**
     * Retélécharge tout de suite si les prévisions sont périmées. Appelé par
     * les widgets à chaque mise à jour système (toutes les 30 minutes, que le
     * système livre même à une application mise en veille, contrairement aux
     * tâches WorkManager) : [goAsync] garde le récepteur en vie le temps du
     * téléchargement.
     */
    fun refreshIfStale(context: Context, goAsync: () -> BroadcastReceiver.PendingResult) {
        if (Weather.place(context) == null || !Weather.isStale(context)) return
        val prefs = HomeWidgetPlugin.getData(context)
        val now = System.currentTimeMillis()
        // Plusieurs widgets météo se mettent à jour ensemble : un seul télécharge.
        if (now - prefs.getLong(STARTED, 0) < 2 * 60_000) return
        prefs.edit().putLong(STARTED, now).apply()
        val pending = goAsync()
        val app = context.applicationContext
        thread(name = "wux-weather") {
            try {
                if (Weather.refresh(app)) redraw(app)
            } finally {
                pending.finish()
            }
        }
    }

    fun redraw(context: Context) {
        WeatherWidget().renderAll(context)
        SunMoonWidget().renderAll(context)
        RainWidget().renderAll(context)
        AllergyWidget().renderAll(context)
        ForecastWidget().renderAll(context)
    }
}

class WeatherWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val ok = withContext(Dispatchers.IO) { Weather.refresh(applicationContext) }
        WeatherRefresh.redraw(applicationContext)
        return if (ok || Weather.place(applicationContext) == null) Result.success() else Result.retry()
    }
}
