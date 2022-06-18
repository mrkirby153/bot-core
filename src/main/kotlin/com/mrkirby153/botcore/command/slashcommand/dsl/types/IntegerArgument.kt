package com.mrkirby153.botcore.command.slashcommand.dsl.types

import com.mrkirby153.botcore.command.args.ArgumentParseException
import com.mrkirby153.botcore.command.slashcommand.dsl.ArgumentConverter
import com.mrkirby153.botcore.command.slashcommand.dsl.Arguments
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import kotlin.math.max
import kotlin.math.min

class IntegerConverter : ArgumentConverter<Int> {
    override fun convert(input: OptionMapping): Int {
        try {
            return input.asInt
        } catch (e: ArithmeticException) {
            throw ArgumentParseException("The provided number is too large")
        }
    }
}

class IntegerArgument : GenericArgument<Int>(OptionType.INTEGER, ::IntegerConverter),
    HasMinAndMax<Int> {
    override var min: Int? = max(Int.MIN_VALUE, OptionData.MIN_NEGATIVE_NUMBER.toInt())
    override var max: Int? = min(Int.MAX_VALUE, OptionData.MAX_POSITIVE_NUMBER.toInt())
}

class OptionalIntegerArgument :
    GenericNullableArgument<Int>(OptionType.INTEGER, ::IntegerConverter), HasMinAndMax<Int> {
    override var min: Int? = max(Int.MIN_VALUE, OptionData.MIN_NEGATIVE_NUMBER.toInt())
    override var max: Int? = min(Int.MAX_VALUE, OptionData.MAX_POSITIVE_NUMBER.toInt())
}

fun Arguments.int(body: IntegerArgument.() -> Unit) = genericArgument(::IntegerArgument, body)
fun Arguments.optionalInt(body: OptionalIntegerArgument.() -> Unit) =
    optionalGenericArgument(::OptionalIntegerArgument, body)