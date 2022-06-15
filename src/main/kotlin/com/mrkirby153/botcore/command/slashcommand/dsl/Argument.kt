package com.mrkirby153.botcore.command.slashcommand.dsl

import kotlin.reflect.KProperty

data class Argument<T: Any>(
    val required: Boolean,
    val displayName: String,
    val description: String,
    val converter: ArgumentConverter<T>
) {
    lateinit var parentArgs: Arguments

    lateinit var parsed: T

    operator fun getValue(args: Arguments, property: KProperty<*>): T {
        return parsed
    }
}