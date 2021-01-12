package com.kneelawk.marionette.gradle

import com.kneelawk.marionette.rt.mod.CallbackMaybeProxied
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
