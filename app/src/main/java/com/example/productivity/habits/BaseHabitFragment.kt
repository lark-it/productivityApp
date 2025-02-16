package com.example.productivity.habits

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.ViewTreeObserver
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.example.productivity.R

open class BaseHabitFragment : Fragment() {
    protected var selectedIcon: Int? = null
    protected var selectedColor: Int? = null

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
        R.drawable.emo_alien,
        R.drawable.emo_apple,
        R.drawable.emo_ball,
        R.drawable.emo_basket,
        R.drawable.emo_biceps,
        R.drawable.emo_bicycle,
        R.drawable.emo_broccoli,
        R.drawable.emo_chick,
        R.drawable.emo_demon,
        R.drawable.emo_fire,
        R.drawable.emo_nerd,
        R.drawable.emo_proger,
        R.drawable.emo_sleep,
        R.drawable.emo_sport,
        R.drawable.emo_sunglasses,
        R.drawable.emo_teacher,
        R.drawable.emo_trophy,
        R.drawable.emo_writing
    )
    fun getColorList(): List<Int> = colors

    protected val colors = listOf(
        Color.rgb(254,255,204),
        Color.rgb(249,204,154),
        Color.rgb(168,148,153),
        Color.rgb(195,164,164),
        Color.rgb(247,153,154),

        Color.rgb(250,204,204),
        Color.rgb(248,154,204),
        Color.rgb(252,204,255),
        Color.rgb(202,153,255),
        Color.rgb(206,204,255),

        Color.rgb(161,204,255),
        Color.rgb(158,197,196),
        Color.rgb(210,255,255),
        Color.rgb(208,255,204),
        Color.rgb(24,26,32) // ТУТ должна быть кнопка а как?
    )

}
