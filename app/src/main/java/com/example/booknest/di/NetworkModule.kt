package com.example.booknest.di

import coil.ImageLoader
import com.example.booknest.BuildConfig
import com.example.booknest.data.constants.MediaType
import com.example.booknest.data.constants.RetrofitConstants
import com.example.booknest.data.session.TokenAuthenticator
import com.example.booknest.data.session.TokenInterceptor
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
import com.example.booknest.domain.repository.AuthRepository
import com.example.booknest.domain.usecase.auth.RefreshTokenUseCase
import com.example.booknest.port.AuthTokenAccessor
import com.example.booknest.port.SessionWriter
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

val networkModule = module {
    factory { RefreshTokenUseCase(authRepository = lazy { get<AuthRepository>() }) }

    single {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level =
            if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE

        OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .addInterceptor(TokenInterceptor(get<AuthTokenAccessor>()))
            .authenticator(TokenAuthenticator(get<RefreshTokenUseCase>(), get<SessionWriter>()))
            .connectTimeout(RetrofitConstants.TIME, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
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
            isLenient = true
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
}
