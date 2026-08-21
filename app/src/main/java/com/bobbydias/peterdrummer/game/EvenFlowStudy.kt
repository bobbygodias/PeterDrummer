package com.bobbydias.peterdrummer.game

data class TempoMarker(val bar: Int, val bpm: Int)

/**
 * Verified timing skeleton for the first laboratory track.
 *
 * The source PDF and isolated-drums reference stay outside the public
 * repository. This object stores only measurements needed to author and test
 * the eventual temporal chart.
 */
object EvenFlowStudy {
    const val songId = "pearl_jam_even_flow"
    const val title = "Even Flow"
    const val artist = "Pearl Jam"
    const val barCount = 131
    const val isolatedDrumsDurationMs = 294_139L
    const val firstDetectedDrumActivityMs = 1_038L

    val tempoMarkers = listOf(
        TempoMarker(1, 100),
        TempoMarker(5, 104),
        TempoMarker(16, 102),
        TempoMarker(20, 103),
        TempoMarker(22, 104),
        TempoMarker(40, 105),
        TempoMarker(53, 104),
        TempoMarker(62, 105),
        TempoMarker(72, 106),
        TempoMarker(76, 107),
        TempoMarker(80, 110),
        TempoMarker(88, 112),
        TempoMarker(98, 108),
        TempoMarker(102, 106),
        TempoMarker(106, 105),
        TempoMarker(120, 107),
        TempoMarker(124, 109),
        TempoMarker(128, 111),
    )

    private val twoBeatBars = setOf(7, 55)

    val scoreDurationMs: Long
        get() {
            var currentTempo = tempoMarkers.first().bpm
            var durationMs = 0.0
            for (bar in 1..barCount) {
                tempoMarkers.firstOrNull { it.bar == bar }?.let { currentTempo = it.bpm }
                val beats = if (bar in twoBeatBars) 2 else 4
                durationMs += beats * 60_000.0 / currentTempo
            }
            return durationMs.toLong()
        }

    val referenceDurationDifferenceMs: Long
        get() = scoreDurationMs - isolatedDrumsDurationMs
}
