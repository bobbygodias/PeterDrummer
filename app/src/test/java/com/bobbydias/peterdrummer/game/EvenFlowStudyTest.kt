package com.bobbydias.peterdrummer.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class EvenFlowStudyTest {
    @Test
    fun scoreSkeletonMatchesTheReferenceRecordingDuration() {
        assertEquals(131, EvenFlowStudy.barCount)
        assertEquals(18, EvenFlowStudy.tempoMarkers.size)
        assertTrue(abs(EvenFlowStudy.referenceDurationDifferenceMs) < 1_000L)
        assertEquals(774L, EvenFlowStudy.referenceDurationDifferenceMs)
    }
}
