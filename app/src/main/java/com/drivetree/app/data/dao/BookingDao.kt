package com.drivetree.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.drivetree.app.data.entity.BookingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookingDao {
    @Query("SELECT * FROM bookings ORDER BY epochTime DESC")
    fun all(): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE instructorId = :instructorId ORDER BY epochTime DESC")
    fun byInstructor(instructorId: String): Flow<List<BookingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(b: BookingEntity)
}
