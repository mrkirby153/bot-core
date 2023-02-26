package com.mrkirby153.botcore.i18n

import kotlin.reflect.KProperty

/**
 * A kotlin class that lists of translations can extend for convenience in setting a component's
 * bundle name. Use the [translatable] delegate to create a [TranslatableMessage]
 * with the provided [bundle].
 *
 * @param bundle The name of the bundle for all translatable messages in this class
 */
open class Translations(val bundle: String)

/**
 * Data class for translatable messages. Stores the bundle and key of the message for translation.
 *
 * @param bundle The resource bundle
 * @param key The key in the resource bundle
 */
data class TranslatableMessage internal constructor(
    override val bundle: String,
    override val key: String
) : TranslationKey {
    companion object {
        /**
         * Java helper function for creating [TranslatableMessage]
         *
         * @param bundle The resource bundle where this translation is located
         * @param key The key in the resource bundle where the translation is located
         *
         * @return A [TranslatableMessage]
         */
        @JvmStatic
        fun translatable(bundle: String, key: String) = TranslatableMessage(bundle, key)
    }
}

/**
 * Delegate class for constructing [TranslatableMessage]
 */
class MessageTranslation(private val key: String) {
    operator fun getValue(instance: Translations, property: KProperty<*>) =
        TranslatableMessage(instance.bundle, key)
}

/**
 * Delegate for easily constructing [MessageTranslation] with the provided [key] inside of a
 * [Translations] class. The constructed [MessageTranslation] will inherit the [Translations.bundle]
 * from the parent [Translations] class
 */
fun Translations.translatable(key: String) = MessageTranslation(key)