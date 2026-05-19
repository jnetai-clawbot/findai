package com.jnetaol.findai.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "indexed_files",
    indices = [
        Index(value = ["filePath"], unique = true),
        Index(value = ["fileName"]),
        Index(value = ["extension"]),
        Index(value = ["category"]),
        Index(value = ["lastModified"])
    ]
)
data class IndexedFile(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fileName: String,
    val filePath: String,
    val extension: String,
    val mimeType: String,
    val category: String,
    val sizeBytes: Long,
    val lastModified: Long,
    val textContent: String = "",
    val dateIndexed: Long = System.currentTimeMillis()
)

@Entity(tableName = "search_queries", indices = [Index(value = ["queryText"])])
data class SearchQuery(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val queryText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val resultCount: Int = 0
)

@Entity(
    tableName = "search_results",
    indices = [
        Index(value = ["queryId"]),
        Index(value = ["fileId"])
    ]
)
data class SearchResult(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val queryId: Long,
    val fileId: Long,
    val score: Float,
    val matchedFields: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class SearchResultDisplay(
    val file: IndexedFile,
    val score: Float,
    val matchedFields: String
)

data class IndexStats(
    val totalFiles: Long = 0,
    val documents: Long = 0,
    val images: Long = 0,
    val videos: Long = 0,
    val audio: Long = 0,
    val archives: Long = 0,
    val others: Long = 0,
    val totalSizeBytes: Long = 0
)
