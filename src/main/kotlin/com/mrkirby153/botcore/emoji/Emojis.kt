package com.mrkirby153.botcore.emoji

import com.mrkirby153.botcore.coroutine.await
import com.mrkirby153.botcore.utils.SLF4J
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Icon
import net.dv8tion.jda.api.entities.emoji.ApplicationEmoji
import net.dv8tion.jda.api.sharding.ShardManager

/**
 * A class that provides a way to manage Application Emojis.
 *
 * Emojis are loaded from the provided [metadata] file on the classpath. Emojis are loaded relative
 * to the [emojis] directory, also on the classpath.
 *
 * ## Metadata
 * The metadata file is a JSON file that maps the name of the emoji to the path of the emoji file.
 * ```json
 * {
 *  "green_tick": "green_tick.png",
 *  "red_tick": "red_tick.png"
 * }
 * ```
 *
 * ## Example Usage
 *
 * ```kotlin
 * object Emojis : Emojis() {
 *   val greenTick by emoji("green_tick")
 * }
 * ```
 *
 * @param metadata The metadata file to load emojis from
 * @param emojis The directory to load emojis from
 */
open class Emojis(val metadata: String = "emojis/emojis.json", val emojis: String = "emojis") {

    private val log by SLF4J

    private val emojiMap = mutableMapOf<String, Emoji>()

    /**
     * A delegate to get an emoji by its [name]
     */
    fun emoji(name: String) = EmojiDelegate(name)

    /**
     * Registers the emojis in the [metadata] file to the application that this [shardManager] is
     */
    suspend fun register(shardManager: ShardManager) {
        register(shardManager.shards.first())
    }

    /**
     * Registers the emojis in the [metadata] file to the application that this [jda] is
     */
    suspend fun register(jda: JDA) {
        log.debug("Registering application emojis")
        val resource = this::class.java.classLoader.getResource(metadata)
        checkNotNull(resource) { "Classpath resource \"$metadata\" not found" }
        val emojis = resource.openStream().use {
            Json.decodeFromString<Map<String, String>>(it.reader().readText())
        }

        val discordEmojis = jda.retrieveApplicationEmojis().await()
        val discordEmojiMap = discordEmojis.associateBy { it.name }

        val toRegister = emojis.filter { (name, _) ->
            discordEmojiMap.containsKey(name).not()
        }
        val toDelete = discordEmojiMap.filter { (name, _) ->
            emojis.containsKey(name).not()
        }
        log.debug("Registering ${toRegister.size} emojis")
        log.debug("Deleting ${toDelete.size} emojis")

        val createdEmojis = mutableListOf<ApplicationEmoji>()

        toRegister.forEach { (name, path) ->
            log.debug("Creating application emoji $name from $path")
            val emojiResource =
                this::class.java.classLoader.getResourceAsStream("${this.emojis}/$path")
            if (emojiResource == null) {
                log.error("Unable to create emoji $name from $path")
                return@forEach
            }
            emojiResource.use {
                val icon = Icon.from(it)
                val created = jda.createApplicationEmoji(name, icon).await()
                createdEmojis.add(created)
            }
            log.debug("Created emoji $name")
        }
        toDelete.forEach { (name, emoji) ->
            log.debug("Deleting emoji $name")
            emoji.delete().await()
            log.debug("Deleted emoji $name")
        }

        val allEmojis = (discordEmojis - toDelete.values + createdEmojis).associateBy { it.name }
        log.debug("${allEmojis.size} emojis registered")
        emojiMap.clear()
        emojiMap.putAll(allEmojis.mapValues { Emoji(it.value) })
    }

    internal fun getEmoji(name: String) = emojiMap[name]
}