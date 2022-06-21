package com.mrkirby153.botcore.command.slashcommand.dsl

import com.mrkirby153.botcore.command.args.ArgumentParseException
import com.mrkirby153.botcore.command.slashcommand.dsl.types.ArgBuilder
import com.mrkirby153.botcore.command.slashcommand.dsl.types.IArgBuilder
import com.mrkirby153.botcore.command.slashcommand.dsl.types.NullableArgBuilder
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import kotlin.reflect.KProperty

/**
 * The top level interface for arguments (Nullable and non-nullable)
 */
interface IArgument<T : Any, Builder : IArgBuilder<T>> {
    /**
     * The type of the argument
     */
    val type: OptionType

    /**
     * The display name of the argument, as shown to the user
     */
    val displayName: String

    /**
     * The description of the argument to show to the user
     */
    val description: String

    /**
     * The converter used by [parse] to convert the argument into the correct JVM object
     */
    val converter: ArgumentConverter<T>

    /**
     * The builder that declared this argument. Holds additional information about the argument
     *
     * @see [IArgBuilder]
     */
    val builder: Builder

    /**
     * Parses the provided [value] into the argument's JVM type.
     *
     * @throws ArgumentParseException If there was an error parsing the argument
     */
    fun parse(value: OptionMapping)
}

/**
 * Data class for a built non-nullable argument
 */
data class Argument<T : Any>(
    override val type: OptionType,
    override val displayName: String,
    override val description: String,
    override val converter: ArgumentConverter<T>,
    override val builder: ArgBuilder<T>
) : IArgument<T, ArgBuilder<T>> {

    private lateinit var parsed: T

    override fun parse(value: OptionMapping) {
        parsed = converter.convert(value)
    }

    operator fun getValue(args: Arguments, property: KProperty<*>): T {
        return parsed
    }

    override fun toString(): String {
        return parsed.toString()
    }
}

/**
 * Data class for a nullable argument
 */
data class NullableArgument<T : Any>(
    override val type: OptionType,
    override val displayName: String,
    override val description: String,
    override val converter: ArgumentConverter<T>,
    override val builder: NullableArgBuilder<T>
) : IArgument<T, NullableArgBuilder<T>> {
    private var parsed: T? = null

    override fun parse(value: OptionMapping) {
        parsed = converter.convert(value)
    }

    operator fun getValue(args: Arguments, property: KProperty<*>): T? {
        return parsed
    }

    override fun toString(): String {
        return parsed?.toString() ?: "null"
    }
}