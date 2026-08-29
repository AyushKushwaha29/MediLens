package com.example.ui.screens.trends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.dao.TrackedParameterSummary
import com.example.data.repository.AuthRepository
import com.example.data.repository.ReportRepository
import com.example.data.trend.ParameterTrendAnalysis
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TrendsUiState(
    val trackedParameters: List<TrackedParameterSummary> = emptyList(),
    val selectedParameterNormalized: String? = null,
    val trendAnalysis: ParameterTrendAnalysis? = null,
    val isLoading: Boolean = false
)

class TrendsViewModel(
    private val authRepository: AuthRepository,
    private val reportRepository: ReportRepository
) : ViewModel() {

    private val _selectedParam = MutableStateFlow<String?>(null)
    private val _trendAnalysis = MutableStateFlow<ParameterTrendAnalysis?>(null)
    private val _isLoading = MutableStateFlow(false)

    val uiState: StateFlow<TrendsUiState> = authRepository.currentUserId.flatMapLatest { userId ->
        if (userId == null) {
            flowOf(TrendsUiState())
        } else {
            combine(
                reportRepository.getTrackedParameters(userId),
                _selectedParam,
                _trendAnalysis,
                _isLoading
            ) { params, selected, trend, loading ->
                // Auto-select first parameter if none selected and list is available
                if (selected == null && params.isNotEmpty()) {
                    selectParameter(params.first().normalizedName)
                }

                TrendsUiState(
                    trackedParameters = params,
                    selectedParameterNormalized = selected,
                    trendAnalysis = trend,
                    isLoading = loading
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TrendsUiState(isLoading = true)
    )

    fun selectParameter(normalizedName: String) {
        _selectedParam.value = normalizedName
        viewModelScope.launch {
            val user = authRepository.getCurrentUser() ?: return@launch
            _isLoading.value = true
            try {
                val analysis = reportRepository.getTrendForParameter(user.id, normalizedName)
                _trendAnalysis.value = analysis
            } catch (_: Exception) {}
            finally {
                _isLoading.value = false
            }
        }
    }
}
