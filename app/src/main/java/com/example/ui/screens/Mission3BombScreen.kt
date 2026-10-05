package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.GameSoundManager
import com.example.model.MissionId
import com.example.model.MissionResult
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedGlow
import com.example.ui.theme.CarbonBlack
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonCard
import com.example.ui.theme.CarbonDark
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.CarbonTextMuted
import com.example.ui.theme.CarbonTextPrimary
import com.example.ui.theme.CarbonTextSecondary
import com.example.ui.theme.DesertGold
import com.example.ui.theme.DesertGoldLight
import com.example.ui.theme.NitroBlue
import com.example.ui.theme.NitroBlueGlow
import com.example.ui.theme.RadarGreen
import com.example.ui.theme.RadarGreenGlow
import com.example.ui.theme.ThreatOrange
import kotlin.math.cos
import kotlin.math.sin

data class DefusalWire(
  val id: Int,
  val name: String,
  val color: Color,
  val isDetonator: Boolean,
  val isCorrectToCut: Boolean,
  var isCut: Boolean = false
)

@Composable
fun Mission3BombScreen(
  soundManager: GameSoundManager,
  onMissionFinished: (MissionResult) -> Unit,
  onExit: () -> Unit
) {
  // 60-second countdown (in seconds)
  var timeLeftSeconds by remember { mutableFloatStateOf(60.00f) }
  var isGameOver by remember { mutableStateOf(false) }
  var isMissionSuccess by remember { mutableStateOf(false) }
  var failureReason by remember { mutableStateOf("") }

  // Current stage: 1 = Frequency Sync, 2 = Pressure Balancing, 3 = Wire Cutting Matrix
  var currentStage by remember { mutableIntStateOf(1) }

  // Stage 1: Frequency Sync
  var syncFreq1 by remember { mutableFloatStateOf(0.2f) }
  var syncFreq2 by remember { mutableFloatStateOf(0.7f) }
  var isFreq1Locked by remember { mutableStateOf(false) }
  var isFreq2Locked by remember { mutableStateOf(false) }

  // Stage 2: Pressure valves (3 valves must all be in safe green range 20..40 PSI)
  var pressureA by remember { mutableFloatStateOf(78f) }
  var pressureB by remember { mutableFloatStateOf(65f) }
  var pressureC by remember { mutableFloatStateOf(85f) }
  var isPressureStabilized by remember { mutableStateOf(false) }

  // Stage 3: Wire Matrix
  // Rules:
  // Red is detonator (instant explosion!)
  // Blue is false wire
  // Green is auxiliary capacitor (must cut 1st)
  // Yellow is ignition trigger (must cut 2nd)
  val wires = remember {
    mutableStateListOf(
      DefusalWire(1, "CIRCUIT ALPHA (GREEN)", RadarGreen, isDetonator = false, isCorrectToCut = true),
      DefusalWire(2, "IGNITION LEAD (YELLOW)", DesertGold, isDetonator = false, isCorrectToCut = true),
      DefusalWire(3, "STABILIZER (BLUE)", NitroBlue, isDetonator = false, isCorrectToCut = false),
      DefusalWire(4, "DETONATOR CORE (RED)", AlertRed, isDetonator = true, isCorrectToCut = false)
    )
  }
  var wiresCutCount by remember { mutableIntStateOf(0) }

  // Hostile Drone Breach alert
  var breachDroneActive by remember { mutableStateOf(false) }
  var breachTimer by remember { mutableFloatStateOf(14f) }

  // Red screen pulse animation when time is low
  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.05f,
    targetValue = if (timeLeftSeconds <= 15f) 0.35f else 0.12f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = if (timeLeftSeconds <= 15f) 350 else 750),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulseAlpha"
  )

  // 60FPS Game Timer Loop
  var lastFrameNanos by remember { mutableLongStateOf(0L) }
  var lastTickSecond by remember { mutableIntStateOf(60) }

  LaunchedEffect(isGameOver, isMissionSuccess) {
    if (isGameOver || isMissionSuccess) return@LaunchedEffect
    lastFrameNanos = 0L

    while (true) {
      withFrameNanos { now ->
        if (lastFrameNanos != 0L) {
          val dt = ((now - lastFrameNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)

          // Hostile breach timer
          breachTimer -= dt
          if (breachTimer <= 0f && !breachDroneActive && !isMissionSuccess) {
            breachDroneActive = true
            soundManager.playAlert()
          }

          // If breach active, time drains 50% faster due to interference!
          val timeMultiplier = if (breachDroneActive) 1.5f else 1.0f
          timeLeftSeconds = (timeLeftSeconds - dt * timeMultiplier).coerceAtLeast(0f)

          // Audio heartbeat tick
          val secInt = timeLeftSeconds.toInt()
          if (secInt != lastTickSecond) {
            lastTickSecond = secInt
            soundManager.playBombTick(secInt)
          }

          // Stage 1 dynamic oscillating waves
          if (currentStage == 1) {
            if (!isFreq1Locked) {
              syncFreq1 = (0.5f + sin(timeLeftSeconds * 3.5f) * 0.45f).coerceIn(0f, 1f)
            }
            if (!isFreq2Locked) {
              syncFreq2 = (0.5f + cos(timeLeftSeconds * 2.8f) * 0.45f).coerceIn(0f, 1f)
            }

            if (isFreq1Locked && isFreq2Locked) {
              currentStage = 2
              soundManager.playSuccess()
            }
          }

          // Stage 2 dynamic pressure creep
          if (currentStage == 2 && !isPressureStabilized) {
            pressureA = (pressureA + dt * 4f).coerceAtMost(100f)
            pressureB = (pressureB + dt * 3f).coerceAtMost(100f)
            pressureC = (pressureC + dt * 5f).coerceAtMost(100f)

            if (pressureA in 15f..42f && pressureB in 15f..42f && pressureC in 15f..42f) {
              isPressureStabilized = true
              currentStage = 3
              soundManager.playSuccess()
            }
          }

          // Check timer expiration
          if (timeLeftSeconds <= 0f) {
            isGameOver = true
            failureReason = "TIME EXPIRED: Warhead detonated at 00:00:00."
            soundManager.playExplosion()
          }
        }
        lastFrameNanos = now
      }
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(CarbonBlack)
      .testTag("mission_3_bomb_container")
  ) {
    // Red emergency room pulse
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(AlertRed.copy(alpha = pulseAlpha))
    )

    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
      // Top Status Bar: Exit button & HQ Alert header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Security, contentDescription = "Bunker", tint = AlertRed)
          Spacer(modifier = Modifier.width(8.dp))
          Column {
            Text(
              text = "ENEMY HQ BUNKER: CORE CHAMBER",
              color = CarbonTextPrimary,
              fontSize = 13.sp,
              fontWeight = FontWeight.Black,
              fontFamily = FontFamily.Monospace
            )
            Text(
              text = "OMEGA WARHEAD ARMED",
              color = AlertRed,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
          }
        }

        IconButton(
          onClick = onExit,
          modifier = Modifier
            .size(36.dp)
            .background(CarbonCard, CircleShape)
            .border(1.dp, CarbonBorder, CircleShape)
        ) {
          Icon(Icons.Default.Close, contentDescription = "Abort", tint = CarbonTextSecondary, modifier = Modifier.size(18.dp))
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // MASTER 60-SECOND DIGITAL COUNTDOWN CLOCK
      Card(
        colors = CardDefaults.cardColors(containerColor = CarbonDark),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, if (timeLeftSeconds <= 15f) AlertRed else AlertRedGlow),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Timer, contentDescription = "Timer", tint = AlertRed, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "DETONATION COUNTDOWN",
              color = AlertRed,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
          }

          Spacer(modifier = Modifier.height(6.dp))

          // Milliseconds format
          val sec = timeLeftSeconds.toInt()
          val millis = ((timeLeftSeconds - sec) * 100).toInt()
          Text(
            text = "00:${"%02d".format(sec)}:${"%02d".format(millis)}",
            color = if (timeLeftSeconds <= 15f) AlertRed else AlertRedGlow,
            fontSize = 42.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 2.sp
          )

          // Linear countdown visualizer
          LinearProgressIndicator(
            progress = { (timeLeftSeconds / 60f).coerceIn(0f, 1f) },
            modifier = Modifier
              .fillMaxWidth()
              .height(8.dp)
              .clip(RoundedCornerShape(4.dp)),
            color = if (timeLeftSeconds <= 15f) AlertRed else ThreatOrange,
            trackColor = CarbonBlack
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // HOSTILE INTERFERENCE DRONE WARNING
      AnimatedVisibility(visible = breachDroneActive) {
        Card(
          colors = CardDefaults.cardColors(containerColor = AlertRed.copy(alpha = 0.2f)),
          shape = RoundedCornerShape(12.dp),
          border = androidx.compose.foundation.BorderStroke(1.5.dp, AlertRed),
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
              Icon(Icons.Default.Warning, contentDescription = "Drone Breach", tint = AlertRed)
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = "HOSTILE BREACH DRONE DETECTED!",
                  color = AlertRed,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Black
                )
                Text(
                  text = "Interference accelerating timer! Fire EMP Counter-Pulse!",
                  color = CarbonTextPrimary,
                  fontSize = 10.sp
                )
              }
            }

            Button(
              onClick = {
                breachDroneActive = false
                breachTimer = 18f
                soundManager.playSound("explosion", 0.7f)
                soundManager.vibrate(100)
              },
              colors = ButtonDefaults.buttonColors(containerColor = NitroBlue),
              shape = RoundedCornerShape(10.dp)
            ) {
              Icon(Icons.Default.ElectricBolt, contentDescription = "EMP", tint = CarbonBlack, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("EMP BLAST", color = CarbonBlack, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }

      // STAGES PROGRESS INDICATOR
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        StageBadge(stageNum = 1, title = "CIPHER", isCompleted = currentStage > 1, isActive = currentStage == 1)
        StageBadge(stageNum = 2, title = "PRESSURE", isCompleted = currentStage > 2, isActive = currentStage == 2)
        StageBadge(stageNum = 3, title = "WIRES", isCompleted = isMissionSuccess, isActive = currentStage == 3)
      }

      Spacer(modifier = Modifier.height(14.dp))

      // INTERACTIVE DEFUSAL WORKBENCH BASED ON CURRENT STAGE
      when (currentStage) {
        1 -> {
          // STAGE 1: Frequency Harmonizer
          Card(
            colors = CardDefaults.cardColors(containerColor = CarbonCard),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, NitroBlue),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lock, contentDescription = "Stage 1", tint = NitroBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                  Text("STAGE 1: FREQUENCY HARMONIZER", color = NitroBlue, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                  Text("Tap LOCK when oscillating frequency hits the GREEN TARGET ZONE (40%-60%).", color = CarbonTextSecondary, fontSize = 11.sp)
                }
              }

              Spacer(modifier = Modifier.height(16.dp))

              // Dial Channel 1
              Text("CHANNEL 1 (ALPHA BAND): ${(syncFreq1 * 100).toInt()} MHz", color = CarbonTextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
              Spacer(modifier = Modifier.height(4.dp))
              LinearProgressIndicator(
                progress = { syncFreq1 },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(14.dp)
                  .clip(RoundedCornerShape(6.dp)),
                color = if (syncFreq1 in 0.40f..0.60f) RadarGreen else AlertRed,
                trackColor = CarbonDark
              )
              Spacer(modifier = Modifier.height(6.dp))
              Button(
                onClick = {
                  if (syncFreq1 in 0.38f..0.62f) {
                    isFreq1Locked = true
                    soundManager.playWireCut()
                  } else {
                    timeLeftSeconds = (timeLeftSeconds - 4f).coerceAtLeast(0f)
                    soundManager.playAlert()
                  }
                },
                enabled = !isFreq1Locked,
                colors = ButtonDefaults.buttonColors(
                  containerColor = if (isFreq1Locked) RadarGreen else NitroBlue
                ),
                modifier = Modifier.fillMaxWidth()
              ) {
                Text(if (isFreq1Locked) "ALPHA LOCKED [SECURE]" else "LOCK ALPHA (TAP IN GREEN)")
              }

              Spacer(modifier = Modifier.height(14.dp))

              // Dial Channel 2
              Text("CHANNEL 2 (BETA BAND): ${(syncFreq2 * 100).toInt()} MHz", color = CarbonTextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
              Spacer(modifier = Modifier.height(4.dp))
              LinearProgressIndicator(
                progress = { syncFreq2 },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(14.dp)
                  .clip(RoundedCornerShape(6.dp)),
                color = if (syncFreq2 in 0.40f..0.60f) RadarGreen else AlertRed,
                trackColor = CarbonDark
              )
              Spacer(modifier = Modifier.height(6.dp))
              Button(
                onClick = {
                  if (syncFreq2 in 0.38f..0.62f) {
                    isFreq2Locked = true
                    soundManager.playWireCut()
                  } else {
                    timeLeftSeconds = (timeLeftSeconds - 4f).coerceAtLeast(0f)
                    soundManager.playAlert()
                  }
                },
                enabled = !isFreq2Locked,
                colors = ButtonDefaults.buttonColors(
                  containerColor = if (isFreq2Locked) RadarGreen else NitroBlue
                ),
                modifier = Modifier.fillMaxWidth()
              ) {
                Text(if (isFreq2Locked) "BETA LOCKED [SECURE]" else "LOCK BETA (TAP IN GREEN)")
              }
            }
          }
        }

        2 -> {
          // STAGE 2: Coolant & Pressure Stabilization
          Card(
            colors = CardDefaults.cardColors(containerColor = CarbonCard),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, DesertGold),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Thermostat, contentDescription = "Stage 2", tint = DesertGold)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                  Text("STAGE 2: COOLANT PRESSURE CHAMBERS", color = DesertGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                  Text("Tap valves to purge pressure below 40 PSI before thermal explosion.", color = CarbonTextSecondary, fontSize = 11.sp)
                }
              }

              Spacer(modifier = Modifier.height(16.dp))

              // Chamber A
              PressureValveRow(
                label = "VALVE ALPHA",
                pressure = pressureA,
                onPurge = {
                  pressureA = (pressureA - 28f).coerceAtLeast(18f)
                  soundManager.playWireCut()
                }
              )

              Spacer(modifier = Modifier.height(12.dp))

              // Chamber B
              PressureValveRow(
                label = "VALVE BETA",
                pressure = pressureB,
                onPurge = {
                  pressureB = (pressureB - 25f).coerceAtLeast(18f)
                  soundManager.playWireCut()
                }
              )

              Spacer(modifier = Modifier.height(12.dp))

              // Chamber C
              PressureValveRow(
                label = "VALVE GAMMA",
                pressure = pressureC,
                onPurge = {
                  pressureC = (pressureC - 30f).coerceAtLeast(18f)
                  soundManager.playWireCut()
                }
              )
            }
          }
        }

        3 -> {
          // STAGE 3: Final Wire Cutting Matrix
          Card(
            colors = CardDefaults.cardColors(containerColor = CarbonCard),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(2.dp, RadarGreen),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ContentCut, contentDescription = "Stage 3", tint = RadarGreen)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                  Text("STAGE 3: WIRE CUTTING MATRIX", color = RadarGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                  Text("Follow tactical schematic protocol to sever ignition leads.", color = CarbonTextSecondary, fontSize = 11.sp)
                }
              }

              Spacer(modifier = Modifier.height(12.dp))

              // Tactical Classified Intel Box
              Surface(
                color = CarbonDark,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder)
              ) {
                Column(modifier = Modifier.padding(12.dp)) {
                  Text(
                    text = "CLASSIFIED DEFUSAL DIRECTIVE:",
                    color = DesertGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                  )
                  Text(
                    text = "1. Cut GREEN wire (Capacitor shunt) FIRST.\n2. Cut YELLOW wire (Ignition lead) SECOND.\n3. NEVER cut RED (Direct Detonator Trigger = INSTANT EXPLOSION!).\n4. DO NOT cut BLUE.",
                    color = CarbonTextPrimary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                  )
                }
              }

              Spacer(modifier = Modifier.height(16.dp))

              // 4 Interactive Wires
              wires.forEach { wire ->
                WireItemRow(
                  wire = wire,
                  onCut = {
                    if (!wire.isCut) {
                      wire.isCut = true
                      soundManager.playWireCut()

                      if (wire.isDetonator) {
                        // Instant explosion!
                        isGameOver = true
                        failureReason = "CRITICAL DETONATION: You severed the live Red Detonator trigger!"
                        soundManager.playExplosion()
                      } else if (!wire.isCorrectToCut) {
                        // Penalty on false wire
                        timeLeftSeconds = (timeLeftSeconds - 12f).coerceAtLeast(0f)
                        soundManager.playAlert()
                      } else {
                        wiresCutCount++
                        if (wiresCutCount >= 2) {
                          // Defusal complete!
                          isMissionSuccess = true
                          soundManager.playSuccess()

                          val stars = when {
                            timeLeftSeconds > 25f -> 3
                            timeLeftSeconds > 10f -> 2
                            else -> 1
                          }
                          val score = (15000 + (timeLeftSeconds * 120).toInt()).coerceAtLeast(5000)
                          val credits = stars * 2000 + (score / 5)

                          onMissionFinished(
                            MissionResult(
                              missionId = MissionId.MISSION_3,
                              isSuccess = true,
                              timeElapsedSeconds = (60f - timeLeftSeconds),
                              stars = stars,
                              score = score,
                              creditsEarned = credits,
                              debriefNotes = listOf(
                                "Omega warhead safely disarmed with ${"%.1f".format(timeLeftSeconds)}s on the clock.",
                                "Enemy bunker facility neutralized.",
                                "Campaign Completed: Desert Storm Legacy Restored!"
                              )
                            )
                          )
                        }
                      }
                    }
                  }
                )
                Spacer(modifier = Modifier.height(10.dp))
              }
            }
          }
        }
      }
    }

    // Game Over Overlay
    if (isGameOver) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color.Black.copy(alpha = 0.92f)),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.padding(24.dp)
        ) {
          Icon(Icons.Default.Warning, contentDescription = "Detonated", tint = AlertRed, modifier = Modifier.size(60.dp))
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "WARHEAD DETONATED",
            color = AlertRed,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = failureReason,
            color = CarbonTextSecondary,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
          )
          Spacer(modifier = Modifier.height(24.dp))
          Button(
            onClick = {
              timeLeftSeconds = 60.00f
              currentStage = 1
              isFreq1Locked = false
              isFreq2Locked = false
              pressureA = 78f
              pressureB = 65f
              pressureC = 85f
              isPressureStabilized = false
              wires.forEach { it.isCut = false }
              wiresCutCount = 0
              breachDroneActive = false
              breachTimer = 14f
              isGameOver = false
            },
            colors = ButtonDefaults.buttonColors(containerColor = DesertGold)
          ) {
            Text("RETRY DEFUSAL (60s)", color = CarbonBlack, fontWeight = FontWeight.Bold)
          }
          Spacer(modifier = Modifier.height(10.dp))
          Button(
            onClick = onExit,
            colors = ButtonDefaults.buttonColors(containerColor = CarbonCard)
          ) {
            Text("ABORT TO MENU", color = CarbonTextPrimary)
          }
        }
      }
    }
  }
}

