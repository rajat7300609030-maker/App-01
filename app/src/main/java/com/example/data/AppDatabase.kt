package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [SavedPage::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun savedPageDao(): SavedPageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "education_hills.db"
                ).addCallback(AppDatabaseCallback(scope))
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class AppDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialBookmarks(database.savedPageDao())
                    }
                }
            }

            private suspend fun populateInitialBookmarks(dao: SavedPageDao) {
                dao.insertSavedPage(
                    SavedPage(
                        title = "The Education Hills - Official Portal",
                        url = "https://educationhills.netlify.app/",
                        category = "Portal",
                        notes = "Main school portal and campus hub",
                        isPinned = true
                    )
                )
                dao.insertSavedPage(
                    SavedPage(
                        title = "Admissions & Procedures",
                        url = "https://educationhills.netlify.app/#admissions",
                        category = "Admissions",
                        notes = "Enrollment criteria, guidelines, and admission inquiries",
                        isPinned = true
                    )
                )
                dao.insertSavedPage(
                    SavedPage(
                        title = "Academics & Faculty",
                        url = "https://educationhills.netlify.app/#academics",
                        category = "Academics",
                        notes = "Curriculum, teaching staff, and learning programs",
                        isPinned = false
                    )
                )
                dao.insertSavedPage(
                    SavedPage(
                        title = "Campus & Student Life",
                        url = "https://educationhills.netlify.app/#campus",
                        category = "Campus",
                        notes = "Smart classrooms, sports grounds, and activities",
                        isPinned = false
                    )
                )
                dao.insertSavedPage(
                    SavedPage(
                        title = "Contact & Location",
                        url = "https://educationhills.netlify.app/#contact",
                        category = "Contact",
                        notes = "School address, email, phone numbers, and visits",
                        isPinned = false
                    )
                )
            }
        }
    }
}
