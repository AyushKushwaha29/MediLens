package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.QueryBuilder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.trend.HistoricalDataPoint
import com.example.data.trend.ParameterTrendAnalysis
import com.example.ui.theme.MinimalNavyPrimary
import com.example.ui.theme.MinimalOutlineBorder
import com.example.ui.theme.MinimalOutlineLight
import com.example.ui.theme.MinimalSkyAccent
import com.example.ui.theme.MinimalSurfaceVariantLight
import com.example.ui.theme.MinimalTextPrimary
import com.example.ui.theme.MinimalTextSecondary
import com.example.ui.theme.MinimalTextTertiary
import com.example.ui.theme.StatusAttentionBg
import com.example.ui.theme.StatusAttentionRed
import com.example.ui.theme.StatusAttentionText
import com.example.ui.theme.StatusLowBg
import com.example.ui.theme.StatusLowBlue
import com.example.ui.theme.StatusLowText
import com.example.ui.theme.StatusNormalBg
import com.example.ui.theme.StatusNormalText

@Composable
fun TrendChart(
    trendAnalysis: ParameterTrendAnalysis,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("trend_chart_card_${trendAnalysis.normalizedName}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, MinimalOutlineLight)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header: Parameter Name & Direction
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = trendAnalysis.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MinimalNavyPrimary
                        )
                    )
                    Text(
                        text = "${trendAnalysis.observationCount} Observations • Unit: ${trendAnalysis.unit}",
                        style = MaterialTheme.typography.bodySmall.copy(color = MinimalTextSecondary)
                    )
                }
                TrendDirectionBadge(trendAnalysis.trendDirection)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Stat metrics row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricItem(
                    label = "LATEST VALUE",
                    value = if (trendAnalysis.currentValue != null) "${trendAnalysis.currentValue} ${trendAnalysis.unit}" else "--"
                )
                MetricItem(
                    label = "PREVIOUS",
                    value = if (trendAnalysis.previousValue != null) "${trendAnalysis.previousValue} ${trendAnalysis.unit}" else "--"
                )
                MetricItem(
                    label = "ABSOLUTE DIFF",
                    value = if (trendAnalysis.absoluteChange != null) {
                        val sign = if (trendAnalysis.absoluteChange > 0) "+" else ""
                        "$sign${trendAnalysis.absoluteChange} ${trendAnalysis.unit}"
                    } else "--"
                )
                MetricItem(
                    label = "% CHANGE",
                    value = if (trendAnalysis.percentageChange != null) {
                        val sign = if (trendAnalysis.percentageChange > 0) "+" else ""
                        "$sign${trendAnalysis.percentageChange}%"
                    } else "--"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Canvas Line Chart
            val history = trendAnalysis.history
            if (history.size >= 2) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(MinimalSurfaceVariantLight, RoundedCornerShape(16.dp))
                        .border(BorderStroke(1.dp, MinimalOutlineLight), RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxWidth().height(140.dp)) {
                        val width = size.width
                        val height = size.height

                        val allValues = history.map { it.value }.toMutableList()
                        if (trendAnalysis.projection.isProjectable && trendAnalysis.projection.projectedValue != null) {
                            allValues.add(trendAnalysis.projection.projectedValue)
                        }
                        if (trendAnalysis.referenceMin != null) allValues.add(trendAnalysis.referenceMin)
                        if (trendAnalysis.referenceMax != null) allValues.add(trendAnalysis.referenceMax)

                        val minVal = (allValues.minOrNull() ?: 0.0) * 0.90
                        val maxVal = (allValues.maxOrNull() ?: 10.0) * 1.10
                        val valRange = if (maxVal > minVal) maxVal - minVal else 1.0

                        fun getY(v: Double): Float {
                            val normalized = (v - minVal) / valRange
                            return (height - (normalized * height)).toFloat().coerceIn(10f, height - 10f)
                        }

                        // Draw Reference Interval Corridor if available
                        if (trendAnalysis.referenceMin != null && trendAnalysis.referenceMax != null) {
                            val yTop = getY(trendAnalysis.referenceMax)
                            val yBottom = getY(trendAnalysis.referenceMin)
                            drawRect(
                                color = Color(0x15001D35),
                                topLeft = Offset(0f, yTop),
                                size = Size(width, yBottom - yTop)
                            )
                            // Dashed reference lines
                            drawLine(
                                color = Color(0x30001D35),
                                start = Offset(0f, yTop),
                                end = Offset(width, yTop),
                                strokeWidth = 1.5f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                            )
                            drawLine(
                                color = Color(0x30001D35),
                                start = Offset(0f, yBottom),
                                end = Offset(width, yBottom),
                                strokeWidth = 1.5f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                            )
                        }

                        val totalPoints = if (trendAnalysis.projection.isProjectable) history.size + 1 else history.size
                        val stepX = width / (totalPoints - 1).coerceAtLeast(1)

                        // Draw historical line
                        val path = Path()
                        val points = mutableListOf<Offset>()
                        for (i in history.indices) {
                            val x = i * stepX
                            val y = getY(history[i].value)
                            val pt = Offset(x, y)
                            points.add(pt)
                            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }

                        drawPath(
                            path = path,
                            color = MinimalNavyPrimary,
                            style = Stroke(width = 4f)
                        )

                        // Draw Historical Dots
                        for (i in history.indices) {
                            val pt = points[i]
                            val isAbnormal = history[i].status == "HIGH" || history[i].status == "LOW"
                            val dotColor = if (isAbnormal) StatusAttentionRed else MinimalNavyPrimary
                            drawCircle(color = Color.White, radius = 7f, center = pt)
                            drawCircle(color = dotColor, radius = 4.5f, center = pt)
                        }

                        // Draw Projection Dashed Line & Dot if projectable
                        if (trendAnalysis.projection.isProjectable && trendAnalysis.projection.projectedValue != null) {
                            val lastHistPt = points.last()
                            val projX = (history.size) * stepX
                            val projY = getY(trendAnalysis.projection.projectedValue)
                            val projPt = Offset(projX, projY)

                            drawLine(
                                color = MinimalNavyPrimary.copy(alpha = 0.5f),
                                start = lastHistPt,
                                end = projPt,
                                strokeWidth = 3f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
                            )

                            drawCircle(color = Color.White, radius = 8f, center = projPt)
                            drawCircle(color = MinimalNavyPrimary, radius = 5f, center = projPt)
                        }
                    }
                }

                // Timeline date labels under chart
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (pt in history) {
                        Text(
                            text = pt.dateLabel,
                            style = MaterialTheme.typography.labelSmall.copy(color = MinimalTextSecondary, fontSize = 10.sp)
                        )
                    }
                    if (trendAnalysis.projection.isProjectable && trendAnalysis.projection.projectedDateLabel != null) {
                        Text(
                            text = "Proj. (~${trendAnalysis.projection.projectedDateLabel})",
                            style = MaterialTheme.typography.labelSmall.copy(color = MinimalNavyPrimary, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MinimalSurfaceVariantLight, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Upload at least 2 reports containing ${trendAnalysis.name} to view historical timeline graphs and projections.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MinimalTextSecondary)
                    )
                }
            }

            // Statistical Trend Projection Section
            Spacer(modifier = Modifier.height(14.dp))
            TrendProjectionBox(trendAnalysis.projection)
        }
    }
}

