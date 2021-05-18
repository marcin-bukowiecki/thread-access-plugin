/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.mbukowiecki.bundle

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.PropertyKey

const val BUNDLE = "messages.threadAccess"

/**
 * @author Marcin Bukowiecki
 */
object ThreadAccessBundle : DynamicBundle(BUNDLE) {

    fun message(@PropertyKey(resourceBundle = BUNDLE) key: String): @Nls String {
        return getMessage(key)
    }

    fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, arg1: Any): @Nls String {
        return getMessage(key, arg1)
    }

    fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, arg1: Any, arg2: Any): @Nls String {
        return getMessage(key, arg1, arg2)
    }
}
