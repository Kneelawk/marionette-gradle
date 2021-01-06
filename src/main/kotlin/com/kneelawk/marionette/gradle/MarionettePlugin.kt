package com.kneelawk.marionette.gradle

import com.google.common.base.CaseFormat
import com.kneelawk.marionette.rt.MarionetteConstants
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.testing.Test
import java.io.File

class MarionettePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("marionette", MarionetteExtension::class.java, project)
        val javaPlugin = project.convention.getPlugin(JavaPluginConvention::class.java)

        val marionetteBuildDir = File(project.buildDir, "marionette")
        val genSrcDir = File(marionetteBuildDir, "generated/sources")
        val srcSetMap = mutableMapOf<String, SourceSetSet>()

        setupSourceSets(project, javaPlugin, extension, genSrcDir, srcSetMap)
        setupTestTasks(project, extension, srcSetMap)
    }

    fun setupSourceSets(
        project: Project,
        javaPlugin: JavaPluginConvention,
        extension: MarionetteExtension,
        genSrcDir: File,
        srcSetMap: MutableMap<String, SourceSetSet>
    ) {
        extension.testSets.all { testSet ->
            val genApiSrcDir = File(genSrcDir, "${testSet.name}Api")
            val genModSrcDir = File(genSrcDir, "${testSet.name}Mod")
            val genTestSrcDir = File(genSrcDir, "${testSet.name}Test")
            val srcDirs = SourceDirSet(
                File(genApiSrcDir, "java"),
                File(genApiSrcDir, "resources"),
                File(genModSrcDir, "java"),
                File(genModSrcDir, "resources"),
                File(genTestSrcDir, "java"),
                File(genTestSrcDir, "resources")
            )

            val genTask = createGenTask(project, testSet, srcDirs)

            srcSetMap[testSet.name] = createSourceSets(project, javaPlugin, testSet, genTask, srcDirs)
        }
    }

    private fun createGenTask(
        project: Project,
        testSet: MarionetteTestSet,
        srcDirs: SourceDirSet
    ): Task {
        val taskName = if (testSet.name == "marionette") {
            "marionetteGenSources"
        } else {
            val name = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, testSet.name)
            "marionette${name}GenSources"
        }

        return (project.task(
            mapOf("type" to MarionetteSourceGenerator::class.java),
            taskName
        ) as MarionetteSourceGenerator).apply {
            description = "Generates '${testSet.name}' glue source files"
            group = "Marionette"
            apiJavaOutput = srcDirs.apiJava
            apiResourcesOutput = srcDirs.apiRes
            modJavaOutput = srcDirs.modJava
            modResourcesOutput = srcDirs.modRes
            testJavaOutput = srcDirs.testJava
            testResourcesOutput = srcDirs.testRes
            testSetName = testSet.name
            names = testSet.names
            testSet.commonSignals.signals.all { commonSignals.signals.add(it) }
            testSet.clientSignals.signals.all { clientSignals.signals.add(it) }
            testSet.serverSignals.signals.all { serverSignals.signals.add(it) }
        }
    }

    fun createSourceSets(
        project: Project,
        javaPlugin: JavaPluginConvention,
        testSet: MarionetteTestSet,
        genTask: Task,
        srcDirs: SourceDirSet
    ): SourceSetSet {
        val apiSourceSet = javaPlugin.sourceSets.create("${testSet.name}Api")
        val modSourceSet = javaPlugin.sourceSets.create("${testSet.name}Mod")
        val testSourceSet = javaPlugin.sourceSets.create("${testSet.name}Test")

        val genApiSourceSet = javaPlugin.sourceSets.create("${testSet.name}GenApi")
        genApiSourceSet.java.setSrcDirs(listOf(srcDirs.apiJava))
        genApiSourceSet.resources.setSrcDirs(listOf(srcDirs.apiRes))
        val genModSourceSet = javaPlugin.sourceSets.create("${testSet.name}GenMod")
        genModSourceSet.java.setSrcDirs(listOf(srcDirs.modJava))
        genModSourceSet.resources.setSrcDirs(listOf(srcDirs.modRes))
        val genTestSourceSet = javaPlugin.sourceSets.create("${testSet.name}GenTest")
        genTestSourceSet.java.setSrcDirs(listOf(srcDirs.testJava))
        genTestSourceSet.resources.setSrcDirs(listOf(srcDirs.testRes))

        project.tasks.getByName(genApiSourceSet.compileJavaTaskName).dependsOn(genTask)
        project.tasks.getByName(genModSourceSet.compileJavaTaskName).dependsOn(genTask)
        project.tasks.getByName(genTestSourceSet.compileJavaTaskName).dependsOn(genTask)

        val mainSourceSet = javaPlugin.sourceSets.getByName("main")
        val mainCompileClasspath = project.configurations.getByName(mainSourceSet.compileClasspathConfigurationName)
        val mainRuntimeClasspath = project.configurations.getByName(mainSourceSet.runtimeClasspathConfigurationName)

        val marionetteRtVersion = MarionetteConstants.MARIONETTE_VERSION

        project.dependencies.apply {
            // configure inter-set dependencies
            add(apiSourceSet.implementationConfigurationName, genApiSourceSet.output)
            add(genModSourceSet.implementationConfigurationName, mainSourceSet.output)
            add(genModSourceSet.implementationConfigurationName, mainCompileClasspath)
            add(genModSourceSet.runtimeClasspathConfigurationName, mainRuntimeClasspath)
            add(genModSourceSet.implementationConfigurationName, genApiSourceSet.output)
            add(modSourceSet.implementationConfigurationName, mainSourceSet.output)
            add(modSourceSet.implementationConfigurationName, mainCompileClasspath)
            add(modSourceSet.runtimeClasspathConfigurationName, mainRuntimeClasspath)
            add(modSourceSet.implementationConfigurationName, genModSourceSet.output)
            add(modSourceSet.implementationConfigurationName, genApiSourceSet.output)
            add(modSourceSet.implementationConfigurationName, apiSourceSet.output)
            add(genTestSourceSet.implementationConfigurationName, genApiSourceSet.output)
            add(testSourceSet.implementationConfigurationName, genTestSourceSet.output)
            add(testSourceSet.implementationConfigurationName, genApiSourceSet.output)
            add(testSourceSet.implementationConfigurationName, apiSourceSet.output)

            // configure external dependencies
            add(
                genApiSourceSet.implementationConfigurationName,
                "com.kneelawk.marionette:marionette-rt:${marionetteRtVersion}"
            )
            add(
                genModSourceSet.implementationConfigurationName,
                "com.kneelawk.marionette:marionette-rt:${marionetteRtVersion}"
            )
            add(
                genTestSourceSet.implementationConfigurationName,
                "com.kneelawk.marionette:marionette-rt:${marionetteRtVersion}"
            )
            add(
                apiSourceSet.implementationConfigurationName,
                "com.kneelawk.marionette:marionette-rt:${marionetteRtVersion}"
            )
            add(
                modSourceSet.implementationConfigurationName,
                "com.kneelawk.marionette:marionette-rt:${marionetteRtVersion}"
            )
            add(
                testSourceSet.implementationConfigurationName,
                "com.kneelawk.marionette:marionette-rt:${marionetteRtVersion}"
            )
        }

        return SourceSetSet(modSourceSet, testSourceSet)
    }

    fun setupTestTasks(project: Project, extension: MarionetteExtension, srcSetMap: MutableMap<String, SourceSetSet>) {
        project.afterEvaluate { afterProject ->
            val testTasks = extension.testSets.map { testSet ->
                createTestTask(
                    project,
                    testSet,
                    srcSetMap[testSet.name]!!,
                    extension.useJUnitPlatform || testSet.useJUnitPlatform
                )
            }

            project.task(mapOf("dependsOn" to testTasks), "marionetteTestAll").apply {
                description = "Runs all Marionette Minecraft integration tests"
                group = "verification"
            }
        }
    }

    fun createTestTask(
        project: Project,
        testSet: MarionetteTestSet,
        srcs: SourceSetSet,
        useJUnitPlatform: Boolean
    ): Task {
        val taskName = if (testSet.name == "marionette") {
            "marionetteTest"
        } else {
            val name = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, testSet.name)
            "marionette${name}Test"
        }

        return (project.task(mapOf("type" to Test::class.java), taskName) as Test).apply {
            description = "Runs '${testSet.name}' Minecraft integration tests"
            group = "verification"
            dependsOn(srcs.mod.classesTaskName)

            testClassesDirs = srcs.test.output
            classpath = srcs.test.runtimeClasspath
            outputs.upToDateWhen { false }
            mustRunAfter("test")
            if (useJUnitPlatform) {
                useJUnitPlatform()
            }
            testLogging.showStandardStreams = testSet.showStandardStreams
            systemProperty(
                MarionetteConstants.MINECRAFT_CLASSPATH_PROPERTY,
                srcs.mod.runtimeClasspath.joinToString(File.pathSeparator)
            )
            systemProperty(MarionetteConstants.PROJECT_DIR_PROPERTY, project.projectDir)
            systemProperty(MarionetteConstants.PROJECT_ROOT_DIR_PROPERTY, project.rootDir)
            systemProperty(MarionetteConstants.PROJECT_BUILD_DIR_PROPERTY, project.buildDir)
        }
    }

    data class SourceSetSet(val mod: SourceSet, val test: SourceSet)
}