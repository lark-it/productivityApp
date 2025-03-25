package com.example.productivity.calendar

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.productivity.AppDatabase
import com.example.productivity.R
import com.example.productivity.calendar.TaskEntity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddTasksFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_tasks, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val passedDate = arguments?.getString("selectedDate")
        var selectedDate = passedDate ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        var selectedTime: String = "12:00"

        val etTaskTitle = view.findViewById<EditText>(R.id.et_task_title)
        val btnSaveTask = view.findViewById<Button>(R.id.btn_save_task)
        val etTaskDate = view.findViewById<EditText>(R.id.et_task_date)
        val etTaskTime = view.findViewById<EditText>(R.id.et_task_time)
        val radioGroupImportance = view.findViewById<RadioGroup>(R.id.radio_group_importance)

        etTaskDate.setText(selectedDate)

        etTaskDate.setOnClickListener {
            val cal = Calendar.getInstance()
            val parts = selectedDate.split("-")
            val year = parts[0].toInt()
            val month = parts[1].toInt() - 1
            val day = parts[2].toInt()
            val datePicker = DatePickerDialog(requireContext(), { _, newYear, newMonth, newDay ->
                selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", newYear, newMonth + 1, newDay)
                etTaskDate.setText(selectedDate)
            }, year, month, day)
            datePicker.show()
        }

        etTaskTime.setOnClickListener {
            val cal = Calendar.getInstance()
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            val minute = cal.get(Calendar.MINUTE)
            val timePicker = TimePickerDialog(requireContext(), { _, selectedHour, selectedMinute ->
                selectedTime = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute)
                etTaskTime.setText(selectedTime)
            }, hour, minute, true)
            timePicker.show()
        }
        etTaskTime.setText(selectedTime)

        btnSaveTask.setOnClickListener {
            val taskTitle = etTaskTitle.text.toString().trim()
            if (taskTitle.isNotEmpty()) {
                // Определяем важность из радиогруппы:
                val selectedRadioId = radioGroupImportance.checkedRadioButtonId
                val importance = when (selectedRadioId) {
                    R.id.radio_1 -> 1
                    R.id.radio_2 -> 2
                    R.id.radio_3 -> 3
                    else -> 1
                }
                saveTaskToDatabase(taskTitle, selectedDate, selectedTime, importance)
            } else {
                Toast.makeText(requireContext(), "Введите название задачи", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        view.findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun saveTaskToDatabase(title: String, date: String, time: String, importance: Int) {
        lifecycleScope.launch {
            val taskDao = AppDatabase.getDatabase(requireContext()).taskDao()
            val newTask = TaskEntity(
                title = title,
                date = date,
                time = time,
                importance = importance,
                isCompleted = false
            )
            taskDao.insertTask(newTask)
            findNavController().navigateUp()
        }
    }
}
