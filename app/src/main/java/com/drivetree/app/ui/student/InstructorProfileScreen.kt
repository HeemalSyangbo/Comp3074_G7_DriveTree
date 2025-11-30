package com.drivetree.app.ui.student

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
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
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header card with name and rating
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            instructor.name,
                            style = MaterialTheme.typography.headlineSmall
                        )
                        if (instructor.verified) {
                            AssistChip(
                                onClick = {},
                                label = { Text("Verified ✓") }
                            )
                        }
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "★ ${"%.1f".format(instructor.rating)}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "${(instructor.rating * 10).toInt()} reviews",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            "$${instructor.pricePerHour}/hr",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }

            // Details card
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Details", style = MaterialTheme.typography.titleMedium)
                    
                    // Experience (simulated based on rating)
                    val experienceYears = if (instructor.rating >= 4.5) "5+ years" else if (instructor.rating >= 4.0) "3+ years" else "1+ years"
                    ProfileInfoRow("Experience", "$experienceYears, ex-MTO examiner")
                    ProfileInfoRow("Location", "${instructor.address}, ${instructor.city}")
                    ProfileInfoRow("Areas Covered", instructor.city)
                    ProfileInfoRow("Car Type", instructor.carType)
                    ProfileInfoRow("Transmission", if (instructor.carType.lowercase().contains("automatic")) "Automatic" else "Manual")
                    ProfileInfoRow("Languages", instructor.languages.joinToString(", "))
                }
            }

            Spacer(Modifier.weight(1f))

            // Book button
            Button(
                onClick = onBook,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("Book a Lesson", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun ProfileInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
    }
}


