package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.ElectricBluePrimary
import com.example.ui.theme.MontserratFontFamily
import com.example.ui.theme.NeonCyanAccent
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PureWhiteText

/**
 * Renders the iconic Body Complete – كامل الأجسام Logo with glowing electric blue muscular emblem
 * matching the user's reference image.
 */
@Composable
fun BodyCompleteBrandHeader(
    modifier: Modifier = Modifier,
    compact: Boolean = true
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!compact) {
            MuscularEmblemCanvas(size = 116.dp)
            Spacer(modifier = Modifier.height(10.dp))
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "BODY ",
                fontFamily = MontserratFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontStyle = FontStyle.Italic,
                fontSize = if (compact) 21.sp else 30.sp,
                color = PureWhiteText
            )
            Text(
                text = "COMPLETE",
                fontFamily = MontserratFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontStyle = FontStyle.Italic,
                fontSize = if (compact) 21.sp else 30.sp,
                color = ElectricBluePrimary
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .width(20.dp)
                    .height(2.dp)
                    .background(ElectricBluePrimary)
            )
            Text(
                text = "  كامل الأجسام  ",
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = if (compact) 12.sp else 16.sp,
                color = PureWhiteText
            )
            Box(
                modifier = Modifier
                    .width(20.dp)
                    .height(2.dp)
                    .background(ElectricBluePrimary)
            )
        }
    }
}

@Composable
fun MuscularEmblemCanvas(
    size: Dp = 96.dp,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "emblemGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowPulse"
    )

    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val center = Offset(w / 2f, h / 2f)
        val radius = w * 0.42f

        // Outer neon aura
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    ElectricBluePrimary.copy(alpha = glowAlpha * 0.55f),
                    NeonCyanAccent.copy(alpha = 0.15f),
                    Color.Transparent
                ),
                center = center,
                radius = w * 0.52f
            ),
            radius = w * 0.52f,
            center = center
        )

        // Glowing electric blue ring
        drawCircle(
            color = ElectricBluePrimary,
            radius = radius,
            center = center,
            style = Stroke(width = w * 0.055f)
        )

        // Muscular silhouette head
        drawCircle(
            color = PureWhiteText,
            radius = w * 0.095f,
            center = Offset(w * 0.5f, h * 0.25f)
        )

        // Muscular shoulders, flexed arms & V-taper torso
        val torsoPath = Path().apply {
            moveTo(w * 0.36f, h * 0.36f)
            lineTo(w * 0.64f, h * 0.36f)
            lineTo(w * 0.78f, h * 0.42f)
            lineTo(w * 0.86f, h * 0.30f)
            lineTo(w * 0.92f, h * 0.35f)
            lineTo(w * 0.84f, h * 0.52f)
            lineTo(w * 0.69f, h * 0.53f)
            lineTo(w * 0.63f, h * 0.77f)
            lineTo(w * 0.37f, h * 0.77f)
            lineTo(w * 0.31f, h * 0.53f)
            lineTo(w * 0.16f, h * 0.52f)
            lineTo(w * 0.08f, h * 0.35f)
            lineTo(w * 0.14f, h * 0.30f)
            lineTo(w * 0.22f, h * 0.42f)
            close()
        }
        drawPath(
            path = torsoPath,
            brush = Brush.verticalGradient(
                colors = listOf(PureWhiteText, Color(0xFFBAE6FD), ElectricBluePrimary)
            )
        )

        // Pectoral & Abdominal definition lines
        drawRoundRect(
            color = ObsidianBlack,
            topLeft = Offset(w * 0.38f, h * 0.41f),
            size = Size(w * 0.105f, h * 0.09f),
            cornerRadius = CornerRadius(4f, 4f),
            style = Stroke(width = w * 0.018f)
        )
        drawRoundRect(
            color = ObsidianBlack,
            topLeft = Offset(w * 0.515f, h * 0.41f),
            size = Size(w * 0.105f, h * 0.09f),
            cornerRadius = CornerRadius(4f, 4f),
            style = Stroke(width = w * 0.018f)
        )
        // 6-pack abs grid
        for (row in 0..2) {
            val topY = h * (0.53f + row * 0.07f)
            drawRoundRect(
                color = ObsidianBlack,
                topLeft = Offset(w * 0.42f, topY),
                size = Size(w * 0.07f, h * 0.05f),
                cornerRadius = CornerRadius(3f, 3f)
            )
            drawRoundRect(
                color = ObsidianBlack,
                topLeft = Offset(w * 0.51f, topY),
                size = Size(w * 0.07f, h * 0.05f),
                cornerRadius = CornerRadius(3f, 3f)
            )
        }
    }
}

/**
 * High-contrast anatomical muscle & exercise illustration canvas
 * highlighting the targeted muscle group in glowing electric blue.
 */
