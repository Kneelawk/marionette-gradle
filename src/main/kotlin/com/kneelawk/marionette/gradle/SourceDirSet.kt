package com.kneelawk.marionette.gradle

import java.io.File

data class SourceDirSet(
    val apiJava: File,
    val apiRes: File,
    val modJava: File,
    val modRes: File,
    val testJava: File,
    val testRes: File
)