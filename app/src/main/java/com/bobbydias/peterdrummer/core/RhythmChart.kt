package com.bobbydias.peterdrummer.core

enum class DrumArticulation {
    STANDARD,
    HI_HAT_CLOSED,
    HI_HAT_OPEN,
    HI_HAT_PEDAL,
}

data class RhythmEvent(
    val timeMs: Long,
    val lane: DrumLane,
    val velocity: Int = 100,
    val articulation: DrumArticulation = DrumArticulation.STANDARD,
)

data class RhythmChart(
    val id: String,
    val title: String,
    val artist: String,
    val durationMs: Long,
    val audioOffsetMs: Long = 0L,
    val audioFileNames: List<String> = emptyList(),
    val events: List<RhythmEvent>,
)

data class ChartProblem(val eventIndex: Int?, val message: String)

object ChartValidator {
    fun validate(chart: RhythmChart): List<ChartProblem> {
        val problems = mutableListOf<ChartProblem>()
        if (chart.id.isBlank()) problems += ChartProblem(null, "Chart id is blank")
        if (chart.durationMs <= 0L) problems += ChartProblem(null, "Duration must be positive")
        if (chart.events.isEmpty()) problems += ChartProblem(null, "Chart has no drum events")

        var previousTime = Long.MIN_VALUE
        val seenLaneAtTime = mutableSetOf<Pair<Long, DrumLane>>()
        chart.events.forEachIndexed { index, event ->
            if (event.timeMs < 0L || event.timeMs > chart.durationMs) {
                problems += ChartProblem(index, "Event lies outside the song duration")
            }
            if (event.velocity !in 1..127) {
                problems += ChartProblem(index, "Velocity must be between 1 and 127")
            }
            val isHiHatArticulation = event.articulation != DrumArticulation.STANDARD
            if (isHiHatArticulation && event.lane != DrumLane.HI_HAT) {
                problems += ChartProblem(index, "Hi-hat articulation used on a different lane")
            }
            if (event.timeMs < previousTime) {
                problems += ChartProblem(index, "Events are not sorted by time")
            }
            if (!seenLaneAtTime.add(event.timeMs to event.lane)) {
                problems += ChartProblem(index, "Duplicate event on the same lane and time")
            }
            previousTime = event.timeMs
        }
        return problems
    }
}
