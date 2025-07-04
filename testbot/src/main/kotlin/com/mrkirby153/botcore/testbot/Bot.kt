package com.mrkirby153.botcore.testbot

import com.mrkirby153.botcore.ConfirmationHandler
import com.mrkirby153.botcore.command.slashcommand.dsl.DslCommandExecutor
import com.mrkirby153.botcore.coroutine.enableCoroutines
import com.mrkirby153.botcore.modal.ModalManager
import com.mrkirby153.botcore.testbot.command.TestCommands
import com.mrkirby153.botcore.utils.SLF4J
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder


val log by SLF4J("Test Bot")

fun main() {
    val token = System.getenv("TOKEN")?.trim()
    requireNotNull(token) { "Token must be provided" }
    val shardManager = DefaultShardManagerBuilder.createDefault(token).enableCoroutines().build()
    // Wait for ready
    shardManager.shards.forEach { it.awaitReady() }
    log.info("All shards ready!")

    runBlocking {
        Emojis.register(shardManager)
    }

    val dslCommandExecutor = DslCommandExecutor()
    shardManager.addEventListener(dslCommandExecutor.getListener())

    val modalManager = ModalManager()
    shardManager.addEventListener(modalManager)

    shardManager.addEventListener(ConfirmationHandler)

    val testCommands = TestCommands(modalManager)
    testCommands.registerSlashCommands(dslCommandExecutor)

    val guilds = (System.getenv("SLASH_COMMAND_GUILDS")?.trim() ?: "").split(",")
    require(guilds.isNotEmpty()) { "Slash command guilds not provided" }

    log.info("Committing slash commands to guilds: $guilds")
    dslCommandExecutor.commit(shardManager.shards[0], *guilds.toTypedArray()).thenRun {
        log.info("Slash commands committed")
    }
}
