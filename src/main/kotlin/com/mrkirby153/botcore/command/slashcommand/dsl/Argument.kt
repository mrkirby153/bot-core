package com.mrkirby153.botcore.command.slashcommand.dsl

import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import kotlin.reflect.KProperty

data class Argument<T : Any>(
    val type: OptionType,
    val displayName: String,
    val description: String,
    val converter: ArgumentConverter<T>,
) {

    private lateinit var parsed: T

    fun parse(value: OptionMapping) {
        parsed = converter.convert(value)
    }

    operator fun getValue(args: Arguments, property: KProperty<*>): T {
        return parsed
    }
}

data class NullableArgument<T>(
    val type: OptionType,
    val displayName: String,
    val description: String,
    val converter: ArgumentConverter<T>
) {
    private var parsed: T? = null

    fun parse(value: OptionMapping) {
        parsed = converter.convert(value)
    }

    operator fun getValue(args: Arguments, property: KProperty<*>): T? {
        return parsed
    }
}