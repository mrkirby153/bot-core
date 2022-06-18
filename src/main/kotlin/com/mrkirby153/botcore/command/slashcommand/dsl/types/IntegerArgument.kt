package com.mrkirby153.botcore.command.slashcommand.dsl.types

import com.mrkirby153.botcore.command.slashcommand.dsl.ArgumentConverter
import com.mrkirby153.botcore.command.slashcommand.dsl.Arguments
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType

class IntegerConverter : ArgumentConverter<Int> {
    override fun convert(input: OptionMapping): Int {
        return input.asInt
    }
}

class IntegerArgument : GenericArgument<Int>(OptionType.INTEGER, ::IntegerConverter)
class OptionalIntegerArgument : GenericNullableArgument<Int>(OptionType.INTEGER, ::IntegerConverter)

fun Arguments.int(body: IntegerArgument.() -> Unit) = genericArgument(::IntegerArgument, body)
fun Arguments.optionalInt(body: OptionalIntegerArgument.() -> Unit) =
    optionalGenericArgument(::OptionalIntegerArgument, body)