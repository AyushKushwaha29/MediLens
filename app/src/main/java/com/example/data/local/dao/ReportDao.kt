package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ReportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReportDao {
    @Query("SELECT * FROM reports WHERE userId = :userId ORDER BY reportDate DESC, uploadDate DESC")
    fun getReportsForUser(userId: String): Flow<List<ReportEntity>>

    @Query("SELECT * FROM reports WHERE id = :reportId AND userId = :userId LIMIT 1")
    fun getReportById(reportId: String, userId: String): Flow<ReportEntity?>

    @Query("SELECT * FROM reports WHERE id = :reportId AND userId = :userId LIMIT 1")
    suspend fun getReportByIdSync(reportId: String, userId: String): ReportEntity?

    @Query("SELECT COUNT(*) FROM reports WHERE userId = :userId")
    fun getReportCount(userId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ReportEntity)

    @Update
    suspend fun updateReport(report: ReportEntity)

    @Query("DELETE FROM reports WHERE id = :reportId AND userId = :userId")
    suspend fun deleteReport(reportId: String, userId: String)
}
