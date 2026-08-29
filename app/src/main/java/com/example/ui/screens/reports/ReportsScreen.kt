package com.example.ui.screens.reports

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.ReportEntity
import com.example.ui.components.EmptyStateView
import com.example.ui.components.LoadingView
import com.example.ui.components.MedicalDisclaimerCard
import com.example.ui.theme.MinimalBackgroundLight
import com.example.ui.theme.MinimalNavyPrimary
import com.example.ui.theme.MinimalOutlineLight
import com.example.ui.theme.MinimalSkyAccent
import com.example.ui.theme.MinimalSurfaceLight
import com.example.ui.theme.MinimalSurfaceVariantLight
import com.example.ui.theme.MinimalTextPrimary
import com.example.ui.theme.MinimalTextSecondary
import com.example.ui.theme.MinimalTextTertiary
import com.example.ui.theme.StatusAttentionRed

@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel,
    onNavigateToReportDetail: (String) -> Unit,
    onNavigateToUpload: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var reportToDelete by remember { mutableStateOf<ReportEntity?>(null) }

    if (uiState.isLoading) {
        LoadingView(message = "Loading reports...", modifier = modifier.fillMaxSize())
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MinimalBackgroundLight)
            .padding(horizontal = 16.dp)
            .testTag("reports_screen")
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Screen Title
        Text(
            text = "Medical Reports & Timeline",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MinimalNavyPrimary
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Segmented Tabs: Reports List vs Timeline
        TabRow(
            selectedTabIndex = uiState.activeTab,
            containerColor = MinimalSurfaceVariantLight,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[uiState.activeTab]),
                    color = MinimalNavyPrimary
                )
            },
            modifier = Modifier.clip(RoundedCornerShape(16.dp))
        ) {
            Tab(
                selected = uiState.activeTab == 0,
                onClick = { viewModel.setActiveTab(0) },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "All Reports (${uiState.reports.size})",
                            fontWeight = if (uiState.activeTab == 0) FontWeight.Bold else FontWeight.Medium,
                            color = if (uiState.activeTab == 0) MinimalNavyPrimary else MinimalTextSecondary
                        )
                    }
                },
                modifier = Modifier.testTag("tab_all_reports")
            )
            Tab(
                selected = uiState.activeTab == 1,
                onClick = { viewModel.setActiveTab(1) },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Timeline, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Health Timeline",
                            fontWeight = if (uiState.activeTab == 1) FontWeight.Bold else FontWeight.Medium,
                            color = if (uiState.activeTab == 1) MinimalNavyPrimary else MinimalTextSecondary
                        )
                    }
                },
                modifier = Modifier.testTag("tab_timeline")
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (uiState.reports.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.Description,
                title = "No medical reports found",
                message = "Upload your laboratory test reports or import sample datasets to start building your health timeline.",
                actionLabel = "Upload Report",
                onAction = onNavigateToUpload
            )
        } else if (uiState.activeTab == 0) {
            // All Reports List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(uiState.reports) { report ->
                    ReportListItemCard(
                        report = report,
                        onClick = { onNavigateToReportDetail(report.id) },
                        onDelete = { reportToDelete = report }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    MedicalDisclaimerCard(compact = true)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        } else {
            // Health Timeline View
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(0.dp),
                modifier = Modifier.weight(1f)
            ) {
                val sortedReports = uiState.reports.sortedByDescending { it.reportDate }
                items(sortedReports) { report ->
                    TimelineNodeItem(
                        report = report,
                        onClick = { onNavigateToReportDetail(report.id) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    MedicalDisclaimerCard(compact = true)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (reportToDelete != null) {
        AlertDialog(
            onDismissRequest = { reportToDelete = null },
            title = { Text("Delete Report?", color = MinimalNavyPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete '${reportToDelete!!.reportTitle}'? This will permanently remove its extracted parameters and historical records.", color = MinimalTextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteReport(reportToDelete!!.id)
                        reportToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = StatusAttentionRed)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { reportToDelete = null }) {
                    Text("Cancel", color = MinimalTextSecondary)
                }
            }
        )
    }
}

@Composable
fun ReportListItemCard(
    report: ReportEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("report_item_${report.id}"),
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
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MinimalSkyAccent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = MinimalNavyPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = report.reportTitle,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MinimalNavyPrimary
                    ),
                    maxLines = 1
                )
                Text(
                    text = "${report.reportDateFormatted} • ${report.laboratoryName}",
                    style = MaterialTheme.typography.bodySmall.copy(color = MinimalTextSecondary),
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "${report.parameterCount} parameters",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MinimalTextSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    if (report.abnormalCount > 0) {
                        Text(
                            text = "• ${report.abnormalCount} outside range",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = StatusAttentionRed,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete",
                    tint = MinimalTextTertiary
                )
            }
        }
    }
}

@Composable
fun TimelineNodeItem(
    report: ReportEntity,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Left Column: Date & Line
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(36.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(MinimalNavyPrimary)
            )
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(90.dp)
                    .background(MinimalSkyAccent)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Right Card
        Card(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MinimalSurfaceLight),
            border = BorderStroke(1.dp, MinimalOutlineLight)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = report.reportDateFormatted,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MinimalNavyPrimary,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = report.reportTitle,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MinimalNavyPrimary
                    )
                )
                Text(
                    text = report.summaryText,
                    style = MaterialTheme.typography.bodySmall.copy(color = MinimalTextSecondary),
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${report.parameterCount} tests extracted",
                        style = MaterialTheme.typography.labelSmall.copy(color = MinimalTextSecondary)
                    )
                    Text(
                        text = "View Details →",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MinimalNavyPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

