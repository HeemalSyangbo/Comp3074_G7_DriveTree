package com.drivetree.app.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.drivetree.app.data.AppViewModel
import com.drivetree.app.data.entity.ApplicationEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen(openAbout: () -> Unit, onLogout: () -> Unit, appVm: AppViewModel) {
    var tab by remember { mutableStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard") },
                actions = {
                    IconButton(onClick = openAbout) {
                        Icon(Icons.Filled.Info, contentDescription = "About")
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { pad ->
        Column(Modifier.padding(pad)) {
            TabRow(selectedTabIndex = tab) {
                listOf("Pending", "All Applications", "Users", "Reports").forEachIndexed { i, t ->
                    Tab(selected = tab == i, onClick = { tab = i }, text = { Text(t) })
                }
            }

            when (tab) {
                0 -> PendingTab(
                    appVm = appVm,
                    onSnack = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } }
                )
                1 -> AllTab(appVm)
                2 -> UsersTab(
                    appVm = appVm,
                    onSnack = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } }
                )
                else -> ReportsTab(appVm)
            }
        }
    }
}

@Composable
private fun AllInstructorsTab(appVm: AppViewModel) {
    val instructors by appVm.instructors.collectAsState(initial = emptyList())
    
    if (instructors.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No instructors found")
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(instructors) { instructor ->
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(instructor.name, style = MaterialTheme.typography.titleMedium)
                                Text("${instructor.city} • ★${"%.1f".format(instructor.rating)}", 
                                     color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (instructor.verified) {
                                AssistChip(
                                    onClick = {},
                                    label = { Text("Verified ✓") },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer
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

@Composable
private fun AllBookingsTab(appVm: AppViewModel) {
    val bookings by appVm.bookings.collectAsState(initial = emptyList())
    val instructors by appVm.instructors.collectAsState(initial = emptyList())
    
    val dateFmt = remember { SimpleDateFormat("EEE, MMM d, yyyy  h:mm a", Locale.getDefault()) }
    
    if (bookings.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No bookings yet")
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(bookings.sortedByDescending { it.epochTime }) { booking ->
                val instructor = instructors.firstOrNull { it.id == booking.instructorId }
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Student: ${booking.studentName}", style = MaterialTheme.typography.titleMedium)
                        Text("Instructor: ${instructor?.name ?: "Unknown"}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Date & Time: ${dateFmt.format(Date(booking.epochTime))}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        booking.pickupLocation?.let {
                            Text("Pickup: $it", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("Status: ${booking.status}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun PendingTab(appVm: AppViewModel, onSnack: (String) -> Unit) {
    val pending by appVm.pendingApps.collectAsState(initial = emptyList())
    if (pending.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No pending applications")
        }
    } else {
        ApplicationList(
            apps = pending,
            appVm = appVm,
            showActions = true,
            onApproved = { onSnack("Approved: ${it.instructorName}") },
            onRejected = { onSnack("Rejected: ${it.instructorName}") }
        )
    }
}

@Composable
private fun AllTab(appVm: AppViewModel) {
    val all by appVm.allApps.collectAsState(initial = emptyList())
    if (all.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No applications yet")
        }
    } else {
        ApplicationList(
            apps = all,
            appVm = appVm,
            showActions = false
        )
    }
}

@Composable
private fun ReportsTab(appVm: AppViewModel) {
    val pending by appVm.pendingApps.collectAsState(initial = emptyList())
    val allApps by appVm.allApps.collectAsState(initial = emptyList())
    val instructors by appVm.instructors.collectAsState(initial = emptyList())
    val bookings by appVm.bookings.collectAsState(initial = emptyList())

    val fmt = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }
    val lastBookingDate = bookings.maxByOrNull { it.epochTime }?.epochTime?.let { fmt.format(Date(it)) } ?: "—"

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Reports", style = MaterialTheme.typography.titleLarge)

        StatCard(
            title = "Pending Applications",
            value = pending.size.toString(),
            subtitle = "Awaiting review"
        )
        StatCard(
            title = "Total Applications",
            value = allApps.size.toString(),
            subtitle = "All statuses"
        )
        StatCard(
            title = "Instructors",
            value = instructors.size.toString(),
            subtitle = "Visible to students"
        )
        StatCard(
            title = "Bookings",
            value = bookings.size.toString(),
            subtitle = "Last booking: $lastBookingDate"
        )
    }
}

@Composable
private fun StatCard(title: String, value: String, subtitle: String) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(value, style = MaterialTheme.typography.headlineMedium)
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ApplicationList(
    apps: List<ApplicationEntity>,
    appVm: AppViewModel,
    showActions: Boolean = true,
    onApproved: (ApplicationEntity) -> Unit = {},
    onRejected: (ApplicationEntity) -> Unit = {}
) {
    LazyColumn(
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(apps) { app ->
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text(app.instructorName, style = MaterialTheme.typography.titleMedium)
                    Text(app.email, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Status: ${app.status}", color = MaterialTheme.colorScheme.onSurfaceVariant)

                    if (showActions && app.status == "PENDING") {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Button(
                                onClick = {
                                    appVm.approveApplication(app.id)
                                    onApproved(app)
                                }
                            ) {
                                Icon(Icons.Filled.Check, contentDescription = "Approve")
                                Spacer(Modifier.width(6.dp))
                                Text("Approve")
                            }
                            OutlinedButton(
                                onClick = {
                                    appVm.rejectApplication(app.id)
                                    onRejected(app)
                                }
                            ) {
                                Icon(Icons.Filled.Close, contentDescription = "Reject")
                                Spacer(Modifier.width(6.dp))
                                Text("Reject")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UsersTab(appVm: AppViewModel, onSnack: (String) -> Unit) {
    val instructors by appVm.instructors.collectAsState(initial = emptyList())
    var filterStatus by remember { mutableStateOf<String?>(null) }
    var showBanDialog by remember { mutableStateOf<String?>(null) }
    var showSuspendDialog by remember { mutableStateOf<String?>(null) }
    
    val statusOptions = listOf("All", "ACTIVE", "SUSPENDED", "BANNED")
    
    Column(Modifier.fillMaxSize()) {
        // Filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            statusOptions.forEach { status ->
                FilterChip(
                    selected = filterStatus == status || (filterStatus == null && status == "All"),
                    onClick = { filterStatus = if (status == "All") null else status },
                    label = { Text(status) }
                )
            }
        }
        
        val filteredInstructors = if (filterStatus == null) {
            instructors
        } else {
            instructors.filter { it.status.equals(filterStatus, ignoreCase = true) }
        }
        
        if (filteredInstructors.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No instructors found")
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredInstructors) { instructor ->
                    ElevatedCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(instructor.name, style = MaterialTheme.typography.titleMedium)
                                    Text("${instructor.city} • $${instructor.pricePerHour}/hr", 
                                         color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                AssistChip(
                                    onClick = {},
                                    label = { Text(instructor.status) },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = when (instructor.status) {
                                            "ACTIVE" -> MaterialTheme.colorScheme.primaryContainer
                                            "SUSPENDED" -> MaterialTheme.colorScheme.errorContainer
                                            "BANNED" -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                        }
                                    )
                                )
                            }
                            
                            if (instructor.status == "ACTIVE") {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { showSuspendDialog = instructor.id },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Filled.Pause, contentDescription = null)
                                        Spacer(Modifier.width(4.dp))
                                        Text("Suspend")
                                    }
                                    OutlinedButton(
                                        onClick = { showBanDialog = instructor.id },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = MaterialTheme.colorScheme.error
                                        )
                                    ) {
                                        Icon(Icons.Filled.Block, contentDescription = null)
                                        Spacer(Modifier.width(4.dp))
                                        Text("Ban")
                                    }
                                }
                            } else if (instructor.status == "SUSPENDED" || instructor.status == "BANNED") {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            appVm.updateInstructorStatus(instructor.id, "ACTIVE")
                                            onSnack("${instructor.name} account reactivated")
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Filled.Check, contentDescription = null)
                                        Spacer(Modifier.width(4.dp))
                                        Text("Reactivate")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Ban confirmation dialog
    if (showBanDialog != null) {
        val instructor = instructors.firstOrNull { it.id == showBanDialog }
        AlertDialog(
            onDismissRequest = { showBanDialog = null },
            title = { Text("Ban Instructor?") },
            text = { Text("This will ban ${instructor?.name ?: "this instructor"}. They will not be able to use the app.") },
            confirmButton = {
                Button(
                    onClick = {
                        instructor?.let {
                            appVm.updateInstructorStatus(it.id, "BANNED")
                            onSnack("${it.name} has been banned")
                        }
                        showBanDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Ban")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBanDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Suspend confirmation dialog
    if (showSuspendDialog != null) {
        val instructor = instructors.firstOrNull { it.id == showSuspendDialog }
        AlertDialog(
            onDismissRequest = { showSuspendDialog = null },
            title = { Text("Suspend Instructor?") },
            text = { Text("This will suspend ${instructor?.name ?: "this instructor"}. They will not be visible to students.") },
            confirmButton = {
                Button(
                    onClick = {
                        instructor?.let {
                            appVm.updateInstructorStatus(it.id, "SUSPENDED")
                            onSnack("${it.name} has been suspended")
                        }
                        showSuspendDialog = null
                    }
                ) {
                    Text("Suspend")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSuspendDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
