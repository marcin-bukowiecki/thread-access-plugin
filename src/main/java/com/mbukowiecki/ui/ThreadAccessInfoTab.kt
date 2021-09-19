/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.mbukowiecki.ui

import com.intellij.icons.AllIcons
import com.intellij.ui.JBSplitter
import com.intellij.util.ui.JBFont
import com.intellij.util.ui.JBUI
import com.mbukowiecki.bundle.ThreadAccessBundle.message
import java.awt.BorderLayout
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

/**
 * @author Marcin Bukowiecki
 */
class ThreadAccessInfoTab {

    val threadNameLabel: JLabel = JLabel(message("threadAccessInfo.thread.name"))
    val mainPanel = JBUI.Panels.simplePanel()
    val threadStatusModel = ThreadStatusModel()

    init {
        threadNameLabel.icon = AllIcons.Debugger.Threads
        threadNameLabel.font = JBFont.label()
        threadNameLabel.border = EmptyBorder(10, 10, 10, 10)

        val headerPanel: JPanel = JBUI.Panels.simplePanel()
            .addToLeft(threadNameLabel)

        val statusInfoPanel: JPanel = JBUI.Panels.simplePanel()
            .addToLeft(threadStatusModel.firstList)
            .addToCenter(threadStatusModel.secondList)

        val splitter = JBSplitter(true, 0.1f, 0.3f, 0.8f)
        splitter.splitterProportionKey = getDimensionServiceKey() + ".splitter"
        mainPanel.add(splitter, BorderLayout.CENTER)
        splitter.firstComponent = headerPanel
        splitter.secondComponent = statusInfoPanel
    }

    private fun getDimensionServiceKey(): String {
        return "#threadAccessInfo"
    }
}
