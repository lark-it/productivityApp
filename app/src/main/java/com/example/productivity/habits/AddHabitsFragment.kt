package com.example.productivity.habits

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.ActionBar.LayoutParams
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.productivity.AppDatabase
import com.example.productivity.R
import kotlinx.coroutines.launch

class AddHabitsFragment : BaseHabitFragment() {
    private lateinit var db: AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_add_habits, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = AppDatabase.getDatabase(requireContext())

        val iconContainer = view.findViewById<LinearLayout>(R.id.linearLayoutIcons)
        addIconsToScrollView(iconContainer, icons)

        val recyclerViewColors = view.findViewById<RecyclerView>(R.id.rvColors)
        recyclerViewColors.layoutManager = GridLayoutManager(requireContext(), 5)

        val colorAdapter = ColorAdapter(getColorList(), selectedColor) { selectedColor = it }
        recyclerViewColors.adapter = colorAdapter

        view.findViewById<Button>(R.id.btSaveHabit).setOnClickListener {
            saveHabit(view)
        }
        view.findViewById<ImageButton>(R.id.buttonBack).setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun saveHabit(view: View) {
        val habitTitle = view.findViewById<EditText>(R.id.etHabitTitle).text.toString()
        if (habitTitle.isNotEmpty() && selectedIcon != null && selectedColor != null) {
            val newHabit = HabitsEntity(
                title = habitTitle,
                iconResId = selectedIcon!!,
                color = selectedColor!!
            )
            lifecycleScope.launch {
                db.habitsDao().insertHabit(newHabit)
                findNavController().navigateUp()
            }
        } else {
            Toast.makeText(requireContext(), "Выберите иконку и цвет!", Toast.LENGTH_SHORT).show()
        }
    }
}

