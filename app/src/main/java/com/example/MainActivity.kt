package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.audio.GameSoundManager
import com.example.data.GameRepository
import com.example.model.MissionId
import com.example.model.MissionResult
import com.example.model.ScreenState
import com.example.ui.screens.GarageScreen
import com.example.ui.screens.IntelArchiveScreen
import com.example.ui.screens.MainMenuScreen
import com.example.ui.screens.Mission1StealthScreen
import com.example.ui.screens.Mission2ChaseScreen
import com.example.ui.screens.Mission3BombScreen
import com.example.ui.screens.MissionDebriefScreen
import com.example.ui.screens.MissionSelectScreen
import com.example.ui.theme.CarbonBlack
import com.example.ui.theme.DesertStormTheme

class MainActivity : ComponentActivity() {
  private lateinit var soundManager: GameSoundManager
  private lateinit var repository: GameRepository

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    soundManager = GameSoundManager(this)
    repository = GameRepository(this)

    setContent {
      DesertStormTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = CarbonBlack
        ) {
          DesertStormGameApp(
            repository = repository,
            soundManager = soundManager
          )
        }
      }
    }
  }
}

@Composable
fun DesertStormGameApp(
  repository: GameRepository,
  soundManager: GameSoundManager
) {
  var currentScreen by remember { mutableStateOf(ScreenState.MAIN_MENU) }
  var activeMissionResult by remember { mutableStateOf<MissionResult?>(null) }

  val credits by repository.credits.collectAsState()
  val selectedVehicleId by repository.selectedVehicleId.collectAsState()
  val missionProgress by repository.missionProgressMap.collectAsState()
  val engineLevel by repository.engineLevel.collectAsState()
  val nitroLevel by repository.nitroLevel.collectAsState()
  val armorLevel by repository.armorLevel.collectAsState()
  val weaponsLevel by repository.weaponsLevel.collectAsState()

  val selectedVehicle = repository.availableVehicles.firstOrNull { it.id == selectedVehicleId }
    ?: repository.availableVehicles.first()

  when (currentScreen) {
    ScreenState.MAIN_MENU -> {
      MainMenuScreen(
        credits = credits,
        missionProgress = missionProgress,
        soundManager = soundManager,
        onStartCampaign = { currentScreen = ScreenState.MISSION_SELECT },
        onOpenGarage = { currentScreen = ScreenState.GARAGE },
        onOpenIntel = { currentScreen = ScreenState.INTEL_ARCHIVE },
        onToggleSound = { soundManager.isSoundEnabled = !soundManager.isSoundEnabled },
        onToggleVibration = { soundManager.isVibrationEnabled = !soundManager.isVibrationEnabled }
      )
    }

    ScreenState.MISSION_SELECT -> {
      BackHandler { currentScreen = ScreenState.MAIN_MENU }
      MissionSelectScreen(
        missionProgress = missionProgress,
        soundManager = soundManager,
        onLaunchMission = { missionId ->
          when (missionId) {
            MissionId.MISSION_1 -> currentScreen = ScreenState.MISSION_1_STEALTH
            MissionId.MISSION_2 -> currentScreen = ScreenState.MISSION_2_CHASE
            MissionId.MISSION_3 -> currentScreen = ScreenState.MISSION_3_BOMB
          }
        },
        onBack = { currentScreen = ScreenState.MAIN_MENU }
      )
    }

    ScreenState.MISSION_1_STEALTH -> {
      BackHandler { currentScreen = ScreenState.MISSION_SELECT }
      Mission1StealthScreen(
        soundManager = soundManager,
        onMissionFinished = { result ->
          repository.completeMission(result)
          activeMissionResult = result
          currentScreen = ScreenState.MISSION_DEBRIEF
        },
        onExit = { currentScreen = ScreenState.MISSION_SELECT }
      )
    }

    ScreenState.MISSION_2_CHASE -> {
      BackHandler { currentScreen = ScreenState.MISSION_SELECT }
      Mission2ChaseScreen(
        selectedVehicle = selectedVehicle,
        engineLevel = engineLevel,
        nitroLevel = nitroLevel,
        armorLevel = armorLevel,
        weaponsLevel = weaponsLevel,
        soundManager = soundManager,
        onMissionFinished = { result ->
          repository.completeMission(result)
          activeMissionResult = result
          currentScreen = ScreenState.MISSION_DEBRIEF
        },
        onExit = { currentScreen = ScreenState.MISSION_SELECT }
      )
    }

    ScreenState.MISSION_3_BOMB -> {
      BackHandler { currentScreen = ScreenState.MISSION_SELECT }
      Mission3BombScreen(
        soundManager = soundManager,
        onMissionFinished = { result ->
          repository.completeMission(result)
          activeMissionResult = result
          currentScreen = ScreenState.MISSION_DEBRIEF
        },
        onExit = { currentScreen = ScreenState.MISSION_SELECT }
      )
    }

    ScreenState.GARAGE -> {
      BackHandler { currentScreen = ScreenState.MAIN_MENU }
      GarageScreen(
        vehicles = repository.availableVehicles,
        selectedVehicleId = selectedVehicleId,
        credits = credits,
        engineLevel = engineLevel,
        nitroLevel = nitroLevel,
        armorLevel = armorLevel,
        weaponsLevel = weaponsLevel,
        soundManager = soundManager,
        onSelectVehicle = { repository.selectVehicle(it) },
        onUnlockVehicle = { vId, cost -> repository.unlockVehicle(vId, cost) },
        onUpgradeStat = { stat, cost -> repository.upgradeStat(stat, cost) },
        onBack = { currentScreen = ScreenState.MAIN_MENU }
      )
    }

    ScreenState.INTEL_ARCHIVE -> {
      BackHandler { currentScreen = ScreenState.MAIN_MENU }
      IntelArchiveScreen(
        soundManager = soundManager,
        onBack = { currentScreen = ScreenState.MAIN_MENU }
      )
    }

    ScreenState.MISSION_DEBRIEF -> {
      BackHandler { currentScreen = ScreenState.MISSION_SELECT }
      activeMissionResult?.let { result ->
        MissionDebriefScreen(
          result = result,
          soundManager = soundManager,
          onNextMission = { nextId ->
            when (nextId) {
              MissionId.MISSION_1 -> currentScreen = ScreenState.MISSION_1_STEALTH
              MissionId.MISSION_2 -> currentScreen = ScreenState.MISSION_2_CHASE
              MissionId.MISSION_3 -> currentScreen = ScreenState.MISSION_3_BOMB
            }
          },
          onReturnToMenu = { currentScreen = ScreenState.MAIN_MENU }
        )
      } ?: run {
        currentScreen = ScreenState.MAIN_MENU
      }
    }
  }
}
