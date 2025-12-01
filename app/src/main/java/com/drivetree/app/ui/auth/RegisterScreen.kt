package com.drivetree.app.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.drivetree.app.data.Role
import com.drivetree.app.data.AppViewModel
import com.drivetree.app.data.UserSession
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
    
    // Student profile fields
    var phone by remember { mutableStateOf("") }
    var studentAddress by remember { mutableStateOf("") }
    
    // Instructor-specific fields
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var pricePerHour by remember { mutableStateOf("") }
    var carType by remember { mutableStateOf("") }
    var languages by remember { mutableStateOf("") }

    fun submit() {
        error = when {
            name.isBlank() -> "Full name is required"
            email.isBlank() || !email.contains("@") -> "Enter a valid email"
            pass.length < 6 -> "Password must be at least 6 characters"
            pass != confirm -> "Passwords do not match"
            role == Role.STUDENT && phone.isBlank() -> "Phone number is required"
            role == Role.STUDENT && studentAddress.isBlank() -> "Address is required"
            role == Role.INSTRUCTOR && address.isBlank() -> "Address is required for instructors"
            role == Role.INSTRUCTOR && city.isBlank() -> "City is required for instructors"
            role == Role.INSTRUCTOR && pricePerHour.isBlank() -> "Price per hour is required"
            role == Role.INSTRUCTOR && pricePerHour.toIntOrNull() == null -> "Price must be a valid number"
            role == Role.INSTRUCTOR && carType.isBlank() -> "Car type is required"
            role == Role.INSTRUCTOR && languages.isBlank() -> "Languages are required"
            else -> null
        }
        if (error == null) {
            when (role) {
                Role.STUDENT -> {
                    // Save student profile to database
                    val student = com.drivetree.app.data.entity.StudentEntity(
                        email = email,
                        name = name,
                        phone = phone,
                        address = studentAddress
                    )
                    appVm.upsertStudent(student)
                    // Store student info in session including phone and address
                    UserSession.setStudentProfile(name, email, phone, studentAddress)
                    // Navigate back to login screen
                    onCancel()
                }
                Role.INSTRUCTOR -> {
                    // Create a pending instructor application with profile info
                    val app = ApplicationEntity(
                        id = UUID.randomUUID().toString(),
                        instructorName = name,
                        email = email,
                        submittedAt = System.currentTimeMillis(),
                        status = "PENDING",
                        address = address,
                        city = city,
                        pricePerHour = pricePerHour.toIntOrNull() ?: 45,
                        carType = carType,
                        languages = languages
                    )
                    appVm.submitApplication(app)
                    // Go back to login instead of auto-login
                    onCancel()
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
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Account Type Selection
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Account Type",
                            style = MaterialTheme.typography.titleMedium
                        )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(Role.STUDENT, Role.INSTRUCTOR).forEach { r ->
                    FilterChip(
                        selected = role == r,
                        onClick = { role = r },
                                    label = { Text(r.name.lowercase().replaceFirstChar { c -> c.titlecase() }) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Basic Account Information
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Account Information",
                            style = MaterialTheme.typography.titleMedium
                        )
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Full Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = pass,
                            onValueChange = { pass = it },
                            label = { Text("Password") },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = confirm,
                            onValueChange = { confirm = it },
                            label = { Text("Confirm Password") },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true
                        )
                    }
                }

                // Student Profile Information
                if (role == Role.STUDENT) {
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "Profile Information",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "Please provide your contact details",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = { Text("Phone Number") },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("(416) 555-0123") },
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = studentAddress,
                                onValueChange = { studentAddress = it },
                                label = { Text("Address") },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("123 Main St, Toronto, ON") },
                                singleLine = true
                            )
                        }
                    }
                }

                // Instructor Profile Information
                if (role == Role.INSTRUCTOR) {
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "Instructor Profile",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "This information will be reviewed by an admin before approval",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Text(
                                "Location",
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            OutlinedTextField(
                                value = address,
                                onValueChange = { address = it },
                                label = { Text("Street Address") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = city,
                                onValueChange = { city = it },
                                label = { Text("City") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            
                            Text(
                                "Service Details",
                                style = MaterialTheme.typography.titleSmall
                            )
                            OutlinedTextField(
                                value = pricePerHour,
                                onValueChange = { pricePerHour = it },
                                label = { Text("Price per Hour ($)") },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("45") },
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = carType,
                                onValueChange = { carType = it },
                                label = { Text("Car Type") },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Sedan, SUV, Hatchback, etc.") },
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = languages,
                                onValueChange = { languages = it },
                                label = { Text("Languages") },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("English, French, Spanish (comma-separated)") },
                                singleLine = true
                            )
                        }
                    }
                }

                // Error message
                error?.let {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                // Submit button
                Button(
                    onClick = ::submit,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Create Account")
                }

                // Footer note
            Text(
                "Prototype note: Sign-up is emulated locally; no server yet.",
                style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
            )
            }
        }
    }
}
