package com.mrkirby153.botcore.command.slashcommand.dsl.types

import com.mrkirby153.botcore.command.slashcommand.dsl.ArgumentParseException
import com.mrkirby153.botcore.command.slashcommand.dsl.ArgumentConverter
import com.mrkirby153.botcore.command.slashcommand.dsl.Arguments
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import kotlin.math.max
import kotlin.math.min

abstract class NumberArgumentBuilder<T : Number>(inst: Arguments, converter: ArgumentConverter<T>) :
    ArgumentBuilder<T>(inst, converter) {
    abstract var min: T
    abstract var max: T
}

object DoubleConverter : ArgumentConverter<Double> {
    override fun convert(input: OptionMapping): Double {
        try {
            return input.asDouble
        } catch (e: NumberFormatException) {
            throw ArgumentParseException("The provided value could not be converted to a double")
        }
    }

    override val type = OptionType.NUMBER
}

object LongConverter : ArgumentConverter<Long> {
    override fun convert(input: OptionMapping): Long {
        try {
            return input.asLong
        } catch (e: NumberFormatException) {
            throw ArgumentParseException("The provided value could not be converted to a long")
        }
    }

    override val type = OptionType.INTEGER
}

object IntegerConverter : ArgumentConverter<Int> {
    override fun convert(input: OptionMapping): Int {
        try {
            return input.asInt
        } catch (e: ArithmeticException) {
            throw ArgumentParseException("The provided number is too large")
        }
    }

    override val type = OptionType.INTEGER
}


class DoubleArgumentBuilder(inst: Arguments) :
    NumberArgumentBuilder<Double>(inst, DoubleConverter) {
    override var min: Double = max(OptionData.MIN_NEGATIVE_NUMBER, Double.MIN_VALUE)
    override var max: Double = min(OptionData.MAX_POSITIVE_NUMBER, Double.MAX_VALUE)

    override fun createOption() = super.createOption().apply {
        setMaxValue(max)
        setMinValue(min)
    }
}

class LongArgumentBuilder(inst: Arguments) : NumberArgumentBuilder<Long>(inst, LongConverter) {
    override var min: Long = max(Long.MIN_VALUE, OptionData.MIN_NEGATIVE_NUMBER.toLong())
    override var max: Long = min(Long.MAX_VALUE, OptionData.MAX_POSITIVE_NUMBER.toLong())

    override fun createOption() = super.createOption().apply {
        setMaxValue(max)
        setMinValue(min)
    }
}

class IntegerArgumentBuilder(inst: Arguments) : NumberArgumentBuilder<Int>(inst, IntegerConverter) {
    override var min: Int = max(Int.MIN_VALUE, OptionData.MIN_NEGATIVE_NUMBER.toInt())
    override var max: Int = min(Int.MAX_VALUE, OptionData.MAX_POSITIVE_NUMBER.toInt())

    override fun createOption() = super.createOption().apply {
        setMaxValue(max.toLong())
        setMinValue(min.toLong())
    }
}

fun Arguments.double(body: DoubleArgumentBuilder.() -> Unit) =
    DoubleArgumentBuilder(this).apply(body)

fun Arguments.long(body: LongArgumentBuilder.() -> Unit) = LongArgumentBuilder(this).apply(body)

fun Arguments.int(body: IntegerArgumentBuilder.() -> Unit) =
    IntegerArgumentBuilder(this).apply(body)