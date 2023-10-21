package com.mrkirby153.botcore.command.slashcommand.dsl.types

import com.mrkirby153.botcore.command.slashcommand.dsl.AbstractSlashCommand
import com.mrkirby153.botcore.command.slashcommand.dsl.ArgumentConverter
import net.dv8tion.jda.api.entities.Message.Attachment
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType


object AttachmentConverter : ArgumentConverter<Attachment> {
    override fun convert(input: OptionMapping) = input.asAttachment

    override val type = OptionType.ATTACHMENT
}

fun AbstractSlashCommand.attachment(
    name: String? = null,
    body: SimpleArgumentBuilder<Attachment>.() -> Unit = {}
) = SimpleArgumentBuilder(this, AttachmentConverter).apply(body)
    .apply { if (name != null) this@apply.name = name }