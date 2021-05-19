/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.mbukowiecki.ui

import com.intellij.icons.AllIcons
import com.intellij.util.ui.JBFont
import com.mbukowiecki.providers.PresentationWrapper
import javax.swing.DefaultListModel
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

/**
 * @author Marcin Bukowiecki
 */
class ThreadAccessTabForm {

    lateinit var mainPanel: JPanel
    lateinit var threadAccessList: JList<PresentationWrapper>
    lateinit var threadNameLabel: JLabel

    init {
        threadAccessList.font = JBFont.label()
        threadAccessList.model = DefaultListModel()
        threadAccessList.cellRenderer = ThreadAccessCell()

        threadNameLabel.icon = AllIcons.Debugger.Threads
        threadNameLabel.font = JBFont.label()
        threadNameLabel.border = EmptyBorder(10, 10, 10, 10)
    }
}