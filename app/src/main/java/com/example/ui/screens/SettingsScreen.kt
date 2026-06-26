package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*
import com.example.viewmodel.VpnViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: VpnViewModel,
    onBack: () -> Unit
) {
    var autoConnect by remember { mutableStateOf(viewModel.settingsManager.autoConnect) }
    var bypassLan by remember { mutableStateOf(viewModel.settingsManager.bypassLan) }
    var blockIpv6 by remember { mutableStateOf(viewModel.settingsManager.blockIpv6) }
    var customDns by remember { mutableStateOf(viewModel.settingsManager.customDns) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = NeonCyan) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CyberpunkBackground)
            )
        },
        containerColor = CyberpunkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            SettingSwitch(
                title = "Auto-connect on boot",
                checked = autoConnect,
                onCheckedChange = { 
                    autoConnect = it
                    viewModel.settingsManager.autoConnect = it
                }
            )
            HorizontalDivider(color = CyberpunkDivider)
            SettingSwitch(
                title = "Bypass LAN",
                checked = bypassLan,
                onCheckedChange = { 
                    bypassLan = it
                    viewModel.settingsManager.bypassLan = it
                }
            )
            HorizontalDivider(color = CyberpunkDivider)
            SettingSwitch(
                title = "Block IPv6",
                checked = blockIpv6,
                onCheckedChange = { 
                    blockIpv6 = it
                    viewModel.settingsManager.blockIpv6 = it
                }
            )
            HorizontalDivider(color = CyberpunkDivider)
            Spacer(modifier = Modifier.height(16.dp))
            
            var bypassLocations by remember { mutableStateOf(viewModel.settingsManager.bypassLocations) }

            Text("Custom DNS", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = customDns,
                onValueChange = {
                    customDns = it
                    viewModel.settingsManager.customDns = it
                },
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(color = TextPrimary),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = CyberpunkDivider,
                    cursorColor = NeonCyan
                )
            )

            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Bypass Locations (V2Ray Secure Routing)", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = bypassLocations,
                onValueChange = {
                    bypassLocations = it
                    viewModel.settingsManager.bypassLocations = it
                },
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(color = TextPrimary),
                placeholder = { Text("e.g. geoip:private, geosite:cn", color = TextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = CyberpunkDivider,
                    cursorColor = NeonCyan
                )
            )
        }
    }
}

@Composable
fun SettingSwitch(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = TextPrimary, style = MaterialTheme.typography.bodyLarge)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = CyberpunkBackground,
                checkedTrackColor = NeonCyan,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = CyberpunkSurface
            )
        )
    }
}
