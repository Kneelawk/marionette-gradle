package com.kneelawk.marionette.gradle

import com.google.common.base.CaseFormat
import com.kneelawk.marionette.gradle.callback.QueueCallback
import com.kneelawk.marionette.gradle.names.MarionetteNames
import com.kneelawk.marionette.gradle.signal.MarionetteSignals
import com.kneelawk.marionette.rt.callback.template.CallbackTData
import com.kneelawk.marionette.rt.instance.template.MinecraftClientInstanceBuilderTData
import com.kneelawk.marionette.rt.instance.template.MinecraftClientInstanceTData
import com.kneelawk.marionette.rt.instance.template.MinecraftServerInstanceBuilderTData
import com.kneelawk.marionette.rt.instance.template.MinecraftServerInstanceTData
import com.kneelawk.marionette.rt.mod.client.template.ClientGlobalSignalsTData
import com.kneelawk.marionette.rt.mod.client.template.MarionetteClientPreLaunchTData
import com.kneelawk.marionette.rt.mod.client.template.MinecraftClientAccessTData
import com.kneelawk.marionette.rt.mod.server.template.MarionetteServerPreLaunchTData
import com.kneelawk.marionette.rt.mod.server.template.MinecraftServerAccessTData
import com.kneelawk.marionette.rt.mod.server.template.ServerGlobalSignalsTData
import com.kneelawk.marionette.rt.mod.template.MarionetteModPreLaunchTData
import com.kneelawk.marionette.rt.rmi.template.RMIMinecraftClientAccessTData
import com.kneelawk.marionette.rt.rmi.template.RMIMinecraftServerAccessTData
import com.kneelawk.marionette.rt.template.FabricModJsonTData
import com.kneelawk.marionette.rt.template.MarionetteTemplates
import com.kneelawk.marionette.rt.template.TemplateUtils
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import org.gradle.api.DefaultTask
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.FileWriter
import java.io.InputStream
import java.io.InputStreamReader
import javax.inject.Inject

open class MarionetteSourceGenerator @Inject constructor(private val objectFactory: ObjectFactory) : DefaultTask() {
    @get:OutputDirectory
    var apiJavaOutput = File("apiJava")

    @get:OutputDirectory
    var apiResourcesOutput = File("apiResources")

    @get:OutputDirectory
    var modJavaOutput = File("modJava")

    @get:OutputDirectory
    var modResourcesOutput = File("modResources")

    @get:OutputDirectory
    var testJavaOutput = File("testJava")

    @get:OutputDirectory
    var testResourcesOutput = File("testResources")

    @get:Input
    var testSetName = "marionette"

    @get:Nested
    var names = MarionetteNames()

    @get:Nested
    var commonSignals: MarionetteSignals = objectFactory.newInstance(MarionetteSignals::class.java)

    @get:Nested
    var clientSignals: MarionetteSignals = objectFactory.newInstance(MarionetteSignals::class.java)

    @get:Nested
    var serverSignals: MarionetteSignals = objectFactory.newInstance(MarionetteSignals::class.java)

    @get:Nested
    var commonQueues: NamedDomainObjectContainer<QueueCallback> =
        objectFactory.domainObjectContainer(QueueCallback::class.java)

    @get:Nested
    var clientQueues: NamedDomainObjectContainer<QueueCallback> =
        objectFactory.domainObjectContainer(QueueCallback::class.java)

    @get:Nested
    var serverQueues: NamedDomainObjectContainer<QueueCallback> =
        objectFactory.domainObjectContainer(QueueCallback::class.java)

