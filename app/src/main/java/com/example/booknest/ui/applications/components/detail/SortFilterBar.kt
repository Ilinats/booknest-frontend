package com.example.booknest.ui.applications.components.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.booknest.ui.applications.models.SortOption

@Composable
fun SortFilterBar(
    sortOption: SortOption,
    onSortOptionChange: (SortOption) -> Unit,
    showSortMenu: Boolean,
    onShowSortMenuChange: (Boolean) -> Unit,
    onToggleSelectionMode: () -> Unit,
    isSelectionMode: Boolean,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            OutlinedButton(onClick = { onShowSortMenuChange(true) }) {
                Icon(Icons.Default.Sort, contentDescription = "Sort")
                Spacer(modifier = Modifier.width(4.dp))
                Text(sortOption.displayName)
            }

            DropdownMenu(
                expanded = showSortMenu,
                onDismissRequest = { onShowSortMenuChange(false) }
            ) {
                SortOption.values().forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.displayName) },
                        onClick = {
                            onSortOptionChange(option)
                            onShowSortMenuChange(false)
                        }
                    )
                }
            }
        }

        IconButton(
            onClick = onToggleSelectionMode,
            enabled = enabled
        ) {
            Icon(
                if (isSelectionMode) Icons.Default.CheckCircle else Icons.Default.CheckCircleOutline,
                contentDescription = "Select",
                tint = when {
                    !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                    isSelectionMode -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}
