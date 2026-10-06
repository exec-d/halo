package dev.levilainpetit.wux.theme

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import androidx.annotation.RequiresApi

/**
 * Les sons des thèmes : copiés dans les sons du téléphone (Sonneries/Halo,
 * Notifications/Halo, Alarmes/Halo), où ils restent au choix dans les
 * réglages d'Android, puis réglés comme sons par défaut. Android demande pour
 * cela l'autorisation « Modifier les paramètres système », une fois. Depuis
 * Android 10 seulement : avant, il faudrait l'accès au stockage.
 */
object ThemeSounds {

    enum class Kind(val id: String, val type: Int, val directory: String, val flag: String) {
        RING("ring", RingtoneManager.TYPE_RINGTONE, Environment.DIRECTORY_RINGTONES, MediaStore.Audio.AudioColumns.IS_RINGTONE),
        NOTIFICATION("notification", RingtoneManager.TYPE_NOTIFICATION, Environment.DIRECTORY_NOTIFICATIONS, MediaStore.Audio.AudioColumns.IS_NOTIFICATION),
        ALARM("alarm", RingtoneManager.TYPE_ALARM, Environment.DIRECTORY_ALARMS, MediaStore.Audio.AudioColumns.IS_ALARM),
        ;

        companion object {
            fun of(id: String) = entries.firstOrNull { it.id == id }
        }
    }

    val supported get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    fun canWrite(context: Context) = Settings.System.canWrite(context)

    /** L'écran d'Android qui accorde « Modifier les paramètres système » à Halo. */
    fun permissionIntent(context: Context) =
        Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:${context.packageName}"))

    /** Règle les sons [kinds] de [theme] ; rend ceux qui ont été réglés. */
    fun apply(context: Context, theme: HaloTheme, kinds: Collection<Kind>): List<Kind> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || !canWrite(context)) return emptyList()
        return kinds.mapNotNull { kind ->
            val (raw, title) = theme.sounds[kind] ?: return@mapNotNull null
            val uri = runCatching { install(context, "halo_${theme.id}_${kind.id}.wav", context.getString(title), raw, kind) }.getOrNull()
                ?: return@mapNotNull null
            runCatching { RingtoneManager.setActualDefaultRingtoneUri(context, kind.type, uri) }.getOrNull()
                ?.let { kind }
        }
    }

    /** Le son dans la médiathèque du téléphone : réutilisé s'il y est déjà. */
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun install(context: Context, file: String, title: String, raw: Int, kind: Kind): Uri? {
        val resolver = context.contentResolver
        val collection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val folder = "${kind.directory}/Halo/"
        resolver.query(
            collection,
            arrayOf(MediaStore.MediaColumns._ID),
            "${MediaStore.MediaColumns.DISPLAY_NAME} = ? AND ${MediaStore.MediaColumns.RELATIVE_PATH} = ?",
            arrayOf(file, folder),
            null,
        )?.use { cursor ->
            if (cursor.moveToFirst()) return Uri.withAppendedPath(collection, cursor.getLong(0).toString())
        }
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, file)
            put(MediaStore.MediaColumns.TITLE, title)
            put(MediaStore.MediaColumns.MIME_TYPE, "audio/wav")
            put(MediaStore.MediaColumns.RELATIVE_PATH, folder)
            put(kind.flag, true)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
        val uri = resolver.insert(collection, values) ?: return null
        try {
            resolver.openOutputStream(uri)?.use { out ->
                context.resources.openRawResource(raw).use { it.copyTo(out) }
            } ?: error("écriture impossible")
            resolver.update(uri, ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }, null, null)
        } catch (e: Exception) {
            resolver.delete(uri, null, null)
            throw e
        }
        return uri
    }

    private var player: MediaPlayer? = null

    /** Joue un son de thème dans l'application (aperçu). */
    fun preview(context: Context, theme: HaloTheme, kind: Kind) {
        stop()
        val raw = theme.sounds[kind]?.first ?: return
        player = MediaPlayer.create(context.applicationContext, raw)?.apply {
            setOnCompletionListener { this@ThemeSounds.stop() }
            start()
        }
    }

    fun stop() {
        player?.release()
        player = null
    }
}
