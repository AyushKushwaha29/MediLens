package com.example.ui.screens.upload

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.MedicalDisclaimerCard
import com.example.ui.theme.MinimalBackgroundLight
import com.example.ui.theme.MinimalNavyPrimary
import com.example.ui.theme.MinimalOutlineBorder
import com.example.ui.theme.MinimalOutlineLight
import com.example.ui.theme.MinimalSkyAccent
import com.example.ui.theme.MinimalSurfaceLight
import com.example.ui.theme.MinimalSurfaceVariantLight
import com.example.ui.theme.MinimalTextPrimary
import com.example.ui.theme.MinimalTextSecondary
import com.example.ui.theme.MinimalTextTertiary
import com.example.ui.theme.StatusAttentionBg
import com.example.ui.theme.StatusAttentionRed
import com.example.ui.theme.StatusAttentionText

@Composable
fun UploadScreen(
    viewModel: UploadViewModel,
    onReportProcessed: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            var fileName = "medical_report.pdf"
            try {
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1) fileName = it.getString(nameIndex)
                    }
                }
            } catch (_: Exception) {}

            val mimeType = context.contentResolver.getType(uri)
            viewModel.processSelectedFile(uri, fileName, mimeType, onReportProcessed)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MinimalBackgroundLight)
            .padding(16.dp)
            .testTag("upload_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Upload Medical Report",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MinimalNavyPrimary
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Upload PDF reports, scanned lab documents, or camera photos. The pipeline extracts test parameters, maps reference ranges, and organizes your health timeline.",
                style = MaterialTheme.typography.bodyMedium.copy(color = MinimalTextSecondary, lineHeight = 20.sp)
            )
        }

        // Active Processing State Card
        if (uiState.isProcessing) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("processing_progress_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MinimalSurfaceLight),
                    border = BorderStroke(1.5.dp, MinimalNavyPrimary)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MinimalSkyAccent),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Science,
                                    contentDescription = null,
                                    tint = MinimalNavyPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Processing Medical Report",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MinimalNavyPrimary
                                    )
                                )
                                Text(
                                    text = uiState.selectedFileName ?: "Report",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MinimalTextSecondary)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        LinearProgressIndicator(
                            progress = { uiState.progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = MinimalNavyPrimary,
                            trackColor = MinimalSurfaceVariantLight
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = uiState.currentStep,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MinimalNavyPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }

        // Error Banner
        if (uiState.errorMessage != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = StatusAttentionBg),
                    border = BorderStroke(1.dp, StatusAttentionRed.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = "Error",
                                tint = StatusAttentionRed
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Processing Issue Encountered",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = StatusAttentionRed
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = uiState.errorMessage!!,
                            style = MaterialTheme.typography.bodySmall.copy(color = StatusAttentionText)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { viewModel.clearError() },
                            colors = ButtonDefaults.buttonColors(containerColor = StatusAttentionRed),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Dismiss & Try Again", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Main Upload Drop-zone Card
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(
                        BorderStroke(1.5.dp, MinimalNavyPrimary.copy(alpha = 0.25f)),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .background(MinimalSurfaceLight)
                    .clickable(enabled = !uiState.isProcessing) {
                        filePickerLauncher.launch("*/*")
                    }
                    .testTag("upload_dropzone"),
                color = MinimalSurfaceLight
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MinimalSkyAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = "Upload",
                            tint = MinimalNavyPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Tap to choose a file from your device",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MinimalNavyPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Supports PDF (including multi-page & scanned), JPG, JPEG, and PNG",
                        style = MaterialTheme.typography.bodySmall.copy(color = MinimalTextSecondary)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { filePickerLauncher.launch("*/*") },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MinimalNavyPrimary),
                        enabled = !uiState.isProcessing,
                        modifier = Modifier.testTag("btn_select_file")
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Browse Files", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Instant Sample Report Templates
        item {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Presets",
                        tint = MinimalNavyPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Instant Clinical Report Presets",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MinimalNavyPrimary
                        )
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Load pre-configured realistic medical reports to test parameter extraction, timelines, and trend projections instantly.",
                    style = MaterialTheme.typography.bodySmall.copy(color = MinimalTextSecondary)
                )
            }
        }

        items(uiState.samplePresets) { preset ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sample_preset_${preset.title.replace(" ", "_")}"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MinimalSurfaceLight),
                border = BorderStroke(1.dp, MinimalOutlineLight)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MinimalSkyAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = null,
                            tint = MinimalNavyPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = preset.title,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MinimalNavyPrimary
                            )
                        )
                        Text(
                            text = "${preset.dateString} • ${preset.parameters.size} parameters",
                            style = MaterialTheme.typography.bodySmall.copy(color = MinimalTextSecondary)
                        )
                    }

                    Button(
                        onClick = { viewModel.importPreset(preset, onReportProcessed) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MinimalNavyPrimary),
                        enabled = !uiState.isProcessing
                    ) {
                        Text("Import", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            MedicalDisclaimerCard()
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

