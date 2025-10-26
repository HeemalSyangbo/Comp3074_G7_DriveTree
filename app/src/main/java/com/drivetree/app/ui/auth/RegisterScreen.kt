package com.drivetree.app.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.drivetree.app.data.Role
import com.drivetree.app.data.AppViewModel
import com.drivetree.app.data.entity.ApplicationEntity
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onStudent: () -> Unit,
    onInstructor: () -> Unit,
    onCancel: () -> Unit,
    appVm: AppViewModel   // <-- NEW: inject ViewModel so we can write to DB
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(Role.STUDENT) }
    var error by remember { mutableStateOf<String?>(null) }

    fun submit() {
        error = when {
            name.isBlank() -> "Full name is required"
            email.isBlank() || !email.contains("@") -> "Enter a valid email"
            pass.length < 6 -> "Password must be at least 6 characters"
            pass != confirm -> "Passwords do not match"
            else -> null
        }
        if (error == null) {
            when (role) {
                Role.STUDENT -> onStudent()
                Role.INSTRUCTOR -> {
                    // NEW: create a pending instructor application for Admin review
                    val app = ApplicationEntity(
                        id = UUID.randomUUID().toString(),
                        instructorName = name,
                        email = email,
                        submittedAt = System.currentTimeMillis(),
                        status = "PENDING"
                    )
                    appVm.submitApplication(app)
                    onInstructor()
                }
                Role.ADMIN -> { /* Admin sign-up not allowed */ }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create account") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { pad ->
        Column(
            Modifier.padding(pad).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(name, { name = it }, label = { Text("Full name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(email, { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                pass, { pass = it }, label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation()
            )
            OutlinedTextField(
                confirm, { confirm = it }, label = { Text("Confirm password") },
                modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(Role.STUDENT, Role.INSTRUCTOR).forEach { r ->
                    FilterChip(
                        selected = role == r,
                        onClick = { role = r },
                        label = { Text(r.name.lowercase().replaceFirstChar { c -> c.titlecase() }) }
                    )
                }
            }

            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(onClick = ::submit, modifier = Modifier.fillMaxWidth()) { Text("Create account") }

            Text(
                "Prototype note: Sign-up is emulated locally; no server yet.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
