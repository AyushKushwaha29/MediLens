package com.example.data.ai

import android.graphics.Bitmap
import android.util.Log
import com.example.data.classifier.ReferenceRangeClassifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

data class ExtractedRawParameter(
    val name: String,
    val normalizedName: String,
    val value: Double,
    val displayValue: String,
    val unit: String,
    val referenceRange: String,
    val sourceText: String = "",
    val confidence: Float = 0.95f
)

data class ParameterExplanation(
    val explanationText: String,
    val whyMeasured: String,
    val influencingFactors: String,
    val whenToDiscuss: String
)

data class ExtractionAndAnalysisResult(
    val reportTitle: String,
    val reportType: String,
    val laboratoryName: String,
    val reportDateFormatted: String,
    val parameters: List<ExtractedRawParameter>,
    val summaryText: String,
    val nextSteps: List<String>
)

object MedicalAnalysisService {
    private const val TAG = "MedicalAnalysisService"

    private const val SYSTEM_INSTRUCTION_EXTRACTION = """
You are an expert clinical laboratory report OCR and parameter extraction assistant.
Extract all laboratory test parameters from the medical report image or text.
Return ONLY valid JSON matching this exact structure:
{
  "reportTitle": "Complete Blood Count",
  "reportType": "Hematology / CBC",
  "laboratoryName": "Quest Diagnostics / Core Lab",
  "reportDate": "2026-08-15",
  "summaryText": "Brief 2-3 sentence high-level educational summary of the report contents.",
  "parameters": [
    {
      "name": "Hemoglobin",
      "normalizedName": "hemoglobin",
      "value": 14.2,
      "displayValue": "14.2",
      "unit": "g/dL",
      "referenceRange": "13.0 - 17.0",
      "sourceText": "Hemoglobin 14.2 g/dL (13.0 - 17.0)"
    }
  ],
  "nextSteps": [
    "Consider sharing this report with your primary care provider at your next visit.",
    "Review historical comparisons if previous tests are available."
  ]
}

CRITICAL RULES:
1. Extract ALL test parameters found, including standard and non-standard tests.
2. DO NOT invent or fabricate reference ranges if none are provided on the report.
3. For normalizedName, use lowercase with underscores (e.g. 'hemoglobin', 'wbc', 'platelets', 'fasting_glucose', 'creatinine', 'alt', 'ast', 'total_cholesterol', 'ldl', 'hdl', 'triglycerides', 'tsh', 'vitamin_d', 'vitamin_b12', 'bun', 'potassium', 'sodium', etc.).
4. Parse the exact numerical value and unit.
5. Provide educational, strictly non-diagnostic next steps.
"""

    private const val SYSTEM_INSTRUCTION_CHAT = """
You are MediLens AI, an educational clinical report assistant.
You help patients understand their uploaded laboratory reports.
CRITICAL SAFETY & CLINICAL RULES:
1. MediLens provides EDUCATIONAL INFORMATION ONLY. It is not medical advice, diagnosis, or prescription.
2. ALWAYS use the real parameters, values, reference ranges, and classifications from the provided report context.
3. NEVER fabricate or invent laboratory values or test dates not in the context.
4. If a question asks about tests or values that are NOT in the report, clearly state: "I don't have enough information in this report to answer that reliably."
5. Clearly explain what parameters mean in simple, patient-friendly language.
6. When discussing results outside reference intervals, suggest discussing the finding with a qualified healthcare professional who has complete clinical context.
"""

    /**
     * Process report using Gemini Multimodal AI (for images or scanned PDFs)
     */
    suspend fun analyzeReportWithGemini(
        rawText: String?,
        bitmaps: List<Bitmap>
    ): ExtractionAndAnalysisResult = withContext(Dispatchers.IO) {
        val prompt = if (rawText.isNullOrBlank()) {
            "Please analyze this medical laboratory report image, extract all laboratory parameters, units, reference intervals, and generate educational summary and next steps."
        } else {
            "Please analyze this medical report text content:\n\n$rawText\n\nExtract all laboratory parameters, units, reference intervals, and generate educational summary and next steps."
        }

        val responseText = GeminiApiClient.generateContent(
            prompt = prompt,
            systemInstruction = SYSTEM_INSTRUCTION_EXTRACTION,
            bitmaps = bitmaps
        )

        val cleanJson = extractJsonFromResponse(responseText)
        parseExtractionJson(cleanJson)
    }

