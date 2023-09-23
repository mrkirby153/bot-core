package com.mrkirby153.botcore.command.slashcommand.dsl.types

import com.mrkirby153.botcore.command.slashcommand.dsl.ArgumentConverter
import com.mrkirby153.botcore.command.slashcommand.dsl.Arguments
import net.dv8tion.jda.api.entities.Message.Attachment
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType


object AttachmentConverter : ArgumentConverter<Attachment> {
    override fun convert(input: OptionMapping) = input.asAttachment

    override val type = OptionType.ATTACHMENT
}

//fun Arguments.attachment(body: ArgumentBuilder<Attachment>.() -> Unit) =
//    ArgumentBuilder(this, AttachmentConverter).apply(body)