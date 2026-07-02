package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Archer::class, ArcherySession::class, EndSession::class, ArrowShot::class],
    version = 1,
    exportSchema = false
)
abstract class ArcheryDatabase : RoomDatabase() {
    abstract fun archeryDao(): ArcheryDao

    companion object {
        @Volatile
        private var INSTANCE: ArcheryDatabase? = null

        fun getInstance(context: Context): ArcheryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ArcheryDatabase::class.java,
                    "scorify_archery_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
