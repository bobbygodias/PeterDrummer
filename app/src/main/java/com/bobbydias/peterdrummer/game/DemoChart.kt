package com.bobbydias.peterdrummer.game

import com.bobbydias.peterdrummer.core.DrumLane
import com.bobbydias.peterdrummer.core.DrumArticulation
import com.bobbydias.peterdrummer.core.RhythmChart
import com.bobbydias.peterdrummer.core.RhythmEvent

object DemoChart {
    fun create(): RhythmChart {
        val events = buildList {
            var time = 2_200L
            repeat(8) { bar ->
                add(RhythmEvent(time, DrumLane.KICK, 118))
                add(
                    RhythmEvent(
                        time,
                        if (bar % 2 == 0) DrumLane.HI_HAT else DrumLane.RIDE,
                        94,
                        if (bar % 2 == 0) DrumArticulation.HI_HAT_CLOSED else DrumArticulation.STANDARD,
                    ),
                )
                add(RhythmEvent(time + 500, DrumLane.SNARE, 120))
                add(RhythmEvent(time + 500, DrumLane.HI_HAT, 90, DrumArticulation.HI_HAT_CLOSED))
                add(RhythmEvent(time + 1_000, DrumLane.KICK, 112))
                add(
                    RhythmEvent(
                        time + 1_000,
                        DrumLane.HI_HAT,
                        104,
                        if (bar % 2 == 0) DrumArticulation.HI_HAT_OPEN else DrumArticulation.HI_HAT_CLOSED,
                    ),
                )
                add(RhythmEvent(time + 1_500, DrumLane.SNARE, 122))
                add(
                    RhythmEvent(
                        time + 1_500,
                        DrumLane.HI_HAT,
                        92,
                        if (bar % 2 == 0) DrumArticulation.HI_HAT_PEDAL else DrumArticulation.HI_HAT_CLOSED,
                    ),
                )
                time += 2_000
            }
            add(RhythmEvent(time, DrumLane.KICK, 124))
            add(RhythmEvent(time, DrumLane.CRASH, 126))
            add(RhythmEvent(time + 350, DrumLane.HIGH_TOM, 116))
            add(RhythmEvent(time + 650, DrumLane.MID_TOM, 118))
            add(RhythmEvent(time + 950, DrumLane.FLOOR_TOM, 122))
            add(RhythmEvent(time + 1_300, DrumLane.KICK, 127))
            add(RhythmEvent(time + 1_300, DrumLane.CRASH, 127))
        }.sortedBy { it.timeMs }

        return RhythmChart(
            id = "engine_calibration",
            title = "Calibração das oito pistas",
            artist = "Peter Drummer",
            durationMs = 21_000L,
            events = events,
        )
    }
}
