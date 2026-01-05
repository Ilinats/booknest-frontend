package com.example.booknest.ui.account.components.privacy.address

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.booknest.domain.model.response.ReaderAddressResponse

@Composable
fun AddEditAddressDialog(
    address: ReaderAddressResponse?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, Boolean) -> Unit
) {
    var streetAddress by remember { mutableStateOf(address?.streetAddress ?: "") }
    var city by remember { mutableStateOf(address?.city ?: "") }
    var postalCode by remember { mutableStateOf(address?.postalCode ?: "") }
    var country by remember { mutableStateOf(address?.country ?: "") }
    var isPrimary by remember { mutableStateOf(address?.isPrimary ?: false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (address == null) "Add Address" else "Edit Address") },
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = streetAddress,
                    onValueChange = { streetAddress = it },
                    label = { Text("Street Address *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("City *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = postalCode,
                    onValueChange = { postalCode = it },
                    label = { Text("Postal Code *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = country,
                    onValueChange = { country = it },
                    label = { Text("Country") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = isPrimary,
                        onCheckedChange = { isPrimary = it }
                    )
                    Text("Set as primary address")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (streetAddress.isNotBlank() && city.isNotBlank() && postalCode.isNotBlank()) {
                        onSave(streetAddress, city, postalCode, country, isPrimary)
                    }
                },
                enabled = streetAddress.isNotBlank() && city.isNotBlank() && postalCode.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

