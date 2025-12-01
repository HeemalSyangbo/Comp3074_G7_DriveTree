package com.drivetree.app.ui.student

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.drivetree.app.data.AppViewModel
import com.drivetree.app.data.UserSession
import kotlinx.coroutines.launch
import androidx.compose.material3.ExperimentalMaterial3Api   // <-- added
import java.text.SimpleDateFormat                          // <-- added
import java.util.Calendar                                  // <-- added
import java.util.Date                                      // <-- added
import java.util.Locale                                    // <-- added

enum class SortBy { Relevance, Price, Rating, Name }

@OptIn(ExperimentalMaterial3Api::class)                 // <-- added
@Composable
fun StudentHomeScreen(
    openProfile: (String) -> Unit,
    openAbout: () -> Unit,
    onLogout: () -> Unit,
    appVm: AppViewModel
) {
    var tab by remember { mutableStateOf(0) }
    
    // Check if we should show bookings tab (set after booking success)
    LaunchedEffect(Unit) {
        if (UserSession.shouldShowBookingsTab) {
            tab = 1  // Bookings tab
            UserSession.shouldShowBookingsTab = false  // Reset flag
        }
    }

    val studentName = UserSession.currentUserName ?: "Student"
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Hi, $studentName (Student)", style = MaterialTheme.typography.titleMedium)
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
        }
    ) { pad ->
        Column(Modifier.padding(pad)) {
            TabRow(selectedTabIndex = tab) {
                listOf("Find Instructors", "My Bookings", "Resources", "Profile").forEachIndexed { i, label ->
                    Tab(selected = tab == i, onClick = { tab = i }, text = { Text(label) })
                }
            }
            when (tab) {
                0 -> SearchTab(openProfile, appVm)
                1 -> BookingsTab(appVm, openProfile)
                2 -> ResourcesTab()
                3 -> ProfileTab(appVm)
                else -> Placeholder("Coming soon…")
            }
        }
    }
}

@Composable
private fun SearchTab(openProfile: (String) -> Unit, appVm: AppViewModel) {
    val instructors by appVm.instructors.collectAsState(initial = emptyList())

    var query by remember { mutableStateOf("") }
    var verifiedOnly by remember { mutableStateOf(false) }
    var sortBy by remember { mutableStateOf(SortBy.Relevance) }
    
    // Transmission filter (extract from carType)
    var selectedTransmission by remember { mutableStateOf<String?>(null) } // "Automatic" or "Manual"

    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Search name / address / city / language") },
            modifier = Modifier.fillMaxWidth()
        )

        // Filter controls
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                FilterChip(
                    selected = verifiedOnly,
                    onClick = { verifiedOnly = !verifiedOnly },
                    label = { Text(if (verifiedOnly) "Verified ✓" else "Verified") },
                    modifier = Modifier.weight(1f)
                )
                SortSelectorStable(sortBy) { sortBy = it }
            }
            
            // Transmission filter
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Transmission:", style = MaterialTheme.typography.labelMedium)
                FilterChip(
                    selected = selectedTransmission == null,
                    onClick = { selectedTransmission = null },
                    label = { Text("All") }
                )
                FilterChip(
                    selected = selectedTransmission == "Automatic",
                    onClick = { selectedTransmission = if (selectedTransmission == "Automatic") null else "Automatic" },
                    label = { Text("Automatic") }
                )
                FilterChip(
                    selected = selectedTransmission == "Manual",
                    onClick = { selectedTransmission = if (selectedTransmission == "Manual") null else "Manual" },
                    label = { Text("Manual") }
                )
            }
        }

        val filtered = instructors
            .asSequence()
            .filter { it.status == "ACTIVE" }  // Only show active instructors
            .filter { if (!verifiedOnly) true else it.verified }
            .filter { 
                if (selectedTransmission == null) true else {
                    val carTypeLower = it.carType.lowercase()
                    when (selectedTransmission) {
                        "Automatic" -> carTypeLower.contains("automatic")
                        "Manual" -> carTypeLower.contains("manual")
                        else -> true
                    }
                }
            }
            .filter {
                if (query.isBlank()) true else {
                    listOf(it.name, it.address, it.city, it.languages.joinToString(" "))
                        .any { s -> s.contains(query, ignoreCase = true) }
                }
            }
            .let { seq ->
                when (sortBy) {
                    SortBy.Relevance -> seq
                    SortBy.Price -> seq.sortedBy { it.pricePerHour }
                    SortBy.Rating -> seq.sortedByDescending { it.rating }
                    SortBy.Name -> seq.sortedBy { it.name.lowercase() }
                }
            }
            .toList()

        if (filtered.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(top = 24.dp), contentAlignment = Alignment.Center) {
                Text("No instructors match your filters")
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(filtered) { r ->
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { openProfile(r.id) }
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(r.name, style = MaterialTheme.typography.titleMedium)
                            Text(r.address, color = MaterialTheme.colorScheme.onSurfaceVariant)

                            // Int price (no .1f) + rating with one decimal
                            Text("${r.city} • $${r.pricePerHour}/hr • ★${"%.1f".format(r.rating)}")

                            Text("Languages: ${r.languages.joinToString("/")}")
                            if (r.verified)
                                AssistChip(onClick = {}, label = { Text("Verified") })
                        }
                    }
                }
            }
        }
    }
}

