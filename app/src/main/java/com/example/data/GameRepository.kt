package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.MissionId
import com.example.model.MissionProgress
import com.example.model.MissionResult
import com.example.model.VehicleConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GameRepository(context: Context) {
  private val prefs: SharedPreferences =
    context.getSharedPreferences("desert_storm_save_data", Context.MODE_PRIVATE)

  private val _credits = MutableStateFlow(prefs.getInt("credits", 1500))
  val credits: StateFlow<Int> = _credits.asStateFlow()

  private val _selectedVehicleId = MutableStateFlow(prefs.getString("selected_vehicle", "dune_raider") ?: "dune_raider")
  val selectedVehicleId: StateFlow<String> = _selectedVehicleId.asStateFlow()

  private val _missionProgressMap = MutableStateFlow<Map<MissionId, MissionProgress>>(loadMissionProgress())
  val missionProgressMap: StateFlow<Map<MissionId, MissionProgress>> = _missionProgressMap.asStateFlow()

  // Upgrades: levels 0 to 4 (each level +10% boost)
  private val _engineLevel = MutableStateFlow(prefs.getInt("upgrade_engine", 1))
  val engineLevel: StateFlow<Int> = _engineLevel.asStateFlow()

  private val _nitroLevel = MutableStateFlow(prefs.getInt("upgrade_nitro", 1))
  val nitroLevel: StateFlow<Int> = _nitroLevel.asStateFlow()

  private val _armorLevel = MutableStateFlow(prefs.getInt("upgrade_armor", 1))
  val armorLevel: StateFlow<Int> = _armorLevel.asStateFlow()

  private val _weaponsLevel = MutableStateFlow(prefs.getInt("upgrade_weapons", 1))
  val weaponsLevel: StateFlow<Int> = _weaponsLevel.asStateFlow()

  val availableVehicles: List<VehicleConfig> = listOf(
    VehicleConfig(
      id = "dune_raider",
      name = "Dune Raider MK-IV",
      tag = "Assault Buggy",
      baseSpeed = 180,
      baseArmor = 100,
      baseNitro = 100,
      baseFirepower = 95,
      unlockCost = 0,
      isUnlocked = true
    ),
    VehicleConfig(
      id = "iron_vulture",
      name = "Iron Vulture XT",
      tag = "Heavy Juggernaut",
      baseSpeed = 155,
      baseArmor = 175,
      baseNitro = 85,
      baseFirepower = 135,
      unlockCost = 3000,
      isUnlocked = prefs.getBoolean("vehicle_unlocked_iron_vulture", false)
    ),
    VehicleConfig(
      id = "ghost_spectre",
      name = "Ghost Spectre VX",
      tag = "Stealth Interceptor",
      baseSpeed = 220,
      baseArmor = 85,
      baseNitro = 140,
      baseFirepower = 110,
      unlockCost = 5000,
      isUnlocked = prefs.getBoolean("vehicle_unlocked_ghost_spectre", false)
    )
  )

  private fun loadMissionProgress(): Map<MissionId, MissionProgress> {
    return mapOf(
      MissionId.MISSION_1 to MissionProgress(
        missionId = MissionId.MISSION_1,
        isUnlocked = true, // First mission is always unlocked
        isCompleted = prefs.getBoolean("m1_completed", false),
        stars = prefs.getInt("m1_stars", 0),
        bestTimeSeconds = prefs.getFloat("m1_best_time", 0f),
        highScore = prefs.getInt("m1_score", 0)
      ),
      MissionId.MISSION_2 to MissionProgress(
        missionId = MissionId.MISSION_2,
        isUnlocked = prefs.getBoolean("m2_unlocked", false) || prefs.getBoolean("m1_completed", false),
        isCompleted = prefs.getBoolean("m2_completed", false),
        stars = prefs.getInt("m2_stars", 0),
        bestTimeSeconds = prefs.getFloat("m2_best_time", 0f),
        highScore = prefs.getInt("m2_score", 0)
      ),
      MissionId.MISSION_3 to MissionProgress(
        missionId = MissionId.MISSION_3,
        isUnlocked = prefs.getBoolean("m3_unlocked", false) || prefs.getBoolean("m2_completed", false),
        isCompleted = prefs.getBoolean("m3_completed", false),
        stars = prefs.getInt("m3_stars", 0),
        bestTimeSeconds = prefs.getFloat("m3_best_time", 0f),
        highScore = prefs.getInt("m3_score", 0)
      )
    )
  }

  fun completeMission(result: MissionResult) {
    val mKey = when (result.missionId) {
      MissionId.MISSION_1 -> "m1"
      MissionId.MISSION_2 -> "m2"
      MissionId.MISSION_3 -> "m3"
    }

    val editor = prefs.edit()
    editor.putBoolean("${mKey}_completed", true)

    val prevStars = prefs.getInt("${mKey}_stars", 0)
    if (result.stars > prevStars) {
      editor.putInt("${mKey}_stars", result.stars)
    }

    val prevBestTime = prefs.getFloat("${mKey}_best_time", 0f)
    if (prevBestTime == 0f || result.timeElapsedSeconds < prevBestTime) {
      editor.putFloat("${mKey}_best_time", result.timeElapsedSeconds)
    }

    val prevScore = prefs.getInt("${mKey}_score", 0)
    if (result.score > prevScore) {
      editor.putInt("${mKey}_score", result.score)
    }

    // Unlock subsequent missions
    if (result.missionId == MissionId.MISSION_1) {
      editor.putBoolean("m2_unlocked", true)
    } else if (result.missionId == MissionId.MISSION_2) {
      editor.putBoolean("m3_unlocked", true)
    }

    // Add credits
    val newCredits = _credits.value + result.creditsEarned
    editor.putInt("credits", newCredits)
    editor.apply()

    _credits.value = newCredits
    _missionProgressMap.value = loadMissionProgress()
  }

  fun selectVehicle(vehicleId: String) {
    prefs.edit().putString("selected_vehicle", vehicleId).apply()
    _selectedVehicleId.value = vehicleId
  }

  fun unlockVehicle(vehicleId: String, cost: Int): Boolean {
    if (_credits.value < cost) return false
    val newCredits = _credits.value - cost
    prefs.edit()
      .putInt("credits", newCredits)
      .putBoolean("vehicle_unlocked_$vehicleId", true)
      .apply()
    _credits.value = newCredits
    selectVehicle(vehicleId)
    return true
  }

  fun upgradeStat(type: String, cost: Int): Boolean {
    if (_credits.value < cost) return false
    val editor = prefs.edit()
    val newCredits = _credits.value - cost
    editor.putInt("credits", newCredits)

    when (type) {
      "engine" -> {
        val next = (_engineLevel.value + 1).coerceAtMost(5)
        editor.putInt("upgrade_engine", next)
        _engineLevel.value = next
      }
      "nitro" -> {
        val next = (_nitroLevel.value + 1).coerceAtMost(5)
        editor.putInt("upgrade_nitro", next)
        _nitroLevel.value = next
      }
      "armor" -> {
        val next = (_armorLevel.value + 1).coerceAtMost(5)
        editor.putInt("upgrade_armor", next)
        _armorLevel.value = next
      }
      "weapons" -> {
        val next = (_weaponsLevel.value + 1).coerceAtMost(5)
        editor.putInt("upgrade_weapons", next)
        _weaponsLevel.value = next
      }
    }
    editor.apply()
    _credits.value = newCredits
    return true
  }
}
