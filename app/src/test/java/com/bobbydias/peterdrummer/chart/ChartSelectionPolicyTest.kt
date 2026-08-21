package com.bobbydias.peterdrummer.chart

import com.bobbydias.peterdrummer.game.DemoChart
import com.bobbydias.peterdrummer.core.RhythmChart
import com.bobbydias.peterdrummer.core.RhythmEvent
import com.bobbydias.peterdrummer.core.DrumLane
import org.junit.Assert.assertEquals
import org.junit.Test

class ChartSelectionPolicyTest {
    @Test
    fun realMusicHidesCalibrationFromManualAndRandomChoices() {
        val calibration = ChartLibraryItem(
            DemoChart.create(),
            ChartOrigin.BUILT_IN,
            "Calibração",
            null,
        )
        val demo = ChartLibraryItem(
            RhythmChart(
                id = "ampularia_demo",
                title = "Valsa da Ampularia",
                artist = "Bobby Dias",
                durationMs = 2_000,
                events = listOf(RhythmEvent(1_000, DrumLane.SNARE)),
            ),
            ChartOrigin.BUILT_IN,
            "Demo interna",
            android.net.Uri.EMPTY,
        )
        val snapshot = ChartLibrarySnapshot(
            items = listOf(calibration, demo),
            unchartedSongs = emptyList(),
            musicFileCount = 0,
            bundledMusicCount = 1,
            extraChartCount = 0,
            problems = emptyList(),
        )

        assertEquals(listOf("ampularia_demo"), snapshot.selectionItems.map { it.chart.id })
        assertEquals(listOf("ampularia_demo"), snapshot.playableChoices.map { it.chart.id })
    }
}
