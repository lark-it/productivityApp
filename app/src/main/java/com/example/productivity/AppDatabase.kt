package com.example.productivity

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.productivity.calendar.TaskDao
import com.example.productivity.calendar.TaskEntity
import com.example.productivity.habits.HabitCompletionDao
import com.example.productivity.habits.HabitCompletionEntity
import com.example.productivity.habits.HabitsDao
import com.example.productivity.habits.HabitsEntity
import com.example.productivity.home.HabitBonusEntity
import com.example.productivity.home.UserDao
import com.example.productivity.home.UserEntity

@Database(
    entities = [TaskEntity::class, HabitsEntity::class,
        HabitCompletionEntity::class, UserEntity::class, HabitBonusEntity::class],
    version = 10)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun habitsDao(): HabitsDao
    abstract fun habitCompletionDao(): HabitCompletionDao
    abstract fun userDao(): UserDao

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