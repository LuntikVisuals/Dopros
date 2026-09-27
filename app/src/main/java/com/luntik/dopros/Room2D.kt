package com.luntik.dopros

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

enum class FxKind { None, Shock, Flash }

/**
 * 2D interrogation room: walls, lamp, table, suspect silhouette.
 * Flash / shock overlay driven by [fx].
 */
@Composable
fun InterrogationRoom2D(
    fearSubject: Float,
    fx: FxKind,
    onFxDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val flash = remember { Animatable(0f) }
    val shake = remember { Animatable(0f) }
    val blink = rememberInfiniteTransition(label = "blink")
    val lampPulse by blink.animateFloat(
        0.85f, 1f,
        infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Reverse),
        label = "lamp"
    )

    LaunchedEffect(fx) {
        when (fx) {
            FxKind.Shock -> {
                launch {
                    flash.snapTo(0.9f)
                    flash.animateTo(0f, tween(350))
                }
                launch {
                    repeat(6) {
                        shake.snapTo(if (it % 2 == 0) 8f else -8f)
                        kotlinx.coroutines.delay(40)
                    }
                    shake.snapTo(0f)
                }
                kotlinx.coroutines.delay(400)
                onFxDone()
            }
            FxKind.Flash -> {
                flash.snapTo(1f)
                flash.animateTo(0f, tween(600))
                onFxDone()
            }
            FxKind.None -> {}
        }
    }

    val sweat = (fearSubject / 100f).coerceIn(0f, 1f)

    Box(modifier.height(220.dp).fillMaxWidth()) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val sx = shake.value

            withTransform({
                translate(sx, 0f)
            }) {
                // back wall
                drawRect(
                    Brush.verticalGradient(
                        listOf(Color(0xFF1A1520), Color(0xFF0C0A10))
                    ),
                    size = Size(w, h * 0.72f)
                )
                // side perspective strips
                drawRect(
                    Color(0xFF0E0C12),
                    topLeft = Offset(0f, 0f),
                    size = Size(w * 0.08f, h)
                )
                drawRect(
                    Color(0xFF0E0C12),
                    topLeft = Offset(w * 0.92f, 0f),
                    size = Size(w * 0.08f, h)
                )

                // ceiling lamp
                val lx = w * 0.5f
                val ly = h * 0.08f
                drawCircle(
                    Color(0xFFE8D090).copy(alpha = 0.15f * lampPulse),
                    radius = h * 0.35f * lampPulse,
                    center = Offset(lx, ly)
                )
                drawCircle(Color(0xFF3A3428), radius = 10f, center = Offset(lx, ly))
                drawCircle(
                    Color(0xFFE8D090).copy(alpha = 0.9f * lampPulse),
                    radius = 5f,
                    center = Offset(lx, ly)
                )

                // one-way glass hint
                drawRect(
                    Color.White.copy(alpha = 0.04f),
                    topLeft = Offset(w * 0.72f, h * 0.12f),
                    size = Size(w * 0.18f, h * 0.28f)
                )
                drawRect(
                    Color.White.copy(alpha = 0.08f),
                    topLeft = Offset(w * 0.72f, h * 0.12f),
                    size = Size(w * 0.18f, h * 0.28f),
                    style = Stroke(width = 2f)
                )

                // suspect silhouette (head + shoulders)
                val cx = w * 0.5f
                val cy = h * 0.38f
                val headR = h * 0.11f
                // body
                val body = Path().apply {
                    moveTo(cx - headR * 1.6f, cy + headR * 0.9f)
                    quadraticBezierTo(cx, cy + headR * 2.8f, cx + headR * 1.6f, cy + headR * 0.9f)
                    lineTo(cx + headR * 1.8f, h * 0.72f)
                    lineTo(cx - headR * 1.8f, h * 0.72f)
                    close()
                }
                drawPath(body, Color(0xFF1C1822))
                drawCircle(Color(0xFF25202C), radius = headR, center = Offset(cx, cy))
                // eyes — wider when afraid
                val eyeY = cy - headR * 0.05f
                val eyeOpen = 2.5f + sweat * 3f
                drawCircle(Color(0xFFE8A838).copy(alpha = 0.35f + sweat * 0.5f), eyeOpen, Offset(cx - headR * 0.35f, eyeY))
                drawCircle(Color(0xFFE8A838).copy(alpha = 0.35f + sweat * 0.5f), eyeOpen, Offset(cx + headR * 0.35f, eyeY))
                // sweat drops
                if (sweat > 0.4f) {
                    drawCircle(Color(0xFF6EC8FF).copy(alpha = 0.5f), 2.5f, Offset(cx + headR * 0.7f, cy + headR * 0.3f))
                }

                // table
                val tableTop = h * 0.68f
                drawRect(
                    Brush.verticalGradient(listOf(Color(0xFF2A221C), Color(0xFF1A1510))),
                    topLeft = Offset(w * 0.06f, tableTop),
                    size = Size(w * 0.88f, h * 0.32f)
                )
                // table edge
                drawRoundRect(
                    Color(0xFF3D3228),
                    topLeft = Offset(w * 0.05f, tableTop - 6f),
                    size = Size(w * 0.9f, 12f),
                    cornerRadius = CornerRadius(4f, 4f)
                )
                // folder on table
                drawRoundRect(
                    Color(0xFF3A3030),
                    topLeft = Offset(w * 0.15f, tableTop + 16f),
                    size = Size(w * 0.22f, h * 0.12f),
                    cornerRadius = CornerRadius(3f, 3f)
                )
                // recorder
                drawRoundRect(
                    Color(0xFF222228),
                    topLeft = Offset(w * 0.62f, tableTop + 20f),
                    size = Size(w * 0.2f, h * 0.08f),
                    cornerRadius = CornerRadius(4f, 4f)
                )
                drawCircle(Color(0xFFC45C5C), 4f, Offset(w * 0.72f, tableTop + 20f + h * 0.04f))
            }

            // FX overlay
            if (flash.value > 0.01f) {
                val col = when (fx) {
                    FxKind.Shock -> Color(0xFF6EC8FF).copy(alpha = flash.value * 0.75f)
                    else -> Color.White.copy(alpha = flash.value * 0.85f)
                }
                drawRect(col, size = size)
            }
        }
    }
}
