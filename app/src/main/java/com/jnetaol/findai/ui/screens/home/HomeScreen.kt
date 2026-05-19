package com.jnetaol.findai.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.jnetaol.findai.ui.components.*
import com.jnetaol.findai.ui.screens.AppViewModel
import com.jnetaol.findai.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, viewModel: AppViewModel = viewModel()) {
    var searchText by remember { mutableStateOf("") }
    var suggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val allFiles by viewModel.allFiles.collectAsState()
    val recentQueries by viewModel.recentQueries.collectAsState()
    val stats by viewModel.indexStats.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FindAI", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface,
                    titleContentColor = NeonBlue
                ),
                actions = {
                    IconButton(onClick = { navController.navigate("index") }) {
                        Icon(Icons.Default.Storage, "Index", tint = TextSecondary)
                    }
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(Icons.Default.Settings, "Settings", tint = TextSecondary)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = searchText,
                    onValueChange = { value ->
                        searchText = value
                        if (value.length >= 2) {
                            viewModel.getQuerySuggestions(value) { suggestions = it }
                        } else {
                            suggestions = emptyList()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            "\"that PDF about nginx\" or \"video from last week\"",
                            color = TextTertiary
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, null, tint = NeonBlue)
                    },
                    trailingIcon = {
                        if (searchText.isNotBlank()) {
                            IconButton(onClick = {
                                searchText = ""
                                suggestions = emptyList()
                            }) {
                                Icon(Icons.Default.Clear, "Clear", tint = TextTertiary)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonBlue,
                        unfocusedBorderColor = DarkBorder,
                        cursorColor = NeonBlue,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = DarkSurface,
                        unfocusedContainerColor = DarkSurface
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        if (searchText.isNotBlank()) {
                            focusManager.clearFocus()
                            navController.navigate("results/$searchText")
                        }
                    })
                )
            }

            if (suggestions.isNotEmpty()) {
                item {
                    NeonCard {
                        Column(Modifier.fillMaxWidth().padding(12.dp)) {
                            SectionHeader("Suggestions")
                            suggestions.forEach { suggestion ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            searchText = suggestion
                                            suggestions = emptyList()
                                            focusManager.clearFocus()
                                            navController.navigate("results/$suggestion")
                                        }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.History, null,
                                        tint = TextTertiary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        suggestion,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                SectionHeader("Quick Filters")
            }

            item {
                val filters = listOf("document", "image", "video", "audio", "archive", "code")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filters) { filter ->
                        FilterChip(
                            selected = false,
                            onClick = { navController.navigate("results/$filter files") },
                            label = { Text(filter.replaceFirstChar { it.uppercase() }) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = DarkSurfaceVariant,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }
            }

            item {
                SectionHeader("Recent Searches")
            }

            if (recentQueries.isEmpty()) {
                item {
                    EmptyState(
                        Icons.Default.SearchOff,
                        "No recent searches",
                        "Your search history will appear here"
                    )
                }
            } else {
                items(recentQueries.take(5)) { query ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate("results/${query.queryText}") }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.History, null,
                            tint = TextTertiary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            query.queryText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            "${query.resultCount} results",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary
                        )
                    }
                }
                item {
                    TextButton(
                        onClick = { viewModel.clearHistory() },
                        colors = ButtonDefaults.textButtonColors(contentColor = ErrorRed)
                    ) {
                        Icon(Icons.Default.DeleteSweep, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Clear history")
                    }
                }
            }

            item {
                SectionHeader("Index Overview")
            }

            item {
                NeonCard {
                    Row(
                        Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem("Files", "${stats.totalFiles}", CategoryDocument)
                        StatItem("Docs", "${stats.documents}", CategoryCode)
                        StatItem("Images", "${stats.images}", CategoryImage)
                        StatItem("Videos", "${stats.videos}", CategoryVideo)
                    }
                }
            }

            item {
                Spacer(Modifier.height(16.dp))
                GlowButton(
                    text = "Scan & Index Files",
                    icon = Icons.Default.Refresh,
                    onClick = { navController.navigate("index") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextTertiary)
    }
}


