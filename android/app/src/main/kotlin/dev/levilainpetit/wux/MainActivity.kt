package dev.levilainpetit.wux

import android.Manifest
import android.appwidget.AppWidgetManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.PowerManager
import android.net.Uri
import android.provider.Settings
import dev.levilainpetit.wux.calendar.CalendarRepository
import dev.levilainpetit.wux.wallpaper.WallpaperPreview
import dev.levilainpetit.wux.weather.Place
import dev.levilainpetit.wux.weather.Weather
import dev.levilainpetit.wux.weather.WeatherRefresh
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
import java.util.Locale

/**
 * Hôte Flutter, et canal `dev.levilainpetit.wux/native` : tout ce dont
 * l'application a besoin et que `home_widget` ne couvre pas : calendrier,
 * aperçus (le vrai widget dessiné en PNG), rafraîchissement des agendas.
 */
open class MainActivity : FlutterActivity() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var pendingPermission: MethodChannel.Result? = null
    private var pendingLocation: MethodChannel.Result? = null
    private var pendingLocationPermission: MethodChannel.Result? = null

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL)
            .setMethodCallHandler(::handle)
    }

    override fun onResume() {
        super.onResume()
        // Ouvrir WUX rafraîchit des prévisions périmées.
        if (Weather.place(this) != null && Weather.isStale(this)) {
            scope.launch {
                val ok = withContext(Dispatchers.IO) { Weather.refresh(applicationContext) }
                if (ok) WeatherRefresh.redraw(applicationContext)
            }
        }
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
            "weatherPlace" -> result.success(Weather.place(this)?.name)
            "weatherSearch" -> background(result) {
                Weather.search(call.argument<String>("query").orEmpty()).map {
                    mapOf("name" to it.name, "latitude" to it.latitude, "longitude" to it.longitude)
                }
            }
            "weatherSetPlace" -> {
                val place = Place(
                    call.argument<String>("name").orEmpty(),
                    call.argument<Double>("latitude") ?: 0.0,
                    call.argument<Double>("longitude") ?: 0.0,
                )
                usePlace(place, result)
            }
            "weatherLocate" -> locate(result)
            "appInfo" -> {
                val info = packageManager.getPackageInfo(packageName, 0)
                val code = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    info.longVersionCode
                } else {
                    @Suppress("DEPRECATION")
                    info.versionCode.toLong()
                }
                result.success(mapOf("version" to info.versionName, "build" to code))
            }
            "status" -> result.success(
                mapOf(
                    "calendar" to repository.hasPermission(),
                    "location" to (
                        checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) ==
                            PackageManager.PERMISSION_GRANTED
                        ),
                    "batteryUnrestricted" to (
                        getSystemService(PowerManager::class.java)?.isIgnoringBatteryOptimizations(packageName) == true
                        ),
                    "weatherPlace" to Weather.place(this)?.name,
                    "weatherUpdatedAt" to Weather.fetchedAt(this),
                ),
            )
            "requestLocationPermission" -> {
                if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    result.success(true)
                } else {
                    pendingLocationPermission?.success(false)
                    pendingLocationPermission = result
                    requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), LOCATION_ONLY_REQUEST)
                }
            }
            "openBatterySettings" -> {
                startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                result.success(null)
            }
            "refreshWeather" -> scope.launch {
                val ok = withContext(Dispatchers.IO) { Weather.refresh(applicationContext) }
                if (ok) WeatherRefresh.redraw(applicationContext)
                result.success(ok)
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
                val sample = call.argument<Boolean>("sample") == true
                try {
                    result.success(WidgetPreviews.render(this, id, width, height, sample))
                } catch (e: Exception) {
                    result.error("render", e.message, null)
                }
            }
            "renderWallpaper" -> {
                val width = call.argument<Number>("width")?.toInt() ?: 0
                val height = call.argument<Number>("height")?.toInt() ?: 0
                if (width <= 0 || height <= 0) {
                    result.success(null)
                } else {
                    background(result) { WallpaperPreview.render(applicationContext, width, height) }
                }
            }
            "wallpaperActive" -> result.success(WallpaperPreview.isActive(this))
            "applyWallpaper" -> {
                try {
                    startActivity(WallpaperPreview.applyIntent(this))
                    result.success(true)
                } catch (e: ActivityNotFoundException) {
                    result.success(false)
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

    /** Enregistre [place], télécharge ses prévisions et redessine les widgets. */
    private fun usePlace(place: Place, result: MethodChannel.Result) {
        Weather.setPlace(applicationContext, place)
        scope.launch {
            val ok = withContext(Dispatchers.IO) { Weather.refresh(applicationContext) }
            WeatherRefresh.redraw(applicationContext)
            WeatherRefresh.schedule(applicationContext)
            if (!ok) WeatherRefresh.now(applicationContext)
            result.success(mapOf("name" to place.name, "fetched" to ok))
        }
    }

    /** Position approximative (réseau), demandée au premier usage. */
    private fun locate(result: MethodChannel.Result) {
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            pendingLocation?.success(null)
            pendingLocation = result
            requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), LOCATION_REQUEST)
            return
        }
        val locations: LocationManager? = getSystemService(LocationManager::class.java)
        val provider = listOf(LocationManager.NETWORK_PROVIDER, "fused", LocationManager.GPS_PROVIDER)
            .firstOrNull { runCatching { locations?.isProviderEnabled(it) == true }.getOrDefault(false) }
        if (locations == null || provider == null) {
            result.error("location", "La localisation est désactivée.", null)
            return
        }
        val found = { location: Location? ->
            val known = location ?: runCatching { locations.getLastKnownLocation(provider) }.getOrNull()
            if (known == null) {
                result.error("location", "Position introuvable.", null)
            } else {
                scope.launch {
                    val name = withContext(Dispatchers.IO) { placeName(known) }
                    usePlace(Place(name, known.latitude, known.longitude), result)
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            locations.getCurrentLocation(provider, null, mainExecutor) { found(it) }
        } else {
            found(null)
        }
    }

    private fun placeName(location: Location): String = runCatching {
        @Suppress("DEPRECATION")
        Geocoder(this, Locale.getDefault()).getFromLocation(location.latitude, location.longitude, 1)
            ?.firstOrNull()?.locality
    }.getOrNull() ?: getString(R.string.weather_my_position)

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
        val granted = grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED
        if (requestCode == LOCATION_ONLY_REQUEST) {
            pendingLocationPermission?.success(granted)
            pendingLocationPermission = null
            return
        }
        if (requestCode == LOCATION_REQUEST) {
            val pending = pendingLocation ?: return
            pendingLocation = null
            if (granted) locate(pending) else pending.success(null)
            return
        }
        if (requestCode != CALENDAR_REQUEST) return
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
        const val LOCATION_REQUEST = 4202
        const val LOCATION_ONLY_REQUEST = 4203
    }
}
