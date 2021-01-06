package com.kneelawk.marionette.gradle.signal

import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Input
import java.io.Serializable
import javax.inject.Inject

open class MarionetteSignals @Inject constructor(objectFactory: ObjectFactory) : Serializable {
    @get:Input
    val signals: NamedDomainObjectContainer<MarionetteSignal> =
        objectFactory.domainObjectContainer(MarionetteSignal::class.java) { name ->
            objectFactory.newInstance(
                MarionetteSignal::class.java,
                name
            ).apply { type = "oneShot" }
        }

    fun oneShot(name: String) {
        signals.create(name) {
            it.type = "oneShot"
        }
    }

    fun oneShot(name: String, action: Action<MarionetteSignal>) {
        signals.create(name) {
            it.type = "oneShot"
            action.execute(it)
        }
    }

    fun oneShot(name: String, closure: Closure<Unit>) {
        signals.create(name) {
            it.type = "oneShot"
            closure.delegate = it
            closure.call(it)
        }
    }

    fun reusable(name: String) {
        signals.create(name) {
            it.type = "reusable"
        }
    }

    fun reusable(name: String, action: Action<MarionetteSignal>) {
        signals.create(name) {
            it.type = "reusable"
            action.execute(it)
        }
    }

    fun reusable(name: String, closure: Closure<Unit>) {
        signals.create(name) {
            it.type = "reusable"
            closure.delegate = it
            closure.call(it)
        }
    }

    fun repeated(name: String) {
        signals.create(name) {
            it.type = "repeated"
        }
    }

    fun repeated(name: String, action: Action<MarionetteSignal>) {
        signals.create(name) {
            it.type = "repeated"
            action.execute(it)
        }
    }

    fun repeated(name: String, closure: Closure<Unit>) {
        signals.create(name) {
            it.type = "repeated"
            closure.delegate = it
            closure.call(it)
        }
    }

    fun signals(action: Action<NamedDomainObjectContainer<MarionetteSignal>>) {
        action.execute(signals)
    }
}