    /**
     * Parse extraction JSON into ExtractionAndAnalysisResult
     */
    fun parseExtractionJson(jsonStr: String): ExtractionAndAnalysisResult {
        val obj = JSONObject(jsonStr)
        val reportTitle = obj.optString("reportTitle", "Medical Laboratory Report")
        val reportType = obj.optString("reportType", "Diagnostic Laboratory Report")
        val laboratoryName = obj.optString("laboratoryName", "Clinical Diagnostic Laboratory")
        val reportDateFormatted = obj.optString("reportDate", "Recent Report")
        val summaryText = obj.optString("summaryText", "Report successfully processed and analyzed.")

        val nextStepsList = mutableListOf<String>()
        val nextStepsArray = obj.optJSONArray("nextSteps")
        if (nextStepsArray != null) {
            for (i in 0 until nextStepsArray.length()) {
                val step = nextStepsArray.optString(i)
                if (step.isNotBlank()) nextStepsList.add(step)
            }
        }
        if (nextStepsList.isEmpty()) {
            nextStepsList.add("Review these findings alongside your previous laboratory reports.")
            nextStepsList.add("Discuss any results outside reference intervals with your healthcare provider.")
        }

        val paramList = mutableListOf<ExtractedRawParameter>()
        val paramsArray = obj.optJSONArray("parameters")
        if (paramsArray != null) {
            for (i in 0 until paramsArray.length()) {
                val pObj = paramsArray.getJSONObject(i)
                val name = pObj.optString("name", "Unknown Parameter")
                val normalizedName = pObj.optString("normalizedName", name.lowercase().replace(" ", "_"))
                val valNum = pObj.optDouble("value", 0.0)
                val displayVal = pObj.optString("displayValue", valNum.toString())
                val unit = pObj.optString("unit", "")
                val refRange = pObj.optString("referenceRange", "")
                val srcText = pObj.optString("sourceText", "$name $displayVal $unit ($refRange)")

                paramList.add(
                    ExtractedRawParameter(
                        name = name,
                        normalizedName = normalizedName,
                        value = valNum,
                        displayValue = displayVal,
                        unit = unit,
                        referenceRange = refRange,
                        sourceText = srcText,
                        confidence = 0.96f
                    )
                )
            }
        }

        return ExtractionAndAnalysisResult(
            reportTitle = reportTitle,
            reportType = reportType,
            laboratoryName = laboratoryName,
            reportDateFormatted = reportDateFormatted,
            parameters = paramList,
            summaryText = summaryText,
            nextSteps = nextStepsList
        )
    }

    /**
     * Generate educational explanation for a specific parameter
     */
    suspend fun getParameterExplanation(
        name: String,
        normalizedName: String,
        value: Double,
        unit: String,
        referenceRange: String,
        status: String
    ): ParameterExplanation = withContext(Dispatchers.IO) {
        if (GeminiApiClient.isApiKeyConfigured()) {
            try {
                val prompt = """
Provide educational medical insights for this laboratory parameter:
Parameter: $name
Result: $value $unit
Reference Interval: $referenceRange
Status: $status

Return JSON:
{
  "explanationText": "Educational explanation of what this parameter represents and what this result means relative to the laboratory range.",
  "whyMeasured": "Why this laboratory test is typically ordered.",
  "influencingFactors": "General physiological or lifestyle factors that can influence this parameter (e.g. hydration, diet, stress).",
  "whenToDiscuss": "Educational guidance on when discussing this result with a healthcare professional may be appropriate."
}
DO NOT diagnose any disease, DO NOT prescribe medication, DO NOT claim clinical certainty.
"""
                val resp = GeminiApiClient.generateContent(
                    prompt = prompt,
                    systemInstruction = "You are an educational clinical lab assistant. Return only valid JSON."
                )
                val clean = extractJsonFromResponse(resp)
                val obj = JSONObject(clean)
                return@withContext ParameterExplanation(
                    explanationText = obj.optString("explanationText", getDefaultExplanation(normalizedName, name, status)),
                    whyMeasured = obj.optString("whyMeasured", getDefaultWhyMeasured(normalizedName, name)),
                    influencingFactors = obj.optString("influencingFactors", getDefaultInfluencingFactors(normalizedName)),
                    whenToDiscuss = obj.optString("whenToDiscuss", getDefaultWhenToDiscuss(status))
                )
            } catch (e: Exception) {
                Log.w(TAG, "Gemini call failed for parameter explanation, using rich fallback knowledge base", e)
            }
        }

        // Default clinical educational knowledge base fallback
        ParameterExplanation(
            explanationText = getDefaultExplanation(normalizedName, name, status),
            whyMeasured = getDefaultWhyMeasured(normalizedName, name),
            influencingFactors = getDefaultInfluencingFactors(normalizedName),
            whenToDiscuss = getDefaultWhenToDiscuss(status)
        )
    }

