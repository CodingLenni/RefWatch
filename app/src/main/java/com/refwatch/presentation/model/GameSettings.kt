package com.refwatch.presentation.model

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

object GameSettings {
    const val FIRST_HALF_TEXT: String = "1. Halbzeit"
    const val SECOND_HALF_TEXT: String = "2. Halbzeit"

    /**
     * The fixed, immutable list of all available GameSetting options.
     */
    val availableSettings: List<Game> = listOf(
        Game(
            Halftime(Duration.ZERO, 45.minutes, FIRST_HALF_TEXT, Period.FIRST),
            Halftime(45.minutes, 45.minutes, SECOND_HALF_TEXT, Period.SECOND),
            "Damen / Herren"
        ),
        Game(
            Halftime(Duration.ZERO, 40.minutes, FIRST_HALF_TEXT, Period.FIRST),
            Halftime(40.minutes, 40.minutes, SECOND_HALF_TEXT, Period.SECOND),
            "B - Jugend"
        ),
        Game(
            Halftime(Duration.ZERO, 35.minutes, FIRST_HALF_TEXT, Period.FIRST),
            Halftime(35.minutes, 35.minutes, SECOND_HALF_TEXT, Period.SECOND),
            "C - Jugend"
        ),
        Game(
            Halftime(Duration.ZERO, 30.minutes, FIRST_HALF_TEXT, Period.FIRST),
            Halftime(30.minutes, 30.minutes, SECOND_HALF_TEXT, Period.SECOND),
            "D - Jugend"
        ),
    )

    /**
     * A convenient property to get the default setting.
     */
    val defaultSetting: Game = availableSettings.first()
}