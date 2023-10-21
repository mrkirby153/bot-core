package com.mrkirby153.botcore.command.slashcommand.dsl

import com.mrkirby153.botcore.command.slashcommand.dsl.types.ArgumentBuilder
import kotlin.reflect.KProperty

/**
 * A data class for storing arguments
 */
class ArgumentContainer<Type : Any?, ConverterType>(
    private val inst: AbstractSlashCommand,
    internal val converter: ArgumentConverter<ConverterType>,
    val builder: ArgumentBuilder<*, *>,
    val required: Boolean,
    val default: Type? = null
) {
    operator fun provideDelegate(ref: Any?, prop: KProperty<*>): OptionDelegate<Type> {
        val name = (builder.name ?: prop.name).lowercase()
        builder.name = name // Once we know the name, update the builder
        inst.addArgument(name, this)
        return OptionDelegate(name)
    }
}