package com.mrkirby153.botcore.command.slashcommand.dsl.types

import com.mrkirby153.botcore.command.slashcommand.dsl.Argument
import com.mrkirby153.botcore.command.slashcommand.dsl.ArgumentConverter
import com.mrkirby153.botcore.command.slashcommand.dsl.Arguments
import com.mrkirby153.botcore.command.slashcommand.dsl.NullableArgument
import net.dv8tion.jda.api.interactions.commands.OptionType

/**
 * A helper class for [ArgBuilder] that defines a generic argument
 *
 * @param type The [OptionType] of the argument
 * @param converter The converter to convert this argument into a JVM type
 * @param Type the JVM type of this argument
 */
open class GenericArgument<Type : Any>(
    type: OptionType,
    private val converter: () -> ArgumentConverter<Type>,
) : ArgBuilder<Type>(type), AutocompleteEligible {
    override var autocompleteFunction: AutoCompleteCallback? = null
    override fun build(arguments: Arguments): Argument<Type> {
        return Argument(type, displayName, description, converter(), this)
    }
}

/**
 * A helper class for [ArgBuilder] that defines a generic optional argument
 *
 * @param type The [OptionType] of the argument
 * @param converter The converter to convert this argument into a JVM type
 * @param Type The JVM type of this argument
 */
open class GenericNullableArgument<Type : Any>(
    type: OptionType,
    private val converter: () -> ArgumentConverter<Type>
) : NullableArgBuilder<Type>(type), AutocompleteEligible {
    override var autocompleteFunction: AutoCompleteCallback? = null
    override fun build(arguments: Arguments): NullableArgument<Type> {
        return NullableArgument(type, displayName, description, converter(), this)
    }
}

/**
 * Delegator for a generic argument.
 *
 * @param creator A function returning a new instance of an [ArgBuilder] for this argument
 * @param T The JVM type of this argument
 * @param Inst The [ArgBuilder] that will be constructed
 */
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

/**
 * Delegator for a generic optional argument
 *
 * @param creator A function returning an instance of an [ArgBuilder] for this argument
 * @param T The JVM type of this argument
 * @param Inst The [ArgBuilder] that will be constructed
 */
fun <T : Any, Inst : NullableArgBuilder<T>> Arguments.optionalGenericArgument(
    creator: () -> Inst,
    body: Inst.() -> Unit
): NullableArgument<T> {
    val builder = creator()
    body(builder)
    val built = builder.build(this)
    this.addArgument(built)
    return built
}