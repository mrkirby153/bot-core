package com.mrkirby153.botcore.command.slashcommand.dsl.types

import com.mrkirby153.botcore.command.slashcommand.dsl.Argument
import com.mrkirby153.botcore.command.slashcommand.dsl.Arguments
import com.mrkirby153.botcore.command.slashcommand.dsl.NullableArgument
import net.dv8tion.jda.api.interactions.commands.OptionType

abstract class ArgBuilder<T : Any>(val type: OptionType) {
    lateinit var displayName: String
    lateinit var description: String

    abstract fun build(arguments: Arguments): Argument<T>
}

abstract class NullableArgBuilder<T>(val type: OptionType) {
    lateinit var displayName: String
    lateinit var description: String

    abstract fun build(arguments: Arguments): NullableArgument<T>
}