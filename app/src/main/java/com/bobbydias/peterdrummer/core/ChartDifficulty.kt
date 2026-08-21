package com.bobbydias.peterdrummer.core

import kotlin.math.roundToInt

data class ChartDifficulty(
    val level: Int,
    val label: String,
    val notesPerMinute: Int,
    val peakNotesInTwoSeconds: Int,
    val largestChord: Int,
    val approachTimeMs: Long,
)

/** Difficulty is evidence from the authored chart, never a menu guess. */
object ChartDifficultyAnalyzer {
    fun analyze(chart: RhythmChart): ChartDifficulty {
        val durationMinutes = chart.durationMs.coerceAtLeast(1L) / 60_000.0
        val notesPerMinute = (chart.events.size / durationMinutes).roundToInt()
        val times = chart.events.map(RhythmEvent::timeMs).sorted()
        var peak = 0
        var left = 0
        times.indices.forEach { right ->
            while (times[right] - times[left] >= 2_000L) left++
            peak = maxOf(peak, right - left + 1)
        }
        val largestChord = chart.events.groupingBy(RhythmEvent::timeMs)
            .eachCount()
            .values
            .maxOrNull()
            ?: 1
        val pressure = notesPerMinute / 35.0 + peak / 4.0 + (largestChord - 1) * 0.85
        val level = (pressure / 2.0).roundToInt().coerceIn(1, 5)
        val labels = listOf("Tranquila", "Moderada", "Pegada", "Pesada", "Insana")
        val approachTimes = longArrayOf(4_200L, 3_800L, 3_400L, 3_000L, 2_700L)
        return ChartDifficulty(
            level = level,
            label = labels[level - 1],
            notesPerMinute = notesPerMinute,
            peakNotesInTwoSeconds = peak,
            largestChord = largestChord,
            approachTimeMs = approachTimes[level - 1],
        )
    }
}
