package com.jnetaol.findai.ui.screens.results

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.jnetaol.findai.data.model.SearchResultDisplay
import com.jnetaol.findai.ui.components.*
import com.jnetaol.findai.ui.screens.AppViewModel
import com.jnetaol.findai.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(navController: NavController, query: String, viewModel: AppViewModel = viewModel()) {
    val results by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()

    var selectedFilter by remember { mutableStateOf<String?>(null) }
    var sortBy by remember { mutableStateOf("score") }

    LaunchedEffect(query) {
        viewModel.search(query)
    }

    val filteredResults = remember(results, selectedFilter) {
        if (selectedFilter == null) results
        else results.filter { it.file.category == selectedFilter }
    }

    val sortedResults = remember(filteredResults, sortBy) {
        when (sortBy) {
            "name" -> filteredResults.sortedBy { it.file.fileName.lowercase() }
            "size" -> filteredResults.sortedByDescending { it.file.sizeBytes }
            "date" -> filteredResults.sortedByDescending { it.file.lastModified }
            else -> filteredResults
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Results", fontWeight = FontWeight.Bold)
                        Text(
                            "\"$query\"",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextTertiary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface,
                    titleContentColor = NeonBlue
                ),
                actions = {
                    IconButton(onClick = {
                        sortBy = when (sortBy) {
                            "score" -> "name"
                            "name" -> "size"
                            "size" -> "date"
                            else -> "score"
                        }
                    }) {
                        Icon(Icons.Default.Sort, "Sort", tint = TextSecondary)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(4.dp))
            FilterChips(
                labels = listOf("document", "image", "video", "audio", "archive", "code"),
                selectedLabel = selectedFilter,
                onSelect = { selectedFilter = it }
            )

            if (isSearching) {
                Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    CircularProgressIndicator(color = NeonBlue)
                }
            } else if (sortedResults.isEmpty()) {
                EmptyState(
                    Icons.Default.SearchOff,
                    "No files found",
                    "Try different keywords or check if files are indexed"
                )
            } else {
                Text(
                    "${sortedResults.size} files found",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextTertiary
                )
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(sortedResults, key = { it.file.id }) { result ->
                        SearchResultRow(
                            file = result.file,
                            score = result.score,
                            matchedFields = result.matchedFields,
                            onClick = { navController.navigate("detail/${result.file.id}") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item { Spacer(Modifier.height(32.dp)) }
                }
            }
        }
    }
}
