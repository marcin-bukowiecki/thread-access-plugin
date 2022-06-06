/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.mbukowiecki.ui

import com.intellij.ui.components.JBList
import com.intellij.util.ui.JBFont
import com.mbukowiecki.providers.CheckCallback
import com.mbukowiecki.providers.PresentationWrapper
import javax.swing.DefaultListModel
import javax.swing.JList

/**
 * @author Marcin Bukowiecki
 */
class ThreadStatusModel {

    val firstList = JBList<PresentationWrapper>(DefaultListModel())
    val secondList = JBList<PresentationWrapper>(DefaultListModel())

    init {
        firstList.font = JBFont.label()
        firstList.cellRenderer = ThreadAccessCell()

        secondList.font = JBFont.label()
        secondList.cellRenderer = ThreadAccessCell()
    }

    fun clear() {
        (firstList.model as? DefaultListModel<PresentationWrapper>)?.clear()
        (secondList.model as? DefaultListModel<PresentationWrapper>)?.clear()
    }

    fun addElement(checkCallback: CheckCallback, presentationWrapper: PresentationWrapper) {
        val lists = getLists()
        val presentationColumn = checkCallback.presentationColumn
        if (presentationColumn >= lists.size) {
            (lists[0].model as? DefaultListModel)?.addElement(presentationWrapper)
        } else {
            (lists[presentationColumn].model as? DefaultListModel)?.addElement(presentationWrapper)
        }
    }

    private fun getLists(): List<JList<PresentationWrapper>> = listOf(firstList, secondList)
}
