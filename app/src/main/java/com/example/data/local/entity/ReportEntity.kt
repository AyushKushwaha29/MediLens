package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reports",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId"), Index("uploadDate")]
)
data class ReportEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val fileName: String,
    val reportTitle: String,
    val reportType: String,
    val reportDate: Long,
    val reportDateFormatted: String,
    val uploadDate: Long = System.currentTimeMillis(),
    val processingStatus: String, // "PROCESSED", "PROCESSING", "FAILED"
    val errorMessage: String? = null,
    val laboratoryName: String = "Laboratory Services",
    val parameterCount: Int = 0,
    val abnormalCount: Int = 0,
    val summaryText: String = "",
    val nextStepsJson: String = "" // List of next steps stored as JSON/newline separated
)
