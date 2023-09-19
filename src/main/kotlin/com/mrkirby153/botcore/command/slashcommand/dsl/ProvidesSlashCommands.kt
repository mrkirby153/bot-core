package com.mrkirby153.botcore.command.slashcommand.dsl


/**
 * Interface indicating that this class provides slash commands that can be executed
 * by the [DslCommandExecutor]
 */
interface ProvidesSlashCommands {

    /**
     * Registers all the slash commands for the given [executor]
     */
    fun registerSlashCommands(executor: DslCommandExecutor)
}