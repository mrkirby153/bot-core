package com.mrkirby153.botcore.command.slashcommand.dsl.types

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command


typealias AutoCompleteCallback = (CommandAutoCompleteInteractionEvent) -> List<Command.Choice>

interface AutocompleteEligible {
    var autocompleteFunction: AutoCompleteCallback?
}

fun AutocompleteEligible.autoComplete(function: AutoCompleteCallback) {
    this.autocompleteFunction = function
}