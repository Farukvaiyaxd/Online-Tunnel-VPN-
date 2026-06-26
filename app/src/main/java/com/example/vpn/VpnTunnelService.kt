package com.example.vpn

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.VpnConfig

class VpnTunnelService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null
    private var engine: VpnEngine? = null
    private var activeConfig: VpnConfig? = null

    companion object {
        const val ACTION_CONNECT = "com.example.vpn.CONNECT"
        const val ACTION_DISCONNECT = "com.example.vpn.DISCONNECT"
        const val ACTION_RECONNECT = "com.example.vpn.RECONNECT"
        const val EXTRA_CONFIG = "extra_config"
        const val NOTIFICATION_CHANNEL_ID = "vpn_channel"
        const val NOTIFICATION_ID = 1

        var isRunning = false
            private set
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CONNECT -> {
                val config = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getSerializableExtra(EXTRA_CONFIG, VpnConfig::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getSerializableExtra(EXTRA_CONFIG) as? VpnConfig
                }
                config?.let { startVpn(it) }
            }
            ACTION_DISCONNECT -> {
                stopVpn()
            }
            ACTION_RECONNECT -> {
                val config = activeConfig
                stopVpn(shutdownService = false)
                config?.let { startVpn(it) }
            }
        }
        return START_STICKY
    }

    private fun startVpn(config: VpnConfig) {
        if (isRunning) stopVpn(shutdownService = false)
        activeConfig = config

        setupNotification(config)

        try {
            val builder = Builder()
                .setSession(config.name)
                .addAddress("10.10.10.2", 32)
                .addAddress("fd00::1", 64)
                .addRoute("0.0.0.0", 0)
                .addRoute("10.10.10.0", 24)
                .addRoute("192.168.254.0", 24)
                .setMtu(1500)
                .setBlocking(true)

            // TODO: Fetch bypass locations from SettingsManager (e.g. geoip:private, geosite:cn)
            // and pass them to the underlying V2Ray/Xray engine for secure routing rules.
            
            // TODO: Use protect(socket) natively inside the real engine to prevent routing loops.
            // When migrating to real XrayCore/V2Ray, pass a callback to protect native sockets.

            vpnInterface = builder.establish()

            if (vpnInterface != null) {
                engine = SimulatedVpnEngine()
                val success = engine?.start(config, vpnInterface!!)
                if (success == true) {
                    isRunning = true
                    Log.d("VpnTunnelService", "VPN Started Successfully")
                } else {
                    stopVpn()
                }
            } else {
                Log.e("VpnTunnelService", "Failed to establish VPN interface. Did you call VpnService.prepare()?")
                stopSelf()
            }

        } catch (e: Exception) {
            Log.e("VpnTunnelService", "Error starting VPN", e)
            stopVpn()
        }
    }

    private fun stopVpn(shutdownService: Boolean = true) {
        engine?.stop()
        engine = null
        try {
            vpnInterface?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        vpnInterface = null
        isRunning = false
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
        
        if (shutdownService) {
            stopSelf()
        }
    }

    private fun setupNotification(config: VpnConfig) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "VPN Status",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }

        val mainIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val disconnectIntent = Intent(this, VpnTunnelService::class.java).apply {
            action = ACTION_DISCONNECT
        }
        val disconnectPendingIntent = PendingIntent.getService(
            this, 1, disconnectIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val reconnectIntent = Intent(this, VpnTunnelService::class.java).apply {
            action = ACTION_RECONNECT
        }
        val reconnectPendingIntent = PendingIntent.getService(
            this, 2, reconnectIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Online Tunnel Active")
            .setContentText("Connected to ${config.name}")
            .setSmallIcon(android.R.drawable.ic_secure) // TODO: use real app icon
            .setContentIntent(pendingIntent)
            .setUsesChronometer(true)
            .setWhen(System.currentTimeMillis())
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Disconnect", disconnectPendingIntent)
            .addAction(android.R.drawable.ic_menu_rotate, "Reconnect", reconnectPendingIntent)
            .setOngoing(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopVpn()
    }
}
