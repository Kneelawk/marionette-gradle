package com.kneelawk.marionette.gradle

import com.kneelawk.marionette.gradle.callback.QueueCallback
import com.kneelawk.marionette.gradle.names.MarionetteNames
import com.kneelawk.marionette.gradle.proxy.MarionetteProxies
import com.kneelawk.marionette.gradle.signal.MarionetteSignals
import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.model.ObjectFactory
import java.io.Serializable
import javax.inject.Inject

open class MarionetteTestSet @Inject constructor(private val name: String, private val objectFactory: ObjectFactory) :
    Named, Serializable {
    var useJUnitPlatform = false
    var showStandardStreams = true
    var names = MarionetteNames()
    val commonSignals: MarionetteSignals = objectFactory.newInstance(MarionetteSignals::class.java)
    val clientSignals: MarionetteSignals = objectFactory.newInstance(MarionetteSignals::class.java)
    val serverSignals: MarionetteSignals = objectFactory.newInstance(MarionetteSignals::class.java)
    val commonQueues: NamedDomainObjectContainer<QueueCallback> =
        objectFactory.domainObjectContainer(QueueCallback::class.java)
    val clientQueues: NamedDomainObjectContainer<QueueCallback> =
        objectFactory.domainObjectContainer(QueueCallback::class.java)
    val serverQueues: NamedDomainObjectContainer<QueueCallback> =
        objectFactory.domainObjectContainer(QueueCallback::class.java)
    var commonProxies: MarionetteProxies = objectFactory.newInstance(MarionetteProxies::class.java)
    var clientProxies: MarionetteProxies = objectFactory.newInstance(MarionetteProxies::class.java)
    var serverProxies: MarionetteProxies = objectFactory.newInstance(MarionetteProxies::class.java)

    override fun getName(): String {
        return name
    }

    fun useJUnitPlatform() {
        useJUnitPlatform = true
    }

    fun showStandardStreams(showStandardStreams: Boolean) {
        this.showStandardStreams = showStandardStreams
    }

    fun names(action: Action<MarionetteNames>) {
        action.execute(names)
    }

    fun names(closure: Closure<Unit>) {
        closure.delegate = names
        closure.call()
    }

    fun commonSignals(action: Action<MarionetteSignals>) {
        action.execute(commonSignals)
    }

    fun commonSignals(closure: Closure<Any>) {
        closure.delegate = commonSignals
        closure.call(commonSignals)
    }

    fun clientSignals(action: Action<MarionetteSignals>) {
        action.execute(clientSignals)
    }

    fun clientSignals(closure: Closure<Any>) {
        closure.delegate = clientSignals
        closure.call(clientSignals)
    }

    fun serverSignals(action: Action<MarionetteSignals>) {
        action.execute(serverSignals)
    }

    fun serverSignals(closure: Closure<Any>) {
        closure.delegate = serverSignals
        closure.call(serverSignals)
    }

    fun commonQueues(action: Action<NamedDomainObjectContainer<QueueCallback>>) {
        action.execute(commonQueues)
    }

    fun clientQueues(action: Action<NamedDomainObjectContainer<QueueCallback>>) {
        action.execute(clientQueues)
    }

    fun serverQueues(action: Action<NamedDomainObjectContainer<QueueCallback>>) {
        action.execute(serverQueues)
    }

    fun commonProxies(action: Action<MarionetteProxies>) {
        action.execute(commonProxies)
    }

    fun commonProxies(closure: Closure<Any>) {
        closure.delegate = commonProxies
        closure.call(commonProxies)
    }

    fun clientProxies(action: Action<MarionetteProxies>) {
        action.execute(clientProxies)
    }

    fun clientProxies(closure: Closure<Any>) {
        closure.delegate = clientProxies
        closure.call(clientProxies)
    }

    fun serverProxies(action: Action<MarionetteProxies>) {
        action.execute(serverProxies)
    }

    fun serverProxies(closure: Closure<Any>) {
        closure.delegate = serverProxies
        closure.call(serverProxies)
    }
}