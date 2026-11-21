package com.ifoodroitracker.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.BottomNavigation
import androidx.compose.material3.BottomNavigationItem
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.navigation.NavController
import androidx.compose.navigation.NavHost
import androidx.compose.navigation.composable
import androidx.compose.navigation.rememberNavController
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.ifoodroitracker.R
import com.ifoodroitracker.ui.viewmodel.HistoryViewModel
import com.ifoodroitracker.ui.viewmodel.MainViewModel
import com.ifoodroitracker.ui.viewmodel.SettingsViewModel
import com.ifoodroitracker.ui.viewmodel.StatsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val historyViewModel: HistoryViewModel by viewModels()
    private val statsViewModel: StatsViewModel by viewModels()

    private val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.SYSTEM_ALERT_WINDOW,
        Manifest.permission.POST_NOTIFICATIONS
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkAndRequestPermissions()
        setContent { AppNavHost(mainViewModel, settingsViewModel, historyViewModel, statsViewModel) }
    }

    private fun checkAndRequestPermissions() {
        val missingPermissions = REQUIRED_PERMISSIONS.filter { permission ->
            if (permission == Manifest.permission.SYSTEM_ALERT_WINDOW) {
                !Settings.canDrawOverlays(this)
            } else {
                ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
            }
        }

        if (missingPermissions.isNotEmpty()) {
            if (missingPermissions.contains(Manifest.permission.SYSTEM_ALERT_WINDOW)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                startActivity(intent)
            }
            if (missingPermissions.contains(Manifest.permission.POST_NOTIFICATIONS) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 100)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh stats quando volta para o app
        mainViewModel.refreshTodayStats()
    }
}

@Composable
fun AppNavHost(
    mainViewModel: MainViewModel,
    settingsViewModel: SettingsViewModel,
    historyViewModel: HistoryViewModel,
    statsViewModel: StatsViewModel
) {
    val navController = rememberNavController()
    val scaffoldState = remember { androidx.compose.material3.ScaffoldState() }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = { MainTopAppBar(navController) },
        bottomBar = { MainBottomBar(navController) }
    ) { padding ->
        NavHost(navController, "home", Modifier.padding(padding)) {
            composable("home") {
                HomeScreen(mainViewModel)
            }
            composable("settings") {
                SettingsScreen(settingsViewModel)
            }
            composable("history") {
                HistoryScreen(historyViewModel)
            }
            composable("stats") {
                StatsScreen(statsViewModel)
            }
        }
    }
}

