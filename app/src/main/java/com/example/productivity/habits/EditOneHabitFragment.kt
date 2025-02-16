package com.example.productivity.habits

import android.graphics.Color
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
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.productivity.AppDatabase
import com.example.productivity.R
import kotlinx.coroutines.launch

class EditOneHabitFragment : BaseHabitFragment() {
    private var habitId: Int = 0
    private lateinit var habitTitle: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let { bundle ->
            habitId = bundle.getInt("habitId", 0)
            habitTitle = bundle.getString("habitTitle", "") ?: ""
            selectedIcon = bundle.getInt("iconResId", 0)
            selectedColor = bundle.getInt("habitColor",0)
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_edit_one_habit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val editText = view.findViewById<EditText>(R.id.etHabitTitle)
        val iconsContainer = view.findViewById<LinearLayout>(R.id.linearLayoutIcons)
        val recyclerViewColors = view.findViewById<RecyclerView>(R.id.rvColors)

        editText.setText(habitTitle)
        addIconsToScrollView(iconsContainer, icons)

        recyclerViewColors.layoutManager = GridLayoutManager(requireContext(), 5)

        val colorAdapter = ColorAdapter(getColorList(), selectedColor) { selectedColor = it }
        recyclerViewColors.adapter = colorAdapter

        view.findViewById<Button>(R.id.btSaveHabit).setOnClickListener {
            saveHabit(editText.text.toString())
        }
        view.findViewById<ImageButton>(R.id.buttonBack).setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun saveHabit(newTitle: String) {
        val db = Room.databaseBuilder(requireContext(), AppDatabase::class.java, "habits-db").build()
        val habitDao = db.habitsDao()

        lifecycleScope.launch {
            habitDao.updateHabit(habitId, newTitle, selectedIcon ?: 0, selectedColor ?: 0)
            findNavController().navigateUp()
        }
    }

}