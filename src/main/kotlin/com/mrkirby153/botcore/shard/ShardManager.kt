package com.mrkirby153.botcore.shard

import com.mrkirby153.botcore.LOGGER
import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.JDABuilder
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.User
import net.dv8tion.jda.core.events.ReadyEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
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
class ShardManager(private val token: String, val numShards: Int) {

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

    /**
     * Adds a shard to the pool.
     *
     * If it's been less than 5 seconds since the last shard was started, it will wait 5 seconds as per the spec
     *
     * @param id The id of the shard
     */
    fun addShard(id: Int) {
        if (id > numShards) {
            throw IllegalArgumentException("Cannot start more than the allocated amount of shards")
        }
        if (numShards > 1 && lastStartTime != -1L && lastStartTime + 5000 < System.currentTimeMillis()) {
            LOGGER.info("Waiting 5 seconds before starting the next shard")
            Thread.sleep(5000)
        }
        startingShards.add(id)
        LOGGER.info("Starting shard $id")
        val jda = buildJDA(id)
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
        shards.forEach { it.addEventListener(eventListener) }
    }

    /**
     * Removes an event listener across all shards
     *
     * @param eventListener The event listener to remove
     */
    fun removeListener(eventListener: Any) {
        LOGGER.debug("Removing ${eventListener.javaClass} across ${shards.size} shards")
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
    private fun buildJDA(id: Int): JDA {
        return JDABuilder(AccountType.BOT).run {
            setToken(this@ShardManager.token)
            setAutoReconnect(true)
            setBulkDeleteSplittingEnabled(false)
            if (this@ShardManager.numShards > 1)
                useSharding(id, numShards)
            addEventListener(object : ListenerAdapter() {
                override fun onReady(event: ReadyEvent) {
                    LOGGER.debug("Shard $id is ready")
                    startingShards.remove(id)
                    event.jda.removeEventListener(this)
                }
            })
            buildAsync()
        }
    }

    /**
     * Starts all shards async
     */
    fun startAllShards() {
        startupThread = Thread {
            LOGGER.debug("Starting all shards")
            for (i in 0 until numShards) {
                addShard(i)
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