package dev.levilainpetit.wux.widgets

import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import android.util.SizeF
import android.widget.RemoteViews
import dev.levilainpetit.wux.R

/**
 * Horloge analogique néon : un `AnalogClock`, que le lanceur fait tourner
 * seul, avec le cadran et les aiguilles de `tool/analog_assets.py`.
 */
class AnalogClockWidget : NeonWidget() {

    override fun build(context: Context, size: SizeF, sample: Boolean): RemoteViews =
        RemoteViews(context.packageName, R.layout.widget_analog).apply {
            setOnClickPendingIntent(R.id.analog_root, activity(context, Intent(AlarmClock.ACTION_SHOW_ALARMS), 82))
        }
}
