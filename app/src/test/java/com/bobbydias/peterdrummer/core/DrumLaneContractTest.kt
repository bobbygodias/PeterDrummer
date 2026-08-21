package com.bobbydias.peterdrummer.core

import org.junit.Assert.assertEquals
import org.junit.Test

class DrumLaneContractTest {
    @Test
    fun canonicalKitHasExactlyEightPiecesInTheApprovedOrder() {
        assertEquals(
            listOf(
                DrumLane.SNARE,
                DrumLane.HIGH_TOM,
                DrumLane.MID_TOM,
                DrumLane.FLOOR_TOM,
                DrumLane.KICK,
                DrumLane.HI_HAT,
                DrumLane.CRASH,
                DrumLane.RIDE,
            ),
            DrumLane.entries,
        )
        assertEquals((0..7).toList(), DrumLane.entries.map(DrumLane::index))
        assertEquals(
            listOf(
                "Caixa",
                "Tom 1",
                "Tom 2",
                "Surdo",
                "Bumbo",
                "Chimbal",
                "Prato de ataque",
                "Prato de condução",
            ),
            DrumLane.entries.map(DrumLane::displayName),
        )
        assertEquals(
            listOf("snare", "tom1", "tom2", "floor_tom", "kick", "hi_hat", "crash", "ride"),
            DrumLane.entries.map(DrumLane::chartId),
        )
    }
}
