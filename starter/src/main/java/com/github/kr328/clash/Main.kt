package com.github.kr328.clash

import android.util.Log
import java.io.File
import kotlin.system.exitProcess

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        if (args.size != 2) {
            System.err.println("Usage: app_process /system/bin com.github.kr328.clash.Starter [CORE-DIR] [DATA-DIR]")
            exitProcess(1)
        }

        Log.i(Constants.TAG, "Starter started")

        Starter(File(args[0]), File(args[1])).exec()

        exitProcess(0)
    }
}