package com.bobbydias.peterdrummer.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChartValidatorTest {
    @Test
    fun simultaneousDifferentLanesAreValid() {
        val chart = RhythmChart(
            id = "test",
            title = "Test",
            artist = "Test",
            durationMs = 5_000,
            events = listOf(
                RhythmEvent(1_000, DrumLane.KICK),
                RhythmEvent(1_000, DrumLane.CRASH),
                RhythmEvent(2_000, DrumLane.SNARE),
            ),
        )

        assertTrue(ChartValidator.validate(chart).isEmpty())
    }

    @Test
    fun duplicateSameLaneAndTimeIsRejected() {
        val chart = RhythmChart(
            id = "test",
            title = "Test",
            artist = "Test",
            durationMs = 5_000,
            events = listOf(
                RhythmEvent(1_000, DrumLane.KICK),
                RhythmEvent(1_000, DrumLane.KICK),
            ),
        )

        assertEquals(1, ChartValidator.validate(chart).size)
    }
}
