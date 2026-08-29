package com.example.ui.screens.reports

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.MedicalParameterEntity
import com.example.ui.components.EmptyStateView
import com.example.ui.components.LoadingView
import com.example.ui.components.MedicalDisclaimerCard
import com.example.ui.components.NextStepsCard
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
import com.example.ui.theme.StatusAttentionRed
import com.example.ui.theme.StatusOptimalText
import com.example.ui.theme.StatusReviewOrange

@Composable
fun ReportDetailScreen(
    viewModel: ReportDetailViewModel,
    onBack: () -> Unit,
    onNavigateToTrend: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var activeDetailTab by remember { mutableIntStateOf(0) } // 0 = Parameters, 1 = Next Steps, 2 = AI Chat
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (uiState.isLoading) {
        LoadingView(message = "Loading report details...", modifier = modifier.fillMaxSize())
        return
    }

    val report = uiState.report
    if (report == null) {
        EmptyStateView(
            icon = Icons.Default.Info,
            title = "Report not found",
            message = "This report may have been removed.",
            actionLabel = "Go Back",
            onAction = onBack,
            modifier = modifier.fillMaxSize()
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MinimalBackgroundLight)
            .padding(horizontal = 16.dp)
            .testTag("report_detail_screen")
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Top Navigation Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MinimalNavyPrimary)
            }
            Spacer(modifier = Modifier.width(4.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = report.reportTitle,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MinimalNavyPrimary
                    ),
                    maxLines = 1
                )
                Text(
                    text = "${report.reportDateFormatted} • ${report.laboratoryName}",
                    style = MaterialTheme.typography.bodySmall.copy(color = MinimalTextSecondary)
                )
            }
            IconButton(onClick = { showDeleteConfirm = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Report", tint = MinimalTextTertiary)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Sub Tabs: Parameters | Next Steps | AI Chat
        TabRow(
            selectedTabIndex = activeDetailTab,
            containerColor = MinimalSurfaceVariantLight,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[activeDetailTab]),
                    color = MinimalNavyPrimary
                )
            },
            modifier = Modifier.clip(RoundedCornerShape(16.dp))
        ) {
            Tab(
                selected = activeDetailTab == 0,
                onClick = { activeDetailTab = 0 },
                text = {
                    Text(
                        text = "Parameters (${uiState.parameters.size})",
                        fontWeight = if (activeDetailTab == 0) FontWeight.Bold else FontWeight.Medium,
                        color = if (activeDetailTab == 0) MinimalNavyPrimary else MinimalTextSecondary,
                        fontSize = 13.sp
                    )
                },
                modifier = Modifier.testTag("tab_parameters")
            )
            Tab(
                selected = activeDetailTab == 1,
                onClick = { activeDetailTab = 1 },
                text = {
                    Text(
                        text = "Next Steps",
                        fontWeight = if (activeDetailTab == 1) FontWeight.Bold else FontWeight.Medium,
                        color = if (activeDetailTab == 1) MinimalNavyPrimary else MinimalTextSecondary,
                        fontSize = 13.sp
                    )
                },
                modifier = Modifier.testTag("tab_next_steps")
            )
            Tab(
                selected = activeDetailTab == 2,
                onClick = { activeDetailTab = 2 },
                text = {
                    Text(
                        text = "AI Q&A Chat",
                        fontWeight = if (activeDetailTab == 2) FontWeight.Bold else FontWeight.Medium,
                        color = if (activeDetailTab == 2) MinimalNavyPrimary else MinimalTextSecondary,
                        fontSize = 13.sp
                    )
                },
                modifier = Modifier.testTag("tab_ai_chat")
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (activeDetailTab) {
            0 -> {
                // Parameters Tab
                Column(modifier = Modifier.weight(1f)) {
                    // Filter Chips Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = uiState.selectedFilter == "ALL",
                            onClick = { viewModel.setFilter("ALL") },
                            label = { Text("All (${uiState.parameters.size})") },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MinimalNavyPrimary,
                                selectedLabelColor = Color.White,
                                containerColor = MinimalSurfaceLight,
                                labelColor = MinimalTextSecondary
                            )
                        )
                        FilterChip(
                            selected = uiState.selectedFilter == "ABNORMAL",
                            onClick = { viewModel.setFilter("ABNORMAL") },
                            label = { Text("Outside Range (${report.abnormalCount})") },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MinimalNavyPrimary,
                                selectedLabelColor = Color.White,
                                containerColor = MinimalSurfaceLight,
                                labelColor = MinimalTextSecondary
                            )
                        )
                        FilterChip(
                            selected = uiState.selectedFilter == "NORMAL",
                            onClick = { viewModel.setFilter("NORMAL") },
                            label = { Text("Normal") },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MinimalNavyPrimary,
                                selectedLabelColor = Color.White,
                                containerColor = MinimalSurfaceLight,
                                labelColor = MinimalTextSecondary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(uiState.filteredParameters) { param ->
                            ExpandableParameterCard(
                                param = param,
                                onNavigateToTrend = { onNavigateToTrend(param.normalizedName) }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            MedicalDisclaimerCard()
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }

            1 -> {
                // Summary & Next Steps Tab
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MinimalSurfaceLight),
                            border = BorderStroke(1.dp, MinimalOutlineLight)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Report Summary",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MinimalNavyPrimary
                                    )
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = report.summaryText,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MinimalTextSecondary,
                                        lineHeight = 22.sp
                                    )
                                )
                            }
                        }
                    }

                    item {
                        NextStepsCard(nextSteps = uiState.nextSteps)
                    }

                    item {
                        MedicalDisclaimerCard()
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            2 -> {
                // AI Report Chat Tab
                ReportChatSection(
                    chatMessages = uiState.chatMessages,
                    chatInput = uiState.chatInput,
                    isChatLoading = uiState.isChatLoading,
                    onChatInputChange = { viewModel.onChatInputChange(it) },
                    onSend = { viewModel.sendChatMessage() },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    // Delete Dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete This Report?", color = MinimalNavyPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete '${report.reportTitle}'? This action cannot be undone.", color = MinimalTextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        viewModel.deleteThisReport(onBack)
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = StatusAttentionRed)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = MinimalTextSecondary)
                }
            }
        )
    }
}

