package com.mrkirby153.botcore.command.slashcommand.dsl

import com.mrkirby153.botcore.utils.PrerequisiteCheck

/**
 * A subclass of [PrerequisiteCheck] explicitly for slash command prerequisite checks
 */
@SlashDsl
class CommandPrerequisiteCheck<T : Arguments>(instance: SlashContext<T>) :
    PrerequisiteCheck<SlashContext<T>>(instance) {

    /**
     * The arguments of the command
     */
    val args: T
        get() = instance.args
}