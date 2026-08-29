package com.example.ui.screens.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.MedicalParameterEntity
import com.example.data.local.entity.ReportEntity
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

data class ReportsUiState(
    val reports: List<ReportEntity> = emptyList(),
    val isLoading: Boolean = false,
    val activeTab: Int = 0 // 0 = Reports List, 1 = Health Timeline
)

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

data class ReportDetailUiState(
    val report: ReportEntity? = null,
    val parameters: List<MedicalParameterEntity> = emptyList(),
    val filteredParameters: List<MedicalParameterEntity> = emptyList(),
    val nextSteps: List<String> = emptyList(),
    val chatMessages: List<ChatMessageEntity> = emptyList(),
    val selectedFilter: String = "ALL", // "ALL", "ABNORMAL", "NORMAL"
    val isChatLoading: Boolean = false,
    val chatInput: String = "",
    val isLoading: Boolean = false,
    val isDeleted: Boolean = false
)

class ReportsViewModel(
    private val authRepository: AuthRepository,
    private val reportRepository: ReportRepository
) : ViewModel() {

    private val _activeTab = MutableStateFlow(0)
    val activeTab: StateFlow<Int> = _activeTab.asStateFlow()

    val uiState: StateFlow<ReportsUiState> = authRepository.currentUserId.flatMapLatest { userId ->
        if (userId == null) {
            flowOf(ReportsUiState())
        } else {
            combine(
                reportRepository.getReports(userId),
                _activeTab
            ) { reports, tab ->
                ReportsUiState(
                    reports = reports,
                    isLoading = false,
                    activeTab = tab
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ReportsUiState(isLoading = true)
    )

    fun setActiveTab(tabIndex: Int) {
        _activeTab.value = tabIndex
    }

    fun deleteReport(reportId: String) {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser() ?: return@launch
            reportRepository.deleteReport(reportId, user.id)
        }
    }
}

class ReportDetailViewModel(
    private val reportId: String,
    private val authRepository: AuthRepository,
    private val reportRepository: ReportRepository
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow("ALL")
    private val _chatInput = MutableStateFlow("")
    private val _isChatLoading = MutableStateFlow(false)
    private val _isDeleted = MutableStateFlow(false)

    private val _dbFlow = authRepository.currentUserId.flatMapLatest { userId ->
        if (userId == null) {
            flowOf(Triple(null, emptyList<MedicalParameterEntity>(), emptyList<ChatMessageEntity>()))
        } else {
            combine(
                reportRepository.getReportById(reportId, userId),
                reportRepository.getParametersForReport(reportId, userId),
                reportRepository.getChatMessages(reportId, userId)
            ) { report, params, msgs ->
                Triple(report, params, msgs)
            }
        }
    }

    private val _uiControlFlow = combine(
        _selectedFilter,
        _chatInput,
        _isChatLoading,
        _isDeleted
    ) { filter, chatText, chatLoading, deleted ->
        Quadruple(filter, chatText, chatLoading, deleted)
    }

    val uiState: StateFlow<ReportDetailUiState> = combine(
        _dbFlow,
        _uiControlFlow
    ) { (report, params, msgs), (filter, chatText, chatLoading, deleted) ->
        val filtered = when (filter) {
            "ABNORMAL" -> params.filter { it.status == "HIGH" || it.status == "LOW" }
            "NORMAL" -> params.filter { it.status == "NORMAL" }
            else -> params
        }

        val nextStepsList = mutableListOf<String>()
        if (report != null && report.nextStepsJson.isNotBlank()) {
            try {
                val arr = JSONArray(report.nextStepsJson)
                for (i in 0 until arr.length()) {
                    nextStepsList.add(arr.getString(i))
                }
            } catch (_: Exception) {}
        }

        ReportDetailUiState(
            report = report,
            parameters = params,
            filteredParameters = filtered,
            nextSteps = nextStepsList,
            chatMessages = msgs,
            selectedFilter = filter,
            chatInput = chatText,
            isChatLoading = chatLoading,
            isLoading = false,
            isDeleted = deleted
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ReportDetailUiState(isLoading = true)
    )


    fun setFilter(filter: String) {
        _selectedFilter.value = filter
    }

    fun onChatInputChange(text: String) {
        _chatInput.value = text
    }

    fun sendChatMessage() {
        val text = _chatInput.value.trim()
        if (text.isBlank()) return

        _chatInput.value = ""
        _isChatLoading.value = true

        viewModelScope.launch {
            val user = authRepository.getCurrentUser() ?: return@launch
            try {
                reportRepository.sendChatMessage(reportId, user.id, text)
            } catch (_: Exception) {}
            finally {
                _isChatLoading.value = false
            }
        }
    }

    fun deleteThisReport(onDone: () -> Unit) {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser() ?: return@launch
            reportRepository.deleteReport(reportId, user.id)
            _isDeleted.value = true
            onDone()
        }
    }
}
