package com.example.productivity

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.productivity.calendar.TaskDao
import com.example.productivity.calendar.TaskEntity
import com.example.productivity.habits.HabitsDao
import com.example.productivity.habits.HabitsEntity

@Database(entities = [TaskEntity::class, HabitsEntity::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun habitsDao(): HabitsDao
}