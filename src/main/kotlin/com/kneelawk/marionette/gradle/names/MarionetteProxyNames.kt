package com.kneelawk.marionette.gradle.names

import org.gradle.api.tasks.Input

open class MarionetteProxyNames {
    @get:Input
    var modClientAccessName = "MinecraftClientAccess"
    @get:Input
    var modClientAccessPackage = "com.kneelawk.marionette.gen.mod.client"
    @get:Input
    var apiClientAccessName = "RMIMinecraftClientAccess"
    @get:Input
    var apiClientAccessPackage = "com.kneelawk.marionette.gen.api.client"
    @get:Input
    var modServerAccessName = "MinecraftServerAccess"
    @get:Input
    var modServerAccessPackage = "com.kneelawk.marionette.gen.mod.server"
    @get:Input
    var apiServerAccessName = "RMIMinecraftServerAccess"
    @get:Input
    var apiServerAccessPackage = "com.kneelawk.marionette.gen.api.server"
}