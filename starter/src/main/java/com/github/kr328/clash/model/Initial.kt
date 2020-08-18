package com.github.kr328.clash.model

import com.github.kr328.clash.Utils
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Initial(
        @SerialName("mode") val mode: String = "redirect",
        @SerialName("blacklist") val blacklist: List<String> =
                listOf("package:com.android.bluetooth", "uid:65534", "user:radio")
) {
    @Serializable
    class FMode(@SerialName("mode") val mode: String)

    @Serializable
    class FBlacklist(@SerialName("blacklist") val blacklist: List<String>)

    companion object {
        val DEFAULT = Initial()

        fun parse(data: String): Initial {
            return Utils.YAML.decodeFromString(serializer(), data)
        }
    }
}