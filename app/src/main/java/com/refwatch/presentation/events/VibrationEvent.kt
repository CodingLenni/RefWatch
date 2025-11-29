package com.refwatch.presentation.events

sealed class VibrationEvent {

    /**
     * Represents a short, sharp pulse of vibration.
     * Duration set to 150ms.
     * @param amplitude The strength, from 1 to 255. Defaults to max strength.
     */
    data class ShortPulse(
        val durationMs: Long = 150L,
        val amplitude: Int = 255
    ) : VibrationEvent()

    /**
     * Represents a long, noticeable pulse of vibration.
     * Duration set to 500ms (half a second).
     * @param amplitude The strength, from 1 to 255. Defaults to max strength.
     */
    data class LongPulse(
        val durationMs: Long = 500L,
        val amplitude: Int = 255
    ) : VibrationEvent()
}