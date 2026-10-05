package com.example.model

enum class ScreenState {
  MAIN_MENU,
  MISSION_SELECT,
  MISSION_1_STEALTH,
  MISSION_2_CHASE,
  MISSION_3_BOMB,
  GARAGE,
  INTEL_ARCHIVE,
  MISSION_DEBRIEF
}

enum class MissionId(val code: String, val title: String, val subtitle: String) {
  MISSION_1("M1", "Hangar Infiltration", "Stealth entry avoiding radar sweeps & security grids"),
  MISSION_2("M2", "Dune Pursuit", "High-speed chase with nitro boosts to recover the data chip"),
  MISSION_3("M3", "Zero Hour HQ", "Final showdown to deactivate bomb under 60 seconds")
}

data class MissionProgress(
  val missionId: MissionId,
  val isUnlocked: Boolean = false,
  val isCompleted: Boolean = false,
  val stars: Int = 0,
  val bestTimeSeconds: Float = 0f,
  val highScore: Int = 0
)

data class VehicleConfig(
  val id: String,
  val name: String,
  val tag: String,
  val baseSpeed: Int,
  val baseArmor: Int,
  val baseNitro: Int,
  val baseFirepower: Int,
  val unlockCost: Int,
  val isUnlocked: Boolean = false
)

data class MissionResult(
  val missionId: MissionId,
  val isSuccess: Boolean,
  val timeElapsedSeconds: Float,
  val stars: Int,
  val score: Int,
  val creditsEarned: Int,
  val debriefNotes: List<String>
)

data class IntelEntry(
  val id: String,
  val title: String,
  val classification: String,
  val summary: String,
  val fullText: String,
  val isUnlocked: Boolean = true
)
