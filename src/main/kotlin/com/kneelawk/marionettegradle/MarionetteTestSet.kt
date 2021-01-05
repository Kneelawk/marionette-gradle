package com.kneelawk.marionettegradle

import com.kneelawk.marionettegradle.names.MarionetteNames
import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.Named
import java.io.Serializable
import javax.inject.Inject

open class MarionetteTestSet @Inject constructor(private val name: String) : Named, Serializable {
    var useJUnitPlatform = false
    var showStandardStreams = true
    var names = MarionetteNames()

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
}