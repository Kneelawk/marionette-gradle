package com.kneelawk.marionettegradle.names

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

    fun instance(action: Action<MarionetteInstanceNames>) {
        action.execute(instance)
    }

    fun instance(closure: Closure<Unit>) {
        closure.delegate = instance
        closure.call()
    }

    fun entryPoint(action: Action<MarionetteEntryPointNames>) {
        action.execute(entryPoint)
    }

    fun entryPoint(closure: Closure<Unit>) {
        closure.delegate = entryPoint
        closure.call()
    }

    fun proxy(action: Action<MarionetteProxyNames>) {
        action.execute(proxy)
    }

    fun proxy(closure: Closure<Unit>) {
        closure.delegate = proxy
        closure.call()
    }
}