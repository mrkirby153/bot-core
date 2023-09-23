package com.mrkirby153.botcore.command.slashcommand.dsl

import kotlin.reflect.KProperty

/**
 * A delegate for options
 */
class OptionDelegate<T>(
    private val name: String
) {

    operator fun getValue(inst: Any?, prop: KProperty<*>): Option<T> {
        return Option(name)
    }
}

data class Option<T>(
    internal val name: String
)