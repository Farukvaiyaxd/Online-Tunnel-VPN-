package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VpnConfig
import com.example.ui.theme.*
import com.example.viewmodel.ConnectionState
import com.example.viewmodel.VpnViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: VpnViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToConfigs: () -> Unit
) {
    val connectionState by viewModel.connectionState.collectAsState()
    val currentConfig by viewModel.currentConfig.collectAsState()
    val allConfigs by viewModel.allConfigs.collectAsState()
    val uploadSpeed by viewModel.uploadSpeed.collectAsState()
    val downloadSpeed by viewModel.downloadSpeed.collectAsState()
    val publicIp by viewModel.publicIp.collectAsState()

    val context = LocalContext.current

    val vpnPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            viewModel.startVpnServiceWithCurrentConfig(context)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        // Notification permission handled
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Online Tunnel", color = NeonCyan) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CyberpunkBackground,
                    titleContentColor = NeonCyan
                ),
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = TextPrimary)
                    }
                }
            )
        },
        containerColor = CyberpunkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Selected Server Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToConfigs() },
                colors = CardDefaults.cardColors(containerColor = CyberpunkSurface),
                border = BorderStroke(1.dp, CyberpunkDivider),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Current Node",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentConfig?.name ?: if (allConfigs.isEmpty()) "No Configs (Tap to add)" else allConfigs.first().name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextPrimary
                        )
                    }
                    Text(
                        text = "CHANGE",
                        color = NeonCyan,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // World Map Animation
            WorldMapCanvas(
                isConnected = connectionState is ConnectionState.Connected,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Connect Button & Status
            val isConnected = connectionState is ConnectionState.Connected
            val isConnecting = connectionState is ConnectionState.Connecting

            val buttonColor by animateColorAsState(
                targetValue = when {
                    isConnected -> NeonCyan.copy(alpha = 0.1f)
                    isConnecting -> CyberpunkDivider
                    connectionState is ConnectionState.Error -> WarningRed.copy(alpha = 0.2f)
                    else -> NeonCyan.copy(alpha = 0.2f)
                }
            )

            val buttonBorder = when {
                isConnected -> NeonCyan
                isConnecting -> TextSecondary
                connectionState is ConnectionState.Error -> WarningRed
                else -> NeonCyan
            }

            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(RoundedCornerShape(70.dp))
                    .background(buttonColor)
                    .clickable { 
                        if (connectionState is ConnectionState.Connected || connectionState is ConnectionState.Connecting) {
                            viewModel.disconnect(context)
                        } else {
                            val intent = android.net.VpnService.prepare(context)
                            if (intent != null) {
                                vpnPermissionLauncher.launch(intent)
                            } else {
                                viewModel.startVpnServiceWithCurrentConfig(context)
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                // Outer ring
                Box(modifier = Modifier.fillMaxSize().background(Color.Transparent, RoundedCornerShape(70.dp)))
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = when (connectionState) {
                            is ConnectionState.Connected -> "CONNECTED"
                            is ConnectionState.Connecting -> "CONNECTING..."
                            is ConnectionState.Disconnecting -> "DISCONNECTING"
                            is ConnectionState.Error -> "ERROR"
                            else -> "CONNECT"
                        },
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = when (connectionState) {
                            is ConnectionState.Connected -> NeonCyan
                            is ConnectionState.Error -> WarningRed
                            else -> TextPrimary
                        },
                        textAlign = TextAlign.Center
                    )
                    
                    AnimatedVisibility(visible = isConnected) {
                        val duration = (connectionState as? ConnectionState.Connected)?.durationSeconds ?: 0
                        val hh = duration / 3600
                        val mm = (duration % 3600) / 60
                        val ss = duration % 60
                        Text(
                            text = String.format("%02d:%02d:%02d", hh, mm, ss),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextPrimary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
            
            if (connectionState is ConnectionState.Error) {
                Text(
                    text = (connectionState as ConnectionState.Error).message,
                    color = WarningRed,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Metrics Row (Speed & Latency)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MetricItem("DL", formatSpeed(downloadSpeed), isConnected)
                VerticalDivider(modifier = Modifier.height(40.dp), color = CyberpunkDivider)
                MetricItem("UL", formatSpeed(uploadSpeed), isConnected)
                VerticalDivider(modifier = Modifier.height(40.dp), color = CyberpunkDivider)
                val ping = (connectionState as? ConnectionState.Connected)?.latencyMs ?: 0
                MetricItem("PING", if (isConnected && ping > 0) "${ping}ms" else "--", isConnected)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // IP Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CyberpunkSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Public IP", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        Text(publicIp, style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
                    }
                    Text(
                        if (isConnected) "SECURED" else "EXPOSED",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isConnected) NeonCyan else WarningRed,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun MetricItem(label: String, value: String, isConnected: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            if (isConnected) value else "--", 
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp), 
            color = if (isConnected) NeonCyan else TextPrimary
        )
    }
}

fun formatSpeed(bps: Long): String {
    if (bps < 1000) return "${bps} B/s"
    val kbps = bps / 1000.0
    if (kbps < 1000) return String.format("%.1f KB/s", kbps)
    val mbps = kbps / 1000.0
    return String.format("%.2f MB/s", mbps)
}
