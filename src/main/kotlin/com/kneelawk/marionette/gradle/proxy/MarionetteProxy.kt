package com.kneelawk.marionette.gradle.proxy

import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import javax.inject.Inject

open class MarionetteProxy @Inject constructor(private val objectFactory: ObjectFactory) {
    @get:Input
    var proxiedClass = ""

    @get:Input
    @get:Optional
    var interfaceClassName: String? = null

    @get:Input
    @get:Optional
    var interfacePackageName: String? = null

    @get:Input
    @get:Optional
    var implementationClassName: String? = null

    @get:Input
    @get:Optional
    var implementationPackageName: String? = null

    @get:Nested
    var methods = mutableListOf<MarionetteProxyMethod>()

    @get:Nested
    var constructors = mutableListOf<MarionetteProxyConstructor>()

    fun method(methodName: String) {
        methods.add(objectFactory.newInstance(MarionetteProxyMethod::class.java, methodName))
    }

    fun method(methodName: String, action: Action<MarionetteProxyMethod>) {
        val method = objectFactory.newInstance(MarionetteProxyMethod::class.java, methodName)
        action.execute(method)
        methods.add(method)
    }

    fun method(methodName: String, closure: Closure<Any>) {
        val method = objectFactory.newInstance(MarionetteProxyMethod::class.java, methodName)
        closure.delegate = method
        closure.call(method)
        methods.add(method)
    }

    fun constructor() {
        constructors.add(objectFactory.newInstance(MarionetteProxyConstructor::class.java))
    }

    fun constructor(action: Action<MarionetteProxyConstructor>) {
        val constructor = objectFactory.newInstance(MarionetteProxyConstructor::class.java)
        action.execute(constructor)
        constructors.add(constructor)
    }

    fun constructor(closure: Closure<Any>) {
        val constructor = objectFactory.newInstance(MarionetteProxyConstructor::class.java)
        closure.delegate = constructor
        closure.call(constructor)
        constructors.add(constructor)
    }
}