package com.mrkirby153.botcore.command.slashcommand.dsl.types

import com.mrkirby153.botcore.command.slashcommand.dsl.Argument
import com.mrkirby153.botcore.command.slashcommand.dsl.ArgumentConverter
import com.mrkirby153.botcore.command.slashcommand.dsl.Arguments
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType


class StringConverter : ArgumentConverter<String> {
    override fun convert(input: OptionMapping): String = input.asString
}

class StringArgument : GenericArgument<String>(OptionType.STRING, ::StringConverter)

class OptionalStringArgument :
    GenericNullableArgument<String>(OptionType.STRING, ::StringConverter)


fun Arguments.string(body: StringArgument.() -> Unit): Argument<String> =
    genericArgument(::StringArgument, body)

fun Arguments.optionalString(body: OptionalStringArgument.() -> Unit) =
    optionalGenericArgument(::OptionalStringArgument, body)