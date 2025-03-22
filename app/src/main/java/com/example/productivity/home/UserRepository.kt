package com.example.productivity.home

import android.util.Log

class UserRepository(private val userDao: UserDao) {

    suspend fun getUser(): UserEntity {
        userDao.createUserIfNotExists()
        return userDao.getUser() ?: UserEntity()
    }

    suspend fun addCoinsAndXP(coins: Int, xp: Int) {
        Log.d("UserRepository", "Начисляем монеты: $coins, XP: $xp")
        userDao.createUserIfNotExists()
        userDao.updateCoinsAndXP(coins, xp)
        val user = getUser()
        Log.d("UserRepository", "Теперь у пользователя: ${user.coins} монет, ${user.xp} XP")
    }

    suspend fun setLives(lives: Int) {
        userDao.updateLives(lives)
    }

    suspend fun getLives(): Int {
        return userDao.getLives()
    }
    suspend fun updateLives(newLives: Int) {
        val user = userDao.getUser() ?: return
        val updatedUser = user.copy(lives = newLives)
        userDao.updateUser(updatedUser)
    }


}

