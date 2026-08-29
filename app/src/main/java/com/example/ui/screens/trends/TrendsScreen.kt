package com.example.ui.screens.trends

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.EmptyStateView
import com.example.ui.components.LoadingView
import com.example.ui.components.MedicalDisclaimerCard
import com.example.ui.components.TrendChart
import com.example.ui.theme.MinimalBackgroundLight
import com.example.ui.theme.MinimalNavyPrimary
import com.example.ui.theme.MinimalOutlineLight
import com.example.ui.theme.MinimalSkyAccent
import com.example.ui.theme.MinimalSurfaceLight
import com.example.ui.theme.MinimalTextPrimary
import com.example.ui.theme.MinimalTextSecondary
import com.example.ui.theme.MinimalTextTertiary

@Composable
fun TrendsScreen(
    viewModel: TrendsViewModel,
    initialParameter: String?,
    onNavigateToUpload: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(initialParameter) {
        if (!initialParameter.isNullOrBlank()) {
            viewModel.selectParameter(initialParameter)
        }
    }

    if (uiState.isLoading && uiState.trendAnalysis == null) {
        LoadingView(message = "Calculating historical trends & projections...", modifier = modifier.fillMaxSize())
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MinimalBackgroundLight)
            .padding(horizontal = 16.dp)
            .testTag("trends_screen")
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Health Trends & Statistical Projections",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MinimalNavyPrimary
            )
        )
        Text(
            text = "Track how laboratory parameters evolve across tests, view changes over time, and inspect statistical linear regression estimates.",
            style = MaterialTheme.typography.bodySmall.copy(color = MinimalTextSecondary)
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (uiState.trackedParameters.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.Timeline,
                title = "No parameter history available",
                message = "Upload at least 2 medical reports with identical laboratory parameters to view trend graphs and mathematical projections.",
                actionLabel = "Upload Report",
                onAction = onNavigateToUpload
            )
        } else {
            // Parameter Selector Horizontal Carousel
            Text(
                text = "Select Laboratory Parameter:",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = MinimalNavyPrimary)
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(uiState.trackedParameters) { param ->
                    val isSelected = uiState.selectedParameterNormalized == param.normalizedName
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectParameter(param.normalizedName) },
                        label = {
                            Text(
                                text = param.name,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MinimalNavyPrimary,
                            selectedLabelColor = Color.White,
                            containerColor = MinimalSurfaceLight,
                            labelColor = MinimalTextSecondary
                        ),
                        modifier = Modifier.testTag("chip_param_${param.normalizedName}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                val trend = uiState.trendAnalysis
                if (trend != null) {
                    item {
                        TrendChart(trendAnalysis = trend)
                    }

                    // Educational Trend Context Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MinimalSurfaceLight),
                            border = BorderStroke(1.dp, MinimalOutlineLight)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = MinimalNavyPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Understanding Trend Directions",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MinimalNavyPrimary
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "An 'Increasing' or 'Decreasing' direction indicates mathematical trajectory between tests. Direction alone does not automatically signify a positive or negative clinical outcome without complete context from your healthcare provider.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MinimalTextSecondary, lineHeight = 18.sp)
                                )
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
    }
}

