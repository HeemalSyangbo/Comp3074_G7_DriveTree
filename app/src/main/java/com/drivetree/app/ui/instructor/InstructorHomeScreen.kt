// Verification Tab → for uploading instructor license & tracking approval status
// Availability Tab → for setting teaching schedule
// Bookings Tab → shows student requests for lessons
// Profile Tab → allows instructor to update profile details

package com.drivetree.app.ui.instructor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.drivetree.app.data.AppViewModel
import com.drivetree.app.data.UserSession
import com.drivetree.app.data.Instructor
import com.drivetree.app.data.entity.ApplicationEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstructorHomeScreen(openAbout: () -> Unit, onLogout: () -> Unit, appVm: AppViewModel) {
    var tab by remember { mutableStateOf(0) }
    val instructorName = UserSession.currentUserName ?: "Instructor"

    Scaffold(topBar = {
        TopAppBar(
            title = { 
                Column {
                    Text("Welcome, $instructorName (Instructor)", style = MaterialTheme.typography.titleMedium)
                }
            },
            actions = {
                IconButton(onClick = openAbout) {
                    Icon(Icons.Filled.Info, contentDescription = "About")
                }
                IconButton(onClick = onLogout) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
                }
            }
        )
    }) { pad ->
        Column(Modifier.padding(pad)) {
            TabRow(selectedTabIndex = tab) {
                listOf("Today's Lessons", "All Bookings", "Verification", "Profile").forEachIndexed { i, t ->
                    Tab(selected = tab == i, onClick = { tab = i }, text = { Text(t) })
                }
            }

            when (tab) {
                0 -> TodaysLessonsTab(appVm)
                1 -> BookingsTab(appVm)
                2 -> VerificationTab(appVm)
                3 -> ProfileTab(appVm)
            }
        }
    }
}

