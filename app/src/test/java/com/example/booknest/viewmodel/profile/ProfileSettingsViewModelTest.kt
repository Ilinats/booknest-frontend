package com.example.booknest.viewmodel.profile

import com.example.booknest.domain.model.enums.NotificationType
import com.example.booknest.domain.model.request.UpdateNotificationSettingsRequest
import com.example.booknest.domain.model.request.UpdatePrivacyRequest
import com.example.booknest.domain.model.request.UpdateSocialMediaRequest
import com.example.booknest.domain.model.response.CustomSocialLinkResponse
import com.example.booknest.domain.model.response.SocialMediaResponse
import com.example.booknest.domain.usecase.profile.UpdateNotificationSettingsUseCase
import com.example.booknest.domain.usecase.profile.UpdatePrivacySettingsUseCase
import com.example.booknest.domain.usecase.profile.UpdateSocialMediaUseCase
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
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileSettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private val updateSocialMediaUseCase = mockk<UpdateSocialMediaUseCase>()
    private val updatePrivacySettingsUseCase = mockk<UpdatePrivacySettingsUseCase>()
    private val updateNotificationSettingsUseCase = mockk<UpdateNotificationSettingsUseCase>()
    private val feedback = UserFeedback(mockk<ToastNotifier>(relaxed = true))

    private fun createViewModel() = ProfileSettingsViewModel(
        feedback = feedback,
        updateSocialMediaUseCase = updateSocialMediaUseCase,
        updatePrivacySettingsUseCase = updatePrivacySettingsUseCase,
        updateNotificationSettingsUseCase = updateNotificationSettingsUseCase,
    )

    @Test
    fun convertNotificationTypesToBooleans_nullEnablesAll() {
        val viewModel = createViewModel()
        val prefs = viewModel.convertNotificationTypesToBooleans(null)

        assertEquals(true, prefs[NotificationType.FRIEND_REQUEST_RECEIVED])
        assertEquals(true, prefs[NotificationType.APPLICATION_APPROVED])
    }

    @Test
    fun convertBooleansToNotificationTypes_returnsEnabledKeys() {
        val viewModel = createViewModel()
        val types = viewModel.convertBooleansToNotificationTypes(
            mapOf(
                NotificationType.FRIEND_REQUEST_RECEIVED to true,
                NotificationType.APPLICATION_REJECTED to false,
            ),
        )

        assertEquals(listOf(NotificationType.FRIEND_REQUEST_RECEIVED), types)
    }

    @Test
    fun updateSocialMedia_delegatesToUseCase() = runTest(testDispatcher) {
        val social = SocialMediaResponse(
            instagram = "@book",
            tiktok = null,
            youtube = null,
            goodreads = null,
            custom = listOf(CustomSocialLinkResponse(platform = "blog", url = "https://x.test")),
        )
        coEvery { updateSocialMediaUseCase(any()) } returns Result.success(TestFixtures.userProfile())

        val viewModel = createViewModel()
        viewModel.updateSocialMedia(social)
        advanceUntilIdle()

        coVerify {
            updateSocialMediaUseCase(
                UpdateSocialMediaRequest(
                    instagram = "@book",
                    tiktok = null,
                    youtube = null,
                    goodreads = null,
                    custom = listOf(
                        com.example.booknest.domain.model.request.CustomSocialLink(
                            platform = "blog",
                            url = "https://x.test",
                        ),
                    ),
                ),
            )
        }
        assertEquals("Social media updated successfully", viewModel.successMessage.value)
    }

    @Test
    fun clearSuccessMessage_resetsState() {
        val viewModel = createViewModel()
        viewModel.clearSuccessMessage()
        assertNull(viewModel.successMessage.value)
    }

    @Test
    fun updatePrivacySettings_delegatesToUseCase() = runTest(testDispatcher) {
        coEvery { updatePrivacySettingsUseCase(any()) } returns Result.success(TestFixtures.userProfile())

        val viewModel = createViewModel()
        viewModel.updatePrivacySettings(profilePrivacy = "public")
        advanceUntilIdle()

        coVerify {
            updatePrivacySettingsUseCase(
                UpdatePrivacyRequest(
                    activityPrivacy = null,
                    profilePrivacy = "public",
                    readingListPrivacy = null,
                    reviewsPrivacy = null,
                ),
            )
        }
    }

    @Test
    fun updateNotificationSettings_delegatesToUseCase() = runTest(testDispatcher) {
        coEvery { updateNotificationSettingsUseCase(any()) } returns Result.success(TestFixtures.userProfile())

        val viewModel = createViewModel()
        viewModel.updateNotificationSettings(
            notificationsEnabled = true,
            emailNotifications = false,
            notificationPreferences = listOf(NotificationType.APPLICATION_APPROVED),
        )
        advanceUntilIdle()

        coVerify {
            updateNotificationSettingsUseCase(
                UpdateNotificationSettingsRequest(
                    notificationsEnabled = true,
                    emailNotifications = false,
                    notificationPreferences = listOf(NotificationType.APPLICATION_APPROVED),
                ),
            )
        }
    }
}
