package com.example.productivity.habits.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.productivity.AppDatabase
import com.example.productivity.R
import com.example.productivity.habits.HabitsDao
import com.example.productivity.habits.HabitsEntity
import kotlinx.coroutines.launch

class EditHabitsFragment : DialogFragment() {
    private lateinit var adapter: EditHabitsAdapter
    private var habits = mutableListOf<HabitsEntity>()
    private lateinit var db: AppDatabase
    private lateinit var habitDao: HabitsDao

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_edit_habits, container, false)
    }

    override fun onStart() {
        super.onStart()
        val buttonBack = view?.findViewById<ImageButton>(R.id.buttonBack)

        if (buttonBack != null) {
            buttonBack.setOnClickListener {
                findNavController().navigateUp()
            }
        }

        db = Room.databaseBuilder(requireContext(), AppDatabase::class.java, "habits-db").build()
        habitDao = db.habitsDao()
        val recyclerView = view?.findViewById<RecyclerView>(R.id.rv_edit_habits)
        adapter = EditHabitsAdapter(habits, { habit ->
            val bundle = Bundle().apply {
                putInt("habitId", habit.id)
                putString("habitTitle", habit.title)
                putInt("iconResId", habit.iconResId)
                putInt("habitColor", habit.color)
                putParcelable("repeatType", habit.repeatType)
                putIntegerArrayList("repeatDays", habit.repeatDays?.let { ArrayList(it) })
            }
            findNavController().navigate(R.id.editOneHabitFragment, bundle)
        }, { habit ->
            deleteHabit(habit)
        })

        recyclerView?.layoutManager = LinearLayoutManager(requireContext())
        recyclerView?.adapter = adapter

        loadHabits()

    }
    private fun loadHabits() {
        lifecycleScope.launch {
            val habitsFromDb = habitDao.getAllHabits()
            habits.clear()
            habits.addAll(habitsFromDb)
            adapter.updateList(habits)
        }
    }
    private fun deleteHabit(habit: HabitsEntity) {
        AlertDialog.Builder(requireContext())
            .setTitle("Удалить привычку?")
            .setMessage("Вы уверены, что хотите удалить привычку \"${habit.title}\"?")
            .setPositiveButton("Удалить") { _, _ ->
                lifecycleScope.launch {
                    habitDao.deleteHabit(habit)
                    loadHabits()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
}
