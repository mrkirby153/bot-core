package com.mrkirby153.botcore.command.slashcommand.dsl

open class Arguments {
    private val arguments = mutableListOf<Argument<*>>()

    fun addArgument(arg: Argument<*>) = arguments.add(arg)

    fun get() = arguments.toList()
}