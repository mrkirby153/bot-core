package com.mrkirby153.botcore.testbot

import com.mrkirby153.botcore.command.slashcommand.dsl.DslCommandExecutor
import com.mrkirby153.botcore.command.slashcommand.dsl.slashCommand
import com.mrkirby153.botcore.command.slashcommand.dsl.types.string
import com.mrkirby153.botcore.coroutine.await
import com.mrkirby153.botcore.coroutine.enableCoroutines
import com.mrkirby153.botcore.utils.SLF4J
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder


val log by SLF4J("Test Bot")

fun main() {
    val token = System.getenv("TOKEN")?.trim()
    requireNotNull(token) { "Token must be provided" }
    val shardManager = DefaultShardManagerBuilder.createDefault(token).enableCoroutines().build()

    // Wait for ready
    shardManager.shards.forEach { it.awaitReady() }
    log.info("All shards ready!")

    val dslCommandExecutor = DslCommandExecutor()
    shardManager.addEventListener(dslCommandExecutor.getListener())

    val command = slashCommand("ping") {
        description = "A cool description"
        run {
            reply {
                content = "Pong!"
            }.await()
        }
    }
    val commandWithArgs = slashCommand("testing") {
        val name by string().required()
        val optionalArg by string().optional()
        run {
            reply {
                content = "Hello, ${name()}"
            }.await()
        }
    }

    val guilds = (System.getenv("SLASH_COMMAND_GUILDS")?.trim() ?: "").split(",")
    require(guilds.isNotEmpty()) { "Slash command guilds not provided" }

    log.info("Committing slash commands to guilds: $guilds")
    dslCommandExecutor.commit(shardManager.shards[0], *guilds.toTypedArray()).thenRun {
        log.info("Slash commands committed")
    }
}
