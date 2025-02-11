package com.example.productivity.habits

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.room.Room
import com.example.productivity.AppDatabase
import com.example.productivity.OverallFragment
import com.example.productivity.R
import com.example.productivity.WeeklyFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class HabitsFragment : Fragment() {
    private lateinit var db: AppDatabase
    private lateinit var habitDao: HabitsDao

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Инфлейтим (создаём) макет для этого фрагмента
        return inflater.inflate(R.layout.fragment_habits, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnToday = view.findViewById<Button>(R.id.btnToday)
        val btnWeekly = view.findViewById<Button>(R.id.btnWeekly)
        val btnOverall = view.findViewById<Button>(R.id.btnOverall)

        replaceFragment(TodayFragment())

        btnToday.setOnClickListener { replaceFragment(TodayFragment()) }
        btnWeekly.setOnClickListener { replaceFragment(WeeklyFragment()) }
        btnOverall.setOnClickListener { replaceFragment(OverallFragment()) }

        db = Room.databaseBuilder(
            requireContext(),
            AppDatabase::class.java,
            "habits-db")
            .build()
        habitDao = db.habitsDao()

        val fabAddHabit = view.findViewById<FloatingActionButton>(R.id.fab_add_habit)
        fabAddHabit.setOnClickListener {
            openHabitsDialog()
        }
        val btnEdit = view.findViewById<ImageButton>(R.id.btn_edit)
        btnEdit.setOnClickListener {openEditHabitsFragment()}

    }

    private fun openHabitsDialog(){
        val input = EditText(requireContext())
        input.hint = "Введите название"

        AlertDialog.Builder(requireContext())
            .setTitle("Добавить привычку")
            .setView(input)
            .setPositiveButton("Добавить") { _, _ ->
                val habitTitle = input.text.toString()
                lifecycleScope.launch {
                    habitDao.insertHabit(HabitsEntity(title = habitTitle))
                   // отправляет сигнал в TodayFragment
                    parentFragmentManager.setFragmentResult("habitAdded", Bundle())
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun replaceFragment(fragment: Fragment){
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun openEditHabitsFragment() {
        findNavController().navigate(R.id.editHabitsFragment) // Переход на экран редактирования
    }



}
