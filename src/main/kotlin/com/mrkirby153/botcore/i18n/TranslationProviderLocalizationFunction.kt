package com.mrkirby153.botcore.i18n

import com.mrkirby153.botcore.log
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction
import java.util.Locale

class TranslationProviderLocalizationFunction(
    private val bundle: String,
    private val provider: TranslationProvider
) : LocalizationFunction {

    override fun apply(localizationKey: String): Map<DiscordLocale, String> {
        log.debug("Localizing command with key $localizationKey")
        val results =  DiscordLocale.values().filter {
            it != DiscordLocale.UNKNOWN && provider.canTranslate(
                TranslatableMessage.translatable(bundle, localizationKey), it.asJavaLocale()
            )
        }.associateWith {
            provider.translate(
                TranslatableMessage.translatable(bundle, localizationKey), it.asJavaLocale()
            )
        }
        println("$localizationKey -> $results")
        return results
    }
}

private fun DiscordLocale.asJavaLocale() = Locale.forLanguageTag(this.locale)