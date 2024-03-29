package com.mrkirby153.botcore.testbot.command

import com.mrkirby153.botcore.command.slashcommand.dsl.CommandException
import com.mrkirby153.botcore.command.slashcommand.dsl.DslCommandExecutor
import com.mrkirby153.botcore.command.slashcommand.dsl.ProvidesSlashCommands
import com.mrkirby153.botcore.command.slashcommand.dsl.slashCommand
import com.mrkirby153.botcore.command.slashcommand.dsl.types.boolean
import com.mrkirby153.botcore.command.slashcommand.dsl.types.choices
import com.mrkirby153.botcore.command.slashcommand.dsl.types.string
import com.mrkirby153.botcore.testbot.wrappedString
import com.mrkirby153.botcore.coroutine.await
import com.mrkirby153.botcore.utils.SLF4J
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TestCommands : ProvidesSlashCommands {
    private val log by SLF4J

    override fun registerSlashCommands(executor: DslCommandExecutor) {
        executor.registerCommands {
            slashCommand("testing") {
                description = "Test command"

                val arg1 by string { description = "Argument 1" }.required()

                run {
                    reply("Test command Executed!: ${arg1()}").await()
                }
            }
            slashCommand("test2") {
                description = "Test command with wrapped data"
                val arg1 by wrappedString { description = "Foo" }.required()
                run {
                    reply("Given ${arg1()}").await()
                }
            }
            slashCommand("context-test") {
                description = "Context test"
                run {
                    val hook = deferReply().await()
                    val job1 = launch {
                        delay(1000)
                        log.info("Job 1 done!")
                    }
                    val job2 = launch {
                        delay(2000)
                        log.info("Job 2 done!")
                    }
                    job1.join()
                    job2.join()
                    hook.editOriginal("Done!").await()
                }
            }
            slashCommand("choices") {
                description = "Choices test"
                val arg1 by choices(choices = listOf("one", "two", "three")) {
                    description = "Argument 1"
                }.required()
                val arg2 by choices(choiceProvider = {
                    val str = it.focusedOption.value
                    if (str.isNotEmpty()) return@choices listOf(str.reversed() to str.reversed())
                    else return@choices emptyList()
                }).optional("testing")

                run {
                    reply("You provided ${arg1()} and ${arg2()}").await()
                }
            }
            slashCommand("deferred-error") {
                val commandException by boolean("command_exception").optional(false)
                run {
                    deferReply(true).await()
                    if (commandException()) {
                        throw CommandException("Deferred command exception")
                    } else {
                        error("Deferred error!")
                    }
                }
            }
        }
    }
}