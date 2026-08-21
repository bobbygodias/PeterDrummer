package com.bobbydias.peterdrummer.core

import org.junit.Assert.assertEquals
import org.junit.Test

class DrumLaneContractTest {
    @Test
    fun canonicalKitHasExactlyEightPiecesInTheApprovedOrder() {
        assertEquals(
            listOf(
                DrumLane.HI_HAT,
                DrumLane.CRASH,
                DrumLane.RIDE,
                DrumLane.SNARE,
                DrumLane.HIGH_TOM,
                DrumLane.MID_TOM,
                DrumLane.FLOOR_TOM,
                DrumLane.KICK,
            ),
            DrumLane.entries,
        )
        assertEquals((0..7).toList(), DrumLane.entries.map(DrumLane::index))
        assertEquals(
            listOf(
                "Chimbal",
                "Prato de ataque",
                "Prato de condução",
                "Snare",
                "Tom 1",
                "Tom 2",
                "Surdo",
                "Bumbo",
            ),
            DrumLane.entries.map(DrumLane::displayName),
        )
    }
}
