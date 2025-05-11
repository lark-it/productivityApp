package com.example.productivity.ui.tasks

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.productivity.R
import com.example.productivity.data.tasks.TaskEntity
import com.example.productivity.data.tasks.TaskRepositoryImpl
import com.example.productivity.AppDatabase
import java.text.SimpleDateFormat
import java.util.*

class AddTasksFragment : Fragment() {

    private val viewModel: TasksViewModel by viewModels {
        TasksViewModelFactory(
            TaskRepositoryImpl(
                AppDatabase.getDatabase(requireContext()).taskDao()
            )
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_add_tasks, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val passedDate = arguments?.getString("selectedDate")
        var selectedDate = passedDate ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        var selectedTime = "12:00"

        val etTitle      = view.findViewById<EditText>(R.id.et_task_title)
        val etDate       = view.findViewById<EditText>(R.id.et_task_date)
        val etTime       = view.findViewById<EditText>(R.id.et_task_time)
        val rgImportance = view.findViewById<RadioGroup>(R.id.radio_group_importance)
        val btnSave      = view.findViewById<Button>(R.id.btn_save_task)
        val btnBack      = view.findViewById<ImageButton>(R.id.backButton)

        etDate.setText(selectedDate)
        etTime.setText(selectedTime)

        etDate.setOnClickListener {
            val parts = selectedDate.split("-").map { it.toInt() }
            DatePickerDialog(requireContext(),
                { _, y, m, d ->
                    selectedDate = String.format("%04d-%02d-%02d", y, m+1, d)
                    etDate.setText(selectedDate)
                },
                parts[0], parts[1]-1, parts[2]
            ).show()
        }
        etTime.setOnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(requireContext(),
                { _, h, min ->
                    selectedTime = String.format("%02d:%02d", h, min)
                    etTime.setText(selectedTime)
                },
                cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true
            ).show()
        }

        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            if (title.isEmpty()) {
                Toast.makeText(requireContext(), "Введите название задачи", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val importance = when (rgImportance.checkedRadioButtonId) {
                R.id.radio_1 -> 1
                R.id.radio_2 -> 2
                R.id.radio_3 -> 3
                else         -> 1
            }
            val task = TaskEntity(
                title       = title,
                date        = selectedDate,
                time        = selectedTime,
                importance  = importance,
                isCompleted = false
            )
            viewModel.add(task)
            findNavController().navigateUp()
        }

        btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }
}
