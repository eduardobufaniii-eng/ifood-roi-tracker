package com.ifoodroitracker.service

import com.ifoodroitracker.data.entity.DeliveryOffer
import com.ifoodroitracker.data.entity.ParsedOffer
import com.ifoodroitracker.data.entity.Thresholds
import java.util.Calendar

/**
 * Motor de decisão: avalia se uma oferta vale a pena baseada nos thresholds do usuário.
 */
object DecisionEngine {

    data class DecisionResult(
        val verdict: Verdict,
        val valuePerKm: Double,
        val valuePerHour: Double,
        val adjustedMinValuePerKm: Double,
        val adjustedMinValuePerHour: Double,
        val reasons: List<String>
    ) {
        override fun toString(): String {
            return "DecisionResult(verdict=$verdict, R$/km=$valuePerKm, R$/h=$valuePerHour, reasons=$reasons)"
        }
    }

    enum class Verdict(val label: String, val colorRes: Int, val description: String) {
        WORTH_IT("VALE A PENA ✅", 0, "Aceitar - atende todos os critérios"),
        BORDERLINE("AVALIAR ⚠️", 0, "Borda - atende alguns critérios, avalie manualmente"),
        NOT_WORTH("NÃO VALE ❌", 0, "Recusar - não atende critérios mínimos")
    }

    fun evaluate(
        offer: ParsedOffer,
        thresholds: Thresholds,
        currentHour: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    ): DecisionResult {
        val valuePerKm = if (offer.distanceKm > 0) offer.value / offer.distanceKm else 0.0
        val valuePerHour = if (offer.estimatedMinutes > 0) (offer.value / offer.estimatedMinutes) * 60.0 else 0.0

        val isPeak = thresholds.peakHours.any { it.contains(currentHour) }
        val adjustedMinValuePerKm = thresholds.minValuePerKm * (if (isPeak) thresholds.peakMultiplier else 1.0)
        val adjustedMinValuePerHour = thresholds.minValuePerHour * (if (isPeak) thresholds.peakMultiplier else 1.0)

        val reasons = mutableListOf<String>()

        // Critérios
        val meetsValuePerKm = valuePerKm >= adjustedMinValuePerKm
        val meetsValuePerHour = valuePerHour >= adjustedMinValuePerHour
        val meetsDistance = offer.distanceKm <= thresholds.maxDistanceKm
        val meetsMinTotal = offer.value >= thresholds.minTotalValue

        if (meetsValuePerKm) reasons.add("R$/km (${String.format("%.2f", valuePerKm)}) ≥ mínimo (${String.format("%.2f", adjustedMinValuePerKm)})")
        else reasons.add("R$/km (${String.format("%.2f", valuePerKm)}) < mínimo (${String.format("%.2f", adjustedMinValuePerKm)})")

        if (meetsValuePerHour) reasons.add("R$/h (${String.format("%.2f", valuePerHour)}) ≥ mínimo (${String.format("%.2f", adjustedMinValuePerHour)})")
        else reasons.add("R$/h (${String.format("%.2f", valuePerHour)}) < mínimo (${String.format("%.2f", adjustedMinValuePerHour)})")

        if (meetsDistance) reasons.add("Distância (${String.format("%.1f", offer.distanceKm)}km) ≤ máxima (${thresholds.maxDistanceKm}km)")
        else reasons.add("Distância (${String.format("%.1f", offer.distanceKm)}km) > máxima (${thresholds.maxDistanceKm}km)")

        if (meetsMinTotal) reasons.add("Valor total (R$${String.format("%.2f", offer.value)}) ≥ mínimo (R$${String.format("%.2f", thresholds.minTotalValue)})")
        else reasons.add("Valor total (R$${String.format("%.2f", offer.value)}) < mínimo (R$${String.format("%.2f", thresholds.minTotalValue)})")

        if (isPeak) reasons.add("🌟 Horário de pico ativo (multiplicador ${String.format("%.1f", thresholds.peakMultiplier)}x)")

        // Decisão final
        val passedCriteria = listOf(meetsValuePerKm, meetsValuePerHour, meetsDistance, meetsMinTotal).count { it }
        val verdict = when {
            passedCriteria == 4 -> Verdict.WORTH_IT
            passedCriteria >= 2 -> Verdict.BORDERLINE
            else -> Verdict.NOT_WORTH
        }

        return DecisionResult(
            verdict = verdict,
            valuePerKm = valuePerKm,
            valuePerHour = valuePerHour,
            adjustedMinValuePerKm = adjustedMinValuePerKm,
            adjustedMinValuePerHour = adjustedMinValuePerHour,
            reasons = reasons
        )
    }
}