package com.drivetree.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.drivetree.app.data.entity.ApplicationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ApplicationDao {
    @Query("SELECT * FROM applications WHERE status = 'PENDING' ORDER BY submittedAt DESC")
    fun pending(): Flow<List<ApplicationEntity>>

    @Query("SELECT * FROM applications ORDER BY submittedAt DESC")
    fun all(): Flow<List<ApplicationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(app: ApplicationEntity)
}
