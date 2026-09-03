package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_pages")
data class SavedPage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val url: String,
    val category: String = "General",
    val notes: String = "",
    val savedAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false
)
