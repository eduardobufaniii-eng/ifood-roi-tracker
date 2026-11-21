package com.ifoodroitracker.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ifoodroitracker.data.entity.Thresholds
import com.ifoodroitracker.data.entity.PeakHour
import com.ifoodroitracker.data.prefs.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application,
    private val prefsRepo: PreferencesRepository
) : AndroidViewModel(application) {

    private val _thresholds = MutableLiveData<Thresholds>()
    val thresholds: LiveData<Thresholds> = _thresholds

    private val _saveStatus = MutableLiveData<SaveStatus>()
    val saveStatus: LiveData<SaveStatus> = _saveStatus

    enum class SaveStatus {
        IDLE, SAVING, SUCCESS, ERROR
    }

    init {
        loadThresholds()
    }

    private fun loadThresholds() {
        CoroutineScope(Dispatchers.IO).launch {
            val thresholds = prefsRepo.getThresholds()
            _thresholds.postValue(thresholds)
        }
    }

    fun updateMinValuePerKm(value: Double) {
        _thresholds.value?.let { current ->
            _thresholds.value = current.copy(minValuePerKm = value)
        }
    }

    fun updateMinValuePerHour(value: Double) {
        _thresholds.value?.let { current ->
            _thresholds.value = current.copy(minValuePerHour = value)
        }
    }

    fun updateMaxDistanceKm(value: Int) {
        _thresholds.value?.let { current ->
            _thresholds.value = current.copy(maxDistanceKm = value)
        }
    }

    fun updateMinTotalValue(value: Double) {
        _thresholds.value?.let { current ->
            _thresholds.value = current.copy(minTotalValue = value)
        }
    }

    fun updatePeakMultiplier(value: Double) {
        _thresholds.value?.let { current ->
            _thresholds.value = current.copy(peakMultiplier = value)
        }
    }

    fun updatePeakHours(peakHours: List<PeakHour>) {
        _thresholds.value?.let { current ->
            _thresholds.value = current.copy(peakHours = peakHours)
        }
    }

    fun updateAutoDismissSeconds(value: Int) {
        _thresholds.value?.let { current ->
            _thresholds.value = current.copy(autoDismissSeconds = value)
        }
    }

    fun updateVibrateOnDecision(value: Boolean) {
        _thresholds.value?.let { current ->
            _thresholds.value = current.copy(vibrateOnDecision = value)
        }
    }

    fun updatePlaySoundOnDecision(value: Boolean) {
        _thresholds.value?.let { current ->
            _thresholds.value = current.copy(playSoundOnDecision = value)
        }
    }

    fun updateShowOnLockscreen(value: Boolean) {
        _thresholds.value?.let { current ->
            _thresholds.value = current.copy(showOnLockscreen = value)
        }
    }

    fun save() {
        _thresholds.value?.let { thresholds ->
            _saveStatus.value = SaveStatus.SAVING
            CoroutineScope(Dispatchers.IO).launch {
                prefsRepo.saveThresholds(thresholds)
                _saveStatus.postValue(SaveStatus.SUCCESS)
                // Reset para IDLE após 2s
                Thread.sleep(2000)
                _saveStatus.postValue(SaveStatus.IDLE)
            }
        }
    }

    fun resetToDefaults() {
        val defaults = Thresholds()
        _thresholds.value = defaults
    }
}