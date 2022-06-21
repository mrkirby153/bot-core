package com.mrkirby153.botcore.command.slashcommand.dsl.types

import com.mrkirby153.botcore.command.slashcommand.dsl.Argument
import com.mrkirby153.botcore.command.slashcommand.dsl.Arguments
import com.mrkirby153.botcore.command.slashcommand.dsl.NullableArgument
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData

/**
 * A top-level interface for arg builders
 */
interface IArgBuilder<T : Any> {
    /**
     * The display (user-facing) name of the argument
     */
    var displayName: String

    /**
     * The description of the argument
     */
    var description: String
}

/**
 * Interface for arguments to implement that apply additional customization to the [OptionData] for
 * this argument.
 *
 * Useful for enforcing choices or other validation
 */
interface ModifiesOption {

    /**
     * Modifies the provided [option]
     */
    fun modify(option: OptionData)
}

/**
 * An [IArgBuilder] for non-nullable (required) arguments
 */
abstract class ArgBuilder<T : Any>(val type: OptionType) : IArgBuilder<T> {
    override lateinit var displayName: String
    override lateinit var description: String

    abstract fun build(arguments: Arguments): Argument<T>
}

/**
 * An [IArgBuilder] for nullable (optional) arguments
 */
abstract class NullableArgBuilder<T : Any>(val type: OptionType) : IArgBuilder<T> {
    override lateinit var displayName: String
    override lateinit var description: String

    abstract fun build(arguments: Arguments): NullableArgument<T>
}