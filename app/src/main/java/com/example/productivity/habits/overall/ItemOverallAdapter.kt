package com.example.productivity.habits.overall

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.productivity.R

class ItemOverallAdapter(private var habits: List<HabitOverallItem>) : RecyclerView.Adapter<ItemOverallAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val habitIcon: ImageView = view.findViewById(R.id.imageView2)
        val habitTitle: TextView = view.findViewById(R.id.tvHabitTitle)
        val gridLayout: GridLayout = view.findViewById(R.id.grid_days)

        // Добавляем ссылки на `TextView` для дней недели
        val dayLabels: List<TextView> = listOf(
            view.findViewById(R.id.tvDay1),
            view.findViewById(R.id.tvDay2),
            view.findViewById(R.id.tvDay3),
            view.findViewById(R.id.tvDay4),
            view.findViewById(R.id.tvDay5),
            view.findViewById(R.id.tvDay6),
            view.findViewById(R.id.tvDay7)
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_habit_overall, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val habit = habits[position]
        holder.habitTitle.text = habit.title
        holder.gridLayout.removeAllViews()

        val transposedDays = transposeDays(habit.daysProgress)

        // Обновляем отображение дней недели
        val daysOfWeek = listOf("M", "T", "W", "T", "F", "S", "S")
        holder.dayLabels.forEachIndexed { index, textView ->
            textView.text = daysOfWeek[index]
        }

        for (row in 0 until 7) {
            for (col in 0 until 15) {
                val view = ImageView(holder.itemView.context)
                val layoutParams = GridLayout.LayoutParams().apply {
                    rowSpec = GridLayout.spec(row, 1f)
                    columnSpec = GridLayout.spec(col, 1f)
                    width = 60
                    height = 60
                    setMargins(6, 6, 6, 6)
                }
                view.layoutParams = layoutParams
                view.setBackgroundResource(if (transposedDays[row][col]) R.drawable.circle_checked else R.drawable.circle_unchecked)
                holder.gridLayout.addView(view)
            }
        }
    }


    private fun transposeDays(days: List<List<Boolean>>): List<List<Boolean>> {
        val numWeeks = days.size // 15
        val numDays = days[0].size // 7

        val transposed = MutableList(numDays) { MutableList(numWeeks) { false } }

        for (week in 0 until numWeeks) {
            for (day in 0 until numDays) {
                if (week < days.size && day < days[week].size) {
                    transposed[day][week] = days[week][day]
                }
            }
        }
        return transposed
    }

    override fun getItemCount(): Int = habits.size

    fun updateList(newHabits: List<HabitOverallItem>) {
        habits = newHabits
        notifyDataSetChanged()
    }
}

data class HabitOverallItem(
    val title: String,
    val daysProgress: List<List<Boolean>>
)
