package com.drivetree.app.ui.misc

import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.drivetree.app.R

//AboutScreen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onClose: () -> Unit) {
    val ctx = LocalContext.current

    // Edit here if you need to change names/emails
    val team = listOf(
        "Heemal Syangbo"     to "heemal.syangbo@georgebrown.ca",
        "Anudhin Thomas"     to "anudhin.thomas@georgebrown.ca",
        "Jeffin Yohannan"    to "jeffin.yohannan@georgebrown.ca",
        "Nashiruddin Feroz"  to "nashiruddin.feroz@georgebrown.ca"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { pad ->
        LazyColumn(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                // Header with logo, app name, tagline, version chip
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(R.drawable.dt_logo),
                        contentDescription = "DriveTree logo",
                        modifier = Modifier.size(120.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("DriveTree", style = MaterialTheme.typography.headlineSmall)
                    Text("Book · Learn · Go",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(6.dp))
                    AssistChip(onClick = {}, label = { Text(stringResource(R.string.app_version)) })
                }
            }

            item { HorizontalDivider() }

            item {
                Text("What this app does", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text("• Find verified driving instructors near you")
                Text("• Compare by price, rating, and language")
                Text("• Request lesson bookings (prototype flow)")
                Text("• Post-lesson reviews (coming after Prototype-1)")
            }

            item { HorizontalDivider() }

            item { Text("Team G-7", style = MaterialTheme.typography.titleMedium) }

            items(team) { (name, email) ->
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(name, style = MaterialTheme.typography.titleMedium)
                            Text(email, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO, "mailto:$email".toUri())
                            try {
                                ctx.startActivity(intent)
                            } catch (_: ActivityNotFoundException) {
                                Toast.makeText(ctx, "No email app found", Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Icon(Icons.Filled.Email, contentDescription = "Email $name")
                        }
                    }
                }
            }
        }
    }
}
