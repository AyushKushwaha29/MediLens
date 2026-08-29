package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.MedicalParameterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicalParameterDao {
    @Query("SELECT * FROM medical_parameters WHERE reportId = :reportId AND userId = :userId")
    fun getParametersForReport(reportId: String, userId: String): Flow<List<MedicalParameterEntity>>

    @Query("SELECT * FROM medical_parameters WHERE reportId = :reportId AND userId = :userId")
    suspend fun getParametersForReportSync(reportId: String, userId: String): List<MedicalParameterEntity>

    @Query("SELECT * FROM medical_parameters WHERE userId = :userId AND normalizedName = :normalizedName ORDER BY measurementDate ASC")
    fun getHistoryForParameter(userId: String, normalizedName: String): Flow<List<MedicalParameterEntity>>

    @Query("SELECT * FROM medical_parameters WHERE userId = :userId AND normalizedName = :normalizedName ORDER BY measurementDate ASC")
    suspend fun getHistoryForParameterSync(userId: String, normalizedName: String): List<MedicalParameterEntity>

    @Query("SELECT DISTINCT normalizedName, name, unit FROM medical_parameters WHERE userId = :userId ORDER BY name ASC")
    fun getDistinctTrackedParameters(userId: String): Flow<List<TrackedParameterSummary>>

    @Query("SELECT * FROM medical_parameters WHERE userId = :userId AND status IN ('HIGH', 'LOW') ORDER BY measurementDate DESC")
    fun getAbnormalParametersForUser(userId: String): Flow<List<MedicalParameterEntity>>

    @Query("SELECT COUNT(DISTINCT normalizedName) FROM medical_parameters WHERE userId = :userId")
    fun getTrackedParameterCount(userId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParameters(parameters: List<MedicalParameterEntity>)

    @Query("DELETE FROM medical_parameters WHERE reportId = :reportId AND userId = :userId")
    suspend fun deleteParametersForReport(reportId: String, userId: String)
}

data class TrackedParameterSummary(
    val normalizedName: String,
    val name: String,
    val unit: String
)
