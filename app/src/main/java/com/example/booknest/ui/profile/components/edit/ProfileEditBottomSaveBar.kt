package com.example.booknest.ui.profile.components.edit

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.booknest.presentation.common.UiState

@Composable
fun ProfileEditBottomSaveBar(
    enabled: Boolean,
    editState: UiState<*>,
    onSaveClick: () -> Unit,
) {
    Button(
        onClick = onSaveClick,
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled
    ) {
        if (editState is UiState.Loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text("Save Changes")
    }
}
