package com.drivetree.app.ui.student

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.drivetree.app.data.Fixtures

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentHomeScreen(openProfile: (String) -> Unit, openAbout: () -> Unit) {
    var tab by remember { mutableStateOf(0) }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Student") },
            actions = { IconButton(onClick = openAbout) { Icon(Icons.Filled.Info, contentDescription = null) } }
        )
    }) { pad ->
        Column(Modifier.padding(pad)) {
            TabRow(selectedTabIndex = tab) {
                listOf("Search", "Bookings", "Resources", "Profile").forEachIndexed { i, label ->
                    Tab(selected = tab == i, onClick = { tab = i }, text = { Text(label) })
                }
            }
            when (tab) {
                0 -> SearchTab(openProfile)
                1 -> Placeholder("Bookings (dummy)")
                2 -> Placeholder("Resources (mock tests / tips)")
                else -> Placeholder("Profile (static)")
            }
        }
    }
}

@Composable
private fun SearchTab(openProfile: (String) -> Unit) {
    var query by remember { mutableStateOf("") }

    // Search by name OR address OR city OR languages
    val filtered = remember(query) {
        val q = query.trim()
        Fixtures.instructors.filter { ins ->
            if (q.isBlank()) true else {
                val haystack = listOf(
                    ins.name,
                    ins.address,
                    ins.city,
                    ins.languages.joinToString(" ")
                )
                haystack.any { it.contains(q, ignoreCase = true) }
            }
        }
    }

    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Search name / address / city / language") },
            modifier = Modifier.fillMaxWidth()
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(filtered) { r ->
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { openProfile(r.id) }
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(r.name, style = MaterialTheme.typography.titleMedium)
                        Text(r.address, color = MaterialTheme.colorScheme.onSurfaceVariant)  // show address
                        Text("${r.city} • $${r.pricePerHour}/hr • ★${r.rating}")
                        Text("Languages: ${r.languages.joinToString("/")}")
                        if (r.verified) AssistChip(onClick = {}, label = { Text("Verified") })
                    }
                }
            }
        }
    }
}

@Composable
fun Placeholder(text: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text)
    }
}
