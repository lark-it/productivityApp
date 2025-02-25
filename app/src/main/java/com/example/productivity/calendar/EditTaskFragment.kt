package com.example.productivity.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.productivity.AppDatabase
import com.example.productivity.R
import com.example.productivity.calendar.TaskEntity
import kotlinx.coroutines.launch

class EditTaskFragment : Fragment() {
    private var taskId: Int = 0
    private lateinit var etTaskTitle: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_task, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etTaskTitle = view.findViewById(R.id.et_task_title)
        val btnSaveTask = view.findViewById<Button>(R.id.btn_save_task)

        // ✅ Загружаем данные из Bundle
        arguments?.let {
            taskId = it.getInt("taskId")
            etTaskTitle.setText(it.getString("taskTitle", ""))
        }

        btnSaveTask.setOnClickListener {
            val updatedTitle = etTaskTitle.text.toString().trim()

            if (updatedTitle.isNotEmpty()) {
                updateTaskInDatabase(updatedTitle)
            }
        }
    }

    private fun updateTaskInDatabase(title: String) {
        lifecycleScope.launch {
            val taskDao = AppDatabase.getDatabase(requireContext()).taskDao()
            val updatedTask = TaskEntity(
                id = taskId,
                title = title,
                date = arguments?.getString("taskDate") ?: "",
                time = arguments?.getString("taskTime"),
                importance = arguments?.getInt("taskImportance") ?: 1,
                isCompleted = arguments?.getBoolean("taskCompleted") ?: false
            )

            taskDao.insertTask(updatedTask)

            findNavController().navigateUp() // Закрываем фрагмент
        }
    }
}
