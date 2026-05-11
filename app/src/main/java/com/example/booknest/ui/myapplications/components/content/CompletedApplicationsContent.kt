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
import com.example.booknest.ui.myapplications.components.cards.CompletedApplicationCard
import com.example.booknest.viewmodel.files.FileViewModel

fun LazyListScope.CompletedApplicationsContent(
    applications: List<ApplicationResponse>,
    navController: NavController,
    fileViewModel: FileViewModel
) {
    items(applications) { application ->
        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            CompletedApplicationCard(
                application = application,
                navController = navController,
                fileViewModel = fileViewModel
            )
        }
    }
}

