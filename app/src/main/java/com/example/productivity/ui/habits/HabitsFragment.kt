package com.example.productivity.ui.habits

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.example.productivity.R
import com.example.productivity.ui.habits.overall.OverallFragment
import com.example.productivity.ui.habits.today.TodayFragment
import com.example.productivity.ui.habits.weekly.WeeklyFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HabitsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_habits, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnToday = view.findViewById<Button>(R.id.btnToday)
        val btnWeekly = view.findViewById<Button>(R.id.btnWeekly)
        val btnOverall = view.findViewById<Button>(R.id.btnOverall)

        val buttons = listOf(btnToday, btnWeekly, btnOverall)

        fun updateButtonSelection(selectedButton: Button) {
            buttons.forEach { button ->
                if (button == selectedButton) {
                    button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.chetwode_blue))
                } else {
                    button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.dark_gunmetal))
                }
            }
        }
        replaceFragment(TodayFragment())
        updateButtonSelection(btnToday)

        btnToday.setOnClickListener {
            replaceFragment(TodayFragment())
            updateButtonSelection(btnToday)
        }
        btnWeekly.setOnClickListener {
            replaceFragment(WeeklyFragment())
            updateButtonSelection(btnWeekly)
        }
        btnOverall.setOnClickListener {
            replaceFragment(OverallFragment())
            updateButtonSelection(btnOverall)
        }

        val fabAddHabit = view.findViewById<FloatingActionButton>(R.id.fab_add_habit)
        fabAddHabit.setOnClickListener {
            findNavController().navigate(R.id.AddHabitsFragment)
        }
        val btnEdit = view.findViewById<ImageButton>(R.id.btn_edit)
        btnEdit.setOnClickListener { openEditHabitsFragment() }
    }

    private fun replaceFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun openEditHabitsFragment() {
        findNavController().navigate(R.id.editHabitsFragment)
    }
}