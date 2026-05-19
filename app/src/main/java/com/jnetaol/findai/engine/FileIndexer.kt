package com.jnetaol.findai.engine

import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import com.jnetaol.findai.data.model.IndexedFile
import com.jnetaol.findai.data.model.IndexStats
import com.jnetaol.findai.logger.DebugLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileIndexer {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

    private val fileCategories = mapOf(
        "document" to listOf("pdf", "doc", "docx", "txt", "md", "csv", "xml", "json", "xls", "xlsx", "ppt", "pptx", "odt", "ods", "odp", "rtf"),
        "image" to listOf("jpg", "jpeg", "png", "gif", "bmp", "webp", "svg", "ico", "tiff", "tif", "heic", "heif"),
        "video" to listOf("mp4", "mkv", "avi", "mov", "wmv", "flv", "webm", "3gp", "m4v", "ts"),
        "audio" to listOf("mp3", "wav", "ogg", "flac", "aac", "m4a", "wma", "opus", "mid", "midi"),
        "archive" to listOf("zip", "rar", "7z", "tar", "gz", "bz2", "xz", "apk", "iso", "dmg"),
        "code" to listOf("kt", "java", "py", "js", "ts", "html", "css", "cpp", "c", "h", "go", "rs", "swift", "sh", "bat", "ps1")
    )

    private val mimeTypes = mapOf(
        "pdf" to "application/pdf",
        "txt" to "text/plain",
        "md" to "text/markdown",
        "csv" to "text/csv",
        "json" to "application/json",
        "xml" to "application/xml",
        "jpg" to "image/jpeg",
        "jpeg" to "image/jpeg",
        "png" to "image/png",
        "gif" to "image/gif",
        "webp" to "image/webp",
        "svg" to "image/svg+xml",
        "mp4" to "video/mp4",
        "mkv" to "video/x-matroska",
        "avi" to "video/x-msvideo",
        "webm" to "video/webm",
        "mp3" to "audio/mpeg",
        "wav" to "audio/wav",
        "ogg" to "audio/ogg",
        "flac" to "audio/flac",
        "zip" to "application/zip",
        "rar" to "application/vnd.rar",
        "7z" to "application/x-7z-compressed"
    )

    data class IndexProgress(
        val currentFile: String = "",
        val filesProcessed: Int = 0,
        val totalFiles: Int = 0,
        val isComplete: Boolean = false
    )

    suspend fun scanAndIndex(
        context: Context,
        includedDirs: List<String>? = null,
        contentReader: ContentReader,
        onProgress: (IndexProgress) -> Unit
    ): List<IndexedFile> = withContext(Dispatchers.IO) {
        DebugLogger.i("FileIndexer", "FA-010 Starting file index scan")
        val indexedFiles = mutableListOf<IndexedFile>()
        val dirs = includedDirs ?: getDefaultScanDirs()

        val allFiles = mutableListOf<File>()
        dirs.forEach { dirPath ->
            val dir = File(dirPath)
            if (dir.exists() && dir.isDirectory) {
                collectFiles(dir, allFiles)
            }
        }

        val textExtensions = listOf("txt", "md", "csv", "xml", "json", "html", "css", "kt", "java", "py", "js", "ts", "cpp", "c", "h", "go", "rs")
        var processed = 0

        allFiles.forEach { file ->
            if (!file.exists() || file.isDirectory) return@forEach
            val ext = file.extension.lowercase()
            val category = getCategory(ext)
            val mime = mimeTypes[ext] ?: "application/octet-stream"

            var textContent = ""
            if (ext in textExtensions && file.length() < 5 * 1024 * 1024) {
                textContent = contentReader.readTextContent(file) ?: ""
            }

            val indexed = IndexedFile(
                fileName = file.name,
                filePath = file.absolutePath,
                extension = ext,
                mimeType = mime,
                category = category,
                sizeBytes = file.length(),
                lastModified = file.lastModified(),
                textContent = textContent
            )
            indexedFiles.add(indexed)
            processed++

            if (processed % 50 == 0) {
                onProgress(IndexProgress(file.name, processed, allFiles.size))
            }
        }

        onProgress(IndexProgress("", processed, allFiles.size, isComplete = true))
        DebugLogger.i("FileIndexer", "FA-011 Index complete: ${indexedFiles.size} files")
        indexedFiles
    }

    fun getStats(files: List<IndexedFile>): IndexStats {
        return IndexStats(
            totalFiles = files.size.toLong(),
            documents = files.count { it.category == "document" || it.category == "code" }.toLong(),
            images = files.count { it.category == "image" }.toLong(),
            videos = files.count { it.category == "video" }.toLong(),
            audio = files.count { it.category == "audio" }.toLong(),
            archives = files.count { it.category == "archive" }.toLong(),
            others = files.count { it.category == "other" }.toLong(),
            totalSizeBytes = files.sumOf { it.sizeBytes }
        )
    }

    private fun getDefaultScanDirs(): List<String> {
        val dirs = mutableListOf(
            Environment.getExternalStorageDirectory().absolutePath,
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath,
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).absolutePath,
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).absolutePath,
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath,
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).absolutePath,
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).absolutePath
        )
        return dirs.distinct()
    }

    private fun collectFiles(dir: File, result: MutableList<File>, maxDepth: Int = 10) {
        if (maxDepth <= 0) return
        val files = try {
            dir.listFiles()
        } catch (e: Exception) {
            DebugLogger.w("FileIndexer", "FA-012 Cannot list dir: ${dir.absolutePath}")
            return
        }
        files?.forEach { file ->
            if (file.isDirectory && !file.name.startsWith(".")) {
                val skipDirs = setOf("Android", "cache", "temp", ".thumbnails")
                if (file.name !in skipDirs) {
                    collectFiles(file, result, maxDepth - 1)
                }
            } else if (file.isFile) {
                result.add(file)
            }
        }
    }

    fun getCategory(extension: String): String {
        fileCategories.forEach { (category, exts) ->
            if (extension in exts) return category
        }
        return "other"
    }

    fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> "${"%.1f".format(bytes / (1024.0 * 1024.0))} MB"
            else -> "${"%.2f".format(bytes / (1024.0 * 1024.0 * 1024.0))} GB"
        }
    }

    fun formatDate(timestamp: Long): String {
        return dateFormat.format(Date(timestamp))
    }
}
