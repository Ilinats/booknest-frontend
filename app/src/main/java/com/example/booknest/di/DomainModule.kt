package com.example.booknest.di

import com.example.booknest.domain.usecase.analytics.GetAuthorAnalyticsUseCase
import com.example.booknest.domain.usecase.analytics.GetBookPerformanceComparisonUseCase
import com.example.booknest.domain.usecase.analytics.GetDetailedBookAnalyticsUseCase
import com.example.booknest.domain.usecase.applications.BulkActionApplicationsUseCase
import com.example.booknest.domain.usecase.applications.CheckApplicationUseCase
import com.example.booknest.domain.usecase.applications.CreateApplicationUseCase
import com.example.booknest.domain.usecase.applications.GetApplicationUseCase
import com.example.booknest.domain.usecase.applications.GetBookApplicationsUseCase
import com.example.booknest.domain.usecase.applications.GetMyApplicationsUseCase
import com.example.booknest.domain.usecase.applications.GetOverdueReviewsUseCase
import com.example.booknest.domain.usecase.applications.GetReadingProgressUseCase
import com.example.booknest.domain.usecase.applications.MarkCopyReceivedUseCase
import com.example.booknest.domain.usecase.applications.MarkCopySentUseCase
import com.example.booknest.domain.usecase.applications.RunLotterySelectionUseCase
import com.example.booknest.domain.usecase.applications.UpdateApplicationCompleteUseCase
import com.example.booknest.domain.usecase.applications.UpdateReadingStatusUseCase
import com.example.booknest.domain.usecase.applications.WithdrawApplicationUseCase
import com.example.booknest.domain.usecase.auth.LoginUseCase
import com.example.booknest.domain.usecase.auth.RegisterUseCase
import com.example.booknest.domain.usecase.auth.RequestPasswordResetUseCase
import com.example.booknest.domain.usecase.auth.ResendVerificationCodeUseCase
import com.example.booknest.domain.usecase.auth.ResetPasswordUseCase
import com.example.booknest.domain.usecase.auth.VerifyEmailUseCase
import com.example.booknest.domain.usecase.author.CheckIfFollowingAuthorUseCase
import com.example.booknest.domain.usecase.author.CreateBookUseCase
import com.example.booknest.domain.usecase.author.DecodeBookLeakFingerprintUseCase
import com.example.booknest.domain.usecase.author.DeleteBookUseCase
import com.example.booknest.domain.usecase.author.FollowAuthorUseCase
import com.example.booknest.domain.usecase.author.GetAuthorFollowersUseCase
import com.example.booknest.domain.usecase.author.GetBookStatsUseCase
import com.example.booknest.domain.usecase.author.GetBooksFromFollowedAuthorsUseCase
import com.example.booknest.domain.usecase.author.GetFollowedAuthorsUseCase
import com.example.booknest.domain.usecase.author.GetMyBooksUseCase
import com.example.booknest.domain.usecase.author.GetMySeriesUseCase
import com.example.booknest.domain.usecase.author.PublishBookUseCase
import com.example.booknest.domain.usecase.author.UnfollowAuthorUseCase
import com.example.booknest.domain.usecase.author.UpdateBookUseCase
import com.example.booknest.domain.usecase.files.GetBookDownloadUrlUseCase
import com.example.booknest.domain.usecase.files.RemoveBookCoverImageUseCase
import com.example.booknest.domain.usecase.files.UploadBookCoverImageUseCase
import com.example.booknest.domain.usecase.files.UploadBookFileUseCase
import com.example.booknest.domain.usecase.friends.AcceptFriendRequestUseCase
import com.example.booknest.domain.usecase.friends.CancelFriendRequestUseCase
import com.example.booknest.domain.usecase.friends.DeclineFriendRequestUseCase
import com.example.booknest.domain.usecase.friends.GetFriendshipStatusUseCase
import com.example.booknest.domain.usecase.friends.GetFriendsActivityUseCase
import com.example.booknest.domain.usecase.friends.GetReceivedFriendRequestsUseCase
import com.example.booknest.domain.usecase.friends.GetSentFriendRequestsUseCase
import com.example.booknest.domain.usecase.friends.SearchUsersUseCase
import com.example.booknest.domain.usecase.friends.SendFriendRequestUseCase
import com.example.booknest.domain.usecase.friends.UnfriendUserUseCase
import com.example.booknest.domain.usecase.genres.GetGenrePreferencesUseCase
import com.example.booknest.domain.usecase.genres.GetGenresUseCase
import com.example.booknest.domain.usecase.genres.SaveUserGenrePreferenceUseCase
import com.example.booknest.domain.usecase.notifications.DeleteAllNotificationsUseCase
import com.example.booknest.domain.usecase.notifications.DeleteNotificationUseCase
import com.example.booknest.domain.usecase.notifications.GetUnreadCountUseCase
import com.example.booknest.domain.usecase.notifications.MarkAllNotificationsAsReadUseCase
import com.example.booknest.domain.usecase.notifications.MarkNotificationAsReadUseCase
import com.example.booknest.domain.usecase.notifications.RegisterDeviceTokenUseCase
import com.example.booknest.domain.usecase.profile.AddAddressUseCase
import com.example.booknest.domain.usecase.profile.DeleteAccountUseCase
import com.example.booknest.domain.usecase.profile.DeleteAddressUseCase
import com.example.booknest.domain.usecase.profile.GetMyAddressesUseCase
import com.example.booknest.domain.usecase.profile.GetMyRecentActivityUseCase
import com.example.booknest.domain.usecase.profile.GetPublicUserProfileUseCase
import com.example.booknest.domain.usecase.profile.GetUserRecentActivityUseCase
import com.example.booknest.domain.usecase.profile.RemoveAvatarUseCase
import com.example.booknest.domain.usecase.profile.UpdateAddressUseCase
import com.example.booknest.domain.usecase.profile.UpdateMyProfileUseCase
import com.example.booknest.domain.usecase.profile.UpdateNotificationSettingsUseCase
import com.example.booknest.domain.usecase.profile.UpdatePrivacySettingsUseCase
import com.example.booknest.domain.usecase.profile.UpdateSocialMediaUseCase
import com.example.booknest.domain.usecase.reviews.CreateReviewUseCase
import com.example.booknest.domain.usecase.reviews.GetAuthorLatestReviewsUseCase
import com.example.booknest.domain.usecase.reviews.GetBookAllReviewsUseCase
import com.example.booknest.domain.usecase.reviews.GetBookReviewsUseCase
import com.example.booknest.domain.usecase.reviews.GetReviewUseCase
import com.example.booknest.domain.usecase.reviews.GetUserReviewsUseCase
import com.example.booknest.domain.usecase.reviews.UpdateReviewUseCase
import com.example.booknest.domain.usecase.series.CreateSeriesUseCase
import com.example.booknest.domain.usecase.series.DeleteSeriesUseCase
import com.example.booknest.domain.usecase.series.UpdateSeriesUseCase
import com.example.booknest.domain.usecase.books.BrowseBooksUseCase
import com.example.booknest.domain.usecase.books.GetBookDetailsUseCase
import com.example.booknest.domain.usecase.books.GetNewReleasesUseCase
import com.example.booknest.domain.usecase.books.GetRecommendedBooksUseCase
import com.example.booknest.domain.usecase.books.GetTrendingBooksUseCase
import com.example.booknest.domain.usecase.books.SearchBooksUseCase
import com.example.booknest.domain.usecase.files.UploadProfileImageUseCase
import com.example.booknest.domain.usecase.friends.GetFriendsUseCase
import com.example.booknest.domain.usecase.notifications.GetNotificationsUseCase
import com.example.booknest.domain.usecase.profile.GetAuthorStatsUseCase
import com.example.booknest.domain.usecase.profile.GetCurrentUserUseCase
import com.example.booknest.domain.usecase.profile.GetMyActivityUseCase
import com.example.booknest.domain.usecase.profile.GetMyProfileUseCase
import com.example.booknest.domain.usecase.profile.GetMyStatsUseCase
import com.example.booknest.domain.usecase.profile.GetUserProfileUseCase
import org.koin.dsl.module

