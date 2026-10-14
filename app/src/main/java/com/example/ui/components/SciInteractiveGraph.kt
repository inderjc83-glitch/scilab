package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ExperimentDataPoint
import com.example.ui.theme.SciAmber
import com.example.ui.theme.SciCyan
import com.example.ui.theme.SciEmerald

@Composable
fun SciInteractiveGraph(
    dataPoints: List<ExperimentDataPoint>,
    paramName: String,
    unit: String,
    isStreaming: Boolean,
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    // Pulsing animation for real-time telemetry head
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
            .testTag("interactive_graph_container")
    ) {
        // Graph Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = paramName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (dataPoints.isNotEmpty()) "Latest: ${String.format("%.2f", dataPoints.last().value)} $unit" else "No telemetry data",
                    style = MaterialTheme.typography.bodySmall,
                    color = SciCyan
                )
            }

            if (isStreaming) {
                Surface(
                    color = SciEmerald.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SciEmerald.copy(alpha = pulseAlpha))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(SciEmerald, RoundedCornerShape(4.dp))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LIVE 1Hz",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SciEmerald
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Selected point inspector banner
        if (selectedIndex != null && selectedIndex!! in dataPoints.indices) {
            val pt = dataPoints[selectedIndex!!]
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Point #${selectedIndex!! + 1} (t = ${pt.timeOffsetSec}s)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${String.format("%.3f", pt.value)} $unit",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SciAmber
                    )
                }
            }
        }

        // Canvas Graph
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            if (dataPoints.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Start auto-logger or add trial data points to chart results.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            } else {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(dataPoints) {
                            detectTapGestures { offset ->
                                val spacing = size.width / (dataPoints.size.coerceAtLeast(2) - 1).coerceAtLeast(1)
                                val tappedIndex = ((offset.x + spacing / 2) / spacing).toInt().coerceIn(0, dataPoints.lastIndex)
                                selectedIndex = tappedIndex
                            }
                        }
                ) {
                    val width = size.width
                    val height = size.height
                    val paddingBottom = 24f
                    val paddingTop = 16f
                    val graphHeight = height - paddingTop - paddingBottom

                    val values = dataPoints.map { it.value }
                    val rawMin = values.minOrNull() ?: 0f
                    val rawMax = values.maxOrNull() ?: 10f
                    val minVal = if (rawMin == rawMax) rawMin - 1f else rawMin
                    val maxVal = if (rawMin == rawMax) rawMax + 1f else rawMax
                    val range = (maxVal - minVal).coerceAtLeast(0.01f)

                    // Draw horizontal grid lines
                    val gridSteps = 4
                    for (i in 0..gridSteps) {
                        val y = paddingTop + graphHeight * (i.toFloat() / gridSteps)
                        drawLine(
                            color = Color.White.copy(alpha = 0.08f),
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1f
                        )
                    }

                    // Build path and gradient area
                    val linePath = Path()
                    val areaPath = Path()

                    val stepX = if (dataPoints.size > 1) width / (dataPoints.size - 1) else width

                    dataPoints.forEachIndexed { index, pt ->
                        val x = index * stepX
                        val normalizedY = (pt.value - minVal) / range
                        val y = paddingTop + graphHeight * (1f - normalizedY)

                        if (index == 0) {
                            linePath.moveTo(x, y)
                            areaPath.moveTo(x, paddingTop + graphHeight)
                            areaPath.lineTo(x, y)
                        } else {
                            val prevX = (index - 1) * stepX
                            val prevNormY = (dataPoints[index - 1].value - minVal) / range
                            val prevY = paddingTop + graphHeight * (1f - prevNormY)

                            // Smooth cubic Bezier interpolation
                            val controlX1 = prevX + (x - prevX) / 2
                            val controlY1 = prevY
                            val controlX2 = prevX + (x - prevX) / 2
                            val controlY2 = y

                            linePath.cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)
                            areaPath.cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)
                        }

                        if (index == dataPoints.lastIndex) {
                            areaPath.lineTo(x, paddingTop + graphHeight)
                            areaPath.close()
                        }
                    }

                    // Fill gradient under curve
                    val areaGradient = Brush.verticalGradient(
                        colors = listOf(
                            SciCyan.copy(alpha = 0.35f),
                            SciCyan.copy(alpha = 0.02f)
                        ),
                        startY = paddingTop,
                        endY = paddingTop + graphHeight
                    )
                    drawPath(path = areaPath, brush = areaGradient)

                    // Draw primary line
                    drawPath(
                        path = linePath,
                        color = SciCyan,
                        style = Stroke(width = 3.5f, cap = StrokeCap.Round)
                    )

                    // Draw nodes
                    dataPoints.forEachIndexed { index, pt ->
                        val x = index * stepX
                        val normalizedY = (pt.value - minVal) / range
                        val y = paddingTop + graphHeight * (1f - normalizedY)

                        val isSelected = selectedIndex == index
                        val nodeColor = if (isSelected) SciAmber else if (index == dataPoints.lastIndex) SciEmerald else Color.White
                        val radius = if (isSelected) 6f else 4f

                        drawCircle(
                            color = nodeColor,
                            radius = radius,
                            center = Offset(x, y)
                        )

                        if (isSelected) {
                            drawCircle(
                                color = SciAmber.copy(alpha = 0.4f),
                                radius = 10f,
                                center = Offset(x, y)
                            )
                        }
                    }

                    // Draw pulse ring around the newest live data point
                    if (isStreaming && dataPoints.isNotEmpty()) {
                        val lastIndex = dataPoints.lastIndex
                        val lastX = lastIndex * stepX
                        val lastNormY = (dataPoints.last().value - minVal) / range
                        val lastY = paddingTop + graphHeight * (1f - lastNormY)

                        drawCircle(
                            color = SciEmerald.copy(alpha = pulseAlpha * 0.7f),
                            radius = 12f * pulseAlpha,
                            center = Offset(lastX, lastY)
                        )
                    }
                }
            }
        }

        // Graph Footer / Scale markers
        if (dataPoints.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "t = 0s",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Tap any node to inspect telemetry",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Text(
                    text = "t = ${dataPoints.last().timeOffsetSec.toInt()}s",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
