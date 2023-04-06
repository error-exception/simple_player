package com.simple.player.util

import android.os.*
import java.util.*
import java.util.concurrent.*

object ProgressHandler {

    private const val EXECUTE_AFTER = 11
    private val mHandler = MyHandler(Looper.getMainLooper(), null)
    private val executor: ThreadPoolExecutor
    private val taskList = LinkedList<Triple<(() -> Unit)?, (() -> Unit)?, (() -> Unit)?>>()
    init {
        val processorCount = Runtime.getRuntime().availableProcessors()
        var value = processorCount shr 1
        if (value <= 0) {
            value = 1
        }
        executor = ThreadPoolExecutor(
            value,
            processorCount,
            5,
            TimeUnit.SECONDS,
            ArrayBlockingQueue(processorCount),
            ThreadPoolExecutor.CallerRunsPolicy()
        )
    }

    fun handle(before: (() -> Unit)? = null,
               after: (() -> Unit)? = null,
               handle: (() -> Unit)? = null)
    {
        taskList.addFirst(Triple(before, handle, after))
        invokeTask()
    }

    private fun invokeTask() {
        val triple = taskList.removeLast()
        triple.first?.invoke()
        val run = Runnable {
            triple.second?.invoke()
            val msg = Message.obtain()
            msg.what = EXECUTE_AFTER
            msg.obj = triple
            mHandler.sendMessage(msg)
        }
        executor.execute(run)
    }

    fun shutdown() {
        if (!executor.isShutdown) {
            executor.shutdown()
        }
    }

    private class MyHandler(looper: Looper, parent: ProgressHandler?) : SimpleHandler<ProgressHandler?>(looper, parent) {
        override fun handleMessage(msg: Message) {
            if (msg.what == EXECUTE_AFTER) {
                val triple = msg.obj as Triple<*, *, *>
                val after = triple.third as (() -> Unit)?
                after?.invoke()

            }
            super.handleMessage(msg)
        }
    }
}