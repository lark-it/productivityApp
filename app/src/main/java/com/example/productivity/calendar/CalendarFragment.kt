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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.productivity.AppDatabase
import com.example.productivity.OnDayClickListener
import com.example.productivity.R
import com.example.productivity.habits.HabitCompletionEntity
import com.example.productivity.habits.HabitsEntity
import com.example.productivity.habits.RepeatType
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
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

        val rvTaskList = view.findViewById<RecyclerView>(R.id.rv_task_list)
        rvTaskList.layoutManager = LinearLayoutManager(requireContext())

        taskAdapter = TaskAdapter(
            onTaskChecked = { task, isChecked -> updateTaskCompletion(task, isChecked) },
            onTaskEdit = { task -> navigateToEditTask(task) },
            onTaskDelete = { task -> deleteTask(task) },
            onHabitChecked = { habit, date, isChecked -> updateHabitCompletion(habit, date, isChecked) } // ✅ Добавили параметр
        )




        rvTaskList.adapter = taskAdapter

        val fabAddTask = view.findViewById<FloatingActionButton>(R.id.fab_add_task)
        fabAddTask.setOnClickListener {
            findNavController().navigate(R.id.AddTasksFragment)
        }



        loadTasksAndHabits()
    }
    private fun updateHabitCompletion(habit: HabitsEntity, date: String, isCompleted: Boolean) {
        lifecycleScope.launch {
            val completionDao = AppDatabase.getDatabase(requireContext()).habitCompletionDao()

            if (isCompleted) {
                completionDao.insertCompletion(HabitCompletionEntity(habit.id, date, true))
            } else {
                completionDao.deleteCompletion(habit.id, date)
            }

            loadTasksAndHabits() // Обновляем список после изменения
        }
    }
    private fun navigateToEditTask(task: TaskEntity) {
        val bundle = Bundle().apply {
            putInt("taskId", task.id)
            putString("taskTitle", task.title)
            putString("taskDate", task.date)
            putString("taskTime", task.time ?: "")
            putInt("taskImportance", task.importance)
            putBoolean("taskCompleted", task.isCompleted)
        }
        findNavController().navigate(R.id.editTaskFragment, bundle)
    }



    private fun updateTaskCompletion(task: TaskEntity, isCompleted: Boolean) {
        lifecycleScope.launch {
            val taskDao = AppDatabase.getDatabase(requireContext()).taskDao()
            val updatedTask = task.copy(isCompleted = isCompleted)
            taskDao.insertTask(updatedTask) // Обновляем в базе

            loadTasksAndHabits() // Обновляем UI
        }
    }

    private fun deleteTask(task: TaskEntity) {
        lifecycleScope.launch {
            val taskDao = AppDatabase.getDatabase(requireContext()).taskDao()
            taskDao.deleteTask(task)

            loadTasksAndHabits() // Обновляем UI после удаления
        }
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
            val completionDao = AppDatabase.getDatabase(requireContext()).habitCompletionDao()

            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            val tasks = taskDao.getAllTasks().filter { task ->
                val taskDate = task.date.split("-")
                val taskYear = taskDate[0].toInt()
                val taskMonth = taskDate[1].toInt()
                taskYear == year && taskMonth == month
            }.groupBy { it.date }

            val habits = habitDao.getAllHabits()
            val mergedData = mutableListOf<CalendarItem>()
            val uniqueDates = mutableSetOf<String>()

            val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            val tempCalendar = Calendar.getInstance()
            tempCalendar.set(year, calendar.get(Calendar.MONTH), 1)

            val habitsByDate = mutableMapOf<String, MutableList<HabitsEntity>>()

            for (habit in habits) {
                val startDate = dateFormat.parse(habit.startDate) ?: continue
                tempCalendar.time = startDate

                while (tempCalendar.get(Calendar.YEAR) == year && tempCalendar.get(Calendar.MONTH) + 1 == month) {
                    val dateKey = dateFormat.format(tempCalendar.time)
                    val dayOfWeek = tempCalendar.get(Calendar.DAY_OF_WEEK) - 1

                    val isHabitDay = when (habit.repeatType) {
                        RepeatType.DAILY -> true
                        RepeatType.WEEKLY -> habit.repeatDays?.contains(dayOfWeek) == true
                        RepeatType.MONTHLY -> tempCalendar.get(Calendar.DAY_OF_MONTH) == startDate.date
                    }

                    if (isHabitDay) {
                        uniqueDates.add(dateKey)
                        val isCompleted = completionDao.isHabitCompleted(habit.id, dateKey) ?: false
                        val updatedHabit = habit.copy(isCompleted = isCompleted)
                        habitsByDate.getOrPut(dateKey) { mutableListOf() }.add(updatedHabit)
                    }

                    tempCalendar.add(Calendar.DAY_OF_MONTH, 1)
                }
            }

            uniqueDates.addAll(tasks.keys)
            val sortedDates = uniqueDates.sorted()

            for (date in sortedDates) {
                mergedData.add(CalendarItem.DateHeader(date))

                habitsByDate[date]?.let { habitList ->
                    mergedData.addAll(habitList.map { CalendarItem.HabitItem(it) })
                }

                tasks[date]?.let { taskList ->
                    mergedData.addAll(taskList.map { CalendarItem.TaskItem(it) })
                }
            }

            taskAdapter.submitList(mergedData)
        }
    }


}
