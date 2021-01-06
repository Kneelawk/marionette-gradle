package com.kneelawk.marionette.gradle

import com.kneelawk.marionette.gradle.names.MarionetteNames
import com.kneelawk.marionette.gradle.signal.MarionetteSignal
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

    fun clientSignals(action: Action<MarionetteSignals>) {
        action.execute(clientSignals)
    }

    fun serverSignals(action: Action<MarionetteSignals>) {
        action.execute(serverSignals)
    }
}