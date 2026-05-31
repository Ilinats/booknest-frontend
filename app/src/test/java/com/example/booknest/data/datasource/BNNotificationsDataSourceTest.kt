package com.example.booknest.data.datasource

import com.example.booknest.data.error.BNError
import com.example.booknest.data.service.NotificationsService
import com.example.booknest.domain.model.request.RegisterDeviceTokenRequest
import com.example.booknest.testutil.DataSourceJsonFixtures
import com.example.booknest.testutil.MockWebServerDataSourceTest
import com.example.booknest.testutil.RetrofitTestSupport
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BNNotificationsDataSourceTest : MockWebServerDataSourceTest() {

    private val dataSource: BNNotificationsDataSource
        get() = BNNotificationsDataSource(RetrofitTestSupport.service<NotificationsService>(mockWebServer))

    @Test
    fun getUnreadCount_successReturnsCount() = runTest {
        enqueueJson(200, DataSourceJsonFixtures.UNREAD_COUNT)

        val result = dataSource.getUnreadCount()

        assertTrue(result.isSuccess)
        assertEquals(3, result.getOrNull()?.count)
        assertEquals("/api/notifications/unread-count", mockWebServer.takeRequest().path)
    }

    @Test
    fun getNotifications_successReturnsList() = runTest {
        enqueueJson(200, DataSourceJsonFixtures.notificationsList)

        val result = dataSource.getNotifications(unreadOnly = true, skip = 0, take = 20)

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.data?.size)
        assertEquals("notif-1", result.getOrNull()?.notifications?.first()?.id)
    }

    @Test
    fun markNotificationAsRead_successReturnsNotification() = runTest {
        enqueueJson(
            200,
            DataSourceJsonFixtures.notification.replace("\"isRead\": false", "\"isRead\": true"),
        )

        val result = dataSource.markNotificationAsRead("notif-1")

        assertTrue(result.isSuccess)
        assertEquals("notif-1", result.getOrNull()?.id)
        assertEquals("/api/notifications/notif-1/read", mockWebServer.takeRequest().path)
    }

    @Test
    fun markAllNotificationsAsRead_successReturnsUnit() = runTest {
        enqueueJson(200, """{"message":"ok"}""")

        val result = dataSource.markAllNotificationsAsRead()

        assertTrue(result.isSuccess)
        assertEquals("/api/notifications/read-all", mockWebServer.takeRequest().path)
    }

    @Test
    fun getUnreadCount_failureMapsBnError() = runTest {
        enqueueJson(500, DataSourceJsonFixtures.errorBody("Server error", 500))

        val result = dataSource.getUnreadCount()

        assertTrue(result.isFailure)
        assertEquals("Server error", (result.exceptionOrNull() as BNError.Generic).messageString)
    }
}
