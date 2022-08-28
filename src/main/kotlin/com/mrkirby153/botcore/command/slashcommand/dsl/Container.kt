package com.mrkirby153.botcore.command.slashcommand.dsl

import com.mrkirby153.botcore.command.slashcommand.dsl.types.ArgumentBuilder

/**
 * A data class for storing arguments
 */
data class ArgumentContainer<Type : Any?, ConverterType : Type>(
    internal val converter: ArgumentConverter<ConverterType>,
    val builder: ArgumentBuilder<*>,
    val required: Boolean
)