/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.mbukowiecki.providers

import com.intellij.icons.AllIcons
import com.mbukowiecki.bundle.ThreadAccessBundle
import com.mbukowiecki.evaluator.ThreadAccessEvaluator
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
                ThreadAccessBundle.message("threadAccessInfo.holdsReadLock.label")
            )
        )
    }
}

/**
 * @author Marcin Bukowiecki
 */
class HoldsReadLockCallback(context: SetupContext,
                            label: String) : CheckCallback(context, label, presentationColumn = 1) {

    override fun run(value: Value?, status: String, icon: Icon, errorOccurred: Boolean) {
        if (context.isValid()) {
            if (errorOccurred) {
                context.model.addElement(
                    this,
                    PresentationWrapperImpl(
                        label,
                        ThreadAccessBundle.message("threadAccessInfo.no"),
                        AllIcons.General.Error
                    ))
            } else {
                context.model.addElement(this, PresentationWrapperImpl(label, status, icon))
            }
        }
        context.nextCallProvider.getNextCall().provide(context)
    }
}
