package com.mrkirby153.botcore.command.slashcommand.dsl

import kotlin.reflect.KProperty

data class Argument<T : Any>(
    val displayName: String,
    val description: String,
    val converter: ArgumentConverter<T>,
) {

    lateinit var parsed: T

    operator fun getValue(args: Arguments, property: KProperty<*>): T {
        return parsed
    }
}

data class NullableArgument<T>(
    val displayName: String,
    val description: String,
    val converter: ArgumentConverter<T>
) {
    var parsed: T? = null

    operator fun getValue(args: Arguments, property: KProperty<*>): T? {
        return parsed
    }
}