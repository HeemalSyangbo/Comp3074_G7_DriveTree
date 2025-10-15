package com.drivetree.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.drivetree.app.nav.Route
import com.drivetree.app.ui.admin.AdminHomeScreen
import com.drivetree.app.ui.auth.AuthScreen
import com.drivetree.app.ui.auth.RegisterScreen     // ✅ import Register screen
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
                            onStudent   = { nav.navigate(Route.StudentHome.path) },
                            onInstructor= { nav.navigate(Route.InstructorHome.path) },
                            onAdmin     = { nav.navigate(Route.AdminHome.path) },
                            onRegister      = { nav.navigate(Route.Register.path) }   // ✅ go to Register
                        )
                    }

                    // Register Signup Only
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
                            onCancel = { nav.popBackStack() }
                        )
                    }

                    composable(Route.StudentHome.path) {
                        StudentHomeScreen(
                            openProfile = { id -> nav.navigate(Route.InstructorProfile.withId(id)) },
                            openAbout   = { nav.navigate(Route.About.path) }
                        )
                    }

                    composable(Route.InstructorHome.path) {
                        InstructorHomeScreen(openAbout = { nav.navigate(Route.About.path) })
                    }

                    composable(Route.AdminHome.path) {
                        AdminHomeScreen(openAbout = { nav.navigate(Route.About.path) })
                    }

                    composable(Route.InstructorProfile.path) { backStack ->
                        val id = backStack.arguments?.getString("id") ?: ""
                        InstructorProfileScreen(
                            id = id,
                            onBook = { nav.navigate(Route.BookingRequest.path) }
                        )
                    }

                    composable(Route.BookingRequest.path) {
                        BookingRequestScreen(onClose = { nav.popBackStack() })
                    }

                    composable(Route.About.path) {
                        AboutScreen(onClose = { nav.popBackStack() })
                    }
                }
            }
        }
    }
}
