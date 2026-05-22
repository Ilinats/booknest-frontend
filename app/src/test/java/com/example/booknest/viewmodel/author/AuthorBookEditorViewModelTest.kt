package com.example.booknest.viewmodel.author

import android.net.Uri
import com.example.booknest.domain.usecase.author.CreateBookUseCase
import com.example.booknest.domain.usecase.author.DecodeBookLeakFingerprintUseCase
import com.example.booknest.domain.usecase.author.UpdateBookUseCase
import com.example.booknest.domain.usecase.files.RemoveBookCoverImageUseCase
import com.example.booknest.domain.usecase.files.UploadBookCoverImageUseCase
import com.example.booknest.domain.usecase.files.UploadBookFileUseCase
import com.example.booknest.port.ToastNotifier
import com.example.booknest.presentation.common.UiState
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
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthorBookEditorViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private val createBookUseCase = mockk<CreateBookUseCase>()
    private val updateBookUseCase = mockk<UpdateBookUseCase>()
    private val uploadBookFileUseCase = mockk<UploadBookFileUseCase>()
    private val uploadBookCoverImageUseCase = mockk<UploadBookCoverImageUseCase>()
    private val removeBookCoverImageUseCase = mockk<RemoveBookCoverImageUseCase>()
    private val decodeBookLeakFingerprintUseCase = mockk<DecodeBookLeakFingerprintUseCase>()
    private val catalogRefresher = AuthorBooksCatalogRefresher()
    private val feedback = UserFeedback(mockk<ToastNotifier>(relaxed = true))

    private fun createViewModel() = AuthorBookEditorViewModel(
        feedback = feedback,
        createBookUseCase = createBookUseCase,
        updateBookUseCase = updateBookUseCase,
        uploadBookFileUseCase = uploadBookFileUseCase,
        uploadBookCoverImageUseCase = uploadBookCoverImageUseCase,
        removeBookCoverImageUseCase = removeBookCoverImageUseCase,
        decodeBookLeakFingerprintUseCase = decodeBookLeakFingerprintUseCase,
        catalogRefresher = catalogRefresher,
    )

    @Test
    fun updateBook_successShowsMessage() = runTest(testDispatcher) {
        coEvery { updateBookUseCase("book-1", any()) } returns Result.success(TestFixtures.bookDetails())

        val viewModel = createViewModel()
        viewModel.updateBook("book-1", com.example.booknest.domain.model.request.UpdateBookRequest(title = "Updated"))
        advanceUntilIdle()

        assertEquals("Book updated successfully!", viewModel.successMessage.value)
        coVerify { updateBookUseCase("book-1", any()) }
    }

    @Test
    fun createBook_withoutContextWhenFileProvided_setsError() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        val fileUri = mockk<Uri>()
        viewModel.createBook(TestFixtures.createBookRequest(), fileUri = fileUri, context = null)
        advanceUntilIdle()

        val state = viewModel.bookCreationState.value
        assertTrue(state is UiState.Error)
        assertEquals("Context required for file upload", (state as UiState.Error).message)
    }

    @Test
    fun createBook_withoutFile_succeeds() = runTest(testDispatcher) {
        val created = TestFixtures.bookDetails(id = "new-book")
        coEvery { createBookUseCase(any(), any()) } returns Result.success(created)

        val viewModel = createViewModel()
        viewModel.createBook(TestFixtures.createBookRequest())
        advanceUntilIdle()

        val state = viewModel.bookCreationState.value
        assertTrue(state is UiState.Success)
        assertEquals(created, (state as UiState.Success).data)
    }

    @Test
    fun clearBookCreationState_resetsToIdle() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        viewModel.clearBookCreationState()
        assertTrue(viewModel.bookCreationState.value is UiState.Idle)
        assertTrue(viewModel.bookFileUploadState.value is UiState.Idle)
    }
}