@Composable
fun StageBadge(stageNum: Int, title: String, isCompleted: Boolean, isActive: Boolean) {
  val borderColor = if (isCompleted) RadarGreen else if (isActive) DesertGold else CarbonBorder
  val textColor = if (isCompleted) RadarGreen else if (isActive) DesertGold else CarbonTextMuted

  Surface(
    color = CarbonCard,
    shape = RoundedCornerShape(8.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      if (isCompleted) {
        Icon(Icons.Default.Check, contentDescription = "Done", tint = RadarGreen, modifier = Modifier.size(14.dp))
      } else {
        Text("$stageNum.", color = textColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
      }
      Spacer(modifier = Modifier.width(4.dp))
      Text(title, color = textColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
  }
}

@Composable
fun PressureValveRow(
  label: String,
  pressure: Float,
  onPurge: () -> Unit
) {
  val isSafe = pressure in 15f..40f
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(label, color = CarbonTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Text(
          "${pressure.toInt()} PSI ${if (isSafe) "[SAFE]" else "[CRITICAL]"}",
          color = if (isSafe) RadarGreen else AlertRed,
          fontSize = 11.sp,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold
        )
      }
      Spacer(modifier = Modifier.height(4.dp))
      LinearProgressIndicator(
        progress = { (pressure / 100f).coerceIn(0f, 1f) },
        modifier = Modifier
          .fillMaxWidth()
          .height(10.dp)
          .clip(RoundedCornerShape(5.dp)),
        color = if (isSafe) RadarGreen else AlertRed,
        trackColor = CarbonDark
      )
    }

    Spacer(modifier = Modifier.width(12.dp))

    Button(
      onClick = onPurge,
      colors = ButtonDefaults.buttonColors(containerColor = if (isSafe) CarbonDark else DesertGold),
      shape = RoundedCornerShape(8.dp)
    ) {
      Text(
        text = "VENT",
        color = if (isSafe) CarbonTextMuted else CarbonBlack,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp
      )
    }
  }
}

@Composable
fun WireItemRow(
  wire: DefusalWire,
  onCut: () -> Unit
) {
  Surface(
    color = CarbonDark,
    shape = RoundedCornerShape(10.dp),
    border = androidx.compose.foundation.BorderStroke(
      1.5.dp,
      if (wire.isCut) CarbonBorder else wire.color
    ),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 10.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        // Wire color conduit indicator
        Box(
          modifier = Modifier
            .size(18.dp)
            .background(if (wire.isCut) CarbonBorder else wire.color, CircleShape)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          Text(
            text = wire.name,
            color = if (wire.isCut) CarbonTextMuted else CarbonTextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
          Text(
            text = if (wire.isCut) "STATUS: SEVERED" else "STATUS: LIVE VOLTAGE",
            color = if (wire.isCut) RadarGreen else AlertRed,
            fontSize = 10.sp
          )
        }
      }

      Button(
        onClick = onCut,
        enabled = !wire.isCut,
        colors = ButtonDefaults.buttonColors(
          containerColor = if (wire.isCut) CarbonSurface else wire.color
        ),
        shape = RoundedCornerShape(8.dp)
      ) {
        Icon(Icons.Default.ContentCut, contentDescription = "Cut Wire", tint = CarbonBlack, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = if (wire.isCut) "CUT" else "SNIP",
          color = CarbonBlack,
          fontSize = 11.sp,
          fontWeight = FontWeight.Black
        )
      }
    }
  }
}
