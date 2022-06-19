package com.mrkirby153.botcore.command.slashcommand.dsl

import com.mrkirby153.botcore.command.slashcommand.dsl.types.ArgBuilder
import com.mrkirby153.botcore.command.slashcommand.dsl.types.IArgBuilder
import com.mrkirby153.botcore.command.slashcommand.dsl.types.NullableArgBuilder
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import kotlin.reflect.KProperty

interface IArgument<T: Any, Builder : IArgBuilder<T>> {
    val type: OptionType
    val displayName: String
    val description: String
    val converter: ArgumentConverter<T>
    val builder: Builder

    fun parse(value: OptionMapping)
}

data class Argument<T : Any>(
    override val type: OptionType,
    override val displayName: String,
    override val description: String,
    override val converter: ArgumentConverter<T>,
    override val builder: ArgBuilder<T>
) : IArgument<T, ArgBuilder<T>>{

    private lateinit var parsed: T

    override fun parse(value: OptionMapping) {
        parsed = converter.convert(value)
    }

    operator fun getValue(args: Arguments, property: KProperty<*>): T {
        return parsed
    }
}

data class NullableArgument<T: Any>(
    override val type: OptionType,
    override val displayName: String,
    override val description: String,
    override val converter: ArgumentConverter<T>,
    override val builder: NullableArgBuilder<T>
) : IArgument<T, NullableArgBuilder<T>> {
    private var parsed: T? = null

    override fun parse(value: OptionMapping) {
        parsed = converter.convert(value)
    }

    operator fun getValue(args: Arguments, property: KProperty<*>): T? {
        return parsed
    }
}