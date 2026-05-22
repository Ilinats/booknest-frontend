package com.example.booknest.viewmodel.profile

import com.example.booknest.domain.usecase.profile.AddAddressUseCase
import com.example.booknest.domain.usecase.profile.DeleteAddressUseCase
import com.example.booknest.domain.usecase.profile.GetMyAddressesUseCase
import com.example.booknest.domain.usecase.profile.UpdateAddressUseCase
import com.example.booknest.port.ToastNotifier
import com.example.booknest.testutil.MainDispatcherRule
import com.example.booknest.testutil.TestFixtures
import com.example.booknest.viewmodel.common.UserFeedback
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddressViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private val getMyAddressesUseCase = mockk<GetMyAddressesUseCase>()
    private val addAddressUseCase = mockk<AddAddressUseCase>()
    private val updateAddressUseCase = mockk<UpdateAddressUseCase>()
    private val deleteAddressUseCase = mockk<DeleteAddressUseCase>()
    private val feedback = UserFeedback(mockk<ToastNotifier>(relaxed = true))

    private fun createViewModel() = AddressViewModel(
        feedback = feedback,
        getMyAddressesUseCase = getMyAddressesUseCase,
        addAddressUseCase = addAddressUseCase,
        updateAddressUseCase = updateAddressUseCase,
        deleteAddressUseCase = deleteAddressUseCase,
    )

    @Test
    fun loadAddresses_populatesList() = runTest(testDispatcher) {
        val addresses = listOf(TestFixtures.address())
        coEvery { getMyAddressesUseCase() } returns Result.success(addresses)

        val viewModel = createViewModel()
        viewModel.loadAddresses()
        advanceUntilIdle()

        assertEquals(addresses, viewModel.addresses.value)
    }

    @Test
    fun addAddress_invalidForm_skipsUseCase() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        viewModel.addAddress("", "Sofia", "1000", "Bulgaria", isPrimary = true)
        advanceUntilIdle()

        assertEquals(
            "Please fill in all required address fields.",
            viewModel.error.value,
        )
        coVerify(exactly = 0) { addAddressUseCase(any()) }
    }

    @Test
    fun deleteAddress_reloadsList() = runTest(testDispatcher) {
        coEvery { deleteAddressUseCase("addr-1") } returns Result.success(Unit)
        coEvery { getMyAddressesUseCase() } returns Result.success(emptyList())

        val viewModel = createViewModel()
        viewModel.deleteAddress("addr-1")
        advanceUntilIdle()

        coVerify { deleteAddressUseCase("addr-1") }
        coVerify { getMyAddressesUseCase() }
    }
}
