package com.drivetree.app.data.dao


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.drivetree.app.data.entity.InstructorEntity
import kotlinx.coroutines.flow.Flow
import androidx.room.Query


@Dao
interface InstructorDao {
    @Query("SELECT * FROM instructors ORDER BY name ASC")
    fun all(): Flow<List<InstructorEntity>>

    @Query("SELECT * FROM instructors WHERE id = :id LIMIT 1")
    fun byId(id: String): Flow<InstructorEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<InstructorEntity>)
}
