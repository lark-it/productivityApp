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

class TaskAdapter(
    private val onTaskChecked: (TaskEntity, Boolean) -> Unit,
    private val onTaskEdit: (TaskEntity) -> Unit,
    private val onTaskDelete: (TaskEntity) -> Unit,
    private val onHabitChecked: (HabitsEntity, String, Boolean) -> Unit // ✅ Добавлен параметр
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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
            else -> throw IllegalArgumentException("Unknown item type") // ✅ Исправляем ошибку
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            0 -> DateHeaderViewHolder(inflater.inflate(R.layout.item_header, parent, false))
            1 -> TaskViewHolder(inflater.inflate(R.layout.item_task, parent, false), onTaskChecked, onTaskEdit, onTaskDelete)
            2 -> HabitViewHolder(inflater.inflate(R.layout.item_habit_calendar, parent, false), onHabitChecked)
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is CalendarItem.DateHeader -> (holder as DateHeaderViewHolder).bind(item)
            is CalendarItem.TaskItem -> (holder as TaskViewHolder).bind(item.task)
            is CalendarItem.HabitItem -> (holder as HabitViewHolder).bind(item.habit, item.habit.startDate)
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


    class TaskViewHolder(
        view: View,
        private val onTaskChecked: (TaskEntity, Boolean) -> Unit,
        private val onTaskEdit: (TaskEntity) -> Unit,
        private val onTaskDelete: (TaskEntity) -> Unit
    ) : RecyclerView.ViewHolder(view) {
        private val textView: TextView = view.findViewById(R.id.tv_task_title)
        private val checkBox: CheckBox = view.findViewById(R.id.checkBox)
        private val editButton: ImageButton = view.findViewById(R.id.edit_button)
        private val deleteButton: ImageButton = view.findViewById(R.id.delete_button)

        fun bind(task: TaskEntity) {
            textView.text = task.title
            checkBox.setOnCheckedChangeListener(null)
            checkBox.isChecked = task.isCompleted

            checkBox.setOnCheckedChangeListener { _, isChecked ->
                onTaskChecked(task, isChecked)
            }

            editButton.setOnClickListener {
                onTaskEdit(task)
            }

            deleteButton.setOnClickListener {
                onTaskDelete(task)
            }
        }
    }


    class HabitViewHolder(view: View, private val onHabitChecked: (HabitsEntity, String, Boolean) -> Unit) : RecyclerView.ViewHolder(view) {
        private val textView: TextView = view.findViewById(R.id.habitTitle)
        private val icon: ImageView = view.findViewById(R.id.habitIcon)
        private val checkBox: CheckBox = view.findViewById(R.id.checkHabitCalendar)

        fun bind(habit: HabitsEntity, date: String) {
            textView.text = habit.title
            icon.setImageResource(habit.iconResId)

            checkBox.setOnCheckedChangeListener(null)
            checkBox.isChecked = habit.isCompleted

            checkBox.setOnCheckedChangeListener { _, isChecked ->
                onHabitChecked(habit, date, isChecked)
            }
        }
    }
}
