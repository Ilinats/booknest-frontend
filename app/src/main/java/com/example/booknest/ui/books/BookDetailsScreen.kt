package com.example.booknest.ui.books

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import android.app.Activity
import coil.compose.AsyncImage
import com.example.booknest.port.SessionReader
import com.example.booknest.domain.model.response.ApplicationCheckApplicationResponse
import com.example.booknest.domain.model.response.BookResponse
import com.example.booknest.presentation.navigation.Screen
import com.example.booknest.ui.books.components.application.ApplicationInfoSection
import com.example.booknest.ui.books.components.author.AboutAuthorSection
import com.example.booknest.ui.books.components.details.BookDescriptionSection
import com.example.booknest.ui.books.components.details.GenreTagsSection
import com.example.booknest.ui.books.components.dialogs.ApplicationFormDialog
import com.example.booknest.ui.books.components.dialogs.WithdrawApplicationDialog
import com.example.booknest.ui.books.components.reviews.ReviewsSection
import com.example.booknest.viewmodel.applications.ApplicationViewModel
import com.example.booknest.viewmodel.books.BookViewModel
import com.example.booknest.viewmodel.analytics.ReviewViewModel
import com.example.booknest.ui.components.BackButton
import org.koin.androidx.compose.getViewModel
import org.koin.compose.koinInject
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailsScreen(
    navController: NavController,
    sessionReader: SessionReader = koinInject(),
    bookId: String,
    applicationViewModel: ApplicationViewModel = getViewModel(),
    reviewViewModel: ReviewViewModel = getViewModel(),
    bookViewModel: BookViewModel = getViewModel()
) {
    val book by bookViewModel.bookDetailsScreenBook.collectAsState()
    val isLoading by bookViewModel.bookDetailsScreenLoading.collectAsState()
    var showApplyDialog by remember { mutableStateOf(false) }
    var showWithdrawDialog by remember { mutableStateOf(false) }
    var userApplication by remember { mutableStateOf<ApplicationCheckApplicationResponse?>(null) }
    var isApplying by remember { mutableStateOf(false) }
    val isApplicationLoading by applicationViewModel.isLoading.collectAsState()

    if (bookId.isBlank()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Invalid book ID")
        }
        return
    }

    BookDetailsSystemBarsEffect(
        statusBarColor = MaterialTheme.colorScheme.secondary,
        navigationBarColor = MaterialTheme.colorScheme.background,
    )

    val currentUser by sessionReader.currentUser.collectAsState()

    val books by bookViewModel.books.collectAsState()
    val recommendedBooks by bookViewModel.recommendedBooks.collectAsState()
    val newReleases by bookViewModel.newReleases.collectAsState()
    val homeSearchResults by bookViewModel.homeSearchResults.collectAsState()

    LaunchedEffect(bookId) {
        bookViewModel.beginBookDetailsScreen(bookId)
        reviewViewModel.loadBookReviews(bookId)
    }

    LaunchedEffect(book, currentUser?.id) {
        val currentBook = book
        if (currentBook != null && currentUser?.id != null) {
            val authorId = currentBook.authorId ?: currentBook.author?.id
            val isAuthor = authorId != null && currentUser?.id == authorId
            if (!isAuthor) {
                applicationViewModel.checkApplication(bookId)
            }
        }
    }

    val bookReviews by reviewViewModel.bookReviews.collectAsState()

    val calculatedRating = remember(book?.rating, bookReviews) {
        bookViewModel.calculateRating(book?.rating, bookReviews)
    }

    LaunchedEffect(bookId, books, recommendedBooks, newReleases, homeSearchResults) {
        bookViewModel.refreshBookDetailsScreenFromCache()
    }

    val applicationCheck by applicationViewModel.applicationCheck.collectAsState()
    LaunchedEffect(applicationCheck) {
        val check = applicationCheck
        if (check != null) {
            if (check.hasApplied == true && isApplying) {
                isApplying = false
            }
            userApplication = check.application

            if (isApplying && check.application != null) {
                isApplying = false
            }

            if (book == null && check.application?.book != null) {
                val bookFromApp = check.application.book
                bookViewModel.applyBookDetailsFromApplicationCheck(
                    BookResponse(
                        id = bookFromApp.id,
                        authorId = bookFromApp.authorId,
                        title = bookFromApp.title
                    )
                )
            }
        }
    }

    LaunchedEffect(isApplicationLoading) {
        if (!isApplicationLoading && isApplying) {
            kotlinx.coroutines.delay(500)
            if (userApplication != null) {
                isApplying = false
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.secondary),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (book != null) {
            BookDetailsContent(
                book = book!!,
                calculatedRating = calculatedRating,
                navController = navController,
                sessionReader = sessionReader,
                applicationViewModel = applicationViewModel,
                reviewViewModel = reviewViewModel,
                userApplication = userApplication,
                onApplyClick = { showApplyDialog = true },
                onWithdrawClick = { showWithdrawDialog = true },
                isApplicationLoading = isApplicationLoading,
                isApplying = isApplying,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Book not found")
            }
        }
    }

    if (showApplyDialog && book != null) {
        ApplicationFormDialog(
            book = book!!,
            onDismiss = { showApplyDialog = false },
            onSubmit = { message ->
                isApplying = true
                applicationViewModel.createApplication(book!!.id, message)
                applicationViewModel.checkApplication(bookId)
                showApplyDialog = false
            }
        )
    }

    if (showWithdrawDialog && userApplication != null) {
        WithdrawApplicationDialog(
            book = book!!,
            onDismiss = { showWithdrawDialog = false },
            onConfirm = {
                applicationViewModel.withdrawApplication(userApplication!!.id)
                applicationViewModel.checkApplication(bookId)
                showWithdrawDialog = false
            }
        )
    }
}

