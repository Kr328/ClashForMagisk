package com.github.kr328.clash.model

import androidx.annotation.Keep
import com.github.kr328.clash.Utils
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Clash(
        @SerialName("port") val httpPort: Int = 0,
        @SerialName("redir-port") val redirectPort: Int = 0,
        @SerialName("socks-port") val socksPort: Int = 0,
        @SerialName("external-controller") val controller: String = "",
        @SerialName("dns") val dns: Dns = Dns(),
        @SerialName("tun") val tun: Tun = Tun()
) {
    @Keep
    @Serializable
    data class Dns(
            @SerialName("enable") val enable: Boolean = false,
            @SerialName("listen") val listen: String = ":0"
    )

    @Keep
    @Serializable
    data class Tun(
            @SerialName("enable") val enable: Boolean = false,
            @SerialName("device-url") val deviceUrl: String = "",
            @SerialName("dns-listen") val dnsListen: String = ""
    )

    companion object {
        fun parse(data: String): Clash {
            return Utils.YAML.parse(serializer(), data)
        }
    }
}