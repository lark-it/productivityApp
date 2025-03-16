package com.example.productivity.calendar

import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
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
import com.example.productivity.home.UserRepository
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CalendarFragment : Fragment() {

    private lateinit var taskAdapter: TaskAdapter

    private var selectedDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    private val calendar = Calendar.getInstance()
    private val currentDate = Calendar.getInstance()

    private var taskProcessing = mutableSetOf<Int>()
    private var habitProcessing = mutableSetOf<Int>()

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
        rvCalendar.setHasFixedSize(true)
        rvCalendar.setPadding(16, 0, 16, 0)
        rvCalendar.clipToPadding = false
        rvCalendar.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                val spacing = resources.getDimensionPixelSize(R.dimen.calendar_item_spacing)
                outRect.left = spacing
                outRect.right = spacing
                outRect.top = spacing
                outRect.bottom = spacing
                val position = parent.getChildAdapterPosition(view)
                val spanCount = 7
                if (position < spanCount) {
                    outRect.top = spacing / 2
                }
                if (position % spanCount == 0) {
                    outRect.left = spacing / 2
                } else if ((position + 1) % spanCount == 0) {
                    outRect.right = spacing / 2
                }
            }
        })

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
            onTaskChecked           = { task, isChecked -> updateTaskCompletion(task, isChecked) },
            onTaskEdit              = { task -> navigateToEditTask(task) },
            onTaskDelete            = { task -> deleteTask(task) },
            onHabitCheckRequest     = { habit, date, isChecked -> checkHabitAllowedAndUpdate(habit, date, isChecked) },
            onTaskCompletionUpdated = { task, isChecked -> updateTaskCompletion(task, isChecked) },
            onHabitChecked          = { habitId, isChecked, date -> updateHabitCompletion(habitId, isChecked, date) }
        )
        rvTaskList.adapter = taskAdapter

        val fabAddTask = view.findViewById<FloatingActionButton>(R.id.fab_add_task)
        fabAddTask.setOnClickListener {
            val bundle = Bundle().apply {
                putString("selectedDate", selectedDate)
            }
            findNavController().navigate(R.id.AddTasksFragment, bundle)
        }

        updateMonthYearText(view)
        updateCalendarDays()
        loadTasksAndHabits()
    }

    private fun checkHabitAllowedAndUpdate(habit: HabitsEntity, date: String, isCompleted: Boolean): Boolean {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = Calendar.getInstance()
        val selectedDate = Calendar.getInstance().apply { time = dateFormat.parse(date)!! }
        val daysDiff = ((today.timeInMillis - selectedDate.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()

        return if (selectedDate.after(today)) {
            showToast("Вы не можете выполнить привычку за будущие дни")
            false
        } else if (daysDiff > 3) {
            showToast("Вы не можете выполнить привычку за такой давний срок")
            false
        } else {
            lifecycleScope.launch {
                val completionDao = AppDatabase.getDatabase(requireContext()).habitCompletionDao()
                if (isCompleted) {
                    completionDao.insertCompletion(HabitCompletionEntity(habit.id, date, true))
                } else {
                    completionDao.deleteCompletion(habit.id, date)
                }
                loadTasksAndHabits()
            }
            true
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
        if (taskProcessing.contains(task.id)) return
        taskProcessing.add(task.id)

        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            val taskDao = db.taskDao()
            val userDao = db.userDao()
            val userRepository = UserRepository(userDao)

            val wasCompleted = taskDao.isTaskCompleted(task.id)

            val coinChange = when (task.importance) {
                1 -> 1
                2 -> 2
                3 -> 3
                else -> 1
            }
            val xpChange = 1

            if (wasCompleted && !isCompleted) {
                userRepository.addCoinsAndXP(-coinChange, -xpChange)
            } else if (!wasCompleted && isCompleted) {
                userRepository.addCoinsAndXP(coinChange, xpChange)
            }

            taskDao.updateTaskCompletion(task.id, isCompleted)

            requireActivity().runOnUiThread {
                loadTasksAndHabits()
            }

            taskProcessing.remove(task.id)
        }
    }

    private fun updateHabitCompletion(habitId: Int, isCompleted: Boolean, date: String) {
        if (habitProcessing.contains(habitId)) return
        habitProcessing.add(habitId)

        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            val habitCompletionDao = db.habitCompletionDao()
            val userDao = db.userDao()
            val userRepository = UserRepository(userDao)

            val wasCompleted = habitCompletionDao.isHabitCompleted(habitId, date) ?: false

            val coinChange = 2
            val xpChange = 1

            if (!isCompleted) {
                // ✅ Удаляем выполнение привычки **только за этот конкретный день**
                Log.d("CalendarFragment", "❌ Удаляем выполнение habitId=$habitId за дату $date")
                userRepository.addCoinsAndXP(-coinChange, -xpChange)
                habitCompletionDao.deleteCompletion(habitId, date)
            } else {
                // ✅ Привычка выполняется впервые в этот день
                Log.d("CalendarFragment", "✅ Добавляем выполнение habitId=$habitId за дату $date")
                userRepository.addCoinsAndXP(coinChange, xpChange)
                habitCompletionDao.insertCompletion(HabitCompletionEntity(habitId, date, true))
            }

            requireActivity().runOnUiThread {
                loadTasksAndHabits()
            }

            habitProcessing.remove(habitId)
        }
    }

    private fun deleteTask(task: TaskEntity) {
        lifecycleScope.launch {
            val taskDao = AppDatabase.getDatabase(requireContext()).taskDao()
            taskDao.deleteTask(task)
            loadTasksAndHabits()
        }
    }

    private fun updateMonthYearText(view: View) {
        val yearFormatter = SimpleDateFormat("yyyy", Locale.getDefault())
        val monthFormatter = SimpleDateFormat("MMMM", Locale.getDefault())
        val yearText = yearFormatter.format(calendar.time)
        val monthText = monthFormatter.format(calendar.time)
        view.findViewById<TextView>(R.id.tv_year).text = yearText
        view.findViewById<TextView>(R.id.tv_month).text = monthText
    }

    private fun updateCalendarDays() {
        lifecycleScope.launch {
            val days = generateDaysForMonth(
                year = calendar.get(Calendar.YEAR),
                month = calendar.get(Calendar.MONTH)
            )
            val rvCalendar = view?.findViewById<RecyclerView>(R.id.rv_calendar)
            rvCalendar?.adapter = CalendarAdapter(
                days           = days,
                daysWithTasks = emptySet(),
                onDayClickListener = object : OnDayClickListener {
                    override fun onDayClick(day: String) {
                        if (day.isNotEmpty() && day.toIntOrNull() != null) {
                            selectedDate = "${calendar.get(Calendar.YEAR)}-" +
                                    "${String.format("%02d", calendar.get(Calendar.MONTH) + 1)}-" +
                                    day.padStart(2, '0')
                        }
                    }
                },
                currentDate     = currentDate,
                displayedMonth  = calendar.get(Calendar.MONTH),
                displayedYear   = calendar.get(Calendar.YEAR)
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
            val taskDao        = AppDatabase.getDatabase(requireContext()).taskDao()
            val habitDao       = AppDatabase.getDatabase(requireContext()).habitsDao()
            val completionDao  = AppDatabase.getDatabase(requireContext()).habitCompletionDao()
            val year           = calendar.get(Calendar.YEAR)
            val month          = calendar.get(Calendar.MONTH) + 1
            val dateFormat     = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            val tasks = taskDao.getAllTasks().filter { task ->
                val parts = task.date.split("-")
                parts[0].toInt() == year && parts[1].toInt() == month
            }.groupBy { it.date }

            val habits = habitDao.getAllHabits()
            val mergedData = mutableListOf<CalendarItem>()
            val uniqueDates = mutableSetOf<String>()

            val tempCal = Calendar.getInstance().apply { set(year, calendar.get(Calendar.MONTH), 1) }
            val habitsByDate = mutableMapOf<String, MutableList<HabitsEntity>>()

            for (habit in habits) {
                val startDate = dateFormat.parse(habit.startDate) ?: continue
                tempCal.time = startDate
                while (tempCal.get(Calendar.YEAR) == year &&
                    tempCal.get(Calendar.MONTH) + 1 == month
                ) {
                    val dateKey   = dateFormat.format(tempCal.time)
                    val dayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK) - 1
                    val isHabitDay = when (habit.repeatType) {
                        RepeatType.DAILY -> true
                        RepeatType.WEEKLY -> habit.repeatDays?.contains(dayOfWeek) == true
                        RepeatType.MONTHLY -> tempCal.get(Calendar.DAY_OF_MONTH) == startDate.date
                    }

                    if (isHabitDay) {
                        uniqueDates.add(dateKey)
                        val isCompleted = completionDao.isHabitCompleted(habit.id, dateKey) ?: false
                        val habitCopy   = habit.copy(isCompleted = isCompleted)
                        habitsByDate.getOrPut(dateKey) { mutableListOf() }.add(habitCopy)
                    }
                    tempCal.add(Calendar.DAY_OF_MONTH, 1)
                }
            }

            uniqueDates.addAll(tasks.keys)
            val sortedDates = uniqueDates.sorted()

            for (date in sortedDates) {
                mergedData.add(CalendarItem.DateHeader(date))
                habitsByDate[date]?.let { list ->
                    mergedData.addAll(list.map {
                        CalendarItem.HabitItem(it.copy(startDate = date))
                    })
                }
                tasks[date]?.let { list ->
                    mergedData.addAll(list.map { CalendarItem.TaskItem(it) })
                }
            }

            requireActivity().runOnUiThread {
                taskAdapter.submitList(mergedData)
            }
        }
    }

    private fun showToast(message: String) {
        requireActivity().runOnUiThread {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }
}
