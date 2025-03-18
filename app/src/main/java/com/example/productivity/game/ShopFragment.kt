package com.example.productivity.game

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.example.productivity.R

class ShopFragment : Fragment() {

    private lateinit var skinPreview: ImageView
    private lateinit var buyButton: Button
    private lateinit var prevButton: Button
    private lateinit var nextButton: Button
    private var selectedSkinIndex = 0

    private val skins = listOf(
        R.drawable.pet_default,
        R.drawable.pet_blue,
        R.drawable.pet_red
    )

    private val skinPrices = listOf(0, 10, 20, 30)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_shop, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        skinPreview = view.findViewById(R.id.skinPreview)
        buyButton = view.findViewById(R.id.buyButton)
        prevButton = view.findViewById(R.id.prevSkinButton)
        nextButton = view.findViewById(R.id.nextSkinButton)

        updateSkinPreview()

        prevButton.setOnClickListener {
            selectedSkinIndex = if (selectedSkinIndex > 0) selectedSkinIndex - 1 else skins.size - 1
            updateSkinPreview()
        }

        nextButton.setOnClickListener {
            selectedSkinIndex = if (selectedSkinIndex < skins.size - 1) selectedSkinIndex + 1 else 0
            updateSkinPreview()
        }

        buyButton.setOnClickListener {
            buySkin()
        }
    }

    private fun updateSkinPreview() {
        skinPreview.setImageResource(skins[selectedSkinIndex])
        buyButton.text = "Купить за ${skinPrices[selectedSkinIndex]} монет"
    }

    private fun buySkin() {
        val sharedPref = requireActivity().getSharedPreferences("tamagotchi_prefs", Context.MODE_PRIVATE)
        val currentCoins = sharedPref.getInt("coins", 100)

        val skinPrice = skinPrices[selectedSkinIndex]
        if (currentCoins >= skinPrice) {
            sharedPref.edit()
                .putInt("current_skin", skins[selectedSkinIndex])
                .putInt("coins", currentCoins - skinPrice)
                .apply()

            Toast.makeText(requireContext(), "Скин куплен!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Недостаточно монет!", Toast.LENGTH_SHORT).show()
        }
    }
}
