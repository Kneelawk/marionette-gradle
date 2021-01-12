package com.kneelawk.marionette.gradle.proxy

import org.gradle.api.Named
import org.gradle.api.tasks.Input
import javax.inject.Inject

open class MarionetteProxyMethod @Inject constructor(private val name: String) : Named {
    @get:Input
    var returnType = "void"

    @get:Input
    var arguments = mutableListOf<String>()

    @get:Input
    var exceptions = mutableListOf<String>()

    @Input
    override fun getName(): String {
        return name
    }

    fun returnType(string: String) {
        returnType = string
    }

    fun argument(string: String) {
        arguments.add(string)
    }

    fun exception(string: String) {
        exceptions.add(string)
    }
}