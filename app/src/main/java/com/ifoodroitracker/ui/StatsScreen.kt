package com.ifoodroitracker.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
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
import com.ifoodroitracker.ui.viewmodel.StatsViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun StatsScreen(viewModel: StatsViewModel = viewModel()) {
    val period by viewModel.period.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()

    androidx.compose.foundation.layout.Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TopAppBar(
                title = { Text("Estatísticas") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer
                )
            )

            // Seletor de período
            SegmentedButton(
                selected = period,
                onClick = { viewModel.setPeriod(it) },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                singleChoice = true
            ) {
                PeriodSegment("Hoje", StatsViewModel.Period.TODAY, period)
                PeriodSegment("Semana", StatsViewModel.Period.WEEK, period)
                PeriodSegment("Mês", StatsViewModel.Period.MONTH, period)
                PeriodSegment("Tudo", StatsViewModel.Period.ALL, period)
            }

            stats?.let { s ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Resumo geral
                    item { SummaryCards(s) }

                    // Por bairro
                    item { SectionTitle("Por Bairro", Icons.Default.LocationOn) }
                    item { NeighborhoodStats(s.byNeighborhood) }

                    // Por hora
                    item { SectionTitle("Por Hora do Dia", Icons.Default.Schedule) }
                    item { HourlyStats(s.byHour) }

                    // Por dia da semana
                    item { SectionTitle("Por Dia da Semana", Icons.Default.Analytics) }
                    item { WeekdayStats(s.byWeekday) }
                }
            } ?: androidx.compose.foundation.layout.Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Analytics, contentDescription = null, tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(64.dp))
                    Text("Sem dados para este período", fontSize = 18.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun PeriodSegment(label: String, p: StatsViewModel.Period, current: StatsViewModel.Period) {
    val selected = current == p
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
fun SummaryCards(stats: StatsViewModel.PeriodStatsUi) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryCard("Entregas", "${stats.totalDeliveries}", Icons.Default.LocalShipping)
        SummaryCard("Total", "R$ ${String.format("%.2f", stats.totalEarnings)}", Icons.Default.AttachMoney)
        SummaryCard("R$/km", String.format("%.2f", stats.avgValuePerKm), Icons.Default.Speed)
        SummaryCard("R$/h", String.format("%.2f", stats.avgValuePerHour), Icons.Default.Schedule)
    }
}

@Composable
fun SummaryCard(label: String, value: String, icon: androidx.compose.material.icons.filled.Icon) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.25f)
            .padding(4.dp)
            .height(90.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(icon, contentDescription = null, tint = androidx.compose.material3.MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(label, fontSize = 11.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun SectionTitle(title: String, icon: androidx.compose.material.icons.filled.Icon) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = androidx.compose.material3.MaterialTheme.colorScheme.primary)
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun NeighborhoodStats(items: List<StatsViewModel.NeighborhoodUi>) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items.take(10)) { item ->
            StatRowItem(
                label = item.name,
                count = item.count,
                avgValue = item.avgValue,
                avgPerKm = item.avgPerKm
            )
        }
    }
}

@Composable
fun HourlyStats(items: List<StatsViewModel.HourUi>) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(items) { item ->
            StatRowItem(
                label = "${item.hour}h",
                count = item.count,
                avgValue = item.avgValue,
                avgPerKm = item.avgPerKm
            )
        }
    }
}

@Composable
fun WeekdayStats(items: List<StatsViewModel.WeekdayUi>) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items) { item ->
            StatRowItem(
                label = item.name,
                count = item.count,
                avgValue = item.avgValue,
                avgPerKm = item.avgPerKm
            )
        }
    }
}

@Composable
fun StatRowItem(
    label: String,
    count: Int,
    avgValue: Double,
    avgPerKm: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text("${count}x", fontSize = 13.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("R$ ${String.format("%.2f", avgValue)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = androidx.compose.material3.MaterialTheme.colorScheme.primary)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("R$/km", fontSize = 11.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(String.format("%.2f", avgPerKm), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}