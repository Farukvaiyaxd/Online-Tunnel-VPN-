package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyberpunkDivider
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonCyanVariant

@Composable
fun WorldMapCanvas(
    isConnected: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "RadarPing")
    
    val pingRadius by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 60f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Radius"
    )

    val pingAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Alpha"
    )

    val particleProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Particle"
    )

    Canvas(modifier = modifier.fillMaxWidth().height(200.dp)) {
        val w = size.width
        val h = size.height
        
        // Draw stylised dots for world map (simulation)
        val dotRadius = 2f
        val dotSpacing = 16f
        for (x in 0..(w / dotSpacing).toInt()) {
            for (y in 0..(h / dotSpacing).toInt()) {
                val cx = x * dotSpacing
                val cy = y * dotSpacing
                // Very rough map shape approximation
                if ((cx > w * 0.1f && cx < w * 0.4f && cy > h * 0.2f && cy < h * 0.8f) ||
                    (cx > w * 0.5f && cx < w * 0.9f && cy > h * 0.1f && cy < h * 0.9f)
                ) {
                    drawCircle(
                        color = CyberpunkDivider,
                        radius = dotRadius,
                        center = Offset(cx, cy)
                    )
                }
            }
        }

        if (isConnected) {
            // User location (approx left)
            val userLoc = Offset(w * 0.25f, h * 0.4f)
            // Server location (approx right)
            val serverLoc = Offset(w * 0.75f, h * 0.3f)

            // Draw radar ping at server
            drawCircle(
                color = NeonCyan.copy(alpha = pingAlpha),
                radius = pingRadius,
                center = serverLoc,
                style = Stroke(width = 2f)
            )
            drawCircle(
                color = NeonCyanVariant,
                radius = 4f,
                center = serverLoc
            )

            // Draw user dot
            drawCircle(
                color = Color.White,
                radius = 4f,
                center = userLoc
            )

            // Draw arc
            val path = Path().apply {
                moveTo(userLoc.x, userLoc.y)
                quadraticBezierTo(
                    w * 0.5f, h * 0.1f, // control point
                    serverLoc.x, serverLoc.y
                )
            }
            drawPath(
                path = path,
                color = NeonCyan.copy(alpha = 0.5f),
                style = Stroke(
                    width = 2f
                )
            )

            // Draw flying particle along arc (rough approximation for Canvas)
            // A precise point on bezier curve can be calculated: 
            // P(t) = (1-t)^2 P0 + 2(1-t)t P1 + t^2 P2
            val t = particleProgress
            val p0x = userLoc.x
            val p0y = userLoc.y
            val p1x = w * 0.5f
            val p1y = h * 0.1f
            val p2x = serverLoc.x
            val p2y = serverLoc.y

            val partX = (1 - t) * (1 - t) * p0x + 2 * (1 - t) * t * p1x + t * t * p2x
            val partY = (1 - t) * (1 - t) * p0y + 2 * (1 - t) * t * p1y + t * t * p2y

            drawCircle(
                color = NeonCyan,
                radius = 6f,
                center = Offset(partX, partY)
            )
            // Add a small glow to the particle
            drawCircle(
                color = NeonCyan.copy(alpha = 0.3f),
                radius = 12f,
                center = Offset(partX, partY)
            )
        }
    }
}
