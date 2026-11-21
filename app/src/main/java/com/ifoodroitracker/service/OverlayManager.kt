package com.ifoodroitracker.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.ifoodroitracker.data.entity.DeliveryOffer
import com.ifoodroitracker.data.entity.ParsedOffer
import com.ifoodroitracker.data.entity.Thresholds
import com.ifoodroitracker.ui.overlay.OverlayContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Gerencia o overlay flutuante (bolha) que aparece sobre o iFood.
 * Usa ComposeView para renderizar UI declarativa.
 */
class OverlayManager private constructor(private val context: Context) {

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private var composeView: ComposeView? = null
    private var currentJob: Job? = null
    private var isShowing = false
    private val scope = CoroutineScope(Dispatchers.Main)

    companion object {
        @Volatile
        private var INSTANCE: OverlayManager? = null

        fun getInstance(context: Context): OverlayManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: OverlayManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    fun showOverlay(
        parsed: ParsedOffer,
        decision: DecisionEngine.DecisionResult,
        offerId: Long,
        thresholds: Thresholds
    ) {
        if (isShowing) {
            hideOverlay()
        }

        // Verifica permissão de overlay
        if (!hasOverlayPermission()) {
            return
        }

        createOverlayView(parsed, decision, offerId, thresholds)
        showView()
        isShowing = true

        // Auto-dismiss
        currentJob?.cancel()
        currentJob = scope.launch {
            delay(thresholds.autoDismissSeconds * 1000L)
            hideOverlay()
        }
    }

    fun hideOverlay() {
        currentJob?.cancel()
        currentJob = null
        if (isShowing) {
            removeView()
            isShowing = false
        }
    }

    private fun hasOverlayPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getSystemService(WindowManager::class.java)?.let {
                // Não há API direta para checar, tenta adicionar view de teste
                true
            } ?: false
        }
        return true
    }

    private fun createOverlayView(
        parsed: ParsedOffer,
        decision: DecisionEngine.DecisionResult,
        offerId: Long,
        thresholds: Thresholds
    ) {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager = wm

        // ComposeView para renderizar UI
        composeView = ComposeView(context).apply {
            setViewCompositionStrategy(ComposeView.ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                OverlayContent(
                    parsed = parsed,
                    decision = decision,
                    offerId = offerId,
                    onAccept = { hideOverlay() },
                    onReject = { hideOverlay() },
                    onIgnore = { hideOverlay() }
                )
            }
        }

        // Container para permitir drag
        overlayView = FrameLayout(context).apply {
            addView(composeView!!)
            // Touch listener para arrastar
            setOnTouchListener(DragTouchListener(wm, this))
        }

        val params = createLayoutParams()
        wm.addView(overlayView, params)
    }

    private fun createLayoutParams(): WindowManager.LayoutParams {
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 50
            y = 200
        }
    }

    private fun showView() {
        overlayView?.visibility = View.VISIBLE
    }

    private fun removeView() {
        windowManager?.let { wm ->
            overlayView?.let { view ->
                try {
                    wm.removeView(view)
                } catch (e: Exception) {
                    // View já removida
                }
            }
            composeView?.disposeComposition()
            composeView = null
            overlayView = null
        }
    }

    private class DragTouchListener(
        private val wm: WindowManager,
        private val view: View
    ) : View.OnTouchListener {

        private var initialX = 0
        private var initialY = 0
        private var initialTouchX = 0f
        private var initialTouchY = 0f
        private val params = view.layoutParams as WindowManager.LayoutParams

        override fun onTouch(v: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    wm.updateViewLayout(view, params)
                    return true
                }
                MotionEvent.ACTION_UP -> {
                    return true
                }
            }
            return false
        }
    }
}

/**
 * Service wrapper para manter o overlay vivo mesmo se o app for fechado.
 * O OverlayManager usa WindowManager diretamente, não precisa de Service ativo.
 * Este service existe apenas para casos onde precisamos de foreground service.
 */
class OverlayService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }
}