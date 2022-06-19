package com.mrkirby153.botcore.command.slashcommand.dsl.types

import com.mrkirby153.botcore.command.slashcommand.dsl.Argument
import com.mrkirby153.botcore.command.slashcommand.dsl.Arguments
import com.mrkirby153.botcore.command.slashcommand.dsl.NullableArgument
import net.dv8tion.jda.api.interactions.commands.OptionType

interface IArgBuilder<T: Any> {
    var displayName: String
    var description: String
}

abstract class ArgBuilder<T : Any>(val type: OptionType) : IArgBuilder<T> {
    override lateinit var displayName: String
    override lateinit var description: String

    abstract fun build(arguments: Arguments): Argument<T>
}

abstract class NullableArgBuilder<T : Any>(val type: OptionType) : IArgBuilder<T> {
    override lateinit var displayName: String
    override lateinit var description: String

    abstract fun build(arguments: Arguments): NullableArgument<T>
}