package com.mrkirby153.botcore.command.slashcommand.dsl.types

import com.mrkirby153.botcore.command.slashcommand.dsl.ArgumentConverter
import com.mrkirby153.botcore.command.slashcommand.dsl.Arguments
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType

class DoubleConverter : ArgumentConverter<Double> {
    override fun convert(input: OptionMapping): Double {
        return input.asDouble
    }
}

class DoubleArgument : GenericArgument<Double>(OptionType.NUMBER, ::DoubleConverter)
class OptionalDoubleArgument : GenericNullableArgument<Double>(OptionType.NUMBER, ::DoubleConverter)

fun Arguments.double(body: DoubleArgument.() -> Unit) = genericArgument(::DoubleArgument, body)
fun Arguments.optionalDouble(body: OptionalDoubleArgument.() -> Unit) =
    optionalGenericArgument(::OptionalDoubleArgument, body)