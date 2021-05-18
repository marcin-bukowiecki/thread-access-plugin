/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.mbukowiecki.ui

import com.mbukowiecki.providers.PresentationWrapper
import java.awt.Component
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.ListCellRenderer
import javax.swing.border.EmptyBorder

/**
 * @author Marcin Bukowiecki
 */
class ThreadAccessCell : JLabel(), ListCellRenderer<PresentationWrapper> {

    override fun getListCellRendererComponent(
        list: JList<out PresentationWrapper>?,
        value: PresentationWrapper?,
        index: Int,
        isSelected: Boolean,
        cellHasFocus: Boolean
    ): Component {
        text = value?.getPresentationText()
        border = EmptyBorder(5, 10, 5, 10)
        icon = value?.icon
        return this
    }
}