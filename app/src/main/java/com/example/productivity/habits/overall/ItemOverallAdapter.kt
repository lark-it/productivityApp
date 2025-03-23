package com.example.productivity.habits.overall

import android.content.res.Resources
import android.graphics.drawable.GradientDrawable
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
        val rootLayout: View = view.findViewById(R.id.rootLayout)

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

        holder.habitIcon.setImageResource(habit.iconResId)

        val background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 30f
            setColor(habit.color)
        }
        holder.rootLayout.background = background

        val transposedDays = transposeDays(habit.daysProgress)

        val daysOfWeek = listOf("M", "T", "W", "T", "F", "S", "S")
        holder.dayLabels.forEachIndexed { index, textView ->
            textView.text = daysOfWeek[index]
        }

        val sizeInDp = 20
        val marginInDp = 2
        val sizeInPx = (sizeInDp * Resources.getSystem().displayMetrics.density).toInt()
        val marginInPx = (marginInDp * Resources.getSystem().displayMetrics.density).toInt()

        for (row in 0 until 7) {
            for (col in 0 until 15) {
                val view = ImageView(holder.itemView.context)
                val layoutParams = GridLayout.LayoutParams().apply {
                    rowSpec = GridLayout.spec(row)
                    columnSpec = GridLayout.spec(col)
                    width = sizeInPx
                    height = sizeInPx
                    setMargins(marginInPx, marginInPx, marginInPx, marginInPx)
                }
                view.layoutParams = layoutParams
                view.setBackgroundResource(
                    if (transposedDays[row][col]) R.drawable.circle_checked else R.drawable.circle_unchecked
                )
                holder.gridLayout.addView(view)
            }
        }
    }

    private fun transposeDays(days: List<List<Boolean>>): List<List<Boolean>> {
        val numWeeks = days.size
        val numDays = days[0].size

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
    val daysProgress: List<List<Boolean>>,
    val iconResId: Int,
    val color: Int
)