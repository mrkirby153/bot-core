package com.mrkirby153.botcore.spring.config

import com.mrkirby153.botcore.command.slashcommand.dsl.DslCommandExecutor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class DslCommandExecutorConfiguration {
    @Bean
    open fun dslCommandExecutor() = DslCommandExecutor()
}