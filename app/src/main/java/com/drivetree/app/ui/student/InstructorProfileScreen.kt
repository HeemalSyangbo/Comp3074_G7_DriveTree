package com.drivetree.app.ui.student

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.drivetree.app.data.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstructorProfileScreen(
    id: String,
    appVm: AppViewModel,
    onBook: () -> Unit
) {
    val instructor = appVm.instructors.collectAsState(initial = emptyList()).value
        .firstOrNull { it.id == id }

    Scaffold(
        topBar = { TopAppBar(title = { Text(instructor?.name ?: "Instructor Profile") }) }
    ) { pad ->
        if (instructor == null) {
            Box(
                Modifier
                    .padding(pad)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Instructor not found")
            }
            return@Scaffold
        }

        Column(
            Modifier
                .padding(pad)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(instructor.address, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${instructor.city} • $${instructor.pricePerHour}/hr • ★${"%.1f".format(instructor.rating)}")
            Text("Car Type: ${instructor.carType}")
            Text("Languages: ${instructor.languages.joinToString(", ")}")

            if (instructor.verified) {
                AssistChip(onClick = {}, label = { Text("Verified") })
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onBook,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Book a Lesson")
            }
        }
    }
}


