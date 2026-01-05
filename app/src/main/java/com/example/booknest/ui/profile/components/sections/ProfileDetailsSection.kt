package com.example.booknest.ui.profile.components.sections

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.booknest.domain.model.response.UserProfileResponse
import com.example.booknest.ui.profile.components.items.ProfileDetailItem
import com.example.booknest.ui.profile.utils.formatDate

@Composable
fun ProfileDetailsSection(
    profile: UserProfileResponse
) {
    val hasDetails = profile.birthDate != null
    if (!hasDetails) return

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Additional Information",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            profile.birthDate?.let { birthDate ->
                ProfileDetailItem(
                    label = "Birth Date",
                    value = formatDate(birthDate),
                    icon = Icons.Default.CalendarToday
                )
            }
        }
    }
}

