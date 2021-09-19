/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.mbukowiecki.providers

import com.intellij.lang.jvm.JvmModifier
import com.intellij.openapi.application.ApplicationManager
import com.intellij.xdebugger.evaluation.EvaluationMode
import com.mbukowiecki.bundle.ThreadAccessBundle
import com.mbukowiecki.evaluator.ThreadAccessEvaluator
import com.mbukowiecki.utils.ThreadAccessUtils
import com.sun.jdi.Value
import javax.swing.Icon

/**
 * @author Marcin Bukowiecki
 */
@Suppress("UnstableApiUsage")
class IsDisposedProvider : AccessProvider {

    override fun provide(context: SetupContext) {
        val currentPosition = context.caller.debugProcess.session.currentPosition ?: kotlin.run {
            context.nextCallProvider.getNextCall().provide(context)
            return
        }
        ThreadAccessUtils.getMethod(context.caller.project, currentPosition)?.let {
            val hasModifier = ApplicationManager.getApplication().runReadAction<Boolean, Throwable> { it.hasModifier(JvmModifier.STATIC) }
            if (hasModifier) {
                context.nextCallProvider.getNextCall().provide(context)
            } else {
                ThreadAccessEvaluator.getInstance().getStatus(
                    context,
                    "if (this instanceof Disposable) { return Disposer.isDisposed(this); } else { return false; }",
                    "com.intellij.openapi.util.Disposer,com.intellij.openapi.Disposable",
                    IsDisposedCheckCallback(context, ThreadAccessBundle.message("threadAccessInfo.isDisposed.label")),
                    evaluationMode = EvaluationMode.CODE_FRAGMENT
                )
            }
        } ?: kotlin.run {
            context.nextCallProvider.getNextCall().provide(context)
        }
    }
}

/**
 * @author Marcin Bukowiecki
 */
class IsDisposedCheckCallback(context: SetupContext,
                              label: String) : CheckCallback(context, label, presentationColumn = 1) {

    override fun run(value: Value?, status: String, icon: Icon, errorOccurred: Boolean) {
        if (context.isValid()) {
            context.model.addElement(this, PresentationWrapperImpl(label, status, icon))
        }
        context.nextCallProvider.getNextCall().provide(context)
    }
}
