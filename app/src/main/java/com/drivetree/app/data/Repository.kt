package com.drivetree.app.data

import com.drivetree.app.data.entity.BookingEntity
import com.drivetree.app.data.entity.ApplicationEntity
import com.drivetree.app.data.entity.StudentEntity
import kotlinx.coroutines.flow.Flow

interface Repository {
    val instructors: Flow<List<Instructor>>
    fun instructor(id: String): Flow<Instructor?>
    val bookings: Flow<List<BookingEntity>>
    fun bookingsByInstructor(id: String): Flow<List<BookingEntity>>
    val pendingApps: Flow<List<ApplicationEntity>>
    val allApps: Flow<List<ApplicationEntity>>

    suspend fun seedIfEmpty(fixtures: List<Instructor>)
    suspend fun requestBooking(b: BookingEntity)
    suspend fun submitApplication(a: ApplicationEntity)
    suspend fun approveApplication(id: String)
    suspend fun rejectApplication(id: String)

    // NEW: update booking status to CONFIRMED / DECLINED / CANCELLED (etc.)
    suspend fun updateBookingStatus(bookingId: String, status: String)
    
    // Reschedule booking by updating the epoch time
    suspend fun rescheduleBooking(bookingId: String, newEpochTime: Long)
    
    // Update instructor profile
    suspend fun updateInstructor(instructor: Instructor)
    
    // Update instructor status (ACTIVE, SUSPENDED, BANNED)
    suspend fun updateInstructorStatus(instructorId: String, status: String)
    
    // Student profile methods
    fun studentByEmail(email: String): Flow<StudentEntity?>
    suspend fun upsertStudent(student: StudentEntity)
}
