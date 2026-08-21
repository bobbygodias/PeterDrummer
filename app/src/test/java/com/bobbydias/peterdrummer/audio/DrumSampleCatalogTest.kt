package com.bobbydias.peterdrummer.audio

import com.bobbydias.peterdrummer.core.DrumArticulation
import com.bobbydias.peterdrummer.core.DrumLane
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DrumSampleCatalogTest {
    @Test
    fun catalogContainsAllOriginalAndDerivedSamplesWithoutDuplicatePaths() {
        val paths = DrumSampleCatalog.entries.map(DrumSampleCatalog::assetPath)

        assertEquals(144, paths.size)
        assertEquals(paths.size, paths.toSet().size)
        assertEquals(
            15,
            DrumSampleCatalog.entries.count {
                it.lane == DrumLane.HI_HAT &&
                    it.articulation == DrumArticulation.HI_HAT_OPEN
            },
        )
        assertEquals(
            3,
            DrumSampleCatalog.entries.count {
                it.articulation == DrumArticulation.HI_HAT_PEDAL
            },
        )
        assertTrue(paths.all { it.startsWith("drums/") && it.endsWith(".opus") })
    }
}
