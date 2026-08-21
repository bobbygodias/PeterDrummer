package com.bobbydias.peterdrummer

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.VideoView
import com.bobbydias.peterdrummer.audio.DrumSamplePlayer
import com.bobbydias.peterdrummer.chart.ChartLibrary
import com.bobbydias.peterdrummer.chart.ChartLibraryItem
import com.bobbydias.peterdrummer.chart.ChartLibrarySnapshot
import com.bobbydias.peterdrummer.core.ChartDifficultyAnalyzer
import com.bobbydias.peterdrummer.core.DrumLane
import com.bobbydias.peterdrummer.core.GameMode
import com.bobbydias.peterdrummer.core.PlayResult
import com.bobbydias.peterdrummer.game.RhythmGameView
import com.bobbydias.peterdrummer.storage.RankingStore
import com.bobbydias.peterdrummer.storage.SongFolderStore
import java.io.File

class MainActivity : Activity() {
    private var samplePlayer: DrumSamplePlayer? = null
    private var backingPlayer: MediaPlayer? = null
    private val settingsStore by lazy { SongFolderStore(this) }
    private val chartLibrary by lazy { ChartLibrary(this, settingsStore) }
    private val rankingStore by lazy { RankingStore(this) }
    private var latestResult: PlayResult? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val safeRoot = FrameLayout(this).apply { setBackgroundColor(Color.BLACK) }
        setContentView(safeRoot)
        safeRoot.post {
            makeImmersive()
            showIntroOrHome()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) makeImmersive()
    }

    override fun onDestroy() {
        backingPlayer?.release()
        samplePlayer?.release()
        super.onDestroy()
    }

    private fun showIntroOrHome() {
        val root = FrameLayout(this).apply { setBackgroundColor(Color.BLACK) }
        val video = VideoView(this).apply {
            setOnPreparedListener { player ->
                player.isLooping = false
                start()
            }
            setOnCompletionListener { showHome() }
            setOnErrorListener { _, what, extra ->
                showHome()
                true
            }
        }
        root.addView(video, FrameLayout.LayoutParams(MATCH, WRAP, Gravity.CENTER))

        val skip = TextView(this).apply {
            text = "Toque para pular"
            setTextColor(0xB3FFFFFF.toInt())
            textSize = 14f
            gravity = Gravity.CENTER
            setPadding(dp(18), dp(10), dp(18), dp(10))
        }
        root.addView(
            skip,
            FrameLayout.LayoutParams(WRAP, WRAP, Gravity.BOTTOM or Gravity.END).apply {
                setMargins(0, 0, dp(14), dp(24))
            },
        )
        root.setOnClickListener {
            video.stopPlayback()
            showHome()
        }
        setContentView(root)

        // Resolve/copy private media only after Android has received the first
        // real view. A missing or unreadable intro must fall back to Home.
        root.post {
            runCatching {
                val introId = resources.getIdentifier("peter_drummer_intro", "raw", packageName)
                val introUri = if (introId != 0) {
                    Uri.parse("android.resource://$packageName/$introId")
                } else {
                    privateIntroFile()?.let(Uri::fromFile)
                }
                if (introUri == null) {
                    showHome()
                } else {
                    video.setVideoURI(introUri)
                }
            }.onFailure {
                showHome()
            }
        }
    }

    private fun privateIntroFile(): File? = runCatching {
        val destination = File(cacheDir, PRIVATE_INTRO_FILENAME)
        assets.open(PRIVATE_INTRO_ASSET).use { input ->
            destination.outputStream().use(input::copyTo)
        }
        destination
    }.getOrNull()

    private fun showHome() {
        val content = centeredColumn()
        content.addView(title("PETER\nDRUMMER", 46f))
        content.addView(space(20))
        content.addView(label("O palco é seu. A bateria também.", 17f, muted = true))
        content.addView(space(54))
        content.addView(actionButton("CLIQUE AQUI E VAMOS ARREBENTAR!") {
            samplePlayer?.playStartFill { showModeMenu() } ?: showModeMenu()
        })
        content.addView(space(18))
        content.addView(smallButton("CONFIGURAR MÚSICAS E PARTITURAS") { showSettings() })
        val root = stageRoot(content)
        setContentView(root)
        root.post { ensureAudioEngine() }
    }

    private fun ensureAudioEngine() {
        if (samplePlayer != null) return
        runCatching {
            DrumSamplePlayer(this).also { player ->
                player.volume = settingsStore.drumVolume
                player.startLoading()
            }
        }.onSuccess { player -> samplePlayer = player }
    }

    private fun showModeMenu() {
        val content = centeredColumn()
        content.addView(title("ESCOLHE AÊ", 34f))
        content.addView(space(30))
        content.addView(actionButton("MODO ALEATÓRIO") { startRandomChart(GameMode.RANDOM) })
        content.addView(space(14))
        content.addView(actionButton("ESCOLHA AÊ, FERA") { showSongList() })
        content.addView(space(14))
        content.addView(actionButton("SÓ QUER CURTIR?") { startRandomChart(GameMode.DEMO) })
        val scroll = ScrollView(this).apply {
            isFillViewport = true
            addView(content)
        }
        setContentView(stageRoot(scroll))
    }

    private fun showSongList() {
        showLoading("LENDO PARTITURAS")
        loadChartLibrary { snapshot -> renderSongList(snapshot) }
    }

    private fun renderSongList(snapshot: ChartLibrarySnapshot) {
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(24), dp(48), dp(24), dp(36))
        }
        content.addView(title("ESCOLHA AÊ, FERA", 30f))
        content.addView(space(20))
        content.addView(label(
            "${snapshot.musicFileCount} músicas • ${snapshot.extraChartCount} partituras extras",
            14f,
            muted = true,
        ))
        content.addView(space(20))
        snapshot.items.forEach { item ->
            val difficulty = ChartDifficultyAnalyzer.analyze(item.chart)
            content.addView(label("${item.chart.artist} — ${item.chart.title}", 18f))
            content.addView(label(
                "Nível ${difficulty.level}: ${difficulty.label} • ${item.sourceName}",
                13f,
                muted = true,
            ))
            val button = actionButton(
                if (item.canPlay) "TOCAR" else "MÚSICA NÃO ENCONTRADA",
            ) { startChart(item, GameMode.CHOOSE) }
            button.isEnabled = item.canPlay
            button.alpha = if (item.canPlay) 1f else 0.45f
            content.addView(space(8))
            content.addView(button)
            content.addView(space(20))
        }
        snapshot.problems.firstOrNull()?.let {
            content.addView(label(it, 13f, muted = true))
            content.addView(space(12))
        }
        content.addView(space(30))
        content.addView(smallButton("VOLTAR") { showModeMenu() })

        val scroll = ScrollView(this).apply { addView(content) }
        setContentView(stageRoot(scroll))
    }

    private fun startRandomChart(mode: GameMode) {
        showLoading("MONTANDO O PALCO")
        loadChartLibrary { snapshot ->
            val choices = snapshot.playableItems.filterNot(ChartLibraryItem::isCalibration)
                .ifEmpty { snapshot.playableItems }
            choices.randomOrNull()?.let { startChart(it, mode) }
                ?: showCatalogEmpty(snapshot)
        }
    }

    private fun startChart(item: ChartLibraryItem, mode: GameMode) {
        backingPlayer?.release()
        backingPlayer = null
        val musicUri = item.musicUri
        if (musicUri == null) {
            launchGame(item, mode, null)
            return
        }
        showLoading("PREPARANDO ${item.chart.title.uppercase()}")
        runCatching {
            MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build(),
                )
                setDataSource(this@MainActivity, musicUri)
                setVolume(settingsStore.songVolume, settingsStore.songVolume)
                setOnPreparedListener { player ->
                    backingPlayer = player
                    launchGame(item, mode) { player.start() }
                }
                setOnErrorListener { player, _, _ ->
                    player.release()
                    backingPlayer = null
                    showCatalogError("A música foi encontrada, mas o Android não conseguiu abri-la.")
                    true
                }
                prepareAsync()
            }
        }.onFailure {
            showCatalogError("A música foi encontrada, mas o acesso à pasta não permaneceu válido.")
        }
    }

    private fun launchGame(item: ChartLibraryItem, mode: GameMode, startMusic: (() -> Unit)?) {
        val chart = item.chart
        val game = RhythmGameView(
            context = this,
            chart = chart,
            mode = mode,
            onSongStart = { startMusic?.invoke() },
            onDrumHit = { lane, velocity, articulation ->
                samplePlayer?.play(lane, velocity, articulation)
            },
            onFinished = { result ->
                backingPlayer?.runCatching { stop() }
                backingPlayer?.release()
                backingPlayer = null
                latestResult = result
                runOnUiThread { showResults(result) }
            },
        )
        setContentView(game)
    }

    private fun showResults(result: PlayResult) {
        val content = centeredColumn()
        val message = title(result.message, 30f).apply {
            alpha = 0f
            scaleX = 0.84f
            scaleY = 0.84f
            animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(520L).start()
        }
        content.addView(message)
        content.addView(space(18))
        content.addView(label("${result.score} pontos  •  ${(result.accuracy * 100).toInt()}% de acertos", 17f))
        content.addView(space(38))
        content.addView(actionButton("MODO ALEATÓRIO") { startRandomChart(GameMode.RANDOM) })
        content.addView(space(12))
        content.addView(actionButton("ESCOLHA AÊ, FERA") { showSongList() })
        content.addView(space(12))
        content.addView(actionButton("SÓ QUER CURTIR?") { startRandomChart(GameMode.DEMO) })
        content.addView(space(12))
        content.addView(actionButton("POR HOJE JÁ DEU!") { showRankingExit(result) })
        setContentView(stageRoot(content))
    }

    private fun showRankingExit(result: PlayResult) {
        val content = centeredColumn()
        content.addView(title("AS CINCO MAIORES", 30f))
        content.addView(space(18))
        content.addView(rankingTable(rankingStore.topFive()))
        content.addView(space(26))

        val name = EditText(this).apply {
            hint = "Seu nome"
            setTextColor(0xFFF2E9DD.toInt())
            setHintTextColor(0xFF897A6D.toInt())
            textSize = 18f
            gravity = Gravity.CENTER
            maxLines = 1
            background = roundedBackground(0xFF241813.toInt(), 0xFFD29A5B.toInt(), 12f)
            setPadding(dp(18), dp(12), dp(18), dp(12))
        }
        content.addView(name, LinearLayout.LayoutParams(MATCH, WRAP))
        content.addView(space(14))
        content.addView(actionButton("SALVAR E FECHAR") {
            rankingStore.add(name.text.toString(), result.score)
            finishAndRemoveTask()
        })
        setContentView(stageRoot(content))
    }

    private fun showSettings(status: String? = null) {
        val content = centeredColumn()
        content.addView(title("CONFIGURAR", 32f))
        content.addView(space(24))

        status?.let {
            content.addView(label(it, 14f))
            content.addView(space(18))
        }

        content.addView(label(folderDescription("Músicas", settingsStore.songFolderUri), 15f, muted = true))
        content.addView(space(12))
        content.addView(actionButton("ESCOLHER PASTA DAS MÚSICAS") {
            chooseFolder(REQUEST_SONG_FOLDER)
        })
        content.addView(space(20))

        content.addView(label(
            folderDescription("Partituras extras", settingsStore.chartFolderUri),
            15f,
            muted = true,
        ))
        content.addView(space(12))
        content.addView(actionButton("ESCOLHER PASTA DE PARTITURAS EXTRAS") {
            chooseFolder(REQUEST_CHART_FOLDER)
        })
        content.addView(space(26))

        content.addView(label("Volume da música", 16f))
        content.addView(volumeSlider(settingsStore.songVolume) { settingsStore.songVolume = it })
        content.addView(space(18))
        content.addView(label("Volume da bateria", 16f))
        content.addView(volumeSlider(settingsStore.drumVolume) {
            settingsStore.drumVolume = it
            samplePlayer?.volume = it
        })
        content.addView(space(34))
        content.addView(smallButton("VOLTAR") { showHome() })
        val scroll = ScrollView(this).apply {
            isFillViewport = true
            addView(content)
        }
        setContentView(stageRoot(scroll))
    }

    private fun chooseFolder(requestCode: Int) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                    Intent.FLAG_GRANT_PREFIX_URI_PERMISSION or
                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION,
            )
        }
        startActivityForResult(intent, requestCode)
    }

    @Deprecated("Kept for Android 9 compatibility without an additional activity dependency")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode !in setOf(REQUEST_SONG_FOLDER, REQUEST_CHART_FOLDER) || resultCode != RESULT_OK) {
            return
        }
        val resultData = data ?: return
        val uri = resultData.data ?: return
        val grantedFlags = (resultData.flags and (
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )).let { flags ->
            if (flags and Intent.FLAG_GRANT_READ_URI_PERMISSION == 0) {
                flags or Intent.FLAG_GRANT_READ_URI_PERMISSION
            } else {
                flags
            }
        }
        runCatching { contentResolver.takePersistableUriPermission(uri, grantedFlags) }
        if (!hasPersistedReadAccess(uri)) {
            showSettings("O Android não concedeu acesso permanente. Escolha a pasta novamente.")
            return
        }
        val folderName = if (requestCode == REQUEST_SONG_FOLDER) {
            settingsStore.songFolderUri = uri
            "Pasta de músicas fixada."
        } else {
            settingsStore.chartFolderUri = uri
            "Pasta de partituras extras fixada."
        }
        samplePlayer?.playMenuClick()
        showSettings(folderName)
    }

    private fun showLoading(message: String) {
        val content = centeredColumn()
        content.addView(title(message, 30f))
        content.addView(space(16))
        content.addView(label("Só um instante...", 15f, muted = true))
        setContentView(stageRoot(content))
    }

    private fun loadChartLibrary(onReady: (ChartLibrarySnapshot) -> Unit) {
        Thread({
            val snapshot = chartLibrary.load()
            runOnUiThread {
                if (!isFinishing && !isDestroyed) onReady(snapshot)
            }
        }, "peter-chart-library").start()
    }

    private fun showCatalogEmpty(snapshot: ChartLibrarySnapshot) {
        val content = centeredColumn()
        content.addView(title("NENHUMA MÚSICA PRONTA", 28f))
        content.addView(space(18))
        val reason = snapshot.problems.firstOrNull()
            ?: "Escolha a pasta das músicas. As partituras internas já vêm no aplicativo; a pasta extra é opcional."
        content.addView(label(reason, 16f, muted = true))
        content.addView(space(28))
        content.addView(actionButton("VOLTAR À PRIMEIRA TELA") { showHome() })
        setContentView(stageRoot(content))
    }

    private fun showCatalogError(message: String) {
        val content = centeredColumn()
        content.addView(title("NÃO DEU PRA ABRIR", 28f))
        content.addView(space(18))
        content.addView(label(message, 16f, muted = true))
        content.addView(space(28))
        content.addView(actionButton("VOLTAR À PRIMEIRA TELA") { showHome() })
        setContentView(stageRoot(content))
    }

    private fun rankingTable(entries: List<com.bobbydias.peterdrummer.storage.RankingEntry>): View {
        val column = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        if (entries.isEmpty()) {
            column.addView(label("O palco ainda está vazio.", 16f, muted = true))
        } else {
            entries.forEachIndexed { index, entry ->
                column.addView(label("${index + 1}º  ${entry.name}  —  ${entry.score}", 18f))
                column.addView(space(8))
            }
        }
        return column
    }

    private fun volumeSlider(initialValue: Float, onChanged: (Float) -> Unit): SeekBar =
        SeekBar(this).apply {
            max = 100
            progress = (initialValue * 100).toInt()
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) onChanged(progress / 100f)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
                override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
            })
        }

    private fun stageRoot(content: View): FrameLayout = FrameLayout(this).apply {
        background = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(0xFF090706.toInt(), 0xFF23140F.toInt(), 0xFF090706.toInt()),
        )
        addView(content, FrameLayout.LayoutParams(MATCH, MATCH))
    }

    private fun centeredColumn(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER
        setPadding(dp(24), dp(44), dp(24), dp(36))
    }

    private fun title(value: String, sizeSp: Float): TextView = TextView(this).apply {
        text = value
        setTextColor(0xFFF2E9DD.toInt())
        textSize = sizeSp
        gravity = Gravity.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        letterSpacing = 0.025f
    }

    private fun label(value: String, sizeSp: Float, muted: Boolean = false): TextView =
        TextView(this).apply {
            text = value
            setTextColor(if (muted) 0xFFB9A898.toInt() else 0xFFF2E9DD.toInt())
            textSize = sizeSp
            gravity = Gravity.CENTER
        }

    private fun actionButton(
        value: String,
        playClick: Boolean = true,
        action: () -> Unit,
    ): Button = Button(this).apply {
        text = value
        setTextColor(0xFFF8F0E8.toInt())
        textSize = 16f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAllCaps = false
        background = roundedBackground(0xFF351B14.toInt(), 0xFFD29A5B.toInt(), 16f)
        isSoundEffectsEnabled = false
        setPadding(dp(18), dp(14), dp(18), dp(14))
        setOnClickListener {
            if (playClick) samplePlayer?.playMenuClick()
            action()
        }
    }

    private fun smallButton(value: String, action: () -> Unit): Button =
        actionButton(value, action = action).apply {
            textSize = 14f
            alpha = 0.90f
        }

    private fun roundedBackground(fill: Int, stroke: Int, radiusDp: Float): GradientDrawable =
        GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(radiusDp.toInt()).toFloat()
            setColor(fill)
            setStroke(dp(1), stroke)
        }

    private fun space(heightDp: Int): View = View(this).apply {
        layoutParams = LinearLayout.LayoutParams(1, dp(heightDp))
    }

    private fun folderDescription(label: String, uri: Uri?): String = when {
        uri == null -> "$label: nenhuma pasta selecionada"
        hasPersistedReadAccess(uri) -> "$label: ${uri.lastPathSegment ?: uri}"
        else -> "$label: acesso expirou; selecione novamente"
    }

    private fun hasPersistedReadAccess(uri: Uri): Boolean =
        contentResolver.persistedUriPermissions.any { permission ->
            permission.uri == uri && permission.isReadPermission
        }

    private fun makeImmersive() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.apply {
                hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    companion object {
        private const val PRIVATE_INTRO_ASSET = "private/peter_drummer_intro.mp4"
        private const val PRIVATE_INTRO_FILENAME = "peter_drummer_intro.mp4"
        private const val REQUEST_SONG_FOLDER = 1701
        private const val REQUEST_CHART_FOLDER = 1702
        private const val MATCH = ViewGroup.LayoutParams.MATCH_PARENT
        private const val WRAP = ViewGroup.LayoutParams.WRAP_CONTENT
    }
}
