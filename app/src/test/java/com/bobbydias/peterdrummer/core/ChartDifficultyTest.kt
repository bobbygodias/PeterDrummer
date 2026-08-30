package com.bobbydias.peterdrummer.core

import org.junit.Assert.assertTrue
import org.junit.Test

class ChartDifficultyTest {
    @Test
    fun denserAuthoredMusicProducesHigherDifficultyAndLessApproachTime() {
        val easy = RhythmChart(
            id = "easy",
            title = "Easy",
            artist = "Test",
            durationMs = 60_000,
            events = (1..20).map { RhythmEvent(it * 2_500L, DrumLane.SNARE) },
        )
        val hard = RhythmChart(
            id = "hard",
            title = "Hard",
            artist = "Test",
            durationMs = 20_000,
            events = (1..150).map { RhythmEvent(it * 100L, DrumLane.entries[it % 8]) },
        )

        val easyResult = ChartDifficultyAnalyzer.analyze(easy)
        val hardResult = ChartDifficultyAnalyzer.analyze(hard)

        assertTrue(hardResult.level > easyResult.level)
        assertTrue(hardResult.approachTimeMs < easyResult.approachTimeMs)
        assertTrue(hardResult.notesPerMinute > easyResult.notesPerMinute)
    }
}
