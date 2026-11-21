package com.ifoodroitracker.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ifoodroitracker.R
import com.ifoodroitracker.data.entity.PeakHour
import com.ifoodroitracker.data.entity.Thresholds
import com.ifoodroitracker.ui.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = viewModel()) {
    val thresholds by viewModel.thresholds.collectAsStateWithLifecycle()
    val saveStatus by viewModel.saveStatus.collectAsStateWithLifecycle()

    androidx.compose.foundation.layout.Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TopAppBar(
                title = { Text("Configurações") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer
                )
            )

            thresholds?.let { t ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Seção: Limiares de Aceitação
                    SettingsSection("Limiares de Aceitação") {
                        SliderRow(
                            label = "Mínimo R$/km",
                            value = t.minValuePerKm,
                            valueRange = 0.0..15.0,
                            steps = 150,
                            format = "%.2f",
                            onValueChange = { viewModel.updateMinValuePerKm(it) },
                            icon = Icons.Default.Speed
                        )
                        SliderRow(
                            label = "Mínimo R$/h",
                            value = t.minValuePerHour,
                            valueRange = 0.0..200.0,
                            steps = 200,
                            format = "%.2f",
                            onValueChange = { viewModel.updateMinValuePerHour(it) },
                            icon = Icons.Default.AttachMoney
                        )
                        SliderRow(
                            label = "Distância máxima (km)",
                            value = t.maxDistanceKm.toDouble(),
                            valueRange = 1.0..50.0,
                            steps = 49,
                            format = "%.0f",
                            onValueChange = { viewModel.updateMaxDistanceKm(it.toInt()) },
                            icon = Icons.Default.Schedule
                        )
                        SliderRow(
                            label = "Valor mínimo total (R$)",
                            value = t.minTotalValue,
                            valueRange = 0.0..50.0,
                            steps = 100,
                            format = "%.2f",
                            onValueChange = { viewModel.updateMinTotalValue(it) },
                            icon = Icons.Default.AttachMoney
                        )
                    }

                    // Seção: Horários de Pico
                    SettingsSection("Horários de Pico (Multiplicador ${String.format("%.1f", t.peakMultiplier)}x)") {
                        SliderRow(
                            label = "Multiplicador de pico",
                            value = t.peakMultiplier,
                            valueRange = 1.0..3.0,
                            steps = 20,
                            format = "%.1f",
                            onValueChange = { viewModel.updatePeakMultiplier(it) },
                            icon = Icons.Default.AccessTime
                        )
                        PeakHoursEditor(
                            peakHours = t.peakHours,
                            onChange = { viewModel.updatePeakHours(it) }
                        )
                    }

                    // Seção: Comportamento do Overlay
                    SettingsSection("Overlay & Notificações") {
                        SliderRow(
                            label = "Auto-fechar após (segundos)",
                            value = t.autoDismissSeconds.toDouble(),
                            valueRange = 3.0..60.0,
                            steps = 57,
                            format = "%.0f",
                            onValueChange = { viewModel.updateAutoDismissSeconds(it.toInt()) },
                            icon = Icons.Default.AccessTime
                        )
                        SwitchRow(
                            label = "Vibrar na decisão",
                            checked = t.vibrateOnDecision,
                            onCheckedChange = { viewModel.updateVibrateOnDecision(it) }
                        )
                        SwitchRow(
                            label = "Som na decisão",
                            checked = t.playSoundOnDecision,
                            onCheckedChange = { viewModel.updatePlaySoundOnDecision(it) }
                        )
                        SwitchRow(
                            label = "Mostrar na tela de bloqueio",
                            checked = t.showOnLockscreen,
                            onCheckedChange = { viewModel.updateShowOnLockscreen(it) }
                        )
                    }

                    // Seção: Ações
                    SettingsSection("Dados") {
                        ActionButton(
                            label = "Exportar CSV",
                            icon = Icons.Default.Edit,
                            onClick = { /* TODO: implement export */ }
                        )
                        ActionButton(
                            label = "Importar CSV",
                            icon = Icons.Default.Add,
                            onClick = { /* TODO: implement import */ }
                        )
                        ActionButton(
                            label = "Backup completo",
                            icon = Icons.Default.Schedule,
                            onClick = { /* TODO: implement backup */ }
                        )
                        ActionButton(
                            label = "Restaurar backup",
                            icon = Icons.Default.Restore,
                            onClick = { /* TODO: implement restore */ }
                        )
                        ActionButton(
                            label = "Limpar histórico",
                            icon = Icons.Default.Delete,
                            destructive = true,
                            onClick = { /* TODO: implement clear */ }
                        )
                    }

                    // Botão salvar
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = { viewModel.save() },
                            modifier = Modifier.fillMaxWidth(0.8f).height(48.dp),
                            enabled = saveStatus != SettingsViewModel.SaveStatus.SAVING,
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary
                            )
                        ) {
                            when (saveStatus) {
                                SettingsViewModel.SaveStatus.SAVING -> Text("Salvando...", fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
                                SettingsViewModel.SaveStatus.SUCCESS -> Text("Salvo! ✅", fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
                                else -> Text("Salvar Configurações", fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
                            }
                        }
                    }

                    // Reset
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        androidx.compose.material3.TextButton(
                            onClick = { viewModel.resetToDefaults() },
                            colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                                contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text("Restaurar padrões")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(title, fontSize = 18.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            Divider()
            content()
        }
    }
}

@Composable
fun SliderRow(
    label: String,
    value: Double,
    valueRange: androidx.compose.ui.unit.ClosedFloatingPointRange<Double>,
    steps: Int,
    format: String,
    onValueChange: (Double) -> Unit,
    icon: androidx.compose.material.icons.filled.Icon
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = null, tint = androidx.compose.material3.MaterialTheme.colorScheme.primary)
                Text(label, fontSize = 16.sp)
            }
            Text(String.format(format, value), fontSize = 16.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = androidx.compose.material3.MaterialTheme.colorScheme.primary)
        }
        Slider(
            modifier = Modifier.fillMaxWidth(),
            value = value,
            valueRange = valueRange,
            steps = steps,
            onValueChange = onValueChange
        )
    }
}

