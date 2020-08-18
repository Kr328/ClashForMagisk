package com.github.kr328.clash

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.github.kr328.clash.model.Clash
import com.github.kr328.clash.model.Initial
import java.io.File
import java.io.IOException
import java.net.InetAddress

object Utils {
    val YAML = Yaml(configuration = YamlConfiguration(strictMode = false))

    fun splitAddressPort(str: String): Pair<InetAddress, Int> {
        val indexOfPort = str.lastIndexOf(":")

        require(indexOfPort >= 0) { "Port not found" }

        val host = str.substring(0..indexOfPort)
        val port = str.substring(indexOfPort)

        return InetAddress.getByName(host) to port.toInt()
    }

    fun parseClash(dataDir: File): Clash {
        val config = dataDir.resolve("config.yaml").takeIf(File::exists)
                ?: throw IOException("Config not found")

        return Clash.parse(config.readText())
    }

    fun parseInitial(coreDir: File, dataDir: File): Initial {
        val config = dataDir.resolve("starter.yaml")
        val template = coreDir.resolve("starter.template.yaml")

        return try {
            if (config.lastModified() < template.lastModified())
                throw IOException("Out of date config")
            Initial.parse(config.readText())
        } catch (e: Exception) {
            val initial = runCatching {
                Initial.parse(config.readText())
            }.getOrElse {
                Initial.DEFAULT
            }

            val content = template.readText()
                    .replace("%%MODE%%",
                            YAML.encodeToString(Initial.FMode.serializer(), Initial.FMode(initial.mode)))
                    .replace("%%BLACKLIST%%",
                            YAML.encodeToString(Initial.FBlacklist.serializer(), Initial.FBlacklist(initial.blacklist)))

            config.writeText(content)

            initial
        }
    }
}