/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.mbukowiecki.providers

import com.intellij.openapi.components.ServiceManager
import com.mbukowiecki.evaluator.ThreadAccessEvaluator
import com.mbukowiecki.listener.ThreadAccessDebugSessionListener
import com.sun.jdi.BooleanValue
import com.sun.jdi.Value
import javax.swing.DefaultListModel
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
        val model = caller.form.threadAccessList.model as? DefaultListModel<PresentationWrapper> ?: return
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
            return ServiceManager.getService(ThreadAccessStatusesProvider::class.java)
        }
    }
}

/**
 * @author Marcin Bukowiecki
 */
data class SetupContext(
    val model: DefaultListModel<PresentationWrapper>,
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

    ThreadAccessEvaluator.getInstance().getStatus(
        context,
        "ApplicationManager.getApplication().isInternal()",
        "com.intellij.openapi.application.ApplicationManager",
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
    protected val label: String
) {

    constructor(context: SetupContext): this(context, "")

    open fun run(value: Value?, status: String, icon: Icon, errorOccurred: Boolean = false) {
        if (context.isValid()) {
            context.model.addElement(PresentationWrapperImpl(label, status, icon))
        }
        context.nextCallProvider.getNextCall().provide(context)
    }
}
