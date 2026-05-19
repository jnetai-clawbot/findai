package com.jnetaol.findai.data.db

import androidx.room.*
import com.jnetaol.findai.data.model.IndexedFile
import com.jnetaol.findai.data.model.SearchQuery
import com.jnetaol.findai.data.model.SearchResult
import kotlinx.coroutines.flow.Flow

@Dao
interface FileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: IndexedFile): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFiles(files: List<IndexedFile>)

    @Update
    suspend fun updateFile(file: IndexedFile)

    @Delete
    suspend fun deleteFile(file: IndexedFile)

    @Query("DELETE FROM indexed_files")
    suspend fun deleteAllFiles()

    @Query("SELECT * FROM indexed_files WHERE id = :id")
    suspend fun getFileById(id: Long): IndexedFile?

    @Query("SELECT * FROM indexed_files WHERE filePath = :path LIMIT 1")
    suspend fun getFileByPath(path: String): IndexedFile?

    @Query("SELECT * FROM indexed_files ORDER BY dateIndexed DESC")
    fun getAllFiles(): Flow<List<IndexedFile>>

    @Query("SELECT * FROM indexed_files WHERE category = :category ORDER BY fileName ASC")
    suspend fun getFilesByCategory(category: String): List<IndexedFile>

    @Query("SELECT COUNT(*) FROM indexed_files")
    suspend fun getFileCount(): Long

    @Query("SELECT COUNT(*) FROM indexed_files WHERE category = :category")
    suspend fun getCategoryCount(category: String): Long

    @Query("SELECT SUM(sizeBytes) FROM indexed_files")
    suspend fun getTotalSize(): Long?

    @Query("SELECT * FROM indexed_files WHERE LOWER(fileName) LIKE '%' || LOWER(:query) || '%'")
    suspend fun searchByFileName(query: String): List<IndexedFile>

    @Query("SELECT * FROM indexed_files WHERE textContent LIKE '%' || :query || '%'")
    suspend fun searchByContent(query: String): List<IndexedFile>

    @Query("SELECT * FROM indexed_files WHERE category = :category")
    suspend fun getByCategory(category: String): List<IndexedFile>

    @Query("SELECT * FROM indexed_files WHERE extension IN (:extensions)")
    suspend fun getByExtensions(extensions: List<String>): List<IndexedFile>

    @Query("SELECT * FROM indexed_files WHERE lastModified BETWEEN :from AND :to")
    suspend fun getByDateRange(from: Long, to: Long): List<IndexedFile>

    @Query("SELECT * FROM indexed_files WHERE sizeBytes BETWEEN :minSize AND :maxSize")
    suspend fun getBySizeRange(minSize: Long, maxSize: Long): List<IndexedFile>
}

@Dao
interface SearchDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuery(query: SearchQuery): Long

    @Query("SELECT * FROM search_queries ORDER BY timestamp DESC LIMIT 20")
    fun getRecentQueries(): Flow<List<SearchQuery>>

    @Query("SELECT * FROM search_queries WHERE LOWER(queryText) LIKE '%' || LOWER(:prefix) || '%' ORDER BY timestamp DESC LIMIT 5")
    suspend fun getQuerySuggestions(prefix: String): List<SearchQuery>

    @Query("DELETE FROM search_queries")
    suspend fun clearHistory()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: SearchResult)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResults(results: List<SearchResult>)

    @Query("DELETE FROM search_results WHERE queryId = :queryId")
    suspend fun deleteResults(queryId: Long)
}
