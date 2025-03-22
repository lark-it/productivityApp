package com.example.productivity.game

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.productivity.AppDatabase
import com.example.productivity.R
import com.example.productivity.home.MainViewModel
import com.example.productivity.home.UserRepository
import kotlinx.coroutines.launch

class GamesFragment : Fragment() {

    private lateinit var petImage: ImageView
    private lateinit var shopButton: Button
    private lateinit var livesProgressBar: ProgressBar
    private lateinit var livesText: TextView
    private lateinit var reviveLayout: View
    private lateinit var reviveButton: Button
    private lateinit var userRepository: UserRepository
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_games, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        petImage = view.findViewById(R.id.petImage)
        shopButton = view.findViewById(R.id.shopButton)
        livesProgressBar = view.findViewById(R.id.livesProgressBar)
        livesText = view.findViewById(R.id.livesText)
        reviveLayout = view.findViewById(R.id.reviveLayout)
        reviveButton = view.findViewById(R.id.reviveButton)

        // Инициализируем UserRepository
        val db = AppDatabase.getDatabase(requireContext())
        userRepository = UserRepository(db.userDao())

        shopButton.setOnClickListener {
            findNavController().navigate(R.id.action_gamesFragment_to_shopFragment)
        }

        reviveButton.setOnClickListener {
            revivePet()
        }

        loadSkin()

        // Подписываемся на изменения жизней
        viewModel.lives.observe(viewLifecycleOwner) { lives ->
            val maxLives = 3
            livesProgressBar.max = maxLives
            livesProgressBar.progress = lives
            livesText.text = "$lives/$maxLives"

            // Обновляем UI в зависимости от количества жизней
            updatePetState(lives)

            // Запускаем анимацию при изменении жизней
            updateLivesUI(lives)
        }
    }

    private fun loadSkin() {
        val sharedPref = requireActivity().getSharedPreferences("tamagotchi_prefs", 0)
        val skinResId = sharedPref.getInt("current_skin", R.drawable.pet_default)
        // Если жизни не 0, используем текущий скин
        val lives = viewModel.lives.value ?: 3
        if (lives > 0) {
            petImage.setImageResource(skinResId)
        } else {
            petImage.setImageResource(R.drawable.pet_dead)
        }
    }

    private fun updatePetState(lives: Int) {
        if (lives <= 0) {
            // Питомец "мёртв"
            petImage.setImageResource(R.drawable.pet_dead)
            reviveLayout.visibility = View.VISIBLE
            shopButton.visibility = View.GONE // Скрываем кнопку магазина, пока питомец мёртв
        } else {
            // Питомец "жив"
            val sharedPref = requireActivity().getSharedPreferences("tamagotchi_prefs", 0)
            val skinResId = sharedPref.getInt("current_skin", R.drawable.pet_default)
            petImage.setImageResource(skinResId)
            reviveLayout.visibility = View.GONE
            shopButton.visibility = View.VISIBLE
        }
    }

    private fun updateLivesUI(lives: Int) {
        val maxLives = 3
        livesProgressBar.max = maxLives
        livesProgressBar.progress = 0 // Начинаем с 0
        livesProgressBar.animate()
            .setDuration(500) // Анимация 500 мс
            .setUpdateListener { animation ->
                val animatedValue = animation.animatedValue as Float
                livesProgressBar.progress = (animatedValue * lives.toFloat()).toInt()
            }
            .start()

        livesText.text = "$lives/$maxLives"
    }

    private fun revivePet() {
        lifecycleScope.launch {
            val user = userRepository.getUser()
            val reviveCost = 20

            if (user.coins >= reviveCost) {
                // Снимаем монеты и восстанавливаем жизни
                userRepository.addCoinsAndXP(-reviveCost, 0)
                userRepository.updateLives(3)
                viewModel.lives.postValue(3) // Обновляем UI
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Питомец воскрешён!", Toast.LENGTH_SHORT).show()
                }
            } else {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Недостаточно монет! Нужно $reviveCost монет.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // При возвращении на фрагмент обновляем UI, если данные уже есть
        viewModel.lives.value?.let { lives ->
            updateLivesUI(lives)
            updatePetState(lives)
        }
    }
}