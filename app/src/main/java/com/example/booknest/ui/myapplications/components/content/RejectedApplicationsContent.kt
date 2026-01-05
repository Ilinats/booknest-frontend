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
import com.example.booknest.ui.myapplications.components.cards.RejectedApplicationCard

fun LazyListScope.RejectedApplicationsContent(
    applications: List<ApplicationResponse>,
    navController: NavController
) {
    items(applications) { application ->
        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            RejectedApplicationCard(
                application = application,
                navController = navController
            )
        }
    }
}

