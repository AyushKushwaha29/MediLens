Build a production-quality full-stack AI application called "MediLens".

MediLens is an AI-powered Medical Report Explainer that helps users understand laboratory medical reports in simple, non-diagnostic language.

IMPORTANT:
This application is for educational/informational purposes only. It must NEVER claim to diagnose diseases, prescribe medication, or replace a doctor. It should clearly communicate uncertainty and recommend consulting a qualified healthcare professional when appropriate.

==================================================
1. PRODUCT VISION
==================================================

Create a modern SaaS-style web application where a user can:

1. Create an account / log in
2. Upload a medical report in PDF, JPG, or PNG format
3. Extract text and tables from the report
4. Detect and structure medical parameters
5. Read reference ranges directly from the report whenever available
6. Identify values that are outside the provided reference range
7. Explain medical terminology in simple language
8. Generate an overall educational summary
9. Ask questions about the uploaded report
10. Compare two medical reports
11. View previous reports
12. Delete reports and associated data
13. Download/share an educational report summary
14. See privacy and medical disclaimer information

The UI should feel like a polished healthcare SaaS product, not a college demo.

==================================================
2. TECH STACK
==================================================

Frontend:
- React
- Vite
- TypeScript
- Tailwind CSS
- React Router
- TanStack Query
- Recharts
- Lucide React icons

Backend:
- Python
- FastAPI
- Pydantic
- SQLAlchemy

Database:
- PostgreSQL
- pgvector if needed for RAG

Authentication:
- JWT-based authentication
OR Firebase Authentication if easier to implement securely.

File processing:
- PyMuPDF for PDFs
- OCR for scanned documents
- Support JPG/PNG
- Table extraction where possible

AI:
- LLM API with structured JSON output
- Embeddings for semantic retrieval
- RAG architecture for medical terminology/explanations

Storage:
- Local storage for development
- S3-compatible object storage architecture for production

==================================================
3. APPLICATION ARCHITECTURE
==================================================

Use this architecture:

React Frontend
        |
        v
FastAPI REST API
        |
        +---- Authentication
        |
        +---- Report Upload
        |
        +---- Document Processing
        |
        +---- Medical Parameter Extraction
        |
        +---- Reference Range Evaluation
        |
        +---- AI Explanation
        |
        +---- RAG
        |
        +---- Report Q&A
        |
        +---- Report Comparison
        |
        +---- User History
        |
        v
PostgreSQL

AI processing pipeline:

Uploaded Document
        ↓
File Validation
        ↓
Text Extraction / OCR
        ↓
Document Cleaning
        ↓
Medical Parameter Extraction
        ↓
Unit Normalization
        ↓
Reference Range Extraction
        ↓
Deterministic Range Comparison
        ↓
Medical Context Retrieval
        ↓
LLM Explanation
        ↓
Structured Result
        ↓
Frontend Dashboard

IMPORTANT:
Do NOT ask the LLM to determine whether a numerical value is within range when the report provides a reference range.

The backend should perform deterministic comparison whenever possible.

Example:

Result = 10.2
Reference range = 13.0–17.0

Backend:
status = LOW

The LLM should explain what the parameter means and possible general reasons it can vary, without diagnosing the user.

==================================================
4. DATABASE DESIGN
==================================================

Create PostgreSQL models for:

USERS
- id
- name
- email
- password_hash
- created_at
- updated_at

REPORTS
- id
- user_id
- original_filename
- file_type
- file_path
- report_type
- upload_date
- processing_status
- processing_error
- created_at
- updated_at

MEDICAL_PARAMETERS
- id
- report_id
- parameter_name
- normalized_name
- value
- unit
- reference_low
- reference_high
- reference_text
- status
- confidence
- source_text

AI_EXPLANATIONS
- id
- report_id
- parameter_id
- explanation
- educational_context
- created_at

REPORT_COMPARISONS
- id
- user_id
- report_a_id
- report_b_id
- comparison_result
- created_at

