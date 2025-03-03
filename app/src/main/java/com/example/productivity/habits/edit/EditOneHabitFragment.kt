package com.example.productivity.habits.edit

import android.graphics.Color
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
import android.widget.ToggleButton
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
            selectedColor = bundle.getInt("habitColor",0)
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

        //работаем с выбором типа повторения, выделил комменатрием, чтобы не захламлять
        val buttonDaily = view.findViewById<ToggleButton>(R.id.buttonDaily)
        val buttonWeekly = view.findViewById<ToggleButton>(R.id.buttonWeekly)
        val buttonMonthly = view.findViewById<ToggleButton>(R.id.buttonMonthly)

        daysOfWeekGrid = view.findViewById(R.id.daysOfWeekGrid)

        when (selectedRepeatType) {
            RepeatType.DAILY -> {
                buttonDaily.isChecked = true
                showWeeklyDays(false)
            }
            RepeatType.WEEKLY -> {
                buttonWeekly.isChecked = true
                showWeeklyDays(true)
            }
            RepeatType.MONTHLY -> {
                buttonMonthly.isChecked = true
                showWeeklyDays(false)
            }
        }

        createWeekButtons()

        if (selectedRepeatType == RepeatType.WEEKLY) {
            highlightSelectedDays()
        }


        buttonDaily.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedRepeatType = RepeatType.DAILY
                buttonWeekly.isChecked = false
                buttonMonthly.isChecked = false
                showWeeklyDays(false)
            }
        }
        buttonWeekly.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedRepeatType = RepeatType.WEEKLY
                buttonDaily.isChecked = false
                buttonMonthly.isChecked = false
                showWeeklyDays(true)
            }
        }
        buttonMonthly.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedRepeatType = RepeatType.MONTHLY
                buttonDaily.isChecked = false
                buttonWeekly.isChecked = false
                showWeeklyDays(false)
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

    private fun showWeeklyDays(show: Boolean){
        daysOfWeekGrid.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun createWeekButtons() {
        val daysGrid = view?.findViewById<GridLayout>(R.id.daysOfWeekGrid) ?: return
        val daysNames = listOf("ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ", "ВС")

        for ((index, day) in daysNames.withIndex()) {
            val button = Button(requireContext()).apply {
                text = day
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
            button.setBackgroundColor(Color.LTGRAY) // ⚪ Убираем выделение
        } else {
            selectedDays.add(day)
            button.setBackgroundColor(Color.GREEN) // 🟢 Добавляем выделение
        }
        Log.d("RepeatDays", "Выбранные дни недели: $selectedDays")
    }


    private fun highlightSelectedDays(){
        for(i in 0 until daysOfWeekGrid.childCount){
            val button = daysOfWeekGrid.getChildAt(i) as Button
            val day = i + 1

            if (selectedDays.contains(day)){
                button.setBackgroundColor(Color.GREEN)
            } else{
                button.setBackgroundColor(Color.LTGRAY)
            }
        }
    }
}