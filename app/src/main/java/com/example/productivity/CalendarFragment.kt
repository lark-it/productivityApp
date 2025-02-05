package com.example.productivity

import android.app.TimePickerDialog
import android.media.Image
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CalendarFragment : Fragment(),OnDayClickListener  {
    //задачи
    private val tasks = mutableListOf<TaskEntity>()
    private lateinit var taskAdapter: TaskAdapter
    //календарь
    private val calendar = Calendar.getInstance() // Храним текущую дату
    private var selectedDay: String? = null
    private val currentDate = Calendar.getInstance() // Объект текущей даты
    //база данных
    private lateinit var database: AppDatabase
    private lateinit var taskDao: TaskDao

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Инфлейтим (создаём) макет для этого фрагмента
        return inflater.inflate(R.layout.fragment_calendar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Добавляем новое поле isCompleted с дефолтным значением false
                database.execSQL("ALTER TABLE tasks ADD COLUMN isCompleted INTEGER NOT NULL DEFAULT 0")
            }
        }
        database = Room.databaseBuilder(
            requireContext(),
            AppDatabase::class.java,
            "task_database"
        )
            .addMigrations(MIGRATION_1_2) // Подключаем миграцию
            .build()

        taskDao = database.taskDao()

        loadTasksForSelectedDay()

        //календарь
        val rvCalendar = view.findViewById<RecyclerView>(R.id.rv_calendar)
        rvCalendar.layoutManager = GridLayoutManager(requireContext(), 7)

        updateMonthYearText(view) // Отображаем текущий месяц и год
        updateCalendarDays() // Отображаем дни текущего месяца

        val btnPrevious = view.findViewById<ImageButton>(R.id.btn_previous_month)
        val btnNext = view.findViewById<ImageButton>(R.id.btn_next_month)

        btnPrevious.setOnClickListener {
            calendar.add(Calendar.MONTH, -1) // Переключаемся на прошлый месяц
            selectedDay = null // Сбрасываем выбранный день, чтобы не было бага
            updateMonthYearText(view)
            updateCalendarDays()
            loadTasksForSelectedDay() // Загружаем задачи только за новый месяц
        }

        btnNext.setOnClickListener {
            calendar.add(Calendar.MONTH, 1) // Переключаемся на следующий месяц
            selectedDay = null
            updateMonthYearText(view)
            updateCalendarDays()
            loadTasksForSelectedDay()
        }

        //задачи
        val rvTasks = view.findViewById<RecyclerView>(R.id.rv_task_list)
        rvTasks.layoutManager = LinearLayoutManager(requireContext())
        taskAdapter = TaskAdapter(
            items = emptyList(),
            onTaskDelete = { task -> deleteTask(task) },
            onTaskEdit = { task -> openEditDialog(task) { updatedTask -> updateTask(updatedTask) } },
            onTaskCompleted = { task -> updateTask(task) } // Новый колбэк для обновления выполнения
        )

        rvTasks.adapter = taskAdapter

        val fabAddTask = view.findViewById<FloatingActionButton>(R.id.fab_add_task)
        fabAddTask.setOnClickListener {
            openTaskDialog()
        }


    }

    override fun onDayClick(day: String) {
        selectedDay = "${calendar.get(Calendar.YEAR)}-${String.format("%02d", calendar.get(Calendar.MONTH) + 1)}-${day.padStart(2, '0')}"

        lifecycleScope.launch {
            val tasksForSelectedDay = taskDao.getTasksByDate(selectedDay!!)
            val groupedItems = groupTasksByDate(tasksForSelectedDay)

            taskAdapter.updateItems(groupedItems)
        }
    }

    fun generateDaysForMonth(year: Int, month: Int): List<String> {
        val days = mutableListOf<String>()

        val weekDays = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
        days.addAll(weekDays)

        val calendar = Calendar.getInstance()
        calendar.set(year, month, 1)

        // Добавляем пустые ячейки перед первым днём месяца
        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 2 // День недели 1-го числа (0 = Понедельник)
        val offset = if (firstDayOfWeek < 0) 6 else firstDayOfWeek
        for (i in 0 until offset) {
            days.add("") // Пустая ячейка
        }

        // Добавляем дни месяца
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (day in 1..daysInMonth) {
            days.add(day.toString())
        }

        return days
    }

    private fun updateMonthYearText(view: View) {
        val formatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        val monthYearText = formatter.format(calendar.time)

        val monthYearTextView = view.findViewById<TextView>(R.id.tv_month_year)
        monthYearTextView.text = monthYearText
    }

    private fun updateCalendarDays() {
        lifecycleScope.launch {
            val daysWithTasks = getDaysWithTasks()
            val days = generateDaysForMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH))
            val rvCalendar = view?.findViewById<RecyclerView>(R.id.rv_calendar)

            rvCalendar?.adapter = CalendarAdapter(
                days = days,
                daysWithTasks = daysWithTasks,
                onDayClickListener = object : OnDayClickListener {
                    override fun onDayClick(day: String) {
                        selectedDay = "${calendar.get(Calendar.YEAR)}-${String.format("%02d", calendar.get(Calendar.MONTH) + 1)}-${day.padStart(2, '0')}"
                        loadTasksForSelectedDay()
                    }
                },
                currentDate = currentDate,
                displayedMonth = calendar.get(Calendar.MONTH),
                displayedYear = calendar.get(Calendar.YEAR)
            )
            loadTasksForCurrentMonth()
        }
    }

    private fun loadTasksForCurrentMonth() {
        val year = calendar.get(Calendar.YEAR)
        val month = String.format("%02d", calendar.get(Calendar.MONTH) + 1) // Форматируем месяц в "01", "02" и т. д.

        lifecycleScope.launch {
            val tasksForMonth = taskDao.getTasksByMonth("$year-$month") // Загружаем задачи за месяц
            val groupedItems = groupTasksByDate(tasksForMonth)

            taskAdapter.updateItems(groupedItems) // Обновляем адаптер списка задач

            if (tasksForMonth.isEmpty()) {
                // Если задач нет, показываем сообщение
                taskAdapter.updateItems(listOf(HeaderItem("Задач в этот месяц нет")))
            }
        }
    }

    private fun openTaskDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_task, null)
        val taskTitle = dialogView.findViewById<EditText>(R.id.et_task_title)
        val taskTime = dialogView.findViewById<EditText>(R.id.et_task_time)
        val taskImportance = dialogView.findViewById<EditText>(R.id.et_task_importance)

        // Устанавливаем обработчик для выбора времени через TimePickerDialog
        taskTime.setOnClickListener {
            showTimePicker(taskTime)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Добавить задачу")
            .setView(dialogView)
            .setPositiveButton("Добавить") { _, _ ->
                val title = taskTitle.text.toString().trim()
                val time = taskTime.text.toString().trim()
                val importance = taskImportance.text.toString().toIntOrNull()?: 1

                if (title.isEmpty() || importance !in 1..3) {
                    println("Некорректные данные задачи")
                    return@setPositiveButton
                }
                addTask(title, time, importance)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showTimePicker(editText: EditText, initialTime: String? = null) {
        val calendar = Calendar.getInstance()

        // Если в задаче уже есть время, парсим его
        if (!initialTime.isNullOrEmpty()) {
            val parts = initialTime.split(":")
            if (parts.size == 2) {
                val hour = parts[0].toIntOrNull() ?: calendar.get(Calendar.HOUR_OF_DAY)
                val minute = parts[1].toIntOrNull() ?: calendar.get(Calendar.MINUTE)
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
            }
        }

        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, selectedHour, selectedMinute ->
                val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                editText.setText(formattedTime)
            },
            hour,
            minute,
            true // 24-часовой формат
        )

        timePickerDialog.show()
    }

    private fun addTask(title: String, time: String?, importance: Int) {
        val task = TaskEntity(
            title = title,
            date = selectedDay ?: currentDateToString(),
            time = time,
            importance = importance
        )

        lifecycleScope.launch {
            taskDao.insertTask(task)
            loadTasksForSelectedDay()
        }
    }
    private fun loadTasksForSelectedDay() {
        val year = calendar.get(Calendar.YEAR)
        val month = String.format("%02d", calendar.get(Calendar.MONTH) + 1) // Приводим к формату "01", "02" и т. д.

        lifecycleScope.launch {
            val tasksForMonth = taskDao.getTasksByMonth("$year-$month") // Загружаем задачи только за текущий месяц
            val groupedItems = groupTasksByDate(tasksForMonth)

            taskAdapter.updateItems(groupedItems) // Обновляем список задач
        }
    }

    private fun groupTasksByDate(tasks: List<TaskEntity>): List<ListItem> {
        val sortedTasks = tasks.sortedBy { it.date }
        val grouped = sortedTasks.groupBy { it.date }

        val items = mutableListOf<ListItem>()

        // Группировка задач по дате
        grouped.forEach { (date, tasksForDate) ->
            items.add(HeaderItem(date)) // Добавляем заголовок с датой
            // Обычные задачи
            val activeTasks = tasksForDate.filter { !it.isCompleted }
            items.addAll(activeTasks.map { TaskItem(it) })

            // Добавляем заголовок "Выполненные задачи", если есть выполненные задачи
            val completedTasks = tasksForDate.filter { it.isCompleted }
            if (completedTasks.isNotEmpty()) {
                items.add(CompletedHeaderItem(date, completedTasks.size)) // Добавляем заголовок
                items.addAll(completedTasks.map { TaskItem(it) }) // Добавляем выполненные задачи
            }
        }

        return items
    }

    private fun currentDateToString(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return formatter.format(Calendar.getInstance().time) // Всегда форматируем полную дату
    }

    private fun deleteTask(task: TaskEntity) {
        lifecycleScope.launch {
            taskDao.deleteTask(task) // Удаляем задачу из базы данных
            loadTasksForSelectedDay() // Перезагружаем список задач

            // Проверяем, остались ли задачи на этот день
            val remainingTasks = taskDao.getTasksByDate(task.date)
            if (remainingTasks.isEmpty()) {
                updateCalendarDays() // Обновляем календарь, чтобы убрать выделение дня
            }
        }
    }

    private fun updateTask(task: TaskEntity) {
        lifecycleScope.launch {
            taskDao.insertTask(task) // Обновляем задачу в базе
            loadTasksForSelectedDay() // Немедленно обновляем список
        }
    }

    private fun openEditDialog(task: TaskEntity, onSave: (TaskEntity) -> Unit) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_task, null)
        val taskTitle = dialogView.findViewById<EditText>(R.id.et_task_title)
        val taskTime = dialogView.findViewById<EditText>(R.id.et_task_time)
        val taskImportance = dialogView.findViewById<EditText>(R.id.et_task_importance)

        // Заполняем текущие значения задачи
        taskTitle.setText(task.title)
        taskTime.setText(task.time ?: "")
        taskImportance.setText(task.importance.toString())

        // Обработчик для выбора времени через TimePickerDialog
        taskTime.setOnClickListener {
            showTimePicker(taskTime, task.time)
        }
        AlertDialog.Builder(requireContext())
            .setTitle("Редактировать задачу")
            .setView(dialogView)
            .setPositiveButton("Сохранить") { _, _ ->
                val updatedTask = task.copy(
                    title = taskTitle.text.toString().trim(),
                    time = taskTime.text.toString().trim(),
                    importance = taskImportance.text.toString().toIntOrNull() ?: 1
                )

                // Проверяем, что данные корректные
                if (updatedTask.title.isEmpty() || updatedTask.importance !in 1..3) {
                    println("Некорректные данные задачи") // Логируем ошибку
                    return@setPositiveButton
                }

                onSave(updatedTask) // Передаём обновлённую задачу
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private suspend fun getDaysWithTasks(): Set<String> {
        val tasksForDay = mutableSetOf<String>()

        if (!::taskDao.isInitialized) {
            return emptySet() // Если taskDao ещё не создан, возвращаем пустой список
        }

        val tasks = taskDao.getAllTasks()
        tasks.forEach { task ->
            tasksForDay.add(task.date) // Добавляем дату, на которую есть задачи
        }

        return tasksForDay
    }

}
