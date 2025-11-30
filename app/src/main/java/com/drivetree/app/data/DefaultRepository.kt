package com.drivetree.app.data

import com.drivetree.app.data.entity.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class DefaultRepository(private val db: AppDb) : Repository {

    override val instructors: Flow<List<Instructor>> =
        db.instructors().all().map { it.map(InstructorEntity::toDomain) }

    override fun instructor(id: String): Flow<Instructor?> =
        db.instructors().byId(id).map { it?.toDomain() }

    override val bookings: Flow<List<BookingEntity>> = db.bookings().all()

    override fun bookingsByInstructor(id: String): Flow<List<BookingEntity>> =
        db.bookings().byInstructor(id)

    override val pendingApps: Flow<List<ApplicationEntity>> = db.applications().pending()
    override val allApps: Flow<List<ApplicationEntity>> = db.applications().all()

    override suspend fun seedIfEmpty(fixtures: List<Instructor>) {
        val current = db.instructors().all().first()
        if (current.isEmpty()) {
            db.instructors().upsertAll(fixtures.map { it.toEntity() })
        }
    }

    override suspend fun requestBooking(b: BookingEntity) {
        db.bookings().upsert(b)
    }

    override suspend fun submitApplication(a: ApplicationEntity) {
        db.applications().upsert(a)
    }

    override suspend fun approveApplication(id: String) {
        val app = allApps.first().find { it.id == id } ?: return

        // 1) Mark application as APPROVED
        db.applications().upsert(app.copy(status = "APPROVED"))

        // 2) Create/Upsert a corresponding Instructor using profile info from application
        val langList = if (app.languages != null && app.languages.isNotBlank()) {
            app.languages.split(",").map { it.trim() }.filter { it.isNotBlank() }
        } else {
            listOf("English")
        }
        
        val newInstructor = InstructorEntity(
            id = app.id,                          // reuse application id
            name = app.instructorName,
            address = app.address ?: "To be updated",
            pricePerHour = app.pricePerHour ?: 45,
            rating = 0.0,
            city = app.city ?: "Toronto",
            languages = langList,
            verified = true,                      // Approved by Admin
            carType = app.carType ?: "Sedan (Automatic)",
            photoUrl = null,
            status = "ACTIVE",
            availabilityDays = null,
            availabilityStartTime = null,
            availabilityEndTime = null
        )
        db.instructors().upsertAll(listOf(newInstructor))
    }

    override suspend fun rejectApplication(id: String) {
        val app = allApps.first().find { it.id == id } ?: return
        db.applications().upsert(app.copy(status = "REJECTED"))
    }

    // NEW: update booking status (REQUESTED / APPROVED / REJECTED / CANCELLED, etc.)
    override suspend fun updateBookingStatus(bookingId: String, status: String) {
        val current = bookings.first().find { it.id == bookingId } ?: return

        // Normalize status for consistency in UI/filters
        val normalized = status.trim().uppercase()

        // (Optional) keep a known set for readability; still allow others if you add later
        val allowed = setOf("REQUESTED", "APPROVED", "REJECTED", "CANCELLED", "COMPLETED")
        val finalStatus = if (normalized in allowed) normalized else normalized

        db.bookings().upsert(current.copy(status = finalStatus))
    }
    
    override suspend fun rescheduleBooking(bookingId: String, newEpochTime: Long) {
        val current = bookings.first().find { it.id == bookingId } ?: return
        db.bookings().upsert(current.copy(epochTime = newEpochTime, status = "REQUESTED"))
    }
    
    override suspend fun updateInstructor(instructor: Instructor) {
        db.instructors().upsertAll(listOf(instructor.toEntity()))
    }
    
    override suspend fun updateInstructorStatus(instructorId: String, status: String) {
        val current = instructors.first().find { it.id == instructorId } ?: return
        val updated = current.copy(status = status.uppercase())
        db.instructors().upsertAll(listOf(updated.toEntity()))
    }
    
    override fun studentByEmail(email: String): Flow<StudentEntity?> = db.students().byEmail(email)
    
    override suspend fun upsertStudent(student: StudentEntity) {
        db.students().upsert(student)
    }
}
