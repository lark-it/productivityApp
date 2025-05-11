package com.example.productivity.data.auth

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
        updateLevelAndRank(user)
        val updatedUser = getUser()
        Log.d("UserRepository", "Теперь у пользователя: ${updatedUser.coins} монет, ${updatedUser.xp} XP, уровень: ${updatedUser.level}, звание: ${updatedUser.rank}")
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

    private suspend fun updateLevelAndRank(user: UserEntity) {
        val xpThresholds = listOf(0, 10, 40, 80, 130, 190, 260, 340, 430, 520, 620)

        val newLevel = xpThresholds.indexOfFirst { user.xp < it }.coerceAtLeast(1)
        val newRank = getRankForLevel(newLevel)
        Log.d("UserRepository", "Текущий XP: ${user.xp}, текущий уровень: ${user.level}, новый уровень: $newLevel")

        if (newLevel != user.level || newRank != user.rank) {
            userDao.updateLevelAndRank(newLevel, newRank)
            Log.d("UserRepository", "Уровень обновлён: $newLevel, звание: $newRank")
        } else {
            Log.d("UserRepository", "Уровень не изменился: $newLevel")
        }
    }

    private fun getRankForLevel(level: Int): String {
        return when (level) {
            in 1..2 -> "Новичок"
            in 3..4 -> "Ученик"
            in 5..6 -> "Мастер"
            in 7..8 -> "Эксперт"
            in 9..10 -> "Легенда"
            else -> "Бог продуктивности"
        }
    }

    fun getXpForNextLevel(currentLevel: Int): Int {
        val xpThresholds = listOf(0, 10, 40, 80, 130, 190, 260, 340, 430, 520, 620)
        return xpThresholds.getOrElse(currentLevel) { 440 }
    }

    fun getXpForCurrentLevel(currentLevel: Int): Int {
        val xpThresholds = listOf(0, 10, 40, 80, 130, 190, 260, 340, 430, 520, 620)
        return xpThresholds.getOrElse(currentLevel - 1) { 0 }
    }

    fun getXpMaxForLevel(currentLevel: Int): Int {
        val xpThresholds = listOf(0, 10, 40, 80, 130, 190, 260, 340, 430, 520, 620)
        val current = xpThresholds.getOrElse(currentLevel - 1) { 0 }
        val next = xpThresholds.getOrElse(currentLevel) { 440 }
        return next - current
    }
}