package com.bobbydias.peterdrummer.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Handler
import android.os.Looper
import com.bobbydias.peterdrummer.core.DrumArticulation
import com.bobbydias.peterdrummer.core.DrumLane

/**
 * Velocity-layered, round-robin drum player used by the first audible build.
 *
 * The packaged Opus assets are preprocessed with headroom and capped cymbal
 * tails so they stay inside SoundPool's per-sample decoded-memory ceiling.
 * A later native mixer can consume the same catalog without changing charts.
 */
class DrumSamplePlayer(private val context: Context) {
    private val handler = Handler(Looper.getMainLooper())
    private val loadedSoundIds = mutableSetOf<Int>()
    private val soundIds = mutableMapOf<DrumSampleKey, Int>()
    private val roundRobin = mutableMapOf<Pair<DrumLane, DrumArticulation>, Int>()
    private var activeOpenHiHatStream = 0

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(32)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()
        .also { pool ->
            pool.setOnLoadCompleteListener { _, sampleId, status ->
                if (status == 0) loadedSoundIds += sampleId
            }
        }

    init {
        loadCatalog()
    }

    var volume: Float = 0.85f
        set(value) {
            field = value.coerceIn(0f, 1f)
        }

    fun play(
        lane: DrumLane,
        velocity: Int = 110,
        articulation: DrumArticulation = DrumArticulation.STANDARD,
    ) {
        val effectiveArticulation = DrumSampleCatalog.effectiveArticulation(lane, articulation)
        if (lane == DrumLane.HI_HAT && effectiveArticulation != DrumArticulation.HI_HAT_OPEN) {
            chokeOpenHiHat()
        }

        val layerCount = DrumSampleCatalog.layerCount(lane, effectiveArticulation)
        val layer = (((velocity.coerceIn(1, 127) - 1) * layerCount) / 127 + 1)
            .coerceIn(1, layerCount)
        val roundRobinKey = lane to effectiveArticulation
        val variant = (roundRobin.getOrDefault(roundRobinKey, 0) % DrumSampleCatalog.variants) + 1
        roundRobin[roundRobinKey] = variant

        val key = DrumSampleKey(lane, layer, variant, effectiveArticulation)
        val soundId = soundIds[key] ?: return
        if (soundId !in loadedSoundIds) return

        val velocityGain = (velocity.coerceIn(1, 127) / 127f).coerceAtLeast(0.22f)
        val gain = volume * velocityGain
        val streamId = soundPool.play(soundId, gain, gain, 1, 0, 1f)
        if (lane == DrumLane.HI_HAT && effectiveArticulation == DrumArticulation.HI_HAT_OPEN) {
            activeOpenHiHatStream = streamId
        }
    }

    fun playMenuClick() = play(DrumLane.SNARE, 118)

    fun playStartFill(onFinished: () -> Unit) {
        val sequence = listOf(
            0L to DrumLane.HIGH_TOM,
            90L to DrumLane.MID_TOM,
            180L to DrumLane.FLOOR_TOM,
            290L to DrumLane.KICK,
            290L to DrumLane.CRASH,
        )
        sequence.forEach { (delayMs, lane) -> handler.postDelayed({ play(lane, 120) }, delayMs) }
        handler.postDelayed(onFinished, 430L)
    }

    fun release() {
        handler.removeCallbacksAndMessages(null)
        soundPool.release()
    }

    private fun loadCatalog() {
        DrumSampleCatalog.entries.forEach(::load)
    }

    private fun load(key: DrumSampleKey) {
        val path = DrumSampleCatalog.assetPath(key)
        runCatching {
            context.assets.openFd(path).use { descriptor ->
                soundPool.load(descriptor, 1)
            }
        }.onSuccess { soundId -> soundIds[key] = soundId }
    }

    private fun chokeOpenHiHat() {
        if (activeOpenHiHatStream == 0) return
        soundPool.setVolume(activeOpenHiHatStream, 0f, 0f)
        soundPool.stop(activeOpenHiHatStream)
        activeOpenHiHatStream = 0
    }

}
