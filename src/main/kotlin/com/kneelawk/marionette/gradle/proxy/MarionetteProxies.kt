package com.kneelawk.marionette.gradle.proxy

import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Nested
import javax.inject.Inject

open class MarionetteProxies @Inject constructor(private val objectFactory: ObjectFactory) {
    @get:Nested
    var proxies = mutableListOf<MarionetteProxy>()

    fun proxy(className: String) {
        val proxy = objectFactory.newInstance(MarionetteProxy::class.java)
        proxy.proxiedClass = className
        proxies.add(proxy)
    }

    fun proxy(className: String, action: Action<MarionetteProxy>) {
        val proxy = objectFactory.newInstance(MarionetteProxy::class.java)
        proxy.proxiedClass = className
        action.execute(proxy)
        proxies.add(proxy)
    }

    fun proxy(className: String, closure: Closure<Any>) {
        val proxy = objectFactory.newInstance(MarionetteProxy::class.java)
        proxy.proxiedClass = className
        closure.delegate = proxy
        closure.call(proxy)
        proxies.add(proxy)
    }
}