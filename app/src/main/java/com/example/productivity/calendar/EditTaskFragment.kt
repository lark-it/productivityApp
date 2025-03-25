package com.example.productivity.calendar

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.productivity.AppDatabase
import com.example.productivity.R
import kotlinx.coroutines.launch
import java.util.*

class EditTaskFragment : Fragment() {
    private var taskId: Int = 0
    private lateinit var etTaskTitle: EditText
    private lateinit var etTaskDate: EditText
    private lateinit var etTaskTime: EditText
    private lateinit var radioGroupImportance: RadioGroup
    private var selectedDate = ""
    private var selectedTime = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_edit_task, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etTaskTitle = view.findViewById(R.id.et_task_title)
        etTaskDate = view.findViewById(R.id.et_task_date)
        etTaskTime = view.findViewById(R.id.et_task_time)
        radioGroupImportance = view.findViewById(R.id.radio_group_importance)
        val btnSaveTask = view.findViewById<Button>(R.id.btn_save_task)
        val backButton = view.findViewById<ImageButton>(R.id.backButton)

        arguments?.let {
            taskId = it.getInt("taskId")
            etTaskTitle.setText(it.getString("taskTitle", ""))
            selectedDate = it.getString("taskDate", "")
            selectedTime = it.getString("taskTime", "")
            etTaskDate.setText(selectedDate)
            etTaskTime.setText(selectedTime)

            when (it.getInt("taskImportance", 1)) {
                1 -> radioGroupImportance.check(R.id.radio_1)
                2 -> radioGroupImportance.check(R.id.radio_2)
                3 -> radioGroupImportance.check(R.id.radio_3)
            }
        }

        etTaskDate.setOnClickListener {
            val cal = Calendar.getInstance()
            val (year, month, day) = selectedDate.split("-").map { it.toInt() }
            DatePickerDialog(requireContext(), { _, y, m, d ->
                selectedDate = String.format("%04d-%02d-%02d", y, m + 1, d)
                etTaskDate.setText(selectedDate)
            }, year, month - 1, day).show()
        }

        etTaskTime.setOnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(requireContext(), { _, hour, minute ->
                selectedTime = String.format("%02d:%02d", hour, minute)
                etTaskTime.setText(selectedTime)
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }

        btnSaveTask.setOnClickListener {
            val updatedTitle = etTaskTitle.text.toString().trim()
            val updatedImportance = when (radioGroupImportance.checkedRadioButtonId) {
                R.id.radio_1 -> 1
                R.id.radio_2 -> 2
                R.id.radio_3 -> 3
                else -> 1
            }

            if (updatedTitle.isNotEmpty()) {
                updateTaskInDatabase(updatedTitle, selectedDate, selectedTime, updatedImportance)
            } else {
                Toast.makeText(requireContext(), "Введите название задачи", Toast.LENGTH_SHORT).show()
            }
        }

        backButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun updateTaskInDatabase(title: String, date: String, time: String, importance: Int) {
        lifecycleScope.launch {
            val taskDao = AppDatabase.getDatabase(requireContext()).taskDao()
            val updatedTask = TaskEntity(
                id = taskId,
                title = title,
                date = date,
                time = time,
                importance = importance,
                isCompleted = arguments?.getBoolean("taskCompleted") ?: false
            )
            taskDao.insertTask(updatedTask)
            findNavController().navigateUp()
        }
    }
}
