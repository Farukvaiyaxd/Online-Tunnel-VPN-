package com.example.data

import android.content.Context
import android.content.SharedPreferences

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("vpn_settings", Context.MODE_PRIVATE)

    var autoConnect: Boolean
        get() = prefs.getBoolean("auto_connect", false)
        set(value) = prefs.edit().putBoolean("auto_connect", value).apply()

    var bypassLan: Boolean
        get() = prefs.getBoolean("bypass_lan", true)
        set(value) = prefs.edit().putBoolean("bypass_lan", value).apply()

    var blockIpv6: Boolean
        get() = prefs.getBoolean("block_ipv6", false)
        set(value) = prefs.edit().putBoolean("block_ipv6", value).apply()

    var customDns: String
        get() = prefs.getString("custom_dns", "8.8.8.8, 1.1.1.1") ?: "8.8.8.8, 1.1.1.1"
        set(value) = prefs.edit().putString("custom_dns", value).apply()

    var bypassLocations: String
        get() = prefs.getString("bypass_locations", "geoip:private") ?: "geoip:private"
        set(value) = prefs.edit().putString("bypass_locations", value).apply()
}
