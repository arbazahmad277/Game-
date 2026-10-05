package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.GameSoundManager
import com.example.model.MissionId
import com.example.model.MissionResult
import com.example.model.VehicleConfig
import com.example.ui.theme.AlertRed
import com.example.ui.theme.CarbonBlack
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonCard
import com.example.ui.theme.CarbonDark
import com.example.ui.theme.CarbonTextMuted
import com.example.ui.theme.CarbonTextPrimary
import com.example.ui.theme.CarbonTextSecondary
import com.example.ui.theme.DesertGold
import com.example.ui.theme.DesertGoldDark
import com.example.ui.theme.DesertGoldLight
import com.example.ui.theme.NitroBlue
import com.example.ui.theme.NitroBlueGlow
import com.example.ui.theme.RadarGreen
import com.example.ui.theme.SandDune
import com.example.ui.theme.ThreatOrange
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// 3D Road Segment & Enemy Convoy Entities
data class EnemyVehicle(
  val id: Int,
  var z: Float, // Distance along the road ahead (0..3000m)
  var lane: Float, // -1.0 (left) to 1.0 (right)
  var speed: Float, // km/h
  var maxHealth: Float,
  var health: Float,
  val type: String, // "SCOUT", "TRUCK", "BOSS_CARRIER"
  var isDestroyed: Boolean = false,
  var explosionTime: Float = 0f
)

data class RoadHazard(
  var z: Float,
  var lane: Float,
  val type: String // "OIL", "MINE", "BARREL"
)

data class ParticleSpark(
  var x: Float,
  var y: Float,
  var vx: Float,
  var vy: Float,
  var color: Color,
  var life: Float // 1.0 down to 0
)

