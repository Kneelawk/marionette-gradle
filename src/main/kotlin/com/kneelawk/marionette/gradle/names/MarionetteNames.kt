package com.kneelawk.marionette.gradle.names

import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.tasks.Nested

open class MarionetteNames {
    @get:Nested
    var instance = MarionetteInstanceNames()

    @get:Nested
    var entryPoint = MarionetteEntryPointNames()

    @get:Nested
    var proxy = MarionetteProxyNames()

    @get:Nested
    var utils = MarionetteUtilNames()

    fun instance(action: Action<MarionetteInstanceNames>) {
        action.execute(instance)
    }

    fun instance(closure: Closure<Unit>) {
        closure.delegate = instance
        closure.call(instance)
    }

    fun entryPoint(action: Action<MarionetteEntryPointNames>) {
        action.execute(entryPoint)
    }

    fun entryPoint(closure: Closure<Unit>) {
        closure.delegate = entryPoint
        closure.call(entryPoint)
    }

    fun proxy(action: Action<MarionetteProxyNames>) {
        action.execute(proxy)
    }

    fun proxy(closure: Closure<Unit>) {
        closure.delegate = proxy
        closure.call(proxy)
    }

    fun utils(action: Action<MarionetteUtilNames>) {
        action.execute(utils)
    }

    fun utils(closure: Closure<Unit>) {
        closure.delegate = utils
        closure.call(utils)
    }
}