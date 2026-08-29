package com.example.data.classifier

object ReferenceRangeClassifier {

    data class RangeBounds(
        val min: Double?,
        val max: Double?,
        val isQualitative: Boolean = false,
        val normalQualitativeValue: String? = null
    )

    data class ClassificationResult(
        val status: String, // "NORMAL", "LOW", "HIGH", "UNKNOWN"
        val min: Double?,
        val max: Double?,
        val explanation: String
    )

    /**
     * Parse laboratory reference range string into numerical bounds.
     * Never invents ranges.
     */
    fun parseRange(referenceRangeStr: String?): RangeBounds {
        if (referenceRangeStr.isNullOrBlank()) {
            return RangeBounds(min = null, max = null)
        }
        val trimmed = referenceRangeStr.trim()

        // Check qualitative values first
        val lower = trimmed.lowercase()
        if (lower == "negative" || lower == "non-reactive" || lower == "nil" || lower == "absent" || lower == "normal") {
            return RangeBounds(min = null, max = null, isQualitative = true, normalQualitativeValue = trimmed)
        }

        // Regex for Range: "13.0 - 17.0", "13 - 17", "13.5 to 17.5", "13.0–17.0"
        val rangeRegex = Regex("""([0-9]+(?:\.[0-9]+)?)\s*(?:-|–|—|to)\s*([0-9]+(?:\.[0-9]+)?)""", RegexOption.IGNORE_CASE)
        val rangeMatch = rangeRegex.find(trimmed)
        if (rangeMatch != null) {
            val minVal = rangeMatch.groupValues[1].toDoubleOrNull()
            val maxVal = rangeMatch.groupValues[2].toDoubleOrNull()
            if (minVal != null && maxVal != null) {
                return RangeBounds(min = minOf(minVal, maxVal), max = maxOf(minVal, maxVal))
            }
        }

        // Regex for Upper bound: "< 200", "<= 200", "<200", "less than 200", "up to 200"
        val upperRegex = Regex("""(?:<|<=|less than|up to|≤)\s*([0-9]+(?:\.[0-9]+)?)""", RegexOption.IGNORE_CASE)
        val upperMatch = upperRegex.find(trimmed)
        if (upperMatch != null) {
            val maxVal = upperMatch.groupValues[1].toDoubleOrNull()
            if (maxVal != null) {
                return RangeBounds(min = null, max = maxVal)
            }
        }

        // Regex for Lower bound: "> 40", ">= 40", ">40", "greater than 40", "more than 40"
        val lowerRegex = Regex("""(?:>|>=|greater than|more than|≥)\s*([0-9]+(?:\.[0-9]+)?)""", RegexOption.IGNORE_CASE)
        val lowerMatch = lowerRegex.find(trimmed)
        if (lowerMatch != null) {
            val minVal = lowerMatch.groupValues[1].toDoubleOrNull()
            if (minVal != null) {
                return RangeBounds(min = minVal, max = null)
            }
        }

        return RangeBounds(min = null, max = null)
    }

    /**
     * Deterministic classification of value against reference range.
     * Follows strict rule:
     * - Inside laboratory-provided range -> NORMAL
     * - Below min -> LOW
     * - Above max -> HIGH
     * - No reference range -> UNKNOWN
     */
    fun classify(value: Double, referenceRangeStr: String?): ClassificationResult {
        val bounds = parseRange(referenceRangeStr)

        if (bounds.min == null && bounds.max == null) {
            return ClassificationResult(
                status = "UNKNOWN",
                min = null,
                max = null,
                explanation = "No numerical reference range was provided on the laboratory report."
            )
        }

        val min = bounds.min
        val max = bounds.max

        return when {
            min != null && max != null -> {
                when {
                    value < min -> ClassificationResult(
                        status = "LOW",
                        min = min,
                        max = max,
                        explanation = "Value $value is below the laboratory reference interval ($min - $max)."
                    )
                    value > max -> ClassificationResult(
                        status = "HIGH",
                        min = min,
                        max = max,
                        explanation = "Value $value is above the laboratory reference interval ($min - $max)."
                    )
                    else -> ClassificationResult(
                        status = "NORMAL",
                        min = min,
                        max = max,
                        explanation = "Value $value is within the reported laboratory reference interval ($min - $max)."
                    )
                }
            }
            max != null -> {
                if (value > max) {
                    ClassificationResult(
                        status = "HIGH",
                        min = null,
                        max = max,
                        explanation = "Value $value exceeds the reported upper threshold (< $max)."
                    )
                } else {
                    ClassificationResult(
                        status = "NORMAL",
                        min = null,
                        max = max,
                        explanation = "Value $value is within the reported threshold (< $max)."
                    )
                }
            }
            min != null -> {
                if (value < min) {
                    ClassificationResult(
                        status = "LOW",
                        min = min,
                        max = null,
                        explanation = "Value $value is below the reported minimum threshold (> $min)."
                    )
                } else {
                    ClassificationResult(
                        status = "NORMAL",
                        min = min,
                        max = null,
                        explanation = "Value $value is within the reported threshold (> $min)."
                    )
                }
            }
            else -> ClassificationResult(
                status = "UNKNOWN",
                min = null,
                max = null,
                explanation = "Reference interval format could not be verified numerically."
            )
        }
    }
}
