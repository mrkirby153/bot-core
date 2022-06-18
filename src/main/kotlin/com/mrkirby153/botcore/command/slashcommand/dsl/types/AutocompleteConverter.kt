package com.mrkirby153.botcore.command.slashcommand.dsl.types

import com.mrkirby153.botcore.command.slashcommand.dsl.Argument
import com.mrkirby153.botcore.command.slashcommand.dsl.ArgumentConverter
import com.mrkirby153.botcore.command.slashcommand.dsl.Arguments
import com.mrkirby153.botcore.command.slashcommand.dsl.NullableArgument
import net.dv8tion.jda.api.interactions.commands.OptionType

open class GenericArgument<Converter : Any>(
    type: OptionType,
    private val converter: () -> ArgumentConverter<Converter>,
) : ArgBuilder<Converter>(type) {
    override fun build(arguments: Arguments): Argument<Converter> {
        return Argument(type, displayName, description, converter(), this)
    }
}

open class GenericNullableArgument<Converter : Any>(
    type: OptionType,
    private val converter: () -> ArgumentConverter<Converter>
) : NullableArgBuilder<Converter>(type) {
    override fun build(arguments: Arguments): NullableArgument<Converter> {
        return NullableArgument(type, displayName, description, converter(), this)
    }
}

open class GenericAutocompleteArgument<Converter : Any>(
    type: OptionType,
    converter: () -> ArgumentConverter<Converter>
) : GenericArgument<Converter>(type, converter),
    AutocompleteEligible {
    override var autocompleteFunction: AutoCompleteCallback? = null
}

open class GenericNullableAutocompleteArgument<Converter : Any>(
    type: OptionType,
    converter: () -> ArgumentConverter<Converter>
) : GenericNullableArgument<Converter>(type, converter),
    AutocompleteEligible {
    override var autocompleteFunction: AutoCompleteCallback? = null
}

fun <T : Any, Inst : ArgBuilder<T>> Arguments.genericArgument(
    creator: () -> Inst,
    body: Inst.() -> Unit
): Argument<T> {
    val builder = creator()
    body(builder)
    val built = builder.build(this)
    this.addArgument(built)
    return built
}

fun <T : Any, Inst : NullableArgBuilder<T>> Arguments.optionalGenericArgument(
    creator: () -> Inst,
    body: Inst.() -> Unit
): NullableArgument<T> {
    val builder = creator()
    body(builder)
    val built = builder.build(this)
    this.addNullable(built)
    return built
}