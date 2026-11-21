package com.ifoodroitracker.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ifoodroitracker.data.repository.DeliveryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    application: Application,
    private val deliveryRepo: DeliveryRepository
) : AndroidViewModel(application) {

    private val _period = MutableLiveData<Period>()
    val period: LiveData<Period> = _period

    private val _stats = MutableLiveData<PeriodStatsUi?>()
    val stats: LiveData<PeriodStatsUi?> = _stats

    enum class Period(val label: String, val days: Int) {
        TODAY("Hoje", 0),
        WEEK("Semana", 7),
        MONTH("Mês", 30),
        ALL("Tudo", 9999)
    }

    init {
        _period.value = Period.TODAY
        loadStats()
    }

    fun setPeriod(period: Period) {
        _period.value = period
        loadStats()
    }

    private fun loadStats() {
        _period.value?.let { period ->
            val startTime = when (period) {
                Period.TODAY -> getStartOfDay()
                Period.WEEK -> System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000)
                Period.MONTH -> System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
                Period.ALL -> 0L
            }

            CoroutineScope(Dispatchers.IO).launch {
                val stats = deliveryRepo.getPeriodStats(startTime).firstOrNull()
                _stats.postValue(stats?.let { PeriodStatsUi.from(it, period) })
            }
        }
    }

    private fun getStartOfDay(): Long {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    data class PeriodStatsUi(
        val totalDeliveries: Int,
        val totalEarnings: Double,
        val avgValuePerKm: Double,
        val avgValuePerHour: Double,
        val byNeighborhood: List<NeighborhoodUi>,
        val byHour: List<HourUi>,
        val byWeekday: List<WeekdayUi>
    ) {
        companion object {
            fun from(stats: com.ifoodroitracker.data.repository.PeriodStats, period: Period): PeriodStatsUi {
                return PeriodStatsUi(
                    totalDeliveries = stats.totalDeliveries,
                    totalEarnings = stats.totalEarnings,
                    avgValuePerKm = stats.avgValuePerKm,
                    avgValuePerHour = stats.avgValuePerHour,
                    byNeighborhood = stats.byNeighborhood.map { NeighborhoodUi(it.neighborhood, it.count, it.avgValue, it.avgPerKm) },
                    byHour = stats.byHour.map { HourUi(it.hour, it.count, it.avgValue, it.avgPerKm) },
                    byWeekday = stats.byWeekday.map { WeekdayUi(it.weekday, it.count, it.avgValue, it.avgPerKm) }
                )
            }
        }
    }

    data class NeighborhoodUi(
        val name: String,
        val count: Int,
        val avgValue: Double,
        val avgPerKm: Double
    )

    data class HourUi(
        val hour: Int,
        val count: Int,
        val avgValue: Double,
        val avgPerKm: Double
    )

    data class WeekdayUi(
        val weekday: Int, // 0 = Domingo
        val count: Int,
        val avgValue: Double,
        val avgPerKm: Double
    ) {
        val name: String
            get() = when (weekday) {
                0 -> "Dom"
                1 -> "Seg"
                2 -> "Ter"
                3 -> "Qua"
                4 -> "Qui"
                5 -> "Sex"
                6 -> "Sáb"
                else -> ""
            }
    }
}