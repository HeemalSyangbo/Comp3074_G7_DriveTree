package com.drivetree.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "applications")
data class ApplicationEntity(
    @PrimaryKey val id: String,
    val instructorName: String,
    val email: String,
    val submittedAt: Long,
    val status: String
)
