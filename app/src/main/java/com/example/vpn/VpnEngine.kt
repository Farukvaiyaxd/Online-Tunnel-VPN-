package com.example.vpn

import android.os.ParcelFileDescriptor
import com.example.data.VpnConfig

interface VpnEngine {
    fun start(config: VpnConfig, fileDescriptor: ParcelFileDescriptor): Boolean
    fun stop()
}
