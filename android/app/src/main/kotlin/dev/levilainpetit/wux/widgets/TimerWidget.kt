package dev.levilainpetit.wux.widgets

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.SystemClock
import android.util.SizeF
import android.view.View
import android.widget.RemoteViews
import dev.levilainpetit.wux.MainActivity
import dev.levilainpetit.wux.R

/**
 * Chronomètre et minuteur. Le temps défile dans le lanceur (`Chronometer`,
 * à rebours pour le minuteur) sans réveiller Halo ; les boutons passent par
 * ce récepteur, qui garde l'état et, pour un minuteur, programme la sonnerie.
 */
class TimerWidget : NeonWidget() {

    private data class State(
        val timer: Boolean,
        val running: Boolean,
        /** Chronomètre : temps écoulé ; minuteur : temps restant (ms), à l'arrêt. */
        val stored: Long,
        /** Chronomètre : départ ; minuteur : fin (`elapsedRealtime`), en marche. */
        val base: Long,
        /** Durée du minuteur (ms). */
        val duration: Long,
        val done: Boolean,
    )

    override fun build(context: Context, size: SizeF, sample: Boolean): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_timer)
        val state = if (sample) State(true, false, 17 * 60_000L + 42_000L, 0L, 25 * 60_000L, false) else read(context)
        val now = SystemClock.elapsedRealtime()

        views.setTextViewText(
            R.id.timer_mode,
            when {
                state.done -> context.getString(R.string.timer_done)
                state.timer -> context.getString(R.string.timer_countdown, state.duration / 60_000)
                else -> context.getString(R.string.timer_stopwatch)
            },
        )
        // Le chronomètre affiche « maintenant − base » ; à rebours, « base − maintenant ».
        val base = when {
            state.running -> state.base
            state.timer -> now + state.stored
            else -> now - state.stored
        }
        views.setChronometerCountDown(R.id.timer_clock, state.timer)
        views.setChronometer(R.id.timer_clock, base, null, state.running)

        views.setImageViewResource(R.id.timer_toggle, if (state.running) R.drawable.timer_pause else R.drawable.timer_play)
        views.setOnClickPendingIntent(R.id.timer_toggle, action(context, OP_TOGGLE))
        views.setOnClickPendingIntent(R.id.timer_reset, action(context, OP_RESET))
        PRESETS.forEachIndexed { i, (id, minutes) ->
            views.setOnClickPendingIntent(id, action(context, "$OP_PRESET$minutes", i))
        }
        // Étroit : les préréglages cèdent la place.
        views.setViewVisibility(R.id.timer_presets, if (size.width >= 230) View.VISIBLE else View.GONE)
        return views
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_TIMER) {
            handle(context, intent.getStringExtra(EXTRA_OP).orEmpty())
            renderAll(context)
            return
        }
        super.onReceive(context, intent)
    }

    private fun handle(context: Context, op: String) {
        val state = read(context)
        val now = SystemClock.elapsedRealtime()
        val next = when {
            op == OP_TOGGLE && state.running -> {
                cancelAlarm(context)
                if (state.timer) state.copy(running = false, stored = (state.base - now).coerceAtLeast(0L))
                else state.copy(running = false, stored = now - state.base)
            }
            op == OP_TOGGLE -> {
                if (state.timer && state.stored <= 0L) {
                    state
                } else if (state.timer) {
                    schedule(context, now + state.stored)
                    state.copy(running = true, base = now + state.stored, done = false)
                } else {
                    state.copy(running = true, base = now - state.stored, done = false)
                }
            }
            op == OP_RESET -> {
                cancelAlarm(context)
                // Un minuteur déjà remis à zéro redevient un chronomètre.
                val idle = !state.running && state.stored == state.duration
                if (state.timer && !idle) state.copy(running = false, stored = state.duration, done = false)
                else State(false, false, 0L, 0L, state.duration, false)
            }
            op.startsWith(OP_PRESET) -> {
                val minutes = op.removePrefix(OP_PRESET).toLongOrNull() ?: return
                val duration = minutes * 60_000L
                schedule(context, now + duration)
                State(true, true, duration, now + duration, duration, false)
            }
            op == OP_DONE -> {
                notifyDone(context, state.duration)
                state.copy(running = false, stored = 0L, done = true)
            }
            else -> return
        }
        write(context, next)
    }

    // ——— État ———

    private fun read(context: Context): State {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return State(
            timer = prefs.getBoolean("timer", false),
            running = prefs.getBoolean("running", false),
            stored = prefs.getLong("stored", 0L),
            base = prefs.getLong("base", 0L),
            duration = prefs.getLong("duration", 25 * 60_000L),
            done = prefs.getBoolean("done", false),
        )
    }

    private fun write(context: Context, state: State) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean("timer", state.timer)
            .putBoolean("running", state.running)
            .putLong("stored", state.stored)
            .putLong("base", state.base)
            .putLong("duration", state.duration)
            .putBoolean("done", state.done)
            .apply()
    }

    private fun action(context: Context, op: String, code: Int = 0): PendingIntent = PendingIntent.getBroadcast(
        context,
        op.hashCode() + code,
        Intent(context, TimerWidget::class.java).setAction(ACTION_TIMER).putExtra(EXTRA_OP, op),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    // ——— Sonnerie ———

    /** La fin du minuteur, à la seconde, même en veille. */
    private fun schedule(context: Context, endElapsed: Long) {
        val alarms = context.getSystemService(AlarmManager::class.java) ?: return
        val wall = System.currentTimeMillis() + (endElapsed - SystemClock.elapsedRealtime())
        val done = action(context, OP_DONE)
        val exact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarms.canScheduleExactAlarms()
        if (exact) {
            val show = PendingIntent.getActivity(context, 84, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
            alarms.setAlarmClock(AlarmManager.AlarmClockInfo(wall, show), done)
        } else {
            alarms.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, endElapsed, done)
        }
    }

    private fun cancelAlarm(context: Context) {
        context.getSystemService(AlarmManager::class.java)?.cancel(action(context, OP_DONE))
    }

    private fun notifyDone(context: Context, duration: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && manager.getNotificationChannel(CHANNEL) == null) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL, context.getString(R.string.timer_channel), NotificationManager.IMPORTANCE_HIGH).apply {
                    setSound(
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
                        AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build(),
                    )
                    enableVibration(true)
                },
            )
        }
        val open = PendingIntent.getActivity(context, 85, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        val notification = android.app.Notification.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.tile_wallpaper)
            .setContentTitle(context.getString(R.string.timer_done))
            .setContentText(context.getString(R.string.timer_done_detail, duration / 60_000))
            .setCategory(android.app.Notification.CATEGORY_ALARM)
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        manager.notify(NOTIFICATION, notification)
    }

    companion object {
        const val ACTION_TIMER = "dev.levilainpetit.wux.action.TIMER"
        private const val EXTRA_OP = "op"
        private const val OP_TOGGLE = "toggle"
        private const val OP_RESET = "reset"
        private const val OP_PRESET = "preset:"
        private const val OP_DONE = "done"
        private const val PREFS = "halo_timer"
        private const val CHANNEL = "timer"
        private const val NOTIFICATION = 4401

        private val PRESETS = listOf(
            R.id.timer_preset_1 to 1,
            R.id.timer_preset_5 to 5,
            R.id.timer_preset_10 to 10,
            R.id.timer_preset_25 to 25,
        )
    }
}
