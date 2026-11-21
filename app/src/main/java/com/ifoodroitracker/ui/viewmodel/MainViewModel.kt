package com.ifoodroitracker.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.ifoodroitracker.data.entity.DeliveryOffer
import com.ifoodroitracker.data.entity.Thresholds
import com.ifoodroitracker.data.prefs.PreferencesRepository
import com.ifoodroitracker.data.repository.DeliveryRepository
import com.ifoodroitracker.IfoodRoiApplication
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    application: Application,
    private val prefsRepo: PreferencesRepository,
    private val deliveryRepo: DeliveryRepository
) : AndroidViewModel(application) {

    // State observables
    private val _thresholds = MutableLiveData<Thresholds>()
    val thresholds: LiveData<Thresholds> = _thresholds

    private val _todayStats = MutableLiveData<TodayStatsUi>()
    val todayStats: LiveData<TodayStatsUi> = _todayStats

    private val _accessibilityEnabled = MutableLiveData<Boolean>()
    val accessibilityEnabled: LiveData<Boolean> = _accessibilityEnabled

    private val _overlayEnabled = MutableLiveData<Boolean>()
    val overlayEnabled: LiveData<Boolean> = _overlayEnabled

    private val _lastOffer = MutableLiveData<DeliveryOffer?>()
    val lastOffer: LiveData<DeliveryOffer?> = _lastOffer

    init {
        loadInitialData()
        observePrefs()
    }

    private fun loadInitialData() {
        CoroutineScope(Dispatchers.IO).launch {
            val thresholds = prefsRepo.getThresholds()
            _thresholds.postValue(thresholds)
        }
    }

    private fun observePrefs() {
        prefsRepo.observeThresholds()
            .subscribe { thresholds ->
                _thresholds.postValue(thresholds)
            }

        prefsRepo.observeAccessibilityEnabled()
            .subscribe { enabled ->
                _accessibilityEnabled.postValue(enabled)
            }

        prefsRepo.observeOverlayEnabled()
            .subscribe { enabled ->
                _overlayEnabled.postValue(enabled)
            }

        prefsRepo.observeLastKnownOfferTime()
            .subscribe { time ->
                if (time > 0) {
                    deliveryRepo.getAllOffers().firstOrNull()?.let { offers ->
                        offers.firstOrNull { it.timestamp == time }?.let { offer ->
                            _lastOffer.postValue(offer)
                        }
                    }
                }
            }
    }

    fun saveThresholds(newThresholds: Thresholds) {
        CoroutineScope(Dispatchers.IO).launch {
            prefsRepo.saveThresholds(newThresholds)
        }
    }

    fun setAccessibilityEnabled(enabled: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            prefsRepo.setAccessibilityEnabled(enabled)
        }
    }

    fun setOverlayEnabled(enabled: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            prefsRepo.setOverlayEnabled(enabled)
        }
    }

    fun refreshTodayStats() {
        deliveryRepo.getTodayStats().firstOrNull()?.let { stats ->
            _todayStats.postValue(TodayStatsUi(
                count = stats.count,
                totalEarnings = stats.totalEarnings,
                avgValuePerKm = stats.avgValuePerKm,
                avgValuePerHour = stats.avgValuePerHour
            ))
        }
    }

    data class TodayStatsUi(
        val count: Int,
        val totalEarnings: Double,
        val avgValuePerKm: Double,
        val avgValuePerHour: Double
    )
}