    @TaskAction
    fun run() {
        val engine = VelocityEngine()
        engine.init()

        val modId = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_HYPHEN, testSetName)
        val modName = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, testSetName)

        generate(
            engine,
            File(modResourcesOutput, "fabric.mod.json"),
            MarionetteTemplates.getFabricModJsonTemplate(),
            FabricModJsonTData.builder().id(modId).name(modName)
                .preLaunchEntryPoint("${names.entryPoint.preLaunchEntryPointPackage}.${names.entryPoint.preLaunchEntryPointName}")
                .build()
        )

        generateRMIClientAccess(engine)
        generateRMIServerAccess(engine)

        generateClientInstance(engine)
        generateServerInstance(engine)
        generateClientInstanceBuilder(engine)
        generateServerInstanceBuilder(engine)

        generateModPreLaunch(engine)
        generateClientPreLaunch(engine)
        generateServerPreLaunch(engine)
        generateClientAccess(engine)
        generateServerAccess(engine)

        generateClientGlobalSignals(engine)
        generateServerGlobalSignals(engine)

        generateQueueCallbacks(
            engine,
            commonQueues,
            names.proxy.apiCommonQueueCallbackPackage,
            names.proxy.apiCommonQueueCallbackPrefix,
            names.proxy.apiCommonQueueCallbackSuffix,
            true
        )
        generateQueueCallbacks(
            engine,
            clientQueues,
            names.proxy.apiClientQueueCallbackPackage,
            names.proxy.apiClientQueueCallbackPrefix,
            names.proxy.apiClientQueueCallbackSuffix,
            true
        )
        generateQueueCallbacks(
            engine,
            serverQueues,
            names.proxy.apiServerQueueCallbackPackage,
            names.proxy.apiServerQueueCallbackPrefix,
            names.proxy.apiServerQueueCallbackSuffix,
            true
        )
        generateQueueCallbacks(
            engine,
            commonQueues,
            names.proxy.testCommonQueueCallbackPackage,
            names.proxy.testCommonQueueCallbackPrefix,
            names.proxy.testCommonQueueCallbackSuffix,
            false
        )
        generateQueueCallbacks(
            engine,
            clientQueues,
            names.proxy.testClientQueueCallbackPackage,
            names.proxy.testClientQueueCallbackPrefix,
            names.proxy.testClientQueueCallbackSuffix,
            false
        )
        generateQueueCallbacks(
            engine,
            serverQueues,
            names.proxy.testServerQueueCallbackPackage,
            names.proxy.testServerQueueCallbackPrefix,
            names.proxy.testServerQueueCallbackSuffix,
            false
        )
    }

    private fun generate(engine: VelocityEngine, output: File, input: InputStream, data: Any) {
        if (!output.parentFile.exists()) {
            output.parentFile.mkdirs()
        }

        val context = VelocityContext()
        context.put("data", data)
        context.put("utils", TemplateUtils.getInstance())
        FileWriter(output).use { writer ->
            InputStreamReader(input).use { reader ->
                engine.evaluate(context, writer, "Generate ${output.name}", reader)
            }
        }
    }

    private fun generateRMIClientAccess(engine: VelocityEngine) {
        val imports = ImportResolver()

        val packageName = names.proxy.apiClientAccessPackage
        val className = names.proxy.apiClientAccessName

        generate(
            engine,
            fromPackage(apiJavaOutput, packageName, className),
            MarionetteTemplates.getRMIClientAccessTemplate(),
            RMIMinecraftClientAccessTData.builder()
                .packageName(packageName)
                .className(className)
                .importNames(imports.getImports(packageName))
                .signalNames(commonSignals.signals.names)
                .signalNames(clientSignals.signals.names)
                .build()
        )
    }

    private fun generateRMIServerAccess(engine: VelocityEngine) {
        val imports = ImportResolver()

        val packageName = names.proxy.apiServerAccessPackage
        val className = names.proxy.apiServerAccessName

        generate(
            engine,
            fromPackage(apiJavaOutput, packageName, className),
            MarionetteTemplates.getRMIServerAccessTemplate(),
            RMIMinecraftServerAccessTData.builder()
                .packageName(packageName)
                .className(className)
                .importNames(imports.getImports(packageName))
                .signalNames(commonSignals.signals.names)
                .signalNames(serverSignals.signals.names)
                .build()
        )
    }

    private fun generateClientInstance(engine: VelocityEngine) {
        val imports = ImportResolver()
        val rmiClass = imports.add(names.proxy.apiClientAccessPackage, names.proxy.apiClientAccessName)

        val packageName = names.instance.clientInstancePackage
        val className = names.instance.clientInstanceName

        generate(
            engine,
            fromPackage(testJavaOutput, packageName, className),
            MarionetteTemplates.getClientInstanceTemplate(),
            MinecraftClientInstanceTData.builder()
                .packageName(packageName)
                .className(className)
                .importNames(imports.getImports(packageName))
                .rmiClass(imports[rmiClass])
                .signalNames(commonSignals.signals.names)
                .signalNames(clientSignals.signals.names)
                .build()
        )
    }

    private fun generateServerInstance(engine: VelocityEngine) {
        val imports = ImportResolver()
        val rmiClass = imports.add(names.proxy.apiServerAccessPackage, names.proxy.apiServerAccessName)

        val packageName = names.instance.serverInstancePackage
        val className = names.instance.serverInstanceName

        generate(
            engine,
            fromPackage(testJavaOutput, packageName, className),
            MarionetteTemplates.getServerInstanceTemplate(),
            MinecraftServerInstanceTData.builder()
                .packageName(packageName)
                .className(className)
                .importNames(imports.getImports(packageName))
                .rmiClass(imports[rmiClass])
                .signalNames(commonSignals.signals.names)
                .signalNames(serverSignals.signals.names)
                .build()
        )
    }

    private fun generateClientInstanceBuilder(engine: VelocityEngine) {
        val imports = ImportResolver()
        val rmiClass = imports.add(names.proxy.apiClientAccessPackage, names.proxy.apiClientAccessName)
        val instanceClass = imports.add(names.instance.clientInstancePackage, names.instance.clientInstanceName)

        val packageName = names.instance.clientInstanceBuilderPackage
        val className = names.instance.clientInstanceBuilderName

        generate(
            engine,
            fromPackage(testJavaOutput, packageName, className),
            MarionetteTemplates.getClientInstanceBuilderTemplate(),
            MinecraftClientInstanceBuilderTData.builder()
                .packageName(packageName)
                .className(className)
                .importNames(imports.getImports(packageName))
                .rmiClass(imports[rmiClass])
                .instanceClass(imports[instanceClass])
                .build()
        )
    }

    private fun generateServerInstanceBuilder(engine: VelocityEngine) {
        val imports = ImportResolver()
        val rmiClass = imports.add(names.proxy.apiServerAccessPackage, names.proxy.apiServerAccessName)
        val instanceClass = imports.add(names.instance.serverInstancePackage, names.instance.serverInstanceName)

        val packageName = names.instance.serverInstanceBuilderPackage
        val className = names.instance.serverInstanceBuilderName

        generate(
            engine,
            fromPackage(testJavaOutput, packageName, className),
            MarionetteTemplates.getServerInstanceBuilderTemplate(),
            MinecraftServerInstanceBuilderTData.builder()
                .packageName(packageName)
                .className(className)
                .importNames(imports.getImports(packageName))
                .rmiClass(imports[rmiClass])
                .instanceClass(imports[instanceClass])
                .build()
        )
    }

    private fun generateModPreLaunch(engine: VelocityEngine) {
        val imports = ImportResolver()
        val clientPreLaunch = imports.add(names.entryPoint.preLaunchClientPackage, names.entryPoint.preLaunchClientName)
        val serverPreLaunch = imports.add(names.entryPoint.preLaunchServerPackage, names.entryPoint.preLaunchServerName)

        val packageName = names.entryPoint.preLaunchEntryPointPackage
        val className = names.entryPoint.preLaunchEntryPointName

        generate(
            engine,
            fromPackage(modJavaOutput, packageName, className),
            MarionetteTemplates.getModPreLaunchTemplate(),
            MarionetteModPreLaunchTData.builder()
                .packageName(packageName)
                .className(className)
                .importNames(imports.getImports(packageName))
                .clientPreLaunch(imports[clientPreLaunch])
                .serverPreLaunch(imports[serverPreLaunch])
                .build()
        )
    }

    private fun generateClientPreLaunch(engine: VelocityEngine) {
        val imports = ImportResolver()
        val rmiClass = imports.add(names.proxy.apiClientAccessPackage, names.proxy.apiClientAccessName)
        val accessClass = imports.add(names.proxy.modClientAccessPackage, names.proxy.modClientAccessName)

        val packageName = names.entryPoint.preLaunchClientPackage
        val className = names.entryPoint.preLaunchClientName

        generate(
            engine,
            fromPackage(modJavaOutput, packageName, className),
            MarionetteTemplates.getClientPreLaunchTemplate(),
            MarionetteClientPreLaunchTData.builder()
                .packageName(packageName)
                .className(className)
                .importNames(imports.getImports(packageName))
                .rmiClass(imports[rmiClass])
                .accessClass(imports[accessClass])
                .build()
        )
    }

    private fun generateServerPreLaunch(engine: VelocityEngine) {
        val imports = ImportResolver()
        val rmiClass = imports.add(names.proxy.apiServerAccessPackage, names.proxy.apiServerAccessName)
        val accessClass = imports.add(names.proxy.modServerAccessPackage, names.proxy.modServerAccessName)

        val packageName = names.entryPoint.preLaunchServerPackage
        val className = names.entryPoint.preLaunchServerName

        generate(
            engine,
            fromPackage(modJavaOutput, packageName, className),
            MarionetteTemplates.getServerPreLaunchTemplate(),
            MarionetteServerPreLaunchTData.builder()
                .packageName(packageName)
                .className(className)
                .importNames(imports.getImports(packageName))
                .rmiClass(imports[rmiClass])
                .accessClass(imports[accessClass])
                .build()
        )
    }

    private fun generateClientAccess(engine: VelocityEngine) {
        val imports = ImportResolver()
        val rmiClass = imports.add(names.proxy.apiClientAccessPackage, names.proxy.apiClientAccessName)
        val signalClass = imports.add(names.utils.clientGlobalSignalsPackage, names.utils.clientGlobalSignalsName)

        val packageName = names.proxy.modClientAccessPackage
        val className = names.proxy.modClientAccessName

        generate(
            engine,
            fromPackage(modJavaOutput, packageName, className),
            MarionetteTemplates.getClientAccessTemplate(),
            MinecraftClientAccessTData.builder()
                .packageName(packageName)
                .className(className)
                .importNames(imports.getImports(packageName))
                .rmiClass(imports[rmiClass])
                .signalClass(imports[signalClass])
                .signalNames(commonSignals.signals.names)
                .signalNames(clientSignals.signals.names)
                .build()
        )
    }

    private fun generateServerAccess(engine: VelocityEngine) {
        val imports = ImportResolver()
        val rmiClass = imports.add(names.proxy.apiServerAccessPackage, names.proxy.apiServerAccessName)
        val signalClass = imports.add(names.utils.serverGlobalSignalsPackage, names.utils.serverGlobalSignalsName)

        val packageName = names.proxy.modServerAccessPackage
        val className = names.proxy.modServerAccessName

        generate(
            engine,
            fromPackage(modJavaOutput, packageName, className),
            MarionetteTemplates.getServerAccessTemplate(),
            MinecraftServerAccessTData.builder()
                .packageName(packageName)
                .className(className)
                .importNames(imports.getImports(packageName))
                .rmiClass(imports[rmiClass])
                .signalClass(imports[signalClass])
                .signalNames(commonSignals.signals.names)
                .signalNames(serverSignals.signals.names)
                .build()
        )
    }

    private fun generateClientGlobalSignals(engine: VelocityEngine) {
        val packageName = names.utils.clientGlobalSignalsPackage
        val className = names.utils.clientGlobalSignalsName

        generate(
            engine,
            fromPackage(modJavaOutput, packageName, className),
            MarionetteTemplates.getClientGlobalSignalsTemplate(),
            ClientGlobalSignalsTData.builder()
                .packageName(packageName)
                .className(className)
                .signals(commonSignals.signals.map { it.toTData() })
                .signals(clientSignals.signals.map { it.toTData() })
                .build()
        )
    }

    private fun generateServerGlobalSignals(engine: VelocityEngine) {
        val packageName = names.utils.serverGlobalSignalsPackage
        val className = names.utils.serverGlobalSignalsName

        generate(
            engine,
            fromPackage(modJavaOutput, packageName, className),
            MarionetteTemplates.getServerGlobalSignalsTemplate(),
            ServerGlobalSignalsTData.builder()
                .packageName(packageName)
                .className(className)
                .signals(commonSignals.signals.map { it.toTData() })
                .signals(serverSignals.signals.map { it.toTData() })
                .build()
        )
    }

    private fun generateQueueCallbacks(
        engine: VelocityEngine,
        callbacks: NamedDomainObjectContainer<QueueCallback>,
        packageName: String,
        prefix: String,
        suffix: String,
        rmi: Boolean
    ) {
        for (callback in callbacks) {
            generateQueueCallback(engine, callback, packageName, prefix, suffix, rmi)
        }
    }

    private fun generateQueueCallback(
        engine: VelocityEngine,
        callback: QueueCallback,
        packageName: String,
        prefix: String,
        suffix: String,
        rmi: Boolean
    ) {
        val remoteExceptionType = TypeName("java.rmi", "RemoteException")
        val imports = ImportResolver()
        val returnType = if (!rmi) imports.add(TypeName.fromString(callback.returnType)) else null
        val argumentTypes = callback.arguments.map { imports.add(TypeName.fromString(it)) }
        val exceptionTypes = callback.exceptions.map { imports.add(TypeName.fromString(it)) }
        val remoteException = if (rmi) imports.add(remoteExceptionType) else null

        val packageName1 = callback.packageName ?: packageName
        val className =
            callback.className ?: prefix + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, callback.name) + suffix

        val tData = CallbackTData.builder()
            .packageName(packageName1)
            .className(className)
            .importNames(imports.getImports(packageName1))
            .remote(rmi)
            .parameterTypes(argumentTypes.map { imports[it] })
            .exceptionTypes(exceptionTypes.map { imports[it] })

        if (remoteException != null && !exceptionTypes.contains(remoteException)) tData.exceptionType(imports[remoteException])

        tData.returnType(returnType?.let { imports[it] } ?: "void")

        generate(
            engine,
            fromPackage(if (rmi) apiJavaOutput else testJavaOutput, packageName1, className),
            MarionetteTemplates.getCallbackTemplate(),
            tData.build()
        )
    }

    private fun fromPackage(base: File, pack: String, name: String): File {
        return File(File(base, pack.replace('.', '/')), "$name.java")
    }
}