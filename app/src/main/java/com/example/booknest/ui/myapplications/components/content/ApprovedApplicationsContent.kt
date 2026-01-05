package com.example.booknest.ui.myapplications.components.content

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.booknest.domain.model.response.ApplicationResponse
import com.example.booknest.ui.myapplications.components.cards.ApprovedApplicationCard
import com.example.booknest.ui.myapplications.components.info.ApprovedTabInfoCard
import com.example.booknest.viewmodel.ApplicationViewModel
import com.example.booknest.viewmodel.FileViewModel

fun LazyListScope.ApprovedApplicationsContent(
    awaitingCopy: List<ApplicationResponse>,
    reading: List<ApplicationResponse>,
    applicationViewModel: ApplicationViewModel,
    fileViewModel: FileViewModel,
    navController: NavController
) {
    item {
        ApprovedTabInfoCard()
    }

    if (awaitingCopy.isNotEmpty()) {
        item {
            Text(
                text = "Awaiting Copy (${awaitingCopy.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        items(awaitingCopy) { application ->
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                ApprovedApplicationCard(
                    application = application,
                    applicationViewModel = applicationViewModel,
                    fileViewModel = fileViewModel,
                    navController = navController
                )
            }
        }
    }

    if (reading.isNotEmpty()) {
        item {
            Text(
                text = "Reading (${reading.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        items(reading) { application ->
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                ApprovedApplicationCard(
                    application = application,
                    applicationViewModel = applicationViewModel,
                    fileViewModel = fileViewModel,
                    navController = navController
                )
            }
        }
    }
}

