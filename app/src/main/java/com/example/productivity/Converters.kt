package com.example.productivity

import androidx.room.TypeConverter
import com.example.productivity.habits.RepeatType

class Converters {
    @TypeConverter
    fun fromList(list: List<Int>?): String {
        return list?.joinToString(",") ?: ""
    }

    @TypeConverter
    fun toList(data: String): List<Int> {
        return if (data.isEmpty()) emptyList() else data.split(",").map { it.toInt() }
    }
}
