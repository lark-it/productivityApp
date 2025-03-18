package com.example.productivity.game

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.navigation.fragment.findNavController
import com.example.productivity.R

class GamesFragment : Fragment() {

    private lateinit var petImage: ImageView
    private lateinit var shopButton: Button

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

        shopButton.setOnClickListener {
            findNavController().navigate(R.id.action_gamesFragment_to_shopFragment)
        }

        loadSkin()
    }

    private fun loadSkin() {
        val sharedPref = requireActivity().getSharedPreferences("tamagotchi_prefs", 0)
        val skinResId = sharedPref.getInt("current_skin", R.drawable.pet_default)
        petImage.setImageResource(skinResId)
    }
}
