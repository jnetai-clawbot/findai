package com.jnetaol.findai.ui.screens.settings

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.jnetaol.findai.BuildConfig
import com.jnetaol.findai.logger.DebugLogger
import com.jnetaol.findai.ui.components.*
import com.jnetaol.findai.ui.screens.AppViewModel
import com.jnetaol.findai.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, viewModel: AppViewModel = viewModel()) {
    val context = LocalContext.current
    var debugLoggingEnabled by remember { mutableStateOf(DebugLogger.enableLogging) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SectionHeader("App Info")

            NeonCard {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("FindAI", style = MaterialTheme.typography.titleLarge, color = NeonBlue, fontWeight = FontWeight.Bold)
                        StatusBadge("v${BuildConfig.VERSION_NAME}")
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "AI-Powered File Search",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Made By jnetai.com",
                        style = MaterialTheme.typography.bodySmall,
                        color = NeonBlue,
                        modifier = Modifier.clickable {
                            try {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse("https://jnetai.com"))
                                )
                            } catch (e: Exception) {
                                Toast.makeText(context, "Cannot open link", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }

            SectionHeader("Index Management")

            NeonCard {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    SettingsRow(
                        icon = Icons.Default.Refresh,
                        label = "Re-Index Files",
                        subtitle = "Rescan storage and update file index",
                        onClick = { viewModel.startIndexing() }
                    )
                    Divider(color = DarkBorder, thickness = 0.5.dp)
                    SettingsRow(
                        icon = Icons.Default.Storage,
                        label = "View Index Stats",
                        subtitle = "See indexed files by category",
                        onClick = { navController.navigate("index") }
                    )
                }
            }

            SectionHeader("Preferences")

            NeonCard {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "Debug Logging",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary
                            )
                            Text(
                                "Enable detailed debug output",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextTertiary
                            )
                        }
                        Switch(
                            checked = debugLoggingEnabled,
                            onCheckedChange = {
                                debugLoggingEnabled = it
                                DebugLogger.enableLogging = it
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NeonBlue,
                                checkedTrackColor = NeonBlue.copy(alpha = 0.3f),
                                uncheckedThumbColor = TextTertiary,
                                uncheckedTrackColor = DarkSurfaceVariant
                            )
                        )
                    }
                }
            }

            SectionHeader("Actions")

            NeonCard {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    SettingsRow(
                        icon = Icons.Default.Update,
                        label = "Check For Updates",
                        subtitle = "Get the latest version from GitHub",
                        onClick = {
                            try {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/jnetai-clawbot/findai/releases"))
                                )
                            } catch (e: Exception) {
                                Toast.makeText(context, "Cannot open link", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                    Divider(color = DarkBorder, thickness = 0.5.dp)
                    SettingsRow(
                        icon = Icons.Default.Share,
                        label = "Share FindAI",
                        subtitle = "Tell others about this app",
                        onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "Check out FindAI - AI-Powered File Search! https://jnetai.com")
                                putExtra(Intent.EXTRA_SUBJECT, "FindAI - AI File Search")
                            }
                            context.startActivity(Intent.createChooser(intent, "Share FindAI"))
                        }
                    )
                    Divider(color = DarkBorder, thickness = 0.5.dp)
                    SettingsRow(
                        icon = Icons.Default.DeleteSweep,
                        label = "Clear Search History",
                        subtitle = "Remove all recent searches",
                        onClick = { viewModel.clearHistory() }
                    )
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = NeonBlue, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = TextTertiary)
        }
        Icon(Icons.Default.ChevronRight, null, tint = TextTertiary, modifier = Modifier.size(20.dp))
    }
}
