package com.mrkirby153.botcore.command.slashcommand.dsl

import kotlin.reflect.KProperty

/**
 * A delegate for options
 */
class OptionDelegate<T> {

    operator fun getValue(inst: Any?, prop: KProperty<*>): Option<T> {
        TODO()
    }
}