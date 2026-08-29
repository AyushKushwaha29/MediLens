package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "medical_parameters",
    foreignKeys = [
        ForeignKey(
            entity = ReportEntity::class,
            parentColumns = ["id"],
            childColumns = ["reportId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("reportId"), Index("userId"), Index("normalizedName"), Index("measurementDate")]
)
data class MedicalParameterEntity(
    @PrimaryKey
    val id: String,
    val reportId: String,
    val userId: String,
    val name: String,
    val normalizedName: String,
    val value: Double,
    val displayValue: String,
    val unit: String,
    val referenceRange: String,
    val refMin: Double? = null,
    val refMax: Double? = null,
    val status: String, // "NORMAL", "LOW", "HIGH", "UNKNOWN"
    val sourceText: String = "",
    val confidence: Float = 0.95f,
    val measurementDate: Long,
    val explanationText: String = "",
    val whyMeasured: String = "",
    val influencingFactors: String = "",
    val whenToDiscuss: String = ""
)
