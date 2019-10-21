package com.mrkirby153.botcore.shard

import com.mrkirby153.botcore.LOGGER
import net.dv8tion.jda.api.AccountType
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.json.JSONObject
import org.json.JSONTokener
import java.net.HttpURLConnection
import java.net.URL

/**
 * Class to handle sharding of bots
 *
 * @param token The bot's token
 * @param numShards The number of shards to start
 */
class ShardManager(private val token: String, val numShards: Int = 1) {

    /**
     * A list of all the shards
     */
    val shards = mutableListOf<Shard>()

    /**
     * A list of shards that are starting
     */
    val startingShards = mutableListOf<Int>()

    /**
     * If the shard manager is loading shards
     */
    val loadingShards: Boolean
        get() = startingShards.size > 0

    private var lastStartTime = -1L

    private var startupThread: Thread? = null

    private val eventListeners = mutableListOf<Any>()

    /**
     * Adds a shard to the pool.
     *
     * If it's been less than 5 seconds since the last shard was started, it will wait 5 seconds as per the spec
     *
     * @param id The id of the shard
     * @param async If the shard should be started async
     */
    @JvmOverloads
    fun addShard(id: Int, async: Boolean = true) {
        if (id > numShards) {
            throw IllegalArgumentException("Cannot start more than the allocated amount of shards")
        }
        if (numShards > 1 && lastStartTime != -1L && lastStartTime + 5000 < System.currentTimeMillis()) {
            LOGGER.info("Waiting 5 seconds before starting the next shard")
            Thread.sleep(5000)
        }
        startingShards.add(id)
        LOGGER.info("Starting shard $id")
        val jda = buildJDA(id, async)
        shards.add(Shard(jda, id))
        lastStartTime = System.currentTimeMillis()
    }

    /**
     * Registers an event listener across all the shards
     *
     * @param eventListener The event listener to add
     */
    fun addListener(eventListener: Any) {
        LOGGER.debug("Registering ${eventListener.javaClass} across ${shards.size} shards")
        eventListeners.add(eventListener)
        shards.forEach { it.addEventListener(eventListener) }
    }

    /**
     * Removes an event listener across all shards
     *
     * @param eventListener The event listener to remove
     */
    fun removeListener(eventListener: Any) {
        LOGGER.debug("Removing ${eventListener.javaClass} across ${shards.size} shards")
        eventListeners.add(eventListener)
        shards.forEach { it.removeEventListener(eventListener) }
    }

    /**
     * Gets a shard by its id
     *
     * @param id The id of the shard
     *
     * @throws IllegalArgumentException If the id passed is greater than the number of shards
     * @throws NoSuchElementException If the shard was not found
     */
    fun getShard(id: Int): Shard {
        if (id > numShards) {
            throw IllegalArgumentException(
                    "Cannot get a shard with id greater than the number of shards")
        }
        return shards.first { it.shardId == id }
    }

    /**
     * Gets the user by their id
     *
     * @param id The user id
     *
     * @return The user or null if it wasn't found
     */
    fun getUserById(id: String): User? {
        shards.forEach { shard ->
            val user = shard.getUserById(id)
            if (user != null)
                return user
        }
        return null
    }

    /**
     * Gets a guild by its id
     *
     * @return The guild or null if it wasn't found
     */
    fun getGuild(id: String): Guild? {
        shards.forEach { shard ->
            val guild = shard.getGuildById(id)
            if (guild != null)
                return guild
        }
        return null
    }

    /**
     * Gets a [Guild]'s shard
     *
     * @param guild The guild
     *
     * @return The [Shard] or null if it wasn't found
     */
    fun getShard(guild: Guild): Shard? = getShard(guild.id)

    /**
     * Gets a [Guild]'s shard
     *
     * @param guildId The guild id
     *
     * @return The [Shard] or null if it wasn't found
     */
    fun getShard(guildId: String): Shard? {
        shards.forEach { shard ->
            val guild = shard.getGuildById(guildId)
            if (guild != null)
                return shard
        }
        return null
    }

    /**
     * Builds a JDA instance
     *
     * @id The shard id of the instance
     */
    private fun buildJDA(id: Int, async: Boolean = true): JDA {
        val builder = JDABuilder(AccountType.BOT).apply {
            setToken(this@ShardManager.token)
            setAutoReconnect(true)
            setBulkDeleteSplittingEnabled(false)
            if (this@ShardManager.numShards > 1)
                useSharding(id, numShards)
            addListener(object : ListenerAdapter() {
                override fun onReady(event: ReadyEvent) {
                    LOGGER.debug("Shard $id is ready")
                    startingShards.remove(id)
                    event.jda.removeEventListener(this)
                    LOGGER.debug("Registering ${eventListeners.size} event listeners to shard $id")
                    event.jda.addEventListener(*eventListeners.toTypedArray())
                }
            })
        }
        return if (async) {
            builder.build()
        } else {
            builder.build().awaitReady()
        }
    }

    /**
     * Shuts down the shard
     *
     * @param id The shard to restart
     */
    @JvmOverloads
    fun shutdown(id: Int = 1) {
        if (id > numShards)
            throw IllegalArgumentException(
                    "Cannot shutdown a shard id greater than the max number of shards")
        LOGGER.debug("Shard $id is shutting down")
        val shard = getShard(id)
        shard.shutdown()
        shards.remove(shard)
        LOGGER.debug("Shard $id shut down")
    }

    /**
     * Shuts down all shards
     */
    fun shutdownAll() {
        LOGGER.debug("Shutting down all shards")
        shards.forEach {
            it.shutdown()
        }
        shards.clear()
    }

    /**
     * Restarts a shard
     */
    @JvmOverloads
    fun restart(id: Int = 1, async: Boolean = true) {
        if (id > numShards)
            throw IllegalArgumentException(
                    "Cannot restart a shard id greater than the max number of shards")
        LOGGER.debug("Shard $id is restarting")
        shutdown(id)
        addShard(id, async)
        LOGGER.debug("Shard $id has restarted")
    }

    /**
     * Starts all shards async
     */
    @JvmOverloads
    fun startAllShards(async: Boolean = true) {
        if (async) {
            startupThread = Thread {
                LOGGER.debug("Starting all shards Async")
                for (i in 0 until numShards) {
                    addShard(i, async)
                    Thread.sleep(5500)
                }
                LOGGER.debug("All shards started! Terminating thread")
                startupThread = null
            }
            startupThread?.apply {
                name = "ShardManager-StartupThread"
                isDaemon = false
            }
            startupThread?.start()
        } else {
            LOGGER.debug("Starting all shards Sync")
            for (i in 0 until numShards) {
                addShard(i, async)
                if (numShards > 1)
                    Thread.sleep(5500)
            }
            LOGGER.debug("All shards started!")
        }
    }

    companion object {
        @JvmStatic
        fun getReccomendedShardsAmount(token: String): Int {
            LOGGER.debug("Automatically determining the number of shards to use")
            val url = URL("https://discordapp.com/api/v6/gateway/bot")
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.setRequestProperty("Authorization", "Bot $token")
            connection.setRequestProperty("User-Agent",
                    "BotCore (https://github.com/mrkirby153/bot-core, 1.0)")
            val inputStream = connection.inputStream

            val json = JSONObject(JSONTokener(inputStream))
            val shards = json.getInt("shards")
            LOGGER.debug("Determined $shards shards")
            inputStream.close()
            return shards
        }
    }

}