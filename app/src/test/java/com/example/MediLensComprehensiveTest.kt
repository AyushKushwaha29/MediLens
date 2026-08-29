package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.classifier.ReferenceRangeClassifier
import com.example.data.local.MediLensDatabase
import com.example.data.local.SessionManager
import com.example.data.local.entity.MedicalParameterEntity
import com.example.data.local.entity.ReportEntity
import com.example.data.local.entity.UserEntity
import com.example.data.pipeline.ReportProcessingPipeline
import com.example.data.pipeline.SampleReports
import com.example.data.repository.AuthRepository
import com.example.data.repository.AuthResult
import com.example.data.repository.ReportRepository
import com.example.data.trend.HistoricalDataPoint
import com.example.data.trend.TrendAnalysisEngine
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MediLensComprehensiveTest {

    private lateinit var context: Context
    private lateinit var database: MediLensDatabase
    private lateinit var sessionManager: SessionManager
    private lateinit var authRepository: AuthRepository
    private lateinit var reportRepository: ReportRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, MediLensDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        sessionManager = SessionManager(context)
        sessionManager.clearSession()
        authRepository = AuthRepository(database.userDao(), sessionManager)
        reportRepository = ReportRepository(
            database.reportDao(),
            database.medicalParameterDao(),
            database.chatMessageDao()
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testAppNameString() {
        val appName = context.getString(R.string.app_name)
        assertEquals("MediLens", appName)
    }

    @Test
    fun testDeterministicReferenceRangeClassification() {
        // Range 13.0 - 17.0
        val lowResult = ReferenceRangeClassifier.classify(10.2, "13.0 - 17.0")
        assertEquals("LOW", lowResult.status)
        assertEquals(13.0, lowResult.min)
        assertEquals(17.0, lowResult.max)

        val normalResult = ReferenceRangeClassifier.classify(14.5, "13.0 - 17.0")
        assertEquals("NORMAL", normalResult.status)

        val highResult = ReferenceRangeClassifier.classify(18.2, "13.0 - 17.0")
        assertEquals("HIGH", highResult.status)

        // Upper threshold "< 200"
        val normalCholesterol = ReferenceRangeClassifier.classify(180.0, "< 200")
        assertEquals("NORMAL", normalCholesterol.status)

        val highCholesterol = ReferenceRangeClassifier.classify(220.0, "< 200")
        assertEquals("HIGH", highCholesterol.status)

        // Lower threshold "> 40"
        val lowHdl = ReferenceRangeClassifier.classify(35.0, "> 40")
        assertEquals("LOW", lowHdl.status)

        val normalHdl = ReferenceRangeClassifier.classify(55.0, "> 40")
        assertEquals("NORMAL", normalHdl.status)

        // Missing range -> UNKNOWN
        val unknownResult = ReferenceRangeClassifier.classify(5.0, null)
        assertEquals("UNKNOWN", unknownResult.status)
    }

    @Test
    fun testAuthenticationAndPersistentSession() = runBlocking {
        val registerRes = authRepository.register("Alice Doe", "alice@example.com", "secure123")
        assertTrue(registerRes is AuthResult.Success)

        val user = (registerRes as AuthResult.Success).user
        assertEquals("Alice Doe", user.name)
        assertEquals("alice@example.com", user.email)
        assertTrue(authRepository.isLoggedIn())

        // Duplicate registration check
        val dupRes = authRepository.register("Alice 2", "alice@example.com", "password")
        assertTrue(dupRes is AuthResult.Error)

        // Logout
        authRepository.logout()
        assertTrue(!authRepository.isLoggedIn())

        // Re-login
        val loginRes = authRepository.login("alice@example.com", "secure123")
        assertTrue(loginRes is AuthResult.Success)
        assertTrue(authRepository.isLoggedIn())
    }

    @Test
    fun testSampleReportPipelineAndTrends() = runBlocking {
        val userRes = authRepository.register("Bob", "bob@example.com", "password")
        val user = (userRes as AuthResult.Success).user

        val pipeline = ReportProcessingPipeline(context, database)
        val presets = SampleReports.getSamplePresets()

        // Import 3 CBC reports across 7 months
        val cbc1 = presets[0]
        val cbc2 = presets[1]
        val cbc3 = presets[2]

        val id1 = pipeline.importSampleReport(user.id, cbc1)
        val id2 = pipeline.importSampleReport(user.id, cbc2)
        val id3 = pipeline.importSampleReport(user.id, cbc3)

        assertNotNull(id1)
        assertNotNull(id2)
        assertNotNull(id3)

        // Check hemoglobin trend analysis
        val trend = reportRepository.getTrendForParameter(user.id, "hemoglobin")
        assertEquals("hemoglobin", trend.normalizedName)
        assertEquals(3, trend.observationCount)
        assertEquals(11.4, trend.currentValue)
        assertEquals(10.8, trend.previousValue)
        assertEquals(0.6, trend.absoluteChange)
        assertEquals("Increasing", trend.trendDirection)

        // Check statistical linear regression projection
        assertTrue(trend.projection.isProjectable)
        assertNotNull(trend.projection.projectedValue)
        assertTrue(trend.projection.projectedValue!! > 11.4)
    }

    @Test
    fun testReportComparison() = runBlocking {
        val userRes = authRepository.register("Carol", "carol@example.com", "password")
        val user = (userRes as AuthResult.Success).user

        val pipeline = ReportProcessingPipeline(context, database)
        val presets = SampleReports.getSamplePresets()

        val idBaseline = pipeline.importSampleReport(user.id, presets[0])
        val idFollowUp = pipeline.importSampleReport(user.id, presets[2])

        val comp = reportRepository.compareReports(user.id, idBaseline, idFollowUp)
        assertNotNull(comp)
        assertTrue(comp!!.comparisons.isNotEmpty())

        val hgbComp = comp.comparisons.first { it.normalizedName == "hemoglobin" }
        assertEquals(10.2, hgbComp.baselineValue)
        assertEquals(11.4, hgbComp.followUpValue)
        assertEquals(1.2, hgbComp.absoluteDiff)
        assertEquals("Increased", hgbComp.changeDirection)
    }
}