@Composable
fun MainTopAppBar(navController: NavController) {
    TopAppBar(
        title = { Text("iFood ROI Tracker") },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

@Composable
fun MainBottomBar(navController: NavController) {
    val items = listOf(
        BottomBarItem("home", "Início", Icons.Default.Home),
        BottomBarItem("history", "Histórico", Icons.Default.History),
        BottomBarItem("stats", "Estatísticas", Icons.Default.Analytics),
        BottomBarItem("settings", "Configurações", Icons.Default.Settings)
    )

    val currentRoute = remember { mutableStateOf("home") }
    navController.addOnDestinationChangedListener { _, destination, _ ->
        currentRoute.value = destination.route
    }

    BottomNavigation(
        backgroundColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerHighest
    ) {
        items.forEach { item ->
            val selected = currentRoute.value == item.route
            BottomNavigationItem(
                icon = { Icon(item.icon, contentDescription = null) },
                label = { Text(item.label, fontSize = 12.sp) },
                selected = selected,
                onClick = { navController.navigate(item.route) { popUpTo(navController.graph.findStartDestination().id) { inclusive = true } avoidMultipleCopies = true launchSingleTop = true } },
                alwaysShowLabel = true,
                unselectedContentColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                selectedContentColor = androidx.compose.material3.MaterialTheme.colorScheme.primary
            )
        }
    }
}

data class BottomBarItem(val route: String, val label: String, val icon: androidx.compose.material.icons.filled.Icon)

@Composable
fun HomeScreen(viewModel: MainViewModel) {
    val thresholds by viewModel.thresholds.collectAsStateWithLifecycle()
    val todayStats by viewModel.todayStats.collectAsStateWithLifecycle()
    val accessibilityEnabled by viewModel.accessibilityEnabled.collectAsStateWithLifecycle()
    val overlayEnabled by viewModel.overlayEnabled.collectAsStateWithLifecycle()
    val lastOffer by viewModel.lastOffer.collectAsStateWithLifecycle()

    androidx.compose.foundation.layout.Box(
        modifier = androidx.compose.foundation.layout.Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = androidx.compose.foundation.layout.Modifier.fillMaxSize(),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
        ) {
            // Status cards
            androidx.compose.foundation.layout.Row(
                modifier = androidx.compose.foundation.layout.Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
            ) {
                StatusCard(
                    title = "Acessibilidade",
                    value = if (accessibilityEnabled) "Ativa ✅" else "Inativa ❌",
                    color = if (accessibilityEnabled) androidx.compose.material3.MaterialTheme.colorScheme.tertiary else androidx.compose.material3.MaterialTheme.colorScheme.errorContainer,
                    onClick = { openAccessibilitySettings() }
                )
                StatusCard(
                    title = "Overlay",
                    value = if (overlayEnabled) "Ativo ✅" else "Inativo ❌",
                    color = if (overlayEnabled) androidx.compose.material3.MaterialTheme.colorScheme.tertiary else androidx.compose.material3.MaterialTheme.colorScheme.errorContainer,
                    onClick = { viewModel.setOverlayEnabled(!overlayEnabled) }
                )
            }

            // Stats hoje
            todayStats?.let { stats ->
                androidx.compose.foundation.layout.Row(
                    modifier = androidx.compose.foundation.layout.Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
                ) {
                    StatCard("Entregas", "${stats.count}", Icons.Default.LocalShipping)
                    StatCard("Ganhos", "R$ ${String.format("%.2f", stats.totalEarnings)}", Icons.Default.AttachMoney)
                    StatCard("R$/km", String.format("%.2f", stats.avgValuePerKm), Icons.Default.Speed)
                    StatCard("R$/h", String.format("%.2f", stats.avgValuePerHour), Icons.Default.Schedule)
                }
            }

            // Última oferta
            lastOffer?.let { offer ->
                LastOfferCard(offer)
            } ?: androidx.compose.material3.Card(
                modifier = androidx.compose.foundation.layout.Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerHighest
                )
            ) {
                androidx.compose.foundation.layout.Box(
                    modifier = androidx.compose.foundation.layout.Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    androidx.compose.foundation.layout.Column(
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                    ) {
                        androidx.compose.material.icons.filled.Icon(
                            androidx.compose.material.icons.filled.LocalOffer,
                            contentDescription = null,
                            tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = androidx.compose.foundation.layout.Modifier.size(48.dp)
                        )
                        Text(
                            "Nenhuma oferta detectada ainda",
                            fontSize = 16.sp,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Abra o iFood Entregador e aguarde uma corrida aparecer",
                            fontSize = 13.sp,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = androidx.compose.ui.text.TextAlign.Center
                        )
                    }
                }
            }

            // Thresholds rápidos
            QuickThresholdsCard(thresholds)
        }
    }
}