/**
 * Stable dropdown (no ExposedDropdownMenuBox / menuAnchor), works with older Material3 too.
 */
@Composable
private fun SortSelectorStable(current: SortBy, onChange: (SortBy) -> Unit) {
    var open by remember { mutableStateOf(false) }
    val label = when (current) {
        SortBy.Relevance -> "Sort: Relevance"
        SortBy.Price -> "Sort: Price"
        SortBy.Rating -> "Sort: Rating"
        SortBy.Name -> "Sort: Name"
    }

    Box {
        OutlinedButton(onClick = { open = true }) {
            Text(label)
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            SortBy.values().forEach { opt ->
                DropdownMenuItem(
                    text = { Text(opt.name) },
                    onClick = {
                        onChange(opt)
                        open = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookingsTab(appVm: AppViewModel, openProfile: (String) -> Unit) {
    val bookings by appVm.bookings.collectAsState(initial = emptyList())
    val instructors by appVm.instructors.collectAsState(initial = emptyList())
    
    // Get current user's email from session
    val currentUserEmail = UserSession.currentUserEmail ?: ""
    
    // “My bookings” filter - default to true (show only my bookings)
    var myOnly by rememberSaveable { mutableStateOf(true) }
    // Auto-populate email from session
    var myEmail by rememberSaveable { mutableStateOf(currentUserEmail) }
    
    // Update email when session changes (e.g., new user logs in)
    LaunchedEffect(currentUserEmail) {
        myEmail = currentUserEmail
        myOnly = true  // Reset to show only my bookings when user changes
    }

    // Date formatter (remember so it isn't recreated per item)
    val dateFmt = remember {
        SimpleDateFormat("EEE, MMM d, yyyy  h:mm a", Locale.getDefault())
    }

    // Confirm dialog state
    var toCancelId by remember { mutableStateOf<String?>(null) }
    var toRescheduleId by remember { mutableStateOf<String?>(null) }
    
    // Reschedule dialog state
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    
    // Get the booking being rescheduled to initialize time picker
    val bookingToReschedule = remember(toRescheduleId) {
        bookings.firstOrNull { it.id == toRescheduleId }
    }
    val defaultCal = remember(toRescheduleId) {
        Calendar.getInstance().apply {
            timeInMillis = bookingToReschedule?.epochTime ?: System.currentTimeMillis()
        }
    }
    // Create timeState directly (composable function) - will be recreated when toRescheduleId changes
    val timeState = rememberTimePickerState(
        initialHour = defaultCal.get(Calendar.HOUR_OF_DAY),
        initialMinute = defaultCal.get(Calendar.MINUTE),
        is24Hour = false
    )

    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = myOnly,
                onClick = { myOnly = !myOnly },
                label = { Text(if (myOnly) "My bookings ✓" else "All bookings") }
            )
            OutlinedTextField(
                value = if (myOnly) currentUserEmail else myEmail,
                onValueChange = { if (!myOnly) myEmail = it },
                label = { Text(if (myOnly) "Your email" else "Filter by email") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                enabled = !myOnly,  // Allow editing only when showing all bookings
                readOnly = myOnly  // Read-only when showing my bookings
            )
        }

        val list = bookings
            .asSequence()
            .filter { 
                if (myOnly) {
                    // When "My bookings" is on, filter by current user's email
                    it.studentEmail.equals(currentUserEmail, ignoreCase = true)
                } else {
                    // When showing all bookings, optionally filter by entered email
                    if (myEmail.isBlank()) {
                        true  // Show all if no email filter entered
                    } else {
                        it.studentEmail.equals(myEmail, ignoreCase = true)  // Filter by entered email
                    }
                }
            }
            .sortedByDescending { it.epochTime }
            .toList()

        if (list.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No bookings${if (myOnly && myEmail.isNotBlank()) " for $myEmail" else ""}")
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(list) { b ->
                    // Find instructor name by ID
                    val instructorName = instructors.firstOrNull { it.id == b.instructorId }?.name ?: "Instructor"
                    
                    ElevatedCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            val whenText = dateFmt.format(Date(b.epochTime))

                            Text("Booking with $instructorName", style = MaterialTheme.typography.titleMedium)
                            Text("Date & Time: $whenText", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            b.pickupLocation?.let {
                                Text("Pickup Location: $it", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text("Status: ${b.status}", color = MaterialTheme.colorScheme.onSurfaceVariant)

                            Spacer(Modifier.height(8.dp))

                            // Allow cancel and reschedule for REQUESTED / APPROVED
                            val canModify = b.status == "REQUESTED" || b.status == "APPROVED"
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Spacer(Modifier.weight(1f))
                                TextButton(
                                    onClick = { 
                                        toRescheduleId = b.id
                                        selectedDateMillis = b.epochTime
                                    },
                                    enabled = canModify
                                ) { Text("Reschedule") }
                                TextButton(
                                    onClick = { toCancelId = b.id },
                                    enabled = canModify
                                ) { Text("Cancel") }
                            }
                        }
                    }
                }
            }
        }
    }

    // Reschedule date/time pickers
    if (toRescheduleId != null && bookingToReschedule != null) {
        val combinedEpochMillis = remember(selectedDateMillis, timeState.hour, timeState.minute) {
            val base = Calendar.getInstance()
            base.timeInMillis = selectedDateMillis ?: bookingToReschedule.epochTime
            base.set(Calendar.HOUR_OF_DAY, timeState.hour)
            base.set(Calendar.MINUTE, timeState.minute)
            base.set(Calendar.SECOND, 0)
            base.set(Calendar.MILLISECOND, 0)
            base.timeInMillis
        }
        val prettyDateTime = remember(selectedDateMillis, timeState.hour, timeState.minute) {
            val fmt = SimpleDateFormat("EEE, MMM d, yyyy  h:mm a", Locale.getDefault())
            fmt.format(Date(combinedEpochMillis))
        }
        
        AlertDialog(
            onDismissRequest = { 
                toRescheduleId = null
                selectedDateMillis = null
            },
            title = { Text("Reschedule Booking") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("New time: $prettyDateTime")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { showDatePicker = true }) { Text("Pick date") }
                        Button(onClick = { showTimePicker = true }) { Text("Pick time") }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    appVm.rescheduleBooking(toRescheduleId!!, combinedEpochMillis)
                    toRescheduleId = null
                    selectedDateMillis = null
                }) { Text("Reschedule") }
            },
            dismissButton = { 
                TextButton(onClick = { 
                    toRescheduleId = null
                    selectedDateMillis = null
                }) { Text("Cancel") } 
            }
        )
    }
    
    // Date picker for reschedule
    if (showDatePicker && toRescheduleId != null) {
        val state = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedDateMillis = state.selectedDateMillis ?: selectedDateMillis
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) {
            DatePicker(state = state)
        }
    }
    
    // Time picker for reschedule
    if (showTimePicker && toRescheduleId != null) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Cancel") } },
            text = { 
                TimePicker(state = timeState) 
            }
        )
    }
    
    // Confirm dialog
    if (toCancelId != null) {
        AlertDialog(
            onDismissRequest = { toCancelId = null },
            confirmButton = {
                TextButton(onClick = {
                    appVm.cancelBooking(toCancelId!!)
                    toCancelId = null
                }) { Text("Yes, cancel") }
            },
            dismissButton = { TextButton(onClick = { toCancelId = null }) { Text("Keep") } },
            title = { Text("Cancel booking?") },
            text = { Text("This will set the status to CANCELLED.") }
        )
    }
}

