package com.mrkirby153.botcore

import com.mrkirby153.botcore.shard.ShardManager

@Deprecated("Use JDA's shard manager")
open class Bot(private val token: String, val shards: Int? = null) {

    private var ready = false

    lateinit var shardManager: ShardManager

    var startupTime: Long = 0


    fun connect() {
        LOGGER.info("Starting up")
        startupTime = System.currentTimeMillis()
        shardManager = ShardManager(token, shards ?: ShardManager.getReccomendedShardsAmount(token))
        shardManager.startAllShards()

        LOGGER.info("Startup complete!")
        ready = true
    }
}