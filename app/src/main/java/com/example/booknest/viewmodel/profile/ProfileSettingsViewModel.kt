package com.example.booknest.viewmodel.profile
import com.example.booknest.viewmodel.common.BaseViewModel

import com.example.booknest.domain.model.request.CustomSocialLink
import com.example.booknest.domain.model.enums.NotificationType
import com.example.booknest.domain.model.request.UpdateNotificationSettingsRequest
import com.example.booknest.domain.model.request.UpdatePrivacyRequest
import com.example.booknest.domain.model.request.UpdateSocialMediaRequest
import com.example.booknest.domain.model.response.SocialMediaResponse
import com.example.booknest.domain.usecase.profile.UpdateNotificationSettingsUseCase
import com.example.booknest.domain.usecase.profile.UpdatePrivacySettingsUseCase
import com.example.booknest.domain.usecase.profile.UpdateSocialMediaUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProfileSettingsViewModel(
    private val updateSocialMediaUseCase: UpdateSocialMediaUseCase,
    private val updatePrivacySettingsUseCase: UpdatePrivacySettingsUseCase,
    private val updateNotificationSettingsUseCase: UpdateNotificationSettingsUseCase
) : BaseViewModel() {

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    fun clearSuccessMessage() { _successMessage.value = null }

    fun updateSocialMedia(socialMedia: SocialMediaResponse) {
        val customLinks = socialMedia.custom?.map {
            CustomSocialLink(platform = it.platform, url = it.url)
        }
        val request = UpdateSocialMediaRequest(
            instagram = socialMedia.instagram,
            tiktok = socialMedia.tiktok,
            youtube = socialMedia.youtube,
            goodreads = socialMedia.goodreads,
            custom = customLinks
        )
        launchWithLoading<Unit>(
            onSuccess = { _successMessage.value = "Social media updated successfully" },
            block = { updateSocialMediaUseCase(request).map {} }
        )
    }

    fun updatePrivacySettings(
        activityPrivacy: String? = null,
        profilePrivacy: String? = null,
        readingListPrivacy: String? = null,
        reviewsPrivacy: String? = null
    ) {
        val request = UpdatePrivacyRequest(
            activityPrivacy = activityPrivacy,
            profilePrivacy = profilePrivacy,
            readingListPrivacy = readingListPrivacy,
            reviewsPrivacy = reviewsPrivacy
        )
        launchWithLoading<Unit>(
            onSuccess = { _successMessage.value = "Privacy settings updated" },
            block = { updatePrivacySettingsUseCase(request).map {} }
        )
    }

    fun updateNotificationSettings(
        notificationsEnabled: Boolean? = null,
        emailNotifications: Boolean? = null,
        notificationPreferences: List<String>? = null
    ) {
        val request = UpdateNotificationSettingsRequest(
            notificationsEnabled = notificationsEnabled,
            emailNotifications = emailNotifications,
            notificationPreferences = notificationPreferences
        )
        launchWithLoading<Unit>(
            onSuccess = { _successMessage.value = "Notification settings updated" },
            block = { updateNotificationSettingsUseCase(request).map {} }
        )
    }

    fun convertNotificationTypesToBooleans(types: List<String>?): Map<String, Boolean> {
        if (types == null) {
            return mapOf(
                NotificationType.FRIEND_REQUEST_RECEIVED to true,
                NotificationType.FRIEND_REQUEST_ACCEPTED to true,
                NotificationType.FRIEND_REQUEST_DECLINED to true,
                NotificationType.APPLICATION_APPROVED to true,
                NotificationType.APPLICATION_REJECTED to true,
                NotificationType.REVIEW_DEADLINE_REMINDER to true,
                NotificationType.AUTHOR_BOOK_PUBLISHED to true
            )
        }
        return mapOf(
            NotificationType.FRIEND_REQUEST_RECEIVED to types.contains(NotificationType.FRIEND_REQUEST_RECEIVED),
            NotificationType.FRIEND_REQUEST_ACCEPTED to types.contains(NotificationType.FRIEND_REQUEST_ACCEPTED),
            NotificationType.FRIEND_REQUEST_DECLINED to types.contains(NotificationType.FRIEND_REQUEST_DECLINED),
            NotificationType.APPLICATION_APPROVED to types.contains(NotificationType.APPLICATION_APPROVED),
            NotificationType.APPLICATION_REJECTED to types.contains(NotificationType.APPLICATION_REJECTED),
            NotificationType.REVIEW_DEADLINE_REMINDER to types.contains(NotificationType.REVIEW_DEADLINE_REMINDER),
            NotificationType.AUTHOR_BOOK_PUBLISHED to types.contains(NotificationType.AUTHOR_BOOK_PUBLISHED)
        )
    }

    fun convertBooleansToNotificationTypes(prefs: Map<String, Boolean>): List<String> {
        return prefs.filter { it.value }.keys.toList()
    }
}
