package com.mrkirby153.botcore.testbot

import com.mrkirby153.botcore.utils.SLF4J
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder


val log by SLF4J("Test Bot")

fun main() {
    val token = System.getenv("TOKEN")?.trim()
    requireNotNull(token) { "Token must be provided" }
    val shardManager = DefaultShardManagerBuilder.createDefault(token).build()

    // Wait for ready
    shardManager.shards.forEach { it.awaitReady() }
    log.info("All shards ready!")
}