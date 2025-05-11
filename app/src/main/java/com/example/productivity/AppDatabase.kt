package com.example.productivity

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.productivity.data.calendar.Converters
import com.example.productivity.data.tasks.TaskDao
import com.example.productivity.data.tasks.TaskEntity
import com.example.productivity.data.habits.HabitCompletionDao
import com.example.productivity.data.habits.HabitCompletionEntity
import com.example.productivity.data.habits.HabitsDao
import com.example.productivity.data.habits.HabitsEntity
import com.example.productivity.data.auth.HabitBonusEntity
import com.example.productivity.data.auth.UserDao
import com.example.productivity.data.auth.UserEntity
import com.example.productivity.data.home.AchievementDao
import com.example.productivity.data.home.AchievementEntity

@Database(
    entities = [TaskEntity::class, HabitsEntity::class,
        HabitCompletionEntity::class, UserEntity::class, HabitBonusEntity::class,
        AchievementEntity::class],
    version = 11)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun habitsDao(): HabitsDao
    abstract fun habitCompletionDao(): HabitCompletionDao
    abstract fun userDao(): UserDao
    abstract fun achievementDao(): AchievementDao

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
                    .fallbackToDestructiveMigration() // ВАЖНО! Удалит старую БД при изменении версии
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}