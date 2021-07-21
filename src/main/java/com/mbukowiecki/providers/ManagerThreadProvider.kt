/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.mbukowiecki.providers

import com.mbukowiecki.bundle.ThreadAccessBundle
import com.mbukowiecki.evaluator.ThreadAccessEvaluator

/**
 * @author Marcin Bukowiecki
 */
@Suppress("unused")
class ManagerThreadProvider : AccessProvider {

    override fun provide(context: SetupContext) {
        ThreadAccessEvaluator.getInstance().getStatus(
            context,
            "DebuggerManagerThreadImpl.isManagerThread()",
            "com.intellij.debugger.engine.DebuggerManagerThreadImpl",
            CheckCallback(
                context,
                ThreadAccessBundle.message("managerThread.label")
            )
        )
    }
}