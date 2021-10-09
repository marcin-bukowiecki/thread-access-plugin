/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.mbukowiecki.evaluator

import com.intellij.debugger.engine.JavaValue
import com.intellij.icons.AllIcons
import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ServiceManager
import com.intellij.xdebugger.evaluation.EvaluationMode
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator
import com.intellij.xdebugger.frame.XValue
import com.intellij.xdebugger.impl.breakpoints.XExpressionImpl
import com.mbukowiecki.bundle.ThreadAccessBundle
import com.mbukowiecki.providers.CheckCallback
import com.mbukowiecki.providers.SetupContext
import com.sun.jdi.BooleanValue
import javax.swing.Icon

/**
 * @author Marcin Bukowiecki
 */
class ThreadAccessEvaluator {

    fun getStatus(
        context: SetupContext,
        expression: String,
        customInfo: String,
        checkCallback: CheckCallback,
        evaluationMode: EvaluationMode = EvaluationMode.EXPRESSION
    ) {
        if (context.caller.toIgnore) return

        val expr = XExpressionImpl(expression, JavaLanguage.INSTANCE, customInfo, evaluationMode)

        context.caller.debugProcess.evaluator?.evaluate(expr, object : XDebuggerEvaluator.XEvaluationCallback {

            override fun errorOccurred(errorMessage: String) {
                checkCallback.run(
                    null,
                    errorMessage,
                    AllIcons.General.InspectionsWarning,
                    true
                )
            }

            override fun evaluated(result: XValue) {
                (result as? JavaValue).let {
                    val evaluateException = it?.descriptor?.evaluateException
                    if (evaluateException != null) {
                        checkCallback.run(
                            null,
                            evaluateException.message ?: ThreadAccessBundle.message("threadAccessInfo.access.exception"),
                            AllIcons.General.InspectionsWarning,
                            true
                        )
                    } else {
                        it?.descriptor?.value.let { value ->
                            if (value is BooleanValue) {
                                checkCallback.run(
                                    value,
                                    getBooleanPresentation(value.value()),
                                    getBooleanIcon(value.value())
                                )
                            } else {
                                checkCallback.run(
                                    null,
                                    ThreadAccessBundle.message("threadAccessInfo.access.unknown"),
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
            ThreadAccessBundle.message("threadAccessInfo.yes")
        } else {
            ThreadAccessBundle.message("threadAccessInfo.no")
        }
    }

    private fun getBooleanIcon(value: Boolean): Icon {
        return if (value) {
            AllIcons.General.InspectionsOK
        } else {
            AllIcons.General.Error
        }
    }

    companion object {

        fun getInstance(): ThreadAccessEvaluator {
            return ApplicationManager.getApplication().getService(ThreadAccessEvaluator::class.java)
        }
    }
}
