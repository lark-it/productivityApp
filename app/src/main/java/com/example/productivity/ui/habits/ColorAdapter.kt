package com.example.productivity.ui.habits

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.productivity.R

class ColorAdapter(
    private val colors: List<Int>,
    private val selectedColor: Int?,
    private val onColorSelected: (Int) -> Unit
) : RecyclerView.Adapter<ColorAdapter.ColorViewHolder>() {

    private var currentSelectedColor: Int? = selectedColor

    inner class ColorViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val colorView: View = view.findViewById(R.id.colorView)

        fun bind(color: Int){
            (colorView.background as GradientDrawable).setColor(color)
            colorView.alpha = if (color == currentSelectedColor) 1.0f else 0.5f

            colorView.setOnClickListener{
                currentSelectedColor = color
                notifyDataSetChanged()
                onColorSelected(color)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ColorViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_color, parent,false)
        return ColorViewHolder(view)
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        holder.bind(colors[position])
    }

    override fun getItemCount(): Int = colors.size
}