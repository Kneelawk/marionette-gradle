package com.kneelawk.marionette.gradle

import com.google.common.collect.ImmutableMap
import org.gradle.api.Named
import java.util.regex.Pattern

data class TypeName(val packageName: String, val className: String) : Named {
    companion object {
        val PRIMITIVE_TO_WRAPPER: ImmutableMap<String, TypeName>
        val WRAPPER_TO_PRIMITIVE: ImmutableMap<TypeName, String>

        init {
            val p2wb = ImmutableMap.builder<String, TypeName>()
            val w2pb = ImmutableMap.builder<TypeName, String>()
            setup(p2wb, w2pb, "byte" to "Byte")
            setup(p2wb, w2pb, "char" to "Character")
            setup(p2wb, w2pb, "short" to "Short")
            setup(p2wb, w2pb, "int" to "Integer")
            setup(p2wb, w2pb, "long" to "Long")
            setup(p2wb, w2pb, "float" to "Float")
            setup(p2wb, w2pb, "double" to "Double")
            setup(p2wb, w2pb, "boolean" to "Boolean")
            setup(p2wb, w2pb, "void" to "Void")
            PRIMITIVE_TO_WRAPPER = p2wb.build()
            WRAPPER_TO_PRIMITIVE = w2pb.build()
        }

        private val PACKAGE_SEPARATOR = Pattern.compile("\\.[A-Z]")

        fun fromString(qualified: String): TypeName {
            val match = PACKAGE_SEPARATOR.matcher(qualified)
            return if (match.find()) {
                TypeName(qualified.substring(0, match.start()), qualified.substring(match.start() + 1))
            } else {
                val index = qualified.lastIndexOf('.')
                if (index == -1) {
                    // must be a primitive
                    TypeName("", qualified)
                } else {
                    // the class name is not capitalized
                    TypeName(qualified.substring(0, index), qualified.substring(index + 1))
                }
            }
        }

        private fun setup(
            p2wb: ImmutableMap.Builder<String, TypeName>,
            w2pb: ImmutableMap.Builder<TypeName, String>,
            p2w: Pair<String, String>
        ) {
            val wrapper = TypeName("java.lang", p2w.second)
            p2wb.put(p2w.first, wrapper)
            w2pb.put(wrapper, p2w.first)
        }
    }

    val primitive by lazy { WRAPPER_TO_PRIMITIVE[this] }
    val wrapper by lazy { PRIMITIVE_TO_WRAPPER[className] }
    val qualified = if (packageName.isEmpty()) className else "$packageName.$className"

    override fun getName(): String {
        return qualified
    }
}
