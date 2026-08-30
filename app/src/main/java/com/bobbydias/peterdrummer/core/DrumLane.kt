package com.bobbydias.peterdrummer.core

/**
 * The eight fixed playable parts of the Peter Drummer kit.
 *
 * Lane indexes are part of the chart file contract. Do not reorder them after
 * charts are published; change colors or labels instead.
 */
enum class DrumLane(
    val index: Int,
    val chartId: String,
    val displayName: String,
    val colorArgb: Int,
    val sampleResourceName: String,
) {
    SNARE(0, "snare", "Caixa", 0xFFE53935.toInt(), "drum_snare"),
    HIGH_TOM(1, "tom1", "Tom 1", 0xFFFB8C00.toInt(), "drum_tom_high"),
    MID_TOM(2, "tom2", "Tom 2", 0xFFFDD835.toInt(), "drum_tom_mid"),
    FLOOR_TOM(3, "floor_tom", "Surdo", 0xFF43A047.toInt(), "drum_floor_tom"),
    KICK(4, "kick", "Bumbo", 0xFF00ACC1.toInt(), "drum_kick"),
    HI_HAT(5, "hi_hat", "Chimbal", 0xFF1E88E5.toInt(), "drum_hihat"),
    CRASH(6, "crash", "Prato de ataque", 0xFFD81B60.toInt(), "drum_crash"),
    RIDE(7, "ride", "Prato de condução", 0xFF8E24AA.toInt(), "drum_ride"),
    ;

    companion object {
        fun fromIndex(index: Int): DrumLane =
            entries.firstOrNull { it.index == index }
                ?: throw IllegalArgumentException("Invalid drum lane index: $index")

        fun fromChartId(chartId: String): DrumLane =
            entries.firstOrNull { it.chartId == chartId.lowercase() }
                ?: throw IllegalArgumentException("Invalid drum lane id: $chartId")
    }
}
