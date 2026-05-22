package com.example.booknest.viewmodel.files

import android.content.Context
import com.example.booknest.data.error.BNError
import com.example.booknest.domain.usecase.files.GetBookDownloadUrlUseCase
import com.example.booknest.domain.usecase.files.UploadBookFileUseCase
import com.example.booknest.port.DownloadNotifier
import com.example.booknest.testutil.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class FileViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val context = mockk<Context>(relaxed = true)
    private val uploadBookFileUseCase = mockk<UploadBookFileUseCase>()
    private val getBookDownloadUrlUseCase = mockk<GetBookDownloadUrlUseCase>()
    private val downloadNotifier = mockk<DownloadNotifier>(relaxed = true)

    private fun createViewModel() = FileViewModel(
        context = context,
        uploadBookFileUseCase = uploadBookFileUseCase,
        getBookDownloadUrlUseCase = getBookDownloadUrlUseCase,
        downloadNotifier = downloadNotifier,
    )

    @Test
    fun uploadBookFile_missingFile_setsValidationError() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        val missing = File(tempFolder.root, "missing.pdf")

        viewModel.uploadBookFile("book-1", missing)
        advanceUntilIdle()

        assertEquals("File does not exist", viewModel.uiState.value.error)
        coVerify(exactly = 0) { uploadBookFileUseCase(any(), any()) }
    }

    @Test
    fun uploadBookFile_successUpdatesState() = runTest(testDispatcher) {
        val file = tempFolder.newFile("book.pdf")
        file.writeBytes(byteArrayOf(1, 2, 3))
        coEvery { uploadBookFileUseCase("book-1", any()) } returns Result.success(mockk(relaxed = true))

        val viewModel = createViewModel()
        viewModel.uploadBookFile("book-1", file)
        advanceUntilIdle()

        assertEquals("File uploaded successfully", viewModel.uiState.value.successMessage)
        assertNull(viewModel.uiState.value.error)
        coVerify { uploadBookFileUseCase("book-1", any()) }
    }

    @Test
    fun downloadBook_useCaseFailure_setsFriendlyError() = runTest(testDispatcher) {
        coEvery { getBookDownloadUrlUseCase("book-1") } returns Result.failure(
            BNError.Generic(
                messageString = "BOOK_FILE_NOT_AVAILABLE",
                error = null,
                statusCode = 404,
            ),
        )

        val viewModel = createViewModel()
        viewModel.downloadBook("book-1")
        advanceUntilIdle()

        assertEquals("BOOK_FILE_NOT_AVAILABLE", viewModel.uiState.value.error)
        verify { downloadNotifier.showDownloadError("BOOK_FILE_NOT_AVAILABLE") }
    }

    @Test
    fun clearError_clearsErrorState() {
        val viewModel = createViewModel()
        viewModel.clearError()
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun clearSuccessMessage_clearsMessage() {
        val viewModel = createViewModel()
        viewModel.clearSuccessMessage()
        assertNull(viewModel.uiState.value.successMessage)
    }
}