@Composable
private fun VerificationTab(appVm: AppViewModel) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Get current instructor info
    val instructors by appVm.instructors.collectAsState(initial = emptyList())
    val currentInstructorName = UserSession.currentUserName
    val currentUserEmail = UserSession.currentUserEmail
    
    // Check if there's already an application (any status)
    val allApps by appVm.allApps.collectAsState(initial = emptyList())
    val existingApp = remember(allApps, currentUserEmail, currentInstructorName) {
        if (currentUserEmail != null) {
            allApps.firstOrNull { it.email.equals(currentUserEmail, ignoreCase = true) }
        } else if (currentInstructorName != null) {
            allApps.firstOrNull { it.instructorName.equals(currentInstructorName, ignoreCase = true) }
        } else {
            null
        }
    }

    // Persist upload states - only set to true when instructor actually uploads
    // Use a key based on user email to prevent state leaking between users
    val uploadStateKey = currentUserEmail ?: currentInstructorName ?: "default"
    
    var licenseUploaded by rememberSaveable(key = "license_$uploadStateKey") { 
        mutableStateOf(false) 
    }
    var insuranceUploaded by rememberSaveable(key = "insurance_$uploadStateKey") { 
        mutableStateOf(false) 
    }
    var backgroundCheckUploaded by rememberSaveable(key = "background_$uploadStateKey") { 
        mutableStateOf(false) 
    }
    
    // Don't automatically set upload states to true based on existingApp
    // Upload states should only be true if the instructor actually clicked the upload button
    // The "already submitted" message is shown separately based on application status
    
    var verificationStatus by remember { mutableStateOf("Pending") }
    
    // Update status based on application
    LaunchedEffect(existingApp) {
        verificationStatus = when (existingApp?.status) {
            "PENDING" -> "Under Review"
            "APPROVED" -> "Approved"
            "REJECTED" -> "Rejected"
            else -> "Pending"
        }
    }

    val allUploaded = licenseUploaded && insuranceUploaded && backgroundCheckUploaded

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Verification Documents", style = MaterialTheme.typography.headlineMedium)

            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    DocumentUploadItem(
                        title = "Driver's License",
                        description = if (licenseUploaded) "Already uploaded" else "Upload a clear photo of your valid driver's license",
                        uploaded = licenseUploaded,
                        onUpload = { 
                            if (!licenseUploaded) {
                                licenseUploaded = true
                            }
                        }
                    )
                    DocumentUploadItem(
                        title = "Insurance Certificate",
                        description = if (insuranceUploaded) "Already uploaded" else "Upload your vehicle insurance certificate",
                        uploaded = insuranceUploaded,
                        onUpload = { 
                            if (!insuranceUploaded) {
                                insuranceUploaded = true
                            }
                        }
                    )
                    DocumentUploadItem(
                        title = "Background Check",
                        description = if (backgroundCheckUploaded) "Already uploaded" else "Upload your criminal background check certificate",
                        uploaded = backgroundCheckUploaded,
                        onUpload = { 
                            if (!backgroundCheckUploaded) {
                                backgroundCheckUploaded = true
                            }
                        }
                    )
                }
            }

            // Show "already submitted" message only if application exists, has been submitted, AND files have been uploaded
            // This prevents showing the message for newly registered instructors who haven't uploaded files yet
            if (existingApp != null && existingApp.status == "PENDING" && allUploaded) {
                ElevatedCard(
                    Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Application Already Submitted",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "Your documents have been submitted and are under review. You will be notified once verification is complete.",
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            } else if (allUploaded && existingApp == null) {
                ElevatedCard(
                    Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "All documents uploaded!",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "Click 'Submit for Review' to send your application to the admin.",
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        if (allUploaded && currentInstructorName != null && UserSession.currentUserEmail != null) {
                            val currentInstructor = instructors.firstOrNull { 
                                it.name.equals(currentInstructorName, ignoreCase = true) 
                            }
                            
                            // Use existing app ID if updating, or create new one
                            val appId = existingApp?.id ?: UUID.randomUUID().toString()
                            
                            // Create or update application for admin review
                            val app = ApplicationEntity(
                                id = appId,
                                instructorName = currentInstructorName,
                                email = UserSession.currentUserEmail ?: "",
                                submittedAt = existingApp?.submittedAt ?: System.currentTimeMillis(),
                                status = "PENDING",
                                address = currentInstructor?.address ?: existingApp?.address,
                                city = currentInstructor?.city ?: existingApp?.city,
                                pricePerHour = currentInstructor?.pricePerHour ?: existingApp?.pricePerHour,
                                carType = currentInstructor?.carType ?: existingApp?.carType,
                                languages = currentInstructor?.languages?.joinToString(", ") ?: existingApp?.languages
                            )
                            scope.launch {
                                appVm.submitApplication(app)
                                verificationStatus = "Under Review"
                                snackbarHostState.showSnackbar(
                                    if (existingApp != null) "Application updated" else "Application submitted for review"
                                )
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = allUploaded && (existingApp == null || existingApp.status == "PENDING")
                ) {
                    Text(if (existingApp != null) "Update Application" else "Submit for Review")
                }
            }

            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Verification Status", style = MaterialTheme.typography.titleMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Status:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        AssistChip(
                            onClick = {},
                            label = { Text(verificationStatus) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DocumentUploadItem(
    title: String,
    description: String,
    uploaded: Boolean,
    onUpload: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = if (uploaded) CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ) else CardDefaults.cardColors()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (uploaded) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Filled.Check, contentDescription = "Uploaded", tint = MaterialTheme.colorScheme.primary)
                    Text(
                        "Already uploaded",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                Button(onClick = onUpload) {
                    Text("Upload")
                }
            }
        }
    }
}

@Composable
private fun AvailabilityTab(appVm: AppViewModel) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val instructors by appVm.instructors.collectAsState(initial = emptyList())
    val currentInstructorName = UserSession.currentUserName
    val currentInstructor = remember(instructors, currentInstructorName) {
        if (currentInstructorName != null) {
            instructors.firstOrNull { it.name.equals(currentInstructorName, ignoreCase = true) }
        } else {
            null
        }
    }

    val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    // Initialize from current instructor's availability
    var availability by remember(currentInstructor) {
        val daysStr = currentInstructor?.availabilityDays ?: ""
        val daysList = if (daysStr.isNotBlank()) {
            daysStr.split(",").map { it.trim() }
        } else {
            emptyList()
        }
        mutableStateOf(
            daysOfWeek.associateWith { day -> day in daysList }
        )
    }

    var startTime by remember(currentInstructor) {
        mutableStateOf(currentInstructor?.availabilityStartTime ?: "9:00 AM")
    }
    var endTime by remember(currentInstructor) {
        mutableStateOf(currentInstructor?.availabilityEndTime ?: "5:00 PM")
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Weekly Availability", style = MaterialTheme.typography.headlineMedium)

            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Working Hours", style = MaterialTheme.typography.titleMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = startTime,
                            onValueChange = { startTime = it },
                            label = { Text("Start Time") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = endTime,
                            onValueChange = { endTime = it },
                            label = { Text("End Time") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Available Days", style = MaterialTheme.typography.titleMedium)
                    daysOfWeek.forEach { day ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(day)
                            Switch(
                                checked = availability[day] ?: false,
                                onCheckedChange = {
                                    availability = availability.toMutableMap().apply {
                                        this[day] = it
                                    }
                                }
                            )
                        }
                    }
                }
            }

            Button(
                onClick = {
                    if (currentInstructor != null) {
                        val selectedDays = availability.filter { it.value }.keys.joinToString(",")
                        val updated = currentInstructor.copy(
                            availabilityDays = selectedDays,
                            availabilityStartTime = startTime,
                            availabilityEndTime = endTime
                        )
                        scope.launch {
                            appVm.updateInstructor(updated)
                            snackbarHostState.showSnackbar("Availability saved successfully")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = currentInstructor != null
            ) {
                Text("Save Availability")
            }
        }
    }
}

