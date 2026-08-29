package com.example.data.trend

import com.example.data.local.entity.MedicalParameterEntity
import kotlin.math.abs

data class HistoricalDataPoint(
    val reportId: String,
    val dateMillis: Long,
    val dateLabel: String,
    val value: Double,
    val unit: String,
    val status: String,
    val refMin: Double?,
    val refMax: Double?
)

data class ParameterTrendAnalysis(
    val normalizedName: String,
    val name: String,
    val unit: String,
    val observationCount: Int,
    val history: List<HistoricalDataPoint>,
    val currentValue: Double?,
    val previousValue: Double?,
    val absoluteChange: Double?,
    val percentageChange: Double?,
    val trendDirection: String, // "Increasing", "Decreasing", "Stable", "Insufficient Data"
    val projection: TrendProjectionResult,
    val referenceMin: Double?,
    val referenceMax: Double?
)

data class TrendProjectionResult(
    val isProjectable: Boolean,
    val projectedValue: Double?,
    val projectedDateMillis: Long?,
    val projectedDateLabel: String?,
    val slopePerDay: Double?,
    val message: String,
    val disclaimer: String = "Statistical estimate based on previous measurements. This is not a medical prediction and actual results may differ."
)

object TrendAnalysisEngine {

    /**
     * Compute historical trend and statistical linear regression projection.
     */
    fun analyzeTrend(
        normalizedName: String,
        displayName: String,
        unit: String,
        parametersSortedByDate: List<MedicalParameterEntity>
    ): ParameterTrendAnalysis {
        val count = parametersSortedByDate.size
        if (count == 0) {
            return ParameterTrendAnalysis(
                normalizedName = normalizedName,
                name = displayName,
                unit = unit,
                observationCount = 0,
                history = emptyList(),
                currentValue = null,
                previousValue = null,
                absoluteChange = null,
                percentageChange = null,
                trendDirection = "Insufficient Data",
                projection = TrendProjectionResult(
                    isProjectable = false,
                    projectedValue = null,
                    projectedDateMillis = null,
                    projectedDateLabel = null,
                    slopePerDay = null,
                    message = "More historical measurements are needed to estimate a meaningful trend."
                ),
                referenceMin = null,
                referenceMax = null
            )
        }

        val historyPoints = parametersSortedByDate.map { param ->
            val dateStr = formatShortDate(param.measurementDate)
            HistoricalDataPoint(
                reportId = param.reportId,
                dateMillis = param.measurementDate,
                dateLabel = dateStr,
                value = param.value,
                unit = param.unit,
                status = param.status,
                refMin = param.refMin,
                refMax = param.refMax
            )
        }

        val currentPoint = historyPoints.last()
        val previousPoint = if (count > 1) historyPoints[count - 2] else null

        val currentVal = currentPoint.value
        val previousVal = previousPoint?.value

        val absChange = if (previousVal != null) {
            roundTo2(currentVal - previousVal)
        } else null

        val pctChange = if (previousVal != null && previousVal != 0.0) {
            roundTo2(((currentVal - previousVal) / previousVal) * 100.0)
        } else null

        val trendDirection = when {
            count < 2 -> "Insufficient Data"
            absChange == null || abs(absChange) < 0.01 -> "Stable"
            absChange > 0.05 -> "Increasing"
            absChange < -0.05 -> "Decreasing"
            else -> "Stable"
        }

        // Statistical Linear Regression for Projection (when >= 2 observations)
        val projection = calculateLinearRegressionProjection(historyPoints)

        // Find reference bounds if available from latest entry
        val refMin = currentPoint.refMin ?: historyPoints.firstOrNull { it.refMin != null }?.refMin
        val refMax = currentPoint.refMax ?: historyPoints.firstOrNull { it.refMax != null }?.refMax

        return ParameterTrendAnalysis(
            normalizedName = normalizedName,
            name = displayName,
            unit = unit,
            observationCount = count,
            history = historyPoints,
            currentValue = currentVal,
            previousValue = previousVal,
            absoluteChange = absChange,
            percentageChange = pctChange,
            trendDirection = trendDirection,
            projection = projection,
            referenceMin = refMin,
            referenceMax = refMax
        )
    }

    /**
     * Statistical Ordinary Least Squares Linear Regression over time intervals
     */
    private fun calculateLinearRegressionProjection(
        points: List<HistoricalDataPoint>
    ): TrendProjectionResult {
        if (points.size < 2) {
            return TrendProjectionResult(
                isProjectable = false,
                projectedValue = null,
                projectedDateMillis = null,
                projectedDateLabel = null,
                slopePerDay = null,
                message = "More historical measurements are needed to estimate a meaningful trend."
            )
        }

        val n = points.size
        val baseTime = points.first().dateMillis
        // convert time delta to days for numerical stability
        val xDays = points.map { (it.dateMillis - baseTime).toDouble() / (1000.0 * 60.0 * 60.0 * 24.0) }
        val yVals = points.map { it.value }

        val sumX = xDays.sum()
        val sumY = yVals.sum()
        val sumXY = xDays.zip(yVals) { x, y -> x * y }.sum()
        val sumX2 = xDays.map { it * it }.sum()

        val denominator = (n * sumX2) - (sumX * sumX)
        if (abs(denominator) < 1e-9) {
            // All measurements taken at identical time
            return TrendProjectionResult(
                isProjectable = false,
                projectedValue = null,
                projectedDateMillis = null,
                projectedDateLabel = null,
                slopePerDay = null,
                message = "Measurements were recorded at the same time interval. Further chronological data needed."
            )
        }

        val slope = ((n * sumXY) - (sumX * sumY)) / denominator
        val intercept = (sumY - (slope * sumX)) / n

        // Project 30 days beyond the latest point
        val latestDay = xDays.last()
        val averageGapDays = if (n > 1) maxOf(30.0, latestDay / (n - 1)) else 30.0
        val targetDay = latestDay + averageGapDays
        val projectedValRaw = intercept + (slope * targetDay)
        val projectedValue = roundTo2(maxOf(0.0, projectedValRaw))

        val projectedMillis = points.last().dateMillis + (averageGapDays * 24 * 60 * 60 * 1000).toLong()
        val projectedDateLabel = formatShortDate(projectedMillis)

        return TrendProjectionResult(
            isProjectable = true,
            projectedValue = projectedValue,
            projectedDateMillis = projectedMillis,
            projectedDateLabel = projectedDateLabel,
            slopePerDay = slope,
            message = "Projected estimate for ~$projectedDateLabel: $projectedValue based on $n historical observations."
        )
    }

    private fun roundTo2(value: Double): Double {
        return kotlin.math.round(value * 100.0) / 100.0
    }

    private fun formatShortDate(millis: Long): String {
        val sdf = java.text.SimpleDateFormat("MMM yyyy", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(millis))
    }
}
