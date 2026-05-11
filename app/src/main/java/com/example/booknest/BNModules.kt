package com.example.booknest

import com.example.booknest.data.constants.MediaType
import com.example.booknest.data.constants.RetrofitConstants
import com.example.booknest.data.datasource.ApplicationsDataSource
import com.example.booknest.data.datasource.AuthDataSource
import com.example.booknest.data.datasource.AuthorsDataSource
import com.example.booknest.data.datasource.BNApplicationsDataSource
import com.example.booknest.data.datasource.BNAuthDataSource
import com.example.booknest.data.datasource.BNAuthorsDataSource
import com.example.booknest.data.datasource.BNBooksDataSource
import com.example.booknest.data.datasource.BNFriendsDataSource
import com.example.booknest.data.datasource.BNGenresDataSource
import com.example.booknest.data.datasource.BNNotificationsDataSource
import com.example.booknest.data.datasource.BNProfilesDataSource
import com.example.booknest.data.datasource.BNReviewsDataSource
import com.example.booknest.data.datasource.BNSeriesDataSource
import com.example.booknest.data.datasource.BooksDataSource
import com.example.booknest.data.datasource.FriendsDataSource
import com.example.booknest.data.datasource.GenresDataSource
import com.example.booknest.data.datasource.NotificationsDataSource
import com.example.booknest.data.datasource.ProfilesDataSource
import com.example.booknest.data.datasource.ReviewsDataSource
import com.example.booknest.data.datasource.SeriesDataSource
import com.example.booknest.data.repository.BNApplicationsRepository
import com.example.booknest.data.repository.BNAuthRepository
import com.example.booknest.data.repository.BNAuthorFollowRepository
import com.example.booknest.data.repository.BNBooksRepository
import com.example.booknest.data.repository.BNFriendsRepository
import com.example.booknest.data.repository.BNGenresRepository
import com.example.booknest.data.repository.BNNotificationsRepository
import com.example.booknest.data.repository.BNProfileRepository
import com.example.booknest.data.repository.BNReviewsRepository
import com.example.booknest.data.repository.BNSeriesRepository
import com.example.booknest.data.service.ApplicationsService
import com.example.booknest.data.service.AuthService
import com.example.booknest.data.service.AuthorsService
import com.example.booknest.data.service.BooksService
import com.example.booknest.data.service.FriendsService
import com.example.booknest.data.service.GenresService
import com.example.booknest.data.service.NotificationsService
import com.example.booknest.data.service.ProfilesService
import com.example.booknest.data.service.ReviewsService
import com.example.booknest.data.service.SeriesService
import com.example.booknest.network.NetworkConnectivityMonitor
import com.example.booknest.data.session.SearchHistoryManager
import com.example.booknest.data.session.SessionManager
import com.example.booknest.data.session.TokenAuthenticator
import com.example.booknest.data.session.TokenInterceptor
import com.example.booknest.data.session.searchHistoryDataStore
import com.example.booknest.domain.repository.ApplicationsRepository
import com.example.booknest.domain.repository.AuthRepository
import com.example.booknest.domain.repository.AuthorFollowRepository
import com.example.booknest.domain.repository.BooksRepository
import com.example.booknest.domain.repository.FriendsRepository
import com.example.booknest.domain.repository.GenresRepository
import com.example.booknest.domain.repository.NotificationsRepository
import com.example.booknest.domain.repository.ProfileRepository
import com.example.booknest.domain.repository.ReviewsRepository
import com.example.booknest.domain.repository.SeriesRepository
import com.example.booknest.domain.usecase.analytics.GetAuthorAnalyticsUseCase
import com.example.booknest.domain.usecase.analytics.GetBookPerformanceComparisonUseCase
import com.example.booknest.domain.usecase.analytics.GetDetailedBookAnalyticsUseCase
import com.example.booknest.domain.usecase.applications.GetMyApplicationsUseCase
import com.example.booknest.domain.usecase.auth.LoginUseCase
import com.example.booknest.domain.usecase.auth.RefreshTokenUseCase
import com.example.booknest.domain.usecase.auth.RegisterUseCase
import com.example.booknest.domain.usecase.auth.RequestPasswordResetUseCase
import com.example.booknest.domain.usecase.auth.ResendVerificationCodeUseCase
import com.example.booknest.domain.usecase.auth.ResetPasswordUseCase
import com.example.booknest.domain.usecase.auth.VerifyEmailUseCase
import com.example.booknest.domain.usecase.author.GetBookStatsUseCase
import com.example.booknest.domain.usecase.author.GetMyBooksUseCase
import com.example.booknest.domain.usecase.author.GetMySeriesUseCase
import com.example.booknest.domain.usecase.books.BrowseBooksUseCase
import com.example.booknest.domain.usecase.books.GetBookDetailsUseCase
import com.example.booknest.domain.usecase.books.GetNewReleasesUseCase
import com.example.booknest.domain.usecase.books.GetRecommendedBooksUseCase
import com.example.booknest.domain.usecase.books.GetTrendingBooksUseCase
import com.example.booknest.domain.usecase.books.SearchBooksUseCase
import com.example.booknest.domain.usecase.files.GetBookDownloadUrlUseCase
import com.example.booknest.domain.usecase.files.UploadBookFileUseCase
import com.example.booknest.domain.usecase.files.UploadProfileImageUseCase
import com.example.booknest.domain.usecase.friends.GetFriendsUseCase
import com.example.booknest.domain.usecase.genres.GetGenrePreferencesUseCase
import com.example.booknest.domain.usecase.genres.GetGenresUseCase
import com.example.booknest.domain.usecase.genres.SaveUserGenrePreferenceUseCase
import com.example.booknest.domain.usecase.notifications.GetNotificationsUseCase
import com.example.booknest.domain.usecase.profile.GetAuthorStatsUseCase
import com.example.booknest.domain.usecase.profile.GetCurrentUserUseCase
import com.example.booknest.domain.usecase.profile.GetMyActivityUseCase
import com.example.booknest.domain.usecase.profile.GetMyProfileUseCase
import com.example.booknest.domain.usecase.profile.GetMyStatsUseCase
import com.example.booknest.domain.usecase.profile.GetUserProfileUseCase
import com.example.booknest.viewmodel.analytics.AnalyticsViewModel
import com.example.booknest.viewmodel.applications.ApplicationViewModel
import com.example.booknest.viewmodel.applications.BookApplicationViewModel
import com.example.booknest.viewmodel.author.AuthorFollowViewModel
import com.example.booknest.viewmodel.author.AuthorBooksViewModel
import com.example.booknest.viewmodel.author.AuthorDashboardViewModel
import com.example.booknest.viewmodel.author.AuthorSeriesViewModel
import com.example.booknest.viewmodel.books.BookViewModel
import com.example.booknest.viewmodel.auth.EmailVerificationViewModel
import com.example.booknest.viewmodel.genres.FavoriteGenresViewModel
import com.example.booknest.viewmodel.files.FileViewModel
import com.example.booknest.viewmodel.friends.FriendViewModel
import com.example.booknest.viewmodel.auth.LoginViewModel
import com.example.booknest.viewmodel.main.MainViewModel
import com.example.booknest.viewmodel.notifications.NotificationViewModel
import com.example.booknest.viewmodel.auth.PasswordResetViewModel
import com.example.booknest.viewmodel.profile.AddressViewModel
import com.example.booknest.viewmodel.profile.ProfileViewModel
import com.example.booknest.viewmodel.profile.ProfileSettingsViewModel
import com.example.booknest.viewmodel.profile.ProfileStatsViewModel
import com.example.booknest.viewmodel.analytics.ReviewViewModel
import com.example.booknest.viewmodel.series.SeriesViewModel
import com.example.booknest.viewmodel.auth.SignupViewModel
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import coil.ImageLoader

