package com.mrkirby153.botcore.command.slashcommand.dsl

@SlashDsl
class SlashCommand<A : Arguments>(
    private val arguments: (() -> A)? = null
) {
    var body: (Context<A>.() -> Unit)? = null
    lateinit var name: String
    lateinit var description: String

    val groups = mutableMapOf<String, List<SubCommand<*>>>()

    fun action(action: Context<A>.() -> Unit) {
        if (groups.isNotEmpty()) {
            throw IllegalArgumentException("Cannot mix groups and non-grouped commands")
        }
        this.body = action
    }

    override fun toString(): String {
        return buildString {
            appendLine("SlashCommand: $name")
            if (arguments != null) {
                val a = arguments.invoke()
                append("args: ${a.get().joinToString(",")}")
            }
        }
    }

    fun run() {
        val ctx = Context(args()!!)
    }

    fun args(): A? = arguments?.invoke()
}

@SlashDsl
class Group {
    val commands = mutableListOf<SubCommand<*>>()
}

@SlashDsl
class SubCommand<A : Arguments>(private val arguments: (() -> A)? = null) {
    lateinit var body: Context<A>.() -> Unit
    lateinit var name: String
    lateinit var description: String

    fun action(action: Context<A>.() -> Unit) {
        this.body = action
    }
}