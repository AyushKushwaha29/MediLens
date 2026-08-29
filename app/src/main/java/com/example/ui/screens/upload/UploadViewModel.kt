package com.example.ui.screens.upload

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.pipeline.ProcessingProgressState
import com.example.data.pipeline.ReportProcessingPipeline
import com.example.data.pipeline.SampleReportTemplate
import com.example.data.pipeline.SampleReports
import com.example.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UploadUiState(
    val isProcessing: Boolean = false,
    val currentStep: String = "",
    val progressFraction: Float = 0f,
    val completedReportId: String? = null,
    val errorMessage: String? = null,
    val selectedFileName: String? = null,
    val samplePresets: List<SampleReportTemplate> = SampleReports.getSamplePresets()
)

class UploadViewModel(
    private val authRepository: AuthRepository,
    private val processingPipeline: ReportProcessingPipeline
) : ViewModel() {

    private val _uiState = MutableStateFlow(UploadUiState())
    val uiState: StateFlow<UploadUiState> = _uiState.asStateFlow()

    fun processSelectedFile(uri: Uri, fileName: String, mimeType: String?, onComplete: (String) -> Unit) {
        val user = authRepository.isLoggedIn()
        if (!user) return

        viewModelScope.launch {
            val currentUser = authRepository.getCurrentUser() ?: return@launch
            _uiState.value = _uiState.value.copy(
                isProcessing = true,
                selectedFileName = fileName,
                errorMessage = null,
                completedReportId = null,
                currentStep = "Starting report analysis...",
                progressFraction = 0.05f
            )

            try {
                val reportId = processingPipeline.processUploadedReport(
                    userId = currentUser.id,
                    uri = uri,
                    fileName = fileName,
                    mimeType = mimeType,
                    onProgress = { progressState ->
                        when (progressState) {
                            is ProcessingProgressState.Progress -> {
                                _uiState.value = _uiState.value.copy(
                                    currentStep = progressState.step,
                                    progressFraction = progressState.progressFraction
                                )
                            }
                            is ProcessingProgressState.Success -> {
                                _uiState.value = _uiState.value.copy(
                                    isProcessing = false,
                                    completedReportId = progressState.reportId,
                                    progressFraction = 1.0f
                                )
                                onComplete(progressState.reportId)
                            }
                            is ProcessingProgressState.Failure -> {
                                _uiState.value = _uiState.value.copy(
                                    isProcessing = false,
                                    errorMessage = progressState.errorMessage
                                )
                            }
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    errorMessage = e.message ?: "An unexpected error occurred during processing."
                )
            }
        }
    }

    fun importPreset(preset: SampleReportTemplate, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser() ?: return@launch
            _uiState.value = _uiState.value.copy(
                isProcessing = true,
                selectedFileName = preset.title,
                errorMessage = null,
                currentStep = "Importing and classifying ${preset.title}...",
                progressFraction = 0.5f
            )

            try {
                val reportId = processingPipeline.importSampleReport(user.id, preset)
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    completedReportId = reportId,
                    progressFraction = 1f
                )
                onComplete(reportId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    errorMessage = e.message ?: "Failed to import sample report."
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null, isProcessing = false)
    }
}
