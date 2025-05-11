package com.example.productivity.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.productivity.data.auth.UserRepository
import kotlinx.coroutines.launch

class GamesViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _lives = MutableLiveData<Int>()
    val lives: LiveData<Int> = _lives

    private val _level = MutableLiveData<Int>()
    val level: LiveData<Int> = _level

    private val _rank = MutableLiveData<String>()
    val rank: LiveData<String> = _rank

    fun loadUserData() {
        viewModelScope.launch {
            val user = userRepository.getUser()
            _lives.postValue(user.lives)
            _level.postValue(user.level)
            _rank.postValue(user.rank)
        }
    }

    fun decreaseLives() {
        viewModelScope.launch {
            val currentLives = userRepository.getUser().lives
            if (currentLives > 0) {
                userRepository.updateLives(currentLives - 1)
                _lives.postValue(currentLives - 1)
            }
        }
    }
}
