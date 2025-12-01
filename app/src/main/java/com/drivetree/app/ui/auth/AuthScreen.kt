package com.drivetree.app.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.drivetree.app.data.Role
import com.drivetree.app.data.UserSession
import com.drivetree.app.data.AppViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onStudent: () -> Unit,
    onInstructor: () -> Unit,
    onAdmin: () -> Unit,
    onRegister: () -> Unit,
    appVm: AppViewModel
) {
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(Role.STUDENT) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    
    // Get current data from ViewModel
    val allApps by appVm.allApps.collectAsState(initial = emptyList())
    val instructors by appVm.instructors.collectAsState(initial = emptyList())

    fun extractNameFromEmail(email: String): String {
        val emailPart = email.substringBefore("@")
        return if (emailPart.contains(".")) {
            // Split by dots and capitalize each word
            emailPart.split(".").joinToString(" ") { word ->
                word.trim().replaceFirstChar { it.uppercaseChar() }
            }
        } else {
            // Single word - just capitalize first letter
            emailPart.replaceFirstChar { it.uppercaseChar() }
        }
    }

    fun submit() {
        error = when {
            email.isBlank() -> "Email is required"
            !email.contains("@") -> "Enter a valid email"
            pass.length < 6 -> "Password must be at least 6 characters"
            else -> null
        }
        if (error == null) {
            scope.launch {
                // STUDENT LOGIN: Only use database, never extract from email
                if (role == Role.STUDENT) {
                    val existingStudent = appVm.studentByEmail(email).first()
                    if (existingStudent != null) {
                        // Use saved data from database - this is the ONLY source for students
                        UserSession.setStudentProfile(
                            existingStudent.name,
                            existingStudent.email,
                            existingStudent.phone,
                            existingStudent.address
                        )
                    } else {
                        // No database record found - for demo purposes, allow login
                        // but user should register to have full profile
                        UserSession.setUser(email, email, role)
                    }
                } 
                // INSTRUCTOR LOGIN: Only use application or instructor record, never extract from email
                else if (role == Role.INSTRUCTOR) {
                    val existingApp = allApps.firstOrNull { it.email.equals(email, ignoreCase = true) }
                    val existingInstructor = instructors.firstOrNull { 
                        // Match by checking if any application with this email created this instructor
                        allApps.any { app -> 
                            app.email.equals(email, ignoreCase = true) && app.id == it.id 
                        }
                    }
                    
                    when {
                        existingInstructor != null -> {
                            // Instructor is approved - use instructor record
                            UserSession.setUser(existingInstructor.name, email, role)
                        }
                        existingApp != null -> {
                            // Instructor application exists - use application data
                            UserSession.setUser(existingApp.instructorName, email, role)
                        }
                        else -> {
                            // No record found - for demo purposes, allow login
                            // but user should register to have full profile
                            UserSession.setUser(email, email, role)
                        }
                    }
                } 
                // ADMIN LOGIN: Can extract from email (no registration required)
                else {
                    val userName = UserSession.currentUserName?.takeIf { 
                        UserSession.currentUserEmail?.equals(email, ignoreCase = true) == true 
                    } ?: extractNameFromEmail(email)
                    UserSession.setUser(userName, email, role)
                }
                
                when (role) {
                    Role.STUDENT -> onStudent()
                    Role.INSTRUCTOR -> onInstructor()
                    Role.ADMIN -> onAdmin()
                }
            }
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Sign In") }) }) { pad ->
        Column(Modifier.padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(email, { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(pass, { pass = it }, label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation())

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Role.entries.forEach { r ->
                    FilterChip(
                        selected = role == r, onClick = { role = r },
                        label = { Text(r.name.lowercase().replaceFirstChar { c -> c.titlecase() }) }
                    )
                }
            }

            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(onClick = ::submit, modifier = Modifier.fillMaxWidth()) { Text("Continue") }

            // ✅ Create account link
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                TextButton(onClick = onRegister) {
                    Text("Create account")
                }
            }
        }
    }
}
