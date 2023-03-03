package com.mrkirby153.botcore.i18n

import com.mrkirby153.botcore.utils.SLF4J
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction
import org.slf4j.Logger
import java.util.Locale

class TranslationProviderLocalizationFunction(
    private val bundle: String,
    private val provider: TranslationProvider
) : LocalizationFunction {

    private val log: Logger by SLF4J

    override fun apply(localizationKey: String): Map<DiscordLocale, String> {
        log.debug("Localizing command with key $localizationKey")
        val results = DiscordLocale.values().filter {
            it != DiscordLocale.UNKNOWN && provider.canTranslate(
                Translatable(bundle, localizationKey), it.asJavaLocale()
            )
        }.associateWith {
            provider.translate(
                Translatable(bundle, localizationKey), it.asJavaLocale()
            )
        }
        return results
    }

    private data class Translatable(override val bundle: String, override val key: String) :
        TranslationKey
}

private fun DiscordLocale.asJavaLocale() = Locale.forLanguageTag(this.locale)