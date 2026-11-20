package com.aquaflow.waterreminder.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

/**
 * An elegant, modern, minimal water glass illustration with dynamic fluid simulation:
 * - Fluid water level corresponding to daily hydration progress
 * - Surface wave motion that becomes more animated during pours
 * - Graceful water pouring stream with splash ripples
 * - Frosted glass outline with specular reflections
 */
@Composable
fun WaterGlassVessel(
    progress: Float,
    isPouring: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 95.dp
) {
    // Ambient gentle wave motion
    val infiniteTransition = rememberInfiniteTransition(label = "WaveTransition")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "WavePhase"
    )

    // Smoothly animated fill level (0f to 1f)
    val animatedLevel by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "WaterLevelAnimation"
    )

    // Pour stream animation controls
    val streamAlpha = remember { Animatable(0f) }
    val streamProgress = remember { Animatable(0f) }
    val waveIntensity = remember { Animatable(2.5f) }

    LaunchedEffect(isPouring) {
        if (isPouring) {
            waveIntensity.animateTo(6f, tween(160, easing = FastOutSlowInEasing))
            streamProgress.snapTo(0f)
            streamAlpha.snapTo(1f)
            streamProgress.animateTo(1f, tween(700, easing = LinearOutSlowInEasing))
            streamAlpha.animateTo(0f, tween(250, easing = LinearEasing))
            waveIntensity.animateTo(2.5f, tween(450, easing = FastOutSlowInEasing))
        } else {
            streamAlpha.snapTo(0f)
            streamProgress.snapTo(0f)
            waveIntensity.snapTo(2.5f)
        }
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val canvasWidth = this.size.width
            val canvasHeight = this.size.height

            // Glass dimensions
            val glassMarginH = canvasWidth * 0.18f
            val glassTop = canvasHeight * 0.18f
            val glassBottom = canvasHeight * 0.94f
            val glassLeft = glassMarginH
            val glassRight = canvasWidth - glassMarginH
            val glassWidth = glassRight - glassLeft
            val glassHeight = glassBottom - glassTop
            val cornerRadius = glassWidth * 0.22f

            // Create glass interior clip path
            val glassPath = Path().apply {
                addRoundRect(
                    RoundRect(
                        left = glassLeft,
                        top = glassTop,
                        right = glassRight,
                        bottom = glassBottom,
                        bottomLeftCornerRadius = CornerRadius(cornerRadius, cornerRadius),
                        bottomRightCornerRadius = CornerRadius(cornerRadius, cornerRadius),
                        topLeftCornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                        topRightCornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                )
            }

            // Draw frosted glass backing
            drawPath(
                path = glassPath,
                color = Color.White.copy(alpha = 0.25f)
            )

            // Draw Water inside glass clipped to glass path
            clipPath(glassPath) {
                if (animatedLevel > 0.01f) {
                    val maxWaterHeight = glassHeight * 0.92f
                    val currentWaterHeight = maxWaterHeight * animatedLevel
                    val surfaceY = glassBottom - currentWaterHeight
                    val currentWaveAmp = waveIntensity.value.dp.toPx()

                    // Build liquid surface wave path
                    val waterPath = Path().apply {
                        moveTo(glassLeft - 10f, glassBottom + 10f)
                        lineTo(glassLeft - 10f, surfaceY)

                        val step = 4f
                        var x = glassLeft - 10f
                        while (x <= glassRight + 10f) {
                            val relX = (x - glassLeft) / glassWidth
                            val waveY = surfaceY + currentWaveAmp * sin(relX * 2.5f * PI.toFloat() + wavePhase)
                            lineTo(x, waveY)
                            x += step
                        }

                        lineTo(glassRight + 10f, glassBottom + 10f)
                        close()
                    }

                    // Water liquid gradient
                    drawPath(
                        path = waterPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF38BDF8), // Vibrant cyan surface
                                Color(0xFF087EBC), // AquaBlue body
                                Color(0xFF075985)  // DeepBlue depth
                            ),
                            startY = surfaceY,
                            endY = glassBottom
                        )
                    )

                    // Surface glistening foam/highlight line
                    val surfaceLine = Path().apply {
                        var x = glassLeft
                        var first = true
                        while (x <= glassRight) {
                            val relX = (x - glassLeft) / glassWidth
                            val waveY = surfaceY + currentWaveAmp * sin(relX * 2.5f * PI.toFloat() + wavePhase)
                            if (first) {
                                moveTo(x, waveY)
                                first = false
                            } else {
                                lineTo(x, waveY)
                            }
                            x += 4f
                        }
                    }
                    drawPath(
                        path = surfaceLine,
                        color = Color.White.copy(alpha = 0.7f),
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            // Draw Pouring Water Stream if active
            if (streamAlpha.value > 0.01f) {
                val streamStartX = canvasWidth * 0.62f
                val streamStartY = 0f
                val streamTargetX = canvasWidth * 0.50f
                val currentWaterHeight = (glassHeight * 0.92f) * animatedLevel
                val streamTargetY = (glassBottom - currentWaterHeight).coerceAtLeast(glassTop + 10f)

                val streamCurrentY = streamStartY + (streamTargetY - streamStartY) * streamProgress.value.coerceIn(0f, 1f)

                val streamPath = Path().apply {
                    moveTo(streamStartX, streamStartY)
                    cubicTo(
                        streamStartX - 6.dp.toPx(),
                        canvasHeight * 0.12f,
                        streamTargetX + 4.dp.toPx(),
                        streamCurrentY * 0.6f,
                        streamTargetX,
                        streamCurrentY
                    )
                }

                // Outer stream flow
                drawPath(
                    path = streamPath,
                    color = Color(0xFF7DD3FC).copy(alpha = streamAlpha.value * 0.85f),
                    style = Stroke(width = 4.5.dp.toPx(), cap = StrokeCap.Round)
                )
                // Inner glistening core
                drawPath(
                    path = streamPath,
                    color = Color.White.copy(alpha = streamAlpha.value * 0.95f),
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )

                // Splash ripples at impact point
                if (streamProgress.value > 0.4f) {
                    val splashProgress = ((streamProgress.value - 0.4f) / 0.6f)
                    val splashRadius = 10.dp.toPx() * (1f + splashProgress)
                    val splashAlpha = (streamAlpha.value * (1f - splashProgress * 0.5f)).coerceIn(0f, 1f)

                    drawCircle(
                        color = Color.White.copy(alpha = splashAlpha * 0.7f),
                        radius = splashRadius * 0.5f,
                        center = Offset(streamTargetX, streamCurrentY),
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                }
            }

            // Draw Glass Outer Contour & Specular Reflections
            drawPath(
                path = glassPath,
                color = Color.White.copy(alpha = 0.55f),
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )

            // Left vertical specular reflection on glass body
            drawLine(
                color = Color.White.copy(alpha = 0.4f),
                start = Offset(glassLeft + 4.dp.toPx(), glassTop + 12.dp.toPx()),
                end = Offset(glassLeft + 4.dp.toPx(), glassBottom - 16.dp.toPx()),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Subtle top rim highlight
            drawLine(
                color = Color.White.copy(alpha = 0.7f),
                start = Offset(glassLeft + 6.dp.toPx(), glassTop),
                end = Offset(glassRight - 6.dp.toPx(), glassTop),
                strokeWidth = 2.5.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}
