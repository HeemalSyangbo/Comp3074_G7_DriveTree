package com.drivetree.app.ui.student

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingRequestScreen(onClose: () -> Unit) {
    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Request Lesson") },
            navigationIcon = { IconButton(onClick = onClose) { Icon(Icons.Filled.ArrowBack, contentDescription = null) } }
        )
    }) { pad ->
        Column(Modifier.padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Select a slot (placeholder)")
            Button(onClick = onClose, modifier = Modifier.fillMaxWidth()) { Text("Submit (dummy)") }
        }
    }
}
