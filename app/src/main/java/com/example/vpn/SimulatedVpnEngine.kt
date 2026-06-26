package com.example.vpn

import android.os.ParcelFileDescriptor
import android.util.Log
import com.example.data.VpnConfig

class SimulatedVpnEngine : VpnEngine {
    private var isRunning = false

    override fun start(config: VpnConfig, fileDescriptor: ParcelFileDescriptor): Boolean {
        Log.d("SimulatedVpnEngine", "Starting simulation for config: ${config.name}")
        // TODO: Replace with real Xray/V2Ray native initialization
        // e.g., XrayCore.start(config.toNativeFormat(), fileDescriptor.fd)
        isRunning = true
        return true
    }

    override fun stop() {
        Log.d("SimulatedVpnEngine", "Stopping simulation.")
        // TODO: Replace with real native stop
        // e.g., XrayCore.stop()
        isRunning = false
    }
}
