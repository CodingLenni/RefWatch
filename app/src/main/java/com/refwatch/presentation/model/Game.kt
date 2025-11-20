package com.refwatch.presentation.model

import java.util.UUID

class Game {
    val id: String
    val firstHalf: Halftime
    val secondHalf: Halftime
    val description: String

    constructor(firstHalf: Halftime, secondHalf: Halftime, description: String) {
        this.firstHalf = firstHalf
        this.secondHalf = secondHalf
        this.description = description
        this.id = UUID.randomUUID().toString()
    }
}