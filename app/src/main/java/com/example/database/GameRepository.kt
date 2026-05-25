package com.example.database

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GameRepository(private val gameDao: GameDao) {
    val allWeapons: Flow<List<WeaponItem>> = gameDao.getAllWeapons()
    val playerProfile: Flow<PlayerProfile?> = gameDao.getPlayerProfile()

    suspend fun updateProfile(profile: PlayerProfile) = withContext(Dispatchers.IO) {
        gameDao.updatePlayerProfile(profile)
    }

    suspend fun updateWeapon(weapon: WeaponItem) = withContext(Dispatchers.IO) {
        gameDao.updateWeapon(weapon)
    }

    suspend fun insertWeapon(weapon: WeaponItem) = withContext(Dispatchers.IO) {
        gameDao.insertWeapon(weapon)
    }

    suspend fun ensureDefaultsPreloaded() = withContext(Dispatchers.IO) {
        val existingProfile = gameDao.getPlayerProfileDirect()
        if (existingProfile == null) {
            // Setup default profile
            val defaultProfile = PlayerProfile(
                id = 0,
                playerName = "Eslam Sobhy", // Default to user's name as executive champion profile
                coins = 3000,
                currentLevel = 1,
                totalWins = 35,
                bossCompletedLevel = 3,
                systemVRModeOn = false,
                selectedLanguage = "ar",
                chosenWeaponId = 1
            )
            gameDao.insertPlayerProfile(defaultProfile)

            // Setup default weapons
            gameDao.deleteAllWeapons()
            gameDao.insertWeapon(WeaponItem(1, "Eslam's Valor Blade", "Laser Sabre", 75.0f, 8.5f, "Melee", "#FF2255", true, 0))
            gameDao.insertWeapon(WeaponItem(2, "Orbital Disrupter", "Plasma Blaster", 45.0f, 6.0f, "Ranged", "#00DDFF", true, 0))
            gameDao.insertWeapon(WeaponItem(3, "Cosmic Rail Cannon", "Quantum Rifle", 90.0f, 2.5f, "Energy Beam", "#FFBB00", false, 1200))
            gameDao.insertWeapon(WeaponItem(4, "Nano Vortex Slayer", "Laser Sabre", 100.0f, 9.5f, "Melee", "#FF00FF", false, 2500))
            gameDao.insertWeapon(WeaponItem(5, "Chronoshift Stinger", "Plasma Blaster", 60.0f, 7.5f, "Ranged", "#00FF66", false, 800))
        }
    }
}
