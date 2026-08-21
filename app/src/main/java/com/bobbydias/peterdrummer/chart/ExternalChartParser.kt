package com.bobbydias.peterdrummer.chart

import com.bobbydias.peterdrummer.core.ChartValidator
import com.bobbydias.peterdrummer.core.DrumArticulation
import com.bobbydias.peterdrummer.core.DrumLane
import com.bobbydias.peterdrummer.core.RhythmChart
import com.bobbydias.peterdrummer.core.RhythmEvent
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.InputStream

object ExternalChartParser {
    private const val MAX_CHART_BYTES = 5 * 1024 * 1024
    private const val MAX_EVENTS = 200_000

    fun parse(input: InputStream): RhythmChart {
        val root = JSONObject(readUtf8(input))
        require(root.optInt("schemaVersion", 0) == 1) { "Unsupported chart schema" }
        val eventArray = root.getJSONArray("events")
        require(eventArray.length() in 1..MAX_EVENTS) { "Invalid chart event count" }
        val events = buildList(eventArray.length()) {
            repeat(eventArray.length()) { index ->
                val item = eventArray.getJSONObject(index)
                add(
                    RhythmEvent(
                        timeMs = item.getLong("timeMs"),
                        lane = DrumLane.fromChartId(item.getString("lane")),
                        velocity = item.optInt("velocity", 100),
                        articulation = articulation(item.optString("articulation", "standard")),
                    ),
                )
            }
        }.sortedBy(RhythmEvent::timeMs)
        val audioNames = root.optJSONArray("audioFileNames")?.let { names ->
            buildList(names.length()) {
                repeat(names.length()) { index -> add(names.getString(index)) }
            }
        }.orEmpty()
        val chart = RhythmChart(
            id = root.getString("id"),
            title = root.getString("title"),
            artist = root.getString("artist"),
            durationMs = root.getLong("durationMs"),
            audioOffsetMs = root.optLong("audioOffsetMs", 0L),
            audioFileNames = audioNames,
            events = events,
        )
        val problems = ChartValidator.validate(chart)
        require(problems.isEmpty()) { problems.joinToString { it.message } }
        return chart
    }

    private fun articulation(value: String): DrumArticulation = when (value.lowercase()) {
        "standard" -> DrumArticulation.STANDARD
        "hi_hat_closed", "closed" -> DrumArticulation.HI_HAT_CLOSED
        "hi_hat_open", "open" -> DrumArticulation.HI_HAT_OPEN
        "hi_hat_pedal", "pedal" -> DrumArticulation.HI_HAT_PEDAL
        else -> throw IllegalArgumentException("Invalid articulation: $value")
    }

    private fun readUtf8(input: InputStream): String {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(16 * 1024)
        var total = 0
        while (true) {
            val read = input.read(buffer)
            if (read < 0) break
            total += read
            require(total <= MAX_CHART_BYTES) { "Chart file is too large" }
            output.write(buffer, 0, read)
        }
        return output.toString(Charsets.UTF_8.name())
    }
}
