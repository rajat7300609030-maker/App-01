package com.example.data

import kotlinx.coroutines.flow.Flow

class SavedPageRepository(private val dao: SavedPageDao) {
    val allSavedPages: Flow<List<SavedPage>> = dao.getAllSavedPages()

    fun searchPages(query: String): Flow<List<SavedPage>> = dao.searchSavedPages(query)

    fun observeByUrl(url: String): Flow<SavedPage?> = dao.getSavedPageByUrl(url)

    suspend fun insert(page: SavedPage): Long = dao.insertSavedPage(page)

    suspend fun update(page: SavedPage) = dao.updateSavedPage(page)

    suspend fun delete(page: SavedPage) = dao.deleteSavedPage(page)

    suspend fun deleteById(id: Long) = dao.deleteById(id)
}
