package com.example.ui.screens.compare

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.repository.ParameterComparisonItem
import com.example.ui.components.EmptyStateView
import com.example.ui.components.LoadingView
import com.example.ui.components.MedicalDisclaimerCard
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompareScreen(
    viewModel: CompareViewModel,
    onNavigateToUpload: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var baselineExpanded by remember { mutableStateOf(false) }
    var followUpExpanded by remember { mutableStateOf(false) }

    if (uiState.isLoading && uiState.comparisonResult == null) {
        LoadingView(message = "Generating report comparison...", modifier = modifier.fillMaxSize())
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MinimalBackgroundLight)
            .padding(horizontal = 16.dp)
            .testTag("compare_screen")
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Report Comparison",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MinimalNavyPrimary
            )
        )
        Text(
            text = "Select two medical reports to generate side-by-side parameter differences and directional changes.",
            style = MaterialTheme.typography.bodySmall.copy(color = MinimalTextSecondary)
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (uiState.reports.size < 2) {
            EmptyStateView(
                icon = Icons.Default.CompareArrows,
                title = "At least 2 reports required",
                message = "You currently have ${uiState.reports.size} report(s). Upload or import another report to compare measurements side-by-side.",
                actionLabel = "Upload Report",
                onAction = onNavigateToUpload
            )
        } else {
            // Selectors Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Baseline Report Dropdown
                ExposedDropdownMenuBox(
                    expanded = baselineExpanded,
                    onExpandedChange = { baselineExpanded = !baselineExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    val baseline = uiState.reports.find { it.id == uiState.selectedBaselineReportId }
                    OutlinedTextField(
                        value = baseline?.reportTitle ?: "Select Baseline",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Baseline (Report A)", fontSize = 11.sp, color = MinimalTextSecondary) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = baselineExpanded) },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MinimalNavyPrimary,
                            unfocusedBorderColor = MinimalOutlineBorder,
                            focusedContainerColor = MinimalSurfaceLight,
                            unfocusedContainerColor = MinimalSurfaceLight
                        ),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = baselineExpanded,
                        onDismissRequest = { baselineExpanded = false }
                    ) {
                        for (r in uiState.reports) {
                            DropdownMenuItem(
                                text = { Text("${r.reportTitle} (${r.reportDateFormatted})", fontSize = 12.sp) },
                                onClick = {
                                    viewModel.selectBaseline(r.id)
                                    baselineExpanded = false
                                }
                            )
                        }
                    }
                }

                // Follow-up Report Dropdown
                ExposedDropdownMenuBox(
                    expanded = followUpExpanded,
                    onExpandedChange = { followUpExpanded = !followUpExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    val followUp = uiState.reports.find { it.id == uiState.selectedFollowUpReportId }
                    OutlinedTextField(
                        value = followUp?.reportTitle ?: "Select Follow-up",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Follow-up (Report B)", fontSize = 11.sp, color = MinimalTextSecondary) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = followUpExpanded) },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MinimalNavyPrimary,
                            unfocusedBorderColor = MinimalOutlineBorder,
                            focusedContainerColor = MinimalSurfaceLight,
                            unfocusedContainerColor = MinimalSurfaceLight
                        ),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = followUpExpanded,
                        onDismissRequest = { followUpExpanded = false }
                    ) {
                        for (r in uiState.reports) {
                            DropdownMenuItem(
                                text = { Text("${r.reportTitle} (${r.reportDateFormatted})", fontSize = 12.sp) },
                                onClick = {
                                    viewModel.selectFollowUp(r.id)
                                    followUpExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            val comp = uiState.comparisonResult
            if (comp != null) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MinimalSurfaceVariantLight),
                            border = BorderStroke(1.dp, MinimalOutlineLight)
                        ) {
                            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = MinimalNavyPrimary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = comp.summary,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MinimalNavyPrimary,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }

                    items(comp.comparisons) { item ->
                        ComparisonItemCard(item = item, baselineDate = comp.baselineReport.reportDateFormatted, followUpDate = comp.followUpReport.reportDateFormatted)
                    }

                    item {
                        MedicalDisclaimerCard()
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ComparisonItemCard(
    item: ParameterComparisonItem,
    baselineDate: String,
    followUpDate: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("compare_item_${item.normalizedName}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MinimalSurfaceLight),
        border = BorderStroke(1.dp, MinimalOutlineLight)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Title & Change Direction Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MinimalNavyPrimary
                    )
                )
                Text(
                    text = item.changeDirection,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = when (item.changeDirection) {
                            "Increased" -> StatusAttentionRed
                            "Decreased" -> StatusReviewOrange
                            "New" -> StatusOptimalText
                            else -> MinimalTextSecondary
                        },
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Values Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Baseline Column
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Baseline ($baselineDate)", style = MaterialTheme.typography.labelSmall.copy(color = MinimalTextTertiary, fontSize = 10.sp))
                    Text(
                        text = if (item.baselineValue != null) "${item.baselineValue} ${item.unit}" else "Not tested",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = MinimalTextSecondary)
                    )
                    if (item.baselineStatus != null) {
                        Spacer(modifier = Modifier.height(2.dp))
                        StatusBadge(item.baselineStatus)
                    }
                }

                // Diff Column
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Difference", style = MaterialTheme.typography.labelSmall.copy(color = MinimalTextTertiary, fontSize = 10.sp))
                    if (item.absoluteDiff != null) {
                        val sign = if (item.absoluteDiff > 0) "+" else ""
                        Text(
                            text = "$sign${item.absoluteDiff} ${item.unit}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (item.absoluteDiff > 0) StatusAttentionRed else StatusReviewOrange
                            )
                        )
                        if (item.percentageDiff != null) {
                            Text(
                                text = "($sign${item.percentageDiff}%)",
                                style = MaterialTheme.typography.labelSmall.copy(color = MinimalTextSecondary, fontSize = 10.sp)
                            )
                        }
                    } else {
                        Text(text = "--", style = MaterialTheme.typography.bodyMedium.copy(color = MinimalTextTertiary))
                    }
                }

                // Follow-up Column
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(text = "Follow-up ($followUpDate)", style = MaterialTheme.typography.labelSmall.copy(color = MinimalTextTertiary, fontSize = 10.sp))
                    Text(
                        text = if (item.followUpValue != null) "${item.followUpValue} ${item.unit}" else "Not tested",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MinimalNavyPrimary
                        )
                    )
                    if (item.followUpStatus != null) {
                        Spacer(modifier = Modifier.height(2.dp))
                        StatusBadge(item.followUpStatus)
                    }
                }
            }
        }
    }
}

