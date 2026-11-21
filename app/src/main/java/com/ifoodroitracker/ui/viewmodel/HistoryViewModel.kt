package com.ifoodroitracker.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ifoodroitracker.data.entity.DeliveryOffer
import com.ifoodroitracker.data.repository.DeliveryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    application: Application,
    private val deliveryRepo: DeliveryRepository
) : AndroidViewModel(application) {

    private val _offers = MutableLiveData<List<DeliveryOffer>>()
    val offers: LiveData<List<DeliveryOffer>> = _offers

    private val _filter = MutableLiveData<FilterType>()
    val filter: LiveData<FilterType> = _filter

    enum class FilterType {
        ALL, ACCEPTED, REJECTED, IGNORED
    }

    init {
        _filter.value = FilterType.ALL
        loadOffers()
    }

    fun setFilter(type: FilterType) {
        _filter.value = type
        loadOffers()
    }

    private fun loadOffers() {
        CoroutineScope(Dispatchers.IO).launch {
            val offers = when (_filter.value) {
                FilterType.ALL -> deliveryRepo.getAllOffers().firstOrNull() ?: emptyList()
                FilterType.ACCEPTED -> deliveryRepo.getOffersByDecision(DeliveryOffer.Decision.ACCEPTED).firstOrNull() ?: emptyList()
                FilterType.REJECTED -> deliveryRepo.getOffersByDecision(DeliveryOffer.Decision.REJECTED).firstOrNull() ?: emptyList()
                FilterType.IGNORED -> deliveryRepo.getOffersByDecision(DeliveryOffer.Decision.IGNORED).firstOrNull() ?: emptyList()
                else -> emptyList()
            }
            _offers.postValue(offers)
        }
    }

    fun updateDecision(offerId: Long, decision: DeliveryOffer.Decision) {
        CoroutineScope(Dispatchers.IO).launch {
            deliveryRepo.updateOfferDecision(offerId, decision)
            loadOffers()
        }
    }

    fun clearHistory() {
        CoroutineScope(Dispatchers.IO).launch {
            deliveryRepo.clearAllHistory()
            loadOffers()
        }
    }
}