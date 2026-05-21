package com.example.booknest.viewmodel.profile

import androidx.lifecycle.ViewModel
import com.example.booknest.viewmodel.common.UserFeedback
import androidx.lifecycle.viewModelScope
import com.example.booknest.domain.model.request.CreateAddressRequest
import com.example.booknest.domain.model.request.UpdateAddressRequest
import com.example.booknest.domain.model.response.ReaderAddressResponse
import com.example.booknest.domain.usecase.profile.AddAddressUseCase
import com.example.booknest.domain.usecase.profile.DeleteAddressUseCase
import com.example.booknest.domain.usecase.profile.GetMyAddressesUseCase
import com.example.booknest.domain.usecase.profile.UpdateAddressUseCase
import com.example.booknest.domain.validation.AddressFormRules
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AddressViewModel(
    private val feedback: UserFeedback,
    private val getMyAddressesUseCase: GetMyAddressesUseCase,
    private val addAddressUseCase: AddAddressUseCase,
    private val updateAddressUseCase: UpdateAddressUseCase,
    private val deleteAddressUseCase: DeleteAddressUseCase
) : ViewModel() {

    private val _addresses = MutableStateFlow<List<ReaderAddressResponse>>(emptyList())
    val addresses: StateFlow<List<ReaderAddressResponse>> = _addresses.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    fun clearError() { _error.value = null }
    fun clearSuccessMessage() { _successMessage.value = null }

    private fun notifyError(message: String) = feedback.error(message, _error)
    private fun notifySuccess(message: String) = feedback.success(message, _successMessage)

    fun loadAddresses() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val result = getMyAddressesUseCase()
                result
                    .onSuccess { addresses -> _addresses.value = addresses }
                    .onFailure { e -> notifyError(e.message ?: "Failed to load addresses") }
            } catch (e: Exception) {
                notifyError(e.message ?: "Unknown error occurred")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addAddress(
        streetAddress: String,
        city: String,
        postalCode: String,
        country: String,
        isPrimary: Boolean
    ) {
        if (!AddressFormRules.isFormValid(streetAddress, city, postalCode, country)) {
            notifyError("Please fill in all required address fields.")
            return
        }
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val request = CreateAddressRequest(
                    streetAddress = streetAddress.trim(),
                    city = city.trim(),
                    postalCode = postalCode.trim(),
                    country = AddressFormRules.normalizeCountryForCreate(country),
                    isPrimary = isPrimary
                )
                val result = addAddressUseCase(request)
                result
                    .onSuccess {
                        loadAddresses()
                        notifySuccess("Address added successfully")
                    }
                    .onFailure { e -> notifyError(e.message ?: "Failed to add address") }
            } catch (e: Exception) {
                notifyError(e.message ?: "Unknown error occurred")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateAddress(
        addressId: String,
        streetAddress: String? = null,
        city: String? = null,
        postalCode: String? = null,
        country: String? = null,
        isPrimary: Boolean? = null
    ) {
        val trimmedStreet = streetAddress?.trim()
        val trimmedCity = city?.trim()
        val trimmedPostal = postalCode?.trim()
        val trimmedCountry = country?.trim()

        if ((trimmedStreet != null && !AddressFormRules.isStreetValid(trimmedStreet)) ||
            (trimmedCity != null && !AddressFormRules.isCityValid(trimmedCity)) ||
            (trimmedPostal != null && !AddressFormRules.isPostalValid(trimmedPostal)) ||
            (trimmedCountry != null && !AddressFormRules.isCountryValid(trimmedCountry))
        ) {
            notifyError("Please correct the address fields before saving.")
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val request = UpdateAddressRequest(
                    streetAddress = trimmedStreet,
                    city = trimmedCity,
                    postalCode = trimmedPostal,
                    country = trimmedCountry?.let { AddressFormRules.countryForUpdate(it) },
                    isPrimary = isPrimary
                )
                val result = updateAddressUseCase(addressId, request)
                result
                    .onSuccess {
                        loadAddresses()
                        notifySuccess("Address updated successfully")
                    }
                    .onFailure { e -> notifyError(e.message ?: "Failed to update address") }
            } catch (e: Exception) {
                notifyError(e.message ?: "Unknown error occurred")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteAddress(addressId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val result = deleteAddressUseCase(addressId)
                result
                    .onSuccess {
                        loadAddresses()
                        notifySuccess("Address deleted successfully")
                    }
                    .onFailure { e -> notifyError(e.message ?: "Failed to delete address") }
            } catch (e: Exception) {
                notifyError(e.message ?: "Unknown error occurred")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
