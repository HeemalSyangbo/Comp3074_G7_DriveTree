package com.drivetree.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey val email: String,  // Use email as primary key since it's unique
    val name: String,
    val phone: String,
    val address: String
)




