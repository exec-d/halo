package dev.levilainpetit.wux.widgets

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.RectF
import android.media.MediaMetadata
import android.media.session.PlaybackState
import android.os.SystemClock
import android.util.SizeF
import android.view.View
import android.widget.RemoteViews
import dev.levilainpetit.wux.MainActivity
import dev.levilainpetit.wux.R
import dev.levilainpetit.wux.media.MediaListener
import kotlin.math.roundToInt

/**
 * Lecture en cours : la pochette passée au néon, titre, artiste, avancement,
 * et précédent / lecture-pause / suivant. Redessiné par [MediaListener] à
 * chaque changement.
 */
class MediaWidget : NeonWidget() {

    private data class Now(
        val title: String,
        val artist: String,
        val app: String,
        val art: Bitmap?,
        val playing: Boolean,
        val position: Long,
        val duration: Long,
    )

    override fun build(context: Context, size: SizeF, sample: Boolean): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_media)
        val now = if (sample) SAMPLE else read(context)
        val allowed = sample || MediaListener.enabled(context)

        val message = when {
            !allowed -> context.getString(R.string.media_permission)
            now == null -> context.getString(R.string.media_nothing)
            else -> null
        }
        views.setViewVisibility(R.id.media_empty, if (message != null) View.VISIBLE else View.GONE)
        views.setViewVisibility(R.id.media_player, if (message != null) View.GONE else View.VISIBLE)
        if (message != null || now == null) {
            views.setTextViewText(R.id.media_empty, message)
            views.setOnClickPendingIntent(R.id.media_root, activity(context, Intent(context, MainActivity::class.java), 86))
            return views
        }

        views.setTextViewText(R.id.media_app, now.app)
        views.setTextViewText(R.id.media_title, now.title)
        views.setTextViewText(R.id.media_artist, now.artist)
        val d = context.resources.displayMetrics.density
        val art = now.art?.let { neon(it, (56 * d).roundToInt()) }
        if (art != null) views.setImageViewBitmap(R.id.media_art, art) else views.setImageViewResource(R.id.media_art, R.drawable.timer_play)
        val showProgress = now.duration > 0
        views.setViewVisibility(R.id.media_progress, if (showProgress) View.VISIBLE else View.GONE)
        if (showProgress) {
            views.setProgressBar(R.id.media_progress, 1000, (now.position * 1000 / now.duration).toInt().coerceIn(0, 1000), false)
        }
        views.setImageViewResource(R.id.media_play, if (now.playing) R.drawable.timer_pause else R.drawable.timer_play)
        views.setOnClickPendingIntent(R.id.media_previous, action(context, OP_PREVIOUS))
        views.setOnClickPendingIntent(R.id.media_play, action(context, OP_PLAY))
        views.setOnClickPendingIntent(R.id.media_next, action(context, OP_NEXT))
        // Toucher le reste ouvre l'application qui joue.
        MediaListener.current(context)?.sessionActivity?.let { views.setOnClickPendingIntent(R.id.media_root, it) }
        return views
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_MEDIA) {
            val controls = MediaListener.current(context)?.transportControls
            when (intent.getStringExtra(EXTRA_OP)) {
                OP_PREVIOUS -> controls?.skipToPrevious()
                OP_NEXT -> controls?.skipToNext()
                OP_PLAY -> {
                    val playing = MediaListener.current(context)?.playbackState?.state == PlaybackState.STATE_PLAYING
                    if (playing) controls?.pause() else controls?.play()
                }
            }
            return
        }
        super.onReceive(context, intent)
    }

    private fun read(context: Context): Now? {
        val controller = MediaListener.current(context) ?: return null
        val metadata = controller.metadata ?: return null
        val state = controller.playbackState
        val app = runCatching {
            val pm = context.packageManager
            pm.getApplicationLabel(pm.getApplicationInfo(controller.packageName, 0)).toString()
        }.getOrDefault(controller.packageName)
        // Position au moment du dessin, d'après la vitesse de lecture.
        val position = state?.let {
            if (it.state == PlaybackState.STATE_PLAYING) {
                it.position + ((SystemClock.elapsedRealtime() - it.lastPositionUpdateTime) * it.playbackSpeed).toLong()
            } else {
                it.position
            }
        } ?: 0L
        return Now(
            title = metadata.getString(MediaMetadata.METADATA_KEY_TITLE).orEmpty(),
            artist = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST)
                ?: metadata.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST).orEmpty(),
            app = app,
            art = metadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART) ?: metadata.getBitmap(MediaMetadata.METADATA_KEY_ART),
            playing = state?.state == PlaybackState.STATE_PLAYING,
            position = position,
            duration = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION),
        )
    }

    /** La pochette en néon : sa luminosité devient l'opacité, teintée par le widget. */
    private fun neon(art: Bitmap, size: Int): Bitmap {
        val argb = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(argb)
        // Luminance → opacité, relevée pour qu'une pochette sombre reste visible.
        val k = 215f / 255f
        val matrix = ColorMatrix(
            floatArrayOf(
                0f, 0f, 0f, 0f, 255f,
                0f, 0f, 0f, 0f, 255f,
                0f, 0f, 0f, 0f, 255f,
                0.3f * k, 0.59f * k, 0.11f * k, 0f, 40f,
            ),
        )
        val paint = Paint(Paint.FILTER_BITMAP_FLAG).apply { colorFilter = ColorMatrixColorFilter(matrix) }
        canvas.drawBitmap(art, null, RectF(0f, 0f, size.toFloat(), size.toFloat()), paint)
        val out = argb.extractAlpha()
        argb.recycle()
        return out
    }

    private fun action(context: Context, op: String) = android.app.PendingIntent.getBroadcast(
        context,
        op.hashCode(),
        Intent(context, MediaWidget::class.java).setAction(ACTION_MEDIA).putExtra(EXTRA_OP, op),
        android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT,
    )

    companion object {
        const val ACTION_MEDIA = "dev.levilainpetit.wux.action.MEDIA"
        private const val EXTRA_OP = "op"
        private const val OP_PREVIOUS = "previous"
        private const val OP_PLAY = "play"
        private const val OP_NEXT = "next"

        private val SAMPLE = Now("Midnight City", "M83", "Musique", null, true, 102_000L, 244_000L)
    }
}
