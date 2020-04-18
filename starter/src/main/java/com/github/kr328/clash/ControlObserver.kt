package com.github.kr328.clash

import android.os.FileObserver
import java.io.File
import java.util.*

class ControlObserver(private val dataDir: File, private val callback: (Action) -> Unit) {
    private var fileObserver: FileObserver? = null

    enum class Action {
        START, RESTART, STOP
    }

    fun start() {
        restart()
    }

    fun close() {
        fileObserver?.stopWatching()
    }

    @Suppress("DEPRECATION")
    private fun restart() {
        dataDir.mkdirs()

        fileObserver = object : FileObserver(dataDir.absolutePath, CREATE or DELETE_SELF or MOVED_FROM or MOVED_TO) {
            override fun onEvent(event: Int, file: String?) {
                val f = file?.toUpperCase(Locale.getDefault()) ?: return

                if (event and DELETE_SELF != 0) {
                    restart()
                } else {
                    callback(when (f) {
                        "START" -> Action.START
                        "RESTART" -> Action.RESTART
                        "STOP" -> Action.STOP
                        else -> return
                    })

                    dataDir.resolve(f).delete()
                }
            }
        }

        fileObserver!!.startWatching()
    }
}