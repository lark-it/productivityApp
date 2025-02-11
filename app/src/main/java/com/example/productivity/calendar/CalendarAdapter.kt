package com.example.productivity.calendar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.productivity.OnDayClickListener
import com.example.productivity.R
import java.util.Calendar

class CalendarAdapter(
    private var days: List<String>,
    private val daysWithTasks: Set<String>,
    private val onDayClickListener: OnDayClickListener,
    private val currentDate: Calendar,
    private val displayedMonth: Int,
    private val displayedYear: Int
) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    private var selectedPosition = RecyclerView.NO_POSITION

    // Создание ячейки (привязываем макет)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_day, parent, false)
        return CalendarViewHolder(view)
    }

    // Привязка данных к ячейке
    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        val day = days[position]
        holder.dayText.text = day

        if (day.toIntOrNull() != null) { // Проверяем, что это число, а не заголовок недели
            val fullDate = "${displayedYear}-${String.format("%02d", displayedMonth + 1)}-${day.padStart(2, '0')}"

            // Если на этот день есть задачи – добавляем обводку
            if (daysWithTasks.contains(fullDate)) {
                holder.dayText.setBackgroundResource(R.drawable.bg_task_day)
            } else {
                holder.dayText.setBackgroundResource(0)
            }
        }

        if (position < 7) { // Дни недели
            holder.dayText.setTextColor(holder.itemView.context.getColor(android.R.color.darker_gray))
            holder.dayText.textSize = 14f
            holder.dayText.setBackgroundResource(0)
        } else {
            holder.dayText.setTextColor(holder.itemView.context.getColor(android.R.color.white))
            holder.dayText.textSize = 16f

            // Проверяем текущую дату
            val isToday = day.toIntOrNull() == currentDate.get(Calendar.DAY_OF_MONTH) &&
                    currentDate.get(Calendar.MONTH) == displayedMonth && // Проверяем месяц
                    currentDate.get(Calendar.YEAR) == displayedYear     // Проверяем год

            if (isToday) {
                holder.dayText.setBackgroundResource(R.drawable.bg_current_day)
            } else if (position == selectedPosition) {
                holder.dayText.setBackgroundResource(R.drawable.bg_selected_day)
            }

            holder.itemView.setOnClickListener {
                val adapterPosition = holder.adapterPosition
                if (adapterPosition != RecyclerView.NO_POSITION && day.isNotEmpty()) {
                    selectedPosition = adapterPosition
                    onDayClickListener.onDayClick(day)
                    notifyDataSetChanged()
                }
            }
        }
    }

    class CalendarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dayText: TextView = itemView.findViewById(R.id.tv_day)
    }

    override fun getItemCount(): Int = days.size
}