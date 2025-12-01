package com.drivetree.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.drivetree.app.data.entity.StudentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM students WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    fun byEmail(email: String): Flow<StudentEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(student: StudentEntity)
}

