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

    @Query("INSERT OR IGNORE INTO user (id, coins, xp) VALUES (1, 0, 0)")
    suspend fun createUserIfNotExists()

}
