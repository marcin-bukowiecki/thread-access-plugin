/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.mbukowiecki.providers

import com.intellij.debugger.engine.SuspendContextImpl
import com.intellij.debugger.engine.evaluation.EvaluateException
import com.intellij.openapi.application.ApplicationManager
import com.mbukowiecki.evaluator.ThreadAccessEvaluator
import com.mbukowiecki.listener.ThreadAccessDebugSessionListener
import com.mbukowiecki.ui.ThreadStatusModel
import com.sun.jdi.BooleanValue
import com.sun.jdi.Value
import javax.swing.Icon

/**
 * @author Marcin Bukowiecki
 */
class ThreadAccessStatusesProvider {

    private val accessProviders = listOf(
        WriteThreadProvider(),
        WriteAccessProvider(),
        ReadAccessProvider(),
        DispatchThreadProvider(),
        IsDisposedProvider(),
        HoldsReadLockProvider(),
        InImpatientReaderProvider()
    )

    fun setupStatuses(caller: ThreadAccessDebugSessionListener, currentExecutionId: Int) {
        val model = caller.tab.threadStatusModel
        val nextCallProvider = NextCallProvider(accessProviders)
        val context = SetupContext(model, currentExecutionId, caller, nextCallProvider)

        checkIfIsPluginDebugging(context) {
            model.clear()
            context.caller.checkTab()
            nextCallProvider.getNextCall().provide(context)
        }
    }

    companion object {

        fun getInstance(): ThreadAccessStatusesProvider {
            return ApplicationManager.getApplication().getService(ThreadAccessStatusesProvider::class.java)
        }
    }
}

/**
 * @author Marcin Bukowiecki
 */
data class SetupContext(
    val model: ThreadStatusModel,
    val currentExecutionId: Int,
    val caller: ThreadAccessDebugSessionListener,
    val nextCallProvider: NextCallProvider
) {

    fun isValid(): Boolean = caller.executionRunId.get() == currentExecutionId
}

/**
 * @author Marcin Bukowiecki
 */
interface PresentationWrapper {
    val icon: Icon
    fun getPresentationText(): String
}

/**
 * @author Marcin Bukowiecki
 */
class PresentationWrapperImpl(
    private val label: String,
    private val status: String,
    override val icon: Icon
) : PresentationWrapper {

    override fun getPresentationText(): String {
        return "$label $status"
    }

    override fun toString(): String {
        return getPresentationText()
    }
}

/**
 * @author Marcin Bukowiecki
 */
fun checkIfIsPluginDebugging(context: SetupContext, nextCall: () -> Unit) {
    if (context.caller.toIgnore) return

    if (context.caller.checked) {
        nextCall.invoke()
        return
    }

    context.caller.log.info("Checking if debugging plugin...")

    val intellijClassMarkerName = "com.intellij.openapi.application.ApplicationManager"
    val xDebugProcess = context.caller.debugProcess
    (xDebugProcess.session.suspendContext as? SuspendContextImpl)?.debugProcess?.let { debugProcess -> // Get the "real" debug process
        debugProcess.debuggerContext.createEvaluationContext().let { evalContext -> // Can be null, in that case the class most likely won't be found
            try {
                val appManagerClassInProcess = debugProcess.findClass(evalContext, intellijClassMarkerName, evalContext?.classLoader)
                if (appManagerClassInProcess == null) {
                    context.caller.toIgnore = true
                    context.caller.log.info("Not debugging plugin")
                } else {
                    context.caller.log.info("Debugging plugin")
                    context.caller.checked = true
                }
                nextCall.invoke()
                return
            } catch (_: EvaluateException) {}
        }
    }

    ThreadAccessEvaluator.getInstance().getStatus(
        context,
        "ApplicationManager.getApplication().isInternal()",
        intellijClassMarkerName,
        object : CheckCallback(context) {

            override fun run(value: Value?, status: String, icon: Icon, errorOccurred: Boolean) {
                if (value == null || (value is BooleanValue && !value.value())) {
                    context.caller.toIgnore = true
                    context.caller.log.info("Not debugging plugin")
                } else {
                    context.caller.log.info("Debugging plugin")
                    context.caller.checked = true
                }
                nextCall.invoke()
            }
        }
    )
}

/**
 * @author Marcin Bukowiecki
 */
open class CheckCallback(
    protected val context: SetupContext,
    protected val label: String,
    val presentationColumn: Int = 0
) {

    constructor(context: SetupContext): this(context, "")

    open fun run(value: Value?, status: String, icon: Icon, errorOccurred: Boolean = false) {
        if (context.isValid()) {
            context.model.addElement(this, PresentationWrapperImpl(label, status, icon))
        }
        context.nextCallProvider.getNextCall().provide(context)
    }
}
