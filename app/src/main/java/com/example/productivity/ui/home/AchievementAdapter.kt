package com.example.productivity.ui.home

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.productivity.R
import com.example.productivity.data.home.AchievementEntity

class AchievementAdapter(
    private val achievements: List<AchievementEntity>
) : RecyclerView.Adapter<AchievementAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title = view.findViewById<TextView>(R.id.achievementTitle)
        val desc = view.findViewById<TextView>(R.id.achievementDescription)
        val icon = view.findViewById<ImageView>(R.id.achievementIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_achievement, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = achievements.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = achievements[position]
        holder.title.text = item.title
        holder.desc.text = item.description
        if (item.isUnlocked) {
            holder.icon.setImageResource(R.drawable.ic_trophy)
            holder.icon.alpha = 1.0f
            holder.title.setTextColor(Color.WHITE)
            holder.desc.setTextColor(Color.parseColor("#A1A1A1"))
        } else {
            holder.icon.setImageResource(R.drawable.ic_lock)
            holder.icon.alpha = 0.5f
            holder.title.setTextColor(Color.GRAY)
            holder.desc.setTextColor(Color.GRAY)
        }

    }
}
