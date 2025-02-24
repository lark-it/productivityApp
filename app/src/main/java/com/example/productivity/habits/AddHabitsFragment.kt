package com.example.productivity.habits

import android.app.DatePickerDialog
import android.graphics.Color
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
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
    private var selectedRepeatType: RepeatType = RepeatType.DAILY
    private lateinit var daysOfWeekGrid: GridLayout
    private var startDate: String? = null

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

        //работаем с выбором типа повторения, выделил комменатрием, чтобы не захламлять
        val buttonDaily = view.findViewById<ToggleButton>(R.id.buttonDaily)
        val buttonWeekly = view.findViewById<ToggleButton>(R.id.buttonWeekly)
        val buttonMonthly = view.findViewById<ToggleButton>(R.id.buttonMonthly)

        daysOfWeekGrid = view.findViewById(R.id.daysOfWeekGrid)

        buttonDaily.isChecked = true
        showWeeklyDays(false)
        createWeekButtons()

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

        //DatePicker
        val btnPickStartDate = view.findViewById<Button>(R.id.btnPickStartDate)
        val tvStartDate = view.findViewById<TextView>(R.id.tvStartDate)

        btnPickStartDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                    startDate = selectedDate
                    tvStartDate.text = "Дата начала: $startDate"
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }
    }

    private fun saveHabit(view: View) {
        val habitTitle = view.findViewById<EditText>(R.id.etHabitTitle).text.toString()
        if (habitTitle.isNotEmpty() && selectedIcon != null && selectedColor != null) {
            val newHabit = HabitsEntity(
                title = habitTitle,
                iconResId = selectedIcon!!,
                color = selectedColor!!,
                repeatType = selectedRepeatType,
                repeatDays = selectedDays,
                startDate = startDate ?: getCurrentDate()
            )
            lifecycleScope.launch {
                db.habitsDao().insertHabit(newHabit)
                findNavController().navigateUp()
            }
        } else {
            Toast.makeText(requireContext(), "Выберите иконку и цвет!", Toast.LENGTH_SHORT).show()
        }
    }
    private val selectedDays = mutableListOf<Int>()
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
            button.setBackgroundColor(Color.LTGRAY)
        } else {
            selectedDays.add(day)
            button.setBackgroundColor(Color.GREEN)
        }
        Log.d("RepeatDays", "Выбранные дни недели: $selectedDays")
    }

    private fun showWeeklyDays(show: Boolean){
        daysOfWeekGrid.visibility = if (show) View.VISIBLE else View.GONE
    }
    private fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        return "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH) + 1}-${calendar.get(Calendar.DAY_OF_MONTH)}"
    }

}

