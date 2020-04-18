package com.github.kr328.clash

import android.util.Log
import java.io.File
import java.io.IOException

class ExecSetup(private val attribute: ExecAttribute) {
    companion object {
        const val MODE_SCRIPT_DIR = "mode.d"

        const val SCRIPT_PARSER = "/system/bin/sh"

        const val SCRIPT_PREPARE = "on-prepare.sh"
        const val SCRIPT_STARTED = "on-start.sh"
        const val SCRIPT_STOPPED = "on-stop.sh"
    }

    fun prepare() = exec(SCRIPT_PREPARE)
    fun started() = exec(SCRIPT_STARTED)
    fun stopped() = exec(SCRIPT_STOPPED)

    private fun exec(script: String) {
        val path = scriptPath(script) ?: return

        val process = with(ProcessBuilder()) {
            command(SCRIPT_PARSER)

            environment().putAll(attribute.environment)

            start()
        }

        try {
            process.apply {
                outputStream.write("exec sh \"$path\" 2>&1 ; exit -1\n".toByteArray())
                outputStream.flush()

                inputStream.bufferedReader().forEachLine {
                    Log.i(Constants.TAG, "[setup] $it")
                }

                if (process.waitFor() != 0)
                    throw IOException("Setup script return failure")
            }
        } finally {
            process.destroy()
        }
    }

    private fun scriptPath(script: String): File? {
        val suffix = "$MODE_SCRIPT_DIR/${attribute.initial.mode}/$script"

        return attribute.dataDir.resolve(suffix).takeIf(File::exists)
                ?: attribute.coreDir.resolve(suffix).takeIf(File::exists)
    }
}