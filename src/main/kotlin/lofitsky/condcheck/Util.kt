package lofitsky.condcheck

import kotlin.reflect.full.declaredMemberProperties


fun Any.getFieldsMap(): Map<String, Any>
    = this::class.declaredMemberProperties
    .associate { it.name to it.getter.call(this) }
    .mapValues { it.value ?: Any() }
