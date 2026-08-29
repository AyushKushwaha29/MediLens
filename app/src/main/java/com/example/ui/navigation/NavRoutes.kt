package com.example.ui.navigation

sealed class Screen(val route: String, val title: String) {
    data object Auth : Screen("auth", "Authentication")
    data object Dashboard : Screen("dashboard", "Dashboard")
    data object Upload : Screen("upload", "Upload")
    data object Reports : Screen("reports", "Reports")
    data object ReportDetail : Screen("report_detail/{reportId}", "Report Details") {
        fun createRoute(reportId: String) = "report_detail/$reportId"
    }
    data object Trends : Screen("trends?param={param}", "Trends") {
        fun createRoute(param: String? = null) = if (param != null) "trends?param=$param" else "trends"
    }
    data object Compare : Screen("compare", "Compare")
}
