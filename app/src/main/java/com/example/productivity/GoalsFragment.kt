package com.example.productivity

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.replace

class GoalsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Инфлейтим (создаём) макет для этого фрагмента
        return inflater.inflate(R.layout.fragment_goals, container, false)
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

    }

    private fun replaceFragment(fragment: Fragment){
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
