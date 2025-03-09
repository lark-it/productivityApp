package com.example.productivity.home

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.db.williamchart.data.AxisType
import com.example.productivity.AppDatabase
import com.example.productivity.R
import com.example.productivity.calendar.TaskEntity
import com.example.productivity.habits.HabitCompletionEntity
import com.example.productivity.habits.RepeatType
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.db.williamchart.view.BarChartView

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Инфлейтим (создаём) макет для этого фрагмента
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        calculateStats()
        setupHabitsCompletedChart()
    }

    private fun calculateStats() {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            val habitCompletionDao = db.habitCompletionDao()
            val habitsDao = db.habitsDao()
            val taskDao = db.taskDao()

            val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)

            val allCompletedHabits = habitCompletionDao.getAllCompletedDates()
            val allCompletedTasks = taskDao.getAllTasks().filter { it.isCompleted }.map { it.date }
            val allTasks = taskDao.getAllTasks()
            val allHabits = habitsDao.getAllHabits()

            val totalHabitsCompleted = allCompletedHabits.size + allTasks.count { it.isCompleted }
            val totalPerfectDays = calculatePerfectDays(allTasks, allCompletedHabits)

            val currentStreak = calculateCurrentStreak(allCompletedHabits.map { it.date }, allCompletedTasks)

            val tasksUntilToday = allTasks.filter { it.date <= todayDate }
            val completedTasksUntilToday = tasksUntilToday.count { it.isCompleted }

            val totalHabitOccurrences = allHabits.sumOf { habit ->
                val startDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(habit.startDate)
                if (startDate != null && startDate.time <= System.currentTimeMillis()) {
                    val daysSinceStart = ((System.currentTimeMillis() - startDate.time) / (1000 * 60 * 60 * 24)).toInt()
                    when (habit.repeatType) {
                        RepeatType.DAILY -> daysSinceStart + 1
                        RepeatType.WEEKLY -> {
                            ((daysSinceStart + 1) / 7) * (habit.repeatDays?.size ?: 1)
                        }
                        RepeatType.MONTHLY -> (daysSinceStart + 1) / 30
                        else -> 1
                    }
                } else 0
            }

            val habitCompletionsUntilToday = allCompletedHabits.filter { it.date <= todayDate }

            val totalTasks = tasksUntilToday.size + totalHabitOccurrences
            val completedTasks = completedTasksUntilToday + habitCompletionsUntilToday.size

            val completionRate = if (totalTasks > 0) ((completedTasks.toFloat() / totalTasks) * 100).toInt().coerceAtMost(100) else 0
            requireActivity().runOnUiThread {
                view?.findViewById<TextView>(R.id.tvCurrentStreak)?.text = "$currentStreak days"
                view?.findViewById<TextView>(R.id.tvCompletionRate)?.text = "$completionRate%"
                view?.findViewById<TextView>(R.id.tvHabitsCompleted)?.text = "$totalHabitsCompleted"
                view?.findViewById<TextView>(R.id.tvPerfectDays)?.text = "$totalPerfectDays"
            }
        }
    }

    private fun calculateCurrentStreak(completedDates: List<String>, completedTasks: List<String>): Int {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val uniqueCompletedDates = (completedDates + completedTasks).distinct().mapNotNull {
            format.parse(it)
        }.sortedDescending()

        val today = Calendar.getInstance()
        var streak = 0

        for (date in uniqueCompletedDates) {
            val cal = Calendar.getInstance()
            cal.time = date

            if (cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) - streak) {
                streak++
            } else {
                break
            }
        }
        return streak
    }

    private fun calculatePerfectDays(tasks: List<TaskEntity>, completedHabits: List<HabitCompletionEntity>): Int {
        val tasksByDate = tasks.groupBy { it.date }
        val habitsByDate = completedHabits.groupBy { it.date }

        return tasksByDate.keys.union(habitsByDate.keys).count { date ->
            val totalTasks = tasksByDate[date]?.size ?: 0
            val totalHabits = habitsByDate[date]?.size ?: 0
            val completedTasks = tasksByDate[date]?.count { it.isCompleted } ?: 0
            val completedHabits = habitsByDate[date]?.count { it.isCompleted } ?: 0

            val totalItems = totalTasks + totalHabits
            val completedItems = completedTasks + completedHabits

            totalItems > 0 && completedItems == totalItems
        }
    }

    private fun setupHabitsCompletedChart() {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            val habitCompletionDao = db.habitCompletionDao()
            val taskDao = db.taskDao()

            val fullDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dayOnlyFormat = SimpleDateFormat("d", Locale.getDefault())
            val calendar = Calendar.getInstance()

            val last7Days = (0..6).map {
                calendar.time = Date(System.currentTimeMillis() - it * 24 * 60 * 60 * 1000)
                fullDateFormat.format(calendar.time)
            }.reversed()

            val habitsData = last7Days.map { date ->
                val habitsCompleted = habitCompletionDao.getCompletedCountByDate(date)
                val tasksCompleted = taskDao.getCompletedCountByDate(date)

                val dayLabel = fullDateFormat.parse(date)?.let { dayOnlyFormat.format(it) } ?: "?"
                dayLabel to (habitsCompleted + tasksCompleted).toFloat()
            }

            requireActivity().runOnUiThread {
                val barChart = view?.findViewById<BarChartView>(R.id.barChart)
                if (barChart != null) {
                    barChart.animate(habitsData)

                    barChart.barsColor = ContextCompat.getColor(requireContext(), R.color.purple_200)
                    barChart.labelsColor = Color.WHITE
                    barChart.axis = AxisType.XY

                    barChart.labelsFormatter = { value -> value.toInt().toString() }
                }
            }
        }
    }
}
