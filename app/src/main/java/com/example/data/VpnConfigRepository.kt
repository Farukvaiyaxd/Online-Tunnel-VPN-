package com.example.data

import kotlinx.coroutines.flow.Flow
import org.json.JSONObject

class VpnConfigRepository(private val vpnConfigDao: VpnConfigDao) {

    val allConfigs: Flow<List<VpnConfig>> = vpnConfigDao.getAllConfigs()

    suspend fun insert(config: VpnConfig) {
        vpnConfigDao.insertConfig(config)
    }

    suspend fun update(config: VpnConfig) {
        vpnConfigDao.updateConfig(config)
    }

    suspend fun delete(config: VpnConfig) {
        vpnConfigDao.deleteConfig(config)
    }

    fun exportToJson(config: VpnConfig, locked: Boolean = false): String {
        val json = JSONObject()
        json.put("name", config.name)
        json.put("serverAddress", if (locked) "***.***.***.***" else config.serverAddress)
        json.put("port", if (locked) 0 else config.port)
        json.put("uuid", if (locked) "********-****-****-****-********" else config.uuid)
        json.put("alterId", config.alterId)
        json.put("networkType", config.networkType)
        json.put("tls", config.tls)
        json.put("sni", config.sni)
        json.put("protocol", config.protocol)
        json.put("realityPublicKey", config.realityPublicKey)
        json.put("flow", config.flow)
        return json.toString(4)
    }

    suspend fun importFromJson(jsonString: String) {
        try {
            val json = JSONObject(jsonString)
            val config = VpnConfig(
                name = json.optString("name", "Imported Config"),
                serverAddress = json.optString("serverAddress", ""),
                port = json.optInt("port", 443),
                uuid = json.optString("uuid", ""),
                alterId = json.optInt("alterId", 0),
                networkType = json.optString("networkType", "tcp"),
                tls = json.optBoolean("tls", false),
                sni = json.optString("sni", ""),
                protocol = json.optString("protocol", "vmess"),
                realityPublicKey = if (json.has("realityPublicKey")) json.getString("realityPublicKey") else null,
                flow = if (json.has("flow")) json.getString("flow") else null
            )
            insert(config)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
