package com.mrkirby153.botcore.command.slashcommand.dsl.types

import com.mrkirby153.botcore.command.slashcommand.dsl.ArgumentConverter
import com.mrkirby153.botcore.command.slashcommand.dsl.Arguments
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import kotlin.math.max
import kotlin.math.min

class LongConverter : ArgumentConverter<Long> {
    override fun convert(input: OptionMapping): Long {
        return input.asLong
    }
}

class LongArgument : GenericArgument<Long>(OptionType.INTEGER, ::LongConverter), HasMinAndMax<Long> {
    override var min: Long? = max(Long.MIN_VALUE, OptionData.MIN_NEGATIVE_NUMBER.toLong())
    override var max: Long? = min(Long.MAX_VALUE, OptionData.MAX_POSITIVE_NUMBER.toLong())
}
class OptionalLongArgument : GenericNullableArgument<Long>(OptionType.INTEGER, ::LongConverter), HasMinAndMax<Long> {
    override var min: Long? = max(Long.MIN_VALUE, OptionData.MIN_NEGATIVE_NUMBER.toLong())
    override var max: Long? = min(Long.MAX_VALUE, OptionData.MAX_POSITIVE_NUMBER.toLong())
}

fun Arguments.long(body: LongArgument.() -> Unit) = genericArgument(::LongArgument, body)
fun Arguments.optionalLong(body: OptionalLongArgument.() -> Unit) =
    optionalGenericArgument(::OptionalLongArgument, body)