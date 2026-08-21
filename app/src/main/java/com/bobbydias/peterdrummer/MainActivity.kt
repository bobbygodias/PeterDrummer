package com.bobbydias.peterdrummer

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
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
import com.bobbydias.peterdrummer.core.DrumLane
import com.bobbydias.peterdrummer.core.GameMode
import com.bobbydias.peterdrummer.core.PlayResult
import com.bobbydias.peterdrummer.game.DemoChart
import com.bobbydias.peterdrummer.game.RhythmGameView
import com.bobbydias.peterdrummer.storage.RankingStore
import com.bobbydias.peterdrummer.storage.SongFolderStore
import java.io.File

class MainActivity : Activity() {
    private var samplePlayer: DrumSamplePlayer? = null
    private val settingsStore by lazy { SongFolderStore(this) }
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
        content.addView(actionButton("MODO ALEATÓRIO") { startCalibration(GameMode.RANDOM) })
        content.addView(space(14))
        content.addView(actionButton("ESCOLHA AÊ, FERA") { showSongList() })
        content.addView(space(14))
        content.addView(actionButton("SÓ QUER CURTIR?") { startCalibration(GameMode.DEMO) })
        content.addView(space(28))
        content.addView(smallButton("CONFIGURAR") { showSettings() })
        setContentView(stageRoot(content))
    }

    private fun showSongList() {
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(24), dp(48), dp(24), dp(36))
        }
        content.addView(title("ESCOLHA AÊ, FERA", 30f))
        content.addView(space(20))
        content.addView(label("FAIXA-LABORATÓRIO", 14f, muted = true))
        content.addView(space(8))
        content.addView(label("Pearl Jam — Even Flow", 20f))
        content.addView(space(8))
        content.addView(label("Partitura e bateria isolada em alinhamento.", 14f, muted = true))
        content.addView(space(20))
        content.addView(actionButton("CALIBRAÇÃO DAS OITO PISTAS") { startCalibration(GameMode.CHOOSE) })
        content.addView(space(30))
        content.addView(smallButton("VOLTAR") { showModeMenu() })

        val scroll = ScrollView(this).apply { addView(content) }
        setContentView(stageRoot(scroll))
    }

    private fun startCalibration(mode: GameMode) {
        val chart = DemoChart.create()
        val game = RhythmGameView(
            context = this,
            chart = chart,
            mode = mode,
            onDrumHit = { lane, velocity, articulation ->
                samplePlayer?.play(lane, velocity, articulation)
            },
            onFinished = { result ->
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
        content.addView(actionButton("MODO ALEATÓRIO") { startCalibration(GameMode.RANDOM) })
        content.addView(space(12))
        content.addView(actionButton("ESCOLHA AÊ, FERA") { showSongList() })
        content.addView(space(12))
        content.addView(actionButton("SÓ QUER CURTIR?") { startCalibration(GameMode.DEMO) })
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

    private fun showSettings() {
        val content = centeredColumn()
        content.addView(title("CONFIGURAR", 32f))
        content.addView(space(24))

        val folderLabel = label(folderDescription(), 15f, muted = true)
        content.addView(folderLabel)
        content.addView(space(12))
        content.addView(actionButton("ESCOLHER PASTA DAS MÚSICAS") { chooseSongFolder() })
        content.addView(space(28))

        content.addView(label("Volume da música", 16f))
        content.addView(volumeSlider(settingsStore.songVolume) { settingsStore.songVolume = it })
        content.addView(space(18))
        content.addView(label("Volume da bateria", 16f))
        content.addView(volumeSlider(settingsStore.drumVolume) {
            settingsStore.drumVolume = it
            samplePlayer?.volume = it
        })
        content.addView(space(34))
        content.addView(smallButton("VOLTAR") { showModeMenu() })
        setContentView(stageRoot(content))
    }

    private fun chooseSongFolder() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION,
            )
        }
        startActivityForResult(intent, REQUEST_SONG_FOLDER)
    }

    @Deprecated("Kept for Android 9 compatibility without an additional activity dependency")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQUEST_SONG_FOLDER || resultCode != RESULT_OK) return
        val uri = data?.data ?: return
        val flags = (data?.flags ?: 0) and Intent.FLAG_GRANT_READ_URI_PERMISSION
        runCatching { contentResolver.takePersistableUriPermission(uri, flags) }
        settingsStore.folderUri = uri
        samplePlayer?.playMenuClick()
        showSettings()
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

    private fun folderDescription(): String = settingsStore.folderUri?.let {
        "Pasta selecionada: ${it.lastPathSegment ?: it}"
    } ?: "Nenhuma pasta de músicas selecionada"

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
        private const val MATCH = ViewGroup.LayoutParams.MATCH_PARENT
        private const val WRAP = ViewGroup.LayoutParams.WRAP_CONTENT
    }
}
