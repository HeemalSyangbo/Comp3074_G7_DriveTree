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
    val photoUrl: String? = null,
    val status: String = "ACTIVE",  // ACTIVE, SUSPENDED, BANNED
    val availabilityDays: String? = null,  // Comma-separated days: "Monday,Tuesday,Wednesday"
    val availabilityStartTime: String? = null,  // e.g., "9:00 AM"
    val availabilityEndTime: String? = null  // e.g., "5:00 PM"
)

fun InstructorEntity.toDomain() = Instructor(
    id, name, address, pricePerHour, rating, city, languages, verified, carType, photoUrl, status,
    availabilityDays, availabilityStartTime, availabilityEndTime
)

fun Instructor.toEntity() = InstructorEntity(
    id, name, address, pricePerHour, rating, city, languages, verified, carType, photoUrl, status,
    availabilityDays, availabilityStartTime, availabilityEndTime
)
