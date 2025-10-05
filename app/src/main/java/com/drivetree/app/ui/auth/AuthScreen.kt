package com.drivetree.app.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.drivetree.app.data.Role

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onStudent: () -> Unit,
    onInstructor: () -> Unit,
    onAdmin: () -> Unit,
    onRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(Role.STUDENT) }
    var error by remember { mutableStateOf<String?>(null) }

    fun submit() {
        error = when {
            email.isBlank() -> "Email is required"
            !email.contains("@") -> "Enter a valid email"
            pass.length < 6 -> "Password must be at least 6 characters"
            else -> null
        }
        if (error == null) when (role) {
            Role.STUDENT -> onStudent()
            Role.INSTRUCTOR -> onInstructor()
            Role.ADMIN -> onAdmin()
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
