package com.example.productivity.ui.calendar

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.productivity.R
import com.example.productivity.data.tasks.TaskEntity
import com.example.productivity.data.habits.HabitsEntity

sealed class CalendarItem {
    data class DateHeader(val date: String) : CalendarItem()
    data class TaskItem(val task: TaskEntity) : CalendarItem()
    data class HabitItem(val habit: HabitsEntity) : CalendarItem()
}

class TaskAdapter(
    private val onTaskChecked           : (TaskEntity, Boolean) -> Unit,
    private val onTaskEdit              : (TaskEntity) -> Unit,
    private val onTaskDelete            : (TaskEntity) -> Unit,
    private val onHabitCheckRequest     : (HabitsEntity, String, Boolean) -> Boolean,
    private val onHabitChecked          : (Int, Boolean, String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<CalendarItem>()

    fun submitList(newItems: List<CalendarItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int) = when (items[position]) {
        is CalendarItem.DateHeader -> 0
        is CalendarItem.TaskItem   -> 1
        is CalendarItem.HabitItem  -> 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        0 -> DateHeaderViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_header, parent, false)
        )
        1 -> TaskViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_task, parent, false),
            onTaskChecked, onTaskEdit, onTaskDelete
        )
        2 -> HabitViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_habit_calendar, parent, false),
            onHabitCheckRequest, onHabitChecked
        )
        else -> throw IllegalArgumentException("Unknown viewType $viewType")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is CalendarItem.DateHeader -> (holder as DateHeaderViewHolder).bind(item)
            is CalendarItem.TaskItem   -> (holder as TaskViewHolder).bind(item.task)
            is CalendarItem.HabitItem  -> (holder as HabitViewHolder).bind(item.habit, item.habit.startDate)
        }
    }

    override fun getItemCount() = items.size

    class DateHeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tv = view.findViewById<TextView>(R.id.tv_header_date)
        fun bind(item: CalendarItem.DateHeader) {
            tv.text = item.date
        }
    }

    class TaskViewHolder(
        view: View,
        private val onChecked: (TaskEntity, Boolean) -> Unit,
        private val onEdit   : (TaskEntity) -> Unit,
        private val onDelete : (TaskEntity) -> Unit
    ) : RecyclerView.ViewHolder(view) {
        private val tvTitle     = view.findViewById<TextView>(R.id.tv_task_title)
        private val cbCompleted = view.findViewById<CheckBox>(R.id.checkBox)
        private val btnEdit     = view.findViewById<ImageButton>(R.id.edit_button)
        private val btnDelete   = view.findViewById<ImageButton>(R.id.delete_button)
        private val tvImp       = view.findViewById<TextView>(R.id.tv_important)

        fun bind(task: TaskEntity) {
            tvTitle.text = task.title
            tvImp.text   = task.importance.toString()
            cbCompleted.setOnCheckedChangeListener(null)
            cbCompleted.isChecked = task.isCompleted
            updateStyles(task.isCompleted)

            cbCompleted.setOnCheckedChangeListener { _, isChecked ->
                onChecked(task, isChecked)
            }
            btnEdit.setOnClickListener   { onEdit(task) }
            btnDelete.setOnClickListener { onDelete(task) }
        }

        private fun updateStyles(done: Boolean) {
            if (done) {
                tvTitle.paintFlags = tvTitle.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
                tvTitle.setTextColor(Color.GRAY)
            } else {
                tvTitle.paintFlags = tvTitle.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
                tvTitle.setTextColor(Color.WHITE)
            }
        }
    }

    class HabitViewHolder(
        view: View,
        private val onRequest: (HabitsEntity, String, Boolean) -> Boolean,
        private val onChecked: (Int, Boolean, String) -> Unit
    ) : RecyclerView.ViewHolder(view) {
        private val tvTitle  = view.findViewById<TextView>(R.id.habitTitle)
        private val ivIcon   = view.findViewById<ImageView>(R.id.habitIcon)
        private val cbHabit  = view.findViewById<CheckBox>(R.id.checkHabitCalendar)

        fun bind(habit: HabitsEntity, date: String) {
            tvTitle.text = habit.title
            ivIcon.setImageResource(habit.iconResId)
            cbHabit.setOnCheckedChangeListener(null)
            cbHabit.isChecked = habit.isCompleted

            cbHabit.setOnClickListener {
                val desired = cbHabit.isChecked
                if (onRequest(habit, date, desired)) {
                    onChecked(habit.id, desired, date)
                } else {
                    cbHabit.isChecked = !desired
                }
            }
        }
    }
}
