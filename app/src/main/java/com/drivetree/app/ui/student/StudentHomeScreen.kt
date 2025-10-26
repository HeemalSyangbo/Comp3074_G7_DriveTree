package com.drivetree.app.ui.student

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.drivetree.app.data.AppViewModel
import androidx.compose.material3.ExperimentalMaterial3Api   // <-- added
import java.text.SimpleDateFormat                          // <-- added
import java.util.Date                                      // <-- added
import java.util.Locale                                    // <-- added

enum class SortBy { Relevance, Price, Rating, Name }

@OptIn(ExperimentalMaterial3Api::class)                 // <-- added
@Composable
fun StudentHomeScreen(
    openProfile: (String) -> Unit,
    openAbout: () -> Unit,
    appVm: AppViewModel
) {
    var tab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Student") },
                actions = {
                    IconButton(onClick = openAbout) {
                        Icon(Icons.Filled.Info, contentDescription = null)
                    }
                }
            )
        }
    ) { pad ->
        Column(Modifier.padding(pad)) {
            TabRow(selectedTabIndex = tab) {
                listOf("Search", "Bookings", "Resources", "Profile").forEachIndexed { i, label ->
                    Tab(selected = tab == i, onClick = { tab = i }, text = { Text(label) })
                }
            }
            when (tab) {
                0 -> SearchTab(openProfile, appVm)
                1 -> BookingsTab(appVm)
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

    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Search name / address / city / language") },
            modifier = Modifier.fillMaxWidth()
        )

        // Filter and sort controls
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = verifiedOnly,
                onClick = { verifiedOnly = !verifiedOnly },
                label = { Text(if (verifiedOnly) "Verified only ✓" else "Verified only") }
            )
            SortSelectorStable(sortBy) { sortBy = it } // ← stable dropdown (no Exposed API)
        }

        val filtered = instructors
            .asSequence()
            .filter { if (!verifiedOnly) true else it.verified }
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

@Composable
private fun BookingsTab(appVm: AppViewModel) {
    val bookings by appVm.bookings.collectAsState(initial = emptyList())

    // “My bookings” filter
    var myOnly by rememberSaveable { mutableStateOf(false) }
    var myEmail by rememberSaveable { mutableStateOf("") }

    // Date formatter (remember so it isn't recreated per item)
    val dateFmt = remember {
        SimpleDateFormat("EEE, MMM d, yyyy  h:mm a", Locale.getDefault())
    }

    // Confirm dialog state
    var toCancelId by remember { mutableStateOf<String?>(null) }

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
                label = { Text(if (myOnly) "My bookings ✓" else "My bookings") }
            )
            OutlinedTextField(
                value = myEmail,
                onValueChange = { myEmail = it },
                label = { Text("Your email (filter)") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }

        val list = bookings
            .asSequence()
            .filter { if (!myOnly || myEmail.isBlank()) true else it.studentEmail.equals(myEmail, ignoreCase = true) }
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
                    ElevatedCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            val whenText = dateFmt.format(Date(b.epochTime))

                            Text("Booking • ${b.studentName} → ${b.instructorId}")
                            Text("When: $whenText", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Email: ${b.studentEmail}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Status: ${b.status}", color = MaterialTheme.colorScheme.onSurfaceVariant)

                            Spacer(Modifier.height(8.dp))

                            // Allow cancel for REQUESTED / APPROVED
                            val canCancel = b.status == "REQUESTED" || b.status == "APPROVED"
                            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                TextButton(
                                    onClick = { toCancelId = b.id },
                                    enabled = canCancel
                                ) { Text("Cancel") }
                            }
                        }
                    }
                }
            }
        }
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
fun Placeholder(text: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text)
    }
}
