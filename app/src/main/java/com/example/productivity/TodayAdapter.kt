package com.example.productivity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

sealed class HabitsModel {
    data class Header(val title: String) : HabitsModel()
    data class Habit(val id: Int, val title: String, val icon: Int, val isCompleted: Boolean) : HabitsModel()
}

class TodayAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>()  {
    val habitsModel = listOf(
        HabitsModel.Header("Нужно сделать:"),
        HabitsModel.Habit(1,"Sport", 4,false)
    )

    override fun getItemViewType(position: Int):Int{
        return when (habitsModel[position]){
            is HabitsModel.Header -> 0
            is HabitsModel.Habit -> 1
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when(viewType){
            0 -> TodayAdapter.TodayHeaderViewHolder(inflater.inflate(R.layout.today_section_header, parent,false))
            1 -> TodayAdapter.HabitsViewHolder(inflater.inflate(R.layout.today_habit,parent, false))
            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(val item = habitsModel[position]){
            is HabitsModel.Header -> (holder as TodayHeaderViewHolder).bind(item)
            is HabitsModel.Habit -> (holder as HabitsViewHolder).bind(item)
        }
    }

    class TodayHeaderViewHolder(view:View) : RecyclerView.ViewHolder(view){
        private val title: TextView = view.findViewById(R.id.headerTitle)

        fun bind(header: HabitsModel.Header){
            title.text = header.title
        }
    }
    class HabitsViewHolder(view: View) : RecyclerView.ViewHolder(view){
        private val habitId: TextView = view.findViewById(R.id.habitId)
        private val title: TextView = view.findViewById(R.id.titleName)

        fun bind(habit: HabitsModel.Habit){
            habitId.text = habit.id.toString()
            title.text = habit.title

        }
    }

    override fun getItemCount(): Int = habitsModel.size
}