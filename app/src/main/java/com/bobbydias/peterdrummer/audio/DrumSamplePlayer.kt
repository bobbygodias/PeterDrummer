package com.bobbydias.peterdrummer.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Handler
import android.os.Looper
import com.bobbydias.peterdrummer.core.DrumLane

/**
 * Low-overhead sample trigger used while the final multi-velocity audio engine
 * is being prepared. Missing resources are deliberately silent, so the source
 * can compile before the approved WAV pack is added.
 */
class DrumSamplePlayer(private val context: Context) {
    private val handler = Handler(Looper.getMainLooper())
    private val soundPool = SoundPool.Builder()
        .setMaxStreams(24)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()

    private val sounds: Map<DrumLane, Int> = DrumLane.entries.mapNotNull { lane ->
        val resourceId = context.resources.getIdentifier(
            lane.sampleResourceName,
            "raw",
            context.packageName,
        )
        if (resourceId == 0) null else lane to soundPool.load(context, resourceId, 1)
    }.toMap()

    var volume: Float = 0.85f
        set(value) {
            field = value.coerceIn(0f, 1f)
        }

    fun play(lane: DrumLane, velocity: Int = 110) {
        val soundId = sounds[lane] ?: return
        val velocityGain = (velocity.coerceIn(1, 127) / 127f).coerceAtLeast(0.18f)
        val gain = volume * velocityGain
        soundPool.play(soundId, gain, gain, 1, 0, 1f)
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
}
