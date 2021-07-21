/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.mbukowiecki.providers

/**
 * @author Marcin Bukowiecki
 */
class NextCallProvider(accessProviders: List<AccessProvider>) {

    private val iterator = accessProviders.iterator()

    fun getNextCall(): AccessProvider {
        return if (!iterator.hasNext()) {
            SinkAccessProvider()
        } else {
            iterator.next()
        }
    }
}