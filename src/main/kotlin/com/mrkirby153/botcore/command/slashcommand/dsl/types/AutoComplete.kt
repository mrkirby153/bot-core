package com.mrkirby153.botcore.command.slashcommand.dsl.types

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command


typealias AutoCompleteCallback = (CommandAutoCompleteInteractionEvent) -> List<Command.Choice>

/**
 * Marker interface for arguments that support autocompletion
 */
interface AutocompleteEligible {
    var autocompleteFunction: AutoCompleteCallback?
}


/**
 * Determines the autocomplete suggestions to display to the user. The maximum number of suggestions
 * is 25. Any more will be truncated
 */
fun AutocompleteEligible.autoComplete(function: AutoCompleteCallback) {
    this.autocompleteFunction = function
}