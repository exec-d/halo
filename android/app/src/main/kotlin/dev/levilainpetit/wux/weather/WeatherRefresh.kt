package dev.levilainpetit.wux.weather

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
import dev.levilainpetit.wux.widgets.SunMoonWidget
import dev.levilainpetit.wux.widgets.WeatherWidget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/** Prévisions rafraîchies toutes les heures, seulement avec du réseau. */
object WeatherRefresh {

    private const val PERIODIC = "weather-hourly"
    private const val NOW = "weather-now"

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

    fun redraw(context: Context) {
        WeatherWidget().renderAll(context)
        SunMoonWidget().renderAll(context)
    }
}

class WeatherWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val ok = withContext(Dispatchers.IO) { Weather.refresh(applicationContext) }
        WeatherRefresh.redraw(applicationContext)
        return if (ok || Weather.place(applicationContext) == null) Result.success() else Result.retry()
    }
}
