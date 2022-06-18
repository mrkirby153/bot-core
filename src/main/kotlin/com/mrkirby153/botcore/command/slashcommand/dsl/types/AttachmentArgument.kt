package com.mrkirby153.botcore.command.slashcommand.dsl.types

import com.mrkirby153.botcore.command.slashcommand.dsl.ArgumentConverter
import com.mrkirby153.botcore.command.slashcommand.dsl.Arguments
import net.dv8tion.jda.api.entities.Message.Attachment
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType


class AttachmentConverter : ArgumentConverter<Attachment> {
    override fun convert(input: OptionMapping) = input.asAttachment
}

class AttachmentArgument : GenericArgument<Attachment>(OptionType.ATTACHMENT, ::AttachmentConverter)

class OptionalAttachment : GenericNullableArgument<Attachment>(OptionType.ATTACHMENT, ::AttachmentConverter)

fun Arguments.attachment(body: AttachmentArgument.() -> Unit) = genericArgument(::AttachmentArgument, body)

fun Arguments.optionalAttachment(body: OptionalAttachment.() -> Unit) = optionalGenericArgument(::OptionalAttachment, body)