@Composable
fun TrendProjectionBox(projection: com.example.data.trend.TrendProjectionResult) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MinimalSurfaceVariantLight,
        border = BorderStroke(1.dp, MinimalOutlineLight)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoGraph,
                    contentDescription = "Trend Projection",
                    tint = MinimalNavyPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Statistical Trend Projection",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MinimalNavyPrimary
                    )
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = projection.message,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MinimalTextPrimary,
                    fontWeight = if (projection.isProjectable) FontWeight.Medium else FontWeight.Normal
                )
            )

            if (projection.isProjectable) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = projection.disclaimer,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MinimalTextTertiary,
                        fontSize = 10.sp,
                        lineHeight = 13.sp
                    )
                )
            }
        }
    }
}

@Composable
fun TrendDirectionBadge(direction: String) {
    val (bg, text, icon) = when (direction) {
        "Increasing" -> Triple(StatusAttentionBg, StatusAttentionText, Icons.AutoMirrored.Filled.TrendingUp)
        "Decreasing" -> Triple(StatusLowBg, StatusLowText, Icons.AutoMirrored.Filled.TrendingDown)
        "Stable" -> Triple(StatusNormalBg, StatusNormalText, Icons.AutoMirrored.Filled.TrendingFlat)
        else -> Triple(MinimalSurfaceVariantLight, MinimalTextSecondary, Icons.Default.QueryBuilder)
    }

    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = direction, tint = text, modifier = Modifier.size(13.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = direction, color = text, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun MetricItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = MinimalTextSecondary,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                color = MinimalNavyPrimary
            )
        )
    }
}

