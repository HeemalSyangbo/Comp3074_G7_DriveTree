package com.drivetree.app.ui.admin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class) // TabRow (M3) is opt-in on your version
@Composable
fun AdminHomeScreen(openAbout: () -> Unit) {
    var tab by remember { mutableStateOf(0) }
    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Admin") },
            actions = { IconButton(onClick = openAbout) { Icon(Icons.Filled.Info, contentDescription = null) } }
        )
    }) { pad ->
        Column(Modifier.padding(pad)) {
            TabRow(selectedTabIndex = tab) {
                listOf("Pending","All Instructors","Reports").forEachIndexed { i, t ->
                    Tab(selected = tab==i, onClick = { tab = i }, text = { Text(t) })
                }
            }
            when (tab) {
                0 -> Text("Pending applications (placeholder)")
                1 -> Text("All verified (placeholder)")
                else -> Text("Reports (placeholder)")
            }
        }
    }
}
