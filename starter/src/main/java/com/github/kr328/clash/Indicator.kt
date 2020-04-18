package com.github.kr328.clash

import java.io.File

class Indicator(private val dataDir: File) {
    companion object {
        const val FILE_STARTED = "RUNNING"
        const val FILE_STOPPED = "STOPPED"
    }

    fun started() {
        delete()

        dataDir.resolve(FILE_STARTED).createNewFile()
    }

    fun stopped() {
        delete()

        dataDir.resolve(FILE_STOPPED).createNewFile()
    }

    private fun delete() {
        dataDir.resolve(FILE_STARTED).delete()
        dataDir.resolve(FILE_STOPPED).delete()
    }
}