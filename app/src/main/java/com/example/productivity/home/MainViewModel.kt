package com.example.productivity.home

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.productivity.calendar.TaskDao
import com.example.productivity.habits.HabitsDao
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class MainViewModel(
    private val userRepository: UserRepository,
    private val taskDao: TaskDao,
    private val habitsDao: HabitsDao,
    private val context: Context
) : ViewModel() {
    val lives = MutableLiveData<Int>()

    fun updateLives() {
        viewModelScope.launch {
            checkAndUpdateLives()
            lives.postValue(userRepository.getLives())
        }
    }

    suspend fun decreaseLifeIfRecent(dateStr: String) {
        val today = LocalDate.now()
        val date = LocalDate.parse(dateStr)
        val daysSince = ChronoUnit.DAYS.between(date, today)

        if (daysSince <= 3) {
            val currentLives = userRepository.getUser().lives
            if (currentLives > 0) {
                val newLives = currentLives - 1
                userRepository.updateLives(newLives)
                lives.postValue(newLives)
                Log.d("MainViewModel", "❌ Жизнь отнята за отмену на $dateStr, осталось $newLives")
            }
        }
    }

    private suspend fun checkAndUpdateLives() {
        val prefs = context.getSharedPreferences("tamagotchi_prefs", Context.MODE_PRIVATE)
        val lastCheckedString = prefs.getString("last_lives_check", null)
        val today = LocalDate.now()
        val lastCheckedDate = lastCheckedString?.let { LocalDate.parse(it) } ?: today

        val user = userRepository.getUser()
        var lives = user.lives
        val lostLifeDates = mutableListOf<LocalDate>()
        val maxLives = 3

        // Шаг 1: Отнимаем жизни за пропущенные дни между последней проверкой и сегодня
        for (i in 1..ChronoUnit.DAYS.between(lastCheckedDate, today)) {
            val dateToCheck = lastCheckedDate.plusDays(i)
            val dateStr = dateToCheck.toString()
            val completedTasks = taskDao.getCompletedCountByDate(dateStr)
            val totalTasks = taskDao.getTotalTaskOccurrencesByDate(dateStr)
            val completedHabits = habitsDao.getCompletedHabitsOnDate(dateStr)

            if (totalTasks == 0 && completedHabits == 0) {
                Log.d("checkAndUpdateLives", "ℹ️ День $dateStr пустой — пропускаем")
                continue
            }

            if ((totalTasks > 0 && completedTasks < totalTasks) || (totalTasks == 0 && completedHabits == 0)) {
                if (lives > 0) {
                    lives--
                    lostLifeDates.add(dateToCheck)
                    Log.d("checkAndUpdateLives", "❌ Потеряна жизнь за $dateStr, осталось $lives")
                }
            }
        }

        // Обновляем данные
        if (lives != user.lives) {
            userRepository.updateLives(lives)
        }

        prefs.edit().putString("last_lives_check", today.toString()).apply()
    }
}