CHAT_MESSAGES
- id
- report_id
- user_id
- role
- message
- created_at

DOCUMENT_CHUNKS
- id
- report_id
- chunk_text
- embedding
- metadata

==================================================
5. MEDICAL REPORT EXTRACTION
==================================================

Support common laboratory reports such as:

- CBC
- Complete Blood Count
- LFT
- Liver Function Test
- KFT
- Kidney Function Test
- Lipid Profile
- Thyroid Profile
- Blood Glucose
- HbA1c
- Vitamin D
- Vitamin B12
- Urine Routine
- Electrolytes

Do NOT hardcode the application only for these tests.

The extraction system should also support unknown/custom laboratory parameters.

Extract:

- Parameter name
- Value
- Unit
- Reference range
- Abnormal flags
- Report date
- Patient age if present
- Patient sex if present
- Laboratory name if present
- Test category

Do not unnecessarily store personally identifying information.

==================================================
6. NORMALIZATION
==================================================

Create a normalization layer.

Example:

"Haemoglobin"
"Hemoglobin"
"Hb"

should map to:

"Hemoglobin"

Similarly:

"SGPT"
"ALT"
"Alanine Aminotransferase"

should be represented consistently internally while preserving the original report wording.

Do not blindly convert units if conversion could introduce medical inaccuracies.

==================================================
7. ABNORMALITY ENGINE
==================================================

Build a deterministic abnormality engine.

Possible statuses:

- NORMAL
- LOW
- HIGH
- CRITICAL_REVIEW
- UNKNOWN

Rules:

If reference range exists:

value < lower_bound → LOW
value > upper_bound → HIGH
otherwise → NORMAL

If the report itself marks a value as H/L/Abnormal, preserve that information.

If no reference range exists:

status = UNKNOWN

Do NOT invent reference ranges.

Do NOT automatically classify a value as medically dangerous just because it is outside a range.

==================================================
8. AI EXPLANATION ENGINE
==================================================

For every extracted parameter, generate:

1. What is this test?
2. What does the reported value mean relative to the report's reference range?
3. Why is this parameter generally measured?
4. What general factors can influence it?
5. What other report parameters may be relevant?
6. When should the user consider discussing it with a healthcare professional?

Avoid diagnosis.

Avoid medication recommendations.

Avoid definitive statements such as:

"You have anemia."

Instead:

"Low hemoglobin can have several possible causes, including nutritional deficiencies and other conditions. A healthcare professional would interpret this together with your symptoms and other blood-count results."

==================================================
9. OVERALL REPORT SUMMARY
==================================================

Generate a dashboard summary.

Example:

REPORT OVERVIEW

Parameters analyzed: 28

Within reported range: 21
Outside reported range: 5
Unable to classify: 2

Sections:

🟢 Within reported range
🟡 Worth discussing
🔴 Outside reported range
⚪ Unable to interpret

The summary must NOT say:

"You are healthy."

"You have a disease."

"You are completely fine."

Instead use language such as:

"Most measured values fall within the reference ranges provided by the laboratory."

==================================================
10. REPORT DASHBOARD UI
==================================================

Create a beautiful dashboard.

Header:

MediLens
Dashboard
Reports
Compare
Settings

Dashboard cards:

Reports Analyzed
Parameters Checked
Values Outside Range
Recent Reports

Main report page:

-------------------------------------
Medical Report
CBC • 26 Aug 2026

Overall educational summary

🟢 18 Normal
🟡 3 Review
🔴 2 Outside Range
-------------------------------------

Parameter cards:

Hemoglobin
10.2 g/dL
LOW

What it means
...

Reference range
13–17 g/dL

Learn more →
-------------------------------------

Use charts where appropriate.

==================================================
11. REPORT COMPARISON
==================================================

Allow users to select two reports.

Display:

Parameter
Previous
Current
Change
Previous Status
Current Status

Example:

Hemoglobin
10.2 → 11.4
Improved numerical value, but interpretation should remain contextual.

