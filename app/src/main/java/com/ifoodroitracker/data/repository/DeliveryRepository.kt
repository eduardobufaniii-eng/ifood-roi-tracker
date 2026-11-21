package com.ifoodroitracker.data.repository

import com.ifoodroitracker.data.db.AppDatabase
import com.ifoodroitracker.data.db.DeliveryOfferDao
import com.ifoodroitracker.data.entity.DeliveryOffer
import com.ifoodroitracker.data.prefs.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeliveryRepository @Inject constructor(
    private val dao: DeliveryOfferDao,
    private val prefs: PreferencesRepository
) {

    fun getRecentOffers(limit: Int = 50): Flow<List<DeliveryOffer>> = dao.getRecentOffers(limit)

    fun getAllOffers(): Flow<List<DeliveryOffer>> = dao.getAllOffers()

    fun getOffersByDecision(decision: DeliveryOffer.Decision): Flow<List<DeliveryOffer>> = dao.getOffersByDecision(decision)

    fun getOffersInRange(startTime: Long, endTime: Long): Flow<List<DeliveryOffer>> = dao.getOffersInRange(startTime, endTime)

    suspend fun insertOffer(offer: DeliveryOffer): Long = dao.insert(offer)

    suspend fun updateOfferDecision(id: Long, decision: DeliveryOffer.Decision) {
        dao.getAllOffers().firstOrNull()?.let { list ->
            list.firstOrNull { it.id == id }?.let { offer ->
                dao.update(offer.copy(decision = decision))
            }
        }
    }

    // Hoje
    fun getTodayStats(): Flow<TodayStats> {
        val startOfDay = getStartOfDay()
        return combine(
            dao.countOffersSince(startOfDay),
            dao.sumEarningsSince(startOfDay),
            dao.avgValuePerKmSince(startOfDay),
            dao.avgValuePerHourSince(startOfDay)
        ) { count, earnings, avgKm, avgHour ->
            TodayStats(
                count = count,
                totalEarnings = earnings ?: 0.0,
                avgValuePerKm = avgKm ?: 0.0,
                avgValuePerHour = avgHour ?: 0.0
            )
        }
    }

    // Período customizado
    fun getPeriodStats(startTime: Long): Flow<PeriodStats> {
        return combine(
            dao.countOffersSince(startTime),
            dao.sumEarningsSince(startTime),
            dao.avgValuePerKmSince(startTime),
            dao.avgValuePerHourSince(startTime),
            dao.getStatsByNeighborhood(startTime),
            dao.getStatsByHour(startTime),
            dao.getStatsByWeekday(startTime)
        ) { count, earnings, avgKm, avgHour, byNeighborhood, byHour, byWeekday ->
            PeriodStats(
                totalDeliveries = count,
                totalEarnings = earnings ?: 0.0,
                avgValuePerKm = avgKm ?: 0.0,
                avgValuePerHour = avgHour ?: 0.0,
                byNeighborhood = byNeighborhood,
                byHour = byHour,
                byWeekday = byWeekday
            )
        }
    }

    suspend fun deleteOldOffers(days: Int = 90) {
        val beforeTime = System.currentTimeMillis() - (days * 24L * 60 * 60 * 1000)
        dao.deleteOldOffers(beforeTime)
    }

    suspend fun clearAllHistory() = dao.deleteAll()

    // Export CSV
    suspend fun exportToCsv(startTime: Long, endTime: Long): List<DeliveryOffer> {
        return dao.getOffersInRange(startTime, endTime).firstOrNull() ?: emptyList()
    }

    private fun getStartOfDay(): Long {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}

data class TodayStats(
    val count: Int,
    val totalEarnings: Double,
    val avgValuePerKm: Double,
    val avgValuePerHour: Double
)

data class PeriodStats(
    val totalDeliveries: Int,
    val totalEarnings: Double,
    val avgValuePerKm: Double,
    val avgValuePerHour: Double,
    val byNeighborhood: List<com.ifoodroitracker.data.db.NeighborhoodStats>,
    val byHour: List<com.ifoodroitracker.data.db.HourlyStats>,
    val byWeekday: List<com.ifoodroitracker.data.db.WeekdayStats>
)