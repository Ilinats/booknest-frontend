package com.example.booknest.ui.account.components.privacy.address

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.booknest.domain.model.response.ReaderAddressResponse

@Composable
fun AddressManagementSection(
    addresses: List<ReaderAddressResponse>,
    onAddAddress: (String, String, String, String, Boolean) -> Unit,
    onUpdateAddress: (String, String?, String?, String?, String?, Boolean?) -> Unit,
    onDeleteAddress: (String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingAddress by remember {
        mutableStateOf<ReaderAddressResponse?>(null)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (addresses.isEmpty()) {
                Text(
                    text = "No addresses added yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                addresses.forEach { address ->
                    AddressCard(
                        address = address,
                        onEdit = { editingAddress = address },
                        onDelete = { onDeleteAddress(address.id) }
                    )
                }
            }

            OutlinedButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Address")
            }
        }
    }

    if (showAddDialog) {
        AddEditAddressDialog(
            address = null,
            onDismiss = { showAddDialog = false },
            onSave = { streetAddress: String, city: String, postalCode: String, country: String, isPrimary: Boolean ->
                onAddAddress(streetAddress, city, postalCode, country, isPrimary)
                showAddDialog = false
            }
        )
    }

    if (editingAddress != null) {
        val address = editingAddress!!
        AddEditAddressDialog(
            address = address,
            onDismiss = { editingAddress = null },
            onSave = { streetAddress: String, city: String, postalCode: String, country: String, isPrimary: Boolean ->
                onUpdateAddress(address.id, streetAddress, city, postalCode, country, isPrimary)
                editingAddress = null
            }
        )
    }
}

