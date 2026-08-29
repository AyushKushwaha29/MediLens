package com.example.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.MedicalParameterEntity
import com.example.data.local.entity.ReportEntity
import com.example.data.pipeline.ReportProcessingPipeline
import com.example.data.pipeline.SampleReports
import com.example.data.repository.AuthRepository
import com.example.data.repository.ReportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray

data class DashboardUiState(
    val userName: String = "Patient",
    val reportCount: Int = 0,
    val trackedParameterCount: Int = 0,
    val abnormalParameters: List<MedicalParameterEntity> = emptyList(),
    val recentReports: List<ReportEntity> = emptyList(),
    val nextSteps: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val isDemoLoading: Boolean = false
)

class DashboardViewModel(
    private val authRepository: AuthRepository,
    private val reportRepository: ReportRepository,
    private val processingPipeline: ReportProcessingPipeline
) : ViewModel() {

    private val _isDemoLoading = MutableStateFlow(false)
    val isDemoLoading: StateFlow<Boolean> = _isDemoLoading.asStateFlow()

    val uiState: StateFlow<DashboardUiState> = authRepository.currentUserId.flatMapLatest { userId ->
        if (userId == null) {
            flowOf(DashboardUiState())
        } else {
            combine(
                authRepository.currentUserName,
                reportRepository.getReports(userId),
                reportRepository.getTrackedParameterCount(userId),
                reportRepository.getAbnormalParameters(userId)
            ) { name, reports, paramCount, abnormals ->
                // Extract latest next steps from the most recent processed report
                val latestReport = reports.firstOrNull { it.processingStatus == "PROCESSED" }
                val nextStepsList = mutableListOf<String>()
                if (latestReport != null && latestReport.nextStepsJson.isNotBlank()) {
                    try {
                        val arr = JSONArray(latestReport.nextStepsJson)
                        for (i in 0 until arr.length()) {
                            nextStepsList.add(arr.getString(i))
                        }
                    } catch (_: Exception) {}
                }
                if (nextStepsList.isEmpty() && abnormals.isNotEmpty()) {
                    nextStepsList.add("Review ${abnormals.size} parameters flagged outside reference intervals with your doctor.")
                    nextStepsList.add("Compare recent test values against your baseline timeline.")
                }

                DashboardUiState(
                    userName = name ?: "Patient",
                    reportCount = reports.size,
                    trackedParameterCount = paramCount,
                    abnormalParameters = abnormals.take(6),
                    recentReports = reports.take(4),
                    nextSteps = nextStepsList,
                    isLoading = false
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState(isLoading = true)
    )

    fun importSampleDataset(onComplete: () -> Unit) {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser() ?: return@launch
            _isDemoLoading.value = true
            try {
                val presets = SampleReports.getSamplePresets()
                for (preset in presets) {
                    processingPipeline.importSampleReport(user.id, preset)
                }
                onComplete()
            } catch (e: Exception) {
                // handled in logs
            } finally {
                _isDemoLoading.value = false
            }
        }
    }
}