val domainModule = module {
    factory { LoginUseCase(get()) }
    factory { RegisterUseCase(get()) }
    factory { VerifyEmailUseCase(get()) }
    factory { ResendVerificationCodeUseCase(get()) }
    factory { RequestPasswordResetUseCase(get()) }
    factory { ResetPasswordUseCase(get()) }

    factory { GetRecommendedBooksUseCase(get()) }
    factory { GetNewReleasesUseCase(get()) }
    factory { BrowseBooksUseCase(get()) }
    factory { SearchBooksUseCase(get()) }
    factory { GetBookDetailsUseCase(get()) }
    factory { GetTrendingBooksUseCase(get()) }

    factory { GetMyBooksUseCase(get()) }
    factory { GetMySeriesUseCase(get()) }
    factory { GetBookStatsUseCase(get()) }
    factory { CreateBookUseCase(get()) }
    factory { UpdateBookUseCase(get()) }
    factory { DeleteBookUseCase(get()) }
    factory { PublishBookUseCase(get()) }
    factory { DecodeBookLeakFingerprintUseCase(get()) }
    factory { FollowAuthorUseCase(get()) }
    factory { UnfollowAuthorUseCase(get()) }
    factory { GetFollowedAuthorsUseCase(get()) }
    factory { GetAuthorFollowersUseCase(get()) }
    factory { CheckIfFollowingAuthorUseCase(get()) }
    factory { GetBooksFromFollowedAuthorsUseCase(get()) }

    factory { CreateSeriesUseCase(get()) }
    factory { UpdateSeriesUseCase(get()) }
    factory { DeleteSeriesUseCase(get()) }

    factory { GetAuthorAnalyticsUseCase(get()) }
    factory { GetDetailedBookAnalyticsUseCase(get()) }
    factory { GetBookPerformanceComparisonUseCase(get()) }

    factory { GetMyApplicationsUseCase(get()) }
    factory { CheckApplicationUseCase(get()) }
    factory { CreateApplicationUseCase(get()) }
    factory { GetApplicationUseCase(get()) }
    factory { GetReadingProgressUseCase(get()) }
    factory { WithdrawApplicationUseCase(get()) }
    factory { MarkCopyReceivedUseCase(get()) }
    factory { UpdateReadingStatusUseCase(get()) }
    factory { GetBookApplicationsUseCase(get()) }
    factory { UpdateApplicationCompleteUseCase(get()) }
    factory { BulkActionApplicationsUseCase(get()) }
    factory { MarkCopySentUseCase(get()) }
    factory { RunLotterySelectionUseCase(get()) }
    factory { GetOverdueReviewsUseCase(get()) }

    factory { GetBookReviewsUseCase(get()) }
    factory { GetBookAllReviewsUseCase(get()) }
    factory { GetUserReviewsUseCase(get()) }
    factory { GetReviewUseCase(get()) }
    factory { CreateReviewUseCase(get()) }
    factory { UpdateReviewUseCase(get()) }
    factory { GetAuthorLatestReviewsUseCase(get()) }

    factory { GetCurrentUserUseCase(get()) }
    factory { GetMyProfileUseCase(get()) }
    factory { GetUserProfileUseCase(get()) }
    factory { GetMyStatsUseCase(get()) }
    factory { GetAuthorStatsUseCase(get()) }
    factory { GetMyActivityUseCase(get()) }
    factory { GetMyRecentActivityUseCase(get()) }
    factory { GetUserRecentActivityUseCase(get()) }
    factory { GetPublicUserProfileUseCase(get()) }
    factory { UpdateMyProfileUseCase(get()) }
    factory { UpdateSocialMediaUseCase(get()) }
    factory { UpdatePrivacySettingsUseCase(get()) }
    factory { UpdateNotificationSettingsUseCase(get()) }
    factory { GetMyAddressesUseCase(get()) }
    factory { AddAddressUseCase(get()) }
    factory { UpdateAddressUseCase(get()) }
    factory { DeleteAddressUseCase(get()) }
    factory { RemoveAvatarUseCase(get()) }
    factory { DeleteAccountUseCase(get()) }

    factory { GetGenresUseCase(get()) }
    factory { GetGenrePreferencesUseCase(get()) }
    factory { SaveUserGenrePreferenceUseCase(get()) }

    factory { GetFriendsUseCase(get()) }
    factory { GetSentFriendRequestsUseCase(get()) }
    factory { GetReceivedFriendRequestsUseCase(get()) }
    factory { GetFriendsActivityUseCase(get()) }
    factory { SearchUsersUseCase(get()) }
    factory { SendFriendRequestUseCase(get()) }
    factory { AcceptFriendRequestUseCase(get()) }
    factory { DeclineFriendRequestUseCase(get()) }
    factory { CancelFriendRequestUseCase(get()) }
    factory { UnfriendUserUseCase(get()) }
    factory { GetFriendshipStatusUseCase(get()) }

    factory { GetNotificationsUseCase(get()) }
    factory { GetUnreadCountUseCase(get()) }
    factory { MarkNotificationAsReadUseCase(get()) }
    factory { MarkAllNotificationsAsReadUseCase(get()) }
    factory { DeleteNotificationUseCase(get()) }
    factory { DeleteAllNotificationsUseCase(get()) }
    factory { RegisterDeviceTokenUseCase(get()) }

    factory { UploadProfileImageUseCase(get()) }
    factory { UploadBookFileUseCase(get()) }
    factory { GetBookDownloadUrlUseCase(get()) }
    factory { UploadBookCoverImageUseCase(get()) }
    factory { RemoveBookCoverImageUseCase(get()) }
}
