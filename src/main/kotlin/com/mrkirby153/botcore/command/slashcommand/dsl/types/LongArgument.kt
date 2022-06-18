package com.mrkirby153.botcore.command.slashcommand.dsl.types

import com.mrkirby153.botcore.command.slashcommand.dsl.ArgumentConverter
import com.mrkirby153.botcore.command.slashcommand.dsl.Arguments
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType

class LongConverter : ArgumentConverter<Long> {
    override fun convert(input: OptionMapping): Long {
        return input.asLong
    }
}

class LongArgument : GenericArgument<Long>(OptionType.INTEGER, ::LongConverter)
class OptionalLongArgument : GenericNullableArgument<Long>(OptionType.INTEGER, ::LongConverter)

fun Arguments.long(body: LongArgument.() -> Unit) = genericArgument(::LongArgument, body)
fun Arguments.optionalLong(body: OptionalLongArgument.() -> Unit) =
    optionalGenericArgument(::OptionalLongArgument, body)