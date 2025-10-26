package com.drivetree.app.ui.admin

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
import com.drivetree.app.data.entity.ApplicationEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen(openAbout: () -> Unit, appVm: AppViewModel) {
    var tab by remember { mutableStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard") },
                actions = {
                    IconButton(onClick = openAbout) {
                        Icon(Icons.Filled.Info, contentDescription = null)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { pad ->
        Column(Modifier.padding(pad)) {
            TabRow(selectedTabIndex = tab) {
                listOf("Pending", "All Applications", "Reports").forEachIndexed { i, t ->
                    Tab(selected = tab == i, onClick = { tab = i }, text = { Text(t) })
                }
            }

            when (tab) {
                0 -> PendingTab(
                    appVm = appVm,
                    onSnack = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } }
                )
                1 -> AllTab(appVm)
                else -> ReportsTab(appVm)
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
