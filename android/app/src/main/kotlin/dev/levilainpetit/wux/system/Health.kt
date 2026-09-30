package dev.levilainpetit.wux.system

import android.content.Context
import android.content.Intent
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateGroupByPeriodRequest
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import es.antonborri.home_widget.HomeWidgetPlugin
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId

/** Une phase de sommeil : de, à (millisecondes), et sa nature. */
data class SleepStage(val start: Long, val end: Long, val kind: Kind) {
    enum class Kind { DEEP, LIGHT, REM, AWAKE, SLEEPING }
}

/** La dernière nuit : coucher, lever, phases. */
data class Sleep(val start: Long, val end: Long, val stages: List<SleepStage>)

/** Les pas des sept derniers jours (le dernier : aujourd'hui) et la dernière nuit. */
data class HealthDay(val date: LocalDate, val steps: Long)

data class HealthSnapshot(val days: List<HealthDay>, val sleep: Sleep?, val updatedAt: Long) {
    val today: Long get() = days.lastOrNull()?.steps ?: 0
}

/**
 * Pas et sommeil par Santé Connect. Lu par la tâche de fond du système (toutes
 * les 15 minutes) et à l'ouverture de Halo, gardé dans les réglages partagés :
 * le widget ne fait que dessiner ce qui est gardé.
 */
object Health {

    private const val SNAPSHOT = "health.snapshot"

    val PERMISSIONS = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        // Sans elle, Santé Connect ne répond qu'à une appli au premier plan.
        HealthPermission.PERMISSION_READ_HEALTH_DATA_IN_BACKGROUND,
    )

    private val DATA = PERMISSIONS - HealthPermission.PERMISSION_READ_HEALTH_DATA_IN_BACKGROUND

    fun available(context: Context) = HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE

    /** La demande d'autorisations de Santé Connect, à lancer par startActivityForResult. */
    fun requestIntent(context: Context): Intent =
        PermissionController.createRequestPermissionResultContract().createIntent(context, PERMISSIONS)

    suspend fun granted(context: Context): Boolean {
        if (!available(context)) return false
        return runCatching {
            HealthConnectClient.getOrCreate(context).permissionController.getGrantedPermissions().containsAll(DATA)
        }.getOrDefault(false)
    }

    /** Lit Santé Connect et garde le résultat ; faux si rien n'a pu être lu. */
    suspend fun refresh(context: Context): Boolean {
        if (!granted(context)) return false
        return runCatching {
            val client = HealthConnectClient.getOrCreate(context)
            val zone = ZoneId.systemDefault()
            val today = LocalDate.now(zone)
            val from = today.minusDays(6).atStartOfDay()
            val groups = client.aggregateGroupByPeriod(
                AggregateGroupByPeriodRequest(
                    metrics = setOf(StepsRecord.COUNT_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(from, today.plusDays(1).atStartOfDay()),
                    timeRangeSlicer = Period.ofDays(1),
                ),
            )
            val byDay = groups.associate { it.startTime.toLocalDate() to (it.result[StepsRecord.COUNT_TOTAL] ?: 0L) }
            val days = (6 downTo 0).map { back -> today.minusDays(back.toLong()).let { HealthDay(it, byDay[it] ?: 0L) } }
            // Le total du jour, au plus juste (l'agrégat par période peut arrondir aux bornes).
            val todaySteps = client.aggregate(
                AggregateRequest(setOf(StepsRecord.COUNT_TOTAL), TimeRangeFilter.between(today.atStartOfDay(zone).toInstant(), Instant.now())),
            )[StepsRecord.COUNT_TOTAL] ?: days.last().steps
            val now = Instant.now()
            val sessions = client.readRecords(
                ReadRecordsRequest(SleepSessionRecord::class, TimeRangeFilter.between(now.minusSeconds(36 * 3600L), now)),
            ).records
            val last = sessions.maxByOrNull { it.endTime }
            val sleep = last?.let { session ->
                Sleep(
                    session.startTime.toEpochMilli(),
                    session.endTime.toEpochMilli(),
                    session.stages.mapNotNull { stage ->
                        val kind = when (stage.stage) {
                            SleepSessionRecord.STAGE_TYPE_DEEP -> SleepStage.Kind.DEEP
                            SleepSessionRecord.STAGE_TYPE_LIGHT -> SleepStage.Kind.LIGHT
                            SleepSessionRecord.STAGE_TYPE_REM -> SleepStage.Kind.REM
                            SleepSessionRecord.STAGE_TYPE_AWAKE,
                            SleepSessionRecord.STAGE_TYPE_AWAKE_IN_BED,
                            SleepSessionRecord.STAGE_TYPE_OUT_OF_BED -> SleepStage.Kind.AWAKE
                            SleepSessionRecord.STAGE_TYPE_SLEEPING -> SleepStage.Kind.SLEEPING
                            else -> null
                        }
                        kind?.let { SleepStage(stage.startTime.toEpochMilli(), stage.endTime.toEpochMilli(), it) }
                    },
                )
            }
            save(context, HealthSnapshot(days.dropLast(1) + HealthDay(today, maxOf(todaySteps, days.last().steps)), sleep, System.currentTimeMillis()))
            true
        }.getOrDefault(false)
    }

    fun snapshot(context: Context): HealthSnapshot? {
        val json = HomeWidgetPlugin.getData(context).getString(SNAPSHOT, null) ?: return null
        return runCatching {
            val root = JSONObject(json)
            val days = root.getJSONArray("days").let { a ->
                List(a.length()) { i -> a.getJSONObject(i).let { HealthDay(LocalDate.parse(it.getString("date")), it.getLong("steps")) } }
            }
            val sleep = root.optJSONObject("sleep")?.let { s ->
                val stages = s.getJSONArray("stages")
                Sleep(
                    s.getLong("start"),
                    s.getLong("end"),
                    List(stages.length()) { i ->
                        stages.getJSONArray(i).let { SleepStage(it.getLong(0), it.getLong(1), SleepStage.Kind.valueOf(it.getString(2))) }
                    },
                )
            }
            HealthSnapshot(days, sleep, root.getLong("updated"))
        }.getOrNull()
    }

    private fun save(context: Context, snapshot: HealthSnapshot) {
        val root = JSONObject()
            .put("updated", snapshot.updatedAt)
            .put("days", JSONArray().apply { snapshot.days.forEach { put(JSONObject().put("date", it.date.toString()).put("steps", it.steps)) } })
        snapshot.sleep?.let { sleep ->
            root.put(
                "sleep",
                JSONObject().put("start", sleep.start).put("end", sleep.end).put(
                    "stages",
                    JSONArray().apply { sleep.stages.forEach { put(JSONArray().put(it.start).put(it.end).put(it.kind.name)) } },
                ),
            )
        }
        HomeWidgetPlugin.getData(context).edit().putString(SNAPSHOT, root.toString()).apply()
    }
}
