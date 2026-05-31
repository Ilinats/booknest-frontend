package com.example.booknest.testutil

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before

abstract class MockWebServerDataSourceTest {

    protected lateinit var mockWebServer: MockWebServer

    @Before
    fun startMockWebServer() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
    }

    @After
    fun stopMockWebServer() {
        mockWebServer.shutdown()
    }

    protected fun enqueueJson(responseCode: Int, body: String) {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(responseCode)
                .setBody(body)
                .addHeader("Content-Type", "application/json"),
        )
    }
}
