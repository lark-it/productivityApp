package com.example.productivity.home

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update

@Dao
interface UserDao {
    @Query("SELECT * FROM user LIMIT 1")
    suspend fun getUser(): UserEntity?

    @Query("UPDATE user SET coins = coins + :coins, xp = xp + :xp WHERE id = 1")
    suspend fun updateCoinsAndXP(coins: Int, xp: Int)

    @Query("INSERT OR IGNORE INTO user (id, coins, xp, lives) VALUES (1, 0, 0, 3)")
    suspend fun createUserIfNotExists()

    @Query("UPDATE user SET lives = :lives WHERE id = 1")
    suspend fun updateLives(lives: Int)

    @Query("SELECT lives FROM user WHERE id = 1")
    suspend fun getLives(): Int

    @Update
    suspend fun updateUser(user: UserEntity)

}
