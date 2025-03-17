package com.example.productivity.habits.weekly

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.productivity.R
import com.example.productivity.habits.*
import com.example.productivity.AppDatabase
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class WeeklyFragment : Fragment() {
    private lateinit var adapter: WeeklyAdapter
    private lateinit var db: AppDatabase
    private lateinit var habitDao: HabitsDao
    private lateinit var habitCompletionDao: HabitCompletionDao

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_weekly, container, false)

        db = AppDatabase.getDatabase(requireContext())
        habitDao = db.habitsDao()
        habitCompletionDao = db.habitCompletionDao()

        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_weekly)
        adapter = WeeklyAdapter(emptyList())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        updateWeeklyView()

        return view
    }

    private fun getWeekDatesForHabit(habit: HabitsEntity): List<String> {
        val startDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(habit.startDate) ?: return emptyList()
        val calendar = Calendar.getInstance()
        calendar.time = startDate

        val weekDates = mutableListOf<String>()
        repeat(7) {
            while (habit.repeatType == RepeatType.WEEKLY && habit.repeatDays?.contains(calendar.get(Calendar.DAY_OF_WEEK) - 1) == false) {
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }
            weekDates.add(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time))
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        return weekDates
    }

    private fun updateWeeklyView() {
        lifecycleScope.launch {
            val habits = habitDao.getAllHabits()
            val weeklyHabits = mutableListOf<HabitWeeklyItem>()

            for (habit in habits) {
                val weekDates = getWeekDatesForHabit(habit)
                val completedDates = habitCompletionDao.getCompletedDates(habit.id)
                val daysCompletion = weekDates.map { date -> completedDates.contains(date) }
                weeklyHabits.add(HabitWeeklyItem(habit.title, daysCompletion, weekDates))
            }

            adapter.updateList(weeklyHabits)
        }
    }
}