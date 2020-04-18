package com.github.kr328.clash

import android.util.Log
import java.io.BufferedReader
import java.io.IOException

class ExecProcess(attribute: ExecAttribute) {
    private val setup = ExecSetup(attribute)
    private val process: Process
    private val input: BufferedReader
    private var processId: Int = 0

    init {
        try {
            setup.prepare()

            Log.i(Constants.TAG, "Clash starting")

            process = Runtime.getRuntime().exec(SHELL)

            process.outputStream.write("""
                echo "PID=[$$]"
                exec \
                ${attribute.coreDir}/setuidgid \
                ${attribute.user} ${attribute.group} \
                ${attribute.groups.joinToString(separator = ",")} \
                ${attribute.coreDir}/clash -d ${attribute.dataDir} 2>&1
                exit -1
                """.trimIndent().toByteArray())
            process.outputStream.flush()

            input = process.inputStream.bufferedReader()

            while (true) {
                val line = input.readLine()?.trim()
                        ?: throw IOException("Start clash process failure")

                PATTERN_PID.matchEntire(line)?.apply {
                    processId = groupValues[1].toInt()
                } ?: continue

                break
            }

            if (processId <= 0)
                throw IOException("Invalid pid $processId")

            setup.started()
        } catch (e: Exception) {
            setup.stopped()
            throw e
        }
    }

    fun kill() {
        android.os.Process.killProcess(processId)
    }

    fun exec() {
        try {
            while (true) {
                val line = input.readLine()?.trim() ?: break

                val matched = PATTERN_LOG.matchEntire(line)

                if (matched == null) {
                    Log.v(Constants.TAG, line)
                    continue
                }

                val level = matched.groupValues[1]
                val message = matched.groupValues[2]

                when (level) {
                    "info" ->
                        Log.i(Constants.TAG, message)
                    "warning" ->
                        Log.w(Constants.TAG, message)
                    "error" ->
                        Log.e(Constants.TAG, message)
                    "debug" ->
                        Log.d(Constants.TAG, message)
                    else ->
                        Log.v(Constants.TAG, message)
                }
            }
        } finally {
            Log.i(Constants.TAG, "Clash process exited")
            setup.stopped()
        }
    }

    companion object {
        private const val SHELL = "/system/bin/sh"
        private val PATTERN_PID = Regex("PID=\\[(\\d+)]")
        private val PATTERN_LOG = Regex("time=\".+\"\\s+level=(\\S+)\\s+msg=\"(.*)\"")
    }
}