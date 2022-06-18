package com.mrkirby153.botcore.command.slashcommand.dsl.types

import com.mrkirby153.botcore.command.args.ArgumentParseException
import com.mrkirby153.botcore.command.slashcommand.dsl.ArgumentConverter
import com.mrkirby153.botcore.command.slashcommand.dsl.Arguments
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import kotlin.math.max
import kotlin.math.min

class DoubleConverter : ArgumentConverter<Double> {
    override fun convert(input: OptionMapping): Double {
        try {
            return input.asDouble
        } catch (e: NumberFormatException) {
            throw ArgumentParseException("The provided value could not be converted to a double")
        }
    }
}

class DoubleArgument : GenericArgument<Double>(OptionType.NUMBER, ::DoubleConverter),
    HasMinAndMax<Double> {
    override var min: Double? = max(OptionData.MIN_NEGATIVE_NUMBER, Double.MIN_VALUE)
    override var max: Double? = min(OptionData.MAX_POSITIVE_NUMBER, Double.MAX_VALUE)
}

class OptionalDoubleArgument :
    GenericNullableArgument<Double>(OptionType.NUMBER, ::DoubleConverter), HasMinAndMax<Double> {
    override var min: Double? = max(OptionData.MIN_NEGATIVE_NUMBER, Double.MIN_VALUE)
    override var max: Double? = min(OptionData.MAX_POSITIVE_NUMBER, Double.MAX_VALUE)
}

fun Arguments.double(body: DoubleArgument.() -> Unit) = genericArgument(::DoubleArgument, body)
fun Arguments.optionalDouble(body: OptionalDoubleArgument.() -> Unit) =
    optionalGenericArgument(::OptionalDoubleArgument, body)