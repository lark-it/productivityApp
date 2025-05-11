package com.example.productivity.ui.habits.overall

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.productivity.R
import com.example.productivity.AppDatabase
import com.example.productivity.data.habits.HabitCompletionEntity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class OverallFragment : Fragment() {

    private lateinit var adapter: ItemOverallAdapter
    private lateinit var db: AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_overall, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = AppDatabase.getDatabase(requireContext())

        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_overall)
        adapter = ItemOverallAdapter(emptyList())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        loadData()
    }

    private fun loadData() {
        lifecycleScope.launch {
            val habits = db.habitsDao().getAllHabits()
            val completed = db.habitCompletionDao().getAllCompletedDates()

            val habitItems = habits.map { habit ->
                val startDate = habit.startDate
                val daysProgress = generateHabitHistory(startDate, completed, habit.id)
                HabitOverallItem(
                    title = habit.title,
                    daysProgress = daysProgress,
                    iconResId = habit.iconResId,
                    color = habit.color
                )
            }

            adapter.updateList(habitItems)
        }
    }

    private fun generateHabitHistory(startDate: String, completedDates: List<HabitCompletionEntity>, habitId: Int): List<List<Boolean>> {
        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        calendar.time = sdf.parse(startDate) ?: return emptyList()

        val history = MutableList(15) { MutableList(7) { false } }

        for (week in 0 until 15) {
            for (day in 0 until 7) {
                if (week < history.size && day < history[week].size) {
                    val dateString = sdf.format(calendar.time)
                    history[week][day] = completedDates.any { it.habitId == habitId && it.date == dateString }
                }
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }
        }
        return history
    }
}