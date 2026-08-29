package com.example.data.repository

import com.example.data.ai.MedicalAnalysisService
import com.example.data.local.dao.ChatMessageDao
import com.example.data.local.dao.MedicalParameterDao
import com.example.data.local.dao.ReportDao
import com.example.data.local.dao.TrackedParameterSummary
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.MedicalParameterEntity
import com.example.data.local.entity.ReportEntity
import com.example.data.trend.ParameterTrendAnalysis
import com.example.data.trend.TrendAnalysisEngine
import kotlinx.coroutines.flow.Flow
import java.util.UUID

data class ParameterComparisonItem(
    val normalizedName: String,
    val name: String,
    val unit: String,
    val baselineValue: Double?,
    val baselineDisplay: String?,
    val baselineStatus: String?,
    val followUpValue: Double?,
    val followUpDisplay: String?,
    val followUpStatus: String?,
    val absoluteDiff: Double?,
    val percentageDiff: Double?,
    val changeDirection: String // "Increased", "Decreased", "Unchanged", "New", "Resolved"
)

data class ReportComparisonResult(
    val baselineReport: ReportEntity,
    val followUpReport: ReportEntity,
    val comparisons: List<ParameterComparisonItem>,
    val summary: String
)

class ReportRepository(
    private val reportDao: ReportDao,
    private val parameterDao: MedicalParameterDao,
    private val chatDao: ChatMessageDao
) {
    fun getReports(userId: String): Flow<List<ReportEntity>> =
        reportDao.getReportsForUser(userId)

    fun getReportById(reportId: String, userId: String): Flow<ReportEntity?> =
        reportDao.getReportById(reportId, userId)

    fun getParametersForReport(reportId: String, userId: String): Flow<List<MedicalParameterEntity>> =
        parameterDao.getParametersForReport(reportId, userId)

    fun getTrackedParameters(userId: String): Flow<List<TrackedParameterSummary>> =
        parameterDao.getDistinctTrackedParameters(userId)

    fun getAbnormalParameters(userId: String): Flow<List<MedicalParameterEntity>> =
        parameterDao.getAbnormalParametersForUser(userId)

    fun getReportCount(userId: String): Flow<Int> =
        reportDao.getReportCount(userId)

    fun getTrackedParameterCount(userId: String): Flow<Int> =
        parameterDao.getTrackedParameterCount(userId)

    suspend fun getTrendForParameter(userId: String, normalizedName: String): ParameterTrendAnalysis {
        val history = parameterDao.getHistoryForParameterSync(userId, normalizedName)
        val name = history.lastOrNull()?.name ?: normalizedName.replace("_", " ").capitalize()
        val unit = history.lastOrNull()?.unit ?: ""
        return TrendAnalysisEngine.analyzeTrend(normalizedName, name, unit, history)
    }

    suspend fun compareReports(
        userId: String,
        baselineReportId: String,
        followUpReportId: String
    ): ReportComparisonResult? {
        val baseline = reportDao.getReportByIdSync(baselineReportId, userId) ?: return null
        val followUp = reportDao.getReportByIdSync(followUpReportId, userId) ?: return null

        val baselineParams = parameterDao.getParametersForReportSync(baselineReportId, userId)
        val followUpParams = parameterDao.getParametersForReportSync(followUpReportId, userId)

        val baselineMap = baselineParams.associateBy { it.normalizedName }
        val followUpMap = followUpParams.associateBy { it.normalizedName }

        val allKeys = (baselineMap.keys + followUpMap.keys).toList().sorted()

        val items = mutableListOf<ParameterComparisonItem>()
        var improvedOrStabilizedCount = 0
        var totalCompared = 0

        for (key in allKeys) {
            val b = baselineMap[key]
            val f = followUpMap[key]

            val name = f?.name ?: b?.name ?: key
            val unit = f?.unit ?: b?.unit ?: ""

            val bVal = b?.value
            val fVal = f?.value

            val absDiff = if (bVal != null && fVal != null) {
                kotlin.math.round((fVal - bVal) * 100.0) / 100.0
            } else null

            val pctDiff = if (bVal != null && fVal != null && bVal != 0.0) {
                kotlin.math.round(((fVal - bVal) / bVal) * 100.0 * 10.0) / 10.0
            } else null

            val direction = when {
                bVal == null -> "New"
                fVal == null -> "Not tested"
                absDiff == null || kotlin.math.abs(absDiff) < 0.01 -> "Unchanged"
                absDiff > 0 -> "Increased"
                else -> "Decreased"
            }

            if (bVal != null && fVal != null) {
                totalCompared++
                if (f.status == "NORMAL" || (b.status != "NORMAL" && f.status == "NORMAL")) {
                    improvedOrStabilizedCount++
                }
            }

            items.add(
                ParameterComparisonItem(
                    normalizedName = key,
                    name = name,
                    unit = unit,
                    baselineValue = bVal,
                    baselineDisplay = b?.displayValue,
                    baselineStatus = b?.status,
                    followUpValue = fVal,
                    followUpDisplay = f?.displayValue,
                    followUpStatus = f?.status,
                    absoluteDiff = absDiff,
                    percentageDiff = pctDiff,
                    changeDirection = direction
                )
            )
        }

        val summary = "Comparing ${baseline.reportTitle} (${baseline.reportDateFormatted}) to ${followUp.reportTitle} (${followUp.reportDateFormatted}). Found $totalCompared matching parameters across reports."

        return ReportComparisonResult(
            baselineReport = baseline,
            followUpReport = followUp,
            comparisons = items,
            summary = summary
        )
    }

    suspend fun deleteReport(reportId: String, userId: String) {
        parameterDao.deleteParametersForReport(reportId, userId)
        chatDao.deleteMessagesForReport(reportId, userId)
        reportDao.deleteReport(reportId, userId)
    }

    // Chat
    fun getChatMessages(reportId: String, userId: String): Flow<List<ChatMessageEntity>> =
        chatDao.getMessagesForReport(reportId, userId)

    suspend fun sendChatMessage(
        reportId: String,
        userId: String,
        userMessage: String
    ): String {
        // Save user message
        val userMsgEntity = ChatMessageEntity(
            id = UUID.randomUUID().toString(),
            reportId = reportId,
            userId = userId,
            sender = "USER",
            message = userMessage
        )
        chatDao.insertMessage(userMsgEntity)

        // Build report context
        val report = reportDao.getReportByIdSync(reportId, userId)
        val params = parameterDao.getParametersForReportSync(reportId, userId)

        val contextBuilder = StringBuilder()
        if (report != null) {
            contextBuilder.append("Report: ${report.reportTitle} (${report.reportType})\n")
            contextBuilder.append("Date: ${report.reportDateFormatted}\n")
            contextBuilder.append("Lab: ${report.laboratoryName}\n")
            contextBuilder.append("Summary: ${report.summaryText}\n")
        }
        contextBuilder.append("Parameters:\n")
        for (p in params) {
            contextBuilder.append("- ${p.name}: ${p.displayValue} ${p.unit} (Ref: ${p.referenceRange}) -> Status: ${p.status}\n")
        }

        val aiResponse = MedicalAnalysisService.answerReportQuestion(
            reportContext = contextBuilder.toString(),
            chatHistory = listOf("USER" to userMessage),
            userQuestion = userMessage
        )

        // Save AI response
        val aiMsgEntity = ChatMessageEntity(
            id = UUID.randomUUID().toString(),
            reportId = reportId,
            userId = userId,
            sender = "AI",
            message = aiResponse
        )
        chatDao.insertMessage(aiMsgEntity)

        return aiResponse
    }
}
