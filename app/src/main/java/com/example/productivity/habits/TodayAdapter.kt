package com.example.productivity.habits

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.productivity.R

sealed class HabitItem  {
    data class Header(val title: String) : HabitItem()
    data class Habit(val id: Int, val title: String, val isCompleted: Boolean) : HabitItem()
}

class TodayAdapter(
    private var habits: List<HabitItem>,
    private val onHabitChecked: (HabitItem.Habit) -> Unit
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>()  {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_HABIT = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (habits[position]) {
            is HabitItem.Header -> TYPE_HEADER
            is HabitItem.Habit -> TYPE_HABIT
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_HEADER -> HeaderTodayViewHolder(inflater.inflate(R.layout.today_section_header, parent, false))
            TYPE_HABIT -> HabitViewHolder(inflater.inflate(R.layout.today_habit, parent, false), onHabitChecked)
            else -> throw IllegalArgumentException("Unknown view type")
        }
    }
    class HeaderTodayViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val title: TextView = view.findViewById(R.id.headerTitle)
        fun bind(header: HabitItem.Header) {
            title.text = header.title
        }
    }

    class HabitViewHolder(view: View, private val onHabitChecked: (HabitItem.Habit) -> Unit) : RecyclerView.ViewHolder(view) {
        private val title: TextView = view.findViewById(R.id.titleName)
        private val checkHabit: CheckBox = view.findViewById(R.id.checkHabit)

        fun bind(habit: HabitItem.Habit) {
            title.text = habit.title
            checkHabit.setOnCheckedChangeListener(null)
            checkHabit.isChecked = habit.isCompleted

            checkHabit.setOnCheckedChangeListener { _, isChecked ->
                onHabitChecked(habit.copy(isCompleted = isChecked)) // Передаем новое состояние
            }
        }
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = habits[position]) {
            is HabitItem.Header -> (holder as HeaderTodayViewHolder).bind(item)
            is HabitItem.Habit -> (holder as HabitViewHolder).bind(item)
        }
    }

    override fun getItemCount(): Int = habits.size

    fun updateList(newHabits: List<HabitItem>) {
        habits = newHabits
        notifyDataSetChanged()
    }


}
