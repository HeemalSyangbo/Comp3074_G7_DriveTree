package com.drivetree.app.nav

sealed class Route(val path: String) {
    data object Splash : Route("splash")
    data object Auth : Route("auth")
    data object Register : Route("register")

    data object StudentHome : Route("student/home")
    data object InstructorHome : Route("instructor/home")
    data object AdminHome : Route("admin/home")

    data object InstructorProfile : Route("instructor/{id}") {
        fun withId(id: String) = "instructor/$id"
    }
    data object BookingRequest : Route("booking/request")
    data object About : Route("about")
}
