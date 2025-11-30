package com.example.booknest.ui.author

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import com.example.booknest.ui.components.BackButton
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.booknest.data.AuthManager
import com.example.booknest.network.AgeRating
import com.example.booknest.network.CreateBookDto
import com.example.booknest.network.DistributionType
import com.example.booknest.network.GenreDto
import com.example.booknest.network.RetrofitInstance
import com.example.booknest.network.SelectionMethod
import com.example.booknest.network.Series
import com.example.booknest.viewmodel.AuthorViewModel
import com.example.booknest.viewmodel.AuthorViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookCreationWizard(
    navController: NavController,
    authManager: AuthManager,
    authorViewModel: AuthorViewModel = viewModel(
        factory = AuthorViewModelFactory(authManager)
    )
) {
    var currentStep by remember { mutableStateOf(1) }
    val totalSteps = 5
    
    // Form state
    var title by remember { mutableStateOf("") }
    var shortDescription by remember { mutableStateOf("") }
    var fullDescription by remember { mutableStateOf("") }
    var pageCount by remember { mutableStateOf("") }
    var selectedAgeRating by remember { mutableStateOf<AgeRating?>(null) }
    var selectedDistributionType by remember { mutableStateOf<DistributionType?>(null) }
    var totalCopies by remember { mutableStateOf("") }
    var applicationDeadline by remember { mutableStateOf("") }
    var reviewDeadlineDays by remember { mutableStateOf("") }
    var selectedSelectionMethod by remember { mutableStateOf<SelectionMethod?>(null) }
    var selectionCriteria by remember { mutableStateOf("") }
    var selectedGenres by remember { mutableStateOf<List<Int>>(emptyList()) }
    var selectedSeries by remember { mutableStateOf<Series?>(null) }
    var seriesOrder by remember { mutableStateOf("") }
    
    var isCreating by remember { mutableStateOf(false) }
    var showCreateSeriesDialog by remember { mutableStateOf(false) }
    
    val mySeries by authorViewModel.mySeries.collectAsState()
    var genres by remember { mutableStateOf(listOf<GenreDto>()) }
    
    LaunchedEffect(Unit) {
        authorViewModel.loadMySeries()
        try {
            val response = RetrofitInstance.api.getGenres()
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success) {
                    genres = apiResponse.data ?: emptyList()
                    println("Genres loaded successfully: ${apiResponse.data?.size ?: 0} genres")
                } else {
                    println("Genres API error: ${apiResponse.message}")
                    genres = emptyList()
                }
            } else {
                println("Genres API error: ${response.code()} - ${response.message()}")
                genres = emptyList()
            }
        } catch (e: Exception) {
            println("Error loading genres: ${e.message}")
            genres = emptyList()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Create Book (Step $currentStep of $totalSteps)",
                        fontWeight = FontWeight.Bold
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Progress indicator
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    repeat(totalSteps) { step ->
                        val stepNumber = step + 1
                        val isCompleted = stepNumber < currentStep
                        val isCurrent = stepNumber == currentStep
                        
                        Card(
                            modifier = Modifier
                                .width(60.dp)
                                .height(40.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    isCompleted -> MaterialTheme.colorScheme.primary
                                    isCurrent -> MaterialTheme.colorScheme.primaryContainer
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isCompleted) {
                                    Icon(
                                        Icons.Filled.Check,
                                        contentDescription = "Completed",
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                } else {
                                    Text(
                                        text = stepNumber.toString(),
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCurrent) MaterialTheme.colorScheme.onPrimaryContainer 
                                               else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Step content
            when (currentStep) {
                1 -> {
                    item { BasicInfoStep(title, shortDescription, fullDescription, pageCount) { t, sd, fd, pc ->
                        title = t
                        shortDescription = sd
                        fullDescription = fd
                        pageCount = pc
                    }}
                }
                2 -> {
                    item { GenresAndSeriesStep(
                        selectedGenres, selectedSeries, seriesOrder, mySeries, genres,
                        onUpdate = { sg, ss, so ->
                            selectedGenres = sg
                            selectedSeries = ss
                            seriesOrder = so
                        },
                        onCreateSeries = { name: String, description: String ->
                            authorViewModel.createSeries(
                                com.example.booknest.network.CreateSeriesDto(
                                    name = name,
                                    description = description.ifBlank { null }
                                )
                            )
                            showCreateSeriesDialog = false
                        },
                        onShowCreateSeriesDialog = { showCreateSeriesDialog = true },
                        showCreateSeriesDialog = showCreateSeriesDialog,
                        onDismissCreateSeriesDialog = { showCreateSeriesDialog = false }
                    )}
                }
                3 -> {
                    item { DistributionStep(selectedAgeRating, selectedDistributionType, totalCopies) { ar, dt, tc ->
                        selectedAgeRating = ar
                        selectedDistributionType = dt
                        totalCopies = tc
                    }}
                }
                4 -> {
                    item { ReviewConfigStep(applicationDeadline, reviewDeadlineDays, selectedSelectionMethod, selectionCriteria) { ad, rdd, ssm, sc ->
                        applicationDeadline = ad
                        reviewDeadlineDays = rdd
                        selectedSelectionMethod = ssm
                        selectionCriteria = sc
                    }}
                }
                5 -> {
                    item { PreviewStep(
                        title, shortDescription, selectedAgeRating, selectedDistributionType,
                        selectedGenres, selectedSeries, applicationDeadline
                    )}
                }
            }
            
            // Navigation buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = { if (currentStep > 1) currentStep-- },
                        enabled = currentStep > 1
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Previous")
                    }
                    
                    if (currentStep < totalSteps) {
                        Button(
                            onClick = { currentStep++ },
                            enabled = isStepValid(currentStep, title, selectedAgeRating, selectedDistributionType, selectedSelectionMethod)
                        ) {
                            Text("Next")
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Filled.ArrowForward, contentDescription = "Next")
                        }
                    } else {
                        Button(
                            onClick = {
                                isCreating = true
                                val book = CreateBookDto(
                                    title = title,
                                    shortDescription = shortDescription.ifBlank { null },
                                    fullDescription = fullDescription.ifBlank { null },
                                    pageCount = pageCount.toIntOrNull() ?: 0,
                                    ageRating = selectedAgeRating!!,
                                    distributionType = selectedDistributionType!!,
                                    totalCopies = totalCopies.toIntOrNull() ?: 1,
                                    applicationDeadline = applicationDeadline,
                                    reviewDeadlineDays = reviewDeadlineDays.toIntOrNull() ?: 30,
                                    selectionMethod = selectedSelectionMethod ?: SelectionMethod.AUTHOR_SELECTS,
                                    selectionCriteria = selectionCriteria.ifBlank { null },
                                    genreIds = selectedGenres.ifEmpty { null },
                                    seriesId = selectedSeries?.id,
                                    seriesOrder = seriesOrder.toIntOrNull()
                                )
                                authorViewModel.createBook(book)
                                navController.popBackStack()
                            },
                            enabled = !isCreating && isStepValid(currentStep, title, selectedAgeRating, selectedDistributionType, selectedSelectionMethod)
                        ) {
                            if (isCreating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text("Create Book")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BasicInfoStep(
    title: String,
    shortDescription: String,
    fullDescription: String,
    pageCount: String,
    onUpdate: (String, String, String, String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Basic Information",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        OutlinedTextField(
            value = title,
            onValueChange = { onUpdate(it, shortDescription, fullDescription, pageCount) },
            label = { Text("Title *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        OutlinedTextField(
            value = shortDescription,
            onValueChange = { onUpdate(title, it, fullDescription, pageCount) },
            label = { Text("Short Description") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )
        
        OutlinedTextField(
            value = fullDescription,
            onValueChange = { onUpdate(title, shortDescription, it, pageCount) },
            label = { Text("Full Description") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 5
        )
        
        OutlinedTextField(
            value = pageCount,
            onValueChange = { onUpdate(title, shortDescription, fullDescription, it) },
            label = { Text("Page Count") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}

@Composable
fun GenresAndSeriesStep(
    selectedGenres: List<Int>,
    selectedSeries: Series?,
    seriesOrder: String,
    mySeries: List<Series>,
    genres: List<GenreDto>,
    onUpdate: (List<Int>, Series?, String) -> Unit,
    onCreateSeries: (String, String) -> Unit,
    onShowCreateSeriesDialog: () -> Unit,
    showCreateSeriesDialog: Boolean,
    onDismissCreateSeriesDialog: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Genres & Series",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        // Genres selection
        Text(
            text = "Select Genres",
            style = MaterialTheme.typography.titleMedium
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(genres) { genre ->
                val isSelected = selectedGenres.contains(genre.id)
                Card(
                    modifier = Modifier
                        .clickable {
                            val newSelection = if (isSelected) {
                                selectedGenres - genre.id
                            } else {
                                selectedGenres + genre.id
                            }
                            onUpdate(newSelection, selectedSeries, seriesOrder)
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = genre.name,
                        modifier = Modifier.padding(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        // Series selection
        Text(
            text = "Select Series (Optional)",
            style = MaterialTheme.typography.titleMedium
        )
        
        Text(
            text = "Choose an existing series or create a new one:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        // Create New Series Button
        Button(
            onClick = onShowCreateSeriesDialog,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = "Create New Series",
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create New Series")
        }
        
        // No Series option
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .selectable(
                    selected = selectedSeries == null,
                    onClick = { onUpdate(selectedGenres, null, seriesOrder) },
                    role = Role.RadioButton
                )
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selectedSeries == null,
                onClick = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "No Series",
                style = MaterialTheme.typography.titleSmall
            )
        }
        
        if (mySeries.isNotEmpty()) {
            Text(
                text = "Existing Series:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Column(modifier = Modifier.selectableGroup()) {
                mySeries.forEach { series ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selectedSeries?.id == series.id,
                                onClick = { onUpdate(selectedGenres, series, seriesOrder) },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedSeries?.id == series.id,
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = series.name,
                                style = MaterialTheme.typography.titleSmall
                            )
                            series.description?.let { desc ->
                                Text(
                                    text = desc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
        
        if (selectedSeries != null) {
            OutlinedTextField(
                value = seriesOrder,
                onValueChange = { onUpdate(selectedGenres, selectedSeries, it) },
                label = { Text("Order in Series") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
    
    // Create Series Dialog
    if (showCreateSeriesDialog) {
        BookCreationCreateSeriesDialog(
            onDismiss = onDismissCreateSeriesDialog,
            onCreateSeries = { name: String, description: String ->
                onCreateSeries(name, description)
                onDismissCreateSeriesDialog()
            }
        )
    }
}

@Composable
fun DistributionStep(
    selectedAgeRating: AgeRating?,
    selectedDistributionType: DistributionType?,
    totalCopies: String,
    onUpdate: (AgeRating?, DistributionType?, String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Distribution Settings",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        // Age Rating
        Text(
            text = "Age Rating *",
            style = MaterialTheme.typography.titleMedium
        )
        
        Column(modifier = Modifier.selectableGroup()) {
            AgeRating.values().forEach { rating ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = selectedAgeRating == rating,
                            onClick = { onUpdate(rating, selectedDistributionType, totalCopies) },
                            role = Role.RadioButton
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedAgeRating == rating,
                        onClick = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = rating.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
                    )
                }
            }
        }
        
        // Distribution Type
        Text(
            text = "Distribution Type *",
            style = MaterialTheme.typography.titleMedium
        )
        
        Column(modifier = Modifier.selectableGroup()) {
            DistributionType.values().forEach { type ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = selectedDistributionType == type,
                            onClick = { onUpdate(selectedAgeRating, type, totalCopies) },
                            role = Role.RadioButton
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedDistributionType == type,
                        onClick = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = type.name.lowercase().replaceFirstChar { it.uppercase() }
                    )
                }
            }
        }
        
        // Total Copies
        OutlinedTextField(
            value = totalCopies,
            onValueChange = { onUpdate(selectedAgeRating, selectedDistributionType, it) },
            label = { Text("Total Copies") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}

@Composable
fun ReviewConfigStep(
    applicationDeadline: String,
    reviewDeadlineDays: String,
    selectedSelectionMethod: SelectionMethod?,
    selectionCriteria: String,
    onUpdate: (String, String, SelectionMethod?, String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Review Configuration",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        OutlinedTextField(
            value = applicationDeadline,
            onValueChange = { onUpdate(it, reviewDeadlineDays, selectedSelectionMethod, selectionCriteria) },
            label = { Text("Application Deadline *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("YYYY-MM-DD") }
        )
        
        OutlinedTextField(
            value = reviewDeadlineDays,
            onValueChange = { onUpdate(applicationDeadline, it, selectedSelectionMethod, selectionCriteria) },
            label = { Text("Review Deadline (Days)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        // Selection Method
        Text(
            text = "Selection Method *",
            style = MaterialTheme.typography.titleMedium
        )
        
        Column(modifier = Modifier.selectableGroup()) {
            SelectionMethod.values().forEach { method ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = selectedSelectionMethod == method,
                            onClick = { onUpdate(applicationDeadline, reviewDeadlineDays, method, selectionCriteria) },
                            role = Role.RadioButton
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedSelectionMethod == method,
                        onClick = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = method.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
                    )
                }
            }
        }
        
        OutlinedTextField(
            value = selectionCriteria,
            onValueChange = { onUpdate(applicationDeadline, reviewDeadlineDays, selectedSelectionMethod, it) },
            label = { Text("Selection Criteria") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )
    }
}

@Composable
fun PreviewStep(
    title: String,
    shortDescription: String,
    ageRating: AgeRating?,
    distributionType: DistributionType?,
    genres: List<Int>,
    series: Series?,
    applicationDeadline: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Preview",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                if (shortDescription.isNotBlank()) {
                    Text(
                        text = shortDescription,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                ageRating?.let { rating ->
                    Text(
                        text = "Age Rating: ${rating.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                distributionType?.let { type ->
                    Text(
                        text = "Distribution: ${type.name.lowercase().replaceFirstChar { it.uppercase() }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                series?.let { s ->
                    Text(
                        text = "Series: ${s.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (applicationDeadline.isNotBlank()) {
                    Text(
                        text = "Application Deadline: $applicationDeadline",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun BookCreationCreateSeriesDialog(
    onDismiss: () -> Unit,
    onCreateSeries: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Series") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Series Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreateSeries(name, description) },
                enabled = name.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun isStepValid(
    step: Int,
    title: String,
    ageRating: AgeRating?,
    distributionType: DistributionType?,
    selectionMethod: SelectionMethod?
): Boolean {
    return when (step) {
        1 -> title.isNotBlank()
        2 -> true // Optional step
        3 -> ageRating != null && distributionType != null
        4 -> true // selectionMethod is now optional
        5 -> true
        else -> false
    }
}
