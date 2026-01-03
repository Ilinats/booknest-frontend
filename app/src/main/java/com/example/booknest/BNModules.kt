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
import com.example.booknest.data.datasource.BNFilesDataSource
import com.example.booknest.data.datasource.BNFriendsDataSource
import com.example.booknest.data.datasource.BNGenresDataSource
import com.example.booknest.data.datasource.BNNotificationsDataSource
import com.example.booknest.data.datasource.BNProfilesDataSource
import com.example.booknest.data.datasource.BNReviewsDataSource
import com.example.booknest.data.datasource.BNSeriesDataSource
import com.example.booknest.data.datasource.BooksDataSource
import com.example.booknest.data.datasource.FilesDataSource
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
import com.example.booknest.data.repository.BNFilesRepository
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
import com.example.booknest.data.service.FilesService
import com.example.booknest.data.service.FriendsService
import com.example.booknest.data.service.GenresService
import com.example.booknest.data.service.NotificationsService
import com.example.booknest.data.service.ProfilesService
import com.example.booknest.data.service.ReviewsService
import com.example.booknest.data.service.SeriesService
import com.example.booknest.data.session.SearchHistoryManager
import com.example.booknest.data.session.SessionManager
import com.example.booknest.data.session.TokenAuthenticator
import com.example.booknest.data.session.TokenInterceptor
import com.example.booknest.data.session.searchHistoryDataStore
import com.example.booknest.domain.repository.ApplicationsRepository
import com.example.booknest.domain.repository.AuthRepository
import com.example.booknest.domain.repository.AuthorFollowRepository
import com.example.booknest.domain.repository.BooksRepository
import com.example.booknest.domain.repository.FilesRepository
import com.example.booknest.domain.repository.FriendsRepository
import com.example.booknest.domain.repository.GenresRepository
import com.example.booknest.domain.repository.NotificationsRepository
import com.example.booknest.domain.repository.ProfileRepository
import com.example.booknest.domain.repository.ReviewsRepository
import com.example.booknest.domain.repository.SeriesRepository
import com.example.booknest.domain.usecase.analytics.GetAuthorAnalyticsUseCase
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
import com.example.booknest.domain.usecase.profile.GetMyActivityUseCase
import com.example.booknest.domain.usecase.profile.GetMyProfileUseCase
import com.example.booknest.domain.usecase.profile.GetMyStatsUseCase
import com.example.booknest.domain.usecase.profile.GetUserProfileUseCase
import com.example.booknest.viewmodel.AnalyticsViewModel
import com.example.booknest.viewmodel.ApplicationViewModel
import com.example.booknest.viewmodel.AuthorFollowViewModel
import com.example.booknest.viewmodel.AuthorViewModel
import com.example.booknest.viewmodel.BookViewModel
import com.example.booknest.viewmodel.EmailVerificationViewModel
import com.example.booknest.viewmodel.FavoriteGenresViewModel
import com.example.booknest.viewmodel.FileViewModel
import com.example.booknest.viewmodel.GoogleAuthViewModel
import com.example.booknest.viewmodel.FriendViewModel
import com.example.booknest.viewmodel.LoginViewModel
import com.example.booknest.viewmodel.NotificationViewModel
import com.example.booknest.viewmodel.PasswordResetViewModel
import com.example.booknest.viewmodel.ProfileViewModel
import com.example.booknest.viewmodel.ReviewViewModel
import com.example.booknest.viewmodel.SeriesViewModel
import com.example.booknest.viewmodel.SignupViewModel
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

