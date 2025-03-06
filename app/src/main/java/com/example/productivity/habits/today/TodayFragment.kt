package com.example.productivity.habits.today

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.productivity.AppDatabase
import com.example.productivity.R
import com.example.productivity.habits.HabitCompletionDao
import com.example.productivity.habits.HabitCompletionEntity
import com.example.productivity.habits.HabitsDao
import com.example.productivity.habits.RepeatType
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TodayFragment : Fragment() {
    private lateinit var adapter: TodayAdapter
    private var habits = mutableListOf<HabitItem>()
    private lateinit var db: AppDatabase
    private lateinit var habitDao: HabitsDao
    private lateinit var habitCompletionDao: HabitCompletionDao

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_today, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = AppDatabase.getDatabase(requireContext())
        habitDao = db.habitsDao()
        habitCompletionDao = db.habitCompletionDao()

        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_today)
        adapter = TodayAdapter(habits) { updatedHabit ->
            lifecycleScope.launch {
                val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                if (updatedHabit.isCompleted) {
                    habitCompletionDao.insertCompletion(HabitCompletionEntity(updatedHabit.id, todayDate, true))
                } else {
                    habitCompletionDao.deleteCompletion(updatedHabit.id, todayDate)
                }

                loadHabits()
            }
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        loadHabits()
    }


    private fun loadHabits() {
        lifecycleScope.launch {
            val habitsFromDb = habitDao.getAllHabits()

            val today = Calendar.getInstance()
            var dayOfWeek = today.get(Calendar.DAY_OF_WEEK) - 1 // 0 = Sunday, 6 = Saturday
            if (dayOfWeek == -1) dayOfWeek = 6 // Переносим воскресенье в конец
            val dayOfMonth = today.get(Calendar.DAY_OF_MONTH)
            val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(today.time)

            val sortedHabits = mutableListOf<HabitItem>()
            sortedHabits.add(HabitItem.Header("Нужно сделать:"))

            val habitsToShow = habitsFromDb.filter { habit ->
                // ✅ Добавляем проверку, что `startDate` не в будущем
                val habitStartDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(habit.startDate)
                if (habitStartDate != null && habitStartDate.after(today.time)) {
                    return@filter false
                }

                val isHabitDay = when (habit.repeatType) {
                    RepeatType.DAILY -> true
                    RepeatType.WEEKLY -> habit.repeatDays?.contains(dayOfWeek) == true
                    RepeatType.MONTHLY -> dayOfMonth in (habit.repeatDays ?: emptyList())
                }

                isHabitDay
            }.map { habit ->
                val isCompleted = habitCompletionDao.isHabitCompleted(habit.id, todayDate) ?: false
                HabitItem.Habit(habit.id, habit.title, isCompleted, habit.iconResId, habit.color)
            }

            sortedHabits.addAll(habitsToShow.filter { !it.isCompleted })
            sortedHabits.add(HabitItem.Header("Выполненные:"))
            sortedHabits.addAll(habitsToShow.filter { it.isCompleted })

            adapter.updateList(sortedHabits)
        }
    }

}
