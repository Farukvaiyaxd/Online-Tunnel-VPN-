package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.VpnService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SettingsManager
import com.example.data.VpnConfig
import com.example.data.VpnConfigRepository
import com.example.data.VpnDatabase
import com.example.network.NetworkClient
import com.example.vpn.VpnTunnelService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.random.Random

import kotlinx.coroutines.flow.first

sealed class ConnectionState {
    object Disconnected : ConnectionState()
    object Connecting : ConnectionState()
    data class Connected(val latencyMs: Int, val durationSeconds: Long) : ConnectionState()
    object Disconnecting : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}

class VpnViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: VpnConfigRepository
    val settingsManager = SettingsManager(application)
    private val connectivityManager = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val allConfigs: StateFlow<List<VpnConfig>>

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _publicIp = MutableStateFlow("Fetching...")
    val publicIp: StateFlow<String> = _publicIp.asStateFlow()

    private val _currentConfig = MutableStateFlow<VpnConfig?>(null)
    val currentConfig: StateFlow<VpnConfig?> = _currentConfig.asStateFlow()

    private val _uploadSpeed = MutableStateFlow(0L)
    val uploadSpeed: StateFlow<Long> = _uploadSpeed.asStateFlow()

    private val _downloadSpeed = MutableStateFlow(0L)
    val downloadSpeed: StateFlow<Long> = _downloadSpeed.asStateFlow()

    private var durationJob: Job? = null
    private var speedSimJob: Job? = null
    private var pingJob: Job? = null
    private var startTimestamp = 0L

    init {
        val database = VpnDatabase.getDatabase(application)
        repository = VpnConfigRepository(database.vpnConfigDao())
        allConfigs = repository.allConfigs.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        viewModelScope.launch {
            val count = database.vpnConfigDao().getAllConfigs().first().size
            if (count == 0) {
                val initialConfig = com.example.data.VpnConfig(
                    name = "inbound-443-Test12",
                    serverAddress = "lowkey.ashen.cfd",
                    port = 443,
                    uuid = "04c20a83-6f9b-4f5d-8028-f1b6c52db0c9",
                    protocol = "vless",
                    networkType = "tcp",
                    tls = true,
                    sni = "telegram.org"
                )
                repository.insert(initialConfig)
            }
        }

        monitorNetwork()
        fetchPublicIp()
    }

    private fun monitorNetwork() {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                fetchPublicIp()
            }
        })
    }

    fun fetchPublicIp() {
        viewModelScope.launch {
            try {
                val response = NetworkClient.ipifyApi.getPublicIp()
                _publicIp.value = response.ip
            } catch (e: Exception) {
                _publicIp.value = "Unknown IP"
            }
        }
    }

    fun saveConfig(config: VpnConfig) {
        viewModelScope.launch {
            if (config.id == 0) {
                repository.insert(config)
            } else {
                repository.update(config)
            }
        }
    }

    fun deleteConfig(config: VpnConfig) {
        viewModelScope.launch {
            repository.delete(config)
        }
    }

    fun selectConfig(config: VpnConfig) {
        _currentConfig.value = config
    }

    fun importConfig(json: String) {
        viewModelScope.launch {
            repository.importFromJson(json)
        }
    }

    fun toggleConnection(context: Context) {
        if (_connectionState.value is ConnectionState.Connected || _connectionState.value is ConnectionState.Connecting) {
            disconnect(context)
        } else {
            val configToConnect = _currentConfig.value ?: allConfigs.value.firstOrNull()
            configToConnect?.let { connect(context, it) }
        }
    }

    private fun connect(context: Context, config: VpnConfig) {
        _currentConfig.value = config
        _connectionState.value = ConnectionState.Connecting
        
        // Ensure VpnService is prepared
        val prepareIntent = VpnService.prepare(context)
        if (prepareIntent != null) {
            // Needs permission, let UI handle this. UI will call startVpnService directly
            // We assume permission is already granted if we get here. If not, this is a bug in caller flow.
            return
        }

        startVpnService(context, config)
    }

    fun startVpnServiceWithCurrentConfig(context: Context) {
        val configToConnect = _currentConfig.value ?: allConfigs.value.firstOrNull()
        configToConnect?.let { 
            _currentConfig.value = it
            _connectionState.value = ConnectionState.Connecting
            startVpnService(context, it) 
        }
    }

    fun startVpnService(context: Context, config: VpnConfig) {
        val intent = Intent(context, VpnTunnelService::class.java).apply {
            action = VpnTunnelService.ACTION_CONNECT
            putExtra(VpnTunnelService.EXTRA_CONFIG, config)
        }
        context.startService(intent)

        // Simulate connection delay
        viewModelScope.launch {
            delay(1500)
            if (VpnTunnelService.isRunning) {
                startTimestamp = System.currentTimeMillis()
                _connectionState.value = ConnectionState.Connected(latencyMs = checkLatency(), durationSeconds = 0)
                startCounters()
            } else {
                _connectionState.value = ConnectionState.Error("Failed to start VPN engine.")
            }
        }
    }

    fun disconnect(context: Context) {
        _connectionState.value = ConnectionState.Disconnecting
        val intent = Intent(context, VpnTunnelService::class.java).apply {
            action = VpnTunnelService.ACTION_DISCONNECT
        }
        context.startService(intent)

        viewModelScope.launch {
            delay(500)
            stopCounters()
            _connectionState.value = ConnectionState.Disconnected
        }
    }

    private suspend fun checkLatency(): Int {
        return withContext(Dispatchers.IO) {
            try {
                val start = System.currentTimeMillis()
                val socket = Socket()
                socket.connect(InetSocketAddress("8.8.8.8", 53), 2000)
                socket.close()
                (System.currentTimeMillis() - start).toInt()
            } catch (e: Exception) {
                -1 // Represents timeout/error
            }
        }
    }

    private fun startCounters() {
        durationJob?.cancel()
        durationJob = viewModelScope.launch {
            while (true) {
                val duration = (System.currentTimeMillis() - startTimestamp) / 1000
                val currentLatency = if (_connectionState.value is ConnectionState.Connected) {
                    (_connectionState.value as ConnectionState.Connected).latencyMs
                } else { 0 }
                _connectionState.value = ConnectionState.Connected(latencyMs = currentLatency, durationSeconds = duration)
                delay(1000)
            }
        }

        speedSimJob?.cancel()
        speedSimJob = viewModelScope.launch {
            while (true) {
                _downloadSpeed.value = Random.nextLong(10_000, 5_000_000)
                _uploadSpeed.value = Random.nextLong(5_000, 2_000_000)
                delay(1000)
            }
        }

        pingJob?.cancel()
        pingJob = viewModelScope.launch {
            while (true) {
                delay(5000)
                if (_connectionState.value is ConnectionState.Connected) {
                    val duration = (_connectionState.value as ConnectionState.Connected).durationSeconds
                    val latency = checkLatency()
                    _connectionState.value = ConnectionState.Connected(latencyMs = latency, durationSeconds = duration)
                }
            }
        }
    }

    private fun stopCounters() {
        durationJob?.cancel()
        speedSimJob?.cancel()
        pingJob?.cancel()
        _uploadSpeed.value = 0
        _downloadSpeed.value = 0
    }
}
