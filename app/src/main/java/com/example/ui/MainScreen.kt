package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.pipeline.ReportProcessingPipeline
import com.example.data.repository.AuthRepository
import com.example.data.repository.ReportRepository
import com.example.ui.navigation.Screen
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.screens.auth.AuthViewModel
import com.example.ui.screens.compare.CompareScreen
import com.example.ui.screens.compare.CompareViewModel
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.dashboard.DashboardViewModel
import com.example.ui.screens.reports.ReportDetailScreen
import com.example.ui.screens.reports.ReportDetailViewModel
import com.example.ui.screens.reports.ReportsScreen
import com.example.ui.screens.reports.ReportsViewModel
import com.example.ui.screens.trends.TrendsScreen
import com.example.ui.screens.trends.TrendsViewModel
import com.example.ui.screens.upload.UploadScreen
import com.example.ui.screens.upload.UploadViewModel
import com.example.ui.theme.MinimalBackgroundLight
import com.example.ui.theme.MinimalNavyPrimary
import com.example.ui.theme.MinimalOutlineLight
import com.example.ui.theme.MinimalSkyAccent
import com.example.ui.theme.MinimalSurfaceLight
import com.example.ui.theme.MinimalSurfaceVariantLight
import com.example.ui.theme.MinimalTextPrimary
import com.example.ui.theme.MinimalTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(
    authRepository: AuthRepository,
    reportRepository: ReportRepository,
    processingPipeline: ReportProcessingPipeline
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val currentUserId by authRepository.currentUserId.collectAsStateWithLifecycle(initialValue = null)
    val currentUserName by authRepository.currentUserName.collectAsStateWithLifecycle(initialValue = null)

    val isAuthRoute = currentRoute == Screen.Auth.route || currentUserId == null

    Scaffold(
        containerColor = MinimalBackgroundLight,
        topBar = {
            if (!isAuthRoute) {
                TopAppBar(
                    title = {
                        Text(
                            text = "MediLens",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MinimalNavyPrimary
                        )
                    },
                    navigationIcon = {
                        Icon(
                            imageVector = Icons.Default.HealthAndSafety,
                            contentDescription = "MediLens",
                            tint = MinimalNavyPrimary,
                            modifier = Modifier
                                .padding(start = 16.dp, end = 8.dp)
                                .size(26.dp)
                        )
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                authRepository.logout()
                                navController.navigate(Screen.Auth.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            modifier = Modifier.testTag("btn_logout")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Log out",
                                tint = MinimalTextSecondary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MinimalBackgroundLight)
                )
            }
        },
        bottomBar = {
            if (!isAuthRoute) {
                NavigationBar(
                    containerColor = MinimalSurfaceVariantLight,
                    tonalElevation = 0.dp
                ) {
                    val items = listOf(
                        Triple(Screen.Dashboard, Icons.Default.Dashboard, "Dashboard"),
                        Triple(Screen.Upload, Icons.Default.UploadFile, "Upload"),
                        Triple(Screen.Reports, Icons.Default.Description, "Reports"),
                        Triple(Screen.Trends, Icons.Default.Timeline, "Trends"),
                        Triple(Screen.Compare, Icons.Default.CompareArrows, "Compare")
                    )

                    for ((screen, icon, label) in items) {
                        val selected = currentRoute == screen.route ||
                                (screen == Screen.Reports && currentRoute?.startsWith("report_detail") == true) ||
                                (screen == Screen.Trends && currentRoute?.startsWith("trends") == true)

                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label, fontSize = 11.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MinimalNavyPrimary,
                                selectedTextColor = MinimalNavyPrimary,
                                unselectedIconColor = MinimalTextSecondary,
                                unselectedTextColor = MinimalTextSecondary,
                                indicatorColor = MinimalSkyAccent
                            ),
                            modifier = Modifier.testTag("nav_item_${label.lowercase()}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MinimalBackgroundLight)
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = if (currentUserId != null) Screen.Dashboard.route else Screen.Auth.route
            ) {
                // Auth Screen
                composable(Screen.Auth.route) {
                    val authVm = AuthViewModel(authRepository)
                    AuthScreen(
                        viewModel = authVm,
                        onAuthSuccess = {
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Auth.route) { inclusive = true }
                            }
                        }
                    )
                }

                // Dashboard Screen
                composable(Screen.Dashboard.route) {
                    val dashboardVm = DashboardViewModel(authRepository, reportRepository, processingPipeline)
                    DashboardScreen(
                        viewModel = dashboardVm,
                        onNavigateToUpload = { navController.navigate(Screen.Upload.route) },
                        onNavigateToReports = { navController.navigate(Screen.Reports.route) },
                        onNavigateToReportDetail = { reportId -> navController.navigate(Screen.ReportDetail.createRoute(reportId)) },
                        onNavigateToTrends = { param -> navController.navigate(Screen.Trends.createRoute(param)) },
                        onNavigateToCompare = { navController.navigate(Screen.Compare.route) }
                    )
                }

                // Upload Screen
                composable(Screen.Upload.route) {
                    val uploadVm = UploadViewModel(authRepository, processingPipeline)
                    UploadScreen(
                        viewModel = uploadVm,
                        onReportProcessed = { reportId ->
                            navController.navigate(Screen.ReportDetail.createRoute(reportId)) {
                                popUpTo(Screen.Upload.route) { inclusive = false }
                            }
                        }
                    )
                }

                // Reports Screen
                composable(Screen.Reports.route) {
                    val reportsVm = ReportsViewModel(authRepository, reportRepository)
                    ReportsScreen(
                        viewModel = reportsVm,
                        onNavigateToReportDetail = { reportId ->
                            navController.navigate(Screen.ReportDetail.createRoute(reportId))
                        },
                        onNavigateToUpload = { navController.navigate(Screen.Upload.route) }
                    )
                }

                // Report Detail Screen
                composable(
                    route = Screen.ReportDetail.route,
                    arguments = listOf(navArgument("reportId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val reportId = backStackEntry.arguments?.getString("reportId") ?: ""
                    val detailVm = ReportDetailViewModel(reportId, authRepository, reportRepository)
                    ReportDetailScreen(
                        viewModel = detailVm,
                        onBack = { navController.popBackStack() },
                        onNavigateToTrend = { param -> navController.navigate(Screen.Trends.createRoute(param)) }
                    )
                }

                // Trends Screen
                composable(
                    route = Screen.Trends.route,
                    arguments = listOf(navArgument("param") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    })
                ) { backStackEntry ->
                    val param = backStackEntry.arguments?.getString("param")
                    val trendsVm = TrendsViewModel(authRepository, reportRepository)
                    TrendsScreen(
                        viewModel = trendsVm,
                        initialParameter = param,
                        onNavigateToUpload = { navController.navigate(Screen.Upload.route) }
                    )
                }

                // Compare Screen
                composable(Screen.Compare.route) {
                    val compareVm = CompareViewModel(authRepository, reportRepository)
                    CompareScreen(
                        viewModel = compareVm,
                        onNavigateToUpload = { navController.navigate(Screen.Upload.route) }
                    )
                }
            }
        }
    }
}

