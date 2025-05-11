package com.example.productivity.ui.habits

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.productivity.AppDatabase
import com.example.productivity.R
import com.example.productivity.data.habits.HabitsEntity
import com.example.productivity.habits.HabitsRepositoryImpl
import com.example.productivity.habits.HabitsViewModel
import com.example.productivity.habits.HabitsViewModelFactory
import com.example.productivity.data.habits.RepeatType

/**
 * Экран редактирования одной привычки.
 * Поддерживает только изменения заголовка, иконки, цвета и повторений.
 */
class EditOneHabitFragment : BaseHabitFragment() {
    private var habitId: Int = 0
    private var initialTitle: String = ""
    private var startDate: String? = null
    private var selectedRepeatType: RepeatType = RepeatType.DAILY

    private val viewModel: HabitsViewModel by viewModels {
        HabitsViewModelFactory(
            HabitsRepositoryImpl(
                AppDatabase.getDatabase(requireContext()).habitsDao(),
                AppDatabase.getDatabase(requireContext()).habitCompletionDao()
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            habitId = args.getInt("habitId", 0)
            initialTitle = args.getString("habitTitle", "") ?: ""
            selectedIcon = args.getInt("iconResId", 0)
            selectedColor = args.getInt("habitColor", 0)
            selectedRepeatType = args.getParcelable("repeatType") ?: RepeatType.DAILY
            args.getIntegerArrayList("repeatDays")?.let { selectedDays.addAll(it) }
            startDate = args.getString("startDate")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_edit_one_habit, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etTitle = view.findViewById<EditText>(R.id.etHabitTitle)
        val iconsContainer = view.findViewById<LinearLayout>(R.id.linearLayoutIcons)
        val rvColors = view.findViewById<RecyclerView>(R.id.rvColors)
        val rgRepeat = view.findViewById<RadioGroup>(R.id.repeatTypeGroup)
        daysOfWeekGrid = view.findViewById(R.id.daysOfWeekGrid)
        val btnSave = view.findViewById<Button>(R.id.btSaveHabit)
        val btnBack = view.findViewById<ImageButton>(R.id.buttonBack)

        etTitle.setText(initialTitle)
        addIconsToScrollView(iconsContainer, icons)
        rvColors.layoutManager = GridLayoutManager(requireContext(), 5)
        rvColors.adapter = ColorAdapter(getColorList(), selectedColor) { selectedColor = it }

        rgRepeat.check(
            if (selectedRepeatType == RepeatType.WEEKLY) R.id.buttonWeekly
            else R.id.buttonDaily
        )
        showWeeklyDays(selectedRepeatType == RepeatType.WEEKLY)
        createWeekButtons()
        highlightSelectedDays()
        rgRepeat.setOnCheckedChangeListener { _, id ->
            if (id == R.id.buttonWeekly) {
                selectedRepeatType = RepeatType.WEEKLY
                showWeeklyDays(true)
            } else {
                selectedRepeatType = RepeatType.DAILY
                showWeeklyDays(false)
            }
        }

        btnSave.setOnClickListener {
            val newTitle = etTitle.text.toString().trim()
            if (newTitle.isEmpty() || selectedIcon == null || selectedColor == null) {
                Toast.makeText(requireContext(), "Заполните все поля!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val updated = HabitsEntity(
                id = habitId,
                title = newTitle,
                iconResId = selectedIcon!!,
                color = selectedColor!!,
                repeatType = selectedRepeatType,
                repeatDays = if (selectedRepeatType == RepeatType.WEEKLY) selectedDays else null,
                startDate = startDate ?: "",
                isCompleted = false
            )
            viewModel.updateHabitDetails(updated)
            findNavController().navigateUp()
        }

        btnBack.setOnClickListener { findNavController().navigateUp() }
    }
}
