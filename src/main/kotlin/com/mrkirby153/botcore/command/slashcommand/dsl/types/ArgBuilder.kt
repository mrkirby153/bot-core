package com.mrkirby153.botcore.command.slashcommand.dsl.types

import com.mrkirby153.botcore.command.slashcommand.dsl.Argument
import com.mrkirby153.botcore.command.slashcommand.dsl.Arguments

abstract class ArgBuilder<T : Any> {
    var required = true
    lateinit var displayName: String
    lateinit var description: String

    abstract fun build(arguments: Arguments): Argument<T>
}