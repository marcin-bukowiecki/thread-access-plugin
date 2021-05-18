/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.mbukowiecki.listener

import com.intellij.debugger.engine.SuspendContextImpl
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.xdebugger.XDebugProcess
import com.intellij.xdebugger.XDebugSessionListener
import com.mbukowiecki.bundle.ThreadAccessBundle
import com.mbukowiecki.providers.setupContext
import com.mbukowiecki.ui.ThreadAccessTabForm
import java.util.concurrent.atomic.AtomicInteger

/**
 * @author Marcin Bukowiecki
 */
class ThreadAccessDebugSessionListener(val debugProcess: XDebugProcess,
                                       val form: ThreadAccessTabForm) : XDebugSessionListener {

    val log = Logger.getInstance(ThreadAccessDebugSessionListener::class.java)

    @Volatile
    private var tabAdded = false

    @Volatile
    var toIgnore = false

    val executionRunId = AtomicInteger()

    override fun sessionPaused() {
        handle()
    }

    override fun sessionStopped() {
        handle()
    }

    private fun handle() {
        if (toIgnore) return

        val currentExecutionId = executionRunId.incrementAndGet()
        (debugProcess.session.suspendContext as? SuspendContextImpl)?.let {
                ctx ->
            form.threadNameLabel.text = "Thread name: " + (ctx.thread?.name() ?: "Unknown thread")
            setupContext(this, currentExecutionId)
        }
    }

    fun checkTab() {
        if (!tabAdded) {
            tabAdded = true
            if (toIgnore) return

            log.info("Initializing Thread Access tab...")
            ApplicationManager.getApplication().invokeLater {
                val myUi = debugProcess.session?.ui ?: return@invokeLater
                val content = myUi.createContent(
                    ThreadAccessBundle.message("thread.debugger.tab.id"),
                    form.mainPanel,
                    ThreadAccessBundle.message("thread.debugger.tab.name"),
                    null,
                    null
                )
                myUi.addContent(content)
                log.info("Thread Access tab initialized")
            }
        }
    }
}