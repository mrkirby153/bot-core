package com.mrkirby153.botcore.spring.config

import com.mrkirby153.botcore.command.slashcommand.dsl.types.spring.JpaArgumentConfig
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
@Target(AnnotationTarget.CLASS)
annotation class RegisterSlashCommands

/**
 * Enables built-in autocomplete for spring jpa entities
 */
@Import(JpaArgumentConfig::class)
@Target(AnnotationTarget.CLASS)
annotation class EnableJpaAutocomplete


/**
 * Enables the built-in modal manager
 */
@Import(ModalManagerConfiguration::class)
@Target(AnnotationTarget.CLASS)
annotation class EnableModalManager