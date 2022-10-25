package com.mrkirby153.botcore.command.slashcommand

import com.mrkirby153.botcore.command.slashcommand.dsl.Arguments
import com.mrkirby153.botcore.command.slashcommand.dsl.DslCommandExecutor
import com.mrkirby153.botcore.command.slashcommand.dsl.SlashCommand
import com.mrkirby153.botcore.command.slashcommand.dsl.SubCommand
import java.lang.reflect.Constructor

/**
 * A container for utilizing reified type parameters in the slash command dsl.
 *
 * This is needed to seamlessly handle inner classes
 */
open class SlashCommandContainer {

    inline fun <reified T : Arguments> getInstanceFunction(): () -> T {
        var constructor: Constructor<T>
        var instanceFunc: () -> T
        try {
            // Try to construct an instance as if this class was an inner class
            constructor = T::class.java.getConstructor(this@SlashCommandContainer.javaClass)
            instanceFunc = {
                constructor.newInstance(this@SlashCommandContainer)
            }
        } catch (e: NoSuchMethodException) {
            // Construct an instance if this class is a normal class, the inner class constructor does not exist
            try {
                constructor = T::class.java.getConstructor()
                instanceFunc = {
                    constructor.newInstance()
                }
            } catch (e: NoSuchMethodException) {
                throw IllegalArgumentException("Could not find a suitable constructor for ${T::class.java}")
            }
        }
        return instanceFunc
    }

    /**
     * Declares a slash command with [T] arguments. [T] _must_ have a noargs constructor
     *
     * @see [SlashCommand]
     */
    inline fun <reified T : Arguments> DslCommandExecutor.slashCommand(body: SlashCommand<T>.() -> Unit) =
        SlashCommand(getInstanceFunction<T>()).apply(body).also { this.register(it) }

    /**
     * Declares a new slash command with the arguments [T]. [T] _must_ have a default noargs constructor
     * for it to be correctly invoked
     *
     * @see [SlashCommand]
     */
    inline fun <reified T : Arguments> slashCommand(body: SlashCommand<T>.() -> Unit) =
        SlashCommand(getInstanceFunction<T>()).apply(body)

    /**
     * Declares a sub-command with the arguments [T]. [T] _must_ have a default noargs constructor
     */
    inline fun <reified T : Arguments> SlashCommand<*>.subCommand(body: SubCommand<T>.() -> Unit) =
        SubCommand(getInstanceFunction<T>()).apply(body).also {
            check(this.subCommands[it.name] == null) { "Registering a duplicate sub-command $name" }
            this.subCommands[it.name] = it
        }
}