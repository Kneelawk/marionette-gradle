package com.kneelawk.marionette.gradle.proxy

import org.gradle.api.Named
import org.gradle.api.tasks.Input

data class MarionetteProxyProperty(private val name: String, @get:Input val type: String) : Named {
    @Input
    override fun getName() = name
}