@Composable
fun BookDetailsContent(
    book: BookResponse,
    calculatedRating: Double,
    navController: NavController,
    sessionReader: SessionReader,
    applicationViewModel: ApplicationViewModel,
    reviewViewModel: ReviewViewModel,
    userApplication: ApplicationCheckApplicationResponse?,
    onApplyClick: () -> Unit,
    onWithdrawClick: () -> Unit,
    isApplicationLoading: Boolean,
    isApplying: Boolean,
    modifier: Modifier = Modifier
) {
    val bookReviews by reviewViewModel.bookReviews.collectAsState()
    val isLoadingReviews by reviewViewModel.isLoading.collectAsState()
    val currentUser by sessionReader.currentUser.collectAsState()

    val isAuthor = remember(currentUser?.id, book.authorId, book.author?.id) {
        val authorId = book.authorId ?: book.author?.id
        currentUser?.id != null && authorId != null && currentUser?.id == authorId
    }

    val isApplicationDeadlinePassed = remember(book.applicationDeadline) {
        try {
            val deadlineFormat =
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            val deadline = book.applicationDeadline?.let { deadlineFormat.parse(it) }
            val now = Date()
            deadline?.before(now) ?: true
        } catch (e: Exception) {
            true
        }
    }

    val coverWidth = 130.dp
    val coverHeight = 195.dp
    val halfCover = coverHeight / 2
    val pageBackground = MaterialTheme.colorScheme.background
    val heroBlue = MaterialTheme.colorScheme.secondary
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(pageBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(heroBlue)
            ) {
                Column(Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(WindowInsets.statusBars)
                            .padding(start = 4.dp, top = 6.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BackButton(
                            onClick = { navController.popBackStack() },
                            tint = MaterialTheme.colorScheme.onSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = halfCover),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    color = MaterialTheme.colorScheme.background,
                    shadowElevation = 8.dp
                ) {
                    Box(modifier = Modifier.height(halfCover + 32.dp))
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(28.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .width(coverWidth)
                            .height(coverHeight)
                            .shadow(
                                elevation = 12.dp,
                                shape = RoundedCornerShape(12.dp),
                                clip = false
                            )
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        if (book.coverImageUrl != null) {
                            AsyncImage(
                                model = book.coverImageUrl,
                                contentDescription = "Book Cover",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                Icons.Filled.Book,
                                contentDescription = "No cover",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .height(coverHeight),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = book.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                softWrap = true
                            )

                            (book.author?.displayName
                                ?: book.authorName)?.takeIf { it.isNotBlank() && it != "Unknown Author" }
                                ?.let { authorName ->
                                    Text(
                                        text = "by $authorName",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.8f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                            if (book.seriesId != null) {
                                val seriesName = book.seriesName ?: book.series?.name
                                Text(
                                    text = if (seriesName != null) {
                                        "Book ${book.seriesOrder ?: 1} of $seriesName"
                                    } else {
                                        "Book ${book.seriesOrder ?: 1} of Series"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.7f),
                                    modifier = Modifier.clickable {
                                        navController.navigate(
                                            Screen.SeriesBooks.createRoute(
                                                book.seriesId,
                                                seriesName
                                            )
                                        )
                                    }
                                )
                            }

                            book.publishedAt?.let { published ->
                                Text(
                                    text = formatDateDMY(published),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.6f)
                                )
                            }
                        }

                        BookStatsRow(
                            book = book,
                            rating = calculatedRating,
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(y = (-16).dp)
                                .padding(start = 8.dp)
                                .padding(end = 8.dp)
                        )
                    }
                }
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    GenreTagsSection(book = book)

                    BookDescriptionSection(book = book)

                    if (!isAuthor) {
                        ApplicationInfoSection(
                            book = book,
                            userApplication = userApplication,
                            onApplyClick = onApplyClick,
                            onWithdrawClick = onWithdrawClick,
                            showApplyButton = !isApplicationDeadlinePassed && (userApplication == null || userApplication.status == "withdrawn") && !isApplicationLoading && !isApplying,
                            showWithdrawButton = !isApplicationDeadlinePassed && userApplication?.status == "pending",
                            navController = navController,
                            sessionReader = sessionReader
                        )
                    }

                    if (!isAuthor) {
                        AboutAuthorSection(
                            book = book,
                            navController = navController
                        )
                    }

                    ReviewsSection(
                        reviews = bookReviews,
                        isLoading = isLoadingReviews,
                        bookId = book.id
                    )
                }
            }
        }
    }
}

