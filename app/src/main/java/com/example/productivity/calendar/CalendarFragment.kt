package com.example.productivity.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.productivity.AppDatabase
import com.example.productivity.OnDayClickListener
import com.example.productivity.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CalendarFragment : Fragment() {
    private lateinit var taskAdapter: TaskAdapter

    private val calendar = Calendar.getInstance()
    private val currentDate = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_calendar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvCalendar = view.findViewById<RecyclerView>(R.id.rv_calendar)
        rvCalendar.layoutManager = GridLayoutManager(requireContext(), 7)

        updateMonthYearText(view)
        updateCalendarDays()

        val btnPrevious = view.findViewById<ImageButton>(R.id.btn_previous_month)
        val btnNext = view.findViewById<ImageButton>(R.id.btn_next_month)

        btnPrevious.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            updateMonthYearText(view)
            updateCalendarDays()
            loadTasksAndHabits()
        }

        btnNext.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            updateMonthYearText(view)
            updateCalendarDays()
            loadTasksAndHabits()
        }

        val rvTaskList = view.findViewById<RecyclerView>(R.id.rv_task_list) // ✅ Исправлено!
        rvTaskList.layoutManager = LinearLayoutManager(requireContext())
        taskAdapter = TaskAdapter()
        rvTaskList.adapter = taskAdapter

        val fabAddTask = view.findViewById<FloatingActionButton>(R.id.fab_add_task)
        fabAddTask.setOnClickListener {
            showAddTaskDialog()
        }

        loadTasksAndHabits()
    }

    private fun updateMonthYearText(view: View) {
        val formatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        val monthYearText = formatter.format(calendar.time)

        val monthYearTextView = view.findViewById<TextView>(R.id.tv_month_year)
        monthYearTextView.text = monthYearText
    }

    private fun updateCalendarDays() {
        lifecycleScope.launch {
            val days = generateDaysForMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH))
            val rvCalendar = view?.findViewById<RecyclerView>(R.id.rv_calendar)

            rvCalendar?.adapter = CalendarAdapter(
                days = days,
                daysWithTasks = emptySet(), // Оставляем пустым, так как убрали задачи
                onDayClickListener = object : OnDayClickListener {
                    override fun onDayClick(day: String) {
                        // Здесь можно добавить новую обработку кликов по дням
                    }
                },
                currentDate = currentDate,
                displayedMonth = calendar.get(Calendar.MONTH),
                displayedYear = calendar.get(Calendar.YEAR)
            )
        }
    }

    private fun generateDaysForMonth(year: Int, month: Int): List<String> {
        val days = mutableListOf<String>()
        val weekDays = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
        days.addAll(weekDays)

        val calendar = Calendar.getInstance()
        calendar.set(year, month, 1)

        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 2
        val offset = if (firstDayOfWeek < 0) 6 else firstDayOfWeek
        for (i in 0 until offset) {
            days.add("")
        }

        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (day in 1..daysInMonth) {
            days.add(day.toString())
        }

        return days
    }

    private fun loadTasksAndHabits() {
        lifecycleScope.launch {
            val taskDao = AppDatabase.getDatabase(requireContext()).taskDao()
            val habitDao = AppDatabase.getDatabase(requireContext()).habitsDao()

            val tasks = taskDao.getAllTasks().groupBy { it.date }
            val habits = habitDao.getAllHabits().groupBy { it.startDate }

            val mergedData = mutableListOf<CalendarItem>()

            val uniqueDates = (tasks.keys + habits.keys).sorted()

            for (date in uniqueDates) {
                mergedData.add(CalendarItem.DateHeader(date))

                tasks[date]?.let { taskList ->
                    mergedData.addAll(taskList.map { CalendarItem.TaskItem(it) })
                }

                habits[date]?.let { habitList ->
                    mergedData.addAll(habitList.map { CalendarItem.HabitItem(it) })
                }
            }

            taskAdapter.submitList(mergedData)
        }
    }
    private fun showAddTaskDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_task, null)
        val taskTitleInput = dialogView.findViewById<EditText>(R.id.et_task_title)

        AlertDialog.Builder(requireContext())
            .setTitle("Добавить задачу")
            .setView(dialogView)
            .setPositiveButton("Добавить") { _, _ ->
                val taskTitle = taskTitleInput.text.toString().trim()
                if (taskTitle.isNotEmpty()) {
                    saveTaskToDatabase(taskTitle)
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
    private fun saveTaskToDatabase(title: String) {
        lifecycleScope.launch {
            val taskDao = AppDatabase.getDatabase(requireContext()).taskDao()

            // Используем текущую дату как дату задачи (или можно добавить выбор даты в диалог)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val currentDate = dateFormat.format(Calendar.getInstance().time)

            val newTask = TaskEntity(
                title = title,
                date = currentDate,
                time = null,
                importance = 1,
                isCompleted = false
            )

            taskDao.insertTask(newTask)

            loadTasksAndHabits()
        }
    }


}
