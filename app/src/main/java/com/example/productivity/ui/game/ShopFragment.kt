package com.example.productivity.ui.game

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.productivity.R
import com.example.productivity.AppDatabase
import com.example.productivity.ui.home.MainViewModel
import com.example.productivity.ui.home.MainViewModelFactory
import com.example.productivity.data.auth.UserRepository
import com.example.productivity.data.util.Constants.MAX_LIVES
import kotlinx.coroutines.launch

class ShopFragment : Fragment() {

    private lateinit var skinPreview: ImageView
    private lateinit var buyButton: Button
    private lateinit var backButton: Button
    private lateinit var userRepository: UserRepository
    private var selectedSkinIndex = 0
    private lateinit var prevButton: ImageButton
    private lateinit var nextButton: ImageButton
    private lateinit var viewModel: MainViewModel
    private lateinit var healButton: ImageButton
    private lateinit var healPopup: View
    private lateinit var confirmHealButton: Button

    private val skins = listOf(
        R.drawable.pet_default,
        R.drawable.pet_flower,
        R.drawable.pet_knight,
        R.drawable.pet_wizard,
        R.drawable.pet_king
    )

    private val skinPrices = listOf(0, 15, 30, 50, 100)

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
        backButton = view.findViewById(R.id.backButton)

        val db = AppDatabase.getDatabase(requireContext())

        val taskDao = db.taskDao()
        val habitsDao = db.habitsDao()

        userRepository = UserRepository(db.userDao())
        viewModel = ViewModelProvider(requireActivity(), MainViewModelFactory(userRepository, taskDao, habitsDao, requireContext()))
            .get(MainViewModel::class.java)


        updateSkinPreview()

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

        healButton = view.findViewById(R.id.healButton)
        healPopup = view.findViewById(R.id.healPopup)
        confirmHealButton = view.findViewById(R.id.confirmHealButton)

        healButton.setOnClickListener {
            lifecycleScope.launch {
                val user = userRepository.getUser()
                if (user.lives >= MAX_LIVES) {
                    Toast.makeText(requireContext(), "У вас уже максимум жизней", Toast.LENGTH_SHORT).show()
                } else {
                    healPopup.visibility = View.VISIBLE
                }
            }
        }

        confirmHealButton.setOnClickListener {
            lifecycleScope.launch {
                val user = userRepository.getUser()
                if (user.coins >= 5 && user.lives < MAX_LIVES) {
                    userRepository.addCoinsAndXP(-5, 0)
                    userRepository.updateLives(user.lives + 1)
                    viewModel.updateLives()
                    Toast.makeText(requireContext(), "+1 жизнь! Береги себя и не забывай выполнять задачи!", Toast.LENGTH_SHORT).show()
                }
 else {
                    Toast.makeText(requireContext(), "Недостаточно монет или максимум жизней", Toast.LENGTH_SHORT).show()
                }
                healPopup.visibility = View.GONE
            }
        }

        prevButton = view.findViewById(R.id.prevSkinButton)
        nextButton = view.findViewById(R.id.nextSkinButton)

        prevButton.setOnClickListener {
            selectedSkinIndex = if (selectedSkinIndex > 0) selectedSkinIndex - 1 else skins.size - 1
            updateSkinPreview()
        }

        nextButton.setOnClickListener {
            selectedSkinIndex = if (selectedSkinIndex < skins.size - 1) selectedSkinIndex + 1 else 0
            updateSkinPreview()
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
