package com.example.ui.screens.dashboard

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.MedicalParameterEntity
import com.example.data.local.entity.ReportEntity
import com.example.ui.components.EmptyStateView
import com.example.ui.components.LoadingView
import com.example.ui.components.MedicalDisclaimerCard
import com.example.ui.components.NextStepsCard
import com.example.ui.components.StatCard
import com.example.ui.components.StatusBadge
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
import com.example.ui.theme.StatusLowBg
import com.example.ui.theme.StatusLowText
import com.example.ui.theme.StatusNormalBg
import com.example.ui.theme.StatusNormalText

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToUpload: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToReportDetail: (String) -> Unit,
    onNavigateToTrends: (String?) -> Unit,
    onNavigateToCompare: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isDemoLoading by viewModel.isDemoLoading.collectAsStateWithLifecycle()

    if (uiState.isLoading) {
        LoadingView(message = "Loading your health dashboard...", modifier = modifier.fillMaxSize())
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MinimalBackgroundLight)
            .padding(horizontal = 16.dp)
            .testTag("dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Minimalist Welcome & Avatar Header
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "WELCOME BACK",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MinimalTextSecondary,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = uiState.userName,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MinimalNavyPrimary
                        )
                    )
                }

                // Avatar initials circle
                val initials = uiState.userName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("").ifEmpty { "U" }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MinimalSkyAccent)
                        .border(BorderStroke(2.dp, Color.White), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials.uppercase(),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MinimalNavyPrimary
                        )
                    )
                }
            }
        }

        // Quick Action Upload & Load Demo Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onNavigateToUpload,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MinimalNavyPrimary,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_dashboard_upload")
                ) {
                    Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Upload Report", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                if (uiState.reportCount == 0) {
                    Button(
                        onClick = { viewModel.importSampleDataset {} },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MinimalSkyAccent,
                            contentColor = MinimalNavyPrimary
                        ),
                        enabled = !isDemoLoading,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_load_sample_data")
                    ) {
                        if (isDemoLoading) {
                            CircularProgressIndicator(
                                color = MinimalNavyPrimary,
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Load Demo", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Quick Stats Section (2x2 Grid)
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Reports Analyzed",
                        value = "${uiState.reportCount}",
                        icon = Icons.Default.Description,
                        iconColor = MinimalNavyPrimary,
                        iconBgColor = MinimalSkyAccent,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToReports
                    )
                    StatCard(
                        title = "Parameters Tracked",
                        value = "${uiState.trackedParameterCount}",
                        icon = Icons.Default.Science,
                        iconColor = MinimalNavyPrimary,
                        iconBgColor = StatusNormalBg,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToTrends(null) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Outside Range",
                        value = "${uiState.abnormalParameters.size}",
                        icon = Icons.Default.WarningAmber,
                        iconColor = StatusAttentionText,
                        iconBgColor = StatusAttentionBg,
                        subtitle = "Needs physician review",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Compare Reports",
                        value = if (uiState.reportCount >= 2) "Ready" else "Need 2+",
                        icon = Icons.Default.CompareArrows,
                        iconColor = MinimalNavyPrimary,
                        iconBgColor = MinimalSkyAccent,
                        subtitle = "Side-by-side diff",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToCompare
                    )
                }
            }
        }

        // Proactive "Next Step Guidance"
        if (uiState.nextSteps.isNotEmpty()) {
            item {
                NextStepsCard(nextSteps = uiState.nextSteps)
            }
        }

        // Abnormal Parameters Alert Section
        if (uiState.abnormalParameters.isNotEmpty()) {
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Parameters Outside Reference Range",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MinimalNavyPrimary
                            )
                        )
                        Text(
                            text = "${uiState.abnormalParameters.size} flagged",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = StatusAttentionText,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.abnormalParameters) { param ->
                            AbnormalParameterCard(
                                param = param,
                                onClick = { onNavigateToTrends(param.normalizedName) }
                            )
                        }
                    }
                }
            }
        }

        // Recent Reports Section
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Reports",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MinimalNavyPrimary
                        )
                    )
                    if (uiState.recentReports.isNotEmpty()) {
                        Text(
                            text = "View All",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = MinimalNavyPrimary,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.clickable { onNavigateToReports() }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (uiState.recentReports.isEmpty()) {
                    EmptyStateView(
                        icon = Icons.Default.Description,
                        title = "No medical reports uploaded yet",
                        message = "Upload your laboratory test PDFs or images to extract parameters and track your health timeline.",
                        actionLabel = "Upload Your First Report",
                        onAction = onNavigateToUpload
                    )
                } else {
                    for (report in uiState.recentReports) {
                        DashboardReportCard(
                            report = report,
                            onClick = { onNavigateToReportDetail(report.id) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        // Medical Disclaimer at bottom of dashboard
        item {
            MedicalDisclaimerCard()
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun AbnormalParameterCard(
    param: MedicalParameterEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .clickable { onClick() }
            .testTag("abnormal_card_${param.normalizedName}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MinimalSurfaceLight),
        border = BorderStroke(1.dp, MinimalOutlineLight)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusBadge(param.status)
                Icon(
                    imageVector = Icons.Default.Timeline,
                    contentDescription = "View Trend",
                    tint = MinimalNavyPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = param.name,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MinimalNavyPrimary
                ),
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${param.displayValue} ${param.unit}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (param.status == "HIGH") StatusAttentionRed else MinimalNavyPrimary
                )
            )
            Text(
                text = "Ref: ${param.referenceRange}",
                style = MaterialTheme.typography.bodySmall.copy(color = MinimalTextSecondary, fontSize = 10.sp)
            )
        }
    }
}

@Composable
fun DashboardReportCard(
    report: ReportEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("report_card_${report.id}"),
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
                    .clip(RoundedCornerShape(14.dp))
                    .background(MinimalSkyAccent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = MinimalNavyPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = report.reportTitle,
                    style = MaterialTheme.typography.bodyLarge.copy(
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
                Spacer(modifier = Modifier.height(3.dp))
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

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View",
                tint = MinimalTextTertiary
            )
        }
    }
}

