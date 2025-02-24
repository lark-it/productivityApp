package com.example.productivity

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.productivity.calendar.TaskDao
import com.example.productivity.calendar.TaskEntity
import com.example.productivity.habits.HabitsDao
import com.example.productivity.habits.HabitsEntity

@Database(entities = [TaskEntity::class, HabitsEntity::class], version = 7)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun habitsDao(): HabitsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "habits-db"
                )
                    .fallbackToDestructiveMigration() // 💥 ВАЖНО! Удалит старую БД при изменении версии
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}