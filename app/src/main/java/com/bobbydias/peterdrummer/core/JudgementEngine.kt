package com.bobbydias.peterdrummer.core

import kotlin.math.abs

enum class HitJudgement(val scoreValue: Int) {
    PERFECT(1000),
    GREAT(750),
    GOOD(500),
    MISS(0),
}

data class JudgedHit(
    val event: RhythmEvent?,
    val judgement: HitJudgement,
    val timingErrorMs: Long?,
)

data class PlayResult(
    val totalEvents: Int,
    val hits: Int,
    val score: Int,
    val maximumCombo: Int,
) {
    val accuracy: Double = if (totalEvents == 0) 0.0 else hits.toDouble() / totalEvents

    val message: String
        get() = when {
            accuracy <= 1.0 / 3.0 -> "Precisa Praticar Mais, Cara!"
            accuracy <= 0.60 -> "É, Tá Quase Lá, Mano!"
            else -> "Cara, Você Toca Pra Caramba!"
        }
}

/**
 * Deterministic scoring against an authored temporal chart.
 * It never attempts to infer drum notes from the backing track.
 */
class JudgementEngine(
    events: List<RhythmEvent>,
    private val perfectWindowMs: Long = 55L,
    private val greatWindowMs: Long = 110L,
    private val goodWindowMs: Long = 170L,
) {
    private val pending = events.mapIndexed { index, event -> PendingEvent(index, event) }.toMutableList()
    private var score = 0
    private var hits = 0
    private var combo = 0
    private var maximumCombo = 0

    fun hit(lane: DrumLane, songTimeMs: Long): JudgedHit {
        markExpired(songTimeMs)
        val candidate = pending
            .asSequence()
            .filter { !it.resolved && it.event.lane == lane }
            .map { it to abs(songTimeMs - it.event.timeMs) }
            .filter { (_, error) -> error <= goodWindowMs }
            .minByOrNull { (_, error) -> error }

        if (candidate == null) {
            combo = 0
            return JudgedHit(null, HitJudgement.MISS, null)
        }

        val (pendingEvent, error) = candidate
        pendingEvent.resolved = true
        val judgement = when {
            error <= perfectWindowMs -> HitJudgement.PERFECT
            error <= greatWindowMs -> HitJudgement.GREAT
            else -> HitJudgement.GOOD
        }
        score += judgement.scoreValue
        hits += 1
        combo += 1
        maximumCombo = maxOf(maximumCombo, combo)
        return JudgedHit(pendingEvent.event, judgement, songTimeMs - pendingEvent.event.timeMs)
    }

    fun markExpired(songTimeMs: Long): Int {
        var misses = 0
        pending.forEach { item ->
            if (!item.resolved && songTimeMs - item.event.timeMs > goodWindowMs) {
                item.resolved = true
                misses += 1
                combo = 0
            }
        }
        return misses
    }

    fun result(songTimeMs: Long = Long.MAX_VALUE): PlayResult {
        markExpired(songTimeMs)
        return PlayResult(
            totalEvents = pending.size,
            hits = hits,
            score = score,
            maximumCombo = maximumCombo,
        )
    }

    private data class PendingEvent(
        val index: Int,
        val event: RhythmEvent,
        var resolved: Boolean = false,
    )
}
