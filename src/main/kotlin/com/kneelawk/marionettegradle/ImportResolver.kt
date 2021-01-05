package com.kneelawk.marionettegradle

import com.google.common.collect.ImmutableList

class ImportResolver {
    private val existingNames = mutableMapOf<String, Int>()
    private val unqualifiedNames = mutableMapOf<Any, String>()
    private val packages = mutableMapOf<Any, String>()
    private val qualifiedNames = mutableMapOf<Any, String>()

    private fun toQualified(pack: String, name: String): String {
        return "${pack}.${name}"
    }

    fun add(pack: String, name: String): Any {
        existingNames.compute(name) { _, value -> value?.let { it + 1 } ?: 1 }
        val key = Any()
        unqualifiedNames[key] = name
        packages[key] = pack
        qualifiedNames[key] = toQualified(pack, name)
        return key
    }

    operator fun get(key: Any): String {
        if (!unqualifiedNames.containsKey(key)) {
            throw IllegalArgumentException("Key not found")
        }

        return if (existingNames[unqualifiedNames[key]]!! < 2) {
            unqualifiedNames[key]!!
        } else {
            qualifiedNames[key]!!
        }
    }

    fun getImport(key: Any): String? {
        if (!unqualifiedNames.containsKey(key)) {
            throw IllegalArgumentException("Key not found")
        }

        return if (existingNames[unqualifiedNames[key]]!! < 2) {
            packages[key]
        } else {
            null
        }
    }

    fun getImport(key: Any, curPack: String): String? {
        val import = getImport(key)
        return if (curPack == import) {
            null
        } else {
            import
        }
    }

    fun getImports(): List<String> {
        val builder = ImmutableList.builder<String>()
        for (key in unqualifiedNames.keys) {
            if (existingNames[unqualifiedNames[key]]!! < 2) {
                builder.add(qualifiedNames[key]!!)
            }
        }
        return builder.build()
    }

    fun getImports(curPack: String): List<String> {
        val builder = ImmutableList.builder<String>()
        for (key in unqualifiedNames.keys) {
            if (existingNames[unqualifiedNames[key]]!! < 2 && curPack != packages[key]) {
                builder.add(qualifiedNames[key]!!)
            }
        }
        return builder.build()
    }
}