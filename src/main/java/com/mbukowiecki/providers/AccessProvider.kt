/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.mbukowiecki.providers

/**
 * @author Marcin Bukowiecki
 */
interface AccessProvider {

    fun provide(context: SetupContext)
}