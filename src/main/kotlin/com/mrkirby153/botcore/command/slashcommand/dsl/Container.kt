package com.mrkirby153.botcore.command.slashcommand.dsl

import com.mrkirby153.botcore.command.slashcommand.dsl.types.ArgumentBuilder
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import kotlin.reflect.KProperty

/**
 * An abstract argument container serving as the base for [RequiredArgumentContainer] and [OptionalArgumentContainer]
 */
abstract class ArgumentContainer<Type : Any?, ConverterType : Type>(
    private val converter: ArgumentConverter<ConverterType>,
    val builder: ArgumentBuilder<*>
) {
    abstract var parsed: Type

    val required: Boolean
        get() = this is RequiredArgumentContainer<*>

    /**
     * Parse the provided [mapping] into [parsed]
     */
    fun doConversion(mapping: OptionMapping) {
        parsed = converter.convert(mapping)
    }

    operator fun getValue(thisRef: Arguments, property: KProperty<*>) = parsed
}

/**
 * An [ArgumentContainer] holding a required argument
 */
class RequiredArgumentContainer<T : Any>(
    converter: ArgumentConverter<T>,
    builder: ArgumentBuilder<*>
) :
    ArgumentContainer<T, T>(converter, builder) {
    override lateinit var parsed: T
}

/**
 * An [ArgumentContainer] holding an optional argument
 */
class OptionalArgumentContainer<T>(converter: ArgumentConverter<T>, builder: ArgumentBuilder<*>) :
    ArgumentContainer<T?, T>(converter, builder) {
    override var parsed: T? = null
}