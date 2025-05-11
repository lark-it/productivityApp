package com.example.productivity.ui.habits

import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.productivity.R

open class BaseHabitFragment : Fragment() {
    protected var selectedIcon: Int? = null
    protected var selectedColor: Int? = null
    protected val selectedDays = mutableListOf<Int>()
    protected lateinit var daysOfWeekGrid: GridLayout

    protected fun addIconsToScrollView(iconContainer: LinearLayout, icons: List<Int>) {
        for (icon in icons) {
            val imageView = ImageView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(130, 130).apply {
                    setMargins(8, 8, 8, 8)
                }
                setImageResource(icon)
                scaleType = ImageView.ScaleType.CENTER_CROP
                setOnClickListener {
                    selectedIcon = icon
                    highlightSelectedIcon(iconContainer, this)
                }
            }
            iconContainer.addView(imageView)
        }
    }

    protected fun highlightSelectedIcon(iconContainer: LinearLayout, selectedImageView: ImageView) {
        for (i in 0 until iconContainer.childCount) {
            val child = iconContainer.getChildAt(i) as ImageView
            child.alpha = if (child == selectedImageView) 1.0f else 0.5f
        }
    }

    protected val icons = listOf(
        R.drawable.emo_alien, R.drawable.emo_apple, R.drawable.emo_ball,
        R.drawable.emo_basket, R.drawable.emo_biceps, R.drawable.emo_bicycle,
        R.drawable.emo_broccoli, R.drawable.emo_chick, R.drawable.emo_demon,
        R.drawable.emo_fire, R.drawable.emo_nerd, R.drawable.emo_proger,
        R.drawable.emo_sleep, R.drawable.emo_sport, R.drawable.emo_sunglasses,
        R.drawable.emo_teacher, R.drawable.emo_trophy, R.drawable.emo_writing
    )

    protected fun getColorList(): List<Int> = colors.map {
        ContextCompat.getColor(requireContext(), it)
    }
    private val colors = listOf(
        R.color.habit_color_1, R.color.habit_color_2, R.color.habit_color_3,
        R.color.habit_color_4, R.color.habit_color_5, R.color.habit_color_6,
        R.color.habit_color_7, R.color.habit_color_8, R.color.habit_color_9,
        R.color.habit_color_10, R.color.habit_color_11, R.color.habit_color_12,
        R.color.habit_color_13, R.color.habit_color_14, R.color.habit_color_15
    )

    protected fun showWeeklyDays(show: Boolean) {
        daysOfWeekGrid.visibility = if (show) View.VISIBLE else View.GONE
    }

    protected fun createWeekButtons() {
        val grid = view?.findViewById<GridLayout>(R.id.daysOfWeekGrid) ?: return
        grid.removeAllViews()
        val daysNames = listOf("ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ", "ВС")
        daysNames.forEachIndexed { index, day ->
            val btn = Button(requireContext()).apply {
                text = day
                textSize = 14f
                setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                setBackgroundResource(R.drawable.day_of_week_button_selector)
                setPadding(16, 8, 16, 8)
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    height = GridLayout.LayoutParams.WRAP_CONTENT
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    setMargins(6, 6, 6, 6)
                }
                setOnClickListener { toggleDaySelection(index + 1, this) }
                tag = "day${index+1}"
            }
            grid.addView(btn)
        }
        highlightSelectedDays()
    }

    protected fun toggleDaySelection(day: Int, button: Button) {
        if (selectedDays.contains(day)) {
            selectedDays.remove(day)
            button.isSelected = false
        } else {
            selectedDays.add(day)
            button.isSelected = true
        }
    }

    protected fun highlightSelectedDays() {
        for (i in 0 until daysOfWeekGrid.childCount) {
            val btn = daysOfWeekGrid.getChildAt(i) as Button
            btn.isSelected = selectedDays.contains(i + 1)
        }
    }
}
