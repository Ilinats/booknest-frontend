package com.example.booknest.domain.usecase.applications

import com.example.booknest.domain.model.request.CreateApplicationRequest
import com.example.booknest.domain.repository.ApplicationsRepository
import com.example.booknest.testutil.TestFixtures
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ApplicationsUseCasesTest {

    private val repository = mockk<ApplicationsRepository>()

    @Test
    fun createApplicationUseCase_delegatesToRepository() = runTest {
        val request = CreateApplicationRequest(bookId = "book-1", applicationMessage = "Hi")
        val response = TestFixtures.application()
        coEvery { repository.createApplication(request) } returns Result.success(response)

        val result = CreateApplicationUseCase(repository)(request)

        assertTrue(result.isSuccess)
        assertEquals(response, result.getOrNull())
        coVerify { repository.createApplication(request) }
    }

    @Test
    fun checkApplicationUseCase_delegatesToRepository() = runTest {
        val check = TestFixtures.applicationCheck(hasApplied = true, applicationId = "app-1")
        coEvery { repository.checkApplication("book-1") } returns Result.success(check)

        val result = CheckApplicationUseCase(repository)("book-1")

        assertEquals(check, result.getOrNull())
    }

    @Test
    fun getMyApplicationsUseCase_delegatesToRepository() = runTest {
        val apps = listOf(TestFixtures.application())
        coEvery { repository.getMyApplications() } returns Result.success(apps)

        val result = GetMyApplicationsUseCase(repository)()

        assertEquals(apps, result.getOrNull())
    }

    @Test
    fun withdrawApplicationUseCase_delegatesToRepository() = runTest {
        coEvery { repository.withdrawApplication("app-1") } returns Result.success(Unit)

        val result = WithdrawApplicationUseCase(repository)("app-1")

        assertTrue(result.isSuccess)
    }
}
