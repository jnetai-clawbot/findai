package com.jnetaol.findai.ui.screens.index

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.jnetaol.findai.engine.FileIndexer
import com.jnetaol.findai.ui.components.*
import com.jnetaol.findai.ui.screens.AppViewModel
import com.jnetaol.findai.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndexScreen(navController: NavController, viewModel: AppViewModel = viewModel()) {
    val stats by viewModel.indexStats.collectAsState()
    val progress by viewModel.indexProgress.collectAsState()
    val isIndexing by viewModel.isIndexing.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("File Index", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface,
                    titleContentColor = NeonBlue
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(Modifier.height(8.dp))
                NeonCard {
                    Column(Modifier.fillMaxWidth().padding(16.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Index Status", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                            StatusBadge(
                                when {
                                    isIndexing -> "indexing"
                                    stats.totalFiles > 0 -> "complete"
                                    else -> "empty"
                                }
                            )
                        }
                        Spacer(Modifier.height(12.dp))

                        if (isIndexing && !progress.isComplete) {
                            val currentProgress = if (progress.totalFiles > 0)
                                progress.filesProcessed.toFloat() / progress.totalFiles.toFloat()
                            else 0f
                            LinearProgressIndicator(
                                progress = currentProgress,
                                modifier = Modifier.fillMaxWidth(),
                                color = NeonBlue,
                                trackColor = DarkSurfaceVariant
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Scanning: ${progress.currentFile}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextTertiary,
                                maxLines = 1
                            )
                            Text(
                                "${progress.filesProcessed} / ${progress.totalFiles} files",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            item { SectionHeader("Files by Category") }

            item {
                NeonCard {
                    Column(Modifier.fillMaxWidth().padding(12.dp)) {
                        IndexCategoryRow("Documents & Code", "${stats.documents}", Icons.Default.Description, CategoryDocument)
                        Divider(color = DarkBorder, thickness = 0.5.dp)
                        IndexCategoryRow("Images", "${stats.images}", Icons.Default.Image, CategoryImage)
                        Divider(color = DarkBorder, thickness = 0.5.dp)
                        IndexCategoryRow("Videos", "${stats.videos}", Icons.Default.VideoFile, CategoryVideo)
                        Divider(color = DarkBorder, thickness = 0.5.dp)
                        IndexCategoryRow("Audio", "${stats.audio}", Icons.Default.AudioFile, CategoryAudio)
                        Divider(color = DarkBorder, thickness = 0.5.dp)
                        IndexCategoryRow("Archives", "${stats.archives}", Icons.Default.FolderZip, CategoryArchive)
                        Divider(color = DarkBorder, thickness = 0.5.dp)
                        IndexCategoryRow("Other", "${stats.others}", Icons.Default.InsertDriveFile, CategoryOther)
                    }
                }
            }

            item {
                NeonCard {
                    Row(
                        Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        DetailItem("Total Files", "${stats.totalFiles}")
                        DetailItem("Total Size", FileIndexer.formatFileSize(stats.totalSizeBytes))
                    }
                }
            }

            item {
                GlowButton(
                    text = if (isIndexing) "Indexing..." else "Re-Index All Files",
                    icon = Icons.Default.Refresh,
                    onClick = { viewModel.startIndexing() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isIndexing
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Indexing scans your storage for files and makes them searchable.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary
                )
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun IndexCategoryRow(
    label: String,
    count: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(36.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
        Text(count, style = MaterialTheme.typography.titleSmall, color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DetailItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, color = NeonBlue, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(2.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextTertiary)
    }
}
