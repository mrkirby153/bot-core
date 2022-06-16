package com.mrkirby153.botcore.command.slashcommand.dsl.types

import com.mrkirby153.botcore.command.slashcommand.dsl.Argument
import com.mrkirby153.botcore.command.slashcommand.dsl.Arguments
import com.mrkirby153.botcore.command.slashcommand.dsl.NullableArgument

abstract class ArgBuilder<T : Any> {
    lateinit var displayName: String
    lateinit var description: String

    abstract fun build(arguments: Arguments): Argument<T>
}

abstract class NullableArgBuilder<T> {
    lateinit var displayName: String
    lateinit var description: String

    abstract fun build(arguments: Arguments): NullableArgument<T>
}