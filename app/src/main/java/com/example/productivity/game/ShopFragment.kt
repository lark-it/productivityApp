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
import androidx.lifecycle.lifecycleScope
import com.example.productivity.R
import com.example.productivity.AppDatabase
import com.example.productivity.home.UserRepository
import kotlinx.coroutines.launch

class ShopFragment : Fragment() {

    private lateinit var skinPreview: ImageView
    private lateinit var buyButton: Button
    private lateinit var prevButton: Button
    private lateinit var nextButton: Button
    private lateinit var backButton: Button
    private lateinit var userRepository: UserRepository
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
        backButton = view.findViewById(R.id.backButton)

        val db = AppDatabase.getDatabase(requireContext())
        userRepository = UserRepository(db.userDao())

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
            if (isSkinPurchased(skins[selectedSkinIndex])) {
                selectSkin()
            } else {
                buySkin()
            }
        }

        backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun updateSkinPreview() {
        skinPreview.setImageResource(skins[selectedSkinIndex])

        if (isSkinPurchased(skins[selectedSkinIndex])) {
            buyButton.text = "Выбрать"
        } else {
            buyButton.text = "Купить за ${skinPrices[selectedSkinIndex]} монет"
        }
    }

    private fun isSkinPurchased(skinResId: Int): Boolean {
        val sharedPref = requireActivity().getSharedPreferences("tamagotchi_prefs", Context.MODE_PRIVATE)
        val purchasedSkins = sharedPref.getStringSet("purchased_skins", mutableSetOf()) ?: mutableSetOf()
        return purchasedSkins.contains(skinResId.toString())
    }

    private fun buySkin() {
        lifecycleScope.launch {
            val user = userRepository.getUser()
            val skinPrice = skinPrices[selectedSkinIndex]

            if (user.coins >= skinPrice) {
                userRepository.addCoinsAndXP(-skinPrice, 0)
                savePurchasedSkin(skins[selectedSkinIndex])
                updateSkinPreview()
                Toast.makeText(requireContext(), "Скин куплен!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Недостаточно монет!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun savePurchasedSkin(skinResId: Int) {
        val sharedPref = requireActivity().getSharedPreferences("tamagotchi_prefs", Context.MODE_PRIVATE)
        val purchasedSkins = sharedPref.getStringSet("purchased_skins", mutableSetOf()) ?: mutableSetOf()
        purchasedSkins.add(skinResId.toString())
        sharedPref.edit().putStringSet("purchased_skins", purchasedSkins).apply()
    }

    private fun selectSkin() {
        val sharedPref = requireActivity().getSharedPreferences("tamagotchi_prefs", Context.MODE_PRIVATE)
        sharedPref.edit().putInt("current_skin", skins[selectedSkinIndex]).apply()
        Toast.makeText(requireContext(), "Скин выбран!", Toast.LENGTH_SHORT).show()
    }
}
