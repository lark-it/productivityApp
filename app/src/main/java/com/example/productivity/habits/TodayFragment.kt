package com.example.productivity.habits

import android.os.Bundle
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
        //ждёт сигнал от HabitFragment что добавлена задача
        parentFragmentManager.setFragmentResultListener("habitAdded", viewLifecycleOwner) { _, _ ->
            loadHabits()
        }

    }
    private fun loadHabits() {
        lifecycleScope.launch {
            val habitsFromDb = habitDao.getAllHabits()

            val sortedHabits = mutableListOf<HabitItem>()

            sortedHabits.add(HabitItem.Header("Нужно сделать:"))
            sortedHabits.addAll(habitsFromDb.filter { !it.isCompleted }
                .map { HabitItem.Habit(it.id, it.title, it.isCompleted) })

            sortedHabits.add(HabitItem.Header("Выполненные:"))
            sortedHabits.addAll(habitsFromDb.filter { it.isCompleted }
                .map { HabitItem.Habit(it.id, it.title, it.isCompleted) })

            adapter.updateList(sortedHabits)
        }
    }
}