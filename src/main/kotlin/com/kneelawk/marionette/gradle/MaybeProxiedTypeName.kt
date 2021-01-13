package com.kneelawk.marionette.gradle

import com.kneelawk.marionette.rt.mod.CallbackMaybeProxied
import com.kneelawk.marionette.rt.mod.ConstructorMaybeProxied
import com.kneelawk.marionette.rt.proxy.template.ImplementationMaybeProxied

data class MaybeProxiedTypeName(
    val unproxied: TypeName,
    val interfaceType: TypeName?,
    val implementationType: TypeName?
) {
    fun toInterface(imports: ImportResolver): Any {
        return interfaceType?.let { imports.add(it) } ?: imports.add(unproxied)
    }

    fun toImplementation(imports: ImportResolver): ImplementationMaybeProxiedTypeName {
        return if (interfaceType != null && implementationType != null) {
            ImplementationMaybeProxiedTypeName.Proxied(
                imports.add(interfaceType),
                imports.add(implementationType)
            )
        } else {
            ImplementationMaybeProxiedTypeName.Unproxied(imports.add(unproxied))
        }
    }

    fun toCallback(imports: ImportResolver): CallbackMaybeProxiedTypeName {
        return CallbackMaybeProxiedTypeName(imports.add(unproxied), implementationType?.let { imports.add(it) })
    }

    fun toConstructor(imports: ImportResolver): ConstructorMaybeProxiedTypeName {
        if (interfaceType == null || implementationType == null) {
            throw IllegalStateException("Cannot make a constructor for a non-proxied type")
        }
        return ConstructorMaybeProxiedTypeName(
            imports.add(unproxied),
            imports.add(interfaceType),
            imports.add(implementationType)
        )
    }
}

sealed class ImplementationMaybeProxiedTypeName {
    abstract fun toImplememtationMaybeProxied(imports: ImportResolver): ImplementationMaybeProxied

    data class Unproxied(val unproxiedKey: Any) : ImplementationMaybeProxiedTypeName() {
        override fun toImplememtationMaybeProxied(imports: ImportResolver): ImplementationMaybeProxied {
            return ImplementationMaybeProxied.ofUnproxied(imports[unproxiedKey])
        }
    }

    data class Proxied(val interfaceKey: Any, val implementationKey: Any) : ImplementationMaybeProxiedTypeName() {
        override fun toImplememtationMaybeProxied(imports: ImportResolver): ImplementationMaybeProxied {
            return ImplementationMaybeProxied.ofImplementation(imports[interfaceKey], imports[implementationKey])
        }
    }
}

data class CallbackMaybeProxiedTypeName(val unproxiedKey: Any, val implementationKey: Any?) {
    fun toCallbackMaybeProxied(imports: ImportResolver): CallbackMaybeProxied {
        return implementationKey?.let { CallbackMaybeProxied.ofImplementation(imports[unproxiedKey], imports[it]) }
            ?: CallbackMaybeProxied.ofUnproxied(imports[unproxiedKey])
    }
}

data class ConstructorMaybeProxiedTypeName(val unproxiedKey: Any, val interfaceKey: Any, val implementationKey: Any) {
    fun toConstructorMaybeProxied(imports: ImportResolver): ConstructorMaybeProxied {
        return ConstructorMaybeProxied(imports[unproxiedKey], imports[interfaceKey], imports[implementationKey])
    }
}
