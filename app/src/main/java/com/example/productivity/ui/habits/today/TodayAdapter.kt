package com.example.productivity.ui.habits.today

import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.productivity.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class HabitItem {
    data class Header(val title: String) : HabitItem()
    data class Habit(val id: Int, val title: String, val isCompleted: Boolean, val iconResId: Int, val color: Int) : HabitItem()
}

class TodayAdapter(
    private var habits: List<HabitItem>,
    private val onHabitChecked: (Int, Boolean, String) -> Unit
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

    class HabitViewHolder(
        view: View,
        private val onHabitChecked: (Int, Boolean, String) -> Unit
    ) : RecyclerView.ViewHolder(view) {

        private val title: TextView = view.findViewById(R.id.titleName)
        private val checkHabit: CheckBox = view.findViewById(R.id.checkHabit)
        private val habitIcon: ImageView = view.findViewById(R.id.habitIcon)
        private val linearHabit: LinearLayout = view.findViewById(R.id.linearHabit)

        fun bind(habit: HabitItem.Habit) {
            val todayDate = getCurrentDate()

            title.text = habit.title
            habitIcon.setImageResource(habit.iconResId)

            val background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 30f
                setColor(habit.color)
            }
            linearHabit.background = background

            checkHabit.setOnCheckedChangeListener(null)
            checkHabit.isChecked = habit.isCompleted

            checkHabit.setOnCheckedChangeListener { _, isChecked ->
                Log.d("TodayAdapter", "Чекбокс изменён: habitId=${habit.id}, isChecked=$isChecked")
                onHabitChecked(habit.id, isChecked, todayDate)
            }

        }

        private fun getCurrentDate(): String {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return dateFormat.format(Date())
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
