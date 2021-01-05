package com.kneelawk.marionettegradle.names

import org.gradle.api.tasks.Input

open class MarionetteInstanceNames {
    @get:Input
    var clientInstanceBuilderName = "MinecraftClientInstanceBuilder"

    @get:Input
    var clientInstanceBuilderPackage = "com.kneelawk.marionette.gen.instance"

    @get:Input
    var clientInstanceName = "MinecraftClientInstance"

    @get:Input
    var clientInstancePackage = "com.kneelawk.marionette.gen.instance"

    @get:Input
    var serverInstanceBuilderName = "MinecraftServerInstanceBuilder"

    @get:Input
    var serverInstanceBuilderPackage = "com.kneelawk.marionette.gen.instance"

    @get:Input
    var serverInstanceName = "MinecraftServerInstance"

    @get:Input
    var serverInstancePackage = "com.kneelawk.marionette.gen.instance"
}