package com.kneelawk.marionette.gradle

import com.google.common.base.CaseFormat
import com.kneelawk.marionette.gradle.callback.QueueCallback
import com.kneelawk.marionette.gradle.names.MarionetteNames
import com.kneelawk.marionette.gradle.proxy.MarionetteProxies
import com.kneelawk.marionette.gradle.proxy.MarionetteProxy
import com.kneelawk.marionette.gradle.signal.MarionetteSignals
import com.kneelawk.marionette.rt.callback.template.CallbackTData
import com.kneelawk.marionette.rt.instance.template.*
import com.kneelawk.marionette.rt.mod.MinecraftAccessConstructorTData
import com.kneelawk.marionette.rt.mod.MinecraftAccessQueueCallbackInfo
import com.kneelawk.marionette.rt.mod.client.template.ClientGlobalQueuesTData
import com.kneelawk.marionette.rt.mod.client.template.ClientGlobalSignalsTData
import com.kneelawk.marionette.rt.mod.client.template.MarionetteClientPreLaunchTData
import com.kneelawk.marionette.rt.mod.client.template.MinecraftClientAccessTData
import com.kneelawk.marionette.rt.mod.server.template.MarionetteServerPreLaunchTData
import com.kneelawk.marionette.rt.mod.server.template.MinecraftServerAccessTData
import com.kneelawk.marionette.rt.mod.server.template.ServerGlobalQueuesTData
import com.kneelawk.marionette.rt.mod.server.template.ServerGlobalSignalsTData
import com.kneelawk.marionette.rt.mod.template.MarionetteModPreLaunchTData
import com.kneelawk.marionette.rt.proxy.template.*
import com.kneelawk.marionette.rt.rmi.template.RMIMinecraftAccessConstructorTData
import com.kneelawk.marionette.rt.rmi.template.RMIMinecraftAccessQueueCallbackInfo
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

    @get:Nested
    var commonProxies: MarionetteProxies = objectFactory.newInstance(MarionetteProxies::class.java)

    @get:Nested
    var clientProxies: MarionetteProxies = objectFactory.newInstance(MarionetteProxies::class.java)

    @get:Nested
    var serverProxies: MarionetteProxies = objectFactory.newInstance(MarionetteProxies::class.java)

    @TaskAction
    fun run() {
        val engine = VelocityEngine()
        engine.init()

        val modId = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_HYPHEN, testSetName)
        val modName = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, testSetName)

        val commonProxyMappings = generateProxyMappings(
            commonProxies,
            names.proxy.apiCommonProxyPackage,
            names.proxy.apiCommonProxyPrefix,
            names.proxy.apiCommonProxySuffix,
            names.proxy.modCommonProxyPackage,
            names.proxy.modCommonProxyPrefix,
            names.proxy.modCommonProxySuffix
        )
        val clientProxyMappings = generateProxyMappings(
            clientProxies,
            names.proxy.apiClientProxyPackage,
            names.proxy.apiClientProxyPrefix,
            names.proxy.apiClientProxySuffix,
            names.proxy.modClientProxyPackage,
            names.proxy.modClientProxyPrefix,
            names.proxy.modClientProxySuffix
        )
        val serverProxyMappings = generateProxyMappings(
            serverProxies,
            names.proxy.apiServerProxyPackage,
            names.proxy.apiServerProxyPrefix,
            names.proxy.apiServerProxySuffix,
            names.proxy.modServerProxyPackage,
            names.proxy.modServerProxyPrefix,
            names.proxy.modServerProxySuffix
        )

        generate(
            engine,
            File(modResourcesOutput, "fabric.mod.json"),
            MarionetteTemplates.getFabricModJsonTemplate(),
            FabricModJsonTData.builder().id(modId).name(modName)
                .preLaunchEntryPoint("${names.entryPoint.preLaunchEntryPointPackage}.${names.entryPoint.preLaunchEntryPointName}")
                .build()
        )

        generateRMIClientAccess(engine, commonProxyMappings, clientProxyMappings)
        generateRMIServerAccess(engine, commonProxyMappings, serverProxyMappings)

        generateClientInstance(engine, commonProxyMappings, clientProxyMappings)
        generateServerInstance(engine, commonProxyMappings, serverProxyMappings)
        generateClientInstanceBuilder(engine)
        generateServerInstanceBuilder(engine)

        generateModPreLaunch(engine)
        generateClientPreLaunch(engine)
        generateServerPreLaunch(engine)
        generateClientAccess(engine, commonProxyMappings, clientProxyMappings)
        generateServerAccess(engine, commonProxyMappings, serverProxyMappings)

        generateClientGlobalSignals(engine)
        generateServerGlobalSignals(engine)

        generateClientGlobalQueues(engine, commonProxyMappings, clientProxyMappings)
        generateServerGlobalQueues(engine, commonProxyMappings, serverProxyMappings)

        generateQueueCallbacks(engine, commonProxyMappings, clientProxyMappings, serverProxyMappings)

        generateProxies(engine, commonProxyMappings, clientProxyMappings, serverProxyMappings)
    }

    private fun generateProxyMappings(
        proxies: MarionetteProxies,
        apiPackage: String,
        apiPrefix: String,
        apiSuffix: String,
        modPackage: String,
        modPrefix: String,
        modSuffix: String
    ): Map<TypeName, MaybeProxiedTypeName> {
        val mappings = mutableMapOf<TypeName, MaybeProxiedTypeName>()

        for (proxy in proxies.proxies) {
            val unproxiedType = TypeName.fromString(proxy.proxiedClass)
            val interfaceType = TypeName(
                proxy.interfacePackageName ?: apiPackage,
                proxy.interfaceClassName ?: getProxyClassName(proxy, apiPrefix, apiSuffix)
            )
            val implementationType = TypeName(
                proxy.implementationPackageName ?: modPackage,
                proxy.implementationClassName ?: getProxyClassName(proxy, modPrefix, modSuffix)
            )
            mappings[unproxiedType] = MaybeProxiedTypeName(unproxiedType, interfaceType, implementationType)
        }

        return mappings
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

    private fun generateRMIClientAccess(
        engine: VelocityEngine,
        commonMappings: Map<TypeName, MaybeProxiedTypeName>,
        clientMappings: Map<TypeName, MaybeProxiedTypeName>
    ) {
        val remoteExceptionType = TypeName("java.rmi", "RemoteException")
        val imports = ImportResolver()
        val remoteException = imports.add(remoteExceptionType)
        // we make lists of suppliers because we need to add all imports before we start resolving them
        val queueCallbacks = mutableListOf<() -> RMIMinecraftAccessQueueCallbackInfo>()
        val constructors = mutableListOf<() -> RMIMinecraftAccessConstructorTData>()
        queueCallbacks.addAll(commonQueues.map { cb ->
            val clazz = imports.add(
                cb.packageName ?: names.proxy.apiCommonQueueCallbackPackage,
                getQueueCallbackClassName(
                    cb,
                    names.proxy.apiCommonQueueCallbackPrefix,
                    names.proxy.apiCommonQueueCallbackSuffix
                )
            )

            return@map {
                RMIMinecraftAccessQueueCallbackInfo.builder()
                    .callbackName(cb.name)
                    .callbackClass(imports[clazz])
                    .build()
            }
        })
        queueCallbacks.addAll(clientQueues.map { cb ->
            val clazz = imports.add(
                cb.packageName ?: names.proxy.apiClientQueueCallbackPackage,
                getQueueCallbackClassName(
                    cb,
                    names.proxy.apiClientQueueCallbackPrefix,
                    names.proxy.apiClientQueueCallbackSuffix
                )
            )

            return@map {
                RMIMinecraftAccessQueueCallbackInfo.builder()
                    .callbackName(cb.name)
                    .callbackClass(imports[clazz])
                    .build()
            }
        })
        // import resolver specifically for constructor name resolution
        val constructorNameImports = ImportResolver()
        constructors.addAll(commonProxies.proxies.flatMap { proxy ->
            return@flatMap proxy.constructors.map { ctr ->
                val unproxied = constructorNameImports.add(TypeName.fromString(proxy.proxiedClass))
                val toConstruct =
                    getProxiedTypeName(commonMappings, TypeName.fromString(proxy.proxiedClass)).toInterface(imports)
                val parameters = ctr.arguments.map {
                    getProxiedTypeName(commonMappings, TypeName.fromString(it)).toInterface(imports)
                }
                val exceptions = ctr.exceptions.map { imports.add(TypeName.fromString(it)) }.toMutableSet()
                exceptions.add(remoteException)

                return@map {
                    RMIMinecraftAccessConstructorTData.builder()
                        .constructorName(constructorNameImports[unproxied].replace('.', '_'))
                        .constructorClass(imports[toConstruct])
                        .parameters(parameters.map { imports[it] })
                        .exceptions(exceptions.map { imports[it] })
                        .build()
                }
            }
        })
        constructors.addAll(clientProxies.proxies.flatMap { proxy ->
            return@flatMap proxy.constructors.map { ctr ->
                val unproxied = constructorNameImports.add(TypeName.fromString(proxy.proxiedClass))
                val toConstruct =
                    getProxiedTypeName(clientMappings, TypeName.fromString(proxy.proxiedClass)).toInterface(imports)
                val parameters = ctr.arguments.map {
                    getProxiedTypeName(clientMappings, TypeName.fromString(it)).toInterface(imports)
                }
                val exceptions = ctr.exceptions.map { imports.add(TypeName.fromString(it)) }.toMutableSet()
                exceptions.add(remoteException)

                return@map {
                    RMIMinecraftAccessConstructorTData.builder()
                        .constructorName(constructorNameImports[unproxied].replace('.', '_'))
                        .constructorClass(imports[toConstruct])
                        .parameters(parameters.map { imports[it] })
                        .exceptions(exceptions.map { imports[it] })
                        .build()
                }
            }
        })

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
                .queueCallbacks(queueCallbacks.map { it() })
                .constructors(constructors.map { it() })
                .build()
        )
    }

    private fun generateRMIServerAccess(
        engine: VelocityEngine,
        commonMappings: Map<TypeName, MaybeProxiedTypeName>,
        serverMappings: Map<TypeName, MaybeProxiedTypeName>
    ) {
        val remoteExceptionType = TypeName("java.rmi", "RemoteException")
        val imports = ImportResolver()
        val remoteException = imports.add(remoteExceptionType)
        val queueCallbacks = mutableListOf<() -> RMIMinecraftAccessQueueCallbackInfo>()
        val constructors = mutableListOf<() -> RMIMinecraftAccessConstructorTData>()
        queueCallbacks.addAll(commonQueues.map { cb ->
            val clazz = imports.add(
                cb.packageName ?: names.proxy.apiCommonQueueCallbackPackage,
                getQueueCallbackClassName(
                    cb,
                    names.proxy.apiCommonQueueCallbackPrefix,
                    names.proxy.apiCommonQueueCallbackSuffix
                )
            )

            return@map {
                RMIMinecraftAccessQueueCallbackInfo.builder()
                    .callbackName(cb.name)
                    .callbackClass(imports[clazz])
                    .build()
            }
        })
        queueCallbacks.addAll(serverQueues.map { cb ->
            val clazz = imports.add(
                cb.packageName ?: names.proxy.apiServerQueueCallbackPackage,
                getQueueCallbackClassName(
                    cb,
                    names.proxy.apiServerQueueCallbackPrefix,
                    names.proxy.apiServerQueueCallbackSuffix
                )
            )

            return@map {
                RMIMinecraftAccessQueueCallbackInfo.builder()
                    .callbackName(cb.name)
                    .callbackClass(imports[clazz])
                    .build()
            }
        })
        val constructorNameImports = ImportResolver()
        constructors.addAll(commonProxies.proxies.flatMap { proxy ->
            return@flatMap proxy.constructors.map { ctr ->
                val unproxied = constructorNameImports.add(TypeName.fromString(proxy.proxiedClass))
                val toConstruct =
                    getProxiedTypeName(commonMappings, TypeName.fromString(proxy.proxiedClass)).toInterface(imports)
                val parameters = ctr.arguments.map {
                    getProxiedTypeName(commonMappings, TypeName.fromString(it)).toInterface(imports)
                }
                val exceptions = ctr.exceptions.map { imports.add(TypeName.fromString(it)) }.toMutableSet()
                exceptions.add(remoteException)

                return@map {
                    RMIMinecraftAccessConstructorTData.builder()
                        .constructorName(constructorNameImports[unproxied].replace('.', '_'))
                        .constructorClass(imports[toConstruct])
                        .parameters(parameters.map { imports[it] })
                        .exceptions(exceptions.map { imports[it] })
                        .build()
                }
            }
        })
        constructors.addAll(serverProxies.proxies.flatMap { proxy ->
            return@flatMap proxy.constructors.map { ctr ->
                val unproxied = constructorNameImports.add(TypeName.fromString(proxy.proxiedClass))
                val toConstruct =
                    getProxiedTypeName(serverMappings, TypeName.fromString(proxy.proxiedClass)).toInterface(imports)
                val parameters = ctr.arguments.map {
                    getProxiedTypeName(serverMappings, TypeName.fromString(it)).toInterface(imports)
                }
                val exceptions = ctr.exceptions.map { imports.add(TypeName.fromString(it)) }.toMutableSet()
                exceptions.add(remoteException)

                return@map {
                    RMIMinecraftAccessConstructorTData.builder()
                        .constructorName(constructorNameImports[unproxied].replace('.', '_'))
                        .constructorClass(imports[toConstruct])
                        .parameters(parameters.map { imports[it] })
                        .exceptions(exceptions.map { imports[it] })
                        .build()
                }
            }
        })

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
                .queueCallbacks(queueCallbacks.map { it() })
                .constructors(constructors.map { it() })
                .build()
        )
    }

    private fun generateClientInstance(
        engine: VelocityEngine,
        commonMappings: Map<TypeName, MaybeProxiedTypeName>,
        clientMappings: Map<TypeName, MaybeProxiedTypeName>
    ) {
        val remoteExceptionType = TypeName("java.rmi", "RemoteException")
        val imports = ImportResolver()
        val remoteException = imports.add(remoteExceptionType)
        val rmiClass = imports.add(names.proxy.apiClientAccessPackage, names.proxy.apiClientAccessName)
        val queueCallbacks = mutableListOf<() -> InstanceQueueCallbackInfo>()
        val constructors = mutableListOf<() -> RMIMinecraftAccessConstructorTData>()
        queueCallbacks.addAll(commonQueues.map { cb ->
            val clazz = imports.add(
                cb.packageName ?: names.proxy.testCommonQueueCallbackPackage,
                getQueueCallbackClassName(
                    cb,
                    names.proxy.testCommonQueueCallbackPrefix,
                    names.proxy.testCommonQueueCallbackSuffix
                )
            )
            val returnTypeName = TypeName.fromString(cb.returnType)
            val returnType = imports.add(returnTypeName.wrapper ?: returnTypeName)

            return@map {
                InstanceQueueCallbackInfo.builder()
                    .callbackName(cb.name)
                    .callbackClass(imports[clazz])
                    .callbackReturnType(imports[returnType])
                    .parameterCount(cb.arguments.size)
                    .build()
            }
        })
        queueCallbacks.addAll(clientQueues.map { cb ->
            val clazz = imports.add(
                cb.packageName ?: names.proxy.testClientQueueCallbackPackage,
                getQueueCallbackClassName(
                    cb,
                    names.proxy.testClientQueueCallbackPrefix,
                    names.proxy.testClientQueueCallbackSuffix
                )
            )
            val returnTypeName = TypeName.fromString(cb.returnType)
            val returnType = imports.add(returnTypeName.wrapper ?: returnTypeName)

            return@map {
                InstanceQueueCallbackInfo.builder()
                    .callbackName(cb.name)
                    .callbackClass(imports[clazz])
                    .callbackReturnType(imports[returnType])
                    .parameterCount(cb.arguments.size)
                    .build()
            }
        })
        val constructorNameImports = ImportResolver()
        constructors.addAll(commonProxies.proxies.flatMap { proxy ->
            return@flatMap proxy.constructors.map { ctr ->
                val unproxied = constructorNameImports.add(TypeName.fromString(proxy.proxiedClass))
                val toConstruct =
                    getProxiedTypeName(commonMappings, TypeName.fromString(proxy.proxiedClass)).toInterface(imports)
                val parameters = ctr.arguments.map {
                    getProxiedTypeName(commonMappings, TypeName.fromString(it)).toInterface(imports)
                }
                val exceptions = ctr.exceptions.map { imports.add(TypeName.fromString(it)) }.toMutableSet()
                exceptions.add(remoteException)

                return@map {
                    RMIMinecraftAccessConstructorTData.builder()
                        .constructorName(constructorNameImports[unproxied].replace('.', '_'))
                        .constructorClass(imports[toConstruct])
                        .parameters(parameters.map { imports[it] })
                        .exceptions(exceptions.map { imports[it] })
                        .build()
                }
            }
        })
        constructors.addAll(clientProxies.proxies.flatMap { proxy ->
            return@flatMap proxy.constructors.map { ctr ->
                val unproxied = constructorNameImports.add(TypeName.fromString(proxy.proxiedClass))
                val toConstruct =
                    getProxiedTypeName(clientMappings, TypeName.fromString(proxy.proxiedClass)).toInterface(imports)
                val parameters = ctr.arguments.map {
                    getProxiedTypeName(clientMappings, TypeName.fromString(it)).toInterface(imports)
                }
                val exceptions = ctr.exceptions.map { imports.add(TypeName.fromString(it)) }.toMutableSet()
                exceptions.add(remoteException)

                return@map {
                    RMIMinecraftAccessConstructorTData.builder()
                        .constructorName(constructorNameImports[unproxied].replace('.', '_'))
                        .constructorClass(imports[toConstruct])
                        .parameters(parameters.map { imports[it] })
                        .exceptions(exceptions.map { imports[it] })
                        .build()
                }
            }
        })

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
                .queueCallbacks(queueCallbacks.map { it() })
                .constructors(constructors.map { it() })
                .build()
        )
    }

    private fun generateServerInstance(
        engine: VelocityEngine,
        commonMappings: Map<TypeName, MaybeProxiedTypeName>,
        serverMappings: Map<TypeName, MaybeProxiedTypeName>
    ) {
        val remoteExceptionType = TypeName("java.rmi", "RemoteException")
        val imports = ImportResolver()
        val remoteException = imports.add(remoteExceptionType)
        val rmiClass = imports.add(names.proxy.apiServerAccessPackage, names.proxy.apiServerAccessName)
        val queueCallbacks = mutableListOf<() -> InstanceQueueCallbackInfo>()
        val constructors = mutableListOf<() -> RMIMinecraftAccessConstructorTData>()
        queueCallbacks.addAll(commonQueues.map { cb ->
            val clazz = imports.add(
                cb.packageName ?: names.proxy.testCommonQueueCallbackPackage,
                getQueueCallbackClassName(
                    cb,
                    names.proxy.testCommonQueueCallbackPrefix,
                    names.proxy.testCommonQueueCallbackSuffix
                )
            )
            val returnTypeName = TypeName.fromString(cb.returnType)
            val returnType = imports.add(returnTypeName.wrapper ?: returnTypeName)

            return@map {
                InstanceQueueCallbackInfo.builder()
                    .callbackName(cb.name)
                    .callbackClass(imports[clazz])
                    .callbackReturnType(imports[returnType])
                    .parameterCount(cb.arguments.size)
                    .build()
            }
        })
        queueCallbacks.addAll(serverQueues.map { cb ->
            val clazz = imports.add(
                cb.packageName ?: names.proxy.testServerQueueCallbackPackage,
                getQueueCallbackClassName(
                    cb,
                    names.proxy.testServerQueueCallbackPrefix,
                    names.proxy.testServerQueueCallbackSuffix
                )
            )
            val returnTypeName = TypeName.fromString(cb.returnType)
            val returnType = imports.add(returnTypeName.wrapper ?: returnTypeName)

            return@map {
                InstanceQueueCallbackInfo.builder()
                    .callbackName(cb.name)
                    .callbackClass(imports[clazz])
                    .callbackReturnType(imports[returnType])
                    .parameterCount(cb.arguments.size)
                    .build()
            }
        })
        val constructorNameImports = ImportResolver()
        constructors.addAll(commonProxies.proxies.flatMap { proxy ->
            return@flatMap proxy.constructors.map { ctr ->
                val unproxied = constructorNameImports.add(TypeName.fromString(proxy.proxiedClass))
                val toConstruct =
                    getProxiedTypeName(commonMappings, TypeName.fromString(proxy.proxiedClass)).toInterface(imports)
                val parameters = ctr.arguments.map {
                    getProxiedTypeName(commonMappings, TypeName.fromString(it)).toInterface(imports)
                }
                val exceptions = ctr.exceptions.map { imports.add(TypeName.fromString(it)) }.toMutableSet()
                exceptions.add(remoteException)

                return@map {
                    RMIMinecraftAccessConstructorTData.builder()
                        .constructorName(constructorNameImports[unproxied].replace('.', '_'))
                        .constructorClass(imports[toConstruct])
                        .parameters(parameters.map { imports[it] })
                        .exceptions(exceptions.map { imports[it] })
                        .build()
                }
            }
        })
        constructors.addAll(serverProxies.proxies.flatMap { proxy ->
            return@flatMap proxy.constructors.map { ctr ->
                val unproxied = constructorNameImports.add(TypeName.fromString(proxy.proxiedClass))
                val toConstruct =
                    getProxiedTypeName(serverMappings, TypeName.fromString(proxy.proxiedClass)).toInterface(imports)
                val parameters = ctr.arguments.map {
                    getProxiedTypeName(serverMappings, TypeName.fromString(it)).toInterface(imports)
                }
                val exceptions = ctr.exceptions.map { imports.add(TypeName.fromString(it)) }.toMutableSet()
                exceptions.add(remoteException)

                return@map {
                    RMIMinecraftAccessConstructorTData.builder()
                        .constructorName(constructorNameImports[unproxied].replace('.', '_'))
                        .constructorClass(imports[toConstruct])
                        .parameters(parameters.map { imports[it] })
                        .exceptions(exceptions.map { imports[it] })
                        .build()
                }
            }
        })

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
                .queueCallbacks(queueCallbacks.map { it() })
                .constructors(constructors.map { it() })
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

    private fun generateClientAccess(
        engine: VelocityEngine,
        commonMappings: Map<TypeName, MaybeProxiedTypeName>,
        clientMappings: Map<TypeName, MaybeProxiedTypeName>
    ) {
        val remoteExceptionType = TypeName("java.rmi", "RemoteException")
        val imports = ImportResolver()
        val remoteException = imports.add(remoteExceptionType)
        val rmiClass = imports.add(names.proxy.apiClientAccessPackage, names.proxy.apiClientAccessName)
        val signalClass = imports.add(names.utils.clientGlobalSignalsPackage, names.utils.clientGlobalSignalsName)
        val queueClass = imports.add(names.utils.clientGlobalQueuesPackage, names.utils.clientGlobalQueuesName)
        val queueCallbacks = mutableListOf<() -> MinecraftAccessQueueCallbackInfo>()
        val constructors = mutableListOf<() -> MinecraftAccessConstructorTData>()
        queueCallbacks.addAll(commonQueues.map { cb ->
            val clazz = imports.add(
                cb.packageName ?: names.proxy.apiCommonQueueCallbackPackage,
                getQueueCallbackClassName(
                    cb,
                    names.proxy.apiCommonQueueCallbackPrefix,
                    names.proxy.apiCommonQueueCallbackSuffix
                )
            )

            return@map {
                MinecraftAccessQueueCallbackInfo.builder()
                    .callbackName(cb.name)
                    .callbackClass(imports[clazz])
                    .build()
            }
        })
        queueCallbacks.addAll(clientQueues.map { cb ->
            val clazz = imports.add(
                cb.packageName ?: names.proxy.apiClientQueueCallbackPackage,
                getQueueCallbackClassName(
                    cb,
                    names.proxy.apiClientQueueCallbackPrefix,
                    names.proxy.apiClientQueueCallbackSuffix
                )
            )

            return@map {
                MinecraftAccessQueueCallbackInfo.builder()
                    .callbackName(cb.name)
                    .callbackClass(imports[clazz])
                    .build()
            }
        })
        constructors.addAll(commonProxies.proxies.flatMap { proxy ->
            return@flatMap proxy.constructors.map { ctr ->
                val unproxied = imports.add(TypeName.fromString(proxy.proxiedClass))
                val typeName =
                    getProxiedTypeName(commonMappings, TypeName.fromString(proxy.proxiedClass)).toConstructor(imports)
                val parameters = ctr.arguments.map {
                    getProxiedTypeName(commonMappings, TypeName.fromString(it)).toImplementation(imports)
                }
                val exceptions = ctr.exceptions.map { imports.add(TypeName.fromString(it)) }.toMutableSet()
                exceptions.add(remoteException)

                return@map {
                    MinecraftAccessConstructorTData.builder()
                        .constructorName(imports[unproxied].replace('.', '_'))
                        .constructorClass(typeName.toConstructorMaybeProxied(imports))
                        .parameters(parameters.map { it.toImplememtationMaybeProxied(imports) })
                        .exceptions(exceptions.map { imports[it] })
                        .build()
                }
            }
        })
        constructors.addAll(clientProxies.proxies.flatMap { proxy ->
            return@flatMap proxy.constructors.map { ctr ->
                val unproxied = imports.add(TypeName.fromString(proxy.proxiedClass))
                val typeName =
                    getProxiedTypeName(clientMappings, TypeName.fromString(proxy.proxiedClass)).toConstructor(imports)
                val parameters = ctr.arguments.map {
                    getProxiedTypeName(clientMappings, TypeName.fromString(it)).toImplementation(imports)
                }
                val exceptions = ctr.exceptions.map { imports.add(TypeName.fromString(it)) }.toMutableSet()
                exceptions.add(remoteException)

                return@map {
                    MinecraftAccessConstructorTData.builder()
                        .constructorName(imports[unproxied].replace('.', '_'))
                        .constructorClass(typeName.toConstructorMaybeProxied(imports))
                        .parameters(parameters.map { it.toImplememtationMaybeProxied(imports) })
                        .exceptions(exceptions.map { imports[it] })
                        .build()
                }
            }
        })

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
                .queueClass(imports[queueClass])
                .queueCallbacks(queueCallbacks.map { it() })
                .constructors(constructors.map { it() })
                .build()
        )
    }

    private fun generateServerAccess(
        engine: VelocityEngine,
        commonMappings: Map<TypeName, MaybeProxiedTypeName>,
        serverMappings: Map<TypeName, MaybeProxiedTypeName>
    ) {
        val remoteExceptionType = TypeName("java.rmi", "RemoteException")
        val imports = ImportResolver()
        val remoteException = imports.add(remoteExceptionType)
        val rmiClass = imports.add(names.proxy.apiServerAccessPackage, names.proxy.apiServerAccessName)
        val signalClass = imports.add(names.utils.serverGlobalSignalsPackage, names.utils.serverGlobalSignalsName)
        val queueClass = imports.add(names.utils.serverGlobalQueuesPackage, names.utils.serverGlobalQueuesName)
        val queueCallbacks = mutableListOf<() -> MinecraftAccessQueueCallbackInfo>()
        val constructors = mutableListOf<() -> MinecraftAccessConstructorTData>()
        queueCallbacks.addAll(commonQueues.map { cb ->
            val clazz = imports.add(
                cb.packageName ?: names.proxy.apiCommonQueueCallbackPackage,
                getQueueCallbackClassName(
                    cb,
                    names.proxy.apiCommonQueueCallbackPrefix,
                    names.proxy.apiCommonQueueCallbackSuffix
                )
            )

            return@map {
                MinecraftAccessQueueCallbackInfo.builder()
                    .callbackName(cb.name)
                    .callbackClass(imports[clazz])
                    .build()
            }
        })
        queueCallbacks.addAll(serverQueues.map { cb ->
            val clazz = imports.add(
                cb.packageName ?: names.proxy.apiServerQueueCallbackPackage,
                getQueueCallbackClassName(
                    cb,
                    names.proxy.apiServerQueueCallbackPrefix,
                    names.proxy.apiServerQueueCallbackSuffix
                )
            )

            return@map {
                MinecraftAccessQueueCallbackInfo.builder()
                    .callbackName(cb.name)
                    .callbackClass(imports[clazz])
                    .build()
            }
        })
        constructors.addAll(commonProxies.proxies.flatMap { proxy ->
            return@flatMap proxy.constructors.map { ctr ->
                val unproxied = imports.add(TypeName.fromString(proxy.proxiedClass))
                val typeName =
                    getProxiedTypeName(commonMappings, TypeName.fromString(proxy.proxiedClass)).toConstructor(imports)
                val parameters = ctr.arguments.map {
                    getProxiedTypeName(commonMappings, TypeName.fromString(it)).toImplementation(imports)
                }
                val exceptions = ctr.exceptions.map { imports.add(TypeName.fromString(it)) }.toMutableSet()
                exceptions.add(remoteException)

                return@map {
                    MinecraftAccessConstructorTData.builder()
                        .constructorName(imports[unproxied].replace('.', '_'))
                        .constructorClass(typeName.toConstructorMaybeProxied(imports))
                        .parameters(parameters.map { it.toImplememtationMaybeProxied(imports) })
                        .exceptions(exceptions.map { imports[it] })
                        .build()
                }
            }
        })
        constructors.addAll(serverProxies.proxies.flatMap { proxy ->
            return@flatMap proxy.constructors.map { ctr ->
                val unproxied = imports.add(TypeName.fromString(proxy.proxiedClass))
                val typeName =
                    getProxiedTypeName(serverMappings, TypeName.fromString(proxy.proxiedClass)).toConstructor(imports)
                val parameters = ctr.arguments.map {
                    getProxiedTypeName(serverMappings, TypeName.fromString(it)).toImplementation(imports)
                }
                val exceptions = ctr.exceptions.map { imports.add(TypeName.fromString(it)) }.toMutableSet()
                exceptions.add(remoteException)

                return@map {
                    MinecraftAccessConstructorTData.builder()
                        .constructorName(imports[unproxied].replace('.', '_'))
                        .constructorClass(typeName.toConstructorMaybeProxied(imports))
                        .parameters(parameters.map { it.toImplememtationMaybeProxied(imports) })
                        .exceptions(exceptions.map { imports[it] })
                        .build()
                }
            }
        })

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
                .queueClass(imports[queueClass])
                .queueCallbacks(queueCallbacks.map { it() })
                .constructors(constructors.map { it() })
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

    private fun generateClientGlobalQueues(
        engine: VelocityEngine,
        commonMappings: Map<TypeName, MaybeProxiedTypeName>,
        clientMappings: Map<TypeName, MaybeProxiedTypeName>
    ) {
        val imports = ImportResolver()
        val queueCallbacks = mutableListOf<() -> MinecraftAccessQueueCallbackInfo>()
        queueCallbacks.addAll(commonQueues.map { cb ->
            val clazz = imports.add(
                cb.packageName ?: names.proxy.apiCommonQueueCallbackPackage,
                getQueueCallbackClassName(
                    cb,
                    names.proxy.apiCommonQueueCallbackPrefix,
                    names.proxy.apiCommonQueueCallbackSuffix
                )
            )

            val parameters =
                cb.arguments.map { getProxiedTypeName(commonMappings, TypeName.fromString(it)).toCallback(imports) }

            return@map {
                MinecraftAccessQueueCallbackInfo.builder()
                    .callbackName(cb.name)
                    .callbackClass(imports[clazz])
                    .parameterTypes(parameters.map { it.toCallbackMaybeProxied(imports) })
                    .build()
            }
        })
        queueCallbacks.addAll(clientQueues.map { cb ->
            val clazz = imports.add(
                cb.packageName ?: names.proxy.apiClientQueueCallbackPackage,
                getQueueCallbackClassName(
                    cb,
                    names.proxy.apiClientQueueCallbackPrefix,
                    names.proxy.apiClientQueueCallbackSuffix
                )
            )

            val parameters =
                cb.arguments.map { getProxiedTypeName(clientMappings, TypeName.fromString(it)).toCallback(imports) }

            return@map {
                MinecraftAccessQueueCallbackInfo.builder()
                    .callbackName(cb.name)
                    .callbackClass(imports[clazz])
                    .parameterTypes(parameters.map { it.toCallbackMaybeProxied(imports) })
                    .build()
            }
        })

        val packageName = names.utils.clientGlobalQueuesPackage
        val className = names.utils.clientGlobalQueuesName

        generate(
            engine,
            fromPackage(modJavaOutput, packageName, className),
            MarionetteTemplates.getClientGlobalQueuesTemplate(),
            ClientGlobalQueuesTData.builder()
                .packageName(packageName)
                .className(className)
                .importNames(imports.getImports(packageName))
                .queueCallbacks(queueCallbacks.map { it() })
                .build()
        )
    }

    private fun generateServerGlobalQueues(
        engine: VelocityEngine,
        commonMappings: Map<TypeName, MaybeProxiedTypeName>,
        serverMappings: Map<TypeName, MaybeProxiedTypeName>
    ) {
        val imports = ImportResolver()
        val queueCallbacks = mutableListOf<() -> MinecraftAccessQueueCallbackInfo>()
        queueCallbacks.addAll(commonQueues.map { cb ->
            val clazz = imports.add(
                cb.packageName ?: names.proxy.apiCommonQueueCallbackPackage,
                getQueueCallbackClassName(
                    cb,
                    names.proxy.apiCommonQueueCallbackPrefix,
                    names.proxy.apiCommonQueueCallbackSuffix
                )
            )

            val parameters =
                cb.arguments.map { getProxiedTypeName(commonMappings, TypeName.fromString(it)).toCallback(imports) }

            return@map {
                MinecraftAccessQueueCallbackInfo.builder()
                    .callbackName(cb.name)
                    .callbackClass(imports[clazz])
                    .parameterTypes(parameters.map { it.toCallbackMaybeProxied(imports) })
                    .build()
            }
        })
        queueCallbacks.addAll(serverQueues.map { cb ->
            val clazz = imports.add(
                cb.packageName ?: names.proxy.apiServerQueueCallbackPackage,
                getQueueCallbackClassName(
                    cb,
                    names.proxy.apiServerQueueCallbackPrefix,
                    names.proxy.apiServerQueueCallbackSuffix
                )
            )

            val parameters =
                cb.arguments.map { getProxiedTypeName(serverMappings, TypeName.fromString(it)).toCallback(imports) }

            return@map {
                MinecraftAccessQueueCallbackInfo.builder()
                    .callbackName(cb.name)
                    .callbackClass(imports[clazz])
                    .parameterTypes(parameters.map { it.toCallbackMaybeProxied(imports) })
                    .build()
            }
        })

        val packageName = names.utils.serverGlobalQueuesPackage
        val className = names.utils.serverGlobalQueuesName

        generate(
            engine,
            fromPackage(modJavaOutput, packageName, className),
            MarionetteTemplates.getServerGlobalQueuesTemplate(),
            ServerGlobalQueuesTData.builder()
                .packageName(packageName)
                .className(className)
                .importNames(imports.getImports(packageName))
                .queueCallbacks(queueCallbacks.map { it() })
                .build()
        )
    }

    private fun generateQueueCallbacks(
        engine: VelocityEngine,
        commonProxyMappings: Map<TypeName, MaybeProxiedTypeName>,
        clientProxyMappings: Map<TypeName, MaybeProxiedTypeName>,
        serverProxyMappings: Map<TypeName, MaybeProxiedTypeName>
    ) {
        for (callback in commonQueues) {
            generateQueueCallback(
                engine,
                callback,
                names.proxy.apiCommonQueueCallbackPackage,
                names.proxy.apiCommonQueueCallbackPrefix,
                names.proxy.apiCommonQueueCallbackSuffix,
                true,
                commonProxyMappings
            )
            generateQueueCallback(
                engine,
                callback,
                names.proxy.testCommonQueueCallbackPackage,
                names.proxy.testCommonQueueCallbackPrefix,
                names.proxy.testCommonQueueCallbackSuffix,
                false,
                commonProxyMappings
            )
        }
        for (callback in clientQueues) {
            generateQueueCallback(
                engine,
                callback,
                names.proxy.apiClientQueueCallbackPackage,
                names.proxy.apiClientQueueCallbackPrefix,
                names.proxy.apiClientQueueCallbackSuffix,
                true,
                clientProxyMappings
            )
            generateQueueCallback(
                engine,
                callback,
                names.proxy.testClientQueueCallbackPackage,
                names.proxy.testClientQueueCallbackPrefix,
                names.proxy.testClientQueueCallbackSuffix,
                false,
                clientProxyMappings
            )
        }
        for (callback in serverQueues) {
            generateQueueCallback(
                engine,
                callback,
                names.proxy.apiServerQueueCallbackPackage,
                names.proxy.apiServerQueueCallbackPrefix,
                names.proxy.apiServerQueueCallbackSuffix,
                true,
                serverProxyMappings
            )
            generateQueueCallback(
                engine,
                callback,
                names.proxy.testServerQueueCallbackPackage,
                names.proxy.testServerQueueCallbackPrefix,
                names.proxy.testServerQueueCallbackSuffix,
                false,
                serverProxyMappings
            )
        }
    }

    private fun generateQueueCallback(
        engine: VelocityEngine,
        callback: QueueCallback,
        packageName: String,
        prefix: String,
        suffix: String,
        rmi: Boolean,
        mappings: Map<TypeName, MaybeProxiedTypeName>
    ) {
        val remoteExceptionType = TypeName("java.rmi", "RemoteException")
        val imports = ImportResolver()
        val returnType = if (!rmi) imports.add(TypeName.fromString(callback.returnType)) else null
        val argumentTypes =
            callback.arguments.map { getProxiedTypeName(mappings, TypeName.fromString(it)).toInterface(imports) }
        val exceptionTypes = if (!rmi) callback.exceptions.map { imports.add(TypeName.fromString(it)) } else null
        val remoteException = if (rmi) imports.add(remoteExceptionType) else null

        val packageName1 = callback.packageName ?: packageName
        val className = getQueueCallbackClassName(callback, prefix, suffix)

        val tData = CallbackTData.builder()
            .packageName(packageName1)
            .className(className)
            .importNames(imports.getImports(packageName1))
            .remote(rmi)
            .parameterTypes(argumentTypes.map { imports[it] })

        tData.returnType(returnType?.let { imports[it] } ?: "void")
        exceptionTypes?.let { et -> tData.exceptionTypes(et.map { imports[it] }) }
        remoteException?.let { tData.exceptionType(imports[it]) }

        generate(
            engine,
            fromPackage(if (rmi) apiJavaOutput else testJavaOutput, packageName1, className),
            MarionetteTemplates.getCallbackTemplate(),
            tData.build()
        )
    }

    private fun getQueueCallbackClassName(callback: QueueCallback, prefix: String, suffix: String): String {
        return callback.className ?: prefix + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, callback.name) + suffix
    }

    private fun generateProxies(
        engine: VelocityEngine,
        commonProxyMappings: Map<TypeName, MaybeProxiedTypeName>,
        clientProxyMappings: Map<TypeName, MaybeProxiedTypeName>,
        serverProxyMappings: Map<TypeName, MaybeProxiedTypeName>
    ) {
        for (proxy in commonProxies.proxies) {
            val rmiType = TypeName(
                proxy.interfacePackageName ?: names.proxy.apiCommonProxyPackage,
                proxy.interfaceClassName ?: getProxyClassName(
                    proxy,
                    names.proxy.apiCommonProxyPrefix,
                    names.proxy.apiCommonProxySuffix
                )
            )
            generateProxyInterface(engine, proxy, rmiType, commonProxyMappings)
            generateProxyImplementation(
                engine,
                proxy,
                names.proxy.modCommonProxyPackage,
                names.proxy.modCommonProxyPrefix,
                names.proxy.modCommonProxySuffix,
                rmiType,
                commonProxyMappings
            )
        }
        for (proxy in clientProxies.proxies) {
            val rmiType = TypeName(
                proxy.interfacePackageName ?: names.proxy.apiClientProxyPackage,
                proxy.interfaceClassName ?: getProxyClassName(
                    proxy,
                    names.proxy.apiClientProxyPrefix,
                    names.proxy.apiClientProxySuffix
                )
            )
            generateProxyInterface(engine, proxy, rmiType, clientProxyMappings)
            generateProxyImplementation(
                engine,
                proxy,
                names.proxy.modClientProxyPackage,
                names.proxy.modClientProxyPrefix,
                names.proxy.modClientProxySuffix,
                rmiType,
                clientProxyMappings
            )
        }
        for (proxy in serverProxies.proxies) {
            val rmiType = TypeName(
                proxy.interfacePackageName ?: names.proxy.apiServerProxyPackage,
                proxy.interfaceClassName ?: getProxyClassName(
                    proxy,
                    names.proxy.apiServerProxyPrefix,
                    names.proxy.apiServerProxySuffix
                )
            )
            generateProxyInterface(engine, proxy, rmiType, serverProxyMappings)
            generateProxyImplementation(
                engine,
                proxy,
                names.proxy.modServerProxyPackage,
                names.proxy.modServerProxyPrefix,
                names.proxy.modServerProxySuffix,
                rmiType,
                serverProxyMappings
            )
        }
    }

    private fun generateProxyInterface(
        engine: VelocityEngine,
        proxy: MarionetteProxy,
        typeName: TypeName,
        mappings: Map<TypeName, MaybeProxiedTypeName>
    ) {
        val remoteExceptionType = TypeName("java.rmi", "RemoteException")
        val imports = ImportResolver()
        val remoteException = imports.add(remoteExceptionType)
        val methods = proxy.methods.map { method ->
            val returnType = getProxiedTypeName(mappings, TypeName.fromString(method.returnType)).toInterface(imports)
            val parameters =
                method.arguments.map { getProxiedTypeName(mappings, TypeName.fromString(it)).toInterface(imports) }
            val exceptions = method.exceptions.map { imports.add(TypeName.fromString(it)) }.toMutableSet()
            exceptions.add(remoteException)

            return@map {
                ProxyInterfaceMethodTData.builder()
                    .methodName(method.name)
                    .returnType(imports[returnType])
                    .parameterTypes(parameters.map { imports[it] })
                    .exceptionTypes(exceptions.map { imports[it] })
                    .build()
            }
        }

        generate(
            engine,
            fromPackage(apiJavaOutput, typeName.packageName, typeName.className),
            MarionetteTemplates.getProxyInterfaceTemplate(),
            ProxyInterfaceTData.builder()
                .packageName(typeName.packageName)
                .className(typeName.className)
                .importNames(imports.getImports(typeName.packageName))
                .methods(methods.map { it() })
                .build()
        )
    }

    private fun generateProxyImplementation(
        engine: VelocityEngine,
        proxy: MarionetteProxy,
        packageName: String,
        prefix: String,
        suffix: String,
        rmiType: TypeName,
        mappings: Map<TypeName, MaybeProxiedTypeName>
    ) {
        val remoteExceptionType = TypeName("java.rmi", "RemoteException")
        val imports = ImportResolver()
        val remoteException = imports.add(remoteExceptionType)
        val rmiClass = imports.add(rmiType)
        val proxiedClass = imports.add(TypeName.fromString(proxy.proxiedClass))
        val methods = proxy.methods.map { method ->
            val returnType =
                getProxiedTypeName(mappings, TypeName.fromString(method.returnType)).toImplementation(imports)
            val parameters =
                method.arguments.map { getProxiedTypeName(mappings, TypeName.fromString(it)).toImplementation(imports) }
            val exceptions = method.exceptions.map { imports.add(TypeName.fromString(it)) }.toMutableSet()
            exceptions.add(remoteException)

            return@map {
                ProxyImplementationMethodTData.builder()
                    .methodName(method.name)
                    .returnType(returnType.toImplememtationMaybeProxied(imports))
                    .parameterTypes(parameters.map { it.toImplememtationMaybeProxied(imports) })
                    .exceptionTypes(exceptions.map { imports[it] })
                    .build()
            }
        }

        val packageName1 = proxy.implementationPackageName ?: packageName
        val className = proxy.implementationClassName ?: getProxyClassName(proxy, prefix, suffix)

        generate(
            engine,
            fromPackage(modJavaOutput, packageName1, className),
            MarionetteTemplates.getProxyImplementationTemplate(),
            ProxyImplementationTData.builder()
                .packageName(packageName1)
                .className(className)
                .importNames(imports.getImports(packageName1))
                .rmiClass(imports[rmiClass])
                .proxiedClass(imports[proxiedClass])
                .methods(methods.map { it() })
                .build()
        )
    }

    private fun getProxyClassName(proxy: MarionetteProxy, prefix: String, suffix: String): String {
        return prefix + TypeName.fromString(proxy.proxiedClass).className.replace('.', '_') + suffix
    }

    private fun getProxiedTypeName(
        mappings: Map<TypeName, MaybeProxiedTypeName>,
        typeName: TypeName
    ): MaybeProxiedTypeName {
        return mappings.getOrDefault(typeName, MaybeProxiedTypeName(typeName, null, null))
    }

    private fun fromPackage(base: File, pack: String, name: String): File {
        return File(File(base, pack.replace('.', '/')), "$name.java")
    }
}