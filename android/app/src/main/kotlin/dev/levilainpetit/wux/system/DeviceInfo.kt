package dev.levilainpetit.wux.system

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.SystemClock
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.ceil

/** La fiche du téléphone, pour le widget Appareil. */
data class DeviceInfo(
    /** Nom d'hôte de la console : `pixel7`. */
    val host: String,
    val model: String,
    val android: String,
    val patch: String?,
    val chip: String,
    /** Mémoire vive, arrondie au Go supérieur. */
    val memoryGb: Int,
    /** Démarrage, dans la base de temps de `SystemClock.elapsedRealtime`. */
    val bootElapsed: Long,
    val uptimeMillis: Long,
) {
    companion object {
        /** Les puces Tensor, par leur nom de code. */
        private val CHIPS = mapOf(
            "GS101" to "Tensor",
            "GS201" to "Tensor G2",
            "ZUMA" to "Tensor G3",
            "ZUMAPRO" to "Tensor G4",
            "LAGUNA" to "Tensor G5",
        )

        fun read(context: Context): DeviceInfo {
            val memory = ActivityManager.MemoryInfo()
            context.getSystemService(ActivityManager::class.java)?.getMemoryInfo(memory)
            val code = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Build.SOC_MODEL else Build.HARDWARE
            val patch = runCatching {
                LocalDate.parse(Build.VERSION.SECURITY_PATCH)
                    .format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault()))
            }.getOrNull()
            val manufacturer = Build.MANUFACTURER.replaceFirstChar { it.titlecase(Locale.getDefault()) }
            return DeviceInfo(
                host = Build.MODEL.lowercase(Locale.ROOT).filter { it.isLetterOrDigit() }.ifEmpty { "android" },
                model = if (Build.MODEL.startsWith(manufacturer, ignoreCase = true)) Build.MODEL else "$manufacturer ${Build.MODEL}",
                android = "Android ${Build.VERSION.RELEASE} · API ${Build.VERSION.SDK_INT}",
                patch = patch,
                chip = CHIPS[code.uppercase(Locale.ROOT)] ?: code,
                memoryGb = ceil(memory.totalMem / 1_000_000_000.0).toInt(),
                bootElapsed = 0L,
                uptimeMillis = SystemClock.elapsedRealtime(),
            )
        }

        fun sample() = DeviceInfo(
            host = "pixel7",
            model = "Google Pixel 7",
            android = "Android 17 · API 37",
            patch = "5 sept. 2026",
            chip = "Tensor G2",
            memoryGb = 8,
            bootElapsed = 0L,
            uptimeMillis = (3 * 24 + 4) * 3_600_000L + 12 * 60_000L,
        )
    }
}
