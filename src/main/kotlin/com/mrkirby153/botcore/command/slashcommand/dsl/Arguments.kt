package com.mrkirby153.botcore.command.slashcommand.dsl

open class Arguments {
    private val arguments = mutableListOf<Argument<*>>()
    private val nullableArguments = mutableListOf<NullableArgument<*>>()

    fun addArgument(arg: Argument<*>) = arguments.add(arg)

    fun addNullable(arg: NullableArgument<*>) = nullableArguments.add(arg)

    fun get() = arguments.toList()

    fun getNullable() = nullableArguments.toList()
}