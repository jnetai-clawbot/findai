package com.jnetaol.findai.ui.screens.detail

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.jnetaol.findai.data.model.IndexedFile
import com.jnetaol.findai.engine.FileIndexer
import com.jnetaol.findai.ui.components.*
import com.jnetaol.findai.ui.screens.AppViewModel
import com.jnetaol.findai.ui.theme.*
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileDetailScreen(navController: NavController, fileId: Long, viewModel: AppViewModel = viewModel()) {
    var file by remember { mutableStateOf<IndexedFile?>(null) }
    var preview by remember { mutableStateOf("Loading...") }

    val context = LocalContext.current

    LaunchedEffect(fileId) {
        viewModel.getFileById(fileId) { file = it }
        viewModel.getFilePreview(fileId) { preview = it }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(file?.fileName ?: "File Detail", fontWeight = FontWeight.Bold) },
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
        if (file == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NeonBlue)
            }
            return@Scaffold
        }

        val f = file!!

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NeonCard {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FileTypeIcon(f.category, Modifier.size(48.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(f.fileName, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                            Text(
                                "${FileIndexer.formatFileSize(f.sizeBytes)} • .${f.extension.uppercase()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            NeonCard {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    SectionHeader("File Info")
                    Spacer(Modifier.height(4.dp))
                    InfoRow("Path", f.filePath)
                    InfoRow("MIME Type", f.mimeType)
                    InfoRow("Size", FileIndexer.formatFileSize(f.sizeBytes))
                    InfoRow("Modified", FileIndexer.formatDate(f.lastModified))
                    InfoRow("Indexed", FileIndexer.formatDate(f.dateIndexed))
                    InfoRow("Category", f.category.replaceFirstChar { it.uppercase() })
                }
            }

            NeonCard {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    SectionHeader("Content Preview")
                    Spacer(Modifier.height(4.dp))
                    Text(
                        preview,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GlowButton(
                    text = "Open",
                    icon = Icons.Default.OpenInNew,
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                val fileObj = File(f.filePath)
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    fileObj
                                )
                                setDataAndType(uri, f.mimeType)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Cannot open file", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
                GlowButton(
                    text = "Share",
                    icon = Icons.Default.Share,
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                val fileObj = File(f.filePath)
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    fileObj
                                )
                                putExtra(Intent.EXTRA_STREAM, uri)
                                type = f.mimeType
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share ${f.fileName}"))
                        } catch (e: Exception) {
                            Toast.makeText(context, "Cannot share file", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedButton(
                onClick = {
                    try {
                        val fileToDelete = File(f.filePath)
                        if (fileToDelete.delete()) {
                            Toast.makeText(context, "File deleted", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        } else {
                            Toast.makeText(context, "Cannot delete file", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error deleting file", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                border = ButtonDefaults.outlinedButtonBorder
            ) {
                Icon(Icons.Default.Delete, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Delete File", color = ErrorRed)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = TextTertiary)
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            color = TextPrimary,
            modifier = Modifier.widthIn(max = 220.dp),
            maxLines = 1
        )
    }
}
