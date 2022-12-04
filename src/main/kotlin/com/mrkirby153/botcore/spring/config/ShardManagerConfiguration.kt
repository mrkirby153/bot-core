package com.mrkirby153.botcore.spring.config

import com.mrkirby153.botcore.coroutine.enableCoroutines
import com.mrkirby153.botcore.spring.EventHandler
import com.mrkirby153.botcore.spring.event.BotReadyEvent
import com.mrkirby153.botcore.utils.SLF4J
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder
import net.dv8tion.jda.api.sharding.ShardManager
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener
import java.util.regex.Pattern


private val rangePattern = Pattern.compile("(\\d+)\\.\\.(\\d+)")

/**
 * Configuration for automatically adding a [ShardManager] to the spring context
 */
@Configuration
open class ShardManagerConfiguration(
    /**
     * The token the bot will use
     */
    @param:Value("\${bot.token}") private val token: String,
    /**
     * If events should be relayed to the spring event handler
     */
    @param:Value("\${bot.event.relay:true}") private val relayEvents: Boolean,
    /**
     * The shard ids to start
     */
    @param:Value("\${bot.shards.shards:}") private val shards: Array<String>,
    /**
     * The total number of shards this bot is running
     */
    @param:Value("\${bot.shards.total:-1}") private val totalShards: Int,
    /**
     * Additional intents to enable
     */
    @param:Value("\${bot.intents:}") private val intents: Array<String>,
    /**
     * If coroutines should be enabled
     */
    @param:Value("\${bot.coroutines:false}") private val enableCoroutines: Boolean,

    private val eventPublisher: ApplicationEventPublisher
) {

    private val log by SLF4J(ShardManagerConfiguration::class.java.name)

    private var botReady = false
    private var appReady = false

    private val readyLock = Any()

    @Bean
    @ConditionalOnMissingBean
    open fun defaultShardManagerBuilder() = DefaultShardManagerBuilder.createDefault(token).also {
        val shards = getShards()
        val intents: List<GatewayIntent> = intents.mapNotNull { intent ->
            try {
                GatewayIntent.valueOf(intent.uppercase())
            } catch (e: IllegalArgumentException) {
                log.warn("Provided intent $intent was not valid, ignoring...")
                null
            }
        }
        log.info("Initializing DefaultShardManagerBuilder with the shards [${shards.joinToString(",")}] of $totalShards and enabling additional intents: $intents")
        if (enableCoroutines) {
            log.info("Enabling coroutine support")
            it.enableCoroutines()
        }
        it.setShardsTotal(totalShards)
        it.setShards(shards)
        it.enableIntents(intents)
    }

    @Bean
    @ConditionalOnMissingBean
    open fun shardManager(
        builder: DefaultShardManagerBuilder,
        eventHandler: EventHandler
    ): ShardManager {
        val manager = builder.build()
        manager.setStatus(OnlineStatus.IDLE)
        manager.setActivity(Activity.playing("Starting up..."))
        if (relayEvents) {
            log.info("Relaying events to the spring application context")
            manager.addEventListener(eventHandler)
        }
        manager.addEventListener(ShardReadyListener(manager))
        return manager
    }

    @Bean
    @ConditionalOnMissingBean
    open fun eventHandler() = EventHandler(eventPublisher)

    @EventListener
    fun onAppReady(event: ApplicationReadyEvent) {
        synchronized(readyLock) {
            appReady = true
            maybeDispatchReadyEvent()
        }
    }

    private fun handleShardReady(shardManager: ShardManager, listener: ShardReadyListener) {
        log.debug("Handling shard ready event")
        val totalShards = shardManager.shards.size
        val readyShards = shardManager.shards.stream()
            .filter { jda -> jda.status === JDA.Status.CONNECTED }.count() + 1
        if (readyShards >= totalShards) {
            log.info("All shards ready!")
            shardManager.setStatus(OnlineStatus.ONLINE)
            shardManager.setActivity(null)
            synchronized(readyLock) {
                botReady = true
                maybeDispatchReadyEvent()
            }
            shardManager.removeEventListener(listener)
        } else {
            log.info("{}/{} shards ready", readyShards, totalShards)
        }
    }

    private fun maybeDispatchReadyEvent() {
        if (botReady && appReady) {
            log.info("Bot is ready")
            eventPublisher.publishEvent(BotReadyEvent())
        } else {
            if (botReady) {
                log.debug("Deferring ready event: App is not ready")
            }
            if (appReady && !botReady) {
                log.debug("Deferring ready event: Bot is not ready")
            }
        }
    }


    private fun getShards(): List<Int> {
        if (shards.size == 1 && shards[0] == "auto") {
            return emptyList()
        }
        val shardIds = mutableListOf<Int>()
        shards.forEach { element ->
            val matcher = rangePattern.matcher(element)
            if (matcher.find()) {
                val start = matcher.group(1).toIntOrNull()
                    ?: throw IllegalArgumentException("Provided start ${matcher.group(1)} is not an integer")
                val end = matcher.group(2).toIntOrNull()
                    ?: throw IllegalArgumentException("Provided end ${matcher.group(2)} is not an integer")
                check(start < end) { "Provided range $element is not valid. Range must be strictly increasing" }
                for (i in start..end) {
                    shardIds.add(i)
                }
            } else {
                shardIds.add(
                    element.toIntOrNull()
                        ?: throw IllegalArgumentException("Provided shard id \"$element\" is not an integer or range")
                )
            }
        }
        return shardIds.toList()
    }

    inner class ShardReadyListener(private val shardManager: ShardManager) : ListenerAdapter() {

        override fun onReady(event: ReadyEvent) {
            log.info("Shard ${event.jda.shardInfo.shardId} has logged in as ${event.jda.selfUser.name}#${event.jda.selfUser.discriminator}")
            handleShardReady(shardManager, this)
        }
    }
}