package com.jnetaol.findai.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jnetaol.findai.data.model.IndexedFile
import com.jnetaol.findai.engine.FileIndexer
import com.jnetaol.findai.ui.theme.*

@Composable
fun GlowButton(
    text: String,
    icon: ImageVector? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier.shadow(8.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = NeonBlue,
            contentColor = DarkBackground,
            disabledContainerColor = DarkSurfaceVariant,
            disabledContentColor = TextTertiary
        )
    ) {
        if (icon != null) Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun NeonCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        content = content
    )
}

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = TextSecondary,
        modifier = modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String = "",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            icon, contentDescription = null,
            tint = TextTertiary, modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = TextSecondary)
        if (subtitle.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = TextTertiary)
        }
    }
}

@Composable
fun StatusBadge(status: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = when (status) {
            "indexing" -> WarningOrange.copy(alpha = 0.2f)
            "complete" -> SuccessGreen.copy(alpha = 0.2f)
            "error" -> ErrorRed.copy(alpha = 0.2f)
            else -> InfoBlue.copy(alpha = 0.2f)
        }
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = when (status) {
                "indexing" -> WarningOrange
                "complete" -> SuccessGreen
                "error" -> ErrorRed
                else -> InfoBlue
            }
        )
    }
}

@Composable
fun FileCard(
    file: IndexedFile,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    NeonCard(modifier = modifier, onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FileTypeIcon(file.category, Modifier.size(40.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = file.fileName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${FileIndexer.formatFileSize(file.sizeBytes)} • ${file.extension.uppercase()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary
                )
            }
            Icon(Icons.Default.ChevronRight, null, tint = TextTertiary)
        }
    }
}

@Composable
fun FileTypeIcon(category: String, modifier: Modifier = Modifier) {
    val (icon, tint) = when (category) {
        "document" -> Icons.Default.Description to CategoryDocument
        "image" -> Icons.Default.Image to CategoryImage
        "video" -> Icons.Default.VideoFile to CategoryVideo
        "audio" -> Icons.Default.AudioFile to CategoryAudio
        "archive" -> Icons.Default.FolderZip to CategoryArchive
        "code" -> Icons.Default.Code to CategoryCode
        else -> Icons.Default.InsertDriveFile to CategoryOther
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(tint.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = category, tint = tint, modifier = Modifier.size(24.dp))
    }
}

@Composable
fun SearchResultRow(
    file: IndexedFile,
    score: Float,
    matchedFields: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    NeonCard(modifier = modifier, onClick = onClick) {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FileTypeIcon(file.category, Modifier.size(36.dp))
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = file.fileName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = FileIndexer.formatFileSize(file.sizeBytes),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                }
                if (score >= 15f) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = NeonBlue.copy(alpha = 0.15f)
                    ) {
                        Text(
                            "Best match",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonBlue,
                            fontSize = 9.sp
                        )
                    }
                }
            }
            if (file.textContent.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = file.textContent.take(150),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = file.filePath,
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary.copy(alpha = 0.6f),
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChips(
    labels: List<String>,
    selectedLabel: String?,
    onSelect: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedLabel == null,
            onClick = { onSelect(null) },
            label = { Text("All") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = NeonBlue.copy(alpha = 0.2f),
                selectedLabelColor = NeonBlue
            )
        )
        labels.forEach { label ->
            FilterChip(
                selected = selectedLabel == label,
                onClick = { onSelect(if (selectedLabel == label) null else label) },
                label = { Text(label.replaceFirstChar { it.uppercase() }) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = NeonBlue.copy(alpha = 0.2f),
                    selectedLabelColor = NeonBlue
                )
            )
        }
    }
}
