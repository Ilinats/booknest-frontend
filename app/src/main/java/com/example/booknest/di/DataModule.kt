package com.example.booknest.di

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
import com.example.booknest.port.AuthTokenAccessor
import com.example.booknest.port.SessionWriter
import com.example.booknest.viewmodel.author.AuthorBooksCatalogRefresher
import com.example.booknest.viewmodel.books.BookCatalogCache
import com.example.booknest.viewmodel.profile.ProfileRefreshBus
import org.koin.dsl.module

val dataModule = module {
    single { BookCatalogCache() }
    single { AuthorBooksCatalogRefresher() }
    single { ProfileRefreshBus() }

    single<AuthDataSource> { BNAuthDataSource(get(), get(), get<SessionWriter>()) }
    single<BooksDataSource> { BNBooksDataSource(get()) }
    single<ApplicationsDataSource> { BNApplicationsDataSource(get()) }
    single<ReviewsDataSource> { BNReviewsDataSource(get()) }
    single<GenresDataSource> { BNGenresDataSource(get()) }
    single<ProfilesDataSource> { BNProfilesDataSource(get()) }
    single<FriendsDataSource> { BNFriendsDataSource(get()) }
    single<AuthorsDataSource> { BNAuthorsDataSource(get()) }
    single<NotificationsDataSource> { BNNotificationsDataSource(get()) }
    single<SeriesDataSource> { BNSeriesDataSource(get()) }

    single<AuthRepository> { BNAuthRepository(get(), get<AuthTokenAccessor>()) }
    single<BooksRepository> { BNBooksRepository(get()) }
    single<ApplicationsRepository> { BNApplicationsRepository(get()) }
    single<ReviewsRepository> { BNReviewsRepository(get()) }
    single<GenresRepository> { BNGenresRepository(get()) }
    single<ProfileRepository> { BNProfileRepository(get()) }
    single<FriendsRepository> { BNFriendsRepository(get()) }
    single<AuthorFollowRepository> { BNAuthorFollowRepository(get()) }
    single<NotificationsRepository> { BNNotificationsRepository(get()) }
    single<SeriesRepository> { BNSeriesRepository(get()) }
}
