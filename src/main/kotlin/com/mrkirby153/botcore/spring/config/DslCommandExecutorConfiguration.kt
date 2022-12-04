package com.mrkirby153.botcore.spring.config

import com.mrkirby153.botcore.command.slashcommand.dsl.DslCommandExecutor
import com.mrkirby153.botcore.i18n.TranslationProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class DslCommandExecutorConfiguration {


    @Bean
    open fun dslCommandExecutor() = DslCommandExecutor()

    @Bean
    @ConditionalOnBean(TranslationProvider::class)
    open fun translatableDslCommandExecutor(
        @Value("\${bot.command.translation-bundle}") bundle: String,
        translationProvider: TranslationProvider
    ) = DslCommandExecutor(bundle, translationProvider)
}