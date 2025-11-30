package com.example.booknest.ui.author

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import com.example.booknest.ui.components.BackButton
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.booknest.data.AuthManager
import com.example.booknest.network.Book
import com.example.booknest.network.BookStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthorProfileScreen(
    navController: NavController,
    authManager: AuthManager
) {
    val currentUser by authManager.currentUser.collectAsState()
    
    // Hardcoded data for now as requested
    val authorName = "Ethan Carter"
    val authorBio = "Ethan Carter is a bestselling author known for his gripping thrillers and suspense novels. His work has been praised for its intricate plots and compelling characters."
    val joinYear = "2021"
    val campaigns = 5
    val reviews = 120
    val followers = 350
    val successRate = 95
    
    // Hardcoded books for portfolio
    val portfolioBooks = listOf(
        Book(
            id = "1",
            authorId = "author1",
            title = "The Silent Echo",
            shortDescription = "A gripping thriller",
            fullDescription = null,
            coverImageUrl = null,
            pageCount = null,
            ageRating = "all",
            distributionType = "digital",
            fileUrl = null,
            fileSize = null,
            fileType = null,
            totalCopies = 100,
            availableCopies = 50,
            applicationDeadline = "",
            reviewDeadlineDays = 30,
            selectionCriteria = null,
            selectionMethod = com.example.booknest.network.SelectionMethod.FIRST_COME,
            status = BookStatus.ACTIVE,
            createdAt = "",
            updatedAt = "",
            publishedAt = null,
            seriesId = null,
            seriesOrder = null,
            seriesName = null
        ),
        Book(
            id = "2",
            authorId = "author1",
            title = "Whispers of the Past",
            shortDescription = "A suspense novel",
            fullDescription = null,
            coverImageUrl = null,
            pageCount = null,
            ageRating = "all",
            distributionType = "digital",
            fileUrl = null,
            fileSize = null,
            fileType = null,
            totalCopies = 100,
            availableCopies = 30,
            applicationDeadline = "",
            reviewDeadlineDays = 30,
            selectionCriteria = null,
            selectionMethod = com.example.booknest.network.SelectionMethod.FIRST_COME,
            status = BookStatus.ACTIVE,
            createdAt = "",
            updatedAt = "",
            publishedAt = null,
            seriesId = null,
            seriesOrder = null,
            seriesName = null
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Author Profile",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ) 
                },
                navigationIcon = {
                    BackButton(onClick = { navController.popBackStack() })
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Author Profile Section
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Profile Picture
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8D5B7)), // Light beige background
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.size(60.dp),
                            tint = Color(0xFF8B4513) // Brown color
                        )
                    }

                    // Author Info
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = authorName,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Author of 'The Silent Echo'",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Joined $joinYear",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Action Buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { 
                                currentUser?.id?.let { userId ->
                                    navController.navigate("profile/$userId")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF424242)
                            )
                        ) {
                            Text("View Profile")
                        }
                        
                        OutlinedButton(
                            onClick = { 
                                currentUser?.id?.let { userId ->
                                    navController.navigate("stats/$userId")
                                }
                            }
                        ) {
                            Text("View Stats")
                        }
                    }
                }
            }

            // Statistics Section
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Top row stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AuthorStatCard(
                            value = campaigns.toString(),
                            label = "Campaigns",
                            modifier = Modifier.weight(1f)
                        )
                        AuthorStatCard(
                            value = reviews.toString(),
                            label = "Reviews",
                            modifier = Modifier.weight(1f)
                        )
                        AuthorStatCard(
                            value = followers.toString(),
                            label = "Followers",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    // Success Rate
                    AuthorStatCard(
                        value = "$successRate%",
                        label = "Success Rate",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Biography Section
            item {
                Column {
                    Text(
                        text = "About",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = authorBio,
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 24.sp
                    )
                }
            }

            // Portfolio Section
            item {
                Column {
                    Text(
                        text = "Portfolio",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(portfolioBooks) { book ->
                            BookCoverCard(book = book)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AuthorStatCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF424242)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun BookCoverCard(book: Book) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Book cover placeholder
        Box(
            modifier = Modifier
                .size(80.dp, 120.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    when (book.title) {
                        "The Silent Echo" -> Color(0xFFF5F5DC) // Cream color
                        "Whispers of the Past" -> Color(0xFFFFFFFF) // White
                        else -> Color(0xFFF5F5DC) // Default cream
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (book.title == "Whispers of the Past") {
                // Green leaf icon for the second book
                Icon(
                    Icons.Filled.Star,
                    contentDescription = "Book Cover",
                    modifier = Modifier.size(32.dp),
                    tint = Color(0xFF4CAF50) // Green color
                )
            } else {
                // Text for the first book
                Text(
                    text = "The Silent Echo",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
        
        Text(
            text = book.title,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            maxLines = 2
        )
    }
}