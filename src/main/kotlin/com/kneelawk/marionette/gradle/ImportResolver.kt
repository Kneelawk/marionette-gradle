package com.kneelawk.marionette.gradle

import com.google.common.collect.ImmutableList

class ImportResolver {
    private val existingTypes = mutableMapOf<TypeName, Any>()
    private val existingNames = mutableMapOf<String, Int>()
    private val types = mutableMapOf<Any, TypeName>()

    fun add(pack: String, name: String): Any {
        val type = TypeName(pack, name)
        return if (existingTypes.containsKey(type)) {
            existingTypes[type]!!
        } else {
            existingNames.compute(name) { _, value -> value?.let { it + 1 } ?: 1 }

            val key = Any()
            existingTypes[type] = key
            types[key] = type

            key
        }
    }

    fun add(type: TypeName): Any {
        return if (existingTypes.containsKey(type)) {
            existingTypes[type]!!
        } else {
            existingNames.compute(type.className) { _, value -> value?.let { it + 1 } ?: 1 }

            val key = Any()
            existingTypes[type] = key
            types[key] = type

            key
        }
    }

    operator fun get(key: Any): String {
        if (!types.containsKey(key)) {
            throw IllegalArgumentException("Key not found")
        }

        return if (existingNames[types[key]!!.className]!! < 2) {
            types[key]!!.className
        } else {
            types[key]!!.qualified
        }
    }

    fun getImport(key: Any): String? {
        if (!types.containsKey(key)) {
            throw IllegalArgumentException("Key not found")
        }

        val type = types[key]!!
        return if (existingNames[type.className]!! < 2) {
            type.qualified
        } else {
            null
        }
    }

    fun getImport(key: Any, curPack: String): String? {
        if (!types.containsKey(key)) {
            throw IllegalArgumentException("Key not found")
        }

        val type = types[key]!!
        return if (curPack != type.packageName && existingNames[type.className]!! < 2) {
            type.qualified
        } else {
            null
        }
    }

    fun getImports(): List<String> {
        val builder = ImmutableList.builder<String>()
        for (type in existingTypes.keys) {
            if (existingNames[type.className]!! < 2 && type.packageName.isNotEmpty() && type.packageName != "java.lang") {
                builder.add(type.qualified)
            }
        }
        return builder.build()
    }

    fun getImports(curPack: String): List<String> {
        val builder = ImmutableList.builder<String>()
        for (type in existingTypes.keys) {
            if (existingNames[type.className]!! < 2 && curPack != type.packageName && type.packageName.isNotEmpty() && type.packageName != "java.lang") {
                builder.add(type.qualified)
            }
        }
        return builder.build()
    }
}