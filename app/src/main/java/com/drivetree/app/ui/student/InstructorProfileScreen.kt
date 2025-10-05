package com.drivetree.app.ui.student

import androidx.compose.foundation.layout.*
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.drivetree.app.data.Fixtures

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstructorProfileScreen(id: String, onBook: () -> Unit) {
    val r = Fixtures.instructors.firstOrNull { it.id == id } ?: return

    Scaffold(topBar = { TopAppBar(title = { Text(r.name) }) }) { pad ->
        Column(
            Modifier.padding(pad).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(r.address) // show address
            Text("${r.city} • $${r.pricePerHour}/hr • ★${r.rating}")
            Text("Car: ${r.carType}")
            Text("Languages: ${r.languages.joinToString("/")}")
            if (r.verified) AssistChip(onClick = {}, label = { Text("Verified") })
            Spacer(Modifier.height(8.dp))
            Button(onClick = onBook, modifier = Modifier.fillMaxWidth()) { Text("Book") }
        }
    }
}
