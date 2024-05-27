package com.mrkirby153.botcore.spring.config

import com.mrkirby153.botcore.modal.ModalManager
import com.mrkirby153.botcore.spring.event.BotReadyEvent
import com.mrkirby153.botcore.utils.SLF4J
import net.dv8tion.jda.api.sharding.ShardManager
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener
import java.util.concurrent.TimeUnit

@Configuration
open class ModalManagerConfiguration(
    private val shardManager: ShardManager,
    @Value("\${bot.modal.gcPeriod:1}") private val time: Long,
    @Value("\${bot.modal.gcUnit:SECOND}") private val unit: TimeUnit,
) {

    private val log by SLF4J

    @Bean
    @ConditionalOnMissingBean
    open fun modalManager() = ModalManager(null, time, unit)

    @EventListener
    fun onReady(event: BotReadyEvent) {
        log.info("Registering modal manager")
        shardManager.addEventListener(modalManager())
    }
}