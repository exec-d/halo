package dev.levilainpetit.wux.system

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dev.levilainpetit.wux.widgets.BatteryWidget
import dev.levilainpetit.wux.widgets.BluetoothDevicesWidget
import dev.levilainpetit.wux.widgets.MobileDataWidget
import dev.levilainpetit.wux.widgets.ScreenTimeWidget
import java.util.concurrent.TimeUnit

/**
 * Les widgets système détaillés redessinés toutes les 15 minutes (le plus
 * court que permet Android), ce qui ajoute aussi un relevé de batterie.
 */
object SystemRefresh {
    private const val PERIODIC = "system-15min"

    fun schedule(context: Context) {
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<SystemRefreshWorker>(15, TimeUnit.MINUTES).build(),
        )
    }

    fun redraw(context: Context) {
        BatteryWidget().renderAll(context)
        BluetoothDevicesWidget().renderAll(context)
        ScreenTimeWidget().renderAll(context)
        MobileDataWidget().renderAll(context)
    }
}

class SystemRefreshWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        // Un relevé même sans widget Batterie posé : la courbe sera prête.
        BatteryHistory.read(applicationContext)
        SystemRefresh.redraw(applicationContext)
        return Result.success()
    }
}
