package com.ifoodroitracker.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.ifoodroitracker.service.IfoodAccessibilityService
import com.ifoodroitracker.service.OverlayService

class BootReceiver : BroadcastReceiver() {

    private val TAG = "BootReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED || action == "android.intent.action.QUICKBOOT_POWERON") {
            Log.d(TAG, "Boot completed, reiniciando serviços")

            // Nota: Accessibility Service não pode ser iniciado por broadcast
            // O usuário precisa ativar manualmente nas configurações
            // Mas podemos iniciar o OverlayService se necessário

            // Verifica se overlay estava ativo
            val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            val overlayEnabled = prefs.getBoolean("overlay_enabled", false)

            if (overlayEnabled) {
                val overlayIntent = Intent(context, OverlayService::class.java)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    context.startForegroundService(overlayIntent)
                } else {
                    context.startService(overlayIntent)
                }
            }

            // Notifica usuário para ativar acessibilidade
            showNotification(context)
        }
    }

    private fun showNotification(context: Context) {
        // Opcional: mostrar notificação lembrando de ativar acessibilidade
    }
}