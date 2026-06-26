package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.VpnConfig
import com.example.ui.theme.*
import com.example.viewmodel.VpnViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddConfigScreen(
    viewModel: VpnViewModel,
    configToEdit: VpnConfig? = null,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf(configToEdit?.name ?: "") }
    var serverAddress by remember { mutableStateOf(configToEdit?.serverAddress ?: "") }
    var port by remember { mutableStateOf(configToEdit?.port?.toString() ?: "") }
    var uuid by remember { mutableStateOf(configToEdit?.uuid ?: "") }
    var protocol by remember { mutableStateOf(configToEdit?.protocol ?: "vmess") }
    var networkType by remember { mutableStateOf(configToEdit?.networkType ?: "tcp") }
    var tls by remember { mutableStateOf(configToEdit?.tls ?: false) }
    var sni by remember { mutableStateOf(configToEdit?.sni ?: "") }

    val protocols = listOf("vmess", "vless", "trojan", "shadowsocks")
    val networkTypes = listOf("tcp", "ws", "grpc", "quic")

    var expandedProtocol by remember { mutableStateOf(false) }
    var expandedNetwork by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (configToEdit != null) "Edit Config" else "Manual Setup", color = NeonCyan) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    TextButton(onClick = {
                        val config = VpnConfig(
                            id = configToEdit?.id ?: 0,
                            name = name.ifEmpty { "New Profile" },
                            serverAddress = serverAddress,
                            port = port.toIntOrNull() ?: 443,
                            uuid = uuid,
                            protocol = protocol,
                            networkType = networkType,
                            tls = tls,
                            sni = sni
                        )
                        viewModel.saveConfig(config)
                        onBack()
                    }) {
                        Text("SAVE", color = NeonCyan)
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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CustomTextField(value = name, onValueChange = { name = it }, label = "Profile Name (e.g. US Server)")
            CustomTextField(value = serverAddress, onValueChange = { serverAddress = it }, label = "Server Address / IP")
            CustomTextField(value = port, onValueChange = { port = it }, label = "Port", keyboardType = KeyboardType.Number)
            CustomTextField(value = uuid, onValueChange = { uuid = it }, label = "UUID / Password")

            // Protocol Dropdown
            ExposedDropdownMenuBox(
                expanded = expandedProtocol,
                onExpandedChange = { expandedProtocol = !expandedProtocol }
            ) {
                OutlinedTextField(
                    value = protocol.uppercase(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Protocol", color = TextSecondary) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedProtocol) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = CyberpunkDivider,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
                ExposedDropdownMenu(
                    expanded = expandedProtocol,
                    onDismissRequest = { expandedProtocol = false },
                    modifier = Modifier.background(CyberpunkSurface)
                ) {
                    protocols.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption.uppercase(), color = TextPrimary) },
                            onClick = {
                                protocol = selectionOption
                                expandedProtocol = false
                            }
                        )
                    }
                }
            }

            // Network Type Dropdown
            ExposedDropdownMenuBox(
                expanded = expandedNetwork,
                onExpandedChange = { expandedNetwork = !expandedNetwork }
            ) {
                OutlinedTextField(
                    value = networkType.uppercase(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Network Type (Transport)", color = TextSecondary) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedNetwork) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = CyberpunkDivider,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
                ExposedDropdownMenu(
                    expanded = expandedNetwork,
                    onDismissRequest = { expandedNetwork = false },
                    modifier = Modifier.background(CyberpunkSurface)
                ) {
                    networkTypes.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption.uppercase(), color = TextPrimary) },
                            onClick = {
                                networkType = selectionOption
                                expandedNetwork = false
                            }
                        )
                    }
                }
            }

            // TLS Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Enable TLS", color = TextPrimary, style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = tls,
                    onCheckedChange = { tls = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = CyberpunkBackground,
                        checkedTrackColor = NeonCyan,
                        uncheckedThumbColor = TextSecondary,
                        uncheckedTrackColor = CyberpunkSurface
                    )
                )
            }

            if (tls) {
                CustomTextField(value = sni, onValueChange = { sni = it }, label = "SNI (Server Name Indication)")
            }
        }
    }
}

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = TextSecondary) },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        textStyle = LocalTextStyle.current.copy(color = TextPrimary),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonCyan,
            unfocusedBorderColor = CyberpunkDivider,
            cursorColor = NeonCyan
        )
    )
}
