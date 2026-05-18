package com.example.booknest.di

import com.example.booknest.viewmodel.analytics.AnalyticsViewModel
import com.example.booknest.viewmodel.analytics.ReviewViewModel
import com.example.booknest.viewmodel.applications.ApplicationViewModel
import com.example.booknest.viewmodel.applications.BookApplicationViewModel
import com.example.booknest.viewmodel.auth.EmailVerificationViewModel
import com.example.booknest.viewmodel.auth.LoginViewModel
import com.example.booknest.viewmodel.auth.PasswordResetViewModel
import com.example.booknest.viewmodel.auth.SignupViewModel
import com.example.booknest.viewmodel.author.AuthorBooksViewModel
import com.example.booknest.viewmodel.author.AuthorDashboardViewModel
import com.example.booknest.viewmodel.author.AuthorFollowViewModel
import com.example.booknest.viewmodel.author.AuthorSeriesViewModel
import com.example.booknest.viewmodel.books.BookViewModel
import com.example.booknest.viewmodel.files.FileViewModel
import com.example.booknest.viewmodel.friends.FriendViewModel
import com.example.booknest.viewmodel.genres.FavoriteGenresViewModel
import com.example.booknest.viewmodel.main.MainViewModel
import com.example.booknest.viewmodel.notifications.NotificationViewModel
import com.example.booknest.viewmodel.profile.AddressViewModel
import com.example.booknest.viewmodel.profile.ProfileSettingsViewModel
import com.example.booknest.viewmodel.profile.ProfileStatsViewModel
import com.example.booknest.viewmodel.profile.ProfileViewModel
import com.example.booknest.viewmodel.series.SeriesViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel {
        LoginViewModel(
            feedback = get(),
            loginUseCase = get(),
            getCurrentUserUseCase = get(),
            sessionManager = get()
        )
    }

    viewModel {
        SignupViewModel(
            feedback = get(),
            sessionManager = get(),
            registerUseCase = get(),
            getGenresUseCase = get(),
            saveUserGenrePreferenceUseCase = get()
        )
    }

    viewModel {
        EmailVerificationViewModel(
            feedback = get(),
            verifyEmailUseCase = get(),
            resendVerificationCodeUseCase = get(),
            getCurrentUserUseCase = get(),
            sessionManager = get()
        )
    }

    viewModel {
        PasswordResetViewModel(
            feedback = get(),
            resetPasswordUseCase = get(),
            requestPasswordResetUseCase = get()
        )
    }

    viewModel {
        BookViewModel(
            getRecommendedBooksUseCase = get(),
            getNewReleasesUseCase = get(),
            browseBooksUseCase = get(),
            searchBooksUseCase = get(),
            getBookDetailsUseCase = get(),
            getTrendingBooksUseCase = get(),
            getGenresUseCase = get(),
            searchHistoryManager = get()
        )
    }

    viewModel {
        ApplicationViewModel(
            feedback = get(),
            getMyApplicationsUseCase = get(),
            checkApplicationUseCase = get(),
            createApplicationUseCase = get(),
            getApplicationUseCase = get(),
            getReadingProgressUseCase = get(),
            withdrawApplicationUseCase = get(),
            markCopyReceivedUseCase = get(),
            updateReadingStatusUseCase = get()
        )
    }

    viewModel {
        BookApplicationViewModel(
            feedback = get(),
            getBookApplicationsUseCase = get(),
            updateApplicationCompleteUseCase = get(),
            bulkActionApplicationsUseCase = get(),
            markCopySentUseCase = get(),
            runLotterySelectionUseCase = get(),
            getOverdueReviewsUseCase = get()
        )
    }

    viewModel {
        ReviewViewModel(
            feedback = get(),
            getBookReviewsUseCase = get(),
            getUserReviewsUseCase = get(),
            getReviewUseCase = get(),
            createReviewUseCase = get(),
            updateReviewUseCase = get(),
            getBookAllReviewsUseCase = get()
        )
    }

    viewModel {
        ProfileViewModel(
            feedback = get(),
            sessionManager = get(),
            getMyProfileUseCase = get(),
            getUserProfileUseCase = get(),
            getCurrentUserUseCase = get(),
            getMyRecentActivityUseCase = get(),
            getUserRecentActivityUseCase = get(),
            getPublicUserProfileUseCase = get(),
            updateMyProfileUseCase = get(),
            removeAvatarUseCase = get(),
            deleteAccountUseCase = get(),
            uploadProfileImageUseCase = get(),
            authRepository = get()
        )
    }

    viewModel {
        ProfileSettingsViewModel(
            feedback = get(),
            updateSocialMediaUseCase = get(),
            updatePrivacySettingsUseCase = get(),
            updateNotificationSettingsUseCase = get()
        )
    }

    viewModel {
        ProfileStatsViewModel(
            getMyStatsUseCase = get(),
            getAuthorStatsUseCase = get()
        )
    }

    viewModel {
        AddressViewModel(
            feedback = get(),
            getMyAddressesUseCase = get(),
            addAddressUseCase = get(),
            updateAddressUseCase = get(),
            deleteAddressUseCase = get()
        )
    }

    viewModel {
        NotificationViewModel(
            feedback = get(),
            getNotificationsUseCase = get(),
            getUnreadCountUseCase = get(),
            markNotificationAsReadUseCase = get(),
            markAllNotificationsAsReadUseCase = get(),
            deleteNotificationUseCase = get(),
            deleteAllNotificationsUseCase = get(),
            registerDeviceTokenUseCase = get(),
            acceptFriendRequestUseCase = get(),
            declineFriendRequestUseCase = get(),
            sessionManager = get()
        )
    }

    viewModel {
        FriendViewModel(
            feedback = get(),
            getFriendsUseCase = get(),
            getSentFriendRequestsUseCase = get(),
            getReceivedFriendRequestsUseCase = get(),
            getFriendsActivityUseCase = get(),
            searchUsersUseCase = get(),
            sendFriendRequestUseCase = get(),
            acceptFriendRequestUseCase = get(),
            declineFriendRequestUseCase = get(),
            cancelFriendRequestUseCase = get(),
            unfriendUserUseCase = get(),
            getFriendshipStatusUseCase = get(),
            sessionManager = get()
        )
    }

    viewModel {
        AuthorFollowViewModel(
            feedback = get(),
            getFollowedAuthorsUseCase = get(),
            getAuthorFollowersUseCase = get(),
            getBooksFromFollowedAuthorsUseCase = get(),
            followAuthorUseCase = get(),
            unfollowAuthorUseCase = get(),
            checkIfFollowingAuthorUseCase = get(),
            sessionManager = get()
        )
    }

    viewModel {
        AuthorBooksViewModel(
            feedback = get(),
            getMyBooksUseCase = get(),
            getBookStatsUseCase = get(),
            createBookUseCase = get(),
            updateBookUseCase = get(),
            deleteBookUseCase = get(),
            publishBookUseCase = get(),
            uploadBookFileUseCase = get(),
            uploadBookCoverImageUseCase = get(),
            removeBookCoverImageUseCase = get(),
            decodeBookLeakFingerprintUseCase = get()
        )
    }

    viewModel {
        AuthorSeriesViewModel(
            feedback = get(),
            getMySeriesUseCase = get(),
            createSeriesUseCase = get(),
            updateSeriesUseCase = get()
        )
    }

    viewModel {
        AuthorDashboardViewModel(
            feedback = get(),
            getMyStatsUseCase = get(),
            getAuthorLatestReviewsUseCase = get(),
            getOverdueReviewsUseCase = get()
        )
    }

    viewModel {
        AnalyticsViewModel(
            feedback = get(),
            getDetailedBookAnalyticsUseCase = get(),
            getAuthorAnalyticsUseCase = get(),
            getBookPerformanceComparisonUseCase = get()
        )
    }

    viewModel {
        FavoriteGenresViewModel(
            feedback = get(),
            getGenresUseCase = get(),
            getGenrePreferencesUseCase = get(),
            saveUserGenrePreferenceUseCase = get()
        )
    }

    viewModel {
        MainViewModel(
            getCurrentUserUseCase = get(),
            sessionManager = get()
        )
    }

    viewModel {
        FileViewModel(
            context = androidContext(),
            uploadBookFileUseCase = get(),
            getBookDownloadUrlUseCase = get(),
            downloadNotifier = get(),
        )
    }

    viewModel {
        SeriesViewModel(
            feedback = get(),
            getMySeriesUseCase = get(),
            createSeriesUseCase = get(),
            updateSeriesUseCase = get(),
            deleteSeriesUseCase = get()
        )
    }
}
