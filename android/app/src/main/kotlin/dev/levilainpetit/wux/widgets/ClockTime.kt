package dev.levilainpetit.wux.widgets

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import android.text.format.DateFormat
import android.widget.RemoteViews
import dev.levilainpetit.wux.R
import java.util.Date
import kotlin.math.ceil

/**
 * L'heure du widget horloge, dessinée avec la police à points.
 *
 * Le lanceur n'a pas accès aux polices de l'application, et `TextClock` ne
 * peut donc pas l'utiliser : l'heure devient une image, redessinée chaque
 * minute. Elle est en `ALPHA_8` (un octet par pixel) et en deux calques, le
 * halo flou et le tracé net, que la mise en page teinte aux couleurs du
 * téléphone.
 */
object ClockTime {

    const val ACTION_TICK = "dev.levilainpetit.wux.action.CLOCK_TICK"

    /** Taille du texte dessiné (dp) ; l'image est ensuite ajustée à sa place. */
    private const val TEXT_SIZE = 96f
    private const val GLOW_RADIUS = 7f

    fun bind(context: Context, views: RemoteViews) {
        val pattern = if (DateFormat.is24HourFormat(context)) "HH:mm" else "h:mm"
        val text = DateFormat.format(pattern, Date()).toString()
        val (glow, core) = render(context, text)
        views.setImageViewBitmap(R.id.clock_time_glow, glow)
        views.setImageViewBitmap(R.id.clock_time, core)
        views.setContentDescription(R.id.clock_time, text)
    }

    private fun render(context: Context, text: String): Pair<Bitmap, Bitmap> {
        val density = context.resources.displayMetrics.density
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = context.resources.getFont(R.font.wux_dots)
            textSize = TEXT_SIZE * density
        }
        val pad = GLOW_RADIUS * 2 * density
        val metrics = paint.fontMetrics
        val width = ceil(paint.measureText(text) + 2 * pad).toInt()
        val height = ceil(metrics.descent - metrics.ascent + 2 * pad).toInt()
        val baseline = pad - metrics.ascent

        fun layer(blur: Boolean): Bitmap {
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ALPHA_8)
            val p = Paint(paint)
            if (blur) p.maskFilter = BlurMaskFilter(GLOW_RADIUS * density, BlurMaskFilter.Blur.NORMAL)
            Canvas(bitmap).drawText(text, pad, baseline, p)
            return bitmap
        }
        return layer(blur = true) to layer(blur = false)
    }

    /**
     * Programme le prochain dessin à la minute pile. Alarme non réveillante :
     * téléphone endormi, elle attend qu'il se rallume, et l'heure est alors
     * redessinée aussitôt.
     */
    fun scheduleNextTick(context: Context) {
        val alarms = context.getSystemService(AlarmManager::class.java) ?: return
        val next = (System.currentTimeMillis() / 60_000 + 1) * 60_000
        val tick = tickIntent(context)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarms.canScheduleExactAlarms()) {
            alarms.setExact(AlarmManager.RTC, next, tick)
        } else {
            // Sans autorisation d'alarme exacte, la minute peut glisser.
            alarms.setWindow(AlarmManager.RTC, next, 60_000, tick)
        }
    }

    fun cancelTicks(context: Context) {
        context.getSystemService(AlarmManager::class.java)?.cancel(tickIntent(context))
    }

    private fun tickIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        Intent(context, ClockWidgetProvider::class.java).setAction(ACTION_TICK),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )
}
