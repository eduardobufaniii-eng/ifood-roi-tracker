package com.ifoodroitracker.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.datastore.preferences.core.preferencesKey
import androidx.datastore.preferences.rxjava2.RxPreferenceDataStoreBuilder
import androidx.datastore.rxjava2.RxDataStore
import com.ifoodroitracker.data.entity.PeakHour
import com.ifoodroitracker.data.entity.Thresholds
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesRepository @Inject constructor(
    private val context: Context
) {
    private val dataStore: RxDataStore<Preferences> = RxPreferenceDataStoreBuilder(context, "settings").build()
    private val gson = Gson()

    // Keys
    private val KEY_MIN_VALUE_PER_KM = doublePreferencesKey("min_value_per_km")
    private val KEY_MIN_VALUE_PER_HOUR = doublePreferencesKey("min_value_per_hour")
    private val KEY_MAX_DISTANCE_KM = intPreferencesKey("max_distance_km")
    private val KEY_MIN_TOTAL_VALUE = doublePreferencesKey("min_total_value")
    private val KEY_PEAK_MULTIPLIER = doublePreferencesKey("peak_multiplier")
    private val KEY_PEAK_HOURS = preferencesKey<String>("peak_hours")
    private val KEY_AUTO_DISMISS_SECONDS = intPreferencesKey("auto_dismiss_seconds")
    private val KEY_VIBRATE_ON_DECISION = preferencesKey<Boolean>("vibrate_on_decision")
    private val KEY_PLAY_SOUND_ON_DECISION = preferencesKey<Boolean>("play_sound_on_decision")
    private val KEY_SHOW_ON_LOCKSCREEN = preferencesKey<Boolean>("show_on_lockscreen")
    private val KEY_ACCESSIBILITY_ENABLED = preferencesKey<Boolean>("accessibility_enabled")
    private val KEY_OVERLAY_ENABLED = preferencesKey<Boolean>("overlay_enabled")
    private val KEY_LAST_KNOWN_OFFER_TIME = longPreferencesKey("last_known_offer_time")

    // Defaults
    private val DEFAULT_THRESHOLDS = Thresholds()

    suspend fun getThresholds(): Thresholds = dataStore.data
        .map { prefs ->
            val peakHoursJson = prefs[KEY_PEAK_HOURS] ?: gson.toJson(DEFAULT_THRESHOLDS.peakHours)
            val peakHours = try {
                gson.fromJson(peakHoursJson, PeakHourListType)
            } catch (e: Exception) {
                DEFAULT_THRESHOLDS.peakHours
            }

            Thresholds(
                minValuePerKm = prefs[KEY_MIN_VALUE_PER_KM] ?: DEFAULT_THRESHOLDS.minValuePerKm,
                minValuePerHour = prefs[KEY_MIN_VALUE_PER_HOUR] ?: DEFAULT_THRESHOLDS.minValuePerHour,
                maxDistanceKm = prefs[KEY_MAX_DISTANCE_KM] ?: DEFAULT_THRESHOLDS.maxDistanceKm,
                minTotalValue = prefs[KEY_MIN_TOTAL_VALUE] ?: DEFAULT_THRESHOLDS.minTotalValue,
                peakMultiplier = prefs[KEY_PEAK_MULTIPLIER] ?: DEFAULT_THRESHOLDS.peakMultiplier,
                peakHours = peakHours,
                autoDismissSeconds = prefs[KEY_AUTO_DISMISS_SECONDS] ?: DEFAULT_THRESHOLDS.autoDismissSeconds,
                vibrateOnDecision = prefs[KEY_VIBRATE_ON_DECISION] ?: DEFAULT_THRESHOLDS.vibrateOnDecision,
                playSoundOnDecision = prefs[KEY_PLAY_SOUND_ON_DECISION] ?: DEFAULT_THRESHOLDS.playSoundOnDecision,
                showOnLockscreen = prefs[KEY_SHOW_ON_LOCKSCREEN] ?: DEFAULT_THRESHOLDS.showOnLockscreen
            )
        }
        .firstOrNull() ?: DEFAULT_THRESHOLDS

    suspend fun saveThresholds(thresholds: Thresholds) {
        dataStore.updateDataAsync { prefs ->
            prefs.toMutablePreferences().apply {
                putDouble(KEY_MIN_VALUE_PER_KM, thresholds.minValuePerKm)
                putDouble(KEY_MIN_VALUE_PER_HOUR, thresholds.minValuePerHour)
                putInt(KEY_MAX_DISTANCE_KM, thresholds.maxDistanceKm)
                putDouble(KEY_MIN_TOTAL_VALUE, thresholds.minTotalValue)
                putDouble(KEY_PEAK_MULTIPLIER, thresholds.peakMultiplier)
                putString(KEY_PEAK_HOURS, gson.toJson(thresholds.peakHours))
                putInt(KEY_AUTO_DISMISS_SECONDS, thresholds.autoDismissSeconds)
                putBoolean(KEY_VIBRATE_ON_DECISION, thresholds.vibrateOnDecision)
                putBoolean(KEY_PLAY_SOUND_ON_DECISION, thresholds.playSoundOnDecision)
                putBoolean(KEY_SHOW_ON_LOCKSCREEN, thresholds.showOnLockscreen)
            }
        }.await()
    }

    fun observeThresholds(): Flowable<Thresholds> = dataStore.data
        .map { prefs ->
            val peakHoursJson = prefs[KEY_PEAK_HOURS] ?: gson.toJson(DEFAULT_THRESHOLDS.peakHours)
            val peakHours = try {
                gson.fromJson(peakHoursJson, PeakHourListType)
            } catch (e: Exception) {
                DEFAULT_THRESHOLDS.peakHours
            }

            Thresholds(
                minValuePerKm = prefs[KEY_MIN_VALUE_PER_KM] ?: DEFAULT_THRESHOLDS.minValuePerKm,
                minValuePerHour = prefs[KEY_MIN_VALUE_PER_HOUR] ?: DEFAULT_THRESHOLDS.minValuePerHour,
                maxDistanceKm = prefs[KEY_MAX_DISTANCE_KM] ?: DEFAULT_THRESHOLDS.maxDistanceKm,
                minTotalValue = prefs[KEY_MIN_TOTAL_VALUE] ?: DEFAULT_THRESHOLDS.minTotalValue,
                peakMultiplier = prefs[KEY_PEAK_MULTIPLIER] ?: DEFAULT_THRESHOLDS.peakMultiplier,
                peakHours = peakHours,
                autoDismissSeconds = prefs[KEY_AUTO_DISMISS_SECONDS] ?: DEFAULT_THRESHOLDS.autoDismissSeconds,
                vibrateOnDecision = prefs[KEY_VIBRATE_ON_DECISION] ?: DEFAULT_THRESHOLDS.vibrateOnDecision,
                playSoundOnDecision = prefs[KEY_PLAY_SOUND_ON_DECISION] ?: DEFAULT_THRESHOLDS.playSoundOnDecision,
                showOnLockscreen = prefs[KEY_SHOW_ON_LOCKSCREEN] ?: DEFAULT_THRESHOLDS.showOnLockscreen
            )
        }

    suspend fun setAccessibilityEnabled(enabled: Boolean) {
        dataStore.updateDataAsync { it.toMutablePreferences().apply { putBoolean(KEY_ACCESSIBILITY_ENABLED, enabled) } }.await()
    }

    fun observeAccessibilityEnabled(): Flowable<Boolean> = dataStore.data
        .map { it[KEY_ACCESSIBILITY_ENABLED] ?: false }

    suspend fun setOverlayEnabled(enabled: Boolean) {
        dataStore.updateDataAsync { it.toMutablePreferences().apply { putBoolean(KEY_OVERLAY_ENABLED, enabled) } }.await()
    }

    fun observeOverlayEnabled(): Flowable<Boolean> = dataStore.data
        .map { it[KEY_OVERLAY_ENABLED] ?: false }

    suspend fun setLastKnownOfferTime(time: Long) {
        dataStore.updateDataAsync { it.toMutablePreferences().apply { putLong(KEY_LAST_KNOWN_OFFER_TIME, time) } }.await()
    }

    fun observeLastKnownOfferTime(): Flowable<Long> = dataStore.data
        .map { it[KEY_LAST_KNOWN_OFFER_TIME] ?: 0L }

    // Export/Import
    suspend fun exportAll(): String {
        val prefs = dataStore.data.firstOrNull()?.toMutablePreferences() ?: mutablePreferencesOf()
        return gson.toJson(prefs.toMap())
    }

    suspend fun importAll(json: String) {
        val map = gson.fromJson(json, SettingsMapType)
        dataStore.updateDataAsync { prefs ->
            val mutable = prefs.toMutablePreferences()
            map.forEach { (key, value) ->
                when (value) {
                    is Double -> mutable.putDouble(key, value)
                    is Long -> mutable.putLong(key, value)
                    is Int -> mutable.putInt(key, value)
                    is Boolean -> mutable.putBoolean(key, value)
                    is String -> mutable.putString(key, value)
                }
            }
            mutable
        }.await()
    }

    companion object {
        private val PeakHourListType = object : TypeToken<List<PeakHour>>() {}.type
        private val SettingsMapType = object : TypeToken<Map<String, Any>>() {}.type
    }
}