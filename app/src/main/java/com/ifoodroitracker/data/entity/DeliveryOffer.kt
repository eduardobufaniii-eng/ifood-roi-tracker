package com.ifoodroitracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.ifoodroitracker.data.db.Converters
import java.time.Instant

@Entity(tableName = "delivery_offers")
data class DeliveryOffer(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Dados brutos da oferta
    val rawValue: Double,
    val rawDistanceKm: Double,
    val rawEstimatedMinutes: Int,
    val rawOrigin: String?,
    val rawDestination: String?,

    // Cálculos
    val valuePerKm: Double,
    val valuePerHour: Double,

    // Decisão do usuário
    val decision: Decision = Decision.PENDING,

    // Metadados
    val timestamp: Long = System.currentTimeMillis(),
    val isPeakHour: Boolean = false,
    val peakMultiplierApplied: Double = 1.0,

    // Thresholds usados na decisão (para auditoria)
    val thresholdMinValuePerKm: Double = 0.0,
    val thresholdMinValuePerHour: Double = 0.0,
    val thresholdMaxDistance: Double = 0.0,
    val thresholdMinTotalValue: Double = 0.0,
) {
    enum class Decision(val label: String, val colorRes: Int) {
        ACCEPTED("Aceita", 0),
        REJECTED("Recusada", 0),
        IGNORED("Ignorada", 0),
        PENDING("Pendente", 0)
    }

    companion object {
        fun createFromParsed(
            parsed: ParsedOffer,
            thresholds: Thresholds,
            isPeak: Boolean,
            peakMultiplier: Double
        ): DeliveryOffer {
            val valuePerKm = if (parsed.distanceKm > 0) parsed.value / parsed.distanceKm else 0.0
            val valuePerHour = if (parsed.estimatedMinutes > 0) (parsed.value / parsed.estimatedMinutes) * 60.0 else 0.0

            return DeliveryOffer(
                rawValue = parsed.value,
                rawDistanceKm = parsed.distanceKm,
                rawEstimatedMinutes = parsed.estimatedMinutes,
                rawOrigin = parsed.origin,
                rawDestination = parsed.destination,
                valuePerKm = valuePerKm,
                valuePerHour = valuePerHour,
                isPeakHour = isPeak,
                peakMultiplierApplied = peakMultiplier,
                thresholdMinValuePerKm = thresholds.minValuePerKm,
                thresholdMinValuePerHour = thresholds.minValuePerHour,
                thresholdMaxDistance = thresholds.maxDistanceKm.toDouble(),
                thresholdMinTotalValue = thresholds.minTotalValue
            )
        }
    }
}

@TypeConverters(Converters::class)
data class ParsedOffer(
    val value: Double,
    val distanceKm: Double,
    val estimatedMinutes: Int,
    val origin: String?,
    val destination: String?,
    val confidence: Double = 1.0
)

data class Thresholds(
    val minValuePerKm: Double = 4.0,
    val minValuePerHour: Double = 55.0,
    val maxDistanceKm: Int = 15,
    val minTotalValue: Double = 10.0,
    val peakMultiplier: Double = 1.2,
    val peakHours: List<PeakHour> = listOf(
        PeakHour(11, 14),
        PeakHour(18, 21)
    ),
    val autoDismissSeconds: Int = 15,
    val vibrateOnDecision: Boolean = true,
    val playSoundOnDecision: Boolean = false,
    val showOnLockscreen: Boolean = true
)

data class PeakHour(val startHour: Int, val endHour: Int) {
    fun contains(hour: Int): Boolean = hour >= startHour && hour < endHour
}