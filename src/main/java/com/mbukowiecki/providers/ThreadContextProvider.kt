/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.mbukowiecki.providers

import com.intellij.debugger.engine.JavaValue
import com.intellij.icons.AllIcons
import com.intellij.lang.java.JavaLanguage
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator
import com.intellij.xdebugger.frame.XValue
import com.intellij.xdebugger.impl.breakpoints.XExpressionImpl
import com.mbukowiecki.bundle.ThreadAccessBundle
import com.mbukowiecki.listener.ThreadAccessDebugSessionListener
import com.sun.jdi.BooleanValue
import com.sun.jdi.Value
import javax.swing.DefaultListModel
import javax.swing.Icon

/**
 * @author Marcin Bukowiecki
 */
fun setupContext(caller: ThreadAccessDebugSessionListener, currentExecutionId: Int) {
    val model = caller.form.threadAccessList.model as? DefaultListModel<PresentationWrapper> ?: return
    val context = SetupContext(model, currentExecutionId, caller)

    checkIfIsPluginDebugging(context) {
        model.clear()
        context.caller.checkTab()

        isWriteThread(context) {
            isWriteAccessAllowed(context) {
                isReadAccessAllowed(context) {
                    isDispatchThread(context) {
                        //isManagerThread(context)
                    }
                }
            }
        }
    }
}

data class SetupContext(
    val model: DefaultListModel<PresentationWrapper>,
    val currentExecutionId: Int,
    val caller: ThreadAccessDebugSessionListener
)

interface PresentationWrapper {
    val icon: Icon
    fun getPresentationText(): String
}

abstract class PresentationWrapperBase : PresentationWrapper {

    override fun toString(): String {
        return getPresentationText()
    }
}

class BasePresentationWrapper(
    private val label: String,
    private val status: String,
    override val icon: Icon
) : PresentationWrapperBase() {

    override fun getPresentationText(): String {
        return "$label $status"
    }
}

fun checkIfIsPluginDebugging(context: SetupContext, nextCall: () -> Unit) {
    if (context.caller.toIgnore) return

    context.caller.log.info("Checking if plugin is starting...")

    getStatus(
        context,
        "ApplicationManager.getApplication().isInternal()",
        "com.intellij.openapi.application.ApplicationManager",
        object : RenderCallback(context) {

            override fun renderStatus(value: Value?, status: String, icon: Icon) {
                if (value == null || (value is BooleanValue && !value.value())) {
                    context.caller.toIgnore = true
                    context.caller.log.info("Not debugging plugin")
                } else {
                    context.caller.log.info("Debugging plugin")
                }
                nextCall.invoke()
            }
        }
    )
}

fun isWriteThread(context: SetupContext, nextCall: () -> Unit) {
    getStatus(
        context,
        "ApplicationManager.getApplication().isWriteThread()",
        "com.intellij.openapi.application.ApplicationManager",
        RenderCallback(context, ThreadAccessBundle.message("writeThread.label"), nextCall)
    )
}

fun isWriteAccessAllowed(context: SetupContext, nextCall: () -> Unit) {
    getStatus(
        context,
        "ApplicationManager.getApplication().isWriteAccessAllowed()",
        "com.intellij.openapi.application.ApplicationManager",
        RenderCallback(context, ThreadAccessBundle.message("writeAccessAllowed.label"), nextCall)
    )
}

fun isReadAccessAllowed(context: SetupContext, nextCall: () -> Unit) {
    getStatus(
        context,
        "ApplicationManager.getApplication().isReadAccessAllowed()",
        "com.intellij.openapi.application.ApplicationManager",
        RenderCallback(context, ThreadAccessBundle.message("readAccessAllowed.label"), nextCall)
    )
}

fun isDispatchThread(context: SetupContext, nextCall: () -> Unit) {
    getStatus(
        context,
        "ApplicationManager.getApplication().isDispatchThread()",
        "com.intellij.openapi.application.ApplicationManager",
        RenderCallback(context, ThreadAccessBundle.message("dispatchThread.label"), nextCall)
    )
}

fun isManagerThread(context: SetupContext, nextCall: () -> Unit) {
    getStatus(
        context,
        "DebuggerManagerThreadImpl.isManagerThread()",
        "com.intellij.debugger.engine.DebuggerManagerThreadImpl",
        RenderCallback(context, ThreadAccessBundle.message("managerThread.label"), nextCall)
    )
}

open class RenderCallback(
    private val context: SetupContext,
    private val label: String,
    private val nextCall: () -> Unit
) {

    constructor(context: SetupContext): this(context, "", {})

    open fun renderStatus(value: Value?, status: String, icon: Icon) {
        if (context.caller.executionRunId.get() == context.currentExecutionId) {
            context.model.addElement(BasePresentationWrapper(label, status, icon))
        }
        nextCall.invoke()
    }
}

private fun getStatus(
    context: SetupContext,
    expression: String,
    customInfo: String,
    renderCallback: RenderCallback
) {
    if (context.caller.toIgnore) return

    val expr = XExpressionImpl(expression, JavaLanguage.INSTANCE, customInfo)
    context.caller.debugProcess.evaluator?.evaluate(expr, object : XDebuggerEvaluator.XEvaluationCallback {

        override fun errorOccurred(errorMessage: String) {
            renderCallback.renderStatus(
                null,
                errorMessage,
                AllIcons.General.InspectionsWarning
            )
        }

        override fun evaluated(result: XValue) {
            (result as? JavaValue).let {
                val evaluateException = it?.descriptor?.evaluateException
                if (evaluateException != null) {
                    renderCallback.renderStatus(
                        null,
                        evaluateException.message ?: "Exception while obtaining access",
                        AllIcons.General.InspectionsWarning
                    )
                } else {
                    it?.descriptor?.value.let { value ->
                        if (value is BooleanValue) {
                            renderCallback.renderStatus(
                                value,
                                getBooleanPresentation(value.value()),
                                getBooleanIcon(value.value())
                            )
                        } else {
                            renderCallback.renderStatus(
                                null,
                                "Unknown access",
                                AllIcons.General.InspectionsWarning
                            )
                        }
                    }
                }
            }
        }

    }, null)
}

private fun getBooleanPresentation(value: Boolean): String {
    return if (value) {
        ThreadAccessBundle.message("yes")
    } else {
        ThreadAccessBundle.message("no")
    }
}

private fun getBooleanIcon(value: Boolean): Icon {
    return if (value) {
        AllIcons.General.InspectionsOK
    } else {
        AllIcons.General.Error
    }
}
