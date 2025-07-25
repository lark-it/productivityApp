package com.example.productivity.ui.game

import android.animation.ValueAnimator
import android.os.Bundle
import android.util.Log
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
import com.example.productivity.ui.home.MainViewModel
import com.example.productivity.data.auth.UserRepository
import android.view.animation.AccelerateDecelerateInterpolator
import com.example.productivity.data.util.Constants.MAX_LIVES
import kotlinx.coroutines.launch

class GamesFragment : Fragment() {

    private lateinit var petImage: ImageView
    private lateinit var shopButton: Button
    private lateinit var livesProgressBar: ProgressBar
    private lateinit var livesText: TextView
    private lateinit var reviveLayout: View
    private lateinit var reviveButton: Button
    private lateinit var xpProgressBar: ProgressBar
    private lateinit var xpText: TextView
    private lateinit var levelText: TextView
    private lateinit var rankText: TextView
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
        xpProgressBar = view.findViewById(R.id.xpProgressBar)
        xpText = view.findViewById(R.id.xpText)
        levelText = view.findViewById(R.id.levelText)
        rankText = view.findViewById(R.id.rankText)

        val db = AppDatabase.getDatabase(requireContext())
        userRepository = UserRepository(db.userDao())

        shopButton.setOnClickListener {
            findNavController().navigate(R.id.action_gamesFragment_to_shopFragment)
        }

        reviveButton.setOnClickListener {
            revivePet()
        }

        loadSkin()
        updateLevelAndXpUI()

        viewModel.lives.observe(viewLifecycleOwner) { lives ->
            livesProgressBar.max = MAX_LIVES
            livesProgressBar.progress = lives
            livesText.text = "$lives/$MAX_LIVES"

            updatePetState(lives)
            updateLivesUI(lives)
        }

        viewModel.level.observe(viewLifecycleOwner) { level ->
            updateLevelAndXpUI()
        }

        viewModel.checkLevelUp(view) // Передаём view
    }

    private fun loadSkin() {
        val sharedPref = requireActivity().getSharedPreferences("tamagotchi_prefs", 0)
        val skinResId = sharedPref.getInt("current_skin", R.drawable.pet_default)
        val lives = viewModel.lives.value ?: MAX_LIVES
        if (lives > 0) {
            petImage.setImageResource(skinResId)
        } else {
            petImage.setImageResource(R.drawable.pet_dead)
        }
    }

    private fun updatePetState(lives: Int) {
        if (lives <= 0) {
            petImage.setImageResource(R.drawable.pet_dead)
            reviveLayout.visibility = View.VISIBLE
            shopButton.visibility = View.GONE
        } else {
            val sharedPref = requireActivity().getSharedPreferences("tamagotchi_prefs", 0)
            val skinResId = sharedPref.getInt("current_skin", R.drawable.pet_default)
            petImage.setImageResource(skinResId)
            reviveLayout.visibility = View.GONE
            shopButton.visibility = View.VISIBLE
        }
    }

    private fun updateLivesUI(lives: Int) {
        livesProgressBar.max = MAX_LIVES

        val animator = ValueAnimator.ofInt(0, lives)
        animator.duration = 1000
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.addUpdateListener { animation ->
            val animatedValue = animation.animatedValue as Int
            livesProgressBar.progress = animatedValue
        }
        animator.start()

        livesText.text = "$lives/$MAX_LIVES"
    }

    private fun updateLevelAndXpUI() {
        lifecycleScope.launch {
            val user = userRepository.getUser()
            val currentLevel = user.level
            val currentXp = user.xp
            val xpForCurrentLevel = userRepository.getXpForCurrentLevel(currentLevel)
            val xpProgress = currentXp - xpForCurrentLevel
            val xpMax = userRepository.getXpMaxForLevel(currentLevel)
            Log.d("GamesFragment", "XP: $currentXp, Уровень: $currentLevel, Прогресс: $xpProgress/$xpMax")

            requireActivity().runOnUiThread {
                levelText.text = "Уровень: $currentLevel"
                rankText.text = user.rank
                xpProgressBar.max = xpMax

                val currentProgress = xpProgressBar.progress
                val animator = ValueAnimator.ofInt(currentProgress, xpProgress.coerceAtLeast(0))
                animator.duration = 1000
                animator.addUpdateListener { animation ->
                    val animatedValue = animation.animatedValue as Int
                    xpProgressBar.progress = animatedValue
                }
                animator.start()

                xpText.text = "$xpProgress/$xpMax"
            }
        }
    }

    private fun revivePet() {
        lifecycleScope.launch {
            val user = userRepository.getUser()
            val reviveCost = 20

            if (user.coins >= reviveCost) {
                userRepository.addCoinsAndXP(-reviveCost, 0)
                userRepository.updateLives(MAX_LIVES)
                viewModel.lives.postValue(MAX_LIVES)
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
        viewModel.lives.value?.let { lives ->
            updateLivesUI(lives)
            updatePetState(lives)
        }
        updateLevelAndXpUI()
        viewModel.checkLevelUp(view)
    }
}