@Composable
fun ExpandableParameterCard(
    param: MedicalParameterEntity,
    onNavigateToTrend: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("param_card_${param.normalizedName}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MinimalSurfaceLight),
        border = BorderStroke(1.dp, MinimalOutlineLight)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row (Click to toggle expansion)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = param.name,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MinimalNavyPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Ref: ${param.referenceRange.ifBlank { "Not specified" }}",
                        style = MaterialTheme.typography.bodySmall.copy(color = MinimalTextSecondary)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${param.displayValue} ${param.unit}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = when (param.status) {
                                    "HIGH" -> StatusAttentionRed
                                    "LOW" -> StatusReviewOrange
                                    "NORMAL" -> StatusOptimalText
                                    else -> MinimalTextSecondary
                                }
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        StatusBadge(param.status)
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand",
                        tint = MinimalTextTertiary
                    )
                }
            }

            // Expanded Educational Details
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 14.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MinimalOutlineLight)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Meaning & Explanation
                    if (param.explanationText.isNotBlank()) {
                        DetailSection(
                            title = "What This Result Means",
                            body = param.explanationText,
                            icon = Icons.Default.Info,
                            iconColor = MinimalNavyPrimary
                        )
                    }

                    // Why Measured
                    if (param.whyMeasured.isNotBlank()) {
                        DetailSection(
                            title = "Why This Is Measured",
                            body = param.whyMeasured,
                            icon = Icons.Default.Lightbulb,
                            iconColor = MinimalNavyPrimary
                        )
                    }

                    // Influencing Factors
                    if (param.influencingFactors.isNotBlank()) {
                        DetailSection(
                            title = "General Influencing Factors",
                            body = param.influencingFactors,
                            icon = Icons.Default.HelpOutline,
                            iconColor = MinimalNavyPrimary
                        )
                    }

                    // When to Discuss
                    if (param.whenToDiscuss.isNotBlank()) {
                        DetailSection(
                            title = "Physician Discussion Guidance",
                            body = param.whenToDiscuss,
                            icon = Icons.Default.Warning,
                            iconColor = StatusAttentionRed
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = onNavigateToTrend,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MinimalSurfaceVariantLight,
                            contentColor = MinimalNavyPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Timeline, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("View Longitudinal History & Trend Chart", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailSection(
    title: String,
    body: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color
) {
    Column(modifier = Modifier.padding(bottom = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MinimalNavyPrimary
                )
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodySmall.copy(color = MinimalTextSecondary, lineHeight = 18.sp)
        )
    }
}

@Composable
fun ReportChatSection(
    chatMessages: List<ChatMessageEntity>,
    chatInput: String,
    isChatLoading: Boolean,
    onChatInputChange: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Quick prompts suggestions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickChip(text = "Are any tests outside range?") { onChatInputChange("Are any test parameters outside the laboratory range?") }
            QuickChip(text = "Questions for my doctor") { onChatInputChange("What questions should I ask my doctor about these results?") }
        }

        // Chat Message List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .background(MinimalSurfaceVariantLight, RoundedCornerShape(20.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (chatMessages.isEmpty()) {
                item {
                    Text(
                        text = "Ask questions about this specific report. MediLens answers based strictly on the parameters extracted above.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MinimalTextSecondary),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            items(chatMessages) { msg ->
                val isUser = msg.sender == "USER"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isUser) 16.dp else 4.dp,
                                    bottomEnd = if (isUser) 4.dp else 16.dp
                                )
                            )
                            .background(if (isUser) MinimalNavyPrimary else MinimalSurfaceLight)
                            .padding(14.dp)
                    ) {
                        Text(
                            text = msg.message,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = if (isUser) Color.White else MinimalNavyPrimary,
                                lineHeight = 20.sp
                            )
                        )
                    }
                }
            }

            if (isChatLoading) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MinimalNavyPrimary, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("MediLens AI is reviewing report context...", style = MaterialTheme.typography.bodySmall.copy(color = MinimalTextSecondary))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Chat Input Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = chatInput,
                onValueChange = onChatInputChange,
                placeholder = { Text("Ask about this report...", color = MinimalTextTertiary) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("input_report_chat"),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MinimalNavyPrimary,
                    unfocusedBorderColor = MinimalOutlineBorder,
                    focusedContainerColor = MinimalSurfaceLight,
                    unfocusedContainerColor = MinimalSurfaceLight
                ),
                maxLines = 3
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onSend,
                enabled = chatInput.isNotBlank() && !isChatLoading,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (chatInput.isNotBlank() && !isChatLoading) MinimalNavyPrimary else MinimalOutlineLight)
                    .testTag("btn_send_chat")
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White)
            }
        }
    }
}

@Composable
private fun QuickChip(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MinimalSkyAccent)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                color = MinimalNavyPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
            )
        )
    }
}

