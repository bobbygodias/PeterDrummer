package com.bobbydias.peterdrummer.game

import com.bobbydias.peterdrummer.core.DrumLane
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PeterPerformanceStateTest {
    @Test
    fun simultaneousHitsProduceACompositePeterPose() {
        val state = PeterPerformanceState(poseHoldMs = 140L)

        state.strike(DrumLane.HI_HAT, atMs = 1_000L)
        state.strike(DrumLane.SNARE, atMs = 1_000L)
        state.strike(DrumLane.KICK, atMs = 1_000L)

        assertEquals(
            listOf(DrumLane.SNARE, DrumLane.KICK, DrumLane.HI_HAT),
            state.activeLanes(atMs = 1_050L),
        )
        assertTrue(state.activeLanes(atMs = 1_140L).isEmpty())
    }

    @Test
    fun repeatedHitExtendsOnlyItsOwnPose() {
        val state = PeterPerformanceState(poseHoldMs = 100L)

        state.strike(DrumLane.CRASH, atMs = 500L)
        state.strike(DrumLane.CRASH, atMs = 580L)

        assertEquals(listOf(DrumLane.CRASH), state.activeLanes(atMs = 650L))
        assertTrue(state.activeLanes(atMs = 680L).isEmpty())
    }
}
