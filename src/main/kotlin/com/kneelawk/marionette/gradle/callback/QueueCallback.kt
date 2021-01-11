package com.kneelawk.marionette.gradle.callback

import org.gradle.api.Named
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import java.io.Serializable
import javax.inject.Inject

open class QueueCallback @Inject constructor(private val name: String, private val objectFactory: ObjectFactory) :
    Named, Serializable {
    @get:Input
    var returnType = "void"

    @get:Input
    var arguments = mutableListOf<String>()

    @get:Input
    var exceptions = mutableListOf<String>()

    @get:Input
    @get:Optional
    var packageName: String? = null

    @get:Input
    @get:Optional
    var className: String? = null

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

    fun packageName(string: String?) {
        packageName = string
    }

    fun className(string: String?) {
        className = string
    }
}