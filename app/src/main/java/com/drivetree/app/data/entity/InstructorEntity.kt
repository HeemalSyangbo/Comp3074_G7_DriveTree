package com.drivetree.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.drivetree.app.data.Instructor

@Entity(tableName = "instructors")
data class InstructorEntity(
    @PrimaryKey val id: String,
    val name: String,
    val address: String,
    val pricePerHour: Int,
    val rating: Double,
    val city: String,
    val languages: List<String>,
    val verified: Boolean,
    val carType: String,
    val photoUrl: String? = null
)

fun InstructorEntity.toDomain() = Instructor(
    id, name, address, pricePerHour, rating, city, languages, verified, carType, photoUrl
)

fun Instructor.toEntity() = InstructorEntity(
    id, name, address, pricePerHour, rating, city, languages, verified, carType, photoUrl
)
