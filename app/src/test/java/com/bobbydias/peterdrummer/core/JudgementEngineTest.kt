package com.bobbydias.peterdrummer.core

import org.junit.Assert.assertEquals
import org.junit.Test

class JudgementEngineTest {
    @Test
    fun scoresTheNearestEventOnTheCorrectLane() {
        val engine = JudgementEngine(
            listOf(
                RhythmEvent(1_000, DrumLane.SNARE),
                RhythmEvent(1_080, DrumLane.KICK),
            ),
        )

        val hit = engine.hit(DrumLane.SNARE, 1_035)

        assertEquals(HitJudgement.PERFECT, hit.judgement)
        assertEquals(1, engine.result(1_100).hits)
    }

    @Test
    fun resultMessagesFollowTheApprovedThreeBands() {
        assertEquals("Precisa Praticar Mais, Cara!", PlayResult(6, 2, 0, 0).message)
        assertEquals("É, Tá Quase Lá, Mano!", PlayResult(10, 6, 0, 0).message)
        assertEquals("Cara, Você Toca Pra Caramba!", PlayResult(10, 7, 0, 0).message)
    }
}