@Composable
fun BookStatsRow(book: BookResponse, rating: Double, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "Rating",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = String.format("%.2f", rating),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Rating",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.MenuBook,
                contentDescription = "Pages",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "${book.pageCount ?: 0}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Pages",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = "Age Rating",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = book.ageRating?.uppercase() ?: "N/A",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Age",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BookDetailsSystemBarsEffect(
    statusBarColor: Color,
    navigationBarColor: Color,
) {
    val view = LocalView.current
    DisposableEffect(statusBarColor, navigationBarColor) {
        val activity = view.context as? Activity
        val window = activity?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        val prevLightStatus = controller?.isAppearanceLightStatusBars
        val prevLightNav = controller?.isAppearanceLightNavigationBars
        val prevStatusBarColor = window?.statusBarColor
        val prevNavBarColor = window?.navigationBarColor
        if (controller != null && window != null) {
            window.statusBarColor = statusBarColor.toArgb()
            window.navigationBarColor = navigationBarColor.toArgb()
            controller.isAppearanceLightStatusBars = false
            controller.isAppearanceLightNavigationBars = true
        }
        onDispose {
            if (window != null) {
                window.statusBarColor = prevStatusBarColor ?: Color.Transparent.toArgb()
                window.navigationBarColor = prevNavBarColor ?: Color.Transparent.toArgb()
            }
            if (controller != null && prevLightStatus != null) {
                controller.isAppearanceLightStatusBars = prevLightStatus
            }
            if (controller != null && prevLightNav != null) {
                controller.isAppearanceLightNavigationBars = prevLightNav
            }
        }
    }
}

private fun formatDateDMY(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateString
    }
}
