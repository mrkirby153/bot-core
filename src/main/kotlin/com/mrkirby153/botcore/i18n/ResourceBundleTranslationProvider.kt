package com.mrkirby153.botcore.i18n

import com.ibm.icu.text.MessageFormat
import com.mrkirby153.botcore.log
import java.text.FieldPosition
import java.util.Locale
import java.util.MissingResourceException
import java.util.ResourceBundle

/**
 * A resource bundle translation provider. This translation provider will load translation strings
 * from Java resource bundles located on the classpath. By default, resource bundles are loaded
 * from the `i18n` directory, but this can be overridden by specifying the [basePath] constructor
 * parameter.
 *
 * @param defaultLocale The default locale for this translation provider
 * @param basePath The base path to look for resource bundles
 */
class ResourceBundleTranslationProvider(
    defaultLocale: () -> Locale,
    private val basePath: String = "i18n"
) : TranslationProvider(defaultLocale) {

    private val resourceBundles = mutableMapOf<Pair<String, Locale>, ResourceBundle>()

    override fun translate(
        component: TranslatableMessage,
        locale: Locale,
        substitutions: Map<String, Any?>
    ): String {
        log.trace("Attempting to translate $component with $substitutions")
        if (!canTranslate(component, locale)) {
            log.trace("Missing translation $component")
            return "<<MISSING TRANSLATION ${component.bundle}/${component.key}: $locale>>"
        }
        val message = getMessage(component, locale)
            ?: "<<MISSING MESSAGE ${component.bundle}/${component.key}: $locale>>"
        val formatter = MessageFormat(message, locale)
        return formatter.format(substitutions, StringBuffer(), FieldPosition(1)).toString()
    }

    override fun canTranslate(component: TranslatableMessage, locale: Locale): Boolean {
        return getMessage(component, locale) != null
    }

    private fun getMessage(component: TranslatableMessage, locale: Locale): String? {
        log.trace("Retrieving $component (locale: $locale)")
        val bundle = getResourceBundle(component.bundle, locale)
        val overrideBundle = getResourceBundle("${component.bundle}_override", locale)
        return if (overrideBundle != null && overrideBundle.containsKey(component.key)) {
            val result = overrideBundle.getString(component.key)
            log.trace("Retrieved $result from ${component.bundle}_overrides")
            result
        } else if (bundle != null && bundle.containsKey(component.key)) {
            val result = bundle.getString(component.key)
            log.trace("Retrieved $result from ${component.bundle}")
            result
        } else {
            log.trace("Key ${component.key} was not found in the bundle")
            null
        }
    }

    private fun getResourceBundle(bundleName: String, locale: Locale): ResourceBundle? {
        val mapKey = Pair(bundleName, locale)
        val existing = resourceBundles[mapKey]
        if (existing != null) {
            return existing
        }
        val bundleLocation = if (basePath.isNotBlank()) "$basePath/${bundleName}" else bundleName
        log.trace("Loading resource bundle from $bundleLocation (locale $locale)")
        return try {
            val bundle = ResourceBundle.getBundle(bundleLocation, locale)
            resourceBundles[mapKey] = bundle
            bundle
        } catch (e: MissingResourceException) {
            null
        }
    }
}