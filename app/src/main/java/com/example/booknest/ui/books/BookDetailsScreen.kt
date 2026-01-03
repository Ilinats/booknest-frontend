package com.example.booknest.ui.books

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.booknest.data.session.SessionManager
import com.example.booknest.domain.model.response.ApplicationCheckApplicationResponse
import com.example.booknest.domain.model.response.ApplicationResponse
import com.example.booknest.domain.model.response.BookResponse
import com.example.booknest.domain.model.response.RecommendedBookResponse
import com.example.booknest.domain.model.response.ReviewResponse
import com.example.booknest.navigation.Screen
import com.example.booknest.viewmodel.ApplicationViewModel
import com.example.booknest.viewmodel.AuthorFollowViewModel
import com.example.booknest.viewmodel.BookViewModel
import com.example.booknest.viewmodel.ProfileViewModel
import com.example.booknest.viewmodel.ReviewType
import com.example.booknest.viewmodel.ReviewViewModel
import com.example.booknest.ui.components.ReviewLinkPreview
import com.example.booknest.ui.components.BackButton
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel
import org.koin.compose.koinInject
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailsScreen(
    navController: NavController,
    sessionManager: SessionManager = koinInject(),
    bookId: String,
    applicationViewModel: ApplicationViewModel = getViewModel(),
    reviewViewModel: ReviewViewModel = getViewModel(),
    bookViewModel: BookViewModel = getViewModel()
) {
    var book by remember { mutableStateOf<BookResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
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

    val currentUser by sessionManager.currentUser.collectAsState()

    LaunchedEffect(bookId) {
        println("DEBUG: BookDetailsScreen - bookId: $bookId")

        val allBooks = bookViewModel.books.value +
                bookViewModel.featuredBooks.value +
                bookViewModel.recommendedBooks.value +
                bookViewModel.newReleases.value +
                bookViewModel.homeSearchResults.value

        val cachedBook = allBooks.find { it.id == bookId }
        if (cachedBook != null) {
            println("DEBUG: Found book in cache immediately, using cached data")
            book = BookResponse(
                id = cachedBook.id,
                title = cachedBook.title,
                authorName = cachedBook.resolvedAuthorName,
                coverImageUrl = cachedBook.coverImageUrl,
                rating = cachedBook.rating,
                seriesName = cachedBook.seriesName,
                seriesOrder = cachedBook.seriesOrder,
                publishedAt = cachedBook.publishedAt,
                applicationDeadline = cachedBook.applicationDeadline,
                availableCopies = cachedBook.availableCopies,
                totalCopies = cachedBook.totalCopies,
                genres = cachedBook.genres,
                distributionType = cachedBook.distributionType,
                author = cachedBook.author
            )
            isLoading = false
        }

        try {
            bookViewModel.getBookDetails(bookId)
        } catch (e: Exception) {
            println("DEBUG: Book details API failed: ${e.message}")
            e.printStackTrace()
        }

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

    val bookDetails by bookViewModel.bookDetails.collectAsState()
    LaunchedEffect(bookDetails) {
        bookDetails?.let { details ->
            if (book == null || (details.fullDescription != null && book?.fullDescription == null)) {
                book = details
                isLoading = false
            }
        }
    }

    LaunchedEffect(
        bookViewModel.books,
        bookViewModel.featuredBooks,
        bookViewModel.recommendedBooks,
        bookViewModel.newReleases,
        bookViewModel.homeSearchResults
    ) {
        if (book == null) {
            println("DEBUG: Trying to find book in main screen data")

            val allBooks = bookViewModel.books.value +
                    bookViewModel.featuredBooks.value +
                    bookViewModel.recommendedBooks.value +
                    bookViewModel.newReleases.value +
                    bookViewModel.homeSearchResults.value

            println("DEBUG: Total books available: ${allBooks.size}")
            println("DEBUG: Looking for bookId: $bookId")

            val foundBook = allBooks.find { it.id == bookId }
            if (foundBook != null) {
                println("DEBUG: Found book in main screen data: $foundBook")
                book = BookResponse(
                    id = foundBook.id,
                    title = foundBook.title,
                    authorName = foundBook.resolvedAuthorName,
                    coverImageUrl = foundBook.coverImageUrl,
                    rating = foundBook.rating,
                    seriesName = foundBook.seriesName,
                    seriesOrder = foundBook.seriesOrder,
                    publishedAt = foundBook.publishedAt,
                    applicationDeadline = foundBook.applicationDeadline,
                    availableCopies = foundBook.availableCopies,
                    totalCopies = foundBook.totalCopies,
                    genres = foundBook.genres,
                    distributionType = foundBook.distributionType,
                    author = foundBook.author
                )
                isLoading = false
            } else {
                println("DEBUG: Book not found in main screen data")
            }
        }
    }

    val applicationCheck by applicationViewModel.applicationCheck.collectAsState()
    LaunchedEffect(applicationCheck) {
        val check = applicationCheck
        if (check != null) {
            if (check.hasApplied == true && isApplying) {
                isApplying = false
            }
            val previousApplication = userApplication
            userApplication = check.application

            if (isApplying && check.application != null) {
                isApplying = false
            }

            if (book == null && check.application?.book != null) {
                val bookFromApp = check.application.book
                println("DEBUG: Creating fallback book from application check: $bookFromApp")
                book = BookResponse(
                    id = bookFromApp.id,
                    authorId = bookFromApp.authorId,
                    title = bookFromApp.title
                )
                println("DEBUG: Application check fallback book created: $book")
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

    Scaffold(
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (book != null) {
            BookDetailsContent(
                book = book!!,
                navController = navController,
                sessionManager = sessionManager,
                applicationViewModel = applicationViewModel,
                reviewViewModel = reviewViewModel,
                userApplication = userApplication,
                onApplyClick = { showApplyDialog = true },
                onWithdrawClick = { showWithdrawDialog = true },
                isApplicationLoading = isApplicationLoading,
                isApplying = isApplying,
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
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
    navController: NavController,
    sessionManager: SessionManager,
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
    val currentUser by sessionManager.currentUser.collectAsState()

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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.secondary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
            ) {
                BackButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(y = (-16).dp)
                                .padding(start = 8.dp)
                                .padding(end = 8.dp)
                        )
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
                            sessionManager = sessionManager
                        )
                    }

                    if (!isAuthor) {
                        AboutAuthorSection(
                            book = book,
                            navController = navController,
                            sessionManager = sessionManager
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
fun BookStatsRow(book: BookResponse, modifier: Modifier = Modifier) {
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
                text = String.format("%.1f", book.rating ?: 0.0),
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
fun GenreTagsSection(book: BookResponse) {
    val genresToShow = book.resolvedGenres.take(3)
    if (genresToShow.isNotEmpty()) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(genresToShow.size) { index ->
                val genre = genresToShow[index]
                GenreTag(
                    text = genre.name,
                    isPrimary = true
                )
            }
        }
    }
}

@Composable
fun GenreTag(
    text: String,
    isPrimary: Boolean
) {
    Box(
        modifier = Modifier
            .background(
                if (isPrimary) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = if (isPrimary) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

@Composable
fun BookDescriptionSection(book: BookResponse) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "Description",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = book.fullDescription ?: book.shortDescription ?: "No description available.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.2
        )
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

@Composable
fun MetadataRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ApplicationInfoSection(
    book: BookResponse,
    userApplication: ApplicationCheckApplicationResponse?,
    onApplyClick: () -> Unit,
    onWithdrawClick: () -> Unit,
    showApplyButton: Boolean,
    showWithdrawButton: Boolean,
    navController: NavController? = null,
    sessionManager: SessionManager? = null,
    applicationViewModel: ApplicationViewModel = getViewModel()
) {
    val profileViewModel: ProfileViewModel = getViewModel()
    val myProfile by profileViewModel.myProfile.collectAsState()
    val addresses by profileViewModel.addresses.collectAsState()
    val currentUser = if (sessionManager != null) {
        val user by sessionManager.currentUser.collectAsState()
        user
    } else {
        null
    }

    val requiresPhysicalCopy = book.distributionType?.lowercase() in listOf("physical", "both")

    val isEmailVerified = currentUser?.emailVerified == true

    LaunchedEffect(requiresPhysicalCopy, profileViewModel) {
        if (requiresPhysicalCopy) {
            if (myProfile == null) {
                profileViewModel.loadMyProfile()
            }
            profileViewModel.loadAddresses()
        }
    }

    val hasAddresses = (myProfile?.addresses?.isNotEmpty() == true) || addresses.isNotEmpty()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Slots Filled: ${(book.totalCopies ?: 0) - (book.availableCopies ?: 0)}/${book.totalCopies ?: 0}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Application Deadline: ${book.applicationDeadline?.let { formatDate(it) } ?: "Not specified"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = if (book.reviewDeadline != null) {
                        "Review Deadline: ${book.reviewDeadline?.let { formatDate(it) }}"
                    } else {
                        "Review Deadline: Not specified"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (book.distributionType != null || book.selectionMethod != null || book.status != null) {
                    Divider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )

                    book.distributionType?.let {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Distribution",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = it.replaceFirstChar { char -> char.uppercase() },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    book.selectionMethod?.takeIf { it.isNotBlank() }?.let {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Selection Method",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = it.replaceFirstChar { char -> char.uppercase() },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    book.status?.takeIf { it.isNotBlank() }?.let {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Status",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = it.replaceFirstChar { char -> char.uppercase() },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        LaunchedEffect(userApplication, showWithdrawButton, showApplyButton) {
            println("DEBUG ApplicationInfoSection: userApplication?.status = ${userApplication?.status}")
            println("DEBUG ApplicationInfoSection: showWithdrawButton = $showWithdrawButton")
            println("DEBUG ApplicationInfoSection: showApplyButton = $showApplyButton")
        }

        when {
            userApplication?.status == "approved" -> {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "✅ Application Approved! Check your books for the copy.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            userApplication?.status == "rejected" -> {
                Text(
                    text = "❌ Application Rejected",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            userApplication?.status == "withdrawn" -> {
                Text(
                    text = "Application Withdrawn",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            showWithdrawButton -> {
                OutlinedButton(
                    onClick = onWithdrawClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Withdraw Application")
                }
            }

            showApplyButton -> {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!isEmailVerified) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Warning,
                                        contentDescription = "Warning",
                                        tint = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Text(
                                        text = "Email verification required to apply for books. Please verify your email address first.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                                if (navController != null) {
                                    Button(
                                        onClick = {
                                            navController.navigate(
                                                Screen.EmailVerification.createRoute(
                                                    currentUser?.email
                                                )
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.error
                                        )
                                    ) {
                                        Text("Verify Email")
                                    }
                                }
                            }
                        }
                    }

                    if (requiresPhysicalCopy && !hasAddresses) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Warning,
                                        contentDescription = "Warning",
                                        tint = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Text(
                                        text = "A shipping address is required to apply for physical copies.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                                if (navController != null) {
                                    Button(
                                        onClick = {
                                            navController.navigate(Screen.PrivacySettings.route)
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.error
                                        )
                                    ) {
                                        Text("Add Address")
                                    }
                                }
                            }
                        }
                    }

                    val isApplicationLoading by applicationViewModel.isLoading.collectAsState()

                    Button(
                        onClick = onApplyClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = isEmailVerified && (!requiresPhysicalCopy || hasAddresses) && !isApplicationLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        if (isApplicationLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(
                                text = if (userApplication?.status == "approved") "Read Now" else "Apply",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AboutAuthorSection(
    book: BookResponse,
    navController: NavController,
    sessionManager: SessionManager
) {
    val authorFollowViewModel: AuthorFollowViewModel = getViewModel()

    var isFollowing by remember { mutableStateOf<Boolean?>(null) }
    val scope = rememberCoroutineScope()
    val loadingAuthors by authorFollowViewModel.loadingAuthors.collectAsState()

    val authorId = book.resolvedAuthorId
    val authorUsername = book.author?.username
    val isAuthorLoading = authorId != null && loadingAuthors.contains(authorId)

    LaunchedEffect(authorId) {
        isFollowing = false
        if (authorId != null) {
            authorFollowViewModel.checkIfFollowingAuthor(authorId) { following ->
                isFollowing = following
            }
        }
    }

    LaunchedEffect(Unit) {
        authorFollowViewModel.error.collectLatest { error ->
            error?.let {
                if (authorId != null) {
                    authorFollowViewModel.checkIfFollowingAuthor(authorId) { following ->
                        isFollowing = following
                    }
                }
            }
        }
    }

    LaunchedEffect(isAuthorLoading) {
        if (!isAuthorLoading && authorId != null) {
            kotlinx.coroutines.delay(300)
            authorFollowViewModel.checkIfFollowingAuthor(authorId) { following ->
                isFollowing = following
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "About the Author",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                book.author?.avatarUrl?.let { profileUrl ->
                    AsyncImage(
                        model = profileUrl,
                        contentDescription = book.author?.displayName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } ?: Text(
                    text = book.author?.displayName?.firstOrNull()?.uppercase() ?: "?",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = book.author?.displayName ?: book.authorName ?: "Unknown Author",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Author of ${book.title}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (authorId != null) {
                OutlinedButton(
                    onClick = {
                        val wasFollowing = isFollowing == true
                        isFollowing = !wasFollowing
                        if (wasFollowing) {
                            authorFollowViewModel.unfollowAuthor(authorId)
                        } else {
                            authorFollowViewModel.followAuthor(authorId)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = isFollowing != null && !isAuthorLoading
                ) {
                    if (isAuthorLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(if (isFollowing == true) "Unfollow" else "Follow Author")
                }
            }

            OutlinedButton(
                onClick = {
                    authorUsername?.let {
                        navController.navigate("profile/$it")
                    } ?: run {
                        println("DEBUG: Cannot navigate to author profile - username is missing")
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = authorUsername != null
            ) {
                Text("View Profile")
            }
        }
    }
}

@Composable
fun ReviewsSection(
    reviews: List<ReviewResponse>,
    isLoading: Boolean,
    bookId: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Reviews",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (reviews.isEmpty()) {
            Text(
                text = "No reviews yet. Be the first to review this book!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                reviews.take(3).forEach { review ->
                    ReviewCard(review = review)
                }

                if (reviews.size > 3) {
                    TextButton(
                        onClick = { }
                    ) {
                        Text("View All Reviews (${reviews.size})")
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewCard(review: ReviewResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val reviewer = review.application?.reader
                    val reviewerInitial = reviewer?.username?.firstOrNull()?.uppercase() ?: "R"
                    val reviewerName = reviewer?.username
                        ?: "${reviewer?.firstName ?: ""} ${reviewer?.lastName ?: ""}".trim()
                            .takeIf { it.isNotBlank() }
                        ?: "Reviewer"

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        reviewer?.profilePictureUrl?.let { profileUrl ->
                            AsyncImage(
                                model = profileUrl,
                                contentDescription = reviewerName,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } ?: Text(
                            text = reviewerInitial,
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = reviewerName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = formatDate(review.createdAt),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row {
                    (1..5).forEach { star ->
                        Icon(
                            imageVector = if (star <= review.rating) Icons.Filled.Star else Icons.Filled.Star,
                            contentDescription = "$star stars",
                            modifier = Modifier.size(16.dp),
                            tint = if (star <= review.rating) Color.Yellow else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            val reviewType = review.reviewType
            when (reviewType) {
                ReviewType.TEXT.value -> {
                    review.reviewContent?.let { content ->
                        Text(
                            text = content,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                ReviewType.LINK.value -> {
                    review.reviewUrls?.forEach { url ->
                        if (url.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            ReviewLinkPreview(
                                url = url,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            if (review.reviewContent != null && review.reviewUrls != null && review.reviewUrls.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                review.reviewUrls.forEach { url ->
                    if (url.isNotBlank()) {
                        ReviewLinkPreview(
                            url = url,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ApplicationFormDialog(
    book: BookResponse,
    onDismiss: () -> Unit,
    onSubmit: (String?) -> Unit
) {
    var message by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Apply for Review Copy") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("You are applying to review \"${book.title}\"")

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Why do you want to review this book? (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSubmit(message.ifBlank { null }) }) {
                Text("Submit Application")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun WithdrawApplicationDialog(
    book: BookResponse,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Withdraw Application") },
        text = {
            Text("Are you sure you want to withdraw your application for \"${book.title}\"? This action cannot be undone.")
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Withdraw")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateString
    }
}

