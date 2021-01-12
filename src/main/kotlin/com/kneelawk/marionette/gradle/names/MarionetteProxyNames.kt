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

    @get:Input
    var modCommonProxyPrefix = ""

    @get:Input
    var modCommonProxySuffix = "Proxy"

    @get:Input
    var modCommonProxyPackage = "com.kneelawk.marionette.gen.mod.proxy"

    @get:Input
    var apiCommonProxyPrefix = "RMI"

    @get:Input
    var apiCommonProxySuffix = "Proxy"

    @get:Input
    var apiCommonProxyPackage = "com.kneelawk.marionette.gen.api.proxy"

    @get:Input
    var modClientProxyPrefix = ""

    @get:Input
    var modClientProxySuffix = "Proxy"

    @get:Input
    var modClientProxyPackage = "com.kneelawk.marionette.gen.mod.client.proxy"

    @get:Input
    var apiClientProxyPrefix = "RMI"

    @get:Input
    var apiClientProxySuffix = "Proxy"

    @get:Input
    var apiClientProxyPackage = "com.kneelawk.marionette.gen.api.client.proxy"

    @get:Input
    var modServerProxyPrefix = ""

    @get:Input
    var modServerProxySuffix = "Proxy"

    @get:Input
    var modServerProxyPackage = "com.kneelawk.marionette.gen.mod.server.proxy"

    @get:Input
    var apiServerProxyPrefix = "RMI"

    @get:Input
    var apiServerProxySuffix = "Proxy"

    @get:Input
    var apiServerProxyPackage = "com.kneelawk.marionette.gen.api.server.proxy"
}