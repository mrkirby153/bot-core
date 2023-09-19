package com.mrkirby153.botcore.spring.config

import com.mrkirby153.botcore.command.slashcommand.dsl.DslCommandExecutor
import com.mrkirby153.botcore.command.slashcommand.dsl.ProvidesSlashCommands
import com.mrkirby153.botcore.spring.event.BotReadyEvent
import com.mrkirby153.botcore.utils.SLF4J
import net.dv8tion.jda.api.sharding.ShardManager
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener

@Configuration
open class DslCommandExecutorAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    open fun dslCommandExecutor() = DslCommandExecutor()
}

@Configuration
open class DslCommandExecutorRegister(
    @Value("\${bot.commands.guilds:}") private val guilds: Array<String>,
    private val commandExecutor: DslCommandExecutor,
    private val commands: List<ProvidesSlashCommands>,
    private val shardManager: ShardManager
) {

    private val log: Logger by SLF4J

    @EventListener
    fun onReady(event: BotReadyEvent) {
        log.debug("Registering ${commands.size} slash commands")
        commands.forEach {
            try {
                log.trace("Registering slash commands in {}", it)
                it.registerSlashCommands(commandExecutor)
            } catch (e: Exception) {
                log.error("Error registering slash commands in $it", e)
            }
        }
        if (guilds.isNotEmpty()) {
            log.info("Registering slash commands in the following guilds: ${guilds.joinToString(",")}")
            commandExecutor.commit(shardManager, *guilds).thenAccept {
                log.info("Registered slash commands successfully")
            }
        } else {
            log.info("Registering slash commands globally")
            commandExecutor.commit(shardManager.shards.first()).thenAccept {
                log.info("Registered slash commands successfully")
            }

        }
    }
}