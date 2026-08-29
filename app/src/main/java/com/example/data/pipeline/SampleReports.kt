package com.example.data.pipeline

import com.example.data.ai.ExtractedRawParameter
import com.example.data.ai.ExtractionAndAnalysisResult

data class SampleReportTemplate(
    val title: String,
    val reportType: String,
    val laboratoryName: String,
    val dateString: String,
    val dateMillis: Long,
    val summaryText: String,
    val parameters: List<ExtractedRawParameter>,
    val nextSteps: List<String>
)

object SampleReports {

    fun getSamplePresets(): List<SampleReportTemplate> {
        val now = System.currentTimeMillis()
        val oneMonthAgo = now - (30L * 24 * 60 * 60 * 1000)
        val fourMonthsAgo = now - (120L * 24 * 60 * 60 * 1000)
        val sevenMonthsAgo = now - (210L * 24 * 60 * 60 * 1000)

        return listOf(
            // Baseline CBC (7 Months Ago)
            SampleReportTemplate(
                title = "Complete Blood Count (CBC) - Baseline",
                reportType = "Hematology",
                laboratoryName = "Metropolis Clinical Diagnostic Laboratories",
                dateString = "January 2026",
                dateMillis = sevenMonthsAgo,
                summaryText = "Baseline Complete Blood Count test measuring red cells, white cells, and platelet indices.",
                parameters = listOf(
                    ExtractedRawParameter("Hemoglobin", "hemoglobin", 10.2, "10.2", "g/dL", "13.0 - 17.0", "Hemoglobin 10.2 g/dL (13.0-17.0)"),
                    ExtractedRawParameter("White Blood Cells (WBC)", "wbc", 6.8, "6.8", "10^3/uL", "4.5 - 11.0", "WBC 6.8 10^3/uL (4.5-11.0)"),
                    ExtractedRawParameter("Platelets", "platelets", 240.0, "240", "10^3/uL", "150 - 450", "Platelets 240 10^3/uL (150-450)"),
                    ExtractedRawParameter("Red Blood Cells (RBC)", "rbc", 3.8, "3.8", "10^6/uL", "4.2 - 5.8", "RBC 3.8 10^6/uL (4.2-5.8)"),
                    ExtractedRawParameter("Hematocrit", "hematocrit", 32.0, "32.0", "%", "38.5 - 50.0", "Hematocrit 32.0% (38.5-50.0)"),
                    ExtractedRawParameter("MCV", "mcv", 78.0, "78.0", "fL", "80.0 - 100.0", "MCV 78.0 fL (80.0-100.0)")
                ),
                nextSteps = listOf(
                    "Discuss low hemoglobin and hematocrit findings with your healthcare provider.",
                    "Review dietary iron intake and potential nutritional evaluation.",
                    "Schedule follow-up hematology panel in 3 months."
                )
            ),
            // Follow-up CBC (4 Months Ago)
            SampleReportTemplate(
                title = "Complete Blood Count (CBC) - Follow-up 1",
                reportType = "Hematology",
                laboratoryName = "Metropolis Clinical Diagnostic Laboratories",
                dateString = "April 2026",
                dateMillis = fourMonthsAgo,
                summaryText = "Follow-up hematology test showing gradual improvement in hemoglobin and red cell indices.",
                parameters = listOf(
                    ExtractedRawParameter("Hemoglobin", "hemoglobin", 10.8, "10.8", "g/dL", "13.0 - 17.0", "Hemoglobin 10.8 g/dL (13.0-17.0)"),
                    ExtractedRawParameter("White Blood Cells (WBC)", "wbc", 7.2, "7.2", "10^3/uL", "4.5 - 11.0", "WBC 7.2 10^3/uL (4.5-11.0)"),
                    ExtractedRawParameter("Platelets", "platelets", 255.0, "255", "10^3/uL", "150 - 450", "Platelets 255 10^3/uL (150-450)"),
                    ExtractedRawParameter("Red Blood Cells (RBC)", "rbc", 4.1, "4.1", "10^6/uL", "4.2 - 5.8", "RBC 4.1 10^6/uL (4.2-5.8)"),
                    ExtractedRawParameter("Hematocrit", "hematocrit", 34.5, "34.5", "%", "38.5 - 50.0", "Hematocrit 34.5% (38.5-50.0)"),
                    ExtractedRawParameter("MCV", "mcv", 81.2, "81.2", "fL", "80.0 - 100.0", "MCV 81.2 fL (80.0-100.0)")
                ),
                nextSteps = listOf(
                    "Hemoglobin is trending positively (+0.6 g/dL). Continue discussing nutritional regimen with provider.",
                    "Repeat CBC check in 3-4 months to monitor steady recovery."
                )
            ),
            // Recent CBC (August 2026)
            SampleReportTemplate(
                title = "Complete Blood Count (CBC) - Recent",
                reportType = "Hematology",
                laboratoryName = "Metropolis Clinical Diagnostic Laboratories",
                dateString = "August 2026",
                dateMillis = now - (7L * 24 * 60 * 60 * 1000),
                summaryText = "Recent Complete Blood Count indicating steady recovery toward normal physiological range.",
                parameters = listOf(
                    ExtractedRawParameter("Hemoglobin", "hemoglobin", 11.4, "11.4", "g/dL", "13.0 - 17.0", "Hemoglobin 11.4 g/dL (13.0-17.0)"),
                    ExtractedRawParameter("White Blood Cells (WBC)", "wbc", 7.0, "7.0", "10^3/uL", "4.5 - 11.0", "WBC 7.0 10^3/uL (4.5-11.0)"),
                    ExtractedRawParameter("Platelets", "platelets", 248.0, "248", "10^3/uL", "150 - 450", "Platelets 248 10^3/uL (150-450)"),
                    ExtractedRawParameter("Red Blood Cells (RBC)", "rbc", 4.3, "4.3", "10^6/uL", "4.2 - 5.8", "RBC 4.3 10^6/uL (4.2-5.8)"),
                    ExtractedRawParameter("Hematocrit", "hematocrit", 36.8, "36.8", "%", "38.5 - 50.0", "Hematocrit 36.8% (38.5-50.0)"),
                    ExtractedRawParameter("MCV", "mcv", 83.5, "83.5", "fL", "80.0 - 100.0", "MCV 83.5 fL (80.0-100.0)")
                ),
                nextSteps = listOf(
                    "Review positive upward trend (+1.2 g/dL over 7 months) with your physician.",
                    "Verify iron indices / ferritin at next routine consultation."
                )
            ),
            // Comprehensive Metabolic & Lipid Panel (Recent)
            SampleReportTemplate(
                title = "Comprehensive Metabolic & Lipid Panel",
                reportType = "Biochemistry & Lipids",
                laboratoryName = "Quest Health Diagnostics",
                dateString = "July 2026",
                dateMillis = oneMonthAgo,
                summaryText = "Comprehensive metabolic profile evaluating blood glucose, kidney function, liver enzymes, and lipid profile.",
                parameters = listOf(
                    ExtractedRawParameter("Fasting Glucose", "glucose", 104.0, "104", "mg/dL", "70 - 99", "Fasting Glucose 104 mg/dL (70-99)"),
                    ExtractedRawParameter("HbA1c", "hba1c", 5.8, "5.8", "%", "4.0 - 5.6", "HbA1c 5.8% (4.0-5.6)"),
                    ExtractedRawParameter("Creatinine", "creatinine", 0.95, "0.95", "mg/dL", "0.6 - 1.2", "Creatinine 0.95 mg/dL (0.6-1.2)"),
                    ExtractedRawParameter("Blood Urea Nitrogen (BUN)", "bun", 14.0, "14", "mg/dL", "7 - 20", "BUN 14 mg/dL (7-20)"),
                    ExtractedRawParameter("ALT (Alanine Aminotransferase)", "alt", 32.0, "32", "U/L", "7 - 56", "ALT 32 U/L (7-56)"),
                    ExtractedRawParameter("AST (Aspartate Aminotransferase)", "ast", 28.0, "28", "U/L", "10 - 40", "AST 28 U/L (10-40)"),
                    ExtractedRawParameter("Total Cholesterol", "total_cholesterol", 215.0, "215", "mg/dL", "< 200", "Total Cholesterol 215 mg/dL (<200)"),
                    ExtractedRawParameter("LDL Cholesterol", "ldl", 138.0, "138", "mg/dL", "< 100", "LDL 138 mg/dL (<100)"),
                    ExtractedRawParameter("HDL Cholesterol", "hdl", 48.0, "48", "mg/dL", "> 40", "HDL 48 mg/dL (>40)"),
                    ExtractedRawParameter("Triglycerides", "triglycerides", 165.0, "165", "mg/dL", "< 150", "Triglycerides 165 mg/dL (<150)"),
                    ExtractedRawParameter("TSH", "tsh", 2.1, "2.1", "mIU/L", "0.4 - 4.0", "TSH 2.1 mIU/L (0.4-4.0)"),
                    ExtractedRawParameter("Vitamin D (25-OH)", "vitamin_d", 22.5, "22.5", "ng/mL", "30.0 - 100.0", "Vitamin D 22.5 ng/mL (30.0-100.0)"),
                    ExtractedRawParameter("Vitamin B12", "vitamin_b12", 480.0, "480", "pg/mL", "200 - 900", "Vitamin B12 480 pg/mL (200-900)")
                ),
                nextSteps = listOf(
                    "Fasting glucose and HbA1c are slightly elevated. Consider reviewing dietary habits with your physician.",
                    "LDL and Triglycerides exceed standard desirable targets. Discuss cardiovascular lifestyle strategies.",
                    "Vitamin D is below sufficiency range (22.5 ng/mL). Consult healthcare provider regarding supplementation."
                )
            )
        )
    }
}
