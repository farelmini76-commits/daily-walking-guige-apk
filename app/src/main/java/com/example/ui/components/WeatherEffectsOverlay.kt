package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.example.data.model.WeatherType
import kotlin.random.Random

private data class RainDrop(
    val xRatio: Float,
    val yOffsetRatio: Float,
    val speed: Float,
    val lengthRatio: Float,
    val alpha: Float
)

private data class Snowflake(
    val xRatio: Float,
    val yOffsetRatio: Float,
    val speed: Float,
    val radius: Float,
    val swayPhase: Float
)

private data class Star(
    val xRatio: Float,
    val yRatio: Float,
    val radius: Float,
    val baseAlpha: Float,
    val flickerSpeed: Float
)

@Composable
fun WeatherEffectsOverlay(
    weatherType: WeatherType,
    isDay: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "weatherEffects")

    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "weatherLoop"
    )

    val slowProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "slowDrift"
    )

    // Pre-generate stable random elements
    val raindrops = remember {
        val random = Random(42)
        List(40) {
            RainDrop(
                xRatio = random.nextFloat(),
                yOffsetRatio = random.nextFloat(),
                speed = 0.8f + random.nextFloat() * 0.6f,
                lengthRatio = 0.04f + random.nextFloat() * 0.03f,
                alpha = 0.35f + random.nextFloat() * 0.45f
            )
        }
    }

    val snowflakes = remember {
        val random = Random(77)
        List(35) {
            Snowflake(
                xRatio = random.nextFloat(),
                yOffsetRatio = random.nextFloat(),
                speed = 0.2f + random.nextFloat() * 0.3f,
                radius = 2.5f + random.nextFloat() * 3f,
                swayPhase = random.nextFloat() * 6.28f
            )
        }
    }

    val stars = remember {
        val random = Random(99)
        List(45) {
            Star(
                xRatio = random.nextFloat(),
                yRatio = random.nextFloat() * 0.65f, // mainly in upper sky
                radius = 1.2f + random.nextFloat() * 1.8f,
                baseAlpha = 0.3f + random.nextFloat() * 0.7f,
                flickerSpeed = 0.5f + random.nextFloat() * 1.5f
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Night stars
        if (!isDay && (weatherType == WeatherType.CLEAR || weatherType == WeatherType.PARTLY_CLOUDY)) {
            drawStars(stars, animationProgress, width, height)
        }

        // Weather specifics
        when (weatherType) {
            WeatherType.CLEAR -> {
                if (isDay) {
                    drawSunGlow(animationProgress, width, height)
                }
            }
            WeatherType.PARTLY_CLOUDY, WeatherType.OVERCAST, WeatherType.FOG -> {
                drawDriftingClouds(slowProgress, width, height, weatherType)
            }
            WeatherType.RAIN, WeatherType.DRIZZLE -> {
                drawRain(raindrops, animationProgress, width, height, isDrizzle = weatherType == WeatherType.DRIZZLE)
            }
            WeatherType.THUNDERSTORM -> {
                drawRain(raindrops, animationProgress, width, height, isDrizzle = false)
                drawLightningFlash(animationProgress, width, height)
            }
            WeatherType.SNOW -> {
                drawSnow(snowflakes, animationProgress, width, height)
            }
        }
    }
}

private fun DrawScope.drawStars(
    stars: List<Star>,
    progress: Float,
    width: Float,
    height: Float
) {
    for (star in stars) {
        val alphaMultiplier = (0.6f + 0.4f * kotlin.math.sin(progress * 6.28f * star.flickerSpeed + star.xRatio * 10f)).coerceIn(0.2f, 1f)
        drawCircle(
            color = Color.White.copy(alpha = (star.baseAlpha * alphaMultiplier).coerceIn(0.1f, 0.95f)),
            radius = star.radius,
            center = Offset(star.xRatio * width, star.yRatio * height)
        )
    }
}

private fun DrawScope.drawSunGlow(
    progress: Float,
    width: Float,
    height: Float
) {
    val sunCenter = Offset(width * 0.85f, height * 0.18f)
    val pulse = 1f + 0.08f * kotlin.math.sin(progress * 6.28f)

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0x66FFEB3B),
                Color(0x33FFC107),
                Color(0x00FF9800)
            ),
            center = sunCenter,
            radius = 160f * pulse
        ),
        radius = 160f * pulse,
        center = sunCenter
    )

    drawCircle(
        color = Color(0xAAFFF9C4),
        radius = 28f,
        center = sunCenter
    )
}

private fun DrawScope.drawDriftingClouds(
    driftProgress: Float,
    width: Float,
    height: Float,
    type: WeatherType
) {
    val cloudAlpha = if (type == WeatherType.OVERCAST) 0.35f else if (type == WeatherType.FOG) 0.45f else 0.22f
    val baseColor = Color.White.copy(alpha = cloudAlpha)

    // Layer 1: Left-to-right drift
    val x1 = ((driftProgress * 1.2f) % 1.4f - 0.2f) * width
    drawOval(
        color = baseColor,
        topLeft = Offset(x1, height * 0.08f),
        size = androidx.compose.ui.geometry.Size(width * 0.45f, height * 0.12f)
    )

    // Layer 2: Slower drift
    val x2 = (((driftProgress * 0.8f + 0.5f) % 1.5f) - 0.3f) * width
    drawOval(
        color = baseColor.copy(alpha = cloudAlpha * 0.75f),
        topLeft = Offset(x2, height * 0.16f),
        size = androidx.compose.ui.geometry.Size(width * 0.6f, height * 0.15f)
    )
}

private fun DrawScope.drawRain(
    raindrops: List<RainDrop>,
    progress: Float,
    width: Float,
    height: Float,
    isDrizzle: Boolean
) {
    val dropColor = Color(0x99B3E5FC)
    val angleRad = 0.15f // slight slant

    for (drop in raindrops) {
        val yPos = ((drop.yOffsetRatio + progress * drop.speed) % 1.0f) * height
        val xPos = (drop.xRatio * width + yPos * kotlin.math.tan(angleRad)) % width
        val dropLen = if (isDrizzle) height * 0.015f else height * drop.lengthRatio

        drawLine(
            color = dropColor.copy(alpha = if (isDrizzle) drop.alpha * 0.6f else drop.alpha),
            start = Offset(xPos, yPos),
            end = Offset(xPos + dropLen * kotlin.math.sin(angleRad), yPos + dropLen * kotlin.math.cos(angleRad)),
            strokeWidth = if (isDrizzle) 1.2f else 2.0f
        )
    }
}

private fun DrawScope.drawSnow(
    snowflakes: List<Snowflake>,
    progress: Float,
    width: Float,
    height: Float
) {
    for (flake in snowflakes) {
        val yPos = ((flake.yOffsetRatio + progress * flake.speed) % 1.0f) * height
        val sway = kotlin.math.sin(progress * 6.28f + flake.swayPhase) * 20f
        val xPos = (flake.xRatio * width + sway) % width

        drawCircle(
            color = Color.White.copy(alpha = 0.75f),
            radius = flake.radius,
            center = Offset(xPos, yPos)
        )
    }
}

private fun DrawScope.drawLightningFlash(
    progress: Float,
    width: Float,
    height: Float
) {
    // Rare quick flash
    if (progress in 0.45f..0.48f || progress in 0.50f..0.52f) {
        drawRect(
            color = Color(0x44E0E0FF),
            topLeft = Offset.Zero,
            size = size
        )
    }
}
