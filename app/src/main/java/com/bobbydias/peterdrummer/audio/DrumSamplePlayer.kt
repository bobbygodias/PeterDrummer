package com.bobbydias.peterdrummer.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Handler
import android.os.Looper
import com.bobbydias.peterdrummer.core.DrumArticulation
import com.bobbydias.peterdrummer.core.DrumLane
import java.util.ArrayDeque

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
    private val pendingLoads = ArrayDeque<DrumSampleKey>()
    private var activeOpenHiHatStream = 0
    private var loadingStarted = false
    private var released = false

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
                handler.post(::loadNext)
            }
        }

    var volume: Float = 0.85f
        set(value) {
            field = value.coerceIn(0f, 1f)
        }

    /**
     * Begins a single-file-at-a-time preload after the Activity has rendered.
     *
     * Loading the complete 144-sample kit inside Activity.onCreate launched a
     * burst of native decoders before Android could draw the first frame. Some
     * tablets terminate the process in that situation. Sequential loading keeps
     * startup responsive and still warms the complete kit in the background.
     */
    fun startLoading() {
        if (loadingStarted || released) return
        loadingStarted = true
        pendingLoads.addAll(prioritizedCatalog())
        handler.post(::loadNext)
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

        val key = DrumSampleKey(lane, layer, variant, effectiveArticulation)
        val soundId = soundIds[key] ?: return
        if (soundId !in loadedSoundIds) return
        roundRobin[roundRobinKey] = variant

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
        released = true
        handler.removeCallbacksAndMessages(null)
        pendingLoads.clear()
        soundPool.release()
    }

    private fun prioritizedCatalog(): List<DrumSampleKey> {
        val warmUp = buildList {
            repeat(DrumSampleCatalog.variants) { index ->
                val variant = index + 1
                add(DrumSampleKey(DrumLane.SNARE, 6, variant, DrumArticulation.STANDARD))
                add(DrumSampleKey(DrumLane.HI_HAT, 5, variant, DrumArticulation.HI_HAT_CLOSED))
                add(DrumSampleKey(DrumLane.HI_HAT, 5, variant, DrumArticulation.HI_HAT_OPEN))
                add(DrumSampleKey(DrumLane.HI_HAT, 1, variant, DrumArticulation.HI_HAT_PEDAL))
                add(DrumSampleKey(DrumLane.CRASH, 5, variant, DrumArticulation.STANDARD))
                add(DrumSampleKey(DrumLane.RIDE, 5, variant, DrumArticulation.STANDARD))
                add(DrumSampleKey(DrumLane.HIGH_TOM, 5, variant, DrumArticulation.STANDARD))
                add(DrumSampleKey(DrumLane.MID_TOM, 5, variant, DrumArticulation.STANDARD))
                add(DrumSampleKey(DrumLane.FLOOR_TOM, 5, variant, DrumArticulation.STANDARD))
                add(DrumSampleKey(DrumLane.KICK, 6, variant, DrumArticulation.STANDARD))
            }
        }
        val warmSet = warmUp.toSet()
        return warmUp + DrumSampleCatalog.entries.filterNot(warmSet::contains)
    }

    private fun loadNext() {
        if (released) return
        val key = pendingLoads.pollFirst() ?: return
        val path = DrumSampleCatalog.assetPath(key)
        runCatching {
            context.assets.openFd(path).use { descriptor ->
                soundPool.load(descriptor, 1)
            }
        }.onSuccess { soundId ->
            if (soundId != 0) {
                soundIds[key] = soundId
            } else {
                handler.post(::loadNext)
            }
        }.onFailure {
            handler.post(::loadNext)
        }
    }

    private fun chokeOpenHiHat() {
        if (activeOpenHiHatStream == 0) return
        soundPool.setVolume(activeOpenHiHatStream, 0f, 0f)
        soundPool.stop(activeOpenHiHatStream)
        activeOpenHiHatStream = 0
    }

}
