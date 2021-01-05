package com.kneelawk.marionette.gradle

import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project

open class MarionetteExtension(project: Project) {
    val testSets: NamedDomainObjectContainer<MarionetteTestSet> = project.container(MarionetteTestSet::class.java)
        .apply { add(project.objects.newInstance(MarionetteTestSet::class.java, "marionette")) }

    var useJUnitPlatform = false

    fun testSets(action: Action<in NamedDomainObjectContainer<MarionetteTestSet>>) {
        action.execute(testSets)
    }

    fun useJUnitPlatform() {
        useJUnitPlatform = true
    }
}
