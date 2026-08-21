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
    KICK(0, "Bumbo", 0xFFE53935.toInt(), "drum_kick"),
    SNARE(1, "Caixa", 0xFFFB8C00.toInt(), "drum_snare"),
    HI_HAT(2, "Chimbal", 0xFFFDD835.toInt(), "drum_hihat"),
    HIGH_TOM(3, "Tom agudo", 0xFF43A047.toInt(), "drum_tom_high"),
    MID_TOM(4, "Tom médio", 0xFF00ACC1.toInt(), "drum_tom_mid"),
    FLOOR_TOM(5, "Surdo", 0xFF1E88E5.toInt(), "drum_floor_tom"),
    CRASH(6, "Crash", 0xFFD81B60.toInt(), "drum_crash"),
    RIDE(7, "Condução", 0xFF8E24AA.toInt(), "drum_ride"),
    ;

    companion object {
        fun fromIndex(index: Int): DrumLane =
            entries.firstOrNull { it.index == index }
                ?: throw IllegalArgumentException("Invalid drum lane index: $index")
    }
}
