package com.example.booknest.domain.usecase.notifications

import com.example.booknest.domain.repository.NotificationsRepository
import com.example.booknest.testutil.TestFixtures
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class NotificationsUseCasesTest {

    private val repository = mockk<NotificationsRepository>()

    @Test
    fun getNotificationsUseCase_delegatesToRepository() = runTest {
        val list = TestFixtures.notificationsList(listOf(TestFixtures.notification()))
        coEvery { repository.getNotifications(unreadOnly = true, skip = 0, take = 20) } returns
            Result.success(list)

        val result = GetNotificationsUseCase(repository)(unreadOnly = true, skip = 0, take = 20)

        assertEquals(list, result.getOrNull())
    }

    @Test
    fun getUnreadCountUseCase_delegatesToRepository() = runTest {
        coEvery { repository.getUnreadCount() } returns Result.success(TestFixtures.unreadCount(5))

        assertEquals(5, GetUnreadCountUseCase(repository)().getOrNull()?.count)
    }

    @Test
    fun markNotificationAsReadUseCase_delegatesToRepository() = runTest {
        val notification = TestFixtures.notification(isRead = true)
        coEvery { repository.markNotificationAsRead("n-1") } returns Result.success(notification)

        assertEquals(notification, MarkNotificationAsReadUseCase(repository)("n-1").getOrNull())
        coVerify { repository.markNotificationAsRead("n-1") }
    }
}
