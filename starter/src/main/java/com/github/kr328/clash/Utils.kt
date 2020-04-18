package com.github.kr328.clash

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.github.kr328.clash.model.Clash
import com.github.kr328.clash.model.Initial
import java.io.File
import java.io.IOException

object Utils {
    val YAML = Yaml(configuration = YamlConfiguration(strictMode = false))

    fun splitHostPort(str: String): Pair<String, Int>? {
        val s = str.split(":")

        if (s.size != 2)
            return null

        val port = s[1].toIntOrNull() ?: return null

        return s[0] to port
    }

    fun prepareClash(dataDir: File): Clash {
        val config = dataDir.resolve("config.yaml").takeIf(File::exists)
                ?: dataDir.resolve("config.yml")
                ?: throw IOException("Config not found")

        return Clash.parse(config.readText())
    }

    fun prepareInitial(coreDir: File, dataDir: File): Initial {
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
                            YAML.stringify(Initial.FMode.serializer(), Initial.FMode(initial.mode)))
                    .replace("%%BLACKLIST%%",
                            YAML.stringify(Initial.FBlacklist.serializer(), Initial.FBlacklist(initial.blacklist)))

            config.writeText(content)

            initial
        }
    }
}