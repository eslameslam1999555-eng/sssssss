package com.example.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM weapon_items ORDER BY id ASC")
    fun getAllWeapons(): Flow<List<WeaponItem>>

    @Query("SELECT * FROM player_profile WHERE id = 0 LIMIT 1")
    fun getPlayerProfile(): Flow<PlayerProfile?>

    @Query("SELECT * FROM player_profile WHERE id = 0 LIMIT 1")
    suspend fun getPlayerProfileDirect(): PlayerProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeapon(weapon: WeaponItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayerProfile(profile: PlayerProfile)

    @Update
    suspend fun updatePlayerProfile(profile: PlayerProfile)

    @Update
    suspend fun updateWeapon(weapon: WeaponItem)

    @Query("DELETE FROM weapon_items")
    suspend fun deleteAllWeapons()
}
