package com.kneelawk.marionette.gradle.signal

import com.kneelawk.marionette.rt.mod.MarionetteSignalType
import com.kneelawk.marionette.rt.mod.template.MarionetteSignalTData
import org.gradle.api.Named
import java.io.Serializable
import javax.inject.Inject

open class MarionetteSignal @Inject constructor(private val name: String) : Named, Serializable {
    var type = "oneShot"

    override fun getName(): String {
        return name
    }

    fun type(string: String) {
        type = string
    }

    fun toTData(): MarionetteSignalTData {
        val typeEnum = when (type) {
            "oneShot" -> MarionetteSignalType.ONE_SHOT
            "reusable" -> MarionetteSignalType.REUSABLE
            "repeated" -> MarionetteSignalType.REPEATED
            else -> throw IllegalArgumentException("Unknown signal type: $type. Valid types are \"oneShot\", \"reusable\", and \"repeated\".")
        }

        return MarionetteSignalTData.builder().name(name).type(typeEnum).build()
    }
}