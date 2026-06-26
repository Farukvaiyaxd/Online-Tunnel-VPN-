package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.VpnConfig
import com.example.ui.theme.*
import com.example.viewmodel.VpnViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigManagementScreen(
    viewModel: VpnViewModel,
    onBack: () -> Unit,
    onNavigateToAdd: () -> Unit,
    onNavigateToEdit: (Int) -> Unit
) {
    val allConfigs by viewModel.allConfigs.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var importText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Server Nodes", color = NeonCyan) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CyberpunkBackground)
            )
        },
        floatingActionButton = {
            var expandedFab by remember { mutableStateOf(false) }
            
            Box {
                FloatingActionButton(
                    onClick = { expandedFab = !expandedFab },
                    containerColor = NeonCyan,
                    contentColor = CyberpunkBackground
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Config")
                }
                
                DropdownMenu(
                    expanded = expandedFab,
                    onDismissRequest = { expandedFab = false },
                    modifier = Modifier.background(CyberpunkSurface)
                ) {
                    DropdownMenuItem(
                        text = { Text("Manual Setup", color = TextPrimary) },
                        onClick = {
                            expandedFab = false
                            onNavigateToAdd()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Import from Clipboard", color = TextPrimary) },
                        onClick = {
                            expandedFab = false
                            showAddDialog = true
                        }
                    )
                }
            }
        },
        containerColor = CyberpunkBackground
    ) { padding ->
        if (allConfigs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No configs found. Tap + to add.", color = TextSecondary)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
                items(allConfigs) { config ->
                    ConfigItem(
                        config = config,
                        onSelect = {
                            viewModel.selectConfig(config)
                            onBack()
                        },
                        onEdit = { onNavigateToEdit(config.id) },
                        onDelete = { viewModel.deleteConfig(config) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Import JSON Config", color = NeonCyan) },
                text = {
                    OutlinedTextField(
                        value = importText,
                        onValueChange = { importText = it },
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        textStyle = LocalTextStyle.current.copy(color = TextPrimary),
                        placeholder = { Text("Paste JSON here...", color = TextSecondary) }
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.importConfig(importText)
                        showAddDialog = false
                        importText = ""
                    }) {
                        Text("IMPORT", color = NeonCyan)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("CANCEL", color = TextSecondary)
                    }
                },
                containerColor = CyberpunkSurface
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigItem(config: VpnConfig, onSelect: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        onClick = onSelect,
        colors = CardDefaults.cardColors(containerColor = CyberpunkSurface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(config.name, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Text("${config.protocol.uppercase()} • ${config.serverAddress}:${config.port}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = NeonCyan)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = WarningRed)
                }
            }
        }
    }
}
