package com.mrkirby153.botcore.spring.config

import org.springframework.context.annotation.Import

/**
 * Enables JDA bot support
 */
@Import(ShardManagerConfiguration::class, DslCommandExecutorConfiguration::class)
@Target(AnnotationTarget.CLASS)
annotation class EnableBot