@Composable
fun MuscleAnatomyIllustration(
    muscleKey: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "muscleHighlight")
    val highlightAlpha by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "muscleAlpha"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF102A4C),
                        Color(0xFF091528),
                        ObsidianBlack
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(ElectricBluePrimary.copy(alpha = 0.7f), Color(0xFF1E293B))
                ),
                shape = RoundedCornerShape(16.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            drawAnatomicalFigure(muscleKey = muscleKey, glowAlpha = highlightAlpha)
        }
    }
}

private fun DrawScope.drawAnatomicalFigure(muscleKey: String, glowAlpha: Float) {
    val w = size.width
    val h = size.height
    val cx = w * 0.5f

    val baseBodyColor = Color(0xFF334155)
    val baseOutlineColor = Color(0xFF64748B)
    val activeGlow = NeonCyanAccent.copy(alpha = glowAlpha)
    val activeFill = ElectricBluePrimary.copy(alpha = glowAlpha)

    // Subtle gym grid lines in background
    for (i in 1..4) {
        val y = h * (i / 5f)
        drawLine(
            color = ElectricBluePrimary.copy(alpha = 0.08f),
            start = Offset(0f, y),
            end = Offset(w, y),
            strokeWidth = 1f
        )
    }

    // Head
    drawCircle(
        color = baseBodyColor,
        radius = h * 0.08f,
        center = Offset(cx, h * 0.14f)
    )

    // Torso / V-Taper
    val torso = Path().apply {
        moveTo(cx - w * 0.20f, h * 0.24f)
        lineTo(cx + w * 0.20f, h * 0.24f)
        lineTo(cx + w * 0.14f, h * 0.58f)
        lineTo(cx - w * 0.14f, h * 0.58f)
        close()
    }
    drawPath(torso, color = if (muscleKey == "back") activeFill.copy(alpha = 0.55f) else baseBodyColor)
    drawPath(torso, color = baseOutlineColor, style = Stroke(width = 2f))

    // Shoulders (Deltoids)
    val isShoulders = muscleKey == "shoulders"
    drawCircle(
        color = if (isShoulders) activeFill else baseBodyColor,
        radius = h * 0.075f,
        center = Offset(cx - w * 0.23f, h * 0.29f)
    )
    drawCircle(
        color = if (isShoulders) activeFill else baseBodyColor,
        radius = h * 0.075f,
        center = Offset(cx + w * 0.23f, h * 0.29f)
    )
    if (isShoulders) {
        drawCircle(
            color = activeGlow.copy(alpha = 0.35f),
            radius = h * 0.11f,
            center = Offset(cx - w * 0.23f, h * 0.29f)
        )
        drawCircle(
            color = activeGlow.copy(alpha = 0.35f),
            radius = h * 0.11f,
            center = Offset(cx + w * 0.23f, h * 0.29f)
        )
    }

    // Chest (Pectorals)
    val isChest = muscleKey == "chest"
    drawRoundRect(
        color = if (isChest) activeFill else Color(0xFF475569),
        topLeft = Offset(cx - w * 0.17f, h * 0.26f),
        size = Size(w * 0.155f, h * 0.13f),
        cornerRadius = CornerRadius(10f, 10f)
    )
    drawRoundRect(
        color = if (isChest) activeFill else Color(0xFF475569),
        topLeft = Offset(cx + w * 0.015f, h * 0.26f),
        size = Size(w * 0.155f, h * 0.13f),
        cornerRadius = CornerRadius(10f, 10f)
    )

    // Back Lats Wing Highlight
    if (muscleKey == "back") {
        val leftLat = Path().apply {
            moveTo(cx - w * 0.21f, h * 0.31f)
            lineTo(cx - w * 0.04f, h * 0.31f)
            lineTo(cx - w * 0.12f, h * 0.54f)
            close()
        }
        val rightLat = Path().apply {
            moveTo(cx + w * 0.21f, h * 0.31f)
            lineTo(cx + w * 0.04f, h * 0.31f)
            lineTo(cx + w * 0.12f, h * 0.54f)
            close()
        }
        drawPath(leftLat, color = activeGlow)
        drawPath(rightLat, color = activeGlow)
    }

    // Arms (Biceps & Triceps)
    val isArms = muscleKey == "biceps" || muscleKey == "triceps"
    val armColor = if (isArms) activeFill else baseBodyColor
    drawRoundRect(
        color = armColor,
        topLeft = Offset(cx - w * 0.32f, h * 0.34f),
        size = Size(w * 0.09f, h * 0.22f),
        cornerRadius = CornerRadius(12f, 12f)
    )
    drawRoundRect(
        color = armColor,
        topLeft = Offset(cx + w * 0.23f, h * 0.34f),
        size = Size(w * 0.09f, h * 0.22f),
        cornerRadius = CornerRadius(12f, 12f)
    )

    // Abs (Core)
    val isAbs = muscleKey == "abs"
    val absColor = if (isAbs) activeGlow else Color(0xFF475569)
    for (row in 0..2) {
        val y = h * (0.41f + row * 0.052f)
        drawRoundRect(
            color = absColor,
            topLeft = Offset(cx - w * 0.10f, y),
            size = Size(w * 0.085f, h * 0.04f),
            cornerRadius = CornerRadius(6f, 6f)
        )
        drawRoundRect(
            color = absColor,
            topLeft = Offset(cx + w * 0.015f, y),
            size = Size(w * 0.085f, h * 0.04f),
            cornerRadius = CornerRadius(6f, 6f)
        )
    }

    // Legs (Quadriceps, Hamstrings & Calves)
    val isLegs = muscleKey == "legs"
    val legColor = if (isLegs) activeFill else baseBodyColor
    drawRoundRect(
        color = legColor,
        topLeft = Offset(cx - w * 0.15f, h * 0.60f),
        size = Size(w * 0.12f, h * 0.32f),
        cornerRadius = CornerRadius(14f, 14f)
    )
    drawRoundRect(
        color = legColor,
        topLeft = Offset(cx + w * 0.03f, h * 0.60f),
        size = Size(w * 0.12f, h * 0.32f),
        cornerRadius = CornerRadius(14f, 14f)
    )
}

