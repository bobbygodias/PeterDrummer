package com.bobbydias.peterdrummer.audio

import com.bobbydias.peterdrummer.core.DrumArticulation
import com.bobbydias.peterdrummer.core.DrumLane

data class DrumSampleKey(
    val lane: DrumLane,
    val layer: Int,
    val variant: Int,
    val articulation: DrumArticulation,
)

object DrumSampleCatalog {
    const val variants = 3

    val entries: List<DrumSampleKey> = buildList {
        DrumLane.entries.forEach { lane ->
            if (lane == DrumLane.HI_HAT) {
                addLayered(lane, DrumArticulation.HI_HAT_CLOSED, 5)
                addLayered(lane, DrumArticulation.HI_HAT_OPEN, 5)
                repeat(variants) { variant ->
                    add(
                        DrumSampleKey(
                            lane = lane,
                            layer = 1,
                            variant = variant + 1,
                            articulation = DrumArticulation.HI_HAT_PEDAL,
                        ),
                    )
                }
            } else {
                addLayered(lane, DrumArticulation.STANDARD, layerCount(lane))
            }
        }
    }

    fun effectiveArticulation(
        lane: DrumLane,
        articulation: DrumArticulation,
    ): DrumArticulation = when {
        lane != DrumLane.HI_HAT -> DrumArticulation.STANDARD
        articulation == DrumArticulation.STANDARD -> DrumArticulation.HI_HAT_CLOSED
        else -> articulation
    }

    fun layerCount(lane: DrumLane, articulation: DrumArticulation? = null): Int =
        if (articulation == DrumArticulation.HI_HAT_PEDAL) {
            1
        } else {
            when (lane) {
                DrumLane.KICK, DrumLane.SNARE -> 6
                else -> 5
            }
        }

    fun assetPath(key: DrumSampleKey): String {
        if (key.lane == DrumLane.HI_HAT) {
            val articulationFolder = when (key.articulation) {
                DrumArticulation.HI_HAT_OPEN -> "open"
                DrumArticulation.HI_HAT_PEDAL -> "pedal"
                else -> "closed"
            }
            return if (key.articulation == DrumArticulation.HI_HAT_PEDAL) {
                "drums/hihat/$articulationFolder/v${key.variant}.opus"
            } else {
                "drums/hihat/$articulationFolder/l${key.layer}_v${key.variant}.opus"
            }
        }
        return "drums/${laneFolder(key.lane)}/l${key.layer}_v${key.variant}.opus"
    }

    private fun MutableList<DrumSampleKey>.addLayered(
        lane: DrumLane,
        articulation: DrumArticulation,
        layers: Int,
    ) {
        repeat(layers) { layer ->
            repeat(variants) { variant ->
                add(DrumSampleKey(lane, layer + 1, variant + 1, articulation))
            }
        }
    }

    private fun laneFolder(lane: DrumLane): String = when (lane) {
        DrumLane.CRASH -> "crash"
        DrumLane.RIDE -> "ride"
        DrumLane.SNARE -> "snare"
        DrumLane.HIGH_TOM -> "tom1"
        DrumLane.MID_TOM -> "tom2"
        DrumLane.FLOOR_TOM -> "tom3"
        DrumLane.KICK -> "kick"
        DrumLane.HI_HAT -> "hihat"
    }
}
