package com.example.productivity.ui.habits

import android.app.DatePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.productivity.AppDatabase
import com.example.productivity.R
import com.example.productivity.data.habits.HabitsEntity
import com.example.productivity.data.habits.RepeatType
import com.example.productivity.habits.HabitsRepositoryImpl
import com.example.productivity.habits.HabitsViewModel
import com.example.productivity.habits.HabitsViewModelFactory

class AddHabitsFragment : BaseHabitFragment() {

    private val viewModel: HabitsViewModel by viewModels {
        HabitsViewModelFactory(
            HabitsRepositoryImpl(
                AppDatabase.getDatabase(requireContext()).habitsDao(),
                AppDatabase.getDatabase(requireContext()).habitCompletionDao()
            )
        )
    }

    private var selectedRepeatType: RepeatType = RepeatType.DAILY
    private var startDate: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_add_habits, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val iconContainer = view.findViewById<LinearLayout>(R.id.linearLayoutIcons)
        addIconsToScrollView(iconContainer, icons)

        val rvColors = view.findViewById<RecyclerView>(R.id.rvColors).apply {
            layoutManager = GridLayoutManager(requireContext(), 5)
            adapter = ColorAdapter(getColorList(), selectedColor) { selectedColor = it }
        }

        daysOfWeekGrid = view.findViewById(R.id.daysOfWeekGrid)
        val rg = view.findViewById<RadioGroup>(R.id.repeatTypeGroup)
        rg.check(R.id.buttonDaily)
        showWeeklyDays(false)
        createWeekButtons()
        rg.setOnCheckedChangeListener { _, id ->
            if (id == R.id.buttonDaily) {
                selectedRepeatType = RepeatType.DAILY
                showWeeklyDays(false)
            } else {
                selectedRepeatType = RepeatType.WEEKLY
                showWeeklyDays(true)
            }
        }

        view.findViewById<Button>(R.id.btnPickStartDate).setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(requireContext(),
                { _, y, m, d ->
                    startDate = String.format("%04d-%02d-%02d", y, m + 1, d)
                    view.findViewById<TextView>(R.id.tvStartDate).text = "Дата начала: $startDate"
                },
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        view.findViewById<Button>(R.id.btSaveHabit).setOnClickListener {
            val title = view.findViewById<EditText>(R.id.etHabitTitle).text.toString().trim()
            if (title.isEmpty() || selectedIcon == null || selectedColor == null) {
                Toast.makeText(requireContext(), "Заполните все поля!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val habit = HabitsEntity(
                title = title,
                iconResId = selectedIcon!!,
                color = selectedColor!!,
                repeatType = selectedRepeatType,
                repeatDays = if (selectedRepeatType == RepeatType.WEEKLY) selectedDays else null,
                startDate = startDate ?: getCurrentDate(),
                isCompleted = false
            )
            viewModel.addHabit(habit)
            findNavController().navigateUp()
        }
        view.findViewById<ImageButton>(R.id.buttonBack)
            .setOnClickListener { findNavController().navigateUp() }
    }

    private fun getCurrentDate(): String {
        val c = Calendar.getInstance()
        return String.format("%04d-%02d-%02d", c.get(Calendar.YEAR),
            c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH))
    }
}
