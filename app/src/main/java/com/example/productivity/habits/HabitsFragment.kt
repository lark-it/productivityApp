package com.example.productivity.habits

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.navigation.fragment.findNavController
import com.example.productivity.R
import com.example.productivity.habits.overall.OverallFragment
import com.example.productivity.habits.today.TodayFragment
import com.example.productivity.habits.weekly.WeeklyFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HabitsFragment : Fragment() {

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

        val fabAddHabit = view.findViewById<FloatingActionButton>(R.id.fab_add_habit)
        fabAddHabit.setOnClickListener {
            findNavController().navigate(R.id.AddHabitsFragment)
        }
        val btnEdit = view.findViewById<ImageButton>(R.id.btn_edit)
        btnEdit.setOnClickListener {openEditHabitsFragment()}

    }

    private fun replaceFragment(fragment: Fragment){
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun openEditHabitsFragment() {
        findNavController().navigate(R.id.editHabitsFragment)
    }

}
