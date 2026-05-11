package com.example.booknest.ui.books.components.author

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.booknest.data.session.SessionManager
import com.example.booknest.domain.model.response.BookResponse
import com.example.booknest.viewmodel.author.AuthorFollowViewModel
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.getViewModel

@Composable
fun AboutAuthorSection(
    book: BookResponse,
    navController: NavController,
    sessionManager: SessionManager
) {
    val authorFollowViewModel: AuthorFollowViewModel = getViewModel()

    val loadingAuthors by authorFollowViewModel.loadingAuthors.collectAsState()
    val followingStatus by authorFollowViewModel.followingStatus.collectAsState()

    val authorId = book.resolvedAuthorId
    val authorUsername = book.author?.username
    val isAuthorLoading = authorId != null && loadingAuthors.contains(authorId)

    val isFollowing = authorId?.let { followingStatus[it] } ?: false

    LaunchedEffect(authorId) {
        if (authorId != null) {
            authorFollowViewModel.checkIfFollowingAuthor(authorId)
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
                        navController.navigate(com.example.booknest.navigation.Screen.Profile.createRoute(it))
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

