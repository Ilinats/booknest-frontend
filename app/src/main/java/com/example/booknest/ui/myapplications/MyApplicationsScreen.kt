package com.example.booknest.ui.myapplications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.draw.shadow
import com.example.booknest.ui.testing.UiTestTags
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.booknest.data.session.SessionManager
import com.example.booknest.ui.components.BackgroundDecoration
import com.example.booknest.viewmodel.applications.ApplicationViewModel
import com.example.booknest.viewmodel.applications.ApplicationSortOption
import com.example.booknest.viewmodel.files.FileViewModel
import org.koin.androidx.compose.getViewModel
import org.koin.compose.koinInject
import com.example.booknest.ui.myapplications.components.stats.QuickStatsSummary
import com.example.booknest.ui.myapplications.components.common.EmptyApplicationsState
import com.example.booknest.ui.myapplications.components.content.AllApplicationsContent
import com.example.booknest.ui.myapplications.components.content.PendingApplicationsContent
import com.example.booknest.ui.myapplications.components.content.ApprovedApplicationsContent
import com.example.booknest.ui.myapplications.components.content.CompletedApplicationsContent
import com.example.booknest.ui.myapplications.components.content.RejectedApplicationsContent
import com.example.booknest.ui.myapplications.components.info.ApprovedTabInfoCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyApplicationsScreen(
    navController: NavController,
    sessionManager: SessionManager = koinInject(),
    applicationViewModel: ApplicationViewModel = getViewModel(),
    fileViewModel: FileViewModel = getViewModel()
) {
    val isLoading by applicationViewModel.isLoading.collectAsState()
    val filteredApplications by applicationViewModel.filteredApplications.collectAsState()
    val approvedApplications by applicationViewModel.approvedApplicationsBySub.collectAsState()
    val stats by applicationViewModel.applicationStats.collectAsState()
    val tabCounts by applicationViewModel.tabCounts.collectAsState()
    val searchQuery by applicationViewModel.searchQuery.collectAsState()
    val selectedTab by applicationViewModel.selectedTab.collectAsState()
    val sortOption by applicationViewModel.sortOption.collectAsState()
    val fileUiState by fileViewModel.uiState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var showSortMenu by remember { mutableStateOf(false) }
    val tabs = listOf("All", "Pending", "Approved", "Completed", "Rejected")

    LaunchedEffect(Unit) {
        applicationViewModel.loadMyApplications()
    }

    LaunchedEffect(fileUiState.error) {
        fileUiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            fileViewModel.clearError()
        }
    }

    LaunchedEffect(fileUiState.successMessage) {
        fileUiState.successMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            fileViewModel.clearSuccessMessage()
            fileViewModel.clearDownloadingMessage()
        }
    }

    LaunchedEffect(fileUiState.downloadingMessage) {
        fileUiState.downloadingMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        BackgroundDecoration(modifier = Modifier.fillMaxSize())

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {},
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { QuickStatsSummary(stats = stats) }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { applicationViewModel.updateSearchQuery(it) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag(UiTestTags.APPLICATIONS_SEARCH_FIELD)
                                .shadow(elevation = 2.dp, shape = RoundedCornerShape(28.dp)),
                            placeholder = { Text("Search applications...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { applicationViewModel.updateSearchQuery("") }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Clear")
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(28.dp),
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent
                            )
                        )
                        Box {
                            IconButton(
                                onClick = { showSortMenu = true },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Sort,
                                    contentDescription = "Sort applications"
                                )
                            }
                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("By Application Date") },
                                    onClick = {
                                        applicationViewModel.updateSortOption(ApplicationSortOption.APPLICATION_DATE)
                                        showSortMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("By Deadline") },
                                    onClick = {
                                        applicationViewModel.updateSortOption(ApplicationSortOption.DEADLINE)
                                        showSortMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("By Status") },
                                    onClick = {
                                        applicationViewModel.updateSortOption(ApplicationSortOption.STATUS)
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                item {
                    ScrollableTabRow(
                        selectedTabIndex = selectedTab,
                        modifier = Modifier.fillMaxWidth(),
                        edgePadding = 0.dp
                    ) {
                        tabs.forEachIndexed { index, title ->
                            val count = tabCounts[index] ?: 0
                            Tab(
                                selected = selectedTab == index,
                                onClick = { applicationViewModel.updateSelectedTab(index) },
                                modifier = Modifier.testTag("${UiTestTags.APPLICATIONS_TAB_PREFIX}$index"),
                                text = {
                                    Text(
                                        if (count > 0) "$title ($count)" else title,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            )
                        }
                    }
                }

                if (selectedTab == 0 && filteredApplications.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                Text(
                                    text = "For detailed information and actions, check the Pending, Approved, Completed, and Rejected tabs.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                if (isLoading && filteredApplications.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(400.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                } else if (selectedTab == 2 && approvedApplications.first.isEmpty() && approvedApplications.second.isEmpty()) {
                    item {
                        EmptyApplicationsState(
                            modifier = Modifier.fillMaxWidth().height(400.dp),
                            message = "No approved applications"
                        )
                    }
                } else if (selectedTab != 2 && filteredApplications.isEmpty()) {
                    item {
                        EmptyApplicationsState(
                            modifier = Modifier.fillMaxWidth().height(400.dp),
                            message = when (selectedTab) {
                                0 -> "No applications yet"
                                1 -> "No pending applications"
                                3 -> "No completed applications"
                                4 -> "No rejected applications"
                                else -> "No applications"
                            }
                        )
                    }
                } else {
                    when (selectedTab) {
                        0 -> AllApplicationsContent(
                            applications = filteredApplications,
                            applicationViewModel = applicationViewModel,
                            fileViewModel = fileViewModel,
                            navController = navController
                        )
                        1 -> PendingApplicationsContent(
                            applications = filteredApplications,
                            applicationViewModel = applicationViewModel,
                            navController = navController
                        )
                        2 -> ApprovedApplicationsContent(
                            awaitingCopy = approvedApplications.first,
                            reading = approvedApplications.second,
                            applicationViewModel = applicationViewModel,
                            fileViewModel = fileViewModel,
                            navController = navController
                        )
                        3 -> CompletedApplicationsContent(
                            applications = filteredApplications,
                            navController = navController,
                            fileViewModel = fileViewModel
                        )
                        4 -> RejectedApplicationsContent(
                            applications = filteredApplications,
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}
