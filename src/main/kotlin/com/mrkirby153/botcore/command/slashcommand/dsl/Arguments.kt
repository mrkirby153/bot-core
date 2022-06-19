package com.mrkirby153.botcore.command.slashcommand.dsl

import com.mrkirby153.botcore.command.slashcommand.dsl.types.IArgBuilder

open class Arguments {

    private val arguments = mutableListOf<IArgument<*, out IArgBuilder<*>>>()

    fun addArgument(arg: IArgument<*, out IArgBuilder<*>>) = arguments.add(arg)

    fun get() = arguments.toList()

    fun get(key: String) = arguments.firstOrNull { it.displayName == key }
}