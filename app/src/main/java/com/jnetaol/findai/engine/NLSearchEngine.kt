package com.jnetaol.findai.engine

import com.jnetaol.findai.data.model.IndexedFile
import com.jnetaol.findai.data.model.SearchResultDisplay
import com.jnetaol.findai.logger.DebugLogger
import java.io.File
import java.util.Calendar

object NLSearchEngine {

    data class ParsedQuery(
        val keywords: List<String>,
        val requiredTypes: List<String> = emptyList(),
        val excludedPhrases: List<String> = emptyList(),
        val isDateQuery: Boolean = false,
        val dateFrom: Long = 0,
        val dateTo: Long = 0,
        val minSize: Long = 0,
        val maxSize: Long = Long.MAX_VALUE,
        val rawQuery: String = ""
    )

    fun parseQuery(rawQuery: String): ParsedQuery {
        val query = rawQuery.trim().lowercase()
        if (query.isEmpty()) return ParsedQuery(emptyList())

        val requiredTypes = mutableListOf<String>()
        val excludedPhrases = mutableListOf<String>()
        var isDateQuery = false
        var dateFrom = 0L
        var dateTo = System.currentTimeMillis()
        var minSize = 0L
        var maxSize = Long.MAX_VALUE

        if (query.contains("pdf") || query.contains("pdfs")) requiredTypes.add("pdf")
        if (query.contains("document") || query.contains("doc") || query.contains("documents")) requiredTypes.add("document")
        if (query.contains("image") || query.contains("photo") || query.contains("picture") || query.contains("images")) requiredTypes.add("image")
        if (query.contains("video") || query.contains("movie") || query.contains("videos")) requiredTypes.add("video")
        if (query.contains("audio") || query.contains("music") || query.contains("song") || query.contains("mp3")) requiredTypes.add("audio")
        if (query.contains("code") || query.contains("source")) requiredTypes.add("code")
        if (query.contains("archive") || query.contains("zip") || query.contains("compressed")) requiredTypes.add("archive")
        if (query.contains("text") || query.contains("txt")) requiredTypes.add("text")

        val typeKeywords = listOf("pdf", "document", "doc", "image", "photo", "picture", "video", "movie",
            "audio", "music", "song", "mp3", "code", "source", "archive", "zip", "compressed", "text", "txt")

        val now = Calendar.getInstance()
        dateFrom = when {
            query.contains("today") -> {
                isDateQuery = true
                now.apply {
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
                }.timeInMillis
            }
            query.contains("yesterday") -> {
                isDateQuery = true
                now.apply {
                    add(Calendar.DAY_OF_MONTH, -1)
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
                }.timeInMillis
            }
            query.contains("last week") || query.contains("this week") -> {
                isDateQuery = true
                now.apply {
                    add(Calendar.DAY_OF_MONTH, -7)
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
                }.timeInMillis
            }
            query.contains("last month") || query.contains("this month") -> {
                isDateQuery = true
                now.apply {
                    add(Calendar.MONTH, -1)
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
                }.timeInMillis
            }
            else -> dateFrom
        }

        if (query.contains("large") || query.contains("big") || query.contains("huge")) {
            minSize = 100 * 1024 * 1024
        }
        if (query.contains("small") || query.contains("tiny")) {
            maxSize = 1024 * 1024
        }

        val words = query.split(Regex("[\\s,;.!?]+")).filter { it.isNotBlank() }
        val keywords = words.filter { word ->
            word !in typeKeywords &&
            word !in listOf("about", "the", "a", "an", "in", "on", "at", "to", "for", "of", "from", "by",
                "that", "with", "my", "me", "is", "are", "was", "were", "find", "search", "show", "get",
                "large", "big", "huge", "small", "tiny", "last", "this", "today", "yesterday", "week", "month")
        }

        return ParsedQuery(
            keywords = keywords,
            requiredTypes = requiredTypes,
            excludedPhrases = excludedPhrases,
            isDateQuery = isDateQuery,
            dateFrom = dateFrom,
            dateTo = dateTo,
            minSize = minSize,
            maxSize = maxSize,
            rawQuery = rawQuery
        )
    }

    fun search(
        parsed: ParsedQuery,
        allFiles: List<IndexedFile>
    ): List<SearchResultDisplay> {
        DebugLogger.i("NLSearchEngine", "FA-020 Searching: ${parsed.rawQuery}")
        val results = mutableListOf<SearchResultDisplay>()

        for (file in allFiles) {
            if (file.sizeBytes < parsed.minSize || file.sizeBytes > parsed.maxSize) continue

            if (parsed.isDateQuery && file.lastModified < parsed.dateFrom) continue

            if (parsed.requiredTypes.isNotEmpty()) {
                val hasMatchingType = parsed.requiredTypes.any { type ->
                    val ext = file.extension.lowercase()
                    when (type) {
                        "pdf" -> ext == "pdf"
                        "document" -> FileIndexer.getCategory(ext) == "document"
                        "image" -> FileIndexer.getCategory(ext) == "image"
                        "video" -> FileIndexer.getCategory(ext) == "video"
                        "audio" -> FileIndexer.getCategory(ext) == "audio"
                        "code" -> FileIndexer.getCategory(ext) == "code"
                        "archive" -> FileIndexer.getCategory(ext) == "archive"
                        "text" -> ext == "txt" || ext == "md"
                        else -> false
                    }
                }
                if (!hasMatchingType) continue
            }

            val (score, matchedFields) = calculateScore(file, parsed.keywords)
            if (score > 0f) {
                results.add(SearchResultDisplay(file, score, matchedFields))
            }
        }

        return results.sortedByDescending { it.score }
    }

    private fun calculateScore(file: IndexedFile, keywords: List<String>): Pair<Float, String> {
        if (keywords.isEmpty()) return 2.0f to "category_match"
        val matched = mutableListOf<String>()
        val fileNameLower = file.fileName.lowercase()
        val contentLower = file.textContent.lowercase()

        var score = 0f
        for (keyword in keywords) {
            if (keyword.length < 2) continue

            if (fileNameLower == keyword) {
                score += 20f
                matched.add("exact_name")
            } else if (fileNameLower.startsWith(keyword)) {
                score += 15f
                matched.add("name_prefix")
            } else if (fileNameLower.contains(keyword)) {
                score += 10f
                matched.add("name_contains")
            }

            if (contentLower.contains(keyword)) {
                score += 5f
                matched.add("content")
            }

            if (file.extension.lowercase() == keyword.lowercase()) {
                score += 12f
                matched.add("extension")
            }
        }

        if (score > 0 && matched.isEmpty()) matched.add("keyword_match")
        return score to matched.distinct().joinToString(", ")
    }
}
