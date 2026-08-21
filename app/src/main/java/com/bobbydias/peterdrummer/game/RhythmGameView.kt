package com.bobbydias.peterdrummer.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import com.bobbydias.peterdrummer.core.DrumLane
import com.bobbydias.peterdrummer.core.GameMode
import com.bobbydias.peterdrummer.core.HitJudgement
import com.bobbydias.peterdrummer.core.JudgementEngine
import com.bobbydias.peterdrummer.core.PlayResult
import com.bobbydias.peterdrummer.core.RhythmChart
import com.bobbydias.peterdrummer.core.RhythmEvent

class RhythmGameView(
    context: Context,
    private val chart: RhythmChart,
    private val mode: GameMode,
    private val onDrumHit: (DrumLane, Int) -> Unit,
    private val onFinished: (PlayResult) -> Unit,
) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val engine = JudgementEngine(chart.events)
    private val pressedUntil = LongArray(DrumLane.entries.size)
    private val autoTriggered = BooleanArray(chart.events.size)
    private val resolvedEvents = mutableSetOf<RhythmEvent>()
    private val peterPerformance = PeterPerformanceState()
    private var startUptimeMs = 0L
    private var finished = false
    private var lastJudgement: HitJudgement? = null
    private var lastFeedbackUntil = 0L

    private val approachTimeMs = 3_200L
    private val preparationProgress = 0.58f
    private val spawnYRatio = 0.30f
    private val hitYRatio = 0.82f
    private val buttonYRatio = 0.92f

    init {
        isFocusable = true
        isClickable = true
        keepScreenOn = true
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startUptimeMs = SystemClock.uptimeMillis() + 650L
        postInvalidateOnAnimation()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val now = SystemClock.uptimeMillis()
        val songTimeMs = (now - startUptimeMs).coerceAtLeast(0L)
        drawStage(canvas)
        drawHeader(canvas, now)
        drawTrack(canvas)
        triggerAutomaticEvents(songTimeMs)
        drawNotes(canvas, songTimeMs)
        drawButtons(canvas, now)
        drawFeedback(canvas, now)

        if (!finished && songTimeMs >= chart.durationMs) {
            finished = true
            onFinished(engine.result(chart.durationMs + 1_000L))
        } else if (!finished) {
            engine.markExpired(songTimeMs)
            postInvalidateOnAnimation()
        }
    }

    private fun drawStage(canvas: Canvas) {
        paint.shader = LinearGradient(
            0f,
            0f,
            0f,
            height.toFloat(),
            intArrayOf(0xFF080706.toInt(), 0xFF17100D.toInt(), 0xFF050404.toInt()),
            null,
            Shader.TileMode.CLAMP,
        )
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        paint.shader = null

        val spotlight = Path().apply {
            moveTo(width * 0.45f, 0f)
            lineTo(width * 0.20f, height * 0.43f)
            lineTo(width * 0.80f, height * 0.43f)
            lineTo(width * 0.55f, 0f)
            close()
        }
        paint.color = 0x18FFD39B
        canvas.drawPath(spotlight, paint)
    }

    private fun drawHeader(canvas: Canvas, now: Long) {
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = android.graphics.Typeface.DEFAULT_BOLD
        paint.color = 0xFFF2E9DD.toInt()
        paint.textSize = height * 0.027f
        canvas.drawText(chart.title, width / 2f, height * 0.050f, paint)
        paint.typeface = android.graphics.Typeface.DEFAULT
        paint.color = 0xFFB9A898.toInt()
        paint.textSize = height * 0.017f
        canvas.drawText(chart.artist, width / 2f, height * 0.078f, paint)

        peterPerformance.activeLanes(now).takeIf { it.isNotEmpty() }?.let { lanes ->
            paint.typeface = android.graphics.Typeface.DEFAULT_BOLD
            paint.color = lanes.last().colorArgb
            paint.textSize = height * 0.021f
            val pieces = lanes.joinToString(" + ") { it.displayName }
            canvas.drawText("Peter toca: $pieces", width / 2f, height * 0.235f, paint)
        }
    }

    private fun drawTrack(canvas: Canvas) {
        val topY = height * spawnYRatio
        val bottomY = height * 0.875f
        val leftTop = width * 0.42f
        val rightTop = width * 0.58f
        val leftBottom = width * 0.018f
        val rightBottom = width * 0.982f

        val track = Path().apply {
            moveTo(leftTop, topY)
            lineTo(rightTop, topY)
            lineTo(rightBottom, bottomY)
            lineTo(leftBottom, bottomY)
            close()
        }
        paint.color = 0xC80B0B0C.toInt()
        canvas.drawPath(track, paint)

        for (boundary in 0..DrumLane.entries.size) {
            val fraction = boundary / DrumLane.entries.size.toFloat()
            val topX = lerp(leftTop, rightTop, fraction)
            val bottomX = lerp(leftBottom, rightBottom, fraction)
            paint.color = if (boundary in 1 until DrumLane.entries.size) {
                DrumLane.fromIndex(boundary - 1).colorArgb
            } else {
                0xFF8F8176.toInt()
            }
            paint.alpha = if (boundary in 1 until DrumLane.entries.size) 160 else 220
            paint.strokeWidth = if (boundary in 1 until DrumLane.entries.size) 2.4f else 4f
            canvas.drawLine(topX, topY, bottomX, bottomY, paint)
        }
        paint.alpha = 255

        val preparationY = lerp(topY, height * hitYRatio, preparationProgress)
        paint.color = 0xFFFFA45B.toInt()
        paint.strokeWidth = 4.5f
        canvas.drawLine(
            trackLeftAtY(preparationY),
            preparationY,
            trackRightAtY(preparationY),
            preparationY,
            paint,
        )
        paint.textAlign = Paint.Align.LEFT
        paint.typeface = android.graphics.Typeface.DEFAULT_BOLD
        paint.textSize = height * 0.014f
        canvas.drawText("PREPARA", trackLeftAtY(preparationY) + 8f, preparationY - 10f, paint)

        val hitY = height * hitYRatio
        paint.color = 0xBFFFFFFF.toInt()
        paint.strokeWidth = 2f
        canvas.drawLine(trackLeftAtY(hitY), hitY, trackRightAtY(hitY), hitY, paint)
    }

    private fun drawNotes(canvas: Canvas, songTimeMs: Long) {
        val topY = height * spawnYRatio
        val hitY = height * hitYRatio
        chart.events.forEach { event ->
            if (event in resolvedEvents) return@forEach
            val spawnTime = event.timeMs - approachTimeMs
            val progress = (songTimeMs - spawnTime).toFloat() / approachTimeMs
            if (progress !in 0f..1.08f) return@forEach

            val clamped = progress.coerceIn(0f, 1f)
            val y = lerp(topY, hitY, clamped)
            val laneWidth = (trackRightAtY(y) - trackLeftAtY(y)) / DrumLane.entries.size
            val centerX = trackLeftAtY(y) + laneWidth * (event.lane.index + 0.5f)
            val radiusX = (laneWidth * 0.34f).coerceAtLeast(5f)
            val radiusY = (radiusX * 0.50f).coerceAtLeast(4f)

            paint.color = event.lane.colorArgb
            paint.style = Paint.Style.FILL
            canvas.drawOval(
                centerX - radiusX,
                y - radiusY,
                centerX + radiusX,
                y + radiusY,
                paint,
            )
            paint.style = Paint.Style.STROKE
            paint.color = Color.WHITE
            paint.strokeWidth = 1.5f
            canvas.drawOval(
                centerX - radiusX,
                y - radiusY,
                centerX + radiusX,
                y + radiusY,
                paint,
            )
            paint.style = Paint.Style.FILL
        }
    }

    private fun drawButtons(canvas: Canvas, now: Long) {
        val buttonY = height * buttonYRatio
        val laneWidth = width / DrumLane.entries.size.toFloat()
        DrumLane.entries.forEach { lane ->
            val centerX = laneWidth * (lane.index + 0.5f)
            val pressed = pressedUntil[lane.index] > now
            val radius = laneWidth * if (pressed) 0.43f else 0.37f
            paint.color = if (pressed) Color.WHITE else lane.colorArgb
            paint.setShadowLayer(if (pressed) 24f else 10f, 0f, 0f, lane.colorArgb)
            canvas.drawCircle(centerX, buttonY, radius, paint)
            paint.clearShadowLayer()
            paint.color = 0xFF151313.toInt()
            canvas.drawCircle(centerX, buttonY, radius * 0.62f, paint)
            paint.color = lane.colorArgb
            canvas.drawCircle(centerX, buttonY, radius * 0.48f, paint)
        }
    }

    private fun drawFeedback(canvas: Canvas, now: Long) {
        if (now > lastFeedbackUntil) return
        val judgement = lastJudgement ?: return
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = android.graphics.Typeface.DEFAULT_BOLD
        paint.textSize = height * 0.030f
        paint.color = when (judgement) {
            HitJudgement.PERFECT -> 0xFFFFD166.toInt()
            HitJudgement.GREAT -> 0xFF7EE081.toInt()
            HitJudgement.GOOD -> 0xFF72B7FF.toInt()
            HitJudgement.MISS -> 0xFFE85D5D.toInt()
        }
        canvas.drawText(judgement.name, width / 2f, height * 0.145f, paint)
    }

    private fun triggerAutomaticEvents(songTimeMs: Long) {
        if (mode != GameMode.DEMO) return
        chart.events.forEachIndexed { index, event ->
            if (!autoTriggered[index] && songTimeMs >= event.timeMs) {
                autoTriggered[index] = true
                engine.hit(event.lane, event.timeMs)
                resolvedEvents += event
                onDrumHit(event.lane, event.velocity)
                showLaneFeedback(event.lane, HitJudgement.PERFECT)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (mode == GameMode.DEMO || finished) return true
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_POINTER_DOWN
            -> {
                val pointerIndex = event.actionIndex
                val laneIndex = ((event.getX(pointerIndex) / width) * DrumLane.entries.size)
                    .toInt()
                    .coerceIn(0, DrumLane.entries.lastIndex)
                val lane = DrumLane.fromIndex(laneIndex)
                val songTimeMs = (SystemClock.uptimeMillis() - startUptimeMs).coerceAtLeast(0L)
                val judged = engine.hit(lane, songTimeMs)
                judged.event?.let(resolvedEvents::add)
                onDrumHit(lane, judged.event?.velocity ?: 100)
                showLaneFeedback(lane, judged.judgement)
                performClick()
            }
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun showLaneFeedback(lane: DrumLane, judgement: HitJudgement) {
        val now = SystemClock.uptimeMillis()
        pressedUntil[lane.index] = now + 95L
        peterPerformance.strike(lane, now)
        lastJudgement = judgement
        lastFeedbackUntil = now + 420L
        invalidate()
    }

    private fun trackLeftAtY(y: Float): Float {
        val fraction = ((y / height) - spawnYRatio) / (0.875f - spawnYRatio)
        return lerp(width * 0.42f, width * 0.018f, fraction.coerceIn(0f, 1f))
    }

    private fun trackRightAtY(y: Float): Float {
        val fraction = ((y / height) - spawnYRatio) / (0.875f - spawnYRatio)
        return lerp(width * 0.58f, width * 0.982f, fraction.coerceIn(0f, 1f))
    }

    private fun lerp(start: Float, end: Float, fraction: Float): Float =
        start + (end - start) * fraction
}