Create line charts for compatible numerical measurements.

Do not compare incompatible units.

If dates are unavailable, clearly state that chronological interpretation is unavailable.

==================================================
12. AI REPORT CHAT
==================================================

Add a chat interface:

"Ask about this report"

Example:

User:
"Why is my hemoglobin marked low?"

AI:
Explain based on the uploaded report.

User:
"What other values should I look at?"

AI:
Reference relevant parameters from the same report.

The AI should use RAG and report-specific context.

The chatbot must NOT hallucinate values that aren't present in the report.

If the answer isn't supported by the report or trusted knowledge source:

"I don't have enough information in this report to answer that reliably."

==================================================
13. RAG SYSTEM
==================================================

Implement RAG for general medical terminology.

Pipeline:

Medical knowledge documents
        ↓
Chunking
        ↓
Embeddings
        ↓
pgvector
        ↓
Semantic retrieval
        ↓
LLM

The RAG knowledge base should contain reliable educational information about:

- Common laboratory tests
- Medical terminology
- Units
- General test purposes
- General interpretation concepts

Do NOT use random medical blogs as the primary knowledge source.

Clearly separate:

REPORT DATA
from
GENERAL MEDICAL EDUCATION

==================================================
14. PRIVACY & SECURITY
==================================================

Implement:

- Authentication
- Authorization
- User-specific report access
- File type validation
- File size limits
- Secure filenames
- API validation
- Rate limiting architecture
- Environment variables for secrets
- No API keys in frontend
- No raw medical report data in application logs
- Report deletion
- Optional automatic file deletion
- PII redaction before external AI processing where feasible

Never expose one user's reports to another user.

==================================================
15. MEDICAL SAFETY
==================================================

Every AI-generated report should include:

"Educational information only. This explanation is not a diagnosis or medical advice. Laboratory results should be interpreted by a qualified healthcare professional in the context of your symptoms, medical history, and other findings."

If the report contains a laboratory-provided critical flag, prominently display:

"Your laboratory report has flagged this result for urgent attention. Please contact an appropriate healthcare professional promptly."

Do NOT invent critical thresholds.

Do NOT provide emergency medical instructions unless explicitly supported by a trusted medical knowledge source.

==================================================
16. ERROR HANDLING
==================================================

Handle:

- Invalid file
- Unsupported format
- Corrupted PDF
- Empty PDF
- Poor OCR quality
- Missing reference ranges
- Ambiguous values
- Unknown parameters
- AI API failure
- Timeout
- Database failure

Never silently fabricate extracted medical information.

If extraction confidence is low:

"Some information could not be read reliably. Please verify these values against the original report."

==================================================
17. FRONTEND PAGES
==================================================

Create:

1. Landing Page
2. Login
3. Register
4. Dashboard
5. Upload Report
6. Processing Screen
7. Report Results
8. Parameter Details
9. AI Chat
10. Report Comparison
11. Report History
12. Settings
13. Privacy Policy
14. Medical Disclaimer

==================================================
18. LANDING PAGE
==================================================

Hero:

"Understand Your Medical Reports, Simply."

Subtitle:

"MediLens uses AI to turn complex laboratory reports into clear, educational explanations."

CTA:

"Analyze a Report"

Secondary CTA:

"See How It Works"

Sections:

How it works
Features
Supported reports
Privacy
Medical disclaimer

Do not make exaggerated claims.

Avoid phrases like:

"AI doctor"

"Diagnose yourself"

"100% accurate"

"Replace your doctor"

==================================================
19. UI DESIGN
==================================================

Design language:

- Modern
- Minimal
- Healthcare SaaS
- Clean white/light background
- Soft blue/green accents
- Rounded cards
- Clear typography
- Accessible contrast
- Responsive
- Mobile friendly

Use Lucide icons.

Use skeleton loaders during AI processing.

Show progress:

