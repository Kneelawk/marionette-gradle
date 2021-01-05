package com.kneelawk.marionettegradle.names

import org.gradle.api.tasks.Input

open class MarionetteEntryPointNames {
    @get:Input
    var preLaunchEntryPointName = "MarionetteModPreLaunch"

    @get:Input
    var preLaunchEntryPointPackage = "com.kneelawk.marionette.gen.mod"

    @get:Input
    var preLaunchClientName = "MarionetteClientPreLaunch"

    @get:Input
    var preLaunchClientPackage = "com.kneelawk.marionette.gen.mod.client"

    @get:Input
    var preLaunchServerName = "MarionetteServerPreLaunch"

    @get:Input
    var preLaunchServerPackage = "com.kneelawk.marionette.gen.mod.server"
}