package com.github.kr328.clash

import android.util.Log
import java.io.File
import java.util.concurrent.LinkedBlockingQueue
import kotlin.concurrent.thread

class Starter(private val coreDir: File, private val dataDir: File) {
    private enum class Event {
        START, STOP, RESTART, STOPPED, STARTED
    }

    private val controlObserver = ControlObserver(dataDir, this::onUserControl)
    private val eventQueue = LinkedBlockingQueue<Event>()
    private val indicator = Indicator(dataDir)
    private var process: ExecProcess? = null

    fun exec() {
        controlObserver.start()

        eventQueue.offer(Event.STOPPED)
        eventQueue.offer(Event.START)

        loop@ while (true) {
            val event = eventQueue.poll() ?: continue

            try {
                when (event) {
                    Event.START -> {
                        if (process != null)
                            continue@loop

                        val initial = Utils.prepareInitial(coreDir, dataDir)
                        val clash = Utils.prepareClash(dataDir)

                        val attribute = ExecAttribute(coreDir, dataDir, initial, clash)

                        process = attribute.start()

                        thread {
                            eventQueue.offer(Event.STARTED)

                            try {
                                process?.exec()
                            } catch (e: Exception) {
                                Log.w(Constants.TAG, "Clash process failure", e)
                            }

                            eventQueue.offer(Event.STOPPED)
                        }
                    }
                    Event.STOP -> {
                        if (process == null)
                            continue@loop

                        process?.kill()

                        eventQueue.offer(eventQueue.poll() ?: continue@loop)
                    }
                    Event.RESTART -> {
                        eventQueue.offer(Event.STOP)
                        eventQueue.offer(Event.START)
                    }
                    Event.STARTED -> {
                        Log.i(Constants.TAG, "Clash started")

                        indicator.started()
                    }
                    Event.STOPPED -> {
                        Log.i(Constants.TAG, "Clash stopped")

                        process = null

                        indicator.stopped()
                    }
                }
            } catch (e: Exception) {
                Log.e(Constants.TAG, "Process Event $event failure", e)

                if (event == Event.STOP)
                    break@loop

                eventQueue.offer(Event.STOP)
            }
        }

        controlObserver.close()
    }

    private fun onUserControl(action: ControlObserver.Action) {
        Log.i(Constants.TAG, "Control $action received")

        when (action) {
            ControlObserver.Action.START ->
                eventQueue.offer(Event.START)
            ControlObserver.Action.RESTART ->
                eventQueue.offer(Event.RESTART)
            ControlObserver.Action.STOP ->
                eventQueue.offer(Event.STOP)
        }
    }
}