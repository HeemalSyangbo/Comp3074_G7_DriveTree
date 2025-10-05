package com.drivetree.app.ui.instructor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstructorHomeScreen(openAbout: () -> Unit) {
    var tab by remember { mutableStateOf(0) }
    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Instructor") },
            actions = { IconButton(onClick = openAbout) { Icon(Icons.Filled.Info, contentDescription = null) } }
        )
    }) { pad ->
        Column(Modifier.padding(pad)) {
            TabRow(selectedTabIndex = tab) {
                listOf("Verification","Availability","Bookings","Profile").forEachIndexed { i, t ->
                    Tab(selected = tab==i, onClick = { tab = i }, text = { Text(t) })
                }
            }
            when (tab) {
                0 -> Text("Upload documents (UI only)")
                1 -> Text("Weekly availability (placeholder)")
                2 -> Text("Bookings list (placeholder)")
                else -> Text("Profile (placeholder)")
            }
        }
    }
}
