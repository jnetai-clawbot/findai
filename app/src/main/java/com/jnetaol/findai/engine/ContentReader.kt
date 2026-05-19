package com.jnetaol.findai.engine

import com.jnetaol.findai.logger.DebugLogger
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

object ContentReader {
    private const val MAX_READ_SIZE = 1 * 1024 * 1024

    fun readTextContent(file: File): String? {
        return try {
            if (!file.exists() || file.length() > MAX_READ_SIZE) return null
            val builder = StringBuilder()
            BufferedReader(InputStreamReader(file.inputStream(), Charsets.UTF_8)).use { reader ->
                var line = reader.readLine()
                var read = 0
                while (line != null && read < 50000) {
                    builder.append(line).append("\n")
                    line = reader.readLine()
                    read += line?.length ?: 0
                }
            }
            builder.toString().take(100000)
        } catch (e: Exception) {
            DebugLogger.w("ContentReader", "FA-030 Cannot read: ${file.name}")
            null
        }
    }

    fun readPdfText(file: File): String? {
        return try {
            if (!file.exists()) return null
            DebugLogger.i("ContentReader", "FA-031 Attempting PDF read: ${file.name}")
            val rawString = file.readText(Charsets.ISO_8859_1)
            val textBlocks = extractPdfText(rawString)
            if (textBlocks.length > 50) textBlocks else null
        } catch (e: Exception) {
            DebugLogger.w("ContentReader", "FA-032 PDF parse failed: ${file.name}")
            null
        }
    }

    private fun extractPdfText(raw: String): String {
        val builder = StringBuilder()
        val btPattern = Regex("""\(([^)]*(?:\([^)]*\))*[^)]*)\)\s*Tj""")
        val matches = btPattern.findAll(raw)
        for (match in matches) {
            val text = match.groupValues[1]
                .replace("\\(", "(")
                .replace("\\)", ")")
                .replace("\\n", "\n")
                .trim()
            if (text.isNotBlank() && text.length > 1) {
                builder.append(text).append(" ")
            }
        }
        return builder.toString().trim()
    }

    fun getFilePreview(filePath: String, maxChars: Int = 2000): String {
        val file = File(filePath)
        if (!file.exists()) return "File not found"
        val ext = file.extension.lowercase()
        return when (ext) {
            "txt", "md", "csv", "xml", "json", "html", "css", "kt", "java", "py", "js", "ts", "cpp",
            "c", "h", "go", "rs", "sh" -> {
                readTextContent(file)?.take(maxChars) ?: "Cannot preview this file"
            }
            "pdf" -> {
                readPdfText(file)?.take(maxChars) ?: "PDF preview not available (binary content)"
            }
            "jpg", "jpeg", "png", "gif", "webp", "svg" -> "Image file: ${file.name}"
            "mp4", "mkv", "avi", "webm" -> "Video file: ${file.name}"
            "mp3", "wav", "ogg", "flac", "aac" -> "Audio file: ${file.name}"
            "zip", "rar", "7z", "tar", "gz" -> "Archive file: ${file.name}"
            else -> "File type: .$ext"
        }
    }
}
