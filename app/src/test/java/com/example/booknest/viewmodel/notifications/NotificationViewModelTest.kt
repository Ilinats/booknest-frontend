package com.example.booknest.viewmodel.notifications

import com.example.booknest.domain.model.enums.NotificationType
import com.example.booknest.domain.usecase.friends.AcceptFriendRequestUseCase
import com.example.booknest.domain.usecase.friends.DeclineFriendRequestUseCase
import com.example.booknest.domain.usecase.notifications.DeleteAllNotificationsUseCase
import com.example.booknest.domain.usecase.notifications.DeleteNotificationUseCase
import com.example.booknest.domain.usecase.notifications.GetNotificationsUseCase
import com.example.booknest.domain.usecase.notifications.GetUnreadCountUseCase
import com.example.booknest.domain.usecase.notifications.MarkAllNotificationsAsReadUseCase
import com.example.booknest.domain.usecase.notifications.MarkNotificationAsReadUseCase
import com.example.booknest.domain.usecase.notifications.RegisterDeviceTokenUseCase
import com.example.booknest.port.ToastNotifier
import com.example.booknest.testutil.MainDispatcherRule
import com.example.booknest.testutil.TestFixtures
import com.example.booknest.testutil.mockLoggedInSessionManager
import com.example.booknest.testutil.mockLoggedOutSessionManager
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
class NotificationViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private val getNotificationsUseCase = mockk<GetNotificationsUseCase>()
    private val getUnreadCountUseCase = mockk<GetUnreadCountUseCase>()
    private val markNotificationAsReadUseCase = mockk<MarkNotificationAsReadUseCase>()
    private val markAllNotificationsAsReadUseCase = mockk<MarkAllNotificationsAsReadUseCase>()
    private val deleteNotificationUseCase = mockk<DeleteNotificationUseCase>()
    private val deleteAllNotificationsUseCase = mockk<DeleteAllNotificationsUseCase>()
    private val registerDeviceTokenUseCase = mockk<RegisterDeviceTokenUseCase>(relaxed = true)
    private val acceptFriendRequestUseCase = mockk<AcceptFriendRequestUseCase>()
    private val declineFriendRequestUseCase = mockk<DeclineFriendRequestUseCase>()
    private val feedback = UserFeedback(mockk<ToastNotifier>(relaxed = true))

    private fun createViewModel(sessionManager: com.example.booknest.data.session.SessionManager) =
        NotificationViewModel(
            feedback = feedback,
            getNotificationsUseCase = getNotificationsUseCase,
            getUnreadCountUseCase = getUnreadCountUseCase,
            markNotificationAsReadUseCase = markNotificationAsReadUseCase,
            markAllNotificationsAsReadUseCase = markAllNotificationsAsReadUseCase,
            deleteNotificationUseCase = deleteNotificationUseCase,
            deleteAllNotificationsUseCase = deleteAllNotificationsUseCase,
            registerDeviceTokenUseCase = registerDeviceTokenUseCase,
            acceptFriendRequestUseCase = acceptFriendRequestUseCase,
            declineFriendRequestUseCase = declineFriendRequestUseCase,
            sessionManager = sessionManager,
        )

    @Test
    fun loadNotifications_whenLoggedOut_doesNothing() = runTest(testDispatcher) {
        val viewModel = createViewModel(mockLoggedOutSessionManager())

        viewModel.loadNotifications(refresh = true)
        advanceUntilIdle()

        assertTrue(viewModel.notifications.value.isEmpty())
        coVerify(exactly = 0) { getNotificationsUseCase(any(), any(), any()) }
    }

    @Test
    fun loadNotifications_populatesListAndUnreadCount() = runTest(testDispatcher) {
        val notifications = listOf(
            TestFixtures.notification(id = "n-1"),
            TestFixtures.notification(id = "n-2", isRead = true),
        )
        coEvery { getNotificationsUseCase(unreadOnly = null, skip = 0, take = 100) } returns
            Result.success(TestFixtures.notificationsList(notifications))
        coEvery { getUnreadCountUseCase() } returns Result.success(TestFixtures.unreadCount(1))

        val viewModel = createViewModel(mockLoggedInSessionManager())
        viewModel.loadNotifications(refresh = true)
        advanceUntilIdle()

        assertEquals(2, viewModel.notifications.value.size)
        assertEquals(1, viewModel.unreadCount.value)
    }

    @Test
    fun markAsRead_optimisticallyUpdatesUnreadCount() = runTest(testDispatcher) {
        val unread = TestFixtures.notification(id = "n-1", isRead = false)
        coEvery { getNotificationsUseCase(any(), any(), any()) } returns
            Result.success(TestFixtures.notificationsList(listOf(unread)))
        coEvery { getUnreadCountUseCase() } returns Result.success(TestFixtures.unreadCount(1))
        coEvery { markNotificationAsReadUseCase("n-1") } returns Result.success(
            unread.copy(isRead = true),
        )

        val viewModel = createViewModel(mockLoggedInSessionManager())
        viewModel.loadNotifications(refresh = true)
        advanceUntilIdle()
        viewModel.markAsRead("n-1")
        advanceUntilIdle()

        assertTrue(viewModel.notifications.value.first { it.id == "n-1" }.isRead)
        coVerify { markNotificationAsReadUseCase("n-1") }
    }

    @Test
    fun markAllAsRead_clearsUnreadCount() = runTest(testDispatcher) {
        coEvery { markAllNotificationsAsReadUseCase() } returns Result.success(Unit)

        val viewModel = createViewModel(mockLoggedInSessionManager())
        viewModel.loadNotifications(refresh = true)
        advanceUntilIdle()
        viewModel.markAllAsRead()
        advanceUntilIdle()

        assertEquals(0, viewModel.unreadCount.value)
        assertTrue(viewModel.notifications.value.all { it.isRead })
    }

    @Test
    fun deleteNotification_removesFromList() = runTest(testDispatcher) {
        val notification = TestFixtures.notification(id = "n-1")
        coEvery { deleteNotificationUseCase("n-1") } returns Result.success(Unit)
        coEvery { getUnreadCountUseCase() } returns Result.success(TestFixtures.unreadCount(0))

        val viewModel = createViewModel(mockLoggedInSessionManager())
        coEvery { getNotificationsUseCase(any(), any(), any()) } returns
            Result.success(TestFixtures.notificationsList(listOf(notification)))
        viewModel.loadNotifications(refresh = true)
        advanceUntilIdle()

        viewModel.deleteNotification("n-1")
        advanceUntilIdle()

        assertTrue(viewModel.notifications.value.isEmpty())
    }
}