val appModule = module {

    single { SessionManager.getInstance(androidContext().dataStore) }

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
    single<FilesService> { get<Retrofit>().create(FilesService::class.java) }
    single<SeriesService> { get<Retrofit>().create(SeriesService::class.java) }

    factory<AuthDataSource> { BNAuthDataSource(get(), get(), androidContext()) }
    factory<BooksDataSource> { BNBooksDataSource(get()) }
    factory<ApplicationsDataSource> { BNApplicationsDataSource(get()) }
    factory<ReviewsDataSource> { BNReviewsDataSource(get()) }
    factory<GenresDataSource> { BNGenresDataSource(get()) }
    factory<ProfilesDataSource> { BNProfilesDataSource(get()) }
    factory<FriendsDataSource> { BNFriendsDataSource(get()) }
    factory<AuthorsDataSource> { BNAuthorsDataSource(get()) }
    factory<NotificationsDataSource> { BNNotificationsDataSource(get()) }
    factory<FilesDataSource> { BNFilesDataSource(get()) }
    factory<SeriesDataSource> { BNSeriesDataSource(get()) }

    factory<AuthRepository> { BNAuthRepository(get()) }
    factory<BooksRepository> { BNBooksRepository(get()) }
    factory<ApplicationsRepository> { BNApplicationsRepository(get()) }
    factory<ReviewsRepository> { BNReviewsRepository(get()) }
    factory<GenresRepository> { BNGenresRepository(get()) }
    factory<ProfileRepository> { BNProfileRepository(get()) }
    factory<FriendsRepository> { BNFriendsRepository(get()) }
    factory<AuthorFollowRepository> { BNAuthorFollowRepository(get()) }
    factory<NotificationsRepository> { BNNotificationsRepository(get()) }
    factory<FilesRepository> { BNFilesRepository(get()) }
    factory<SeriesRepository> { BNSeriesRepository(get()) }

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

    factory { GetMyBooksUseCase(get()) }
    factory { GetMySeriesUseCase(get()) }
    factory { GetBookStatsUseCase(get()) }

    factory { GetAuthorAnalyticsUseCase(get()) }
    factory { GetDetailedBookAnalyticsUseCase(get()) }

    factory { GetMyApplicationsUseCase(get()) }

    factory { GetMyProfileUseCase(get()) }
    factory { GetUserProfileUseCase(get()) }
    factory { GetMyStatsUseCase(get()) }
    factory { GetAuthorStatsUseCase(get()) }
    factory { GetMyActivityUseCase(get()) }

    factory { GetGenresUseCase(get()) }
    factory { GetGenrePreferencesUseCase(get()) }
    factory { SaveUserGenrePreferenceUseCase(get()) }

    factory { GetFriendsUseCase(get()) }

    factory { GetNotificationsUseCase(get()) }

    factory { UploadProfileImageUseCase(get()) }
    factory { UploadBookFileUseCase(get()) }
    factory { GetBookDownloadUrlUseCase(get()) }

    viewModel {
        LoginViewModel(
            loginUseCase = get(),
            sessionManager = get()
        )
    }

    viewModel {
        SignupViewModel(
            sessionManager = get(),
            registerUseCase = get(),
            getGenresUseCase = get(),
            saveUserGenrePreferenceUseCase = get(),
            uploadProfileImageUseCase = get()
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
            getBookDetailsUseCase = get()
        )
    }

    viewModel {
        ApplicationViewModel(
            getMyApplicationsUseCase = get(),
            applicationsRepository = get()
        )
    }

    viewModel {
        ReviewViewModel(
            reviewsRepository = get(),
            booksRepository = get()
        )
    }

    viewModel {
        ProfileViewModel(
            sessionManager = get(),
            getMyStatsUseCase = get(),
            getAuthorStatsUseCase = get(),
            getMyProfileUseCase = get(),
            getUserProfileUseCase = get(),
            getMyActivityUseCase = get(),
            profileRepository = get(),
            browseBooksUseCase = get(),
            uploadProfileImageUseCase = get()
        )
    }

    viewModel {
        NotificationViewModel(
            notificationsRepository = get(),
            friendsRepository = get(),
            getNotificationsUseCase = get(),
            sessionManager = get()
        )
    }

    viewModel {
        FriendViewModel(
            friendsRepository = get(),
            sessionManager = get()
        )
    }

    viewModel {
        AuthorFollowViewModel(
            authorFollowRepository = get(),
            sessionManager = get()
        )
    }

    viewModel {
        AuthorViewModel(
            booksRepository = get(),
            seriesRepository = get(),
            getMyBooksUseCase = get(),
            getMySeriesUseCase = get(),
            getBookStatsUseCase = get(),
            profileRepository = get(),
            reviewsRepository = get(),
            applicationsRepository = get()
        )
    }

    viewModel {
        AnalyticsViewModel(
            getDetailedBookAnalyticsUseCase = get(),
            getAuthorAnalyticsUseCase = get(),
            booksRepository = get()
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
        FileViewModel(
            context = androidContext(),
            uploadBookFileUseCase = get(),
            getBookDownloadUrlUseCase = get()
        )
    }

    viewModel {
        GoogleAuthViewModel(
            authRepository = get(),
            sessionManager = get()
        )
    }

    viewModel {
        SeriesViewModel(
            getMySeriesUseCase = get(),
            seriesRepository = get()
        )
    }
}
