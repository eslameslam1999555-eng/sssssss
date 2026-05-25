package com.example.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weapon_items")
data class WeaponItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: String, // e.g. "Laser Sabre", "Plasma Blaster", "Quantum Rifle"
    val damage: Float, // 10.0 to 100.0
    val speed: Float,  // 1.0 to 10.0
    val attackType: String, // "Melee", "Ranged" or "Energy Beam"
    val crystalColor: String, // Hex string representer (e.g. "#FF0055")
    val isUnlocked: Boolean = false,
    val cost: Int = 100
)

@Entity(tableName = "player_profile")
data class PlayerProfile(
    @PrimaryKey val id: Int = 0, // Only single profile row
    val playerName: String = "Eslam Sobhy Guest",
    val coins: Int = 1500,
    val currentLevel: Int = 1,
    val totalWins: Int = 0,
    val bossCompletedLevel: Int = 0,
    val systemVRModeOn: Boolean = false,
    val selectedLanguage: String = "ar", // "ar" for Arabic, "en" for English
    val chosenWeaponId: Int = 1 // Linked active custom weapon ID
)