@Composable
fun Mission2ChaseScreen(
  selectedVehicle: VehicleConfig,
  engineLevel: Int,
  nitroLevel: Int,
  armorLevel: Int,
  weaponsLevel: Int,
  soundManager: GameSoundManager,
  onMissionFinished: (MissionResult) -> Unit,
  onExit: () -> Unit
) {
  // Stats adjusted by garage upgrades
  val maxVehicleSpeed = selectedVehicle.baseSpeed + (engineLevel * 12f)
  val maxNitroCapacity = selectedVehicle.baseNitro + (nitroLevel * 15f)
  val maxArmor = selectedVehicle.baseArmor + (armorLevel * 20f)
  val weaponPower = selectedVehicle.baseFirepower + (weaponsLevel * 15f)

  // Simulation state
  var playerLane by remember { mutableFloatStateOf(0f) } // -1f (left) to +1f (right)
  var currentSpeedKmH by remember { mutableFloatStateOf(80f) }
  var nitroGauge by remember { mutableFloatStateOf(maxNitroCapacity) }
  var isNitroActive by remember { mutableStateOf(false) }
  var playerHealth by remember { mutableFloatStateOf(maxArmor) }
  var distanceCoveredMeters by remember { mutableFloatStateOf(0f) }
  val totalChaseDistance = 2600f

  var isFiringCannons by remember { mutableStateOf(false) }
  var cannonCooldown by remember { mutableFloatStateOf(0f) }
  var screenShakeAmount by remember { mutableFloatStateOf(0f) }
  var isDataChipDropped by remember { mutableStateOf(false) }
  var dataChipZ by remember { mutableFloatStateOf(0f) }
  var dataChipLane by remember { mutableFloatStateOf(0f) }
  var isDataChipRecovered by remember { mutableStateOf(false) }

  var missionTimeSeconds by remember { mutableFloatStateOf(0f) }
  var isGameOver by remember { mutableStateOf(false) }
  var isMissionSuccess by remember { mutableStateOf(false) }
  var enemiesKilledCount by remember { mutableIntStateOf(0) }

  // Enemy convoy fleet
  val enemies = remember {
    mutableStateListOf(
      EnemyVehicle(1, z = 300f, lane = -0.5f, speed = 120f, maxHealth = 120f, health = 120f, type = "SCOUT"),
      EnemyVehicle(2, z = 650f, lane = 0.5f, speed = 125f, maxHealth = 130f, health = 130f, type = "SCOUT"),
      EnemyVehicle(3, z = 1100f, lane = -0.3f, speed = 110f, maxHealth = 240f, health = 240f, type = "TRUCK"),
      EnemyVehicle(4, z = 1600f, lane = 0.4f, speed = 115f, maxHealth = 260f, health = 260f, type = "TRUCK"),
      EnemyVehicle(5, z = 2200f, lane = 0.0f, speed = 135f, maxHealth = 550f, health = 550f, type = "BOSS_CARRIER")
    )
  }

  // Road hazards
  val hazards = remember {
    mutableStateListOf(
      RoadHazard(z = 450f, lane = 0.2f, type = "BARREL"),
      RoadHazard(z = 850f, lane = -0.4f, type = "OIL"),
      RoadHazard(z = 1350f, lane = 0.1f, type = "MINE"),
      RoadHazard(z = 1850f, lane = -0.5f, type = "BARREL")
    )
  }

  val particles = remember { mutableStateListOf<ParticleSpark>() }

  // 60FPS Game loop
  var lastFrameNanos by remember { mutableLongStateOf(0L) }
  LaunchedEffect(isGameOver, isMissionSuccess) {
    if (isGameOver || isMissionSuccess) return@LaunchedEffect
    lastFrameNanos = 0L

    while (true) {
      withFrameNanos { now ->
        if (lastFrameNanos != 0L) {
          val dt = ((now - lastFrameNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
          missionTimeSeconds += dt

          // Screen shake decay
          if (screenShakeAmount > 0f) {
            screenShakeAmount = (screenShakeAmount - 8f * dt).coerceAtLeast(0f)
          }

          // Nitro acceleration & consumption
          val targetTopSpeed = if (isNitroActive) maxVehicleSpeed * 1.35f else maxVehicleSpeed
          if (isNitroActive && nitroGauge > 0f) {
            nitroGauge = (nitroGauge - 35f * dt).coerceAtLeast(0f)
            currentSpeedKmH = (currentSpeedKmH + 160f * dt).coerceAtMost(targetTopSpeed)
            screenShakeAmount = 3.5f
            if (nitroGauge <= 0f) {
              isNitroActive = false
            }
          } else {
            // Auto refill nitro slowly
            nitroGauge = (nitroGauge + 8f * dt).coerceAtMost(maxNitroCapacity)
            val normalTarget = maxVehicleSpeed * 0.90f
            if (currentSpeedKmH < normalTarget) {
              currentSpeedKmH = (currentSpeedKmH + 60f * dt).coerceAtMost(normalTarget)
            } else if (currentSpeedKmH > normalTarget) {
              currentSpeedKmH = (currentSpeedKmH - 45f * dt).coerceAtLeast(normalTarget)
            }
          }

          // Advance distance
          val metersPerSec = (currentSpeedKmH * 1000f / 3600f)
          distanceCoveredMeters += metersPerSec * dt

          // Fire cannons cooldown
          if (cannonCooldown > 0f) {
            cannonCooldown = (cannonCooldown - dt).coerceAtLeast(0f)
          }

          if (isFiringCannons && cannonCooldown <= 0f) {
            cannonCooldown = 0.12f
            soundManager.playGunshot()

            // Find closest enemy vehicle in front lane
            val target = enemies.firstOrNull {
              !it.isDestroyed && it.z > distanceCoveredMeters && (it.z - distanceCoveredMeters) < 180f &&
                abs(it.lane - playerLane) < 0.45f
            }
            if (target != null) {
              val damage = weaponPower * 0.4f
              target.health = (target.health - damage).coerceAtLeast(0f)

              // Spark particles
              for (p in 0..4) {
                particles.add(
                  ParticleSpark(
                    x = target.lane,
                    y = target.z - distanceCoveredMeters,
                    vx = (Random.nextFloat() - 0.5f) * 2f,
                    vy = Random.nextFloat() * -3f,
                    color = DesertGoldLight,
                    life = 0.5f
                  )
                )
              }

              if (target.health <= 0f && !target.isDestroyed) {
                target.isDestroyed = true
                enemiesKilledCount++
                screenShakeAmount = 8f
                soundManager.playExplosion()

                if (target.type == "BOSS_CARRIER") {
                  // Drop the Data Chip
                  isDataChipDropped = true
                  dataChipZ = target.z + 10f
                  dataChipLane = target.lane
                }
              }
            }
          }

          // Update enemies
          for (e in enemies) {
            if (!e.isDestroyed) {
              val eSpeedMps = (e.speed * 1000f / 3600f)
              e.z += eSpeedMps * dt

              // Slight swerving behavior
              e.lane = (e.lane + sin(missionTimeSeconds * 2f + e.id) * 0.15f * dt).coerceIn(-0.8f, 0.8f)

              // Collision / Ramming with player
              val relZ = e.z - distanceCoveredMeters
              if (abs(relZ) < 8f && abs(e.lane - playerLane) < 0.35f) {
                // Ramming event
                screenShakeAmount = 12f
                soundManager.playExplosion()
                if (isNitroActive) {
                  // Player ram deals massive damage to enemy
                  e.health = (e.health - 120f).coerceAtLeast(0f)
                  playerHealth = (playerHealth - 15f).coerceAtLeast(0f)
                } else {
                  playerHealth = (playerHealth - 25f).coerceAtLeast(0f)
                  currentSpeedKmH = (currentSpeedKmH * 0.7f).coerceAtLeast(50f)
                }

                if (e.health <= 0f && !e.isDestroyed) {
                  e.isDestroyed = true
                  enemiesKilledCount++
                  if (e.type == "BOSS_CARRIER") {
                    isDataChipDropped = true
                    dataChipZ = e.z + 10f
                    dataChipLane = e.lane
                  }
                }

                if (playerHealth <= 0f) {
                  isGameOver = true
                  soundManager.playSound("explosion")
                }
              }
            } else {
              e.explosionTime += dt
            }
          }

          // Check hazards
          for (h in hazards) {
            val relZ = h.z - distanceCoveredMeters
            if (abs(relZ) < 6f && abs(h.lane - playerLane) < 0.30f) {
              when (h.type) {
                "OIL" -> {
                  // Spin car slightly
                  playerLane = (playerLane + (if (Random.nextBoolean()) 0.4f else -0.4f)).coerceIn(-0.9f, 0.9f)
                  screenShakeAmount = 6f
                  soundManager.vibrate(50)
                }
                "MINE", "BARREL" -> {
                  playerHealth = (playerHealth - 30f).coerceAtLeast(0f)
                  screenShakeAmount = 14f
                  soundManager.playExplosion()
                  if (playerHealth <= 0f) {
                    isGameOver = true
                  }
                }
              }
              // Move hazard far behind
              h.z = -999f
            }
          }

          // Check Data Chip recovery
          if (isDataChipDropped && !isDataChipRecovered) {
            val chipRelZ = dataChipZ - distanceCoveredMeters
            if (chipRelZ < 8f && chipRelZ > -10f && abs(dataChipLane - playerLane) < 0.4f) {
              isDataChipRecovered = true
              isMissionSuccess = true
              soundManager.playSuccess()

              val stars = when {
                playerHealth > (maxArmor * 0.65f) && missionTimeSeconds < 55f -> 3
                playerHealth > (maxArmor * 0.35f) -> 2
                else -> 1
              }
              val score = (12000 - (missionTimeSeconds * 50).toInt() + (playerHealth * 30).toInt() + (enemiesKilledCount * 1500)).coerceAtLeast(3000)
              val credits = stars * 1200 + (score / 8)

              onMissionFinished(
                MissionResult(
                  missionId = MissionId.MISSION_2,
                  isSuccess = true,
                  timeElapsedSeconds = missionTimeSeconds,
                  stars = stars,
                  score = score,
                  creditsEarned = credits,
                  debriefNotes = listOf(
                    "Colossus convoy neutralized across Sector 9 dunes.",
                    "Tactical Data Chip securely retrieved during high-speed interception.",
                    "Decrypted telemetry reveals Armed Dirty Bomb countdown at Enemy HQ."
                  )
                )
              )
            }
          }

          // Update particles
          val it = particles.iterator()
          while (it.hasNext()) {
            val p = it.next()
            p.life -= dt
            p.x += p.vx * dt
            p.y += p.vy * dt
            if (p.life <= 0f) {
              it.remove()
            }
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
      .testTag("mission_2_chase_container")
  ) {
    // 3D Perspective Racing & Combat Canvas
    Canvas(
      modifier = Modifier
        .fillMaxSize()
        .pointerInput(Unit) {
          detectDragGestures { change, dragAmount ->
            change.consume()
            // Horizontal drag steers vehicle lane
            val laneDelta = (dragAmount.x / (size.width * 0.35f))
            playerLane = (playerLane + laneDelta).coerceIn(-0.95f, 0.95f)
          }
        }
    ) {
      val canvasW = size.width
      val canvasH = size.height

      // Camera shake offset
      val shakeX = if (screenShakeAmount > 0f) (Random.nextFloat() - 0.5f) * screenShakeAmount * 6f else 0f
      val shakeY = if (screenShakeAmount > 0f) (Random.nextFloat() - 0.5f) * screenShakeAmount * 6f else 0f

      // 1. Sky & Parallax Desert Horizon
      val horizonY = canvasH * 0.44f + shakeY
      drawRect(
        brush = Brush.verticalGradient(
          colors = listOf(Color(0xFF1E102F), Color(0xFF6B21A8), Color(0xFFC2410C), Color(0xFFF97316)),
          startY = 0f,
          endY = horizonY
        ),
        topLeft = Offset(0f, 0f),
        size = Size(canvasW, horizonY)
      )

      // Distant desert canyon silhouettes
      val mountainPath = Path().apply {
        moveTo(0f, horizonY)
        val mSteps = 16
        for (i in 0..mSteps) {
          val mx = (i / mSteps.toFloat()) * canvasW
          val my = horizonY - 35f - (sin(i * 1.2f + distanceCoveredMeters * 0.001f) * 25f).toFloat()
          lineTo(mx, my)
        }
        lineTo(canvasW, horizonY)
        close()
      }
      drawPath(mountainPath, color = Color(0xFF3B1238))

      // 2. Desert Ground Plane
      drawRect(
        brush = Brush.verticalGradient(
          colors = listOf(Color(0xFF854D0E), Color(0xFF5B3004), Color(0xFF311A02)),
          startY = horizonY,
          endY = canvasH
        ),
        topLeft = Offset(0f, horizonY),
        size = Size(canvasW, canvasH - horizonY)
      )

      // 3. 3D Perspective Road Rendering
      val roadTopWidth = canvasW * 0.12f
      val roadBottomWidth = canvasW * 0.92f
      val roadCenterX = canvasW * 0.5f + shakeX

      // Project 3D road polygons
      val roadPath = Path().apply {
        moveTo(roadCenterX - roadTopWidth / 2f, horizonY)
        lineTo(roadCenterX + roadTopWidth / 2f, horizonY)
        lineTo(roadCenterX + roadBottomWidth / 2f, canvasH)
        lineTo(roadCenterX - roadBottomWidth / 2f, canvasH)
        close()
      }
      drawPath(roadPath, color = Color(0xFF1E232B))

      // Road edge shoulders (curbs)
      val leftCurb = Path().apply {
        moveTo(roadCenterX - roadTopWidth / 2f - 6f, horizonY)
        lineTo(roadCenterX - roadTopWidth / 2f, horizonY)
        lineTo(roadCenterX - roadBottomWidth / 2f, canvasH)
        lineTo(roadCenterX - roadBottomWidth / 2f - 30f, canvasH)
        close()
      }
      drawPath(leftCurb, color = DesertGoldDark)

      val rightCurb = Path().apply {
        moveTo(roadCenterX + roadTopWidth / 2f, horizonY)
        lineTo(roadCenterX + roadTopWidth / 2f + 6f, horizonY)
        lineTo(roadCenterX + roadBottomWidth / 2f + 30f, canvasH)
        lineTo(roadCenterX + roadBottomWidth / 2f, canvasH)
        close()
      }
      drawPath(rightCurb, color = DesertGoldDark)

      // Animated 3D Road Center Dashes (Perspective projection)
      val dashCount = 14
      val scrollOffset = (distanceCoveredMeters % 30f) / 30f
      for (i in 0 until dashCount) {
        val depth = ((i + scrollOffset) / dashCount.toFloat()).coerceIn(0.01f, 1f)
        // Perspective mapping: depth^2 gives true perspective spacing
        val perspY = horizonY + (canvasH - horizonY) * (depth * depth)
        val nextDepth = ((i + 0.5f + scrollOffset) / dashCount.toFloat()).coerceIn(0.01f, 1f)
        val nextPerspY = horizonY + (canvasH - horizonY) * (nextDepth * nextDepth)
        val currentRoadW = roadTopWidth + (roadBottomWidth - roadTopWidth) * (depth * depth)
        val dashW = (currentRoadW * 0.035f).coerceAtLeast(2f)

        drawRect(
          color = Color(0xEED97706),
          topLeft = Offset(roadCenterX - dashW / 2f, perspY),
          size = Size(dashW, (nextPerspY - perspY).coerceAtLeast(3f))
        )
      }

      // 4. Render Hazards (Barrels, Mines, Oil)
      for (h in hazards) {
        val relZ = h.z - distanceCoveredMeters
        if (relZ in 5f..280f) {
          val depth = (1f - (relZ / 280f)).coerceIn(0f, 1f)
          val perspY = horizonY + (canvasH - horizonY) * (depth * depth)
          val currentRoadW = roadTopWidth + (roadBottomWidth - roadTopWidth) * (depth * depth)
          val hX = roadCenterX + (h.lane * currentRoadW * 0.42f)
          val scale = depth * 32f

          if (h.type == "OIL") {
            drawOval(
              color = Color(0xCC050505),
              topLeft = Offset(hX - scale * 1.5f, perspY),
              size = Size(scale * 3f, scale * 1.2f)
            )
          } else {
            drawRect(
              color = if (h.type == "MINE") AlertRed else ThreatOrange,
              topLeft = Offset(hX - scale / 2f, perspY - scale),
              size = Size(scale, scale)
            )
          }
        }
      }

      // 5. Render Enemy Vehicles in 3D Perspective (Sorted by distance)
      val sortedEnemies = enemies.filter { (it.z - distanceCoveredMeters) in 5f..320f }
        .sortedByDescending { it.z }

      for (e in sortedEnemies) {
        val relZ = e.z - distanceCoveredMeters
        val depth = (1f - (relZ / 320f)).coerceIn(0f, 1f)
        val perspY = horizonY + (canvasH - horizonY) * (depth * depth)
        val currentRoadW = roadTopWidth + (roadBottomWidth - roadTopWidth) * (depth * depth)
        val eX = roadCenterX + (e.lane * currentRoadW * 0.42f)

        val vScale = depth * 55f

        if (!e.isDestroyed) {
          // Enemy Health Bar
          val barW = vScale * 1.6f
          drawRect(
            color = Color(0x88000000),
            topLeft = Offset(eX - barW / 2f, perspY - vScale * 1.4f),
            size = Size(barW, 6f)
          )
          drawRect(
            color = if (e.type == "BOSS_CARRIER") DesertGold else AlertRed,
            topLeft = Offset(eX - barW / 2f, perspY - vScale * 1.4f),
            size = Size(barW * (e.health / e.maxHealth).coerceIn(0f, 1f), 6f)
          )

          // 3D Shaded Vehicle Silhouette & Model
          val bodyColor = when (e.type) {
            "BOSS_CARRIER" -> Color(0xFF451A03)
            "TRUCK" -> Color(0xFF334155)
            else -> Color(0xFF7F1D1D)
          }

          // Vehicle Chassis
          drawRoundRect(
            color = bodyColor,
            topLeft = Offset(eX - vScale * 0.7f, perspY - vScale),
            size = Size(vScale * 1.4f, vScale * 0.9f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
          )
          // Roof / Windshield
          drawRect(
            color = Color(0xAA00E5FF),
            topLeft = Offset(eX - vScale * 0.45f, perspY - vScale * 0.85f),
            size = Size(vScale * 0.9f, vScale * 0.35f)
          )
          // Tail lights
          drawCircle(color = AlertRed, radius = vScale * 0.12f, center = Offset(eX - vScale * 0.5f, perspY - vScale * 0.2f))
          drawCircle(color = AlertRed, radius = vScale * 0.12f, center = Offset(eX + vScale * 0.5f, perspY - vScale * 0.2f))
        } else {
          // Destruction explosion fireball
          drawCircle(
            color = ThreatOrange.copy(alpha = (1f - (e.explosionTime / 1.5f)).coerceIn(0f, 1f)),
            radius = vScale * 1.8f,
            center = Offset(eX, perspY - vScale / 2f)
          )
          drawCircle(
            color = DesertGoldLight.copy(alpha = (1f - (e.explosionTime / 1.5f)).coerceIn(0f, 1f)),
            radius = vScale * 1.0f,
            center = Offset(eX, perspY - vScale / 2f)
          )
        }
      }

      // 6. Data Chip (Glowing pickup if dropped)
      if (isDataChipDropped && !isDataChipRecovered) {
        val chipRelZ = dataChipZ - distanceCoveredMeters
        if (chipRelZ in 1f..300f) {
          val depth = (1f - (chipRelZ / 300f)).coerceIn(0f, 1f)
          val perspY = horizonY + (canvasH - horizonY) * (depth * depth)
          val currentRoadW = roadTopWidth + (roadBottomWidth - roadTopWidth) * (depth * depth)
          val cX = roadCenterX + (dataChipLane * currentRoadW * 0.42f)

          drawCircle(
            color = NitroBlueGlow,
            radius = depth * 22f,
            center = Offset(cX, perspY - depth * 15f)
          )
          drawCircle(
            color = Color.White,
            radius = depth * 10f,
            center = Offset(cX, perspY - depth * 15f)
          )
        }
      }

      // 7. Player Vehicle (In Foreground 3D perspective)
      val playerY = canvasH * 0.82f + shakeY
      val pRoadW = roadBottomWidth
      val playerScreenX = roadCenterX + (playerLane * pRoadW * 0.42f)
      val pSize = canvasW * 0.32f

      // Rear Nitro Boost Flames
      if (isNitroActive) {
        val flameLen = 45f + Random.nextFloat() * 30f
        // Left Exhaust Flame
        val leftExhaust = Path().apply {
          moveTo(playerScreenX - pSize * 0.28f, playerY + pSize * 0.38f)
          lineTo(playerScreenX - pSize * 0.22f, playerY + pSize * 0.38f)
          lineTo(playerScreenX - pSize * 0.25f, playerY + pSize * 0.38f + flameLen)
          close()
        }
        drawPath(leftExhaust, color = NitroBlueGlow)

        // Right Exhaust Flame
        val rightExhaust = Path().apply {
          moveTo(playerScreenX + pSize * 0.22f, playerY + pSize * 0.38f)
          lineTo(playerScreenX + pSize * 0.28f, playerY + pSize * 0.38f)
          lineTo(playerScreenX + pSize * 0.25f, playerY + pSize * 0.38f + flameLen)
          close()
        }
        drawPath(rightExhaust, color = NitroBlueGlow)

        // Speedlines on screen edges
        for (sl in 0..10) {
          val sy = horizonY + Random.nextFloat() * (canvasH - horizonY)
          val sx = if (Random.nextBoolean()) Random.nextFloat() * 60f else canvasW - Random.nextFloat() * 60f
          drawLine(
            color = Color(0x6600E5FF),
            start = Offset(sx, sy),
            end = Offset(sx, sy + 60f),
            strokeWidth = 2f
          )
        }
      }

      // Vehicle Chassis (Rugged armored off-road buggy)
      // Main Body
      drawRoundRect(
        color = Color(0xFF1E293B),
        topLeft = Offset(playerScreenX - pSize * 0.45f, playerY - pSize * 0.25f),
        size = Size(pSize * 0.9f, pSize * 0.65f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(14f, 14f)
      )
      // Armor Trim & Gold Racing Decals
      drawRoundRect(
        color = DesertGold,
        topLeft = Offset(playerScreenX - pSize * 0.35f, playerY - pSize * 0.18f),
        size = Size(pSize * 0.7f, pSize * 0.48f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f),
        style = Stroke(width = 3.5f)
      )

      // Rear Windshield & Roll Cage
      drawRect(
        color = Color(0x8838BDF8),
        topLeft = Offset(playerScreenX - pSize * 0.28f, playerY - pSize * 0.12f),
        size = Size(pSize * 0.56f, pSize * 0.24f)
      )

      // Top Mounted Twin Gatling Cannons
      drawRect(
        color = Color(0xFF0F172A),
        topLeft = Offset(playerScreenX - pSize * 0.20f, playerY - pSize * 0.35f),
        size = Size(pSize * 0.08f, pSize * 0.22f)
      )
      drawRect(
        color = Color(0xFF0F172A),
        topLeft = Offset(playerScreenX + pSize * 0.12f, playerY - pSize * 0.35f),
        size = Size(pSize * 0.08f, pSize * 0.22f)
      )

      // Muzzle flashes if firing
      if (isFiringCannons) {
        drawCircle(color = DesertGoldLight, radius = 12f, center = Offset(playerScreenX - pSize * 0.16f, playerY - pSize * 0.38f))
        drawCircle(color = DesertGoldLight, radius = 12f, center = Offset(playerScreenX + pSize * 0.16f, playerY - pSize * 0.38f))

        // Bullet tracers heading forward
        drawLine(
          color = DesertGoldLight,
          start = Offset(playerScreenX - pSize * 0.16f, playerY - pSize * 0.38f),
          end = Offset(playerScreenX - pSize * 0.16f, playerY - pSize * 1.5f),
          strokeWidth = 3f
        )
        drawLine(
          color = DesertGoldLight,
          start = Offset(playerScreenX + pSize * 0.16f, playerY - pSize * 0.38f),
          end = Offset(playerScreenX + pSize * 0.16f, playerY - pSize * 1.5f),
          strokeWidth = 3f
        )
      }

      // Massive Off-road Mud Tires
      val tireW = pSize * 0.16f
      val tireH = pSize * 0.32f
      // Rear Left
      drawRoundRect(
        color = Color(0xFF090D12),
        topLeft = Offset(playerScreenX - pSize * 0.54f, playerY + pSize * 0.08f),
        size = Size(tireW, tireH),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
      )
      // Rear Right
      drawRoundRect(
        color = Color(0xFF090D12),
        topLeft = Offset(playerScreenX + pSize * 0.38f, playerY + pSize * 0.08f),
        size = Size(tireW, tireH),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
      )
    }

    // Top HUD: Speed, Distance to Target, Armor, Convoy Tracker
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .statusBarsPadding()
        .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Speed & Nitro cluster
        Surface(
          color = CarbonCard.copy(alpha = 0.88f),
          shape = RoundedCornerShape(12.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.Speed, contentDescription = "Speed", tint = if (isNitroActive) NitroBlue else DesertGold)
            Spacer(modifier = Modifier.width(6.dp))
            Column {
              Text(
                text = "${currentSpeedKmH.toInt()} KM/H",
                color = if (isNitroActive) NitroBlue else CarbonTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
              )
              Text(
                text = if (isNitroActive) "NITRO ENGAGED" else "CRUISE SPEED",
                color = if (isNitroActive) NitroBlue else CarbonTextMuted,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }

        // Distance & Target indicator
        Surface(
          color = CarbonCard.copy(alpha = 0.88f),
          shape = RoundedCornerShape(12.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = if (isDataChipDropped) Icons.Default.Memory else Icons.Default.GpsFixed,
              contentDescription = "Target",
              tint = if (isDataChipDropped) NitroBlue else AlertRed
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
              Text(
                text = if (isDataChipDropped) "INTERCEPT CHIP!" else "BOSS CARRIER",
                color = if (isDataChipDropped) NitroBlue else DesertGold,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
              val distRemaining = (totalChaseDistance - distanceCoveredMeters).coerceAtLeast(0f)
              Text(
                text = "${distRemaining.toInt()}M AHEAD",
                color = CarbonTextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
              )
            }
          }
        }

        // Abort button
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

      Spacer(modifier = Modifier.height(8.dp))

      // Armor bar & Nitro gauge
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("ARMOR SHIELD", color = CarbonTextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text("${playerHealth.toInt()}/${maxArmor.toInt()}", color = RadarGreen, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
          }
          LinearProgressIndicator(
            progress = { (playerHealth / maxArmor).coerceIn(0f, 1f) },
            modifier = Modifier
              .fillMaxWidth()
              .height(6.dp)
              .clip(RoundedCornerShape(3.dp)),
            color = if (playerHealth < maxArmor * 0.3f) AlertRed else RadarGreen,
            trackColor = CarbonDark
          )
        }

        Column(modifier = Modifier.weight(1f)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("NITRO BOOST", color = CarbonTextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text("${(nitroGauge / maxNitroCapacity * 100).toInt()}%", color = NitroBlue, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
          }
          LinearProgressIndicator(
            progress = { (nitroGauge / maxNitroCapacity).coerceIn(0f, 1f) },
            modifier = Modifier
              .fillMaxWidth()
              .height(6.dp)
              .clip(RoundedCornerShape(3.dp)),
            color = NitroBlue,
            trackColor = CarbonDark
          )
        }
      }
    }

    // Bottom Controls: Steering Buttons & Nitro / Cannon Fire Triggers
    Row(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
        .navigationBarsPadding()
        .padding(horizontal = 16.dp, vertical = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.Bottom
    ) {
      // Tactile Steering Buttons (Left / Right)
      Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(
          onClick = {
            playerLane = (playerLane - 0.25f).coerceIn(-0.95f, 0.95f)
            soundManager.vibrate(20)
          },
          colors = ButtonDefaults.buttonColors(containerColor = CarbonCard),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .size(68.dp)
            .border(1.5.dp, CarbonBorder, RoundedCornerShape(14.dp))
        ) {
          Text("◄", fontSize = 26.sp, color = CarbonTextPrimary)
        }

        Button(
          onClick = {
            playerLane = (playerLane + 0.25f).coerceIn(-0.95f, 0.95f)
            soundManager.vibrate(20)
          },
          colors = ButtonDefaults.buttonColors(containerColor = CarbonCard),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .size(68.dp)
            .border(1.5.dp, CarbonBorder, RoundedCornerShape(14.dp))
        ) {
          Text("►", fontSize = 26.sp, color = CarbonTextPrimary)
        }
      }

      // Action Weapons: Twin Cannons & Nitro Boost
      Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        // Twin Cannons Button (Hold or tap to fire)
        Button(
          onClick = {
            isFiringCannons = !isFiringCannons
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = if (isFiringCannons) ThreatOrange else CarbonCard
          ),
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier
            .size(72.dp)
            .border(2.dp, if (isFiringCannons) ThreatOrange else DesertGold, RoundedCornerShape(16.dp))
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.RocketLaunch, contentDescription = "Cannons", tint = if (isFiringCannons) CarbonBlack else DesertGold)
            Text(
              text = if (isFiringCannons) "FIRING" else "FIRE",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = if (isFiringCannons) CarbonBlack else CarbonTextPrimary
            )
          }
        }

        // Nitro Boost Button
        Button(
          onClick = {
            if (nitroGauge > 10f) {
              isNitroActive = true
              soundManager.playNitroBoost()
            }
          },
          enabled = nitroGauge > 10f,
          colors = ButtonDefaults.buttonColors(
            containerColor = if (isNitroActive) NitroBlue else Color(0xFF0F2B3E)
          ),
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier
            .size(76.dp)
            .border(2.dp, if (isNitroActive) NitroBlueGlow else NitroBlue, RoundedCornerShape(16.dp))
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.ElectricBolt, contentDescription = "Nitro", tint = if (isNitroActive) CarbonBlack else NitroBlue)
            Text(
              text = "NITRO",
              fontSize = 11.sp,
              fontWeight = FontWeight.Black,
              color = if (isNitroActive) CarbonBlack else NitroBlue
            )
          }
        }
      }
    }

    // Game Over Overlay
    if (isGameOver) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color.Black.copy(alpha = 0.90f)),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.padding(24.dp)
        ) {
          Icon(Icons.Default.Warning, contentDescription = "Vehicle Destroyed", tint = AlertRed, modifier = Modifier.size(54.dp))
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "VEHICLE DESTROYED",
            color = AlertRed,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace
          )
          Text(
            text = "Your assault buggy took critical chassis damage during the high-speed chase.",
            color = CarbonTextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
          )
          Spacer(modifier = Modifier.height(24.dp))
          Button(
            onClick = {
              playerHealth = maxArmor
              currentSpeedKmH = 80f
              distanceCoveredMeters = 0f
              isDataChipDropped = false
              isDataChipRecovered = false
              isGameOver = false
            },
            colors = ButtonDefaults.buttonColors(containerColor = DesertGold)
          ) {
            Text("RETRY PURSUIT", color = CarbonBlack, fontWeight = FontWeight.Bold)
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
