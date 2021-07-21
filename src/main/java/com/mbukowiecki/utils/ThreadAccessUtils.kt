/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.mbukowiecki.utils

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiMethod
import com.intellij.xdebugger.XSourcePosition
import org.jetbrains.uast.util.classSetOf
import org.jetbrains.uast.util.isInstanceOf

/**
 * @author Marcin Bukowiecki
 */
object ThreadAccessUtils {

    fun getMethod(project: Project, sourcePosition: XSourcePosition): PsiMethod? {
        val element = getPlace(project, sourcePosition) ?: return null
        return findParent(element, PsiMethod::class.java)
    }

    private fun getPlace(project: Project, sourcePosition: XSourcePosition): PsiElement? {
        return ApplicationManager.getApplication().runReadAction<PsiElement> {
            val sourceFile = sourcePosition.file
            val psiFile = PsiManager.getInstance(project).findFile(sourceFile) ?: return@runReadAction null
            psiFile.findElementAt(sourcePosition.offset)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : PsiElement> findParent(element: PsiElement?, type: Class<T>): T? {
        if (element == null) return null

        return if (element.isInstanceOf(classSetOf(type))) {
            element as T
        } else {
            ReadAction.compute<T?, Throwable> { findParent(element.parent, type) }
        }
    }
}