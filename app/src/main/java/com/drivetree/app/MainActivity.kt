package com.drivetree.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.drivetree.app.data.AppDb
import com.drivetree.app.data.AppViewModel
import com.drivetree.app.data.DefaultRepository
import com.drivetree.app.nav.Route
import com.drivetree.app.ui.admin.AdminHomeScreen
import com.drivetree.app.ui.auth.AuthScreen
import com.drivetree.app.ui.auth.RegisterScreen
import com.drivetree.app.ui.instructor.InstructorHomeScreen
import com.drivetree.app.ui.misc.AboutScreen
import com.drivetree.app.ui.misc.SplashScreen
import com.drivetree.app.ui.student.BookingRequestScreen
import com.drivetree.app.ui.student.InstructorProfileScreen
import com.drivetree.app.ui.student.StudentHomeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(color = MaterialTheme.colorScheme.background) {

                // Repository + AppViewModel (seeds DB on first run)
                val repo = DefaultRepository(AppDb.get(applicationContext))
                val appVm: AppViewModel = viewModel(factory = AppViewModel.factory(repo))

                val nav = rememberNavController()

                NavHost(navController = nav, startDestination = Route.Splash.path) {

                    composable(Route.Splash.path) {
                        SplashScreen(
                            onDone = {
                                nav.navigate(Route.Auth.path) {
                                    popUpTo(Route.Splash.path) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(Route.Auth.path) {
                        AuthScreen(
                            onStudent    = { nav.navigate(Route.StudentHome.path) },
                            onInstructor = { nav.navigate(Route.InstructorHome.path) },
                            onAdmin      = { nav.navigate(Route.AdminHome.path) },
                            onRegister   = { nav.navigate(Route.Register.path) },
                            appVm        = appVm
                        )
                    }

                    composable(Route.Register.path) {
                        RegisterScreen(
                            onStudent = {
                                nav.navigate(Route.StudentHome.path) {
                                    popUpTo(Route.Auth.path) { inclusive = true }
                                }
                            },
                            onInstructor = {
                                nav.navigate(Route.InstructorHome.path) {
                                    popUpTo(Route.Auth.path) { inclusive = true }
                                }
                            },
                            onCancel = { nav.popBackStack() },
                            appVm = appVm
                        )
                    }

                    composable(Route.StudentHome.path) {
                        StudentHomeScreen(
                            openProfile = { id -> nav.navigate(Route.InstructorProfile.withId(id)) },
                            openAbout   = { nav.navigate(Route.About.path) },
                            onLogout    = {
                                com.drivetree.app.data.UserSession.clear()
                                nav.navigate(Route.Auth.path) {
                                    popUpTo(Route.StudentHome.path) { inclusive = true }
                                }
                            },
                            appVm       = appVm
                        )
                    }

                    composable(Route.InstructorHome.path) {
                        InstructorHomeScreen(
                            openAbout = { nav.navigate(Route.About.path) },
                            onLogout = {
                                com.drivetree.app.data.UserSession.clear()
                                nav.navigate(Route.Auth.path) {
                                    popUpTo(Route.InstructorHome.path) { inclusive = true }
                                }
                            },
                            appVm = appVm
                        )
                    }

                    composable(Route.AdminHome.path) {
                        AdminHomeScreen(
                            openAbout = { nav.navigate(Route.About.path) },
                            onLogout = {
                                com.drivetree.app.data.UserSession.clear()
                                nav.navigate(Route.Auth.path) {
                                    popUpTo(Route.AdminHome.path) { inclusive = true }
                                }
                            },
                            appVm = appVm
                        )
                    }

                    composable(Route.InstructorProfile.path) { backStack ->
                        val id = backStack.arguments?.getString("id") ?: ""
                        InstructorProfileScreen(
                            id = id,
                            appVm = appVm, // <-- pass ViewModel so screen reads from DB
                            onBook = { nav.navigate(Route.BookingRequest.withId(id)) }
                        )
                    }

                    composable(Route.BookingRequest.path) { backStack ->
                        val id = backStack.arguments?.getString("id") ?: ""
                        BookingRequestScreen(
                            instructorId = id,
                            appVm   = appVm,
                            onClose = { nav.popBackStack() },
                            onBookingSuccess = {
                                // Set flag to show bookings tab, then navigate
                                com.drivetree.app.data.UserSession.shouldShowBookingsTab = true
                                nav.navigate(Route.StudentHome.path) {
                                    popUpTo(Route.StudentHome.path) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(Route.About.path) {
                        AboutScreen(onClose = { nav.popBackStack() })
                    }
                }
            }
        }
    }
}