@Composable
fun StatusCard(title: String, value: String, color: Color, onClick: () -> Unit) {
    androidx.compose.material3.Card(
        modifier = androidx.compose.foundation.layout.Modifier
            .fillMaxWidth(0.5f)
            .padding(8.dp)
            .height(80.dp),
        onClick = onClick,
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = color)
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = androidx.compose.foundation.layout.Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            androidx.compose.foundation.layout.Column(
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp)
            ) {
                Text(title, fontSize = 12.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, fontSize = 18.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, icon: androidx.compose.material.icons.filled.Icon) {
    androidx.compose.material3.Card(
        modifier = androidx.compose.foundation.layout.Modifier
            .fillMaxWidth(0.25f)
            .padding(4.dp)
            .height(80.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = androidx.compose.foundation.layout.Modifier
                .fillMaxSize()
                .padding(12.dp),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            androidx.compose.foundation.layout.Column(
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp)
            ) {
                androidx.compose.material.icons.filled.Icon(icon, contentDescription = null, tint = androidx.compose.material3.MaterialTheme.colorScheme.primary, modifier = androidx.compose.foundation.layout.Modifier.size(20.dp))
                Text(value, fontSize = 14.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                Text(label, fontSize = 10.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun LastOfferCard(offer: com.ifoodroitracker.data.entity.DeliveryOffer) {
    val verdictColor = when (offer.decision) {
        com.ifoodroitracker.data.entity.DeliveryOffer.Decision.ACCEPTED -> androidx.compose.material3.MaterialTheme.colorScheme.tertiary
        com.ifoodroitracker.data.entity.DeliveryOffer.Decision.REJECTED -> androidx.compose.material3.MaterialTheme.colorScheme.error
        com.ifoodroitracker.data.entity.DeliveryOffer.Decision.IGNORED -> androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        else -> androidx.compose.material3.MaterialTheme.colorScheme.primary
    }

    androidx.compose.material3.Card(
        modifier = androidx.compose.foundation.layout.Modifier.fillMaxWidth(),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = verdictColor.copy(alpha = 0.15f))
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = androidx.compose.foundation.layout.Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
        ) {
            Text("Última Oferta", fontSize = 16.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            androidx.compose.foundation.layout.Row(
                modifier = androidx.compose.foundation.layout.Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
            ) {
                Text("R$ ${String.format("%.2f", offer.rawValue)}", fontSize = 24.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = verdictColor)
                Text(offer.decision.label, fontSize = 14.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium, color = verdictColor)
            }
            androidx.compose.foundation.layout.Row(
                modifier = androidx.compose.foundation.layout.Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
            ) {
                DetailItem("Distância", "${String.format("%.1f", offer.rawDistanceKm)} km")
                DetailItem("Tempo", "${offer.rawEstimatedMinutes} min")
                DetailItem("R$/km", String.format("%.2f", offer.valuePerKm))
                DetailItem("R$/h", String.format("%.2f", offer.valuePerHour))
            }
            offer.rawOrigin?.let { DetailItem("Origem", it) }
            offer.rawDestination?.let { DetailItem("Destino", it) }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    androidx.compose.foundation.layout.Column(
        modifier = androidx.compose.foundation.layout.Modifier.weight(1f),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Text(label, fontSize = 11.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 13.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
    }
}

@Composable
fun QuickThresholdsCard(thresholds: com.ifoodroitracker.data.entity.Thresholds?) {
    thresholds?.let { t ->
        androidx.compose.material3.Card(
            modifier = androidx.compose.foundation.layout.Modifier.fillMaxWidth(),
            colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerHigh
            )
        ) {
            androidx.compose.foundation.layout.Column(
                modifier = androidx.compose.foundation.layout.Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
            ) {
                Text("Limiares Atuais", fontSize = 16.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                androidx.compose.foundation.layout.Row(
                    modifier = androidx.compose.foundation.layout.Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                ) {
                    ThresholdChip("R$/km ≥ ${String.format("%.2f", t.minValuePerKm)}")
                    ThresholdChip("R$/h ≥ ${String.format("%.2f", t.minValuePerHour)}")
                    ThresholdChip("Dist ≤ ${t.maxDistanceKm} km")
                    ThresholdChip("Mín R$ ${String.format("%.2f", t.minTotalValue)}")
                }
                if (t.peakHours.isNotEmpty()) {
                    Text("Pico: ${t.peakHours.joinToString(", ") { "${it.startHour}h-${it.endHour}h" }} (${String.format("%.1f", t.peakMultiplier)}x)",
                        fontSize = 12.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun ThresholdChip(text: String) {
    androidx.compose.material3.Chip(
        modifier = androidx.compose.foundation.layout.Modifier.padding(4.dp),
        onClick = {},
        colors = androidx.compose.material3.ChipDefaults.chipColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Text(text, fontSize = 11.sp)
    }
}

private fun openAccessibilitySettings() {
    try {
        val intent = Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        androidx.core.app.ActivityCompat.startActivityForResult(
            (androidx.compose.ui.platform.LocalContext.current as android.app.Activity), intent, 0, null
        )
    } catch (e: Exception) {
        // Fallback
    }
}