package com.refwatch.presentation.model

import kotlin.time.Duration

class Halftime {
    val period: Period
    val startTimeMinutes: Duration
    val length: Duration
    val label: String

    constructor(startTimeMinutes: Duration, length: Duration, label: String, period: Period) {
        this.startTimeMinutes = startTimeMinutes
        this.length = length
        this.label = label
        this.period = period
    }
}