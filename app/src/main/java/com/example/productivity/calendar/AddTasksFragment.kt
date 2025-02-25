package com.example.productivity.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.productivity.AppDatabase
import com.example.productivity.R
import com.example.productivity.calendar.TaskEntity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
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

        val etTaskTitle = view.findViewById<EditText>(R.id.et_task_title)
        val btnSaveTask = view.findViewById<Button>(R.id.btn_save_task)

        btnSaveTask.setOnClickListener {
            val taskTitle = etTaskTitle.text.toString().trim()

            if (taskTitle.isNotEmpty()) {
                saveTaskToDatabase(taskTitle)
            } else {
                Toast.makeText(requireContext(), "Введите название задачи", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveTaskToDatabase(title: String) {
        lifecycleScope.launch {
            val taskDao = AppDatabase.getDatabase(requireContext()).taskDao()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val currentDate = dateFormat.format(Date())

            val newTask = TaskEntity(
                title = title,
                date = currentDate,
                time = null,
                importance = 1,
                isCompleted = false
            )

            taskDao.insertTask(newTask)

            findNavController().navigateUp()
        }
    }

}
