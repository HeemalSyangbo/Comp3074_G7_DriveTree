package com.drivetree.app.data

data class Instructor(
    val id: String,
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

enum class Role { STUDENT, INSTRUCTOR, ADMIN }
