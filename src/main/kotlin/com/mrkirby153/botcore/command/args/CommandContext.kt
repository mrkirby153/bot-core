package com.mrkirby153.botcore.command.args

/**
 * Wrapper that stores all resolved arguments
 */
@Suppress("UNCHECKED_CAST")
class CommandContext {

    /**
     * The raw argument list
     */
    private val arguments = mutableMapOf<String, Any?>()

    /**
     * Gets a potentially nullable argument with the provided key
     *
     * @param key The key
     *
     * @return The value corresponding to the key, or null
     */
    fun <T> get(key: String): T? = arguments[key] as T?

    /**
     * Gets a non-null argument with the given key
     *
     * @param key The key
     *
     * @return A guaranteed non-null argument corresponding to the key
     *
     * @throws NullPointerException If the argument isn't present
     */
    fun <T> getNotNull(key: String): T = arguments[key]!! as T

    /**
     * Puts an object into the command context
     *
     * @param key The key of the object
     * @param obj The value of the object
     */
    fun put(key: String, obj: Any?) {
        arguments[key] = obj
    }

    /**
     * Checks if there is an argument with the given key
     *
     * @param key The key to check
     *
     * @return True if the argument exists
     */
    fun has(key: String) = arguments.containsKey(key)

    /**
     * Executes a lambda function if the argument is present
     *
     * @param key The key to get
     * @param consumer The function to execute if the key exists
     */
    fun <T> ifPresent(key: String, consumer: (T) -> Unit) {
        val d = get<T>(key) ?: return
        consumer.invoke(d)
    }

    override fun toString(): String {
        return "CommandContext(arguments=$arguments)"
    }
}