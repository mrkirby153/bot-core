package com.mrkirby153.botcore.command.slashcommand.dsl.types

import com.mrkirby153.botcore.command.slashcommand.dsl.ArgumentConverter
import com.mrkirby153.botcore.command.slashcommand.dsl.Arguments
import com.mrkirby153.botcore.command.slashcommand.dsl.SlashCommand
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType

object BooleanConverter : ArgumentConverter<Boolean> {
    override fun convert(input: OptionMapping): Boolean {
        return input.asBoolean
    }

    override val type = OptionType.BOOLEAN
}

fun SlashCommand.boolean(
    name: String? = null,
    body: ArgumentBuilder<Boolean>.() -> Unit = {}
) = ArgumentBuilder(this, BooleanConverter).apply(body)
    .apply { if (name != null) this@apply.name = name }