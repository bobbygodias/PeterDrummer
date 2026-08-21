package com.bobbydias.peterdrummer.core

data class RhythmEvent(
    val timeMs: Long,
    val lane: DrumLane,
    val velocity: Int = 100,
)

data class RhythmChart(
    val id: String,
    val title: String,
    val artist: String,
    val durationMs: Long,
    val audioOffsetMs: Long = 0L,
    val events: List<RhythmEvent>,
)

data class ChartProblem(val eventIndex: Int?, val message: String)

object ChartValidator {
    fun validate(chart: RhythmChart): List<ChartProblem> {
        val problems = mutableListOf<ChartProblem>()
        if (chart.id.isBlank()) problems += ChartProblem(null, "Chart id is blank")
        if (chart.durationMs <= 0L) problems += ChartProblem(null, "Duration must be positive")

        var previousTime = Long.MIN_VALUE
        val seenLaneAtTime = mutableSetOf<Pair<Long, DrumLane>>()
        chart.events.forEachIndexed { index, event ->
            if (event.timeMs < 0L || event.timeMs > chart.durationMs) {
                problems += ChartProblem(index, "Event lies outside the song duration")
            }
            if (event.velocity !in 1..127) {
                problems += ChartProblem(index, "Velocity must be between 1 and 127")
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
