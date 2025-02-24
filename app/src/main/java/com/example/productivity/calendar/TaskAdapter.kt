package com.example.productivity.calendar

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.productivity.R
import com.example.productivity.habits.HabitsEntity

sealed class CalendarItem {
    data class DateHeader(val date: String) : CalendarItem()
    data class TaskItem(val task: TaskEntity) : CalendarItem()
    data class HabitItem(val habit: HabitsEntity) : CalendarItem()
}

class TaskAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<CalendarItem>()

    fun submitList(newItems: List<CalendarItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is CalendarItem.DateHeader -> 0
            is CalendarItem.TaskItem -> 1
            is CalendarItem.HabitItem -> 2
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            0 -> DateHeaderViewHolder(inflater.inflate(R.layout.item_header, parent, false))
            1 -> TaskViewHolder(inflater.inflate(R.layout.item_task, parent, false))
            2 -> HabitViewHolder(inflater.inflate(R.layout.item_habit, parent, false))
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is CalendarItem.DateHeader -> (holder as DateHeaderViewHolder).bind(item)
            is CalendarItem.TaskItem -> (holder as TaskViewHolder).bind(item.task)
            is CalendarItem.HabitItem -> (holder as HabitViewHolder).bind(item.habit)
        }
    }

    override fun getItemCount(): Int = items.size

    // Заголовок даты
    class DateHeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val textView: TextView = view.findViewById(R.id.tv_header_date)
        fun bind(item: CalendarItem.DateHeader) {
            textView.text = item.date
        }
    }

    // Элемент задачи
    class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val textView: TextView = view.findViewById(R.id.tv_task_title)
        private val checkBox: CheckBox = view.findViewById(R.id.checkBox)
        private val taskTime: TextView = view.findViewById(R.id.tv_task_time)
        private val importance: TextView = view.findViewById(R.id.tv_important)

        fun bind(task: TaskEntity) {
            textView.text = task.title
            checkBox.isChecked = task.isCompleted
            taskTime.text = task.time ?: ""
            importance.text = task.importance.toString()
        }
    }

    // Элемент привычки
    class HabitViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val textView: TextView = view.findViewById(R.id.habitTitle)
        private val icon: ImageView = view.findViewById(R.id.habitIcon)

        fun bind(habit: HabitsEntity) {
            textView.text = habit.title
            icon.setImageResource(habit.iconResId)
        }
    }
}
