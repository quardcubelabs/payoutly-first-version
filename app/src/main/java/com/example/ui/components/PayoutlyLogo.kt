package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LimeBrand

@Composable
fun PayoutlyLogo(modifier: Modifier = Modifier, size: Dp = 64.dp) {
    Box(
        modifier = modifier
            .size(size)
            .background(Color.Black, shape = RoundedCornerShape(size * 0.22f)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.6f)) {
            val w = size.toPx() * 0.6f
            val h = size.toPx() * 0.6f
            val stroke = w * 0.18f

            // Draw center square (White)
            val cSize = w * 0.28f
            val cX = (w - cSize) / 2f
            val cY = (h - cSize) / 2f
            drawRect(
                color = Color.White,
                topLeft = Offset(cX, cY),
                size = Size(cSize, cSize)
            )

            // Draw outer top-left curves (White)
            val whitePath = Path().apply {
                moveTo(stroke / 2f, h)
                lineTo(stroke / 2f, stroke * 1.2f)
                quadraticBezierTo(stroke / 2f, stroke / 2f, stroke * 1.2f, stroke / 2f)
                lineTo(w - stroke * 1.5f, stroke / 2f)
            }
            drawPath(
                path = whitePath,
                color = Color.White,
                style = Stroke(width = stroke, cap = StrokeCap.Square)
            )

            // Draw outer bottom-right curves (Lime Green)
            val limePath = Path().apply {
                moveTo(w - stroke / 2f, stroke * 1.5f)
                lineTo(w - stroke / 2f, h - stroke * 1.2f)
                quadraticBezierTo(w - stroke / 2f, h - stroke / 2f, w - stroke * 1.2f, h - stroke / 2f)
                lineTo(stroke, h - stroke / 2f)
            }
            drawPath(
                path = limePath,
                color = LimeBrand,
                style = Stroke(width = stroke, cap = StrokeCap.Square)
            )
        }
    }
}
