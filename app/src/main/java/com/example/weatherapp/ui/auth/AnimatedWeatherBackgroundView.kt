package com.example.weatherapp.ui.auth

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.SystemClock
import android.util.AttributeSet
import android.view.View
import kotlin.math.sin

class AnimatedWeatherBackgroundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttribute: Int = 0
) : View(
    context,
    attrs,
    defStyleAttribute
) {

    private val cloudPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = dp(1.5f)
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            alpha = 65
        }

    private val particlePaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            alpha = 45
        }

    private val glowPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = dp(1f)
            alpha = 25
        }

    private var animationStartTime =
        SystemClock.uptimeMillis()

    private var animationRunning =
        false

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        animationStartTime =
            SystemClock.uptimeMillis()

        animationRunning = true

        setLayerType(
            LAYER_TYPE_HARDWARE,
            null
        )

        postInvalidateOnAnimation()
    }

    override fun onDetachedFromWindow() {
        animationRunning = false

        super.onDetachedFromWindow()
    }

    override fun onVisibilityChanged(
        changedView: View,
        visibility: Int
    ) {
        super.onVisibilityChanged(
            changedView,
            visibility
        )

        if (visibility == VISIBLE) {
            animationStartTime =
                SystemClock.uptimeMillis()

            animationRunning = true

            postInvalidateOnAnimation()
        }
    }

    override fun onDraw(
        canvas: Canvas
    ) {
        super.onDraw(canvas)

        if (
            width <= 0 ||
            height <= 0
        ) {
            return
        }

        val elapsedSeconds =
            (
                    SystemClock.uptimeMillis() -
                            animationStartTime
                    ) / 1000f

        drawWeatherGlow(
            canvas = canvas,
            progress = elapsedSeconds
        )

        drawWeatherParticles(
            canvas = canvas,
            progress = elapsedSeconds
        )

        drawLargeCloud(
            canvas = canvas,
            progress = elapsedSeconds
        )

        drawSmallCloud(
            canvas = canvas,
            progress = elapsedSeconds
        )

        drawMiddleCloud(
            canvas = canvas,
            progress = elapsedSeconds
        )

        if (
            animationRunning &&
            isShown
        ) {
            postInvalidateOnAnimation()
        }
    }

    private fun drawLargeCloud(
        canvas: Canvas,
        progress: Float
    ) {
        val cloudWidth =
            dp(140f)

        val travelWidth =
            width + cloudWidth

        val x =
            (
                    progress * dp(18f)
                    ) % travelWidth -
                    cloudWidth

        val verticalMovement =
            sin(
                progress * 0.8f
            ) * dp(7f)

        val y =
            height * 0.18f +
                    verticalMovement

        drawCloud(
            canvas = canvas,
            x = x,
            y = y,
            scale = 1.20f,
            paint = cloudPaint
        )
    }

    private fun drawSmallCloud(
        canvas: Canvas,
        progress: Float
    ) {
        val cloudWidth =
            dp(100f)

        val travelWidth =
            width + cloudWidth

        val x =
            width -
                    (
                            progress * dp(12f)
                            ) % travelWidth

        val verticalMovement =
            sin(
                progress * 0.65f + 2f
            ) * dp(6f)

        val y =
            height * 0.70f +
                    verticalMovement

        drawCloud(
            canvas = canvas,
            x = x,
            y = y,
            scale = 0.78f,
            paint = cloudPaint
        )
    }

    private fun drawMiddleCloud(
        canvas: Canvas,
        progress: Float
    ) {
        val cloudWidth =
            dp(115f)

        val travelWidth =
            width + cloudWidth

        val x =
            (
                    progress * dp(9f) +
                            width * 0.35f
                    ) % travelWidth -
                    cloudWidth

        val verticalMovement =
            sin(
                progress * 0.5f + 4f
            ) * dp(5f)

        val y =
            height * 0.45f +
                    verticalMovement

        drawCloud(
            canvas = canvas,
            x = x,
            y = y,
            scale = 0.60f,
            paint = cloudPaint
        )
    }

    private fun drawCloud(
        canvas: Canvas,
        x: Float,
        y: Float,
        scale: Float,
        paint: Paint
    ) {
        val path =
            Path()

        path.moveTo(
            x,
            y + dp(34f) * scale
        )

        path.cubicTo(
            x - dp(2f) * scale,
            y + dp(20f) * scale,

            x + dp(8f) * scale,
            y + dp(11f) * scale,

            x + dp(23f) * scale,
            y + dp(11f) * scale
        )

        path.cubicTo(
            x + dp(29f) * scale,
            y - dp(5f) * scale,

            x + dp(51f) * scale,
            y - dp(8f) * scale,

            x + dp(64f) * scale,
            y + dp(7f) * scale
        )

        path.cubicTo(
            x + dp(84f) * scale,
            y + dp(2f) * scale,

            x + dp(101f) * scale,
            y + dp(17f) * scale,

            x + dp(98f) * scale,
            y + dp(34f) * scale
        )

        path.cubicTo(
            x + dp(96f) * scale,
            y + dp(45f) * scale,

            x + dp(87f) * scale,
            y + dp(50f) * scale,

            x + dp(75f) * scale,
            y + dp(50f) * scale
        )

        path.lineTo(
            x + dp(23f) * scale,
            y + dp(50f) * scale
        )

        path.cubicTo(
            x + dp(10f) * scale,
            y + dp(50f) * scale,

            x,
            y + dp(44f) * scale,

            x,
            y + dp(34f) * scale
        )

        canvas.drawPath(
            path,
            paint
        )
    }

    private fun drawWeatherParticles(
        canvas: Canvas,
        progress: Float
    ) {
        val particleCount =
            22

        repeat(particleCount) { index ->
            val columnWidth =
                width /
                        particleCount.toFloat()

            val baseX =
                columnWidth * index +
                        dp(8f)

            val verticalRange =
                height + dp(60f)

            val speed =
                dp(
                    14f +
                            index % 7
                )

            val y =
                (
                        progress * speed +
                                index * dp(53f)
                        ) % verticalRange -
                        dp(30f)

            val horizontalMovement =
                sin(
                    progress * 1.1f +
                            index * 0.7f
                ) * dp(10f)

            val radius =
                when {
                    index % 5 == 0 ->
                        dp(1.8f)

                    index % 2 == 0 ->
                        dp(1.3f)

                    else ->
                        dp(0.9f)
                }

            particlePaint.alpha =
                when {
                    index % 5 == 0 -> 55
                    index % 2 == 0 -> 42
                    else -> 30
                }

            canvas.drawCircle(
                baseX + horizontalMovement,
                y,
                radius,
                particlePaint
            )
        }
    }

    private fun drawWeatherGlow(
        canvas: Canvas,
        progress: Float
    ) {
        val breathingEffect =
            sin(
                progress * 1.1f
            ) * dp(8f)

        val horizontalMovement =
            sin(
                progress * 0.25f
            ) * dp(12f)

        val radius =
            dp(58f) +
                    breathingEffect

        val centerX =
            width * 0.82f +
                    horizontalMovement

        val centerY =
            height * 0.12f

        canvas.drawCircle(
            centerX,
            centerY,
            radius,
            glowPaint
        )

        canvas.drawCircle(
            centerX,
            centerY,
            radius + dp(15f),
            glowPaint
        )

        canvas.drawCircle(
            centerX,
            centerY,
            radius + dp(30f),
            glowPaint
        )
    }

    private fun dp(
        value: Float
    ): Float {
        return value *
                resources
                    .displayMetrics
                    .density
    }
}