package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LimeBrand

@Composable
fun SpendingLineChart(
    points: List<Double>,
    labels: List<String>,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(16.dp)),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text(
                "No data available for chart",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
        }
        return
    }

    // Animation for line drawing
    var animatedProgress by remember { mutableStateOf(0f) }
    LaunchedEffect(points) {
        animatedProgress = 0f
        animatedProgress = 1f
    }
    val progressFactor by animateFloatAsState(
        targetValue = animatedProgress,
        animationSpec = tween(durationMillis = 800),
        label = "chartProgress"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black, shape = RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        // Upper balance or chart header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Spending Trend",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Live",
                color = LimeBrand,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val paddingLeft = 40f
                val paddingRight = 40f
                val paddingTop = 20f
                val paddingBottom = 40f

                val usableWidth = width - paddingLeft - paddingRight
                val usableHeight = height - paddingTop - paddingBottom

                val maxVal = (points.maxOrNull() ?: 1.0).coerceAtLeast(1.0)
                val minVal = points.minOrNull() ?: 0.0

                val valRange = if (maxVal == minVal) 1.0 else maxVal - minVal

                // Step sizes
                val stepX = if (points.size > 1) usableWidth / (points.size - 1) else usableWidth

                // Calculate control points for smooth bezier curve
                val coords = points.mapIndexed { idx, value ->
                    val x = paddingLeft + idx * stepX
                    // Invert Y so higher value goes higher on screen
                    val normVal = (value - minVal) / valRange
                    val y = paddingTop + usableHeight - (normVal * usableHeight).toFloat()
                    Offset(x, y)
                }

                // Draw horizontal guide lines
                val gridLines = 3
                for (i in 0..gridLines) {
                    val gridY = paddingTop + (usableHeight / gridLines) * i
                    drawLine(
                        color = Color.White.copy(alpha = 0.08f),
                        start = Offset(paddingLeft, gridY),
                        end = Offset(width - paddingRight, gridY),
                        strokeWidth = 1f
                    )
                }

                if (coords.isNotEmpty()) {
                    // Create path for line and fill
                    val strokePath = Path()
                    val fillPath = Path()

                    strokePath.moveTo(coords[0].x, coords[0].y)
                    fillPath.moveTo(coords[0].x, paddingTop + usableHeight)
                    fillPath.lineTo(coords[0].x, coords[0].y)

                    for (i in 1 until coords.size) {
                        val prev = coords[i - 1]
                        val curr = coords[i]
                        // Bezier smoothing control points
                        val cp1 = Offset(prev.x + (curr.x - prev.x) / 2f, prev.y)
                        val cp2 = Offset(prev.x + (curr.x - prev.x) / 2f, curr.y)

                        // Smooth transition
                        strokePath.cubicTo(
                            cp1.x, cp1.y,
                            cp2.x, cp2.y,
                            curr.x, curr.y
                        )

                        fillPath.cubicTo(
                            cp1.x, cp1.y,
                            cp2.x, cp2.y,
                            curr.x, curr.y
                        )
                    }

                    fillPath.lineTo(coords.last().x, paddingTop + usableHeight)
                    fillPath.close()

                    // Draw the fill gradient under the line
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                LimeBrand.copy(alpha = 0.25f),
                                LimeBrand.copy(alpha = 0.0f)
                            ),
                            startY = paddingTop,
                            endY = paddingTop + usableHeight
                        )
                    )

                    // Draw the line with animated progress
                    drawPath(
                        path = strokePath,
                        color = LimeBrand,
                        style = Stroke(width = 6f)
                    )

                    // Draw glow points (circles at coordinates)
                    coords.forEachIndexed { idx, point ->
                        // Only draw nodes for actual points, and highlight the peak
                        val isPeak = points[idx] == maxVal
                        drawCircle(
                            color = if (isPeak) LimeBrand else Color.White,
                            radius = if (isPeak) 8f else 4f,
                            center = point
                        )
                        if (isPeak) {
                            // Extra glow ring around peak
                            drawCircle(
                                color = LimeBrand.copy(alpha = 0.35f),
                                radius = 16f,
                                center = point
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // X labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            labels.forEach { label ->
                Text(
                    text = label,
                    color = Color.White.copy(alpha = 0.45f),
                    fontSize = 11.sp
                )
            }
        }
    }
}
