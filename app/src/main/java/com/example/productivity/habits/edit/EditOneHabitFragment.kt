package com.example.productivity.habits.edit

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RadioGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.productivity.AppDatabase
import com.example.productivity.R
import com.example.productivity.habits.BaseHabitFragment
import com.example.productivity.habits.ColorAdapter
import com.example.productivity.habits.RepeatType
import kotlinx.coroutines.launch

class EditOneHabitFragment : BaseHabitFragment() {
    private var habitId: Int = 0
    private lateinit var habitTitle: String
    private lateinit var daysOfWeekGrid: GridLayout
    private var selectedRepeatType: RepeatType = RepeatType.DAILY
    private val selectedDays = mutableListOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let { bundle ->
            habitId = bundle.getInt("habitId", 0)
            habitTitle = bundle.getString("habitTitle", "") ?: ""
            selectedIcon = bundle.getInt("iconResId", 0)
            selectedColor = bundle.getInt("habitColor", 0)
            selectedRepeatType = bundle.getParcelable("repeatType") ?: RepeatType.DAILY
            selectedDays.addAll(bundle.getIntegerArrayList("repeatDays") ?: emptyList())
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

        val repeatTypeGroup = view.findViewById<RadioGroup>(R.id.repeatTypeGroup)
        daysOfWeekGrid = view.findViewById(R.id.daysOfWeekGrid)

        when (selectedRepeatType) {
            RepeatType.DAILY -> {
                repeatTypeGroup.check(R.id.buttonDaily)
                showWeeklyDays(false)
            }
            RepeatType.WEEKLY -> {
                repeatTypeGroup.check(R.id.buttonWeekly)
                showWeeklyDays(true)
            }
            else -> {
                selectedRepeatType = RepeatType.DAILY
                repeatTypeGroup.check(R.id.buttonDaily)
                showWeeklyDays(false)
            }
        }

        createWeekButtons()

        if (selectedRepeatType == RepeatType.WEEKLY) {
            highlightSelectedDays()
        }

        repeatTypeGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.buttonDaily -> {
                    selectedRepeatType = RepeatType.DAILY
                    showWeeklyDays(false)
                }
                R.id.buttonWeekly -> {
                    selectedRepeatType = RepeatType.WEEKLY
                    showWeeklyDays(true)
                }
            }
        }
    }

    private fun saveHabit(newTitle: String) {
        val db = Room.databaseBuilder(requireContext(), AppDatabase::class.java, "habits-db").build()
        val habitDao = db.habitsDao()

        lifecycleScope.launch {
            habitDao.updateHabit(habitId, newTitle, selectedIcon ?: 0, selectedColor ?: 0, selectedRepeatType, selectedDays)
            findNavController().navigateUp()
        }
    }

    private fun showWeeklyDays(show: Boolean) {
        daysOfWeekGrid.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun createWeekButtons() {
        val daysGrid = view?.findViewById<GridLayout>(R.id.daysOfWeekGrid) ?: return
        val daysNames = listOf("ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ", "ВС")

        for ((index, day) in daysNames.withIndex()) {
            val button = Button(requireContext()).apply {
                text = day
                textSize = 14f
                setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                setBackgroundResource(R.drawable.day_of_week_button_selector)
                setPadding(16, 8, 16, 8)
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    height = GridLayout.LayoutParams.WRAP_CONTENT
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    setMargins(6, 6, 6, 6)
                }
                setOnClickListener {
                    toggleDaySelection(index + 1, this)
                }
            }
            daysGrid.addView(button)
        }
    }

    private fun toggleDaySelection(day: Int, button: Button) {
        if (selectedDays.contains(day)) {
            selectedDays.remove(day)
            button.isSelected = false
        } else {
            selectedDays.add(day)
            button.isSelected = true
        }
        Log.d("RepeatDays", "Выбранные дни недели: $selectedDays")
    }

    private fun highlightSelectedDays() {
        for (i in 0 until daysOfWeekGrid.childCount) {
            val button = daysOfWeekGrid.getChildAt(i) as Button
            val day = i + 1
            button.isSelected = selectedDays.contains(day)
        }
    }
}