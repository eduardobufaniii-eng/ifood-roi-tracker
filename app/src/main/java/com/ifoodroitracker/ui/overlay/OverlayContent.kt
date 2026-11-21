package com.ifoodroitracker.ui.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ifoodroitracker.R
import com.ifoodroitracker.data.entity.ParsedOffer
import com.ifoodroitracker.service.DecisionEngine

@Composable
fun OverlayContent(
    parsed: ParsedOffer,
    decision: DecisionEngine.DecisionResult,
    offerId: Long,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onIgnore: () -> Unit
) {
    val (verdictColor, verdictText, verdictIcon) = when (decision.verdict) {
        DecisionEngine.Verdict.WORTH_IT -> (
            Color(0xFF006E1C), // Verde escuro
            "VALE A PENA ✅",
            R.drawable.ic_check_circle
        )
        DecisionEngine.Verdict.BORDERLINE -> (
            Color(0xFF8C6B00), // Amarelo/laranja
            "AVALIAR ⚠️",
            R.drawable.ic_warning
        )
        DecisionEngine.Verdict.NOT_WORTH -> (
            Color(0xFFBA1A1A), // Vermelho
            "NÃO VALE ❌",
            R.drawable.ic_cancel
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = verdictColor.copy(alpha = 0.95f),
            contentColor = Color.White
        ),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header com ícone e veredito
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = verdictIcon),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = verdictText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Detalhes da oferta
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                DetailRow("💰 Valor", "R$ ${String.format("%.2f", parsed.value)}")
                DetailRow("📍 Distância", "${String.format("%.1f", parsed.distanceKm)} km")
                DetailRow("⏱️ Tempo est.", "${parsed.estimatedMinutes} min")
                DetailRow("📊 R$/km", "${String.format("%.2f", decision.valuePerKm)} (mín: ${String.format("%.2f", decision.adjustedMinValuePerKm)})")
                DetailRow("🕐 R$/h", "${String.format("%.2f", decision.valuePerHour)} (mín: ${String.format("%.2f", decision.adjustedMinValuePerHour)})")

                parsed.origin?.let { DetailRow("🏁 Origem", it) }
                parsed.destination?.let { DetailRow("🎯 Destino", it) }
            }

            // Botões de ação
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActionButton(
                    text = "✅ Aceitei",
                    color = Color.White.copy(alpha = 0.2f),
                    textColor = Color.White,
                    onClick = onAccept
                )
                ActionButton(
                    text = "❌ Recusei",
                    color = Color.White.copy(alpha = 0.2f),
                    textColor = Color.White,
                    onClick = onReject
                )
                ActionButton(
                    text = "⏭️ Ignorar",
                    color = Color.White.copy(alpha = 0.1f),
                    textColor = Color.White.copy(alpha = 0.8f),
                    onClick = onIgnore
                )
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 13.sp, color = Color.White.copy(alpha = 0.9f))
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.White)
    }
}

@Composable
private fun ActionButton(
    text: String,
    color: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.weight(1f, fill = true).height(40.dp),
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = textColor
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text = text, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1)
    }
}