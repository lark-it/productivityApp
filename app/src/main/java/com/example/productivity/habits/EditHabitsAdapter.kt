package com.example.productivity.habits

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.productivity.R

class EditHabitsAdapter(
    private var habits: List<HabitsEntity>,
    private val onEditHabit: (HabitsEntity) -> Unit,
    private val onDeleteHabit: (HabitsEntity) -> Unit
) : RecyclerView.Adapter<EditHabitsAdapter.EditHabitsViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): EditHabitsViewHolder {
        val view =  LayoutInflater.from(parent.context).inflate(R.layout.item_habit, parent, false)
        return EditHabitsViewHolder(view,onEditHabit, onDeleteHabit)
    }

    override fun onBindViewHolder(holder: EditHabitsViewHolder, position: Int) {
        holder.bind(habits[position])
    }

    override fun getItemCount(): Int = habits.size

    class EditHabitsViewHolder(
        itemView: View,
        private val onEditHabit: (HabitsEntity) -> Unit,
        private val onDeleteHabit: (HabitsEntity) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.habitTitle)
        private val editButton: ImageButton = itemView.findViewById(R.id.edit_habit)
        private val deleteHabit: ImageButton = itemView.findViewById(R.id.delete_habit)
        fun bind(habit: HabitsEntity) {
            title.text = habit.title
            editButton.setOnClickListener {
                onEditHabit(habit)
            }
            deleteHabit.setOnClickListener {
                onDeleteHabit(habit)
            }
        }
    }

    fun updateList(newHabits: List<HabitsEntity>) {
        habits = newHabits
        notifyDataSetChanged()
    }


}