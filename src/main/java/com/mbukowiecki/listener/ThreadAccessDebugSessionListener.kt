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
import com.mbukowiecki.providers.ThreadAccessStatusesProvider
import com.mbukowiecki.ui.ThreadAccessInfoTab
import java.util.concurrent.atomic.AtomicInteger

/**
 * @author Marcin Bukowiecki
 */
class ThreadAccessDebugSessionListener(
    val debugProcess: XDebugProcess,
    val tab: ThreadAccessInfoTab
) : XDebugSessionListener {

    val log = Logger.getInstance(ThreadAccessDebugSessionListener::class.java)

    val project = debugProcess.session.project

    @Volatile
    private var tabAdded = false

    @Volatile
    var checked: Boolean = false

    @Volatile
    var toIgnore = false

    val executionRunId = AtomicInteger()

    override fun sessionPaused() {
        handle()
    }

    override fun sessionStopped() {
        handle()
    }

    fun checkTab() {
        if (!tabAdded) {
            tabAdded = true

            if (toIgnore) return

            log.info("Initializing Thread Access tab...")

            ApplicationManager.getApplication().invokeLater {
                val myUi = debugProcess.session?.ui ?: return@invokeLater
                val content = myUi.createContent(
                    ThreadAccessBundle.message("threadAccessInfo.debugger.tab.id"),
                    tab.mainPanel,
                    ThreadAccessBundle.message("threadAccessInfo.debugger.tab.name"),
                    null,
                    null
                )
                content.isCloseable = false
                myUi.addContent(content)
                log.info("Thread Access tab initialized")
            }
        }
    }

    private fun handle() {
        if (toIgnore) return

        val currentExecutionId = executionRunId.incrementAndGet()

        (debugProcess.session.suspendContext as? SuspendContextImpl)?.let { ctx ->
            ThreadAccessStatusesProvider.getInstance().setupStatuses(this, currentExecutionId)
            setupThreadName(ctx, currentExecutionId)
        }
    }

    private fun setupThreadName(ctx: SuspendContextImpl, currentExecutionId: Int) {
        val currentThread = ctx.thread
        val threadName = if (currentThread == null) {
            ThreadAccessBundle.message("threadAccessInfo.thread.name.unknown")
        } else {
            currentThread.name() ?: ThreadAccessBundle.message("threadAccessInfo.thread.name.unknown")
        }
        if (currentExecutionId == executionRunId.get()) tab.threadNameLabel.text = ThreadAccessBundle.message("threadAccessInfo.thread.name", threadName)
    }
}
