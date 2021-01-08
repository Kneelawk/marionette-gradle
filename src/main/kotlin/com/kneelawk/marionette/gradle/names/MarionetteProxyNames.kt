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

    @get:Input
    var apiCommonQueueCallbackPrefix = "RMI"

    @get:Input
    var apiCommonQueueCallbackSuffix = "Callback"

    @get:Input
    var apiCommonQueueCallbackPackage = "com.kneelawk.marionette.gen.api.callback.queue"

    @get:Input
    var testCommonQueueCallbackPrefix = ""

    @get:Input
    var testCommonQueueCallbackSuffix = "Callback"

    @get:Input
    var testCommonQueueCallbackPackage = "com.kneelawk.marionette.gen.callback.queue"

    @get:Input
    var apiClientQueueCallbackPrefix = "RMI"

    @get:Input
    var apiClientQueueCallbackSuffix = "Callback"

    @get:Input
    var apiClientQueueCallbackPackage = "com.kneelawk.marionette.gen.api.client.callback.queue"

    @get:Input
    var testClientQueueCallbackPrefix = ""

    @get:Input
    var testClientQueueCallbackSuffix = "Callback"

    @get:Input
    var testClientQueueCallbackPackage = "com.kneelawk.marionette.gen.client.callback.queue"

    @get:Input
    var apiServerQueueCallbackPrefix = "RMI"

    @get:Input
    var apiServerQueueCallbackSuffix = "Callback"

    @get:Input
    var apiServerQueueCallbackPackage = "com.kneelawk.marionette.gen.api.callback.queue"

    @get:Input
    var testServerQueueCallbackPrefix = ""

    @get:Input
    var testServerQueueCallbackSuffix = "Callback"

    @get:Input
    var testServerQueueCallbackPackage = "com.kneelawk.marionette.gen.callback.queue"
}