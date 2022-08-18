package com.mrkirby153.botcore.command.slashcommand.dsl.types

import com.mrkirby153.botcore.command.slashcommand.dsl.ArgumentConverter
import com.mrkirby153.botcore.command.slashcommand.dsl.Arguments
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType


object StringConverter : ArgumentConverter<String> {
    override fun convert(input: OptionMapping): String = input.asString

    override val type = OptionType.STRING
}

fun Arguments.string(body: ArgumentBuilder<String>.() -> Unit) =
    ArgumentBuilder(this, StringConverter).apply(body)