@Composable
private fun ProfileTab(appVm: AppViewModel) {
    val instructors by appVm.instructors.collectAsState(initial = emptyList())
    val allApps by appVm.allApps.collectAsState(initial = emptyList())
    
    // Find current instructor by matching name or email with UserSession
    val currentInstructorName = UserSession.currentUserName
    val currentUserEmail = UserSession.currentUserEmail
    
    // First try to find instructor by matching email through applications
    // Since instructors don't have email, we match by finding the application with this email
    // and then finding the instructor with the same ID as that application
    val currentInstructor = remember(instructors, currentUserEmail, allApps) {
        if (currentUserEmail != null) {
            // Find application with this email
            val appForEmail = allApps.firstOrNull { it.email.equals(currentUserEmail, ignoreCase = true) }
            if (appForEmail != null) {
                // Find instructor with matching ID (application ID becomes instructor ID when approved)
                instructors.firstOrNull { it.id == appForEmail.id }
            } else if (currentInstructorName != null) {
                // Fallback: match by name
                instructors.firstOrNull { instructor ->
                    instructor.name.equals(currentInstructorName, ignoreCase = true)
                }
            } else {
                null
            }
        } else if (currentInstructorName != null) {
            // Fallback: match by name only
            instructors.firstOrNull { instructor ->
                instructor.name.equals(currentInstructorName, ignoreCase = true)
            }
        } else {
            null
        }
    }
    
    // If no instructor found, check for application by email
    // Check application independently, not based on currentInstructor
    val currentApplication = remember(allApps, currentUserEmail) {
        if (currentUserEmail != null) {
            allApps.firstOrNull { it.email.equals(currentUserEmail, ignoreCase = true) }
        } else {
            null
        }
    }
    
    // Determine which data source to use (instructor takes priority if both exist)
    val dataSource = remember(currentInstructor, currentApplication) {
        if (currentInstructor != null) {
            "instructor"
        } else if (currentApplication != null) {
            "application"
        } else {
            "none"
        }
    }

    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var pricePerHour by remember { mutableStateOf("45") }
    var carType by remember { mutableStateOf("") }
    var languages by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }

    // Update fields when data source changes
    LaunchedEffect(dataSource, currentInstructor, currentApplication, currentInstructorName, currentUserEmail) {
        when (dataSource) {
            "instructor" -> {
                if (currentInstructor != null) {
                    // Load from instructor record
                    name = currentInstructor.name
                    address = currentInstructor.address
                    city = currentInstructor.city
                    pricePerHour = currentInstructor.pricePerHour.toString()
                    carType = currentInstructor.carType
                    languages = currentInstructor.languages.joinToString(", ")
                }
            }
            "application" -> {
                if (currentApplication != null) {
                    // Load from application - use actual saved values from registration
                    name = currentApplication.instructorName
                    address = currentApplication.address ?: ""
                    city = currentApplication.city ?: ""
                    // Use the saved price, only default to 45 if truly null
                    pricePerHour = currentApplication.pricePerHour?.toString() ?: "45"
                    carType = currentApplication.carType ?: ""
                    languages = currentApplication.languages ?: ""
                }
            }
            "none" -> {
                // Fallback to session name
                if (currentInstructorName != null) {
                    name = currentInstructorName
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Instructor Profile", style = MaterialTheme.typography.headlineMedium)

        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (isEditing) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Address") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("City") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = pricePerHour,
                        onValueChange = { pricePerHour = it },
                        label = { Text("Price per Hour ($)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = carType,
                        onValueChange = { carType = it },
                        label = { Text("Car Type") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = languages,
                        onValueChange = { languages = it },
                        label = { Text("Languages (comma-separated)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                if (currentInstructor != null) {
                                    // Parse price
                                    val price = pricePerHour.toIntOrNull() ?: 45
                                    // Parse languages
                                    val langList = languages.split(",").map { it.trim() }.filter { it.isNotBlank() }

                                    val updatedInstructor = Instructor(
                                        id = currentInstructor.id,
                                        name = name,
                                        address = address,
                                        pricePerHour = price,
                                        rating = currentInstructor.rating, // Keep existing rating
                                        city = city,
                                        languages = if (langList.isNotEmpty()) langList else listOf("English"),
                                        verified = currentInstructor.verified, // Keep verification status
                                        carType = carType,
                                        photoUrl = currentInstructor.photoUrl,
                                        status = currentInstructor.status, // Keep status
                                        availabilityDays = currentInstructor.availabilityDays, // Keep availability
                                        availabilityStartTime = currentInstructor.availabilityStartTime,
                                        availabilityEndTime = currentInstructor.availabilityEndTime
                                    )
                                    appVm.updateInstructor(updatedInstructor)
                                } else if (currentApplication != null) {
                                    // Update application if not approved yet
                                    val price = pricePerHour.toIntOrNull() ?: 45
                                    val updatedApp = currentApplication.copy(
                                        instructorName = name,
                                        address = address,
                                        city = city,
                                        pricePerHour = price,
                                        carType = carType,
                                        languages = languages
                                    )
                                    appVm.submitApplication(updatedApp)
                                }
                                isEditing = false
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Save")
                        }
                        OutlinedButton(
                            onClick = {
                                // Reset to original values
                                if (currentInstructor != null) {
                                    name = currentInstructor.name
                                    address = currentInstructor.address
                                    city = currentInstructor.city
                                    pricePerHour = currentInstructor.pricePerHour.toString()
                                    carType = currentInstructor.carType
                                    languages = currentInstructor.languages.joinToString(", ")
                                } else if (currentApplication != null) {
                                    name = currentApplication.instructorName
                                    address = currentApplication.address ?: ""
                                    city = currentApplication.city ?: ""
                                    pricePerHour = (currentApplication.pricePerHour ?: 45).toString()
                                    carType = currentApplication.carType ?: ""
                                    languages = currentApplication.languages ?: ""
                                }
                                isEditing = false
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }
                    }
                } else {
                    ProfileRow("Name", name)
                    ProfileRow("Address", address)
                    ProfileRow("City", city)
                    ProfileRow("Price per Hour", "$$pricePerHour")
                    ProfileRow("Car Type", carType)
                    ProfileRow("Languages", languages)
                    if (currentInstructor != null && currentInstructor.verified) {
                        Spacer(Modifier.height(8.dp))
                        AssistChip(onClick = {}, label = { Text("Verified ✓") })
                    }
                    if (currentInstructor != null) {
                        Button(
                            onClick = { isEditing = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Edit Profile")
                        }
                    } else if (currentApplication != null && currentApplication.status == "PENDING") {
                        // Allow editing application data while pending
                        Button(
                            onClick = { isEditing = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Edit Application")
                        }
                    }
                }
            }
        }

        // Show application status if not approved yet
        if (currentInstructor == null && currentApplication != null) {
            ElevatedCard(
                Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = when (currentApplication.status) {
                        "PENDING" -> MaterialTheme.colorScheme.primaryContainer
                        "APPROVED" -> MaterialTheme.colorScheme.tertiaryContainer
                        "REJECTED" -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            ) {
                Column(
                    Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Application Status: ${currentApplication.status}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    when (currentApplication.status) {
                        "PENDING" -> Text(
                            "Your instructor application is pending admin review. You can view and edit your profile information below.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        "APPROVED" -> Text(
                            "Your application has been approved! Your instructor profile should be available shortly.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        "REJECTED" -> Text(
                            "Your application was rejected. Please contact support for more information.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        } else if (currentInstructor == null && currentApplication == null) {
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(
                    Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "No instructor profile found",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Please submit an instructor application to get started.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        if (currentInstructor != null) {
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Statistics", style = MaterialTheme.typography.titleMedium)
                    ProfileRow("Rating", "%.1f".format(currentInstructor.rating))
                    ProfileRow("Price per Hour", "$${currentInstructor.pricePerHour}")
                }
            }
        }
    }
}

@Composable
private fun ProfileRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun TodaysLessonsTab(appVm: AppViewModel) {
    val bookings by appVm.bookings.collectAsState(initial = emptyList())
    val instructors by appVm.instructors.collectAsState(initial = emptyList())
    
    // Get current instructor to filter bookings
    val currentInstructorName = UserSession.currentUserName
    val currentInstructor = remember(instructors, currentInstructorName) {
        if (currentInstructorName != null) {
            instructors.firstOrNull { it.name.equals(currentInstructorName, ignoreCase = true) }
        } else {
            null
        }
    }
    
    // Filter bookings for today
    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val tomorrow = Calendar.getInstance().apply {
        timeInMillis = today.timeInMillis
        add(Calendar.DAY_OF_YEAR, 1)
    }
    
    val todaysBookings = remember(bookings, currentInstructor, today.timeInMillis, tomorrow.timeInMillis) {
        if (currentInstructor != null) {
            bookings.filter { 
                it.instructorId == currentInstructor.id &&
                it.epochTime >= today.timeInMillis &&
                it.epochTime < tomorrow.timeInMillis &&
                (it.status == "APPROVED" || it.status == "REQUESTED")
            }.sortedBy { it.epochTime }
        } else {
            emptyList()
        }
    }
    
    val dateFmt = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val dateFmtFull = remember { SimpleDateFormat("EEE, MMM d, yyyy  h:mm a", Locale.getDefault()) }

    if (currentInstructor == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No instructor profile found")
        }
    } else if (todaysBookings.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("No lessons scheduled for today", style = MaterialTheme.typography.titleMedium)
                Text("Check 'All Bookings' for upcoming lessons", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(todaysBookings) { b ->
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Student: ${b.studentName}", style = MaterialTheme.typography.titleMedium)
                        Text("Time: ${dateFmt.format(Date(b.epochTime))}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        b.pickupLocation?.let {
                            Text("Pickup: $it", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("Status: ${b.status}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        
                        if (b.status == "APPROVED") {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Button(
                                    onClick = { appVm.updateBookingStatus(b.id, "COMPLETED") },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.tertiary
                                    )
                                ) {
                                    Icon(Icons.Filled.Check, contentDescription = null)
                                    Spacer(Modifier.width(4.dp))
                                    Text("Mark Completed")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BookingsTab(appVm: AppViewModel) {
    val bookings by appVm.bookings.collectAsState(initial = emptyList())
    val instructors by appVm.instructors.collectAsState(initial = emptyList())
    
    // Get current instructor to filter bookings
    val currentInstructorName = UserSession.currentUserName
    val currentUserEmail = UserSession.currentUserEmail
    val currentInstructor = remember(instructors, currentInstructorName) {
        if (currentInstructorName != null) {
            instructors.firstOrNull { it.name.equals(currentInstructorName, ignoreCase = true) }
        } else {
            null
        }
    }
    
    // Filter bookings by current instructor's ID
    val filteredBookings = remember(bookings, currentInstructor) {
        if (currentInstructor != null) {
            bookings.filter { it.instructorId == currentInstructor.id }
        } else {
            // If no instructor record yet, show empty (they need to be approved first)
            emptyList()
        }
    }
    
    val sorted = filteredBookings.sortedByDescending { it.epochTime }
    val dateFmt = remember { SimpleDateFormat("EEE, MMM d, yyyy  h:mm a", Locale.getDefault()) }

    if (currentInstructor == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "No instructor profile found",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "Your application must be approved before you can receive bookings.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else if (sorted.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No booking requests yet")
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sorted) { b ->
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Student: ${b.studentName}", style = MaterialTheme.typography.titleMedium)
                        Text("Email: ${b.studentEmail}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Date & Time: ${dateFmt.format(Date(b.epochTime))}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        b.pickupLocation?.let {
                            Text("Pickup Location: $it", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        b.note?.let {
                            Text("Note: $it", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                        }
                        Text("Status: ${b.status}", color = MaterialTheme.colorScheme.onSurfaceVariant)

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                            if (b.status == "REQUESTED") {
                                Button(onClick = { appVm.approveBooking(b.id) }) {
                                    Icon(Icons.Filled.Check, contentDescription = null)
                                    Spacer(Modifier.width(4.dp))
                                    Text("Approve")
                                }
                                OutlinedButton(onClick = { appVm.rejectBooking(b.id) }) {
                                    Icon(Icons.Filled.Close, contentDescription = null)
                                    Spacer(Modifier.width(4.dp))
                                    Text("Reject")
                                }
                            } else if (b.status == "APPROVED") {
                                Button(
                                    onClick = { appVm.updateBookingStatus(b.id, "COMPLETED") },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.tertiary
                                    )
                                ) {
                                    Icon(Icons.Filled.Check, contentDescription = null)
                                    Spacer(Modifier.width(4.dp))
                                    Text("Mark Completed")
                                }
                            } else if (b.status == "COMPLETED") {
                                AssistChip(
                                    onClick = {},
                                    label = { Text("Lesson Completed ✓") },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

