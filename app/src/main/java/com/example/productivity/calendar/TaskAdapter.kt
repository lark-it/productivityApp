package com.example.productivity.calendar

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.productivity.R


sealed class ListItem
data class HeaderItem(val date: String) : ListItem()
data class TaskItem(val task: TaskEntity) : ListItem()
data class CompletedHeaderItem(val date: String, val count: Int) : ListItem()

class TaskAdapter(
    private var items: List<ListItem>,
    private val onTaskDelete: (TaskEntity) -> Unit,// Колбэк для удаления задачи
    private val onTaskEdit: (TaskEntity) -> Unit,
    private val onTaskCompleted: (TaskEntity) -> Unit
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val collapsedCompletedTasks = mutableSetOf<String>()

    private var showCompletedTasks = false

    companion object {
        const val TYPE_HEADER = 0
        const val TYPE_TASK = 1
        const val TYPE_COMPLETED_HEADER = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is HeaderItem -> TYPE_HEADER
            is TaskItem -> TYPE_TASK
            is CompletedHeaderItem -> TYPE_COMPLETED_HEADER
            else -> TYPE_TASK
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_header, parent, false)
                HeaderViewHolder(view)
            }
            TYPE_COMPLETED_HEADER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_completed_header, parent, false)
                CompletedHeaderViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
                TaskViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> {
                val header = items[position] as HeaderItem
                holder.headerDate.text = header.date
            }
            is CompletedHeaderViewHolder -> {
                val completedHeader = items[position] as CompletedHeaderItem
                holder.completedHeader.text = "Выполненные задачи (${completedHeader.count}) :"

                // Обработчик нажатия: скрывает/показывает задачи
                holder.itemView.setOnClickListener {
                    if (collapsedCompletedTasks.contains(completedHeader.date)) {
                        collapsedCompletedTasks.remove(completedHeader.date) // Показываем задачи
                    } else {
                        collapsedCompletedTasks.add(completedHeader.date) // Скрываем задачи
                    }
                    notifyDataSetChanged()
                }
            }
            is TaskViewHolder -> {
                val task = (items[position] as TaskItem).task

                if (task.isCompleted) {
                    holder.itemView.setBackgroundResource(R.drawable.rounded_bg_completed)
                    holder.titleText.setTextColor(Color.DKGRAY)
                } else {
                    holder.itemView.setBackgroundResource(R.drawable.rounded_bg)
                    holder.titleText.setTextColor(Color.WHITE)
                }


                holder.titleText.text = task.title
                holder.importanceText.text = task.importance.toString()
                holder.timeText.text = task.time ?: "Без времени"

                holder.checkBox.setOnCheckedChangeListener(null) // Очищаем слушатель
                holder.checkBox.isChecked = task.isCompleted
                holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
                    val updatedTask = task.copy(isCompleted = isChecked)
                    onTaskCompleted(updatedTask) // Обновляем статус задачи

                    // Немедленно перезагружаем список, чтобы переместить задачу
                    notifyDataSetChanged()
                }

                holder.deleteButton.setOnClickListener {
                    onTaskDelete(task)
                }

                holder.editButton.setOnClickListener {
                    onTaskEdit(task)
                }
            }
        }
    }

    class CompletedHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val completedHeader: TextView = itemView.findViewById(R.id.tv_completed_header)
    }

    class TaskViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)
        val deleteButton: ImageButton = itemView.findViewById(R.id.delete_button)
        val titleText: TextView = itemView.findViewById(R.id.tv_task_title)
        val importanceText: TextView = itemView.findViewById(R.id.tv_important)
        val timeText: TextView = itemView.findViewById(R.id.tv_task_time)
        val editButton: ImageButton = itemView.findViewById(R.id.edit_button)
    }
    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val headerDate: TextView = itemView.findViewById(R.id.tv_header_date)
    }

    fun updateItems(newItems: List<ListItem>) {
        items = newItems
        notifyDataSetChanged()
    }
//    fun toggleCompletedTasks(date: String) {
//        if (collapsedCompletedTasks.contains(date)) {
//            collapsedCompletedTasks.remove(date) // Раскрываем задачи
//        } else {
//            collapsedCompletedTasks.add(date) // Сворачиваем задачи
//        }
//        notifyDataSetChanged() // Обновляем UI
//    }
    override fun getItemCount(): Int = items.size

}
