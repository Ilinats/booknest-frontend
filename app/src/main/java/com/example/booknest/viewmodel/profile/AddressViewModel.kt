package com.example.booknest.viewmodel.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booknest.domain.model.request.CreateAddressRequest
import com.example.booknest.domain.model.request.UpdateAddressRequest
import com.example.booknest.domain.model.response.ReaderAddressResponse
import com.example.booknest.domain.usecase.profile.AddAddressUseCase
import com.example.booknest.domain.usecase.profile.DeleteAddressUseCase
import com.example.booknest.domain.usecase.profile.GetMyAddressesUseCase
import com.example.booknest.domain.usecase.profile.UpdateAddressUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AddressViewModel(
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

    fun loadAddresses() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val result = getMyAddressesUseCase()
                result
                    .onSuccess { addresses -> _addresses.value = addresses }
                    .onFailure { e -> _error.value = e.message ?: "Failed to load addresses" }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
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
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val request = CreateAddressRequest(
                    streetAddress = streetAddress,
                    city = city,
                    postalCode = postalCode,
                    country = country,
                    isPrimary = isPrimary
                )
                val result = addAddressUseCase(request)
                result
                    .onSuccess {
                        loadAddresses()
                        _successMessage.value = "Address added successfully"
                    }
                    .onFailure { e -> _error.value = e.message ?: "Failed to add address" }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
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
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val request = UpdateAddressRequest(
                    streetAddress = streetAddress,
                    city = city,
                    postalCode = postalCode,
                    country = country,
                    isPrimary = isPrimary
                )
                val result = updateAddressUseCase(addressId, request)
                result
                    .onSuccess {
                        loadAddresses()
                        _successMessage.value = "Address updated successfully"
                    }
                    .onFailure { e -> _error.value = e.message ?: "Failed to update address" }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
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
                        _successMessage.value = "Address deleted successfully"
                    }
                    .onFailure { e -> _error.value = e.message ?: "Failed to delete address" }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
