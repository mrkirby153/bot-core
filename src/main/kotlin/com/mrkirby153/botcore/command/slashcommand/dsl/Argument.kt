package com.mrkirby153.botcore.command.slashcommand.dsl

import com.mrkirby153.botcore.command.slashcommand.dsl.types.ArgBuilder
import com.mrkirby153.botcore.command.slashcommand.dsl.types.NullableArgBuilder
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import kotlin.reflect.KProperty

data class Argument<T : Any>(
    val type: OptionType,
    val displayName: String,
    val description: String,
    val converter: ArgumentConverter<T>,
    val builder: ArgBuilder<T>
) {

    private lateinit var parsed: T

    fun parse(value: OptionMapping) {
        parsed = converter.convert(value)
    }

    operator fun getValue(args: Arguments, property: KProperty<*>): T {
        return parsed
    }
}

data class NullableArgument<T: Any>(
    val type: OptionType,
    val displayName: String,
    val description: String,
    val converter: ArgumentConverter<T>,
    val builder: NullableArgBuilder<T>
) {
    private var parsed: T? = null

    fun parse(value: OptionMapping) {
        parsed = converter.convert(value)
    }

    operator fun getValue(args: Arguments, property: KProperty<*>): T? {
        return parsed
    }
}