@Composable
private fun ResourcesTab() {
    val resources = remember {
        listOf(
            ResourceItem("Road Signs Guide", "Learn all traffic signs and their meanings", "📚"),
            ResourceItem("Parallel Parking Tips", "Step-by-step guide to master parallel parking", "🚗"),
            ResourceItem("Highway Driving", "Essential tips for safe highway driving", "🛣️"),
            ResourceItem("Parking Rules", "Understanding parking regulations and restrictions", "🅿️"),
            ResourceItem("Right of Way", "Learn who has the right of way in different situations", "🚦"),
            ResourceItem("Emergency Procedures", "What to do in case of accidents or breakdowns", "🚨"),
            ResourceItem("Weather Driving", "Tips for driving in rain, snow, and fog", "🌧️"),
            ResourceItem("Night Driving", "Safety guidelines for driving at night", "🌙")
        )
    }

    LazyColumn(
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(resources) { resource ->
            ElevatedCard(Modifier.fillMaxWidth()) {
                Row(
                    Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = resource.icon,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.width(48.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            resource.title,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            resource.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileTab(appVm: AppViewModel) {
    val scope = rememberCoroutineScope()
    val currentUserEmail = UserSession.currentUserEmail ?: ""
    
    // Load student profile from database - prioritize database over session
    val studentFlow = remember(currentUserEmail) {
        if (currentUserEmail.isNotBlank()) {
            appVm.studentByEmail(currentUserEmail)
        } else {
            null
        }
    }
    val student by (studentFlow?.collectAsState(initial = null) ?: remember { mutableStateOf(null) })
    
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }
    
    // Update fields when student data is loaded from database
    // Prioritize database data over session data
    LaunchedEffect(student, currentUserEmail) {
        if (student != null) {
            // Database has data - use it
            name = student!!.name
            email = student!!.email
            phone = student!!.phone
            address = student!!.address
            // Update UserSession to match database
            UserSession.setStudentProfile(
                student!!.name,
                student!!.email,
                student!!.phone,
                student!!.address
            )
        } else {
            // No database record - use session data
            name = UserSession.currentUserName ?: ""
            email = UserSession.currentUserEmail ?: currentUserEmail
            phone = UserSession.currentUserPhone ?: ""
            address = UserSession.currentUserAddress ?: ""
        }
    }
    
    val bookings by appVm.bookings.collectAsState(initial = emptyList())
    val myBookings = bookings.filter { it.studentEmail.equals(email, ignoreCase = true) }
    val upcomingBookings = myBookings.filter { it.epochTime > System.currentTimeMillis() && it.status == "APPROVED" }
    val completedBookings = myBookings.filter { it.status == "COMPLETED" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Profile", style = MaterialTheme.typography.headlineMedium)

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
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Address") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = { 
                                scope.launch {
                                    // Save to database
                                    val student = com.drivetree.app.data.entity.StudentEntity(
                                        email = email,
                                        name = name,
                                        phone = phone,
                                        address = address
                                    )
                                    appVm.upsertStudent(student)
                                    // Update session when saving (including phone and address)
                                    UserSession.setStudentProfile(
                                        name, 
                                        email, 
                                        phone, 
                                        address
                                    )
                                    isEditing = false
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Save")
                        }
                        OutlinedButton(
                            onClick = { 
                                // Reset to original values from database or session
                                if (student != null) {
                                    name = student!!.name
                                    email = student!!.email
                                    phone = student!!.phone
                                    address = student!!.address
                                } else {
                                    name = UserSession.currentUserName ?: ""
                                    email = UserSession.currentUserEmail ?: ""
                                    phone = UserSession.currentUserPhone ?: ""
                                    address = UserSession.currentUserAddress ?: ""
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
                    ProfileRow("Email", email)
                    ProfileRow("Phone", phone)
                    ProfileRow("Address", address)
                    Button(
                        onClick = { isEditing = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Edit Profile")
                    }
                }
            }
        }

        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Statistics", style = MaterialTheme.typography.titleMedium)
                ProfileRow("Total Bookings", myBookings.size.toString())
                ProfileRow("Completed Lessons", completedBookings.size.toString())
                ProfileRow("Upcoming Lessons", upcomingBookings.size.toString())
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

data class ResourceItem(val title: String, val description: String, val icon: String)

@Composable
fun Placeholder(text: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text)
    }
}
