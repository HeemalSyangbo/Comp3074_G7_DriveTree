
package com.drivetree.app.ui.misc

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drivetree.app.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onDone: () -> Unit) {
    // keep splash visible briefly, then navigate to Auth
    LaunchedEffect(Unit) { delay(1400); onDone() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.dt_logo),
                contentDescription = "DriveTree logo",
                modifier = Modifier.size(220.dp)
            )

            Spacer(Modifier.height(16.dp))

            // Tagline: Book · Learn · Go (with a subtle green→blue gradient to match the swoosh)
            Text(
                text = "Book · Learn · Go",
                style = MaterialTheme.typography.titleLarge.copy(
                    brush = Brush.linearGradient(
                        listOf(Color(0xFF00C26E), Color(0xFF1EA5FF))
                    )
                ),
                letterSpacing = 0.5.sp
            )
        }
    }
}
