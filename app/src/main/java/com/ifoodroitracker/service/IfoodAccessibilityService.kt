package com.ifoodroitracker.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.ifoodroitracker.data.entity.DeliveryOffer
import com.ifoodroitracker.data.entity.ParsedOffer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class IfoodAccessibilityService : AccessibilityService() {

    private val TAG = "IfoodAccessibilitySvc"
    private val handler = Handler(Looper.getMainLooper())
    private val scope = CoroutineScope(Dispatchers.IO)
    private var lastOfferHash = 0
    private var lastOfferTime = 0L
    private val MIN_OFFER_INTERVAL_MS = 3000 // Evita duplicatas

    // Injetados via Hilt (manual init no onCreate)
    private lateinit var parser: IfoodScreenParser
    private lateinit var decisionEngine: DecisionEngine
    private lateinit var prefsRepo: com.ifoodroitracker.data.prefs.PreferencesRepository
    private lateinit var deliveryRepo: com.ifoodroitracker.data.repository.DeliveryRepository
    private lateinit var overlayManager: OverlayManager

    override fun onCreate() {
        super.onCreate()
        // Inicialização manual das dependências (Hilt não injeta em AccessibilityService facilmente)
        val app = applicationContext as IfoodRoiApplication
        parser = IfoodScreenParser
        decisionEngine = DecisionEngine
        prefsRepo = app.preferencesRepository
        deliveryRepo = app.deliveryRepository
        overlayManager = OverlayManager.getInstance(this)

        Log.d(TAG, "Accessibility Service criado")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // Só processa eventos de mudança de conteúdo/janela do iFood
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED ||
            event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {

            val packageName = event.packageName?.toString()
            if (packageName != "br.com.ifood.deliver" && packageName != "br.com.ifood") {
                return
            }

            // Debounce: evita processar múltiplos eventos da mesma tela
            val now = System.currentTimeMillis()
            if (now - lastOfferTime < MIN_OFFER_INTERVAL_MS) return

            scope.launch {
                processEvent(event)
            }
        }
    }

    private fun processEvent(event: AccessibilityEvent) {
        val root = event.source ?: rootInActiveWindow
        root?.let { node ->
            try {
                val parsed = IfoodScreenParser.parseOffer(this, node)
                parsed?.let { offer ->
                    val offerHash = calculateOfferHash(offer)
                    if (offerHash == lastOfferHash) {
                        Log.d(TAG, "Oferta duplicada ignorada")
                        return@launch
                    }
                    lastOfferHash = offerHash
                    lastOfferTime = System.currentTimeMillis()

                    handleNewOffer(offer)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao processar evento", e)
            } finally {
                root.recycle()
            }
        }
    }

    private fun handleNewOffer(parsed: ParsedOffer) {
        // Busca thresholds atuais (síncrono via runBlocking seria ruim, usamos callback)
        scope.launch(Dispatchers.IO) {
            val thresholds = prefsRepo.getThresholds()
            val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)

            val decision = DecisionEngine.evaluate(parsed, thresholds, currentHour)

            Log.d(TAG, "Decisão: $decision")

            // Salva no banco
            val entity = DeliveryOffer.createFromParsed(
                parsed, thresholds, decision.verdict == DecisionEngine.Verdict.WORTH_IT,
                if (thresholds.peakHours.any { it.contains(currentHour) }) thresholds.peakMultiplier else 1.0
            )
            val id = deliveryRepo.insertOffer(entity)

            // Atualiza último horário conhecido
            prefsRepo.setLastKnownOfferTime(System.currentTimeMillis())

            // Mostra overlay na UI thread
            handler.post {
                overlayManager.showOverlay(
                    parsed = parsed,
                    decision = decision,
                    offerId = id,
                    thresholds = thresholds
                )
            }
        }
    }

    private fun calculateOfferHash(offer: ParsedOffer): Int {
        var hash = 17
        hash = 31 * hash + offer.value.hashCode()
        hash = 31 * hash + offer.distanceKm.hashCode()
        hash = 31 * hash + offer.estimatedMinutes.hashCode()
        hash = 31 * hash + (offer.origin?.hashCode() ?: 0)
        hash = 31 * hash + (offer.destination?.hashCode() ?: 0)
        return hash
    }

    override fun onInterrupt() {
        Log.d(TAG, "Accessibility Service interrompido")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "Accessibility Service conectado")
        // Configura info do serviço
        serviceInfo.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        serviceInfo.eventTypes = AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
                                 AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
        serviceInfo.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS or
                            AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                            AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS
        serviceInfo.notificationTimeout = 100
        setServiceInfo(serviceInfo)
    }

    companion object {
        const val ACTION_SHOW_OVERLAY = "com.ifoodroitracker.SHOW_OVERLAY"
        const val EXTRA_PARSED_OFFER = "parsed_offer"
        const val EXTRA_DECISION = "decision"
        const val EXTRA_OFFER_ID = "offer_id"
        const val EXTRA_THRESHOLDS = "thresholds"
    }
}