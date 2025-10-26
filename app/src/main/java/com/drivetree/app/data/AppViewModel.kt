package com.drivetree.app.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(private val repo: Repository) : ViewModel() {

    // Expose data as StateFlow for Compose
    val instructors = repo.instructors
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val bookings = repo.bookings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val pendingApps = repo.pendingApps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val allApps = repo.allApps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        // Seed local DB with your existing fixtures on first launch
        viewModelScope.launch {
            repo.seedIfEmpty(Fixtures.instructors)
        }
    }

    // Simple actions the UI can call
    fun requestBooking(b: com.drivetree.app.data.entity.BookingEntity) =
        viewModelScope.launch { repo.requestBooking(b) }

    fun submitApplication(a: com.drivetree.app.data.entity.ApplicationEntity) =
        viewModelScope.launch { repo.submitApplication(a) }

    fun approveApplication(id: String) =
        viewModelScope.launch { repo.approveApplication(id) }

    fun rejectApplication(id: String) =
        viewModelScope.launch { repo.rejectApplication(id) }

    // ---- Booking status helpers ----
    fun updateBookingStatus(bookingId: String, status: String) =
        viewModelScope.launch { repo.updateBookingStatus(bookingId, status) }

    fun approveBooking(id: String) =
        viewModelScope.launch { repo.updateBookingStatus(id, "APPROVED") }

    fun rejectBooking(id: String) =
        viewModelScope.launch { repo.updateBookingStatus(id, "REJECTED") }

    fun cancelBooking(id: String) =
        viewModelScope.launch { repo.updateBookingStatus(id, "CANCELLED") }

    companion object {
        fun factory(repo: Repository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AppViewModel(repo) as T
                }
            }
    }
}
