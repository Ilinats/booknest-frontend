package com.example.booknest.domain.usecase.profile

import com.example.booknest.domain.repository.ProfileRepository
import com.example.booknest.testutil.TestFixtures
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ProfileUseCasesTest {

    private val repository = mockk<ProfileRepository>()

    @Test
    fun getMyProfileUseCase_delegatesToRepository() = runTest {
        val profile = TestFixtures.userProfile()
        coEvery { repository.getMyProfile() } returns Result.success(profile)

        assertEquals(profile, GetMyProfileUseCase(repository)().getOrNull())
        coVerify { repository.getMyProfile() }
    }

    @Test
    fun getCurrentUserUseCase_delegatesToRepository() = runTest {
        val user = TestFixtures.user()
        coEvery { repository.getMe() } returns Result.success(user)

        assertEquals(user, GetCurrentUserUseCase(repository)().getOrNull())
    }
}
