package com.example.booknest.domain.usecase.files

import com.example.booknest.domain.repository.BooksRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class FilesUseCasesTest {

    private val repository = mockk<BooksRepository>()

    @Test
    fun getBookDownloadUrlUseCase_delegatesToRepository() = runTest {
        coEvery { repository.getBookDownload("book-1") } returns Result.failure(Exception("missing"))

        val result = GetBookDownloadUrlUseCase(repository)("book-1")

        assertTrue(result.isFailure)
        coVerify { repository.getBookDownload("book-1") }
    }

    @Test
    fun uploadBookFileUseCase_delegatesToRepository() = runTest {
        coEvery { repository.uploadBookFile("book-1", any()) } returns Result.success(mockk(relaxed = true))

        UploadBookFileUseCase(repository)("book-1", mockk())

        coVerify { repository.uploadBookFile("book-1", any()) }
    }
}