@Composable
fun SwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 16.sp)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = androidx.compose.material3.SwitchDefaults.colors(
                checkedThumbColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                checkedTrackColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}

@Composable
fun ActionButton(
    label: String,
    icon: androidx.compose.material.icons.filled.Icon,
    destructive: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(48.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = if (destructive) androidx.compose.material3.MaterialTheme.colorScheme.error else androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
            Text(label, fontSize = 16.sp, color = if (destructive) androidx.compose.material3.MaterialTheme.colorScheme.error else androidx.compose.material3.MaterialTheme.colorScheme.onSurface)
        }
        Icon(Icons.Default.Edit, contentDescription = null, tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
    }
        .fillMaxWidth()
        .clickable { onClick() }
}

@Composable
fun PeakHoursEditor(
    peakHours: List<PeakHour>,
    onChange: (List<PeakHour>) -> Unit
) {
    var hours by remember { mutableStateOf(peakHours.toMutableList()) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        hours.forEachIndexed { index, peak ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = peak.startHour.toString(),
                    onValueChange = { text ->
                        text.toIntOrNull()?.let { h ->
                            if (h in 0..23) {
                                hours[index] = PeakHour(h, peak.endHour)
                                onChange(hours)
                            }
                        }
                    },
                    label = { Text("Início") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Text("–", fontSize = 18.sp)
                OutlinedTextField(
                    value = peak.endHour.toString(),
                    onValueChange = { text ->
                        text.toIntOrNull()?.let { h ->
                            if (h in 0..23) {
                                hours[index] = PeakHour(peak.startHour, h)
                                onChange(hours)
                            }
                        }
                    },
                    label = { Text("Fim") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remover",
                    tint = androidx.compose.material3.MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .size(48.dp)
                        .fillMaxSize()
                        .clickable { hours.removeAt(index); onChange(hours) }
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            androidx.compose.material3.OutlinedButton(
                onClick = {
                    hours.add(PeakHour(18, 21))
                    onChange(hours)
                }
            ) {
                Text("Adicionar horário de pico")
            }
        }
    }
}