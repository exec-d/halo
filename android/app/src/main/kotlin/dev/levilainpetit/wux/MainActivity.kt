package dev.levilainpetit.wux

import android.Manifest
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import dev.levilainpetit.wux.calendar.CalendarRepository
import dev.levilainpetit.wux.widgets.AgendaRefresh
import dev.levilainpetit.wux.widgets.WidgetPreviews
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Hôte Flutter, et canal `dev.levilainpetit.wux/native` : tout ce dont
 * l'application a besoin et que `home_widget` ne couvre pas : calendrier,
 * aperçus (le vrai widget dessiné en PNG), rafraîchissement des agendas.
 */
open class MainActivity : FlutterActivity() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var pendingPermission: MethodChannel.Result? = null

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL)
            .setMethodCallHandler(::handle)
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun handle(call: MethodCall, result: MethodChannel.Result) {
        val repository = CalendarRepository(applicationContext)
        when (call.method) {
            "hasCalendarPermission" -> result.success(repository.hasPermission())
            "requestCalendarPermission" -> requestCalendarPermission(result)
            "openAppSettings" -> {
                startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        .setData(Uri.fromParts("package", packageName, null)),
                )
                result.success(null)
            }
            "calendars" -> background(result) {
                repository.calendars().map {
                    mapOf("id" to it.id, "name" to it.name, "account" to it.account, "color" to it.color)
                }
            }
            "renderWidget" -> {
                val id = call.argument<String>("id").orEmpty()
                val width = call.argument<Number>("width")?.toFloat() ?: 0f
                val height = call.argument<Number>("height")?.toFloat() ?: 0f
                try {
                    result.success(WidgetPreviews.render(this, id, width, height))
                } catch (e: Exception) {
                    result.error("render", e.message, null)
                }
            }
            "widgetProvider" -> {
                // Classe Kotlin du widget en cours de configuration, pour que
                // Dart retrouve son écran dans le catalogue.
                val id = call.argument<Int>("id") ?: AppWidgetManager.INVALID_APPWIDGET_ID
                result.success(AppWidgetManager.getInstance(this).getAppWidgetInfo(id)?.provider?.className)
            }
            "refreshWidgets" -> scope.launch {
                withContext(Dispatchers.IO) { AgendaRefresh.refreshAll(applicationContext) }
                AgendaRefresh.schedule(applicationContext)
                result.success(null)
            }
            else -> result.notImplemented()
        }
    }

    private fun background(result: MethodChannel.Result, block: () -> Any?) {
        scope.launch {
            try {
                result.success(withContext(Dispatchers.IO) { block() })
            } catch (e: Exception) {
                result.error("native", e.message, null)
            }
        }
    }

    private fun requestCalendarPermission(result: MethodChannel.Result) {
        if (CalendarRepository(applicationContext).hasPermission()) {
            result.success(true)
            return
        }
        pendingPermission?.success(false)
        pendingPermission = result
        requestPermissions(arrayOf(Manifest.permission.READ_CALENDAR), CALENDAR_REQUEST)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != CALENDAR_REQUEST) return
        val granted = grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED
        pendingPermission?.success(granted)
        pendingPermission = null
        if (granted) {
            scope.launch {
                withContext(Dispatchers.IO) { AgendaRefresh.refreshAll(applicationContext) }
                AgendaRefresh.schedule(applicationContext)
            }
        }
    }

    private companion object {
        const val CHANNEL = "dev.levilainpetit.wux/native"
        const val CALENDAR_REQUEST = 4201

    }
}
