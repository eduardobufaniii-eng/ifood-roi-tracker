package com.ifoodroitracker.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ifoodroitracker.R
import com.ifoodroitracker.data.entity.DeliveryOffer
import com.ifoodroitracker.ui.viewmodel.HistoryViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun HistoryScreen(viewModel: HistoryViewModel = viewModel()) {
    val offers by viewModel.offers.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()

    val dateFormat = remember { SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()) }

    androidx.compose.foundation.layout.Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TopAppBar(
                title = { Text("Histórico de Corridas") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer
                )
            )

            // Filtros
            SegmentedButton(
                selected = filter,
                onClick = { viewModel.setFilter(it) },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                singleChoice = true
            ) {
                FilterSegment("Todas", HistoryViewModel.FilterType.ALL, filter)
                FilterSegment("✅ Aceitas", HistoryViewModel.FilterType.ACCEPTED, filter)
                FilterSegment("❌ Recusadas", HistoryViewModel.FilterType.REJECTED, filter)
                FilterSegment("⏭️ Ignoradas", HistoryViewModel.FilterType.IGNORED, filter)
            }

            // Lista
            if (offers.isEmpty()) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(History, contentDescription = null, tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(64.dp))
                        Text("Nenhuma corrida registrada", fontSize = 18.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("As ofertas detectadas aparecerão aqui", fontSize = 14.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(offers) { offer ->
                        HistoryItemCard(offer, dateFormat) { decision ->
                            viewModel.updateDecision(offer.id!!, decision)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterSegment(label: String, type: HistoryViewModel.FilterType, current: HistoryViewModel.FilterType) {
    val selected = current == type
    SegmentedButtonDefaults.Segment(
        modifier = Modifier.weight(1f, fill = true),
        label = { Text(label, fontSize = 12.sp, maxLines = 1) },
        selected = selected,
        onClick = {},
        colors = SegmentedButtonDefaults.segmentedButtonColors(
            containerColor = if (selected) androidx.compose.material3.MaterialTheme.colorScheme.primary else androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerHighest,
            contentColor = if (selected) androidx.compose.material3.MaterialTheme.colorScheme.onPrimary else androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

@Composable
fun HistoryItemCard(
    offer: DeliveryOffer,
    dateFormat: SimpleDateFormat,
    onDecisionClick: (DeliveryOffer.Decision) -> Unit
) {
    val (decisionColor, decisionIcon) = when (offer.decision) {
        DeliveryOffer.Decision.ACCEPTED -> (androidx.compose.material3.MaterialTheme.colorScheme.tertiary, Icons.Default.CheckCircle)
        DeliveryOffer.Decision.REJECTED -> (androidx.compose.material3.MaterialTheme.colorScheme.error, Icons.Default.Cancel)
        DeliveryOffer.Decision.IGNORED -> (androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), Icons.Default.RemoveCircle)
        else -> (androidx.compose.material3.MaterialTheme.colorScheme.primary, Icons.Default.History)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = decisionColor.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(decisionIcon, contentDescription = null, tint = decisionColor, modifier = Modifier.size(24.dp))
                    Column {
                        Text(offer.decision.label, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = decisionColor)
                        Text(dateFormat.format(java.util.Date(offer.timestamp)), fontSize = 11.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Text("R$ ${String.format("%.2f", offer.rawValue)}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = decisionColor)
            }

            // Detalhes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DetailColumn("Distância", "${String.format("%.1f", offer.rawDistanceKm)} km")
                DetailColumn("Tempo", "${offer.rawEstimatedMinutes} min")
                DetailColumn("R$/km", String.format("%.2f", offer.valuePerKm))
                DetailColumn("R$/h", String.format("%.2f", offer.valuePerHour))
            }

            offer.rawOrigin?.let { DetailRowFull("🏁 Origem", it) }
            offer.rawDestination?.let { DetailRowFull("🎯 Destino", it) }

            // Botões de decisão (se pendente)
            if (offer.decision == DeliveryOffer.Decision.PENDING) {
                Divider()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DecisionButton("✅ Aceitei", androidx.compose.material3.MaterialTheme.colorScheme.tertiary) { onDecisionClick(DeliveryOffer.Decision.ACCEPTED) }
                    DecisionButton("❌ Recusei", androidx.compose.material3.MaterialTheme.colorScheme.error) { onDecisionClick(DeliveryOffer.Decision.REJECTED) }
                    DecisionButton("⏭️ Ignorar", androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)) { onDecisionClick(DeliveryOffer.Decision.IGNORED) }
                }
            }
        }
    }
}

@Composable
fun DetailColumn(label: String, value: String) {
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, fontSize = 11.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun DetailRowFull(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = androidx.compose.ui.text.TextOverflow.Ellipsis)
    }
}

@Composable
fun DecisionButton(label: String, color: Color, onClick: () -> Unit) {
    androidx.compose.material3.Button(
        onClick = onClick,
        modifier = Modifier.weight(1f, fill = true).height(40.dp),
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = color.copy(alpha = 0.15f),
            contentColor = color
        )
    ) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1)
    }
}