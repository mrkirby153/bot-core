package com.mrkirby153.botcore.command.slashcommand.dsl.types

import com.mrkirby153.botcore.command.slashcommand.dsl.ArgumentConverter
import com.mrkirby153.botcore.command.slashcommand.dsl.Arguments
import net.dv8tion.jda.api.entities.IMentionable
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType

class MentionableConverter : ArgumentConverter<IMentionable> {
    override fun convert(input: OptionMapping): IMentionable {
        return input.asMentionable
    }
}

class MentionableArgument :
    GenericArgument<IMentionable>(OptionType.MENTIONABLE, ::MentionableConverter)

class OptionalMentionableArgument :
    GenericNullableArgument<IMentionable>(OptionType.MENTIONABLE, ::MentionableConverter)

fun Arguments.mentonable(body: MentionableArgument.() -> Unit) =
    genericArgument(::MentionableArgument, body)

fun Arguments.optionalMentionable(body: OptionalMentionableArgument.() -> Unit) =
    optionalGenericArgument(::OptionalMentionableArgument, body)