    /**
     * AI Report Chat Q&A
     */
    suspend fun answerReportQuestion(
        reportContext: String,
        chatHistory: List<Pair<String, String>>, // sender to text
        userQuestion: String
    ): String = withContext(Dispatchers.IO) {
        if (GeminiApiClient.isApiKeyConfigured()) {
            try {
                val historyPrompt = StringBuilder()
                historyPrompt.append("=== REPORT DATA CONTEXT ===\n")
                historyPrompt.append(reportContext)
                historyPrompt.append("\n\n=== RECENT CONVERSATION ===\n")
                for ((sender, msg) in chatHistory.takeLast(6)) {
                    historyPrompt.append("$sender: $msg\n")
                }
                historyPrompt.append("USER: $userQuestion\n")
                historyPrompt.append("AI: ")

                val answer = GeminiApiClient.generateContent(
                    prompt = historyPrompt.toString(),
                    systemInstruction = SYSTEM_INSTRUCTION_CHAT
                )
                if (answer.isNotBlank()) return@withContext answer
            } catch (e: Exception) {
                Log.e(TAG, "Chat API failed", e)
            }
        }

        // Local deterministic conversational fallback
        generateLocalChatFallback(reportContext, userQuestion)
    }

    private fun generateLocalChatFallback(reportContext: String, question: String): String {
        val q = question.lowercase()
        return when {
            q.contains("disclaimer") || q.contains("medical advice") -> {
                "MediLens provides educational information only. It is not a diagnosis or medical advice and does not replace evaluation by a qualified healthcare professional."
            }
            q.contains("outside") || q.contains("abnormal") || q.contains("high") || q.contains("low") -> {
                "Based on the laboratory reference intervals in your report, please check the highlighted parameter cards. Any values flagged as HIGH or LOW are noted with their respective reference intervals. Discussing these specific items with your doctor can help contextualize them with your medical history."
            }
            q.contains("doctor") || q.contains("discuss") -> {
                "When speaking with your doctor, you might ask: (1) How do these results compare with my previous baseline? (2) Are any follow-up tests or lifestyle adjustments appropriate? (3) Could any recent supplements or medications have influenced these specific parameters?"
            }
            else -> {
                "Here is what your report indicates: $reportContext\n\nPlease discuss these findings with your healthcare provider for clinical diagnosis and personalized care."
            }
        }
    }

