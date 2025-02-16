package com.mrkirby153.botcore.testbot.command

import com.mrkirby153.botcore.builder.message
import com.mrkirby153.botcore.command.slashcommand.dsl.CommandException
import com.mrkirby153.botcore.command.slashcommand.dsl.DslCommandExecutor
import com.mrkirby153.botcore.command.slashcommand.dsl.ProvidesSlashCommands
import com.mrkirby153.botcore.command.slashcommand.dsl.slashCommand
import com.mrkirby153.botcore.command.slashcommand.dsl.subCommand
import com.mrkirby153.botcore.command.slashcommand.dsl.types.boolean
import com.mrkirby153.botcore.command.slashcommand.dsl.types.choices
import com.mrkirby153.botcore.command.slashcommand.dsl.types.string
import com.mrkirby153.botcore.confirm
import com.mrkirby153.botcore.coroutine.await
import com.mrkirby153.botcore.modal.ModalManager
import com.mrkirby153.botcore.modal.await
import com.mrkirby153.botcore.testbot.Emojis
import com.mrkirby153.botcore.testbot.wrappedString
import com.mrkirby153.botcore.utils.SLF4J
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle

class TestCommands(private val modalManager: ModalManager) : ProvidesSlashCommands {
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
            slashCommand("test-modal") {
                run {
                    val result = modalManager.await {
                        title = "This is a title"
                        textInput {
                            name = "Test One"
                        }
                        textInput {
                            name = "Test Two"
                            style = TextInputStyle.PARAGRAPH
                        }
                    }
                    result.reply(message {
                        text {
                            appendLine("Submitted Data")
                            result.data.forEach { (k, v) ->
                                appendBlock(k)
                                append(":")
                                appendLine("  $v")
                            }
                        }
                    }.create()).setEphemeral(true).await()
                }
            }

            slashCommand("test-confirm") {
                subCommand("normal") {
                    run {
                        val (hook, confirmed) = confirm(true) {
                            text {
                                appendLine("Are you sure you want to continue?")
                            }
                        }
                        if (confirmed) {
                            hook.sendMessage("Confirmed!").await()
                        } else {
                            hook.sendMessage("Failed!").await()
                        }
                    }
                }
                subCommand("defer") {
                    run {
                        defer {
                            delay(5000)
                            val (hook, confirmed) = it.confirm(user) {
                                text {
                                    appendLine("Are you sure you want to continue?")
                                }
                            }
                            if (confirmed) {
                                hook.editOriginal("Confirmed!").await()
                            } else {
                                hook.editOriginal("Failed!").await()
                            }
                        }
                    }
                }
            }

            slashCommand("emoji-test") {
                run {
                    reply("${Emojis.greenTick} This is a test of the emoji system!").await()
                }
            }
        }
    }
}