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
import com.example.booknest.domain.validation.AddressFormRules

@Composable
fun AddEditAddressDialog(
    address: ReaderAddressResponse?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, Boolean) -> Unit
) {
    val isNewAddress = address == null
    var streetAddress by remember {
        mutableStateOf(address?.streetAddress?.trim().orEmpty())
    }
    var city by remember { mutableStateOf(address?.city?.trim().orEmpty()) }
    var postalCode by remember { mutableStateOf(address?.postalCode?.trim().orEmpty()) }
    var country by remember {
        mutableStateOf(
            address?.country?.trim().orEmpty().ifBlank {
                if (isNewAddress) AddressFormRules.DEFAULT_COUNTRY else ""
            }
        )
    }
    var isPrimary by remember { mutableStateOf(address?.isPrimary ?: false) }
    var showErrors by remember { mutableStateOf(false) }

    val formValid = AddressFormRules.isFormValid(streetAddress, city, postalCode, country)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isNewAddress) "Add Address" else "Edit Address") },
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = streetAddress,
                    onValueChange = {
                        if (it.length <= AddressFormRules.STREET_MAX) streetAddress = it
                    },
                    label = { Text("Street Address *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = showErrors && AddressFormRules.streetError(streetAddress) != null,
                    supportingText = {
                        AddressFormRules.streetError(streetAddress)?.let { Text(it) }
                    }
                )
                OutlinedTextField(
                    value = city,
                    onValueChange = {
                        if (it.length <= AddressFormRules.CITY_MAX) city = it
                    },
                    label = { Text("City *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = showErrors && AddressFormRules.cityError(city) != null,
                    supportingText = {
                        AddressFormRules.cityError(city)?.let { Text(it) }
                    }
                )
                OutlinedTextField(
                    value = postalCode,
                    onValueChange = {
                        if (it.length <= AddressFormRules.POSTAL_MAX) postalCode = it
                    },
                    label = { Text("Postal Code *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = showErrors && AddressFormRules.postalError(postalCode) != null,
                    supportingText = {
                        AddressFormRules.postalError(postalCode)?.let { Text(it) }
                    }
                )
                OutlinedTextField(
                    value = country,
                    onValueChange = {
                        if (it.length <= AddressFormRules.COUNTRY_MAX) country = it
                    },
                    label = { Text("Country *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = showErrors && AddressFormRules.countryError(country) != null,
                    supportingText = {
                        val error = AddressFormRules.countryError(country)
                        when {
                            error != null -> Text(error)
                            isNewAddress && country == AddressFormRules.DEFAULT_COUNTRY ->
                                Text("Defaults to ${AddressFormRules.DEFAULT_COUNTRY} if unchanged")
                        }
                    }
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
                    if (formValid) {
                        onSave(
                            streetAddress.trim(),
                            city.trim(),
                            postalCode.trim(),
                            country.trim(),
                            isPrimary
                        )
                    } else {
                        showErrors = true
                    }
                },
                enabled = formValid
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
