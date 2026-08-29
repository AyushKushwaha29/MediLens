package com.example.ui.screens.compare

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.ReportEntity
import com.example.data.repository.AuthRepository
import com.example.data.repository.ReportComparisonResult
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

data class CompareUiState(
    val reports: List<ReportEntity> = emptyList(),
    val selectedBaselineReportId: String? = null,
    val selectedFollowUpReportId: String? = null,
    val comparisonResult: ReportComparisonResult? = null,
    val isLoading: Boolean = false
)

class CompareViewModel(
    private val authRepository: AuthRepository,
    private val reportRepository: ReportRepository
) : ViewModel() {

    private val _baselineId = MutableStateFlow<String?>(null)
    private val _followUpId = MutableStateFlow<String?>(null)
    private val _comparisonResult = MutableStateFlow<ReportComparisonResult?>(null)
    private val _isLoading = MutableStateFlow(false)

    val uiState: StateFlow<CompareUiState> = authRepository.currentUserId.flatMapLatest { userId ->
        if (userId == null) {
            flowOf(CompareUiState())
        } else {
            combine(
                reportRepository.getReports(userId),
                _baselineId,
                _followUpId,
                _comparisonResult,
                _isLoading
            ) { reports, baseId, followId, compResult, loading ->
                // Auto-select baseline and follow-up if available and none selected
                if (reports.size >= 2 && baseId == null && followId == null) {
                    val sorted = reports.sortedBy { it.reportDate }
                    val autoBase = sorted.first().id
                    val autoFollow = sorted.last().id
                    selectReports(autoBase, autoFollow)
                }

                CompareUiState(
                    reports = reports,
                    selectedBaselineReportId = baseId,
                    selectedFollowUpReportId = followId,
                    comparisonResult = compResult,
                    isLoading = loading
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CompareUiState(isLoading = true)
    )

    fun selectBaseline(reportId: String) {
        _baselineId.value = reportId
        runComparison()
    }

    fun selectFollowUp(reportId: String) {
        _followUpId.value = reportId
        runComparison()
    }

    fun selectReports(baseId: String, followId: String) {
        _baselineId.value = baseId
        _followUpId.value = followId
        runComparison()
    }

    private fun runComparison() {
        val base = _baselineId.value ?: return
        val follow = _followUpId.value ?: return
        if (base == follow) return

        viewModelScope.launch {
            val user = authRepository.getCurrentUser() ?: return@launch
            _isLoading.value = true
            try {
                val result = reportRepository.compareReports(user.id, base, follow)
                _comparisonResult.value = result
            } catch (_: Exception) {}
            finally {
                _isLoading.value = false
            }
        }
    }
}
