package com.drivetree.app.ui.instructor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.drivetree.app.data.AppViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstructorHomeScreen(openAbout: () -> Unit, appVm: AppViewModel) {
    var tab by remember { mutableStateOf(0) }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Instructor") },
            actions = { IconButton(onClick = openAbout) { Icon(Icons.Filled.Info, contentDescription = null) } }
        )
    }) { pad ->
        Column(Modifier.padding(pad)) {
            TabRow(selectedTabIndex = tab) {
                listOf("Verification", "Availability", "Bookings", "Profile").forEachIndexed { i, t ->
                    Tab(selected = tab == i, onClick = { tab = i }, text = { Text(t) })
                }
            }

            when (tab) {
                0 -> Text("Upload documents (UI only)", modifier = Modifier.padding(16.dp))
                1 -> Text("Weekly availability (placeholder)", modifier = Modifier.padding(16.dp))
                2 -> BookingsTab(appVm)
                else -> Text("Profile (placeholder)", modifier = Modifier.padding(16.dp))
            }
        }
    }
}

@Composable
private fun BookingsTab(appVm: AppViewModel) {
    val bookings by appVm.bookings.collectAsState(initial = emptyList())
    val sorted = bookings.sortedByDescending { it.epochTime }
    val dateFmt = remember { SimpleDateFormat("EEE, MMM d, yyyy  h:mm a", Locale.getDefault()) }

    if (sorted.isEmpty()) {
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
                        Text("When: ${dateFmt.format(Date(b.epochTime))}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Status: ${b.status}", color = MaterialTheme.colorScheme.onSurfaceVariant)

                        if (b.status == "REQUESTED") {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
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
                            }
                        }
                    }
                }
            }
        }
    }
}
