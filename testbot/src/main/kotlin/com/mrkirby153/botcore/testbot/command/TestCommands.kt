package com.mrkirby153.botcore.testbot.command

import com.mrkirby153.botcore.command.slashcommand.dsl.SlashCommandContainer
import com.mrkirby153.botcore.command.slashcommand.dsl.Arguments
import com.mrkirby153.botcore.command.slashcommand.dsl.DslCommandExecutor
import com.mrkirby153.botcore.command.slashcommand.dsl.types.string
import com.mrkirby153.botcore.coroutine.await
import com.mrkirby153.botcore.utils.SLF4J
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TestCommands : SlashCommandContainer() {

    private val log by SLF4J

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
            slashCommand {
                name = "context-test"
                description = "Context test"
                run {
                    val hook = deferReply().await()
                    val job1 = launch {
                        delay(1000)
                        log.info("Job 1 Done!")
                    }
                    val job2 = launch {
                        delay(2000)
                        log.info("Job 2 Done!")
                    }
                    job1.join()
                    job2.join()
                    hook.editOriginal("Done!").await()
                }
            }
        }
    }
}