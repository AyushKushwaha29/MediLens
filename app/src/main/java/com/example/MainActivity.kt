package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.data.local.MediLensDatabase
import com.example.data.local.SessionManager
import com.example.data.pipeline.ReportProcessingPipeline
import com.example.data.repository.AuthRepository
import com.example.data.repository.ReportRepository
import com.example.ui.MainApp
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var database: MediLensDatabase
    private lateinit var authRepository: AuthRepository
    private lateinit var reportRepository: ReportRepository
    private lateinit var processingPipeline: ReportProcessingPipeline

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        sessionManager = SessionManager(applicationContext)
        database = MediLensDatabase.getDatabase(applicationContext)
        authRepository = AuthRepository(database.userDao(), sessionManager)
        reportRepository = ReportRepository(
            reportDao = database.reportDao(),
            parameterDao = database.medicalParameterDao(),
            chatDao = database.chatMessageDao()
        )
        processingPipeline = ReportProcessingPipeline(
            context = applicationContext,
            database = database
        )

        setContent {
            MyApplicationTheme {
                MainApp(
                    authRepository = authRepository,
                    reportRepository = reportRepository,
                    processingPipeline = processingPipeline
                )
            }
        }
    }
}

