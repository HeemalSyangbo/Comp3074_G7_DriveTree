package com.drivetree.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookings")
data class BookingEntity(
    @PrimaryKey val id: String,
    val instructorId: String,
    val studentName: String,
    val studentEmail: String,
    val epochTime: Long,         // slot time
    val status: String           // REQUESTED / CONFIRMED / CANCELLED / DECLINED
)
