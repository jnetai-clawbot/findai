package com.jnetaol.findai.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jnetaol.findai.data.db.AppDatabase
import com.jnetaol.findai.data.model.IndexStats
import com.jnetaol.findai.data.model.IndexedFile
import com.jnetaol.findai.data.model.SearchQuery
import com.jnetaol.findai.data.model.SearchResultDisplay
import com.jnetaol.findai.engine.ContentReader
import com.jnetaol.findai.engine.FileIndexer
import com.jnetaol.findai.engine.FileIndexer.IndexProgress
import com.jnetaol.findai.engine.NLSearchEngine
import com.jnetaol.findai.logger.DebugLogger
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val fileDao = db.fileDao()
    private val searchDao = db.searchDao()

    val allFiles: StateFlow<List<IndexedFile>> = fileDao.getAllFiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentQueries: StateFlow<List<SearchQuery>> = searchDao.getRecentQueries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchResults = MutableStateFlow<List<SearchResultDisplay>>(emptyList())
    val searchResults: StateFlow<List<SearchResultDisplay>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _currentQuery = MutableStateFlow("")
    val currentQuery: StateFlow<String> = _currentQuery.asStateFlow()

    private val _indexProgress = MutableStateFlow(IndexProgress(isComplete = true))
    val indexProgress: StateFlow<IndexProgress> = _indexProgress.asStateFlow()

    private val _isIndexing = MutableStateFlow(false)
    val isIndexing: StateFlow<Boolean> = _isIndexing.asStateFlow()

    val indexStats: StateFlow<IndexStats> = allFiles.map { files ->
        FileIndexer.getStats(files)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), IndexStats())

    fun search(query: String) {
        if (query.isBlank()) return
        _currentQuery.value = query
        _isSearching.value = true
        viewModelScope.launch {
            try {
                DebugLogger.i("AppViewModel", "FA-100 Searching: $query")
                val parsed = NLSearchEngine.parseQuery(query)
                val results = NLSearchEngine.search(parsed, allFiles.value)

                _searchResults.value = results
                _isSearching.value = false

                searchDao.insertQuery(SearchQuery(queryText = query, resultCount = results.size))
            } catch (e: Exception) {
                DebugLogger.e("AppViewModel", "FA-101 Search error", e)
                _isSearching.value = false
            }
        }
    }

    fun startIndexing() {
        if (_isIndexing.value) return
        _isIndexing.value = true
        viewModelScope.launch {
            try {
                DebugLogger.i("AppViewModel", "FA-102 Starting index")
                fileDao.deleteAllFiles()
                val contentReader = ContentReader
                val files = FileIndexer.scanAndIndex(
                    getApplication(),
                    contentReader = contentReader
                ) { progress ->
                    _indexProgress.value = progress
                }
                fileDao.insertFiles(files)
                DebugLogger.i("AppViewModel", "FA-103 Indexed ${files.size} files")
            } catch (e: Exception) {
                DebugLogger.e("AppViewModel", "FA-104 Index error", e)
            } finally {
                _isIndexing.value = false
            }
        }
    }

    fun getFileById(id: Long, onResult: (IndexedFile?) -> Unit) {
        viewModelScope.launch {
            onResult(fileDao.getFileById(id))
        }
    }

    fun getFilePreview(id: Long, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val file = fileDao.getFileById(id)
            if (file != null) {
                val preview = ContentReader.getFilePreview(file.filePath)
                onResult(preview)
            } else {
                onResult("File not found")
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch { searchDao.clearHistory() }
    }

    fun getQuerySuggestions(prefix: String, onResult: (List<String>) -> Unit) {
        viewModelScope.launch {
            val queries = searchDao.getQuerySuggestions(prefix)
            onResult(queries.mapNotNull { it.queryText }.filter { it.isNotBlank() })
        }
    }
}
