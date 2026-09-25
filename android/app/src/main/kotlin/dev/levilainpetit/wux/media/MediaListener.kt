package dev.levilainpetit.wux.media

import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.service.notification.NotificationListenerService
import dev.levilainpetit.wux.widgets.MediaWidget

/**
 * Accès aux notifications : Android ne dit ce qui joue (et ne laisse le
 * piloter) qu'à un « écouteur de notifications » autorisé. Halo n'en lit
 * aucune ; il suit les sessions multimédias pour redessiner le widget
 * Lecture en cours à chaque changement de morceau ou d'état.
 */
class MediaListener : NotificationListenerService() {

    private val handler = Handler(Looper.getMainLooper())
    private var sessions: MediaSessionManager? = null
    private val followed = mutableListOf<MediaController>()

    private val callback = object : MediaController.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadata?) = redraw()
        override fun onPlaybackStateChanged(state: PlaybackState?) = redraw()
        override fun onSessionDestroyed() = redraw()
    }

    private val sessionsChanged = MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
        follow(controllers.orEmpty())
        redraw()
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        val manager = getSystemService(MediaSessionManager::class.java) ?: return
        sessions = manager
        val component = ComponentName(this, MediaListener::class.java)
        runCatching {
            manager.addOnActiveSessionsChangedListener(sessionsChanged, component, handler)
            follow(manager.getActiveSessions(component))
        }
        redraw()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        sessions?.removeOnActiveSessionsChangedListener(sessionsChanged)
        follow(emptyList())
        redraw()
    }

    private fun follow(controllers: List<MediaController>) {
        followed.forEach { it.unregisterCallback(callback) }
        followed.clear()
        controllers.forEach {
            it.registerCallback(callback, handler)
            followed += it
        }
    }

    // Plusieurs rappels arrivent ensemble : un seul dessin.
    private val draw = Runnable { MediaWidget().renderAll(this) }

    private fun redraw() {
        handler.removeCallbacks(draw)
        handler.postDelayed(draw, 150)
    }

    companion object {
        fun component(context: Context) = ComponentName(context, MediaListener::class.java)

        /** Vrai si l'accès aux notifications est accordé à Halo. */
        fun enabled(context: Context): Boolean {
            val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners").orEmpty()
            return flat.split(':').any { ComponentName.unflattenFromString(it) == component(context) }
        }

        /** La session qui joue, sinon la plus récente ; `null` sans accès ou sans session. */
        fun current(context: Context): MediaController? {
            if (!enabled(context)) return null
            val manager = context.getSystemService(MediaSessionManager::class.java) ?: return null
            val controllers = runCatching { manager.getActiveSessions(component(context)) }.getOrNull().orEmpty()
            return controllers.firstOrNull { it.playbackState?.state == PlaybackState.STATE_PLAYING } ?: controllers.firstOrNull()
        }
    }
}
