/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.mbukowiecki.providers

import com.intellij.icons.AllIcons
import com.mbukowiecki.bundle.ThreadAccessBundle
import com.mbukowiecki.evaluator.ThreadAccessEvaluator
import com.sun.jdi.BooleanValue
import com.sun.jdi.Value
import javax.swing.Icon

/**
 * @author Marcin Bukowiecki
 */
class HoldsReadLockProvider : AccessProvider {

    override fun provide(context: SetupContext) {
        ThreadAccessEvaluator.getInstance().getStatus(
            context,
            "ApplicationManager.getApplication().holdsReadLock()",
            "com.intellij.openapi.application.ApplicationManager",
            HoldsReadLockCallback(
                context,
                ThreadAccessBundle.message("holdsReadLock.label")
            )
        )
    }
}

/**
 * @author Marcin Bukowiecki
 */
class HoldsReadLockCallback(context: SetupContext,
                            label: String) : CheckCallback(context, label) {

    override fun run(value: Value?, status: String, icon: Icon, errorOccurred: Boolean) {
        if (context.isValid()) {
            if (errorOccurred) {
                context.model.addElement(
                    PresentationWrapperImpl(
                        label,
                        ThreadAccessBundle.message("no"),
                        AllIcons.General.Error
                    ))
            } else {
                context.model.addElement(PresentationWrapperImpl(label, status, icon))
            }
        }
        context.nextCallProvider.getNextCall().provide(context)
    }
}