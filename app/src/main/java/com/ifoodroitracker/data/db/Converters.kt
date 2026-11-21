package com.ifoodroitracker.data.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ifoodroitracker.data.entity.DeliveryOffer
import com.ifoodroitracker.data.entity.PeakHour
import com.ifoodroitracker.data.entity.Thresholds

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromThresholdsList(value: String?): List<PeakHour> {
        return value?.let { gson.fromJson(it, PeakHourListType) } ?: emptyList()
    }

    @TypeConverter
    fun toThresholdsList(list: List<PeakHour>?): String {
        return gson.toJson(list ?: emptyList())
    }

    @TypeConverter
    fun fromDecision(value: Int): DeliveryOffer.Decision {
        return DeliveryOffer.Decision.values().firstOrNull { it.ordinal == value } ?: DeliveryOffer.Decision.PENDING
    }

    @TypeConverter
    fun toDecision(decision: DeliveryOffer.Decision): Int {
        return decision.ordinal
    }
}

private val PeakHourListType = object : TypeToken<List<PeakHour>>() {}.type