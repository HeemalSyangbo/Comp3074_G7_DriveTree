package com.drivetree.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "applications")
data class ApplicationEntity(
    @PrimaryKey val id: String,
    val instructorName: String,
    val email: String,
    val submittedAt: Long,
    val status: String,
    val address: String? = null,
    val city: String? = null,
    val pricePerHour: Int? = null,
    val carType: String? = null,
    val languages: String? = null  // Comma-separated string
)