val appModule = module {

    single { SessionManager.getInstance(androidContext().dataStore) }

    single { NetworkConnectivityMonitor(androidContext()) }

    single { SearchHistoryManager(androidContext().searchHistoryDataStore) }

    factory { RefreshTokenUseCase(authRepository = inject()) }

    single {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY

        OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .addInterceptor(TokenInterceptor(androidContext()))
            .authenticator(TokenAuthenticator(get<RefreshTokenUseCase>(), androidContext()))
            .connectTimeout(RetrofitConstants.TIME, TimeUnit.SECONDS)
            .readTimeout(RetrofitConstants.TIME, TimeUnit.SECONDS)
            .writeTimeout(RetrofitConstants.TIME, TimeUnit.SECONDS)
            .build()
    }

    single {
        ImageLoader.Builder(androidContext())
            .okHttpClient(get<OkHttpClient>())
            .respectCacheHeaders(false)
            .build()
    }

    single {
        val json = Json {
            ignoreUnknownKeys = true
            explicitNulls = false
            coerceInputValues = true
        }

        val okHttpClient = get<OkHttpClient>()
        val contentType = MediaType.APPLICATION_MEDIA_TYPE.toMediaType()

        Retrofit.Builder()
            .baseUrl(RetrofitConstants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    single<AuthService> { get<Retrofit>().create(AuthService::class.java) }
    single<BooksService> { get<Retrofit>().create(BooksService::class.java) }
    single<ApplicationsService> { get<Retrofit>().create(ApplicationsService::class.java) }
    single<ReviewsService> { get<Retrofit>().create(ReviewsService::class.java) }
    single<GenresService> { get<Retrofit>().create(GenresService::class.java) }
    single<ProfilesService> { get<Retrofit>().create(ProfilesService::class.java) }
    single<FriendsService> { get<Retrofit>().create(FriendsService::class.java) }
    single<AuthorsService> { get<Retrofit>().create(AuthorsService::class.java) }
    single<NotificationsService> { get<Retrofit>().create(NotificationsService::class.java) }
    single<SeriesService> { get<Retrofit>().create(SeriesService::class.java) }

    single<AuthDataSource> { BNAuthDataSource(get(), get(), androidContext()) }
    single<BooksDataSource> { BNBooksDataSource(get()) }
    single<ApplicationsDataSource> { BNApplicationsDataSource(get()) }
    single<ReviewsDataSource> { BNReviewsDataSource(get()) }
    single<GenresDataSource> { BNGenresDataSource(get()) }
    single<ProfilesDataSource> { BNProfilesDataSource(get()) }
    single<FriendsDataSource> { BNFriendsDataSource(get()) }
    single<AuthorsDataSource> { BNAuthorsDataSource(get()) }
    single<NotificationsDataSource> { BNNotificationsDataSource(get()) }
    single<SeriesDataSource> { BNSeriesDataSource(get()) }

    single<AuthRepository> { BNAuthRepository(get()) }
    single<BooksRepository> { BNBooksRepository(get()) }
    single<ApplicationsRepository> { BNApplicationsRepository(get()) }
    single<ReviewsRepository> { BNReviewsRepository(get()) }
    single<GenresRepository> { BNGenresRepository(get()) }
    single<ProfileRepository> { BNProfileRepository(get()) }
    single<FriendsRepository> { BNFriendsRepository(get()) }
    single<AuthorFollowRepository> { BNAuthorFollowRepository(get()) }
    single<NotificationsRepository> { BNNotificationsRepository(get()) }
    single<SeriesRepository> { BNSeriesRepository(get()) }

    single { LoginUseCase(get()) }
    single { RegisterUseCase(get()) }
    single { VerifyEmailUseCase(get()) }
    single { ResendVerificationCodeUseCase(get()) }
    single { RequestPasswordResetUseCase(get()) }
    single { ResetPasswordUseCase(get()) }

    single { GetRecommendedBooksUseCase(get()) }
    single { GetNewReleasesUseCase(get()) }
    single { BrowseBooksUseCase(get()) }
    single { SearchBooksUseCase(get()) }
    single { GetBookDetailsUseCase(get()) }
    single { GetTrendingBooksUseCase(get()) }

    single { GetMyBooksUseCase(get()) }
    single { GetMySeriesUseCase(get()) }
    single { GetBookStatsUseCase(get()) }
    single { com.example.booknest.domain.usecase.author.CreateBookUseCase(get()) }
    single { com.example.booknest.domain.usecase.author.UpdateBookUseCase(get()) }
    single { com.example.booknest.domain.usecase.author.DeleteBookUseCase(get()) }
    single { com.example.booknest.domain.usecase.author.PublishBookUseCase(get()) }
    single { com.example.booknest.domain.usecase.author.FollowAuthorUseCase(get()) }
    single { com.example.booknest.domain.usecase.author.UnfollowAuthorUseCase(get()) }
    single { com.example.booknest.domain.usecase.author.GetFollowedAuthorsUseCase(get()) }
    single { com.example.booknest.domain.usecase.author.GetAuthorFollowersUseCase(get()) }
    single { com.example.booknest.domain.usecase.author.CheckIfFollowingAuthorUseCase(get()) }
    single { com.example.booknest.domain.usecase.author.GetBooksFromFollowedAuthorsUseCase(get()) }

    single { com.example.booknest.domain.usecase.series.CreateSeriesUseCase(get()) }
    single { com.example.booknest.domain.usecase.series.UpdateSeriesUseCase(get()) }
    single { com.example.booknest.domain.usecase.series.DeleteSeriesUseCase(get()) }

    single { GetAuthorAnalyticsUseCase(get()) }
    single { GetDetailedBookAnalyticsUseCase(get()) }
    single { GetBookPerformanceComparisonUseCase(get()) }

    single { GetMyApplicationsUseCase(get()) }
    single { com.example.booknest.domain.usecase.applications.CheckApplicationUseCase(get()) }
    single { com.example.booknest.domain.usecase.applications.CreateApplicationUseCase(get()) }
    single { com.example.booknest.domain.usecase.applications.GetApplicationUseCase(get()) }
    single { com.example.booknest.domain.usecase.applications.GetReadingProgressUseCase(get()) }
    single { com.example.booknest.domain.usecase.applications.WithdrawApplicationUseCase(get()) }
    single { com.example.booknest.domain.usecase.applications.MarkCopyReceivedUseCase(get()) }
    single { com.example.booknest.domain.usecase.applications.UpdateReadingStatusUseCase(get()) }
    single { com.example.booknest.domain.usecase.applications.GetBookApplicationsUseCase(get()) }
    single { com.example.booknest.domain.usecase.applications.UpdateApplicationCompleteUseCase(get()) }
    single { com.example.booknest.domain.usecase.applications.BulkActionApplicationsUseCase(get()) }
    single { com.example.booknest.domain.usecase.applications.MarkCopySentUseCase(get()) }
    single { com.example.booknest.domain.usecase.applications.RunLotterySelectionUseCase(get()) }
    single { com.example.booknest.domain.usecase.applications.GetOverdueReviewsUseCase(get()) }

    single { com.example.booknest.domain.usecase.reviews.GetBookReviewsUseCase(get()) }
    single { com.example.booknest.domain.usecase.reviews.GetBookAllReviewsUseCase(get()) }
    single { com.example.booknest.domain.usecase.reviews.GetUserReviewsUseCase(get()) }
    single { com.example.booknest.domain.usecase.reviews.GetReviewUseCase(get()) }
    single { com.example.booknest.domain.usecase.reviews.CreateReviewUseCase(get()) }
    single { com.example.booknest.domain.usecase.reviews.UpdateReviewUseCase(get()) }
    single { com.example.booknest.domain.usecase.reviews.GetAuthorLatestReviewsUseCase(get()) }

    single { GetCurrentUserUseCase(get()) }
    single { GetMyProfileUseCase(get()) }
    single { GetUserProfileUseCase(get()) }
    single { GetMyStatsUseCase(get()) }
    single { GetAuthorStatsUseCase(get()) }
    single { GetMyActivityUseCase(get()) }
    single { com.example.booknest.domain.usecase.profile.GetMyRecentActivityUseCase(get()) }
    single { com.example.booknest.domain.usecase.profile.GetUserRecentActivityUseCase(get()) }
    single { com.example.booknest.domain.usecase.profile.GetPublicUserProfileUseCase(get()) }
    single { com.example.booknest.domain.usecase.profile.UpdateMyProfileUseCase(get()) }
    single { com.example.booknest.domain.usecase.profile.UpdateSocialMediaUseCase(get()) }
    single { com.example.booknest.domain.usecase.profile.UpdatePrivacySettingsUseCase(get()) }
    single { com.example.booknest.domain.usecase.profile.UpdateNotificationSettingsUseCase(get()) }
    single { com.example.booknest.domain.usecase.profile.GetMyAddressesUseCase(get()) }
    single { com.example.booknest.domain.usecase.profile.AddAddressUseCase(get()) }
    single { com.example.booknest.domain.usecase.profile.UpdateAddressUseCase(get()) }
    single { com.example.booknest.domain.usecase.profile.DeleteAddressUseCase(get()) }
    single { com.example.booknest.domain.usecase.profile.RemoveAvatarUseCase(get()) }
    single { com.example.booknest.domain.usecase.profile.DeleteAccountUseCase(get()) }

    single { GetGenresUseCase(get()) }
    single { GetGenrePreferencesUseCase(get()) }
    single { SaveUserGenrePreferenceUseCase(get()) }

    single { GetFriendsUseCase(get()) }
    single { com.example.booknest.domain.usecase.friends.GetSentFriendRequestsUseCase(get()) }
    single { com.example.booknest.domain.usecase.friends.GetReceivedFriendRequestsUseCase(get()) }
    single { com.example.booknest.domain.usecase.friends.GetFriendsActivityUseCase(get()) }
    single { com.example.booknest.domain.usecase.friends.SearchUsersUseCase(get()) }
    single { com.example.booknest.domain.usecase.friends.SendFriendRequestUseCase(get()) }
    single { com.example.booknest.domain.usecase.friends.AcceptFriendRequestUseCase(get()) }
    single { com.example.booknest.domain.usecase.friends.DeclineFriendRequestUseCase(get()) }
    single { com.example.booknest.domain.usecase.friends.CancelFriendRequestUseCase(get()) }
    single { com.example.booknest.domain.usecase.friends.UnfriendUserUseCase(get()) }
    single { com.example.booknest.domain.usecase.friends.GetFriendshipStatusUseCase(get()) }

    single { GetNotificationsUseCase(get()) }
    single { com.example.booknest.domain.usecase.notifications.GetUnreadCountUseCase(get()) }
    single { com.example.booknest.domain.usecase.notifications.MarkNotificationAsReadUseCase(get()) }
    single { com.example.booknest.domain.usecase.notifications.MarkAllNotificationsAsReadUseCase(get()) }
    single { com.example.booknest.domain.usecase.notifications.DeleteNotificationUseCase(get()) }
    single { com.example.booknest.domain.usecase.notifications.DeleteAllNotificationsUseCase(get()) }
    single { com.example.booknest.domain.usecase.notifications.RegisterDeviceTokenUseCase(get()) }

    single { UploadProfileImageUseCase(get()) }
    single { UploadBookFileUseCase(get()) }
    single { GetBookDownloadUrlUseCase(get()) }
    single { com.example.booknest.domain.usecase.files.UploadBookCoverImageUseCase(get()) }
    single { com.example.booknest.domain.usecase.files.RemoveBookCoverImageUseCase(get()) }

    viewModel {
        LoginViewModel(
            loginUseCase = get(),
            getCurrentUserUseCase = get(),
            sessionManager = get()
        )
    }

    viewModel {
        SignupViewModel(
            sessionManager = get(),
            registerUseCase = get(),
            getGenresUseCase = get(),
            saveUserGenrePreferenceUseCase = get()
        )
    }

    viewModel {
        EmailVerificationViewModel(
            verifyEmailUseCase = get(),
            resendVerificationCodeUseCase = get()
        )
    }

    viewModel {
        PasswordResetViewModel(
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
            getTrendingBooksUseCase = get()
        )
    }

    viewModel {
        ApplicationViewModel(
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
            getMyAddressesUseCase = get(),
            addAddressUseCase = get(),
            updateAddressUseCase = get(),
            deleteAddressUseCase = get()
        )
    }

    viewModel {
        NotificationViewModel(
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
            getMyBooksUseCase = get(),
            getBookStatsUseCase = get(),
            createBookUseCase = get(),
            updateBookUseCase = get(),
            deleteBookUseCase = get(),
            publishBookUseCase = get(),
            uploadBookFileUseCase = get(),
            uploadBookCoverImageUseCase = get(),
            removeBookCoverImageUseCase = get()
        )
    }

    viewModel {
        AuthorSeriesViewModel(
            getMySeriesUseCase = get(),
            createSeriesUseCase = get(),
            updateSeriesUseCase = get()
        )
    }

    viewModel {
        AuthorDashboardViewModel(
            getMyStatsUseCase = get(),
            getAuthorLatestReviewsUseCase = get(),
            getOverdueReviewsUseCase = get()
        )
    }

    viewModel {
        AnalyticsViewModel(
            getDetailedBookAnalyticsUseCase = get(),
            getAuthorAnalyticsUseCase = get(),
            getBookPerformanceComparisonUseCase = get()
        )
    }

    viewModel {
        FavoriteGenresViewModel(
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
            getBookDownloadUrlUseCase = get()
        )
    }


    viewModel {
        SeriesViewModel(
            getMySeriesUseCase = get(),
            createSeriesUseCase = get(),
            updateSeriesUseCase = get(),
            deleteSeriesUseCase = get()
        )
    }
}
