package com.kneelawk.marionette.gradle.proxy

import org.gradle.api.tasks.Input

open class MarionetteProxyConstructor {
    @get:Input
    var arguments = mutableListOf<String>()

    @get:Input
    var exceptions = mutableListOf<String>()

    fun argument(string: String) {
        arguments.add(string)
    }

    fun exception(string: String) {
        exceptions.add(string)
    }
}