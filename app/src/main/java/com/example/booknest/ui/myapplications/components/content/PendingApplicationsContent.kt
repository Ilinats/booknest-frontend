package com.example.booknest.ui.myapplications.components.content

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.booknest.domain.model.response.ApplicationResponse
import com.example.booknest.ui.myapplications.components.cards.PendingApplicationCard
import com.example.booknest.viewmodel.applications.ApplicationViewModel

fun LazyListScope.PendingApplicationsContent(
    applications: List<ApplicationResponse>,
    applicationViewModel: ApplicationViewModel,
    navController: NavController
) {
    items(applications) { application ->
        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            PendingApplicationCard(
                application = application,
                applicationViewModel = applicationViewModel,
                navController = navController
            )
        }
    }
}

