package com.mrkirby153.botcore.command.slashcommand.dsl.types

import com.mrkirby153.botcore.command.slashcommand.dsl.ArgumentConverter
import com.mrkirby153.botcore.command.slashcommand.dsl.Arguments
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType

class BooleanConverter : ArgumentConverter<Boolean> {
    override fun convert(input: OptionMapping): Boolean {
        return input.asBoolean
    }
}

class BooleanArgument : GenericArgument<Boolean>(OptionType.BOOLEAN, ::BooleanConverter)

class OptionalBooleanArgument :
    GenericNullableArgument<Boolean>(OptionType.BOOLEAN, ::BooleanConverter)

fun Arguments.boolean(body: BooleanArgument.() -> Unit) = genericArgument(::BooleanArgument, body)

fun Arguments.optionalBoolean(body: OptionalBooleanArgument.() -> Unit) =
    optionalGenericArgument(::OptionalBooleanArgument, body)