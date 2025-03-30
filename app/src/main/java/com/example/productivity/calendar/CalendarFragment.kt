package com.example.productivity.calendar

import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
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
import com.example.productivity.home.HabitBonusEntity
import com.example.productivity.home.MainViewModel
import com.example.productivity.home.MainViewModelFactory
import com.example.productivity.home.UserRepository
import com.example.productivity.util.Constants.MAX_LIVES
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CalendarFragment : Fragment() {

    private lateinit var taskAdapter: TaskAdapter
    private lateinit var db: AppDatabase
    private lateinit var habitDao: AppDatabase.() -> com.example.productivity.habits.HabitsDao
    private lateinit var habitCompletionDao: AppDatabase.() -> com.example.productivity.habits.HabitCompletionDao
    private lateinit var userRepository: UserRepository
    private lateinit var viewModel: MainViewModel

    private var selectedDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    private val calendar = Calendar.getInstance()
    private val currentDate = Calendar.getInstance()

    private var taskProcessing = mutableSetOf<Int>()
    private var habitProcessing = mutableSetOf<Int>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_calendar, container, false)
        db = AppDatabase.getDatabase(requireContext())
        habitDao = { habitsDao() }
        habitCompletionDao = { habitCompletionDao() }
        userRepository = UserRepository(db.userDao())
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val taskDao = db.taskDao()
        viewModel = ViewModelProvider(
            requireActivity(),
            MainViewModelFactory(userRepository, taskDao, db.habitsDao(), requireContext())
        ).get(MainViewModel::class.java)

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
                if (position < spanCount) outRect.top = spacing / 2
                if (position % spanCount == 0) outRect.left = spacing / 2
                else if ((position + 1) % spanCount == 0) outRect.right = spacing / 2
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
            onTaskChecked = { task, isChecked -> updateTaskCompletion(task, isChecked) },
            onTaskEdit = { task -> navigateToEditTask(task) },
            onTaskDelete = { task -> deleteTask(task) },
            onHabitCheckRequest = { habit, date, isChecked -> checkHabitAllowedAndUpdate(habit, date, isChecked) },
            onTaskCompletionUpdated = { task, isChecked -> updateTaskCompletion(task, isChecked) },
            onHabitChecked = { habitId, isChecked, date -> updateHabitCompletion(habitId, isChecked, date) }
        )
        rvTaskList.adapter = taskAdapter

        val fabAddTask = view.findViewById<FloatingActionButton>(R.id.fab_add_task)
        fabAddTask.setOnClickListener {
            val bundle = Bundle().apply { putString("selectedDate", selectedDate) }
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
            val taskDao = db.taskDao()
            val wasCompleted = taskDao.isTaskCompleted(task.id)
            val coinChange = when (task.importance) { 1 -> 1; 2 -> 2; 3 -> 3; else -> 1 }
            val xpChange = 1

            if (wasCompleted && !isCompleted) {
                userRepository.addCoinsAndXP(-coinChange, -xpChange)
                viewModel.decreaseLifeIfRecent(task.date)
            } else if (!wasCompleted && isCompleted) {
                userRepository.addCoinsAndXP(coinChange, xpChange)
                val currentLives = userRepository.getUser().lives
                if (currentLives < MAX_LIVES) {
                    val newLives = currentLives + 1
                    userRepository.updateLives(newLives)
                    viewModel.lives.postValue(newLives)
                    Log.d("CalendarFragment", "💖 Восстановлена жизнь за задачу на ${task.date}, теперь $newLives")
                }
            }

            taskDao.updateTaskCompletion(task.id, isCompleted)
            requireActivity().runOnUiThread { loadTasksAndHabits() }
            taskProcessing.remove(task.id)
        }
    }

    private fun updateHabitCompletion(habitId: Int, isCompleted: Boolean, date: String) {
        if (habitProcessing.contains(habitId)) return
        habitProcessing.add(habitId)

        lifecycleScope.launch {
            val completionDao = db.habitCompletionDao()
            val wasCompleted = completionDao.isHabitCompleted(habitId, date) ?: false
            val coinChange = 2
            val xpChange = 1

            if (isCompleted && !wasCompleted) {
                Log.d("CalendarFragment", "Добавляем выполнение habitId=$habitId за дату $date")
                completionDao.insertCompletion(HabitCompletionEntity(habitId, date, true))
                userRepository.addCoinsAndXP(coins = coinChange, xp = xpChange)
                // Добавляем жизнь при выполнении привычки
                val currentLives = userRepository.getUser().lives
                if (currentLives < MAX_LIVES) {
                    val newLives = currentLives + 1
                    userRepository.updateLives(newLives)
                    viewModel.lives.postValue(newLives)
                    Log.d("CalendarFragment", "💖 Восстановлена жизнь за привычку на $date, теперь $newLives")
                }
                checkAndAwardWeeklyBonus(habitId, date)
            } else if (!isCompleted && wasCompleted) {
                Log.d("CalendarFragment", "Удаляем выполнение habitId=$habitId за дату $date")
                completionDao.deleteCompletion(habitId, date)
                userRepository.addCoinsAndXP(coins = -coinChange, xp = -xpChange)
                viewModel.decreaseLifeIfRecent(date) // Отнимаем жизнь при отмене
                checkAndRevokeWeeklyBonus(habitId, date)
            } else {
                Log.d("CalendarFragment", "ℹ️ Состояние не изменилось для habitId=$habitId за дату $date")
            }

            requireActivity().runOnUiThread { loadTasksAndHabits() }
            habitProcessing.remove(habitId)
        }
    }

    private fun deleteTask(task: TaskEntity) {
        lifecycleScope.launch {
            val taskDao = db.taskDao()
            taskDao.deleteTask(task)
            loadTasksAndHabits()
        }
    }

    private fun updateMonthYearText(view: View) {
        val yearFormatter = SimpleDateFormat("yyyy", Locale.getDefault())
        val monthFormatter = SimpleDateFormat("MMMM", Locale.getDefault())
        view.findViewById<TextView>(R.id.tv_year).text = yearFormatter.format(calendar.time)
        view.findViewById<TextView>(R.id.tv_month).text = monthFormatter.format(calendar.time)
    }

    private fun updateCalendarDays() {
        lifecycleScope.launch {
            val days = generateDaysForMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH))
            val rvCalendar = view?.findViewById<RecyclerView>(R.id.rv_calendar)
            rvCalendar?.adapter = CalendarAdapter(
                days = days,
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
        for (i in 0 until offset) days.add("")
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (day in 1..daysInMonth) days.add(day.toString())
        return days
    }

    private fun loadTasksAndHabits() {
        lifecycleScope.launch {
            val taskDao = db.taskDao()
            val habitDao = db.habitDao()
            val completionDao = db.habitCompletionDao()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

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
                val tempCal = Calendar.getInstance().apply { time = startDate }
                val endCal = Calendar.getInstance().apply { set(year, month - 1, getActualMaximum(Calendar.DAY_OF_MONTH)) }

                while (tempCal.time <= endCal.time) {
                    val dateKey = dateFormat.format(tempCal.time)
                    val dayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK) - 1
                    val isHabitDay = when (habit.repeatType) {
                        RepeatType.DAILY -> true
                        RepeatType.WEEKLY -> habit.repeatDays?.contains(dayOfWeek) == true
                    }

                    if (isHabitDay && tempCal.get(Calendar.YEAR) == year && tempCal.get(Calendar.MONTH) + 1 == month) {
                        uniqueDates.add(dateKey)
                        val isCompleted = completionDao.isHabitCompleted(habit.id, dateKey) ?: false
                        val habitCopy = habit.copy(isCompleted = isCompleted)
                        habitsByDate.getOrPut(dateKey) { mutableListOf() }.add(habitCopy)
                    }
                    tempCal.add(Calendar.DAY_OF_MONTH, 1)
                }

                checkAndAwardWeeklyBonus(habit.id, selectedDate)
            }

            uniqueDates.addAll(tasks.keys)
            val sortedDates = uniqueDates.sorted()

            for (date in sortedDates) {
                mergedData.add(CalendarItem.DateHeader(date))
                habitsByDate[date]?.let { list ->
                    mergedData.addAll(list.map { CalendarItem.HabitItem(it.copy(startDate = date)) })
                }
                tasks[date]?.let { list ->
                    mergedData.addAll(list.map { CalendarItem.TaskItem(it) })
                }
            }

            requireActivity().runOnUiThread {
                taskAdapter.submitList(mergedData)
                val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val todayIndex = mergedData.indexOfFirst {
                    it is CalendarItem.DateHeader && it.date == todayDate
                }
                if (todayIndex != -1) {
                    view?.findViewById<RecyclerView>(R.id.rv_task_list)?.post {
                        view?.findViewById<RecyclerView>(R.id.rv_task_list)?.scrollToPosition(todayIndex)
                    }
                }
            }

        }
    }

    private fun getWeekDatesForHabit(habit: HabitsEntity): List<String> {
        val startDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(habit.startDate) ?: return emptyList()
        val calendar = Calendar.getInstance()
        calendar.time = startDate

        val weekDates = mutableListOf<String>()
        repeat(7) {
            while (habit.repeatType == RepeatType.WEEKLY && habit.repeatDays?.contains(calendar.get(Calendar.DAY_OF_WEEK) - 1) == false) {
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }
            weekDates.add(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time))
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        return weekDates
    }

    private fun getWeekBoundaries(date: String): Pair<String, String> {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        calendar.time = dateFormat.parse(date) ?: return "" to ""
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val weekStart = dateFormat.format(calendar.time)
        calendar.add(Calendar.DAY_OF_MONTH, 6)
        val weekEnd = dateFormat.format(calendar.time)
        return weekStart to weekEnd
    }

    private suspend fun checkAndAwardWeeklyBonus(habitId: Int, date: String) {
        val habit = db.habitDao().getAllHabits().find { it.id == habitId } ?: return
        val completedDates = db.habitCompletionDao().getCompletedDates(habitId).sorted()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        Log.d("CalendarFragment", "Checking bonus for habitId=$habitId, repeatType=${habit.repeatType}, repeatDays=${habit.repeatDays}")
        Log.d("CalendarFragment", "Completed dates: $completedDates")

        var allDaysCompleted = false
        val bonusCoins = 7

        when (habit.repeatType) {
            RepeatType.DAILY -> {
                if (completedDates.size >= 7) {
                    val firstCompletion = dateFormat.parse(completedDates[0]) ?: return
                    val calendar = Calendar.getInstance().apply { time = firstCompletion }
                    val requiredDays = mutableListOf<String>()
                    repeat(7) {
                        requiredDays.add(dateFormat.format(calendar.time))
                        calendar.add(Calendar.DAY_OF_MONTH, 1)
                    }
                    Log.d("CalendarFragment", "Required days for DAILY: $requiredDays")
                    allDaysCompleted = requiredDays.all { it in completedDates }
                }
            }
            RepeatType.WEEKLY -> {
                habit.repeatDays?.let { repeatDays ->
                    if (completedDates.isNotEmpty() && repeatDays.isNotEmpty()) {
                        val firstCompletion = dateFormat.parse(completedDates[0]) ?: return
                        val calendar = Calendar.getInstance().apply { time = firstCompletion }
                        val requiredDays = mutableListOf<String>()
                        var daysCounted = 0

                        while (daysCounted < 7) {
                            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
                            if (repeatDays.contains(dayOfWeek)) {
                                requiredDays.add(dateFormat.format(calendar.time))
                                daysCounted++
                            }
                            calendar.add(Calendar.DAY_OF_MONTH, 1)
                        }
                        Log.d("CalendarFragment", "Required days for WEEKLY: $requiredDays")
                        allDaysCompleted = requiredDays.all { it in completedDates }
                    }
                }
            }
        }

        Log.d("CalendarFragment", "All days completed: $allDaysCompleted")

        val (weekStart, weekEnd) = if (completedDates.isNotEmpty()) {
            completedDates[0] to dateFormat.format(
                Calendar.getInstance().apply {
                    time = dateFormat.parse(completedDates[0])!!
                    add(Calendar.DAY_OF_MONTH, 6)
                }.time
            )
        } else {
            getWeekBoundaries(date)
        }
        val bonusAwarded = db.habitCompletionDao().hasBonusBeenAwarded(habitId, weekStart, weekEnd)
        Log.d("CalendarFragment", "Bonus already awarded: $bonusAwarded")

        if (allDaysCompleted && !bonusAwarded) {
            userRepository.addCoinsAndXP(coins = bonusCoins, xp = 0)
            db.habitCompletionDao().markBonusAwarded(HabitBonusEntity(habitId = habitId, weekStart = weekStart, weekEnd = weekEnd))
            Log.d("CalendarFragment", "🎉 Начислен бонус за неделю для habitId=$habitId")
        }
    }

    private suspend fun checkAndRevokeWeeklyBonus(habitId: Int, date: String) {
        val habit = db.habitDao().getAllHabits().find { it.id == habitId } ?: return
        val completedDates = db.habitCompletionDao().getCompletedDates(habitId).sorted()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        Log.d("CalendarFragment", "Revoking bonus check for habitId=$habitId, repeatType=${habit.repeatType}, repeatDays=${habit.repeatDays}")
        Log.d("CalendarFragment", "Completed dates: $completedDates")

        var allDaysCompleted = false

        when (habit.repeatType) {
            RepeatType.DAILY -> {
                if (completedDates.size >= 7) {
                    val firstCompletion = dateFormat.parse(completedDates[0]) ?: return
                    val calendar = Calendar.getInstance().apply { time = firstCompletion }
                    val requiredDays = mutableListOf<String>()
                    repeat(7) {
                        requiredDays.add(dateFormat.format(calendar.time))
                        calendar.add(Calendar.DAY_OF_MONTH, 1)
                    }
                    allDaysCompleted = requiredDays.all { it in completedDates }
                }
            }
            RepeatType.WEEKLY -> {
                habit.repeatDays?.let { repeatDays ->
                    if (completedDates.isNotEmpty() && repeatDays.isNotEmpty()) {
                        val firstCompletion = dateFormat.parse(completedDates[0]) ?: return
                        val calendar = Calendar.getInstance().apply { time = firstCompletion }
                        val requiredDays = mutableListOf<String>()
                        var daysCounted = 0
                        while (daysCounted < 7) {
                            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
                            if (repeatDays.contains(dayOfWeek)) {
                                requiredDays.add(dateFormat.format(calendar.time))
                                daysCounted++
                            }
                            calendar.add(Calendar.DAY_OF_MONTH, 1)
                        }
                        allDaysCompleted = requiredDays.all { it in completedDates }
                    }
                }
            }
        }

        val (weekStart, weekEnd) = if (completedDates.isNotEmpty()) {
            completedDates[0] to dateFormat.format(
                Calendar.getInstance().apply {
                    time = dateFormat.parse(completedDates[0])!!
                    add(Calendar.DAY_OF_MONTH, 6)
                }.time
            )
        } else {
            getWeekBoundaries(date)
        }
        val bonusAwarded = db.habitCompletionDao().hasBonusBeenAwarded(habitId, weekStart, weekEnd)

        if (!allDaysCompleted && bonusAwarded) {
            userRepository.addCoinsAndXP(coins = -7, xp = 0)
            db.habitCompletionDao().revokeBonus(habitId, weekStart, weekEnd)
            Log.d("CalendarFragment", "Бонус отозван для habitId=$habitId")
        }
    }

    private fun showToast(message: String) {
        requireActivity().runOnUiThread {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }
}