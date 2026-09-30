package dev.levilainpetit.wux.widgets

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.provider.Settings
import android.text.format.DateUtils
import android.util.SizeF
import android.view.View
import android.widget.RemoteViews
import dev.levilainpetit.wux.R
import dev.levilainpetit.wux.system.Link
import dev.levilainpetit.wux.system.NetworkStatus
import dev.levilainpetit.wux.system.SpeedTest
import dev.levilainpetit.wux.system.SystemRefresh
import java.util.Locale
import kotlin.concurrent.thread
import kotlin.math.roundToInt

/**
 * Réseau : le Wi-Fi ou le réseau mobile en cours, la force du signal, le
 * ping et l'adresse locale ; le test de débit ne part qu'au toucher de
 * « Tester ».
 */
class NetworkWidget : NeonWidget() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_TEST) {
            if (NetworkStatus.testing(context)) return
            val pending = goAsync()
            val app = context.applicationContext
            thread(name = "wux-speedtest") {
                try {
                    // « Test… » s'affiche pendant la mesure.
                    NetworkWidget().renderAll(app)
                    NetworkStatus.runSpeedTest(app)
                } finally {
                    NetworkWidget().renderAll(app)
                    pending.finish()
                }
            }
            return
        }
        super.onReceive(context, intent)
    }

    override fun build(context: Context, size: SizeF, sample: Boolean): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_network)
        val panel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY) else Intent(Settings.ACTION_WIRELESS_SETTINGS)
        views.setOnClickPendingIntent(R.id.network_root, activity(context, panel, 66))
        views.setOnClickPendingIntent(
            R.id.network_test,
            PendingIntent.getBroadcast(
                context,
                67,
                Intent(context, NetworkWidget::class.java).setAction(ACTION_TEST),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            ),
        )
        val link = if (sample) Link(Link.Kind.WIFI, "Livebox-4F2A", "5 GHz", -58, 3, "192.168.1.24") else NetworkStatus.link(context)
        val ping = if (sample) 18 else NetworkStatus.ping(context)
        val test = if (sample) SpeedTest(312.0, 48.0, System.currentTimeMillis() - 2 * 3_600_000) else NetworkStatus.lastTest(context)
        val testing = !sample && NetworkStatus.testing(context)

        val kind = when (link.kind) {
            Link.Kind.WIFI -> context.getString(R.string.net_wifi)
            Link.Kind.MOBILE -> context.getString(R.string.net_mobile)
            Link.Kind.OTHER -> context.getString(R.string.net_other)
            Link.Kind.NONE -> context.getString(R.string.net_offline)
        }
        views.setTextViewText(R.id.network_kind, listOfNotNull(kind, link.band).joinToString(" · "))
        views.setTextViewText(
            R.id.network_name,
            link.name ?: when (link.kind) {
                Link.Kind.WIFI -> context.getString(if (sample || NetworkStatus.canReadName(context)) R.string.net_wifi else R.string.net_name_hidden)
                Link.Kind.NONE -> context.getString(R.string.net_offline)
                else -> kind
            },
        )
        views.setTextViewText(
            R.id.network_detail,
            listOfNotNull(
                link.rssi?.let { context.getString(R.string.net_rssi, it) },
                ping?.let { context.getString(R.string.net_ping, it) },
                link.address,
            ).joinToString(" · "),
        )
        val density = context.resources.displayMetrics.density
        views.setImageViewBitmap(R.id.network_bars, bars(link.level, (30 * density).roundToInt(), (22 * density).roundToInt(), density))

        // Pendant le test : une roue qui tourne, et les valeurs en attente.
        views.setViewVisibility(R.id.network_spinner, if (testing) View.VISIBLE else View.GONE)
        views.setTextViewText(R.id.network_down, if (testing) "…" else test?.down?.let { mbps(context, it) } ?: "—")
        views.setTextViewText(R.id.network_up, if (testing) "…" else test?.up?.let { mbps(context, it) } ?: "—")
        views.setTextViewText(R.id.network_test, context.getString(if (testing) R.string.net_testing else R.string.net_test))
        views.setTextViewText(
            R.id.network_tested,
            test?.let {
                context.getString(
                    R.string.net_tested,
                    DateUtils.getRelativeTimeSpanString(it.at, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS).toString().lowercase(Locale.getDefault()),
                )
            } ?: context.getString(R.string.net_never),
        )
        // Sur une seule rangée : le lien seulement.
        val compact = size.height < 100f
        views.setViewVisibility(R.id.network_speed, if (compact) View.GONE else View.VISIBLE)
        views.setViewVisibility(R.id.network_tested, if (compact) View.GONE else View.VISIBLE)
        return views
    }

    override fun onSystemUpdate(context: Context, goAsync: () -> PendingResult) {
        val pending = goAsync()
        val app = context.applicationContext
        thread(name = "wux-ping") {
            try {
                NetworkStatus.measurePing(app)
                NetworkWidget().renderAll(app)
            } finally {
                pending.finish()
            }
        }
    }

    override fun onRendered(context: Context) {
        SystemRefresh.schedule(context)
        // Le signal et le ping changent : un nouveau dessin toutes les 15 minutes.
        context.getSystemService(AlarmManager::class.java)
            ?.set(AlarmManager.RTC, System.currentTimeMillis() + 15 * 60_000L, refreshIntent(context, 3))
    }

    private fun mbps(context: Context, value: Double) =
        context.getString(R.string.net_mbps, if (value >= 100) value.roundToInt().toString() else String.format(Locale.getDefault(), "%.1f", value))

    /** Quatre barres de hauteur croissante, allumées selon le niveau (en blanc, teinté par la mise en page). */
    private fun bars(level: Int?, width: Int, height: Int, density: Float): Bitmap {
        val bitmap = Bitmap.createBitmap(width.coerceAtLeast(1), height.coerceAtLeast(1), Bitmap.Config.ALPHA_8)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val gap = 2.5f * density
        val bar = (width - 3 * gap) / 4f
        for (i in 0 until 4) {
            paint.alpha = if (level != null && i < level) 255 else 60
            val left = i * (bar + gap)
            canvas.drawRoundRect(RectF(left, height * (1 - (i + 1) / 4f), left + bar, height.toFloat()), density, density, paint)
        }
        return bitmap
    }

    companion object {
        const val ACTION_TEST = "dev.levilainpetit.wux.action.SPEED_TEST"
    }
}
