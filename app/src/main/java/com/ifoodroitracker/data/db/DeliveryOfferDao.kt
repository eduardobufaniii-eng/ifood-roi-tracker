package com.ifoodroitracker.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ifoodroitracker.data.entity.DeliveryOffer
import kotlinx.coroutines.flow.Flow

@Dao
interface DeliveryOfferDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(offer: DeliveryOffer): Long

    @Update
    suspend fun update(offer: DeliveryOffer): Int

    @Query("SELECT * FROM delivery_offers ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentOffers(limit: Int): Flow<List<DeliveryOffer>>

    @Query("SELECT * FROM delivery_offers ORDER BY timestamp DESC")
    fun getAllOffers(): Flow<List<DeliveryOffer>>

    @Query("SELECT * FROM delivery_offers WHERE decision = :decision ORDER BY timestamp DESC")
    fun getOffersByDecision(decision: DeliveryOffer.Decision): Flow<List<DeliveryOffer>>

    @Query("SELECT * FROM delivery_offers WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    fun getOffersInRange(startTime: Long, endTime: Long): Flow<List<DeliveryOffer>>

    @Query("SELECT COUNT(*) FROM delivery_offers WHERE timestamp >= :startTime")
    fun countOffersSince(startTime: Long): Flow<Int>

    @Query("SELECT SUM(rawValue) FROM delivery_offers WHERE decision = 0 AND timestamp >= :startTime")
    fun sumEarningsSince(startTime: Long): Flow<Double?>

    @Query("SELECT AVG(valuePerKm) FROM delivery_offers WHERE decision = 0 AND timestamp >= :startTime AND rawDistanceKm > 0")
    fun avgValuePerKmSince(startTime: Long): Flow<Double?>

    @Query("SELECT AVG(valuePerHour) FROM delivery_offers WHERE decision = 0 AND timestamp >= :startTime AND rawEstimatedMinutes > 0")
    fun avgValuePerHourSince(startTime: Long): Flow<Double?>

    @Query("DELETE FROM delivery_offers WHERE timestamp < :beforeTime")
    suspend fun deleteOldOffers(beforeTime: Long): Int

    @Query("DELETE FROM delivery_offers")
    suspend fun deleteAll(): Int

    // Estatísticas por bairro
    @Query("""
        SELECT rawOrigin as neighborhood, COUNT(*) as count, AVG(rawValue) as avgValue, AVG(valuePerKm) as avgPerKm
        FROM delivery_offers
        WHERE decision = 0 AND timestamp >= :startTime AND rawOrigin IS NOT NULL
        GROUP BY rawOrigin
        ORDER BY count DESC
    """)
    fun getStatsByNeighborhood(startTime: Long): Flow<List<NeighborhoodStats>>

    // Estatísticas por hora
    @Query("""
        SELECT CAST(strftime('%H', timestamp/1000, 'unixepoch', 'localtime') AS INTEGER) as hour,
               COUNT(*) as count,
               AVG(rawValue) as avgValue,
               AVG(valuePerKm) as avgPerKm
        FROM delivery_offers
        WHERE decision = 0 AND timestamp >= :startTime
        GROUP BY hour
        ORDER BY hour
    """)
    fun getStatsByHour(startTime: Long): Flow<List<HourlyStats>>

    // Estatísticas por dia da semana
    @Query("""
        SELECT CAST(strftime('%w', timestamp/1000, 'unixepoch', 'localtime') AS INTEGER) as weekday,
               COUNT(*) as count,
               AVG(rawValue) as avgValue,
               AVG(valuePerKm) as avgPerKm
        FROM delivery_offers
        WHERE decision = 0 AND timestamp >= :startTime
        GROUP BY weekday
        ORDER BY weekday
    """)
    fun getStatsByWeekday(startTime: Long): Flow<List<WeekdayStats>>
}

data class NeighborhoodStats(
    val neighborhood: String,
    val count: Int,
    val avgValue: Double,
    val avgPerKm: Double
)

data class HourlyStats(
    val hour: Int,
    val count: Int,
    val avgValue: Double,
    val avgPerKm: Double
)

data class WeekdayStats(
    val weekday: Int, // 0 = Domingo
    val count: Int,
    val avgValue: Double,
    val avgPerKm: Double
)