/**
 * Compact circular glowing muscle badge icon used in the "العضلات المستهدفة اليوم" row on HomeScreen.
 */
@Composable
fun MuscleTargetBadgeCard(
    muscleTitle: String,
    muscleKey: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF0A1426))
            .border(1.dp, ElectricBluePrimary.copy(alpha = 0.28f), RoundedCornerShape(14.dp))
            .padding(vertical = 12.dp, horizontal = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            ElectricBluePrimary.copy(alpha = 0.35f),
                            Color(0xFF0E2240)
                        )
                    )
                )
                .border(1.5.dp, NeonCyanAccent.copy(alpha = 0.7f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(32.dp)) {
                val w = size.width
                val h = size.height
                when (muscleKey) {
                    "chest" -> {
                        // Pectoral shield icon
                        drawRoundRect(
                            color = NeonCyanAccent,
                            topLeft = Offset(w * 0.08f, h * 0.22f),
                            size = Size(w * 0.38f, h * 0.42f),
                            cornerRadius = CornerRadius(6f, 6f)
                        )
                        drawRoundRect(
                            color = NeonCyanAccent,
                            topLeft = Offset(w * 0.54f, h * 0.22f),
                            size = Size(w * 0.38f, h * 0.42f),
                            cornerRadius = CornerRadius(6f, 6f)
                        )
                        drawRoundRect(
                            color = PureWhiteText,
                            topLeft = Offset(w * 0.22f, h * 0.68f),
                            size = Size(w * 0.56f, h * 0.18f),
                            cornerRadius = CornerRadius(4f, 4f)
                        )
                    }
                    "triceps", "biceps" -> {
                        // Flexed muscular arm icon
                        val armPath = Path().apply {
                            moveTo(w * 0.20f, h * 0.72f)
                            lineTo(w * 0.48f, h * 0.75f)
                            lineTo(w * 0.78f, h * 0.52f)
                            lineTo(w * 0.65f, h * 0.18f)
                            lineTo(w * 0.45f, h * 0.22f)
                            lineTo(w * 0.54f, h * 0.45f)
                            lineTo(w * 0.30f, h * 0.45f)
                            close()
                        }
                        drawPath(
                            path = armPath,
                            color = NeonCyanAccent,
                            style = Stroke(width = 3.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                        drawCircle(
                            color = ElectricBluePrimary,
                            radius = w * 0.16f,
                            center = Offset(w * 0.42f, h * 0.56f)
                        )
                    }
                    else -> {
                        // Muscular torso / dumbbell icon
                        drawLine(
                            color = NeonCyanAccent,
                            start = Offset(w * 0.15f, h * 0.5f),
                            end = Offset(w * 0.85f, h * 0.5f),
                            strokeWidth = 5f,
                            cap = StrokeCap.Round
                        )
                        drawRoundRect(
                            color = PureWhiteText,
                            topLeft = Offset(w * 0.12f, h * 0.25f),
                            size = Size(w * 0.16f, h * 0.5f),
                            cornerRadius = CornerRadius(4f, 4f)
                        )
                        drawRoundRect(
                            color = PureWhiteText,
                            topLeft = Offset(w * 0.72f, h * 0.25f),
                            size = Size(w * 0.16f, h * 0.5f),
                            cornerRadius = CornerRadius(4f, 4f)
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = muscleTitle,
            style = MaterialTheme.typography.labelLarge,
            color = PureWhiteText
        )
    }
}
