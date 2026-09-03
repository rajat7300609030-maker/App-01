package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedPageDao {
    @Query("SELECT * FROM saved_pages ORDER BY isPinned DESC, savedAt DESC")
    fun getAllSavedPages(): Flow<List<SavedPage>>

    @Query("SELECT * FROM saved_pages WHERE title LIKE '%' || :query || '%' OR url LIKE '%' || :query || '%' OR notes LIKE '%' || :query || '%' ORDER BY isPinned DESC, savedAt DESC")
    fun searchSavedPages(query: String): Flow<List<SavedPage>>

    @Query("SELECT * FROM saved_pages WHERE url = :url LIMIT 1")
    fun getSavedPageByUrl(url: String): Flow<SavedPage?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedPage(page: SavedPage): Long

    @Update
    suspend fun updateSavedPage(page: SavedPage)

    @Delete
    suspend fun deleteSavedPage(page: SavedPage)

    @Query("DELETE FROM saved_pages WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM saved_pages")
    suspend fun getCount(): Int
}
