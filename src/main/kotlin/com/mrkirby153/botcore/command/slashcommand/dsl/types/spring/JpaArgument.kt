package com.mrkirby153.botcore.command.slashcommand.dsl.types.spring

import com.mrkirby153.botcore.command.slashcommand.dsl.AbstractSlashCommand
import com.mrkirby153.botcore.command.slashcommand.dsl.ArgumentConverter
import com.mrkirby153.botcore.command.slashcommand.dsl.ArgumentParseException
import com.mrkirby153.botcore.command.slashcommand.dsl.WrappedData
import com.mrkirby153.botcore.command.slashcommand.dsl.types.ArgumentBuilder
import com.mrkirby153.botcore.utils.SLF4J
import jakarta.persistence.EntityManager
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull
import kotlin.math.min

@Configuration
open class JpaArgumentConfig(
    entityManager: EntityManager
) {
    init {
        Companion.entityManager = entityManager
    }


    @PublishedApi
    internal companion object {
        var entityManager: EntityManager? = null

        fun entityManagerPresent() = entityManager != null

        fun getEntityId(entity: Any): Any? {
            val em = entityManager
            checkNotNull(em) { "No EntityManager found in the current context" }
            return em.entityManagerFactory.persistenceUnitUtil.getIdentifier(entity)
        }
    }
}

class ThreadSafeJpaArgument<Obj, Id, Repo : JpaRepository<Obj, Id>>(
    private val id: Id, private val repo: Repo
) : WrappedData<Obj> {

    private val log by SLF4J

    private val inst: ThreadLocal<Obj> = ThreadLocal()
    private val loaded: ThreadLocal<Boolean> = ThreadLocal.withInitial { false }

    override fun get(): Obj {
        if (loaded.get()) {
            log.trace(
                "Retrieving cached instance of entity {} for thread {}", id, Thread.currentThread()
            )
            return inst.get()
        }
        log.trace(
            "Retrieving entity {} from the database for thread {}", id, Thread.currentThread()
        )
        val existing = repo.findByIdOrNull(this.id)
        inst.set(existing)
        loaded.set(true)
        return existing ?: throw NoSuchElementException("Object with id $id not found")
    }
}

class JpaObjectConverter<Obj, Id, Repo : JpaRepository<Obj, Id>>(
    private val repo: Repo,
    private val mapper: (OptionMapping) -> Id,
    private val displayName: String = "Object for repo $repo"
) : ArgumentConverter<ThreadSafeJpaArgument<Obj, Id, Repo>> {

    override fun convert(input: OptionMapping): ThreadSafeJpaArgument<Obj, Id, Repo> {
        val entityId = mapper(input)
        if (!repo.existsById(entityId)) {
            throw ArgumentParseException("$displayName with id $entityId not found")
        }
        return ThreadSafeJpaArgument(entityId, repo)
    }

    override val type: OptionType
        get() = OptionType.STRING
}

/**
 * Creates a slash command argument that takes this JPA argument as an input. Specify [enableAutocomplete]
 * to enable the default autocomplete handler.
 *
 * **Note:** The default autocomplete handler may be slow for a large number of entities
 *
 * The default autocomplete handler returns values mapped by [autocompleteName]
 */
inline fun <reified T : Any, reified Id> JpaRepository<T, Id>.argument(
    slashCommand: AbstractSlashCommand,
    name: String? = null,
    enableAutocomplete: Boolean = false,
    crossinline autocompleteName: (T) -> String = { it.toString() },
    builder: ArgumentBuilder<T, ThreadSafeJpaArgument<T, Id, JpaRepository<T, Id>>>.() -> Unit = {},
): ArgumentBuilder<T, ThreadSafeJpaArgument<T, Id, JpaRepository<T, Id>>> {
    val className = T::class.java.name
    return ArgumentBuilder<T, ThreadSafeJpaArgument<T, Id, JpaRepository<T, Id>>>(
        slashCommand, JpaObjectConverter(this, {
            when (Id::class.java) {
                Long::class.java, java.lang.Long::class.java -> it.asLong
                Int::class.java, java.lang.Integer::class.java -> it.asInt
                Float::class.java, java.lang.Float::class.java -> it.asDouble.toFloat()
                Double::class.java, java.lang.Double::class.java -> it.asDouble
                String::class.java, java.lang.String::class.java -> it.asString
                else -> throw IllegalArgumentException("Invalid id type ${Id::class.java}. Supported types are long, int, float, double and string")
            } as Id
        }, className)
    ).apply(builder).apply {
        if (name != null) {
            this@apply.name = name
        }
        if (enableAutocomplete) {
            autocomplete { event ->
                if (!JpaArgumentConfig.entityManagerPresent()) {
                    return@autocomplete listOf("No EntityManager found" to "$$$")
                }
                val options = this@argument.findAll()
                val selected = event.focusedOption.value
                options.filter { autocompleteName(it).lowercase().startsWith(selected.lowercase()) }
                    .take(25).map {
                        val n = autocompleteName(it)
                        n.substring(0, min(100, n.length)) to JpaArgumentConfig.getEntityId(it)
                            .toString()
                    }
            }
        }
    }
}

context(AbstractSlashCommand)
@Deprecated("Use the overload that takes the slash command as the first argument")
inline fun <reified T : Any, reified Id> JpaRepository<T, Id>.argument(
    name: String? = null,
    enableAutocomplete: Boolean = false,
    crossinline autocompleteName: (T) -> String = { it.toString() },
    builder: ArgumentBuilder<T, ThreadSafeJpaArgument<T, Id, JpaRepository<T, Id>>>.() -> Unit = {}
) = this.argument(this@AbstractSlashCommand, name, enableAutocomplete, autocompleteName, builder)