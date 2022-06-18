package com.mrkirby153.botcore.command.slashcommand.dsl.types

import com.mrkirby153.botcore.command.slashcommand.dsl.Argument
import com.mrkirby153.botcore.command.slashcommand.dsl.ArgumentConverter
import com.mrkirby153.botcore.command.slashcommand.dsl.Arguments
import com.mrkirby153.botcore.command.slashcommand.dsl.NullableArgument
import net.dv8tion.jda.api.interactions.commands.OptionType

open class GenericArgument<Type : Any>(
    type: OptionType,
    private val converter: () -> ArgumentConverter<Type>,
) : ArgBuilder<Type>(type), AutocompleteEligible{
    override var autocompleteFunction: AutoCompleteCallback? = null
    override fun build(arguments: Arguments): Argument<Type> {
        return Argument(type, displayName, description, converter(), this)
    }
}

open class GenericNullableArgument<Type : Any>(
    type: OptionType,
    private val converter: () -> ArgumentConverter<Type>
) : NullableArgBuilder<Type>(type), AutocompleteEligible {
    override var autocompleteFunction: AutoCompleteCallback? = null
    override fun build(arguments: Arguments): NullableArgument<Type> {
        return NullableArgument(type, displayName, description, converter(), this)
    }
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