    private fun extractJsonFromResponse(response: String): String {
        val trimmed = response.trim()
        val startIndex = trimmed.indexOf('{')
        val endIndex = trimmed.lastIndexOf('}')
        return if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            trimmed.substring(startIndex, endIndex + 1)
        } else {
            trimmed
        }
    }

    private fun getDefaultExplanation(normalizedName: String, name: String, status: String): String {
        val base = when (normalizedName) {
            "hemoglobin" -> "Hemoglobin is the iron-containing protein in red blood cells that transports oxygen throughout the body."
            "wbc", "white_blood_cells" -> "White blood cells (leukocytes) are a vital component of the immune system that defend against infections and inflammation."
            "platelets" -> "Platelets (thrombocytes) are tiny cell fragments essential for normal blood clotting and vessel repair."
            "rbc", "red_blood_cells" -> "Red blood cells carry oxygen from your lungs to tissues throughout your body."
            "glucose", "fasting_glucose" -> "Blood glucose measures the concentration of sugar available in your bloodstream for cellular energy."
            "hba1c" -> "Hemoglobin A1c reflects the average blood sugar level over the past 2 to 3 months."
            "creatinine" -> "Creatinine is a normal metabolic waste product filtered by the kidneys, used to evaluate renal function."
            "urea", "bun" -> "Blood Urea Nitrogen (BUN) measures urea nitrogen in blood, serving as a marker of kidney and liver metabolic balance."
            "alt" -> "Alanine Aminotransferase (ALT) is an enzyme concentrated primarily in liver cells."
            "ast" -> "Aspartate Aminotransferase (AST) is an enzyme found in the liver, heart, and muscle tissue."
            "ldl" -> "Low-Density Lipoprotein (LDL) cholesterol carries cholesterol from the liver to tissues."
            "hdl" -> "High-Density Lipoprotein (HDL) cholesterol helps clear excess cholesterol from arteries back to the liver."
            "triglycerides" -> "Triglycerides are the most common type of fat stored in the bloodstream and adipose tissue."
            "tsh" -> "Thyroid Stimulating Hormone (TSH) is produced by the pituitary gland to regulate thyroid activity."
            "vitamin_d" -> "Vitamin D is essential for calcium absorption, bone mineralization, and immune modulation."
            "vitamin_b12" -> "Vitamin B12 is essential for red blood cell formation, neurological function, and DNA synthesis."
            else -> "$name is a standard clinical laboratory parameter measured to assess physiological and metabolic balance."
        }
        val statusNote = when (status) {
            "HIGH" -> " Your result is elevated relative to the reported laboratory reference interval."
            "LOW" -> " Your result is below the reported laboratory reference interval."
            "NORMAL" -> " Your result falls within the laboratory's standard reference interval."
            else -> " No explicit numerical reference range was available for comparison."
        }
        return base + statusNote
    }

    private fun getDefaultWhyMeasured(normalizedName: String, name: String): String {
        return when (normalizedName) {
            "hemoglobin", "rbc", "hematocrit" -> "Ordered routinely to evaluate for anemia, fatigue, nutritional balance, or blood loss."
            "wbc" -> "Measured to check for immune response, signs of infection, inflammation, or bone marrow activity."
            "platelets" -> "Evaluated to monitor clotting capability, bleeding risk, or hematologic health."
            "glucose", "hba1c" -> "Measured to screen for metabolic health, insulin resistance, or monitor glycemic management."
            "creatinine", "bun", "urea" -> "Ordered to monitor kidney filtration capacity and fluid balance."
            "alt", "ast" -> "Measured to assess hepatic (liver) cellular integrity and overall metabolic health."
            "ldl", "hdl", "triglycerides", "total_cholesterol" -> "Ordered to evaluate cardiovascular lipid profile and vascular risk markers."
            "tsh" -> "Measured as the primary initial screening test for thyroid gland function (hypothyroidism or hyperthyroidism)."
            "vitamin_d", "vitamin_b12" -> "Ordered to assess nutritional sufficiency, bone health, and energy metabolism."
            else -> "Commonly ordered as part of routine health maintenance or comprehensive metabolic evaluation."
        }
    }

    private fun getDefaultInfluencingFactors(normalizedName: String): String {
        return when (normalizedName) {
            "glucose" -> "Recent meal consumption, fasting duration, acute stress, sleep quality, and physical activity."
            "hemoglobin", "rbc" -> "Hydration status, altitude, iron intake, and physiological fluid balance."
            "wbc" -> "Recent viral/bacterial exposure, vigorous exercise, stress hormones, and inflammation."
            "alt", "ast" -> "Recent alcohol intake, strenuous exercise, medications, supplements, and dietary fats."
            "creatinine" -> "Muscle mass, high protein intake, creatine supplements, dehydration, and intensive exercise."
            "triglycerides" -> "Fasting compliance, recent dietary fat/sugar intake, alcohol, and exercise."
            "tsh" -> "Time of day (circadian variation), acute illness, pregnancy, and biotin supplement use."
            else -> "Hydration levels, dietary changes, recent physical activity, and timing of blood draw."
        }
    }

    private fun getDefaultWhenToDiscuss(status: String): String {
        return when (status) {
            "HIGH", "LOW" -> "It is recommended to review this result with your healthcare professional, especially if sustained across multiple tests or accompanied by symptoms."
            else -> "You can review this result during your regular wellness checkup or scheduled physician review."
        }
    }
}
