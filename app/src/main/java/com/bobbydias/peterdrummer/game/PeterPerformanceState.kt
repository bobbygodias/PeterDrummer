package com.bobbydias.peterdrummer.game

import com.bobbydias.peterdrummer.core.DrumLane

/**
 * Holds the pieces Peter is visibly striking at a given instant.
 *
 * Each lane expires independently so combinations such as hi-hat + snare +
 * kick remain a single composite pose instead of the last hit hiding the
 * others. Final artwork can consume [activeLanes] to select arm and pedal
 * frames without changing the rhythm or scoring engines.
 */
class PeterPerformanceState(
    private val poseHoldMs: Long = 140L,
) {
    private val activeUntilMs = LongArray(DrumLane.entries.size)

    init {
        require(poseHoldMs > 0L) { "Pose hold time must be positive" }
    }

    fun strike(lane: DrumLane, atMs: Long) {
        activeUntilMs[lane.index] = maxOf(activeUntilMs[lane.index], atMs + poseHoldMs)
    }

    fun activeLanes(atMs: Long): List<DrumLane> =
        DrumLane.entries.filter { lane -> activeUntilMs[lane.index] > atMs }
}
