package com.mrkirby153.botcore.command.slashcommand.dsl.types

import com.mrkirby153.botcore.command.slashcommand.dsl.ArgumentConverter
import com.mrkirby153.botcore.command.slashcommand.dsl.SlashCommand
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType


object StringConverter : ArgumentConverter<String> {
    override fun convert(input: OptionMapping): String = input.asString

    override val type = OptionType.STRING
}

fun SlashCommand.string(name: String? = null, body: ArgumentBuilder<String>.() -> Unit = {}) =
    ArgumentBuilder(this, StringConverter).apply { if (name != null) this@apply.name = name }
        .apply(body)