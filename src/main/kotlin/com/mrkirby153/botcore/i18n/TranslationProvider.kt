package com.mrkirby153.botcore.i18n

import java.util.Locale

/**
 * An abstract translation provider
 *
 * @param defaultLocale The default locale of this translation provider
 */
abstract class TranslationProvider(
    defaultLocale: () -> Locale
) {
    /**
     * The default locale of this translation provider
     */
    val defaultLocale by lazy { defaultLocale() }

    /**
     * Translates the provided [component] into the given [locale], substituting any [substitutions]
     * into the message. The message is expected to be ICU compatible.
     */
    abstract fun translate(
        component: TranslatableMessage,
        locale: Locale,
        substitutions: Map<String, Any?>
    ): String

    /**
     * Translates the provided [component] into the given [locale], substituting any [substitutions]
     * into the message.
     */
    @JvmOverloads
    open fun translate(
        component: TranslatableMessage,
        locale: Locale = defaultLocale,
        substitutions: Array<Any?> = arrayOf()
    ): String {
        val mapped =
            substitutions.mapIndexed { index, any -> Pair(index.toString(), any) }.associate { it }
        return translate(component, locale, mapped)
    }

    /**
     * Translates the provided [component] into the [defaultLocale], substituting any [substitutions]
     */
    open fun translate(component: TranslatableMessage, substitutions: Array<Any?> = arrayOf()) =
        translate(component, defaultLocale, substitutions)

    /**
     * Translates the provided [component] into the [defaultLocale], substituting any [substitutions]
     */
    open fun translate(
        component: TranslatableMessage,
        substitutions: Map<String, Any?>
    ) = translate(component, defaultLocale, substitutions)

    /**
     * Returns true if the provided [component] can be translated into the given [locale]
     */
    abstract fun canTranslate(component: TranslatableMessage, locale: Locale): Boolean
}