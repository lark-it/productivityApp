package com.example.productivity.habits

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.productivity.AppDatabase
import com.example.productivity.R
import kotlinx.coroutines.launch

class TodayFragment : Fragment(){
    private lateinit var adapter: TodayAdapter
    private var habits = mutableListOf<HabitItem>()
    private lateinit var db: AppDatabase
    private lateinit var habitDao: HabitsDao

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_today, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = Room.databaseBuilder(requireContext(), AppDatabase::class.java, "habits-db").build()
        habitDao = db.habitsDao()

        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_today)
        adapter = TodayAdapter(habits) { updatedHabit ->
            lifecycleScope.launch {
                habitDao.updateHabit(updatedHabit.id, updatedHabit.isCompleted)
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

            val today = java.util.Calendar.getInstance()
            var dayOfWeek = today.get(java.util.Calendar.DAY_OF_WEEK) // 1 = Sunday, 7 = Saturday
            if (dayOfWeek == 1) dayOfWeek = 7 //7 = Sunday
            else dayOfWeek -= 1 //1 = Monday, 7 = Sunday

            val dayOfMonth = today.get(java.util.Calendar.DAY_OF_MONTH)

            val filteredHabits = habitsFromDb.filter { habit ->
                Log.d("FILTERING", "Habit: ${habit.title}, repeatType: ${habit.repeatType}, repeatDays: ${habit.repeatDays}")

                when (habit.repeatType) {
                    RepeatType.DAILY -> {
                        val result = habit.repeatDays.isNullOrEmpty() || habit.repeatDays.contains(dayOfWeek)
                        result
                    }
                    RepeatType.WEEKLY -> {
                        val result = habit.repeatDays?.contains(dayOfWeek) ?: false
                        result
                    }
                    RepeatType.MONTHLY -> {
                        val result = habit.repeatDays?.contains(dayOfMonth) ?: false
                        result
                    }
                }
            }

            val sortedHabits = mutableListOf<HabitItem>()

            sortedHabits.add(HabitItem.Header("Нужно сделать:"))
            sortedHabits.addAll(filteredHabits.filter { !it.isCompleted }
                .map { HabitItem.Habit(it.id, it.title, it.isCompleted, it.iconResId, it.color) })

            sortedHabits.add(HabitItem.Header("Выполненные:"))
            sortedHabits.addAll(filteredHabits.filter { it.isCompleted }
                .map { HabitItem.Habit(it.id, it.title, it.isCompleted, it.iconResId, it.color) })

            adapter.updateList(sortedHabits)
        }
    }


}