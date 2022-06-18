package com.mrkirby153.botcore.command.slashcommand.dsl.types

interface HasMinAndMax<T: Number> {
    var min: T?
    var max: T?
}