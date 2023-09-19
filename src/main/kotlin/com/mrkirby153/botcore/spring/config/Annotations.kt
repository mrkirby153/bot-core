package com.mrkirby153.botcore.spring.config

import org.springframework.context.annotation.Import

/**
 * Enables JDA bot support
 */
@Import(ShardManagerConfiguration::class, DslCommandExecutorAutoConfiguration::class)
@Target(AnnotationTarget.CLASS)
annotation class EnableBot

/**
 * Automatically discover and register slash commands
 */
@Import(DslCommandExecutorRegister::class)
annotation class RegisterSlashCommands
