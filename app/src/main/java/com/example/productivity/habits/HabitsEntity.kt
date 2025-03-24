package com.example.productivity.habits

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Entity(tableName = "habits")
data class HabitsEntity (
    @PrimaryKey(autoGenerate = true) val id: Int =0,
    val title: String,
    val isCompleted:Boolean = false,
    val iconResId: Int,
    val color: Int,
    val repeatType: RepeatType,
    val repeatDays: List<Int>? = null,
    val startDate: String
)

@Parcelize
enum class RepeatType : Parcelable {
    DAILY, WEEKLY
}

