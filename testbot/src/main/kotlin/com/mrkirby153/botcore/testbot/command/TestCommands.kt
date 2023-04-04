package com.mrkirby153.botcore.testbot.command

import com.mrkirby153.botcore.command.slashcommand.SlashCommandContainer
import com.mrkirby153.botcore.command.slashcommand.dsl.Arguments
import com.mrkirby153.botcore.command.slashcommand.dsl.DslCommandExecutor
import com.mrkirby153.botcore.command.slashcommand.dsl.types.string
import com.mrkirby153.botcore.coroutine.await

class TestCommands : SlashCommandContainer() {

    class TestArguments : Arguments() {
        val arg1 by string {
            name = "arg1"
            description = "Argument 1"
        }.required()
    }

    fun register(executor: DslCommandExecutor) {
        executor.registerCommands {
            slashCommand<TestArguments> {
                name = "testing"
                description = "Test command"
                run {
                    reply("Test Command Executed!: ${args.arg1}").setEphemeral(true).await()
                }
            }
        }
    }
}