Uploading
→ Extracting
→ Reading tables
→ Identifying parameters
→ Checking reference ranges
→ Generating explanations
→ Complete

==================================================
20. API ENDPOINTS
==================================================

Implement REST endpoints such as:

POST /auth/register
POST /auth/login

POST /reports/upload
GET /reports
GET /reports/{report_id}
DELETE /reports/{report_id}

GET /reports/{report_id}/parameters
GET /reports/{report_id}/summary

POST /reports/{report_id}/chat

POST /reports/compare

GET /parameters/{parameter_id}

GET /health

Use Pydantic schemas for every request/response.

Generate OpenAPI documentation automatically through FastAPI.

==================================================
21. PROJECT STRUCTURE
==================================================

Use:

frontend/
    src/
        components/
        pages/
        layouts/
        hooks/
        services/
        types/
        utils/
        charts/

backend/
    app/
        api/
        models/
        schemas/
        services/
        ai/
        extraction/
        ocr/
        rag/
        security/
        utils/
        main.py

database/
migrations/
tests/
docker/

Also provide:

.env.example
README.md
docker-compose.yml

==================================================
22. TESTING
==================================================

Create tests for:

- Authentication
- File validation
- PDF extraction
- OCR
- Parameter extraction
- Reference range parsing
- Low/high/normal classification
- Unknown ranges
- Unit handling
- Authorization
- Report deletion
- AI response validation
- Chat context
- Report comparison

Create mock medical reports for testing.

Do NOT use real people's medical records in test fixtures.

==================================================
23. AI OUTPUT VALIDATION
==================================================

The LLM must return structured JSON.

Example:

{
  "parameter": "Hemoglobin",
  "value": 10.2,
  "unit": "g/dL",
  "status": "LOW",
  "explanation": "...",
  "general_factors": [],
  "related_parameters": [],
  "confidence": 0.94
}

Validate all AI outputs using Pydantic.

If the AI returns invalid JSON, retry safely or return a controlled error.

==================================================
24. IMPORTANT ENGINEERING RULES
==================================================

Do NOT create fake medical data.

Do NOT hardcode AI responses.

Do NOT put API keys in frontend code.

Do NOT trust LLM numerical calculations when deterministic code can perform them.

Do NOT invent reference ranges.

Do NOT hallucinate values.

Do NOT claim diagnosis.

Do NOT make unsupported medical claims.

Do NOT store unnecessary personal information.

Write clean, modular, production-quality code.

Use TypeScript types on the frontend.

Use Python type hints throughout the backend.

Add comments only where they provide useful engineering context.

==================================================
25. DEVELOPMENT ORDER
==================================================

Build in this order:

PHASE 1
Project setup
Authentication
Database
Basic dashboard

PHASE 2
File upload
PDF extraction
OCR
Document processing

PHASE 3
Medical parameter extraction
Normalization
Reference range engine

PHASE 4
AI explanation engine
Structured LLM output
Safety validation

PHASE 5
RAG
Report-specific Q&A

PHASE 6
Report comparison
Charts
History

PHASE 7
Security
Testing
Error handling

PHASE 8
UI polish
Responsive design
Deployment configuration
Documentation

==================================================
26. FINAL REQUIREMENT
==================================================

Do not simply generate a prototype with placeholder buttons.

Implement the actual working end-to-end flow:

User
→ Login
→ Upload medical report
→ Extract report
→ Identify parameters
→ Read reference ranges
→ Deterministically classify values
→ Generate educational explanations
→ Display report dashboard
→ Ask questions about report
→ Compare reports
→ Delete report

If an external service/API is required, create a clean abstraction layer and provide an .env.example file.

If API credentials are unavailable, implement a mock provider interface so the rest of the application remains functional.

At the end, provide:

1. Complete project structure
2. Setup instructions
3. Environment variables
4. Database setup
5. How to run frontend
6. How to run backend
7. How to run tests
8. Deployment instructions
9. Explanation of AI architecture
10. Explanation of medical safety architecture
