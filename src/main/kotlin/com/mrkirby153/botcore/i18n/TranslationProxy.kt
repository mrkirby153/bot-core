package com.mrkirby153.botcore.i18n

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.Locale

@PublishedApi
internal val proxyCache = mutableMapOf<Class<*>, Any>()

@PublishedApi
internal val defaultTranslationProvider = ResourceBundleTranslationProvider()

/**
 * Construct a new translation proxy.
 *
 * Every method that is invoked will attempt to be translated.
 *
 * All methods must return [TranslatedMessage].
 *
 * The translation bundle of a proxy must be specified using the [Bundle] annotation. By default,
 * argument names will attempt to be substituted. For example `greet(name: String)` calling
 * `greet("John")` will substitute "John" in the string `Hello, {name}`. The parameter name can
 * be overridden by specifying the [ParameterName] annotation.
 *
 * When looking for the translation key, the method name in lowercase will be used. For example,
 * `testMethod` will translate to the key `testmethod`. This behavior can be overridden using the
 * [Key] annotation.
 *
 * **Note:** In order for automatic substitution to work, method names must be preserved by
 * the compiler. Method names can be preserved with the following snippet in `build.gradle.kts`
 * ```
 *     task.withType<KotlinCompile> {
 *         kotlinOptions {
 *             javaParameters = true
 *         }
 *     }
 * ```
 */
inline fun <reified T : Any> translatable(translationProvider: TranslationProvider = defaultTranslationProvider): T {
    return (synchronized(proxyCache) { proxyCache[T::class.java] } ?: Proxy.newProxyInstance(
        T::class.java.classLoader,
        arrayOf(T::class.java),
        TranslationProxyInvocationHandler(translationProvider)
    ).also {
        synchronized(proxyCache) {
            proxyCache[T::class.java] = it
        }
    }) as T
}

@PublishedApi
internal class TranslationProxyInvocationHandler(
    private val provider: TranslationProvider
) : InvocationHandler {

    override fun invoke(proxy: Any?, method: Method?, args: Array<out Any>?): Any? {
        check(method != null) { "TranslationProxy invoked with a null method" }
        check(method.returnType == TranslatedMessage::class.java) { "${method.name} must have return type of TranslatedMessage. Got ${method.returnType}" }
        val bundle = synchronized(bundleCache) {
            bundleCache[method.declaringClass]
        } ?: getBundle(method).also {
            synchronized(bundleCache) {
                bundleCache[method.declaringClass] = it
            }
        }
        val key = synchronized(keyCache) {
            keyCache[method]
        } ?: getKey(method).also {
            synchronized(keyCache) {
                keyCache[method] = it
            }
        }
        val translatable = TranslatableMessage(bundle, key)
        val substitutions =
            method.parameters.map {
                it.getAnnotation(ParameterName::class.java)?.value ?: it.name
            }.toTypedArray() zip (args ?: emptyArray())
        return TranslatedMessage(translatable, provider, substitutions)
    }


    private fun getBundle(method: Method): String {
        val annotation = method.declaringClass.getAnnotation(Bundle::class.java)
            ?: error("@Bundle must be provided")
        return annotation.value
    }

    private fun getKey(method: Method): String {
        val annotation = method.getAnnotation(Key::class.java)
        return annotation?.value ?: method.name.lowercase()
    }

    companion object {
        private val bundleCache = mutableMapOf<Class<*>, String>()
        private val keyCache = mutableMapOf<Method, String>()
    }
}

@Target(AnnotationTarget.FUNCTION)
annotation class Key(val value: String)

@Target(AnnotationTarget.CLASS)
annotation class Bundle(val value: String)

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class ParameterName(val value: String)

/**
 * A wrapper around [TranslatableMessage] but includes the message, the [TranslationProvider] and
 * a list of substitutions.
 */
data class TranslatedMessage(
    private val translatableMessage: TranslatableMessage,
    private val provider: TranslationProvider,
    private val substitutions: List<Pair<String, Any>>
) {
    /**
     * Translates this message into the provided [locale], or the default if not specified.
     */
    fun translate(locale: Locale = Locale.getDefault()) =
        provider.translate(translatableMessage, substitutions.toMap())
}