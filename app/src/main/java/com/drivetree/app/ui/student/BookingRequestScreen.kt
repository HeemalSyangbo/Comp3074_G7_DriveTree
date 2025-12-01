package com.drivetree.app.ui.student

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.drivetree.app.data.AppViewModel
import com.drivetree.app.data.UserSession
import com.drivetree.app.data.entity.BookingEntity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingRequestScreen(
    instructorId: String,
    appVm: AppViewModel,
    onClose: () -> Unit,
    onBookingSuccess: () -> Unit = onClose
) {
    // Pre-fill from user session
    val sessionName = UserSession.currentUserName ?: ""
    val sessionEmail = UserSession.currentUserEmail ?: ""
    
    var name by remember { mutableStateOf(sessionName) }
    var email by remember { mutableStateOf(sessionEmail) }
    var pickupLocation by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }
    
    // Update when session changes
    LaunchedEffect(sessionName, sessionEmail) {
        if (name.isBlank()) name = sessionName
        if (email.isBlank()) email = sessionEmail
    }

    // --- Date & time picking state ---
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    // Default = now + 1 hour (so it's in near future)
    val defaultCal = remember {
        Calendar.getInstance().apply { add(Calendar.HOUR_OF_DAY, 1) }
    }
    var selectedDateMillis by remember { mutableStateOf<Long?>(defaultCal.timeInMillis) }
    val timeState = rememberTimePickerState(
        initialHour = defaultCal.get(Calendar.HOUR_OF_DAY),
        initialMinute = defaultCal.get(Calendar.MINUTE),
        is24Hour = false
    )

    val snackbarHost = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun combinedEpochMillis(): Long {
        val base = Calendar.getInstance()
        base.timeInMillis = selectedDateMillis ?: System.currentTimeMillis()
        base.set(Calendar.HOUR_OF_DAY, timeState.hour)
        base.set(Calendar.MINUTE, timeState.minute)
        base.set(Calendar.SECOND, 0)
        base.set(Calendar.MILLISECOND, 0)
        return base.timeInMillis
    }

    val prettyDateTime = remember(selectedDateMillis, timeState.hour, timeState.minute) {
        val fmt = SimpleDateFormat("EEE, MMM d, yyyy  h:mm a", Locale.getDefault())
        fmt.format(Date(combinedEpochMillis()))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Request Lesson") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHost) }
    ) { pad ->
        Column(
            Modifier.padding(pad).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Your name") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )

            // ---- Date & Time pickers ----
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Selected: $prettyDateTime")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { showDatePicker = true }) { Text("Pick date") }
                    Button(onClick = { showTimePicker = true }) { Text("Pick time") }
                }
            }
            
            // Pickup Location
            OutlinedTextField(
                value = pickupLocation,
                onValueChange = { pickupLocation = it },
                label = { Text("Pickup Location") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Enter pickup address") }
            )
            
            // Note (optional)
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (optional)") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Any special instructions or requests") },
                maxLines = 3
            )

            Button(
                onClick = {
                    val booking = BookingEntity(
                        id = UUID.randomUUID().toString(),
                        instructorId = instructorId,
                        studentName = name.ifBlank { sessionName.ifBlank { "Student" } },
                        studentEmail = email.ifBlank { sessionEmail },
                        epochTime = combinedEpochMillis(),   // <-- use picked date+time
                        status = "REQUESTED",
                        pickupLocation = pickupLocation.ifBlank { null },
                        note = note.ifBlank { null }
                    )
                    appVm.requestBooking(booking)
                    showSuccessDialog = true
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = email.contains("@") && name.isNotBlank() && pickupLocation.isNotBlank()
            ) {
                Text("Submit Booking")
            }
        }
    }

    // ---------- Date Picker Dialog ----------
    if (showDatePicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedDateMillis = state.selectedDateMillis ?: selectedDateMillis
                        showDatePicker = false
                    }
                ) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) {
            DatePicker(state = state)
        }
    }

    // ---------- Time Picker Dialog ----------
    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Cancel") } },
            text = { TimePicker(state = timeState) }
        )
    }
    
    // ---------- Success Dialog ----------
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { 
                showSuccessDialog = false
                onBookingSuccess()
            },
            title = { Text("Booking Successful!") },
            text = { 
                val instructorName = appVm.instructors.collectAsState(initial = emptyList()).value
                    .firstOrNull { it.id == instructorId }?.name ?: "the instructor"
                Text("Booking request sent to $instructorName") 
            },
            confirmButton = {
                Button(onClick = { 
                    showSuccessDialog = false
                    onBookingSuccess()
                }) {
                    Text("OK")
                }
            }
        )
    }
}
