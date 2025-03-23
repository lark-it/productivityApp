package com.example.productivity.habits.weekly

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.productivity.R
import java.text.SimpleDateFormat
import java.util.Locale

class WeeklyAdapter(private var habits: List<HabitWeeklyItem>) : RecyclerView.Adapter<WeeklyAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val habitTitle: TextView = view.findViewById(R.id.textView9)
        val habitIcon: ImageView = view.findViewById(R.id.imageView)
        val rootLayout: View = view.findViewById(R.id.rootLayout) // Обращаемся к корневому layout
        val dayViews: List<ImageView> = listOf(
            view.findViewById(R.id.monCheck),
            view.findViewById(R.id.tueCheck),
            view.findViewById(R.id.wedCheck),
            view.findViewById(R.id.thuCheck),
            view.findViewById(R.id.friCheck),
            view.findViewById(R.id.satCheck),
            view.findViewById(R.id.sunCheck)
        )
        val dateViews: List<TextView> = listOf(
            view.findViewById(R.id.tvMonDate),
            view.findViewById(R.id.tvTueDate),
            view.findViewById(R.id.tvWedDate),
            view.findViewById(R.id.tvThuDate),
            view.findViewById(R.id.tvFriDate),
            view.findViewById(R.id.tvSatDate),
            view.findViewById(R.id.tvSunDate)
        )
        val dayLabels: List<TextView> = listOf(
            view.findViewById(R.id.tvMonDay),
            view.findViewById(R.id.tvTueDay),
            view.findViewById(R.id.tvWedDay),
            view.findViewById(R.id.tvThuDay),
            view.findViewById(R.id.tvFriDay),
            view.findViewById(R.id.tvSatDay),
            view.findViewById(R.id.tvSunDay)
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_habit_weekly, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val habit = habits[position]
        holder.habitTitle.text = habit.title

        // Устанавливаем иконку
        holder.habitIcon.setImageResource(habit.iconResId)

        // Устанавливаем цветной фон для корневого layout
        val background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 30f
            setColor(habit.color)
        }
        holder.rootLayout.background = background

        // Отображаем дни недели
        habit.weekDates.forEachIndexed { index, date ->
            holder.dateViews[index].text = date.substring(8)
            holder.dayViews[index].setBackgroundResource(
                if (habit.daysCompletion[index]) R.drawable.circle_checked else R.drawable.circle_unchecked
            )
            holder.dayLabels[index].text = getDayOfWeek(date)
        }
    }

    override fun getItemCount(): Int = habits.size

    fun updateList(newHabits: List<HabitWeeklyItem>) {
        habits = newHabits
        notifyDataSetChanged()
    }

    private fun getDayOfWeek(dateString: String): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = dateFormat.parse(dateString) ?: return ""
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
        return dayFormat.format(date)
    }
}

data class HabitWeeklyItem(
    val title: String,
    val daysCompletion: List<Boolean>,
    val weekDates: List<String>,
    val iconResId: Int,
    val color: Int
)