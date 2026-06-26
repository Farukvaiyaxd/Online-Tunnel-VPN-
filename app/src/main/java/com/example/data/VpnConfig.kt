package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "vpn_configs")
data class VpnConfig(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val serverAddress: String,
    val port: Int,
    val uuid: String,
    val alterId: Int = 0,
    val networkType: String = "tcp", // tcp, kws, ws, grpc, quic
    val tls: Boolean = false,
    val sni: String = "",
    val protocol: String = "vmess", // vmess, vless, trojan, shadowsocks, wireguard
    val realityPublicKey: String? = null,
    val flow: String? = null // xtls-rprx-vision
) : Serializable
