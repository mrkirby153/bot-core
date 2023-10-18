package com.mrkirby153.botcore.command.slashcommand.dsl.types

import com.mrkirby153.botcore.command.slashcommand.dsl.AbstractSlashCommand
import com.mrkirby153.botcore.command.slashcommand.dsl.ArgumentConverter
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType

object BooleanConverter : ArgumentConverter<Boolean> {
    override fun convert(input: OptionMapping): Boolean {
        return input.asBoolean
    }

    override val type = OptionType.BOOLEAN
}

fun AbstractSlashCommand.boolean(
    name: String? = null,
    body: ArgumentBuilder<Boolean>.() -> Unit = {}
) = ArgumentBuilder(this, BooleanConverter).apply(body)
    .apply { if (name != null) this@apply.name = name }