package com.drivetree.app.data
object UserSession {
    var currentUserName: String? = null
    var currentUserEmail: String? = null
    var currentUserRole: Role? = null
    var shouldShowBookingsTab: Boolean = false
    
    // Student profile data
    var currentUserPhone: String? = null
    var currentUserAddress: String? = null

    fun setUser(name: String, email: String, role: Role) {
        currentUserName = name
        currentUserEmail = email
        currentUserRole = role
    }
    
    fun setStudentProfile(name: String, email: String, phone: String, address: String) {
        currentUserName = name
        currentUserEmail = email
        currentUserRole = Role.STUDENT
        currentUserPhone = phone
        currentUserAddress = address
    }

    fun clear() {
        currentUserName = null
        currentUserEmail = null
        currentUserRole = null
        shouldShowBookingsTab = false
        currentUserPhone = null
        currentUserAddress = null
    }
}

