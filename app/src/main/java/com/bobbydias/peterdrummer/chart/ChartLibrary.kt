package com.bobbydias.peterdrummer.chart

import android.content.Context
import android.net.Uri
import com.bobbydias.peterdrummer.core.RhythmChart
import com.bobbydias.peterdrummer.game.DemoChart
import com.bobbydias.peterdrummer.storage.DocumentTreeScanner
import com.bobbydias.peterdrummer.storage.SongFolderStore
import com.bobbydias.peterdrummer.storage.TreeDocument
import java.text.Normalizer

enum class ChartOrigin { BUILT_IN, EXTRA }

data class ChartLibraryItem(
    val chart: RhythmChart,
    val origin: ChartOrigin,
    val sourceName: String,
    val musicUri: Uri?,
) {
    val isCalibration: Boolean get() = chart.id == "engine_calibration"
    val canPlay: Boolean get() = isCalibration || musicUri != null
}

data class ChartLibrarySnapshot(
    val items: List<ChartLibraryItem>,
    val musicFileCount: Int,
    val extraChartCount: Int,
    val problems: List<String>,
) {
    val playableItems: List<ChartLibraryItem> get() = items.filter(ChartLibraryItem::canPlay)
}

class ChartLibrary(
    private val context: Context,
    private val settings: SongFolderStore,
) {
    private val scanner = DocumentTreeScanner(context)

    fun load(): ChartLibrarySnapshot {
        val problems = mutableListOf<String>()
        val songs = scanSongs(problems)
        val charts = scanBuiltInCharts(problems).toMutableList()
        charts += scanExtraCharts(problems).map { it.first to (ChartOrigin.EXTRA to it.second) }
        val seenIds = mutableSetOf<String>()
        val items = charts.mapNotNull { (chart, originAndName) ->
            if (!seenIds.add(chart.id)) {
                problems += "Partitura repetida ignorada: ${chart.id}"
                return@mapNotNull null
            }
            ChartLibraryItem(
                chart = chart,
                origin = originAndName.first,
                sourceName = originAndName.second,
                musicUri = matchMusic(chart, songs)?.uri,
            )
        }
        return ChartLibrarySnapshot(
            items = items,
            musicFileCount = songs.size,
            extraChartCount = items.count { it.origin == ChartOrigin.EXTRA },
            problems = problems,
        )
    }

    private fun scanBuiltInCharts(
        problems: MutableList<String>,
    ): List<Pair<RhythmChart, Pair<ChartOrigin, String>>> = buildList {
        add(DemoChart.create() to (ChartOrigin.BUILT_IN to "Partitura interna de calibração"))
        val names = runCatching { context.assets.list(BUILT_IN_CHART_FOLDER).orEmpty() }
            .onFailure { problems += "Não foi possível ler as partituras internas." }
            .getOrDefault(emptyArray())
        names.filter { it.lowercase().endsWith(CHART_EXTENSION) }.sorted().forEach { name ->
            runCatching {
                context.assets.open("$BUILT_IN_CHART_FOLDER/$name").use(ExternalChartParser::parse)
            }.onSuccess { chart ->
                add(chart to (ChartOrigin.BUILT_IN to "Partitura interna"))
            }.onFailure {
                problems += "Partitura interna inválida: $name"
            }
        }
    }

    private fun scanSongs(problems: MutableList<String>): List<TreeDocument> {
        val tree = settings.songFolderUri ?: return emptyList()
        return runCatching { scanner.list(tree).filter { document -> document.isAudio() } }
            .onFailure { problems += "Não foi possível reler a pasta de músicas." }
            .getOrDefault(emptyList())
    }

    private fun scanExtraCharts(problems: MutableList<String>): List<Pair<RhythmChart, String>> {
        val tree = settings.chartFolderUri ?: return emptyList()
        val documents = runCatching { scanner.list(tree) }
            .onFailure { problems += "Não foi possível reler a pasta de partituras extras." }
            .getOrDefault(emptyList())
        return documents.filter { it.name.lowercase().endsWith(CHART_EXTENSION) }.mapNotNull { document ->
            runCatching {
                val input = requireNotNull(context.contentResolver.openInputStream(document.uri))
                input.use(ExternalChartParser::parse) to document.name
            }.onFailure {
                problems += "Partitura inválida ignorada: ${document.name}"
            }.getOrNull()
        }
    }

    private fun matchMusic(chart: RhythmChart, songs: List<TreeDocument>): TreeDocument? {
        val declared = chart.audioFileNames.map(::normalized).filter(String::isNotBlank).toSet()
        if (declared.isNotEmpty()) {
            songs.firstOrNull { normalized(it.name) in declared }?.let { return it }
        }
        val identity = normalized("${chart.artist} ${chart.title}")
        return songs.firstOrNull { song ->
            val candidate = normalized(song.name)
            identity.isNotBlank() && (candidate.contains(identity) || identity.contains(candidate))
        }
    }

    private fun TreeDocument.isAudio(): Boolean {
        val lower = name.lowercase()
        return mimeType.startsWith("audio/") ||
            lower.endsWith(".mp3") || lower.endsWith(".wav") ||
            lower.endsWith(".ogg") || lower.endsWith(".opus") || lower.endsWith(".m4a")
    }

    private fun normalized(value: String): String = Normalizer.normalize(value, Normalizer.Form.NFD)
        .replace("\\p{M}+".toRegex(), "")
        .lowercase()
        .replace("drumless", "")
        .replace("without drums", "")
        .replace("backing track", "")
        .replace("[^a-z0-9]+".toRegex(), " ")
        .trim()

    companion object {
        private const val BUILT_IN_CHART_FOLDER = "charts"
        private const val CHART_EXTENSION = ".pdrum.json"
    }
}
