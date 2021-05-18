/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.mbukowiecki.listener

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.xdebugger.XDebugProcess
import com.intellij.xdebugger.XDebuggerManager
import com.intellij.xdebugger.XDebuggerManagerListener
import com.mbukowiecki.ui.ThreadAccessTabForm

/**
 * @author Marcin Bukowiecki
 */
class ProjectOpened : StartupActivity {

    @Suppress("UnstableApiUsage")
    override fun runActivity(project: Project) {
        project.messageBus.simpleConnect().subscribe(
            XDebuggerManager.TOPIC, object : XDebuggerManagerListener {

                override fun processStarted(debugProcess: XDebugProcess) {
                    debugProcess.session.addSessionListener(
                        ThreadAccessDebugSessionListener(
                            debugProcess,
                            ThreadAccessTabForm()
                        )
                    )
                }
            }
        )
    }
}