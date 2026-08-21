package com.bobbydias.peterdrummer.core

/**
 * The eight fixed playable parts of the Peter Drummer kit.
 *
 * Lane indexes are part of the chart file contract. Do not reorder them after
 * charts are published; change colors or labels instead.
 */
enum class DrumLane(
    val index: Int,
    val displayName: String,
    val colorArgb: Int,
    val sampleResourceName: String,
) {
    HI_HAT(0, "Chimbal", 0xFFE53935.toInt(), "drum_hihat"),
    CRASH(1, "Prato de ataque", 0xFFFB8C00.toInt(), "drum_crash"),
    RIDE(2, "Prato de condução", 0xFFFDD835.toInt(), "drum_ride"),
    SNARE(3, "Snare", 0xFF43A047.toInt(), "drum_snare"),
    HIGH_TOM(4, "Tom 1", 0xFF00ACC1.toInt(), "drum_tom_high"),
    MID_TOM(5, "Tom 2", 0xFF1E88E5.toInt(), "drum_tom_mid"),
    FLOOR_TOM(6, "Surdo", 0xFFD81B60.toInt(), "drum_floor_tom"),
    KICK(7, "Bumbo", 0xFF8E24AA.toInt(), "drum_kick"),
    ;

    companion object {
        fun fromIndex(index: Int): DrumLane =
            entries.firstOrNull { it.index == index }
                ?: throw IllegalArgumentException("Invalid drum lane index: $index")
    }
}
