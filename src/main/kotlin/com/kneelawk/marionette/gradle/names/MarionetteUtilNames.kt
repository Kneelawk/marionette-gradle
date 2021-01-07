package com.kneelawk.marionette.gradle.names

import org.gradle.api.tasks.Input

open class MarionetteUtilNames {
    @get:Input
    var clientGlobalSignalsName = "ClientGlobalSignals"

    @get:Input
    var clientGlobalSignalsPackage = "com.kneelawk.marionette.gen.mod.client"

    @get:Input
    var serverGlobalSignalsName = "ServerGlobalSignals"

    @get:Input
    var serverGlobalSignalsPackage = "com.kneelawk.marionette.gen.mod.server"
}