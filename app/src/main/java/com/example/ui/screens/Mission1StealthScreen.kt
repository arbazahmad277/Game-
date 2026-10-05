package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VolumeUp
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
import androidx.compose.runtime.derivedStateOf
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
import com.example.ui.theme.AlertRed
import com.example.ui.theme.CarbonBlack
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonCard
import com.example.ui.theme.CarbonDark
import com.example.ui.theme.CarbonTextMuted
import com.example.ui.theme.CarbonTextPrimary
import com.example.ui.theme.CarbonTextSecondary
import com.example.ui.theme.DesertGold
import com.example.ui.theme.DesertGoldLight
import com.example.ui.theme.NitroBlue
import com.example.ui.theme.RadarGreen
import com.example.ui.theme.RadarGreenGlow
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

// Obstacle / Cover Box
data class CoverObstacle(
  val x: Float, // Normalized 0..1
  val y: Float, // Normalized 0..1
  val w: Float,
  val h: Float,
  val label: String = "CRATE"
)

// Rotating Radar Unit
data class RadarTower(
  val id: Int,
  val x: Float,
  val y: Float,
  val radius: Float = 0.28f,
  val sweepAngleSpread: Float = 55f,
  var currentAngle: Float = 0f,
  val rotationSpeed: Float = 45f, // deg per sec
  var disabledTimer: Float = 0f
)

// Patrolling Drone
data class PatrolDrone(
  val id: Int,
  var x: Float,
  var y: Float,
  val startX: Float,
  val startY: Float,
  val endX: Float,
  val endY: Float,
  val speed: Float = 0.12f,
  var movingToEnd: Boolean = true,
  var disabledTimer: Float = 0f,
  var angle: Float = 0f
)

@Composable
fun Mission1StealthScreen(
  soundManager: GameSoundManager,
  onMissionFinished: (MissionResult) -> Unit,
  onExit: () -> Unit
) {
  // Game state
  var playerX by remember { mutableFloatStateOf(0.12f) }
  var playerY by remember { mutableFloatStateOf(0.88f) }
  var playerHeading by remember { mutableFloatStateOf(0f) }
  var isCrouching by remember { mutableStateOf(false) }
  var playerHealth by remember { mutableFloatStateOf(100f) }
  var alertLevel by remember { mutableFloatStateOf(0f) } // 0..100
  var empCharges by remember { mutableIntStateOf(2) }
  var stunDarts by remember { mutableIntStateOf(3) }
  var isTerminalHacked by remember { mutableStateOf(false) }
  var isShowingHackDialog by remember { mutableStateOf(false) }
  var hackProgress by remember { mutableFloatStateOf(0f) }
  var hackTargetFrequency by remember { mutableFloatStateOf(0.65f) }
  var hackCurrentDial by remember { mutableFloatStateOf(0.5f) }
  var missionTimeSeconds by remember { mutableFloatStateOf(0f) }
  var isGameOver by remember { mutableStateOf(false) }
  var isMissionSuccess by remember { mutableStateOf(false) }
  var joystickOffset by remember { mutableStateOf(Offset.Zero) }

  // Radar towers
  val radars = remember {
    mutableStateListOf(
      RadarTower(1, x = 0.35f, y = 0.28f, radius = 0.25f, currentAngle = 0f, rotationSpeed = 38f),
      RadarTower(2, x = 0.70f, y = 0.42f, radius = 0.27f, currentAngle = 120f, rotationSpeed = -42f),
      RadarTower(3, x = 0.38f, y = 0.65f, radius = 0.22f, currentAngle = 240f, rotationSpeed = 48f)
    )
  }

  // Patrol drones
  val drones = remember {
    mutableStateListOf(
      PatrolDrone(1, x = 0.20f, y = 0.45f, startX = 0.15f, startY = 0.45f, endX = 0.85f, endY = 0.45f, speed = 0.09f),
      PatrolDrone(2, x = 0.55f, y = 0.18f, startX = 0.55f, startY = 0.15f, endX = 0.55f, endY = 0.78f, speed = 0.11f)
    )
  }

  // Cover crates & obstacles
  val obstacles = remember {
    listOf(
      CoverObstacle(0.25f, 0.78f, 0.12f, 0.08f, "CARGO-A"),
      CoverObstacle(0.55f, 0.72f, 0.14f, 0.09f, "GENERATOR"),
      CoverObstacle(0.18f, 0.32f, 0.10f, 0.14f, "MISSILE RACK"),
      CoverObstacle(0.72f, 0.20f, 0.14f, 0.10f, "HANGAR CRATE"),
      CoverObstacle(0.48f, 0.36f, 0.12f, 0.12f, "HANGAR PILLAR")
    )
  }

  // Interactive points
  val terminalPos = remember { Offset(0.82f, 0.16f) }
  val extractionDoorPos = remember { Offset(0.88f, 0.08f) }

  // Game loop ticker
  var lastFrameNanos by remember { mutableLongStateOf(0L) }
  LaunchedEffect(isGameOver, isMissionSuccess, isShowingHackDialog) {
    if (isGameOver || isMissionSuccess || isShowingHackDialog) return@LaunchedEffect
    lastFrameNanos = 0L
    while (true) {
      withFrameNanos { now ->
        if (lastFrameNanos != 0L) {
          val dt = ((now - lastFrameNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
          missionTimeSeconds += dt

          // Update radars
          for (r in radars) {
            if (r.disabledTimer > 0f) {
              r.disabledTimer = (r.disabledTimer - dt).coerceAtLeast(0f)
            } else {
              r.currentAngle = (r.currentAngle + r.rotationSpeed * dt) % 360f
              if (r.currentAngle < 0f) r.currentAngle += 360f
            }
          }

          // Update drones
          for (d in drones) {
            if (d.disabledTimer > 0f) {
              d.disabledTimer = (d.disabledTimer - dt).coerceAtLeast(0f)
            } else {
              val targetX = if (d.movingToEnd) d.endX else d.startX
              val targetY = if (d.movingToEnd) d.endY else d.startY
              val dx = targetX - d.x
              val dy = targetY - d.y
              val dist = hypot(dx, dy)
              if (dist < 0.02f) {
                d.movingToEnd = !d.movingToEnd
              } else {
                val step = (d.speed * dt).coerceAtMost(dist)
                d.x += (dx / dist) * step
                d.y += (dy / dist) * step
                d.angle = (atan2(dy, dx) * 180f / PI).toFloat()
              }
            }
          }

          // Handle player movement from joystick
          if (joystickOffset != Offset.Zero) {
            val moveSpeed = if (isCrouching) 0.12f else 0.28f
            val len = hypot(joystickOffset.x, joystickOffset.y)
            if (len > 0.05f) {
              val normX = (joystickOffset.x / len).coerceIn(-1f, 1f)
              val normY = (joystickOffset.y / len).coerceIn(-1f, 1f)
              playerHeading = (atan2(normY, normX) * 180f / PI).toFloat()

              val targetPx = (playerX + normX * moveSpeed * dt).coerceIn(0.05f, 0.95f)
              val targetPy = (playerY + normY * moveSpeed * dt).coerceIn(0.05f, 0.95f)

              // Check obstacle collisions
              var collides = false
              for (obs in obstacles) {
                if (targetPx >= obs.x && targetPx <= obs.x + obs.w &&
                  targetPy >= obs.y && targetPy <= obs.y + obs.h
                ) {
                  collides = true
                  break
                }
              }
              if (!collides) {
                playerX = targetPx
                playerY = targetPy
              }
            }
          }

          // Check detection logic
          var isDetectedThisFrame = false

          // 1. Radar beams check
          for (r in radars) {
            if (r.disabledTimer > 0f) continue
            val rDist = hypot(playerX - r.x, playerY - r.y)
            if (rDist <= r.radius) {
              val angleToPlayer = (atan2(playerY - r.y, playerX - r.x) * 180f / PI).toFloat().let {
                if (it < 0f) it + 360f else it
              }
              val angleDiff = kotlin.math.abs(angleToPlayer - r.currentAngle).let {
                if (it > 180f) 360f - it else it
              }
              if (angleDiff <= r.sweepAngleSpread / 2f) {
                // Check if player has obstacle blocking line of sight
                val hasCover = obstacles.any { obs ->
                  val boxCenterX = obs.x + obs.w / 2f
                  val boxCenterY = obs.y + obs.h / 2f
                  hypot(playerX - boxCenterX, playerY - boxCenterY) < 0.09f && isCrouching
                }
                if (!hasCover) {
                  isDetectedThisFrame = true
                }
              }
            }
          }

          // 2. Drone cone check
          for (d in drones) {
            if (d.disabledTimer > 0f) continue
            val dDist = hypot(playerX - d.x, playerY - d.y)
            if (dDist < 0.16f) {
              val angleToPlayer = (atan2(playerY - d.y, playerX - d.x) * 180f / PI).toFloat()
              val angleDiff = kotlin.math.abs(angleToPlayer - d.angle).let {
                if (it > 180f) 360f - it else it
              }
              if (angleDiff < 45f) {
                isDetectedThisFrame = true
              }
            }
          }

          // Alert dynamics
          if (isDetectedThisFrame) {
            val alertGrowth = if (isCrouching) 35f else 60f
            alertLevel = (alertLevel + alertGrowth * dt).coerceAtMost(100f)
            if (alertLevel > 80f) {
              soundManager.playAlert()
              playerHealth = (playerHealth - 18f * dt).coerceAtLeast(0f)
              if (playerHealth <= 0f) {
                isGameOver = true
                soundManager.playSound("explosion")
              }
            }
          } else {
            alertLevel = (alertLevel - 15f * dt).coerceAtLeast(0f)
          }

          // Terminal trigger check
          val distToTerminal = hypot(playerX - terminalPos.x, playerY - terminalPos.y)
          if (distToTerminal < 0.08f && !isTerminalHacked) {
            isShowingHackDialog = true
            soundManager.playSound("beep")
          }

          // Extraction door trigger check
          val distToExit = hypot(playerX - extractionDoorPos.x, playerY - extractionDoorPos.y)
          if (distToExit < 0.09f && isTerminalHacked) {
            isMissionSuccess = true
            soundManager.playSuccess()

            val stars = when {
              alertLevel < 20f && missionTimeSeconds < 65f -> 3
              alertLevel < 50f && missionTimeSeconds < 100f -> 2
              else -> 1
            }
            val score = (10000 - (missionTimeSeconds * 60).toInt() + (playerHealth * 40).toInt()).coerceAtLeast(2000)
            val credits = stars * 1000 + (score / 10)

            onMissionFinished(
              MissionResult(
                missionId = MissionId.MISSION_1,
                isSuccess = true,
                timeElapsedSeconds = missionTimeSeconds,
                stars = stars,
                score = score,
                creditsEarned = credits,
                debriefNotes = listOf(
                  "Hangar radar systems bypassed without global alarm trigger.",
                  "Security terminal cipher cracked; Convoy route extracted.",
                  "Data chip located on transport convoy heading into the Dune Badlands."
                )
              )
            )
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
      .testTag("mission_1_stealth_container")
  ) {
    // 2D Tactical Hangar Canvas
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
      val w = maxWidth.value
      val h = maxHeight.value

      Canvas(
        modifier = Modifier
          .fillMaxSize()
          .pointerInput(Unit) {
            // Tap on drone to stun if player has stun darts
            detectDragGestures(
              onDragStart = { offset ->
                val normX = offset.x / size.width
                val normY = offset.y / size.height
                // Check if tapped near a drone
                if (stunDarts > 0) {
                  for (d in drones) {
                    if (hypot(normX - d.x, normY - d.y) < 0.10f && d.disabledTimer <= 0f) {
                      d.disabledTimer = 7f
                      stunDarts--
                      soundManager.playWireCut()
                      break
                    }
                  }
                }
              },
              onDrag = { _, _ -> }
            )
          }
      ) {
        val canvasW = size.width
        val canvasH = size.height

        // 1. Tactical Hangar Floor Tiles & Runway
        drawRect(
          brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF0A1017), Color(0xFF101923), Color(0xFF090E14))
          )
        )

        // Hangar grid lines
        val stepX = canvasW / 12f
        val stepY = canvasH / 18f
        for (i in 0..12) {
          drawLine(
            color = Color(0x1538BDF8),
            start = Offset(i * stepX, 0f),
            end = Offset(i * stepX, canvasH),
            strokeWidth = 1f
          )
        }
        for (j in 0..18) {
          drawLine(
            color = Color(0x1538BDF8),
            start = Offset(0f, j * stepY),
            end = Offset(canvasW, j * stepY),
            strokeWidth = 1f
          )
        }

        // Hangar Runway Strip
        drawRect(
          color = Color(0x221E293B),
          topLeft = Offset(canvasW * 0.42f, 0f),
          size = Size(canvasW * 0.16f, canvasH)
        )
        for (dashY in 0..15) {
          drawRect(
            color = Color(0x44F59E0B),
            topLeft = Offset(canvasW * 0.495f, dashY * canvasH / 15f + 10f),
            size = Size(canvasW * 0.01f, 25f)
          )
        }

        // Fighter jet parked silhouette
        val jetPath = Path().apply {
          val jx = canvasW * 0.40f
          val jy = canvasH * 0.24f
          moveTo(jx, jy)
          lineTo(jx + canvasW * 0.20f, jy)
          lineTo(jx + canvasW * 0.15f, jy + canvasH * 0.08f)
          lineTo(jx + canvasW * 0.05f, jy + canvasH * 0.08f)
          close()
        }
        drawPath(jetPath, color = Color(0x35334155))

        // 2. Obstacles / Crates
        for (obs in obstacles) {
          val ox = obs.x * canvasW
          val oy = obs.y * canvasH
          val ow = obs.w * canvasW
          val oh = obs.h * canvasH

          drawRect(
            color = Color(0xFF1E2B3A),
            topLeft = Offset(ox, oy),
            size = Size(ow, oh)
          )
          drawRect(
            color = Color(0xFF334A60),
            topLeft = Offset(ox, oy),
            size = Size(ow, oh),
            style = Stroke(width = 2f)
          )
          // Diagonal hazard striping inside crate
          drawLine(
            color = Color(0x44F59E0B),
            start = Offset(ox, oy),
            end = Offset(ox + ow, oy + oh),
            strokeWidth = 2f
          )
        }

        // 3. Security Terminal & Extraction Door
        val termX = terminalPos.x * canvasW
        val termY = terminalPos.y * canvasH
        drawCircle(
          color = if (isTerminalHacked) RadarGreen else DesertGold,
          radius = 16f,
          center = Offset(termX, termY)
        )
        drawCircle(
          color = if (isTerminalHacked) RadarGreen.copy(alpha = 0.3f) else DesertGold.copy(alpha = 0.3f),
          radius = 28f,
          center = Offset(termX, termY),
          style = Stroke(width = 2f)
        )

        // Extraction Door
        val doorX = extractionDoorPos.x * canvasW
        val doorY = extractionDoorPos.y * canvasH
        drawRect(
          color = if (isTerminalHacked) RadarGreen else AlertRed,
          topLeft = Offset(doorX - 25f, doorY - 12f),
          size = Size(50f, 24f)
        )

        // 4. Radars & Sweeping Detection Cones
        for (r in radars) {
          val rx = r.x * canvasW
          val ry = r.y * canvasH
          val radiusPx = r.radius * canvasW

          // Radar Base
          drawCircle(
            color = if (r.disabledTimer > 0f) Color(0xFF475569) else Color(0xFF0F172A),
            radius = 14f,
            center = Offset(rx, ry)
          )
          drawCircle(
            color = if (r.disabledTimer > 0f) Color(0xFF64748B) else NitroBlue,
            radius = 14f,
            center = Offset(rx, ry),
            style = Stroke(width = 2f)
          )

          // Radar Range Ring
          drawCircle(
            color = if (r.disabledTimer > 0f) Color(0x1164748B) else Color(0x2200E5FF),
            radius = radiusPx,
            center = Offset(rx, ry),
            style = Stroke(width = 1.5f)
          )

          // Sweeping Cone
          if (r.disabledTimer <= 0f) {
            val coneColor = if (alertLevel > 50f) AlertRed.copy(alpha = 0.35f) else RadarGreen.copy(alpha = 0.22f)
            val startRad = (r.currentAngle - r.sweepAngleSpread / 2f) * PI / 180f
            val endRad = (r.currentAngle + r.sweepAngleSpread / 2f) * PI / 180f

            val conePath = Path().apply {
              moveTo(rx, ry)
              lineTo((rx + cos(startRad) * radiusPx).toFloat(), (ry + sin(startRad) * radiusPx).toFloat())
              // Draw arc approximation
              val steps = 12
              for (s in 1..steps) {
                val curRad = startRad + (endRad - startRad) * (s.toFloat() / steps)
                lineTo((rx + cos(curRad) * radiusPx).toFloat(), (ry + sin(curRad) * radiusPx).toFloat())
              }
              close()
            }
            drawPath(conePath, color = coneColor)

            // Radar beam sweep line
            val beamRad = r.currentAngle * PI / 180f
            drawLine(
              color = if (alertLevel > 50f) AlertRed else RadarGreenGlow,
              start = Offset(rx, ry),
              end = Offset((rx + cos(beamRad) * radiusPx).toFloat(), (ry + sin(beamRad) * radiusPx).toFloat()),
              strokeWidth = 2.5f
            )
          } else {
            // Disabled EMP sparks
            drawCircle(
              color = NitroBlue.copy(alpha = 0.4f),
              radius = 24f,
              center = Offset(rx, ry),
              style = Stroke(width = 1.5f)
            )
          }
        }

        // 5. Patrol Drones
        for (d in drones) {
          val dx = d.x * canvasW
          val dy = d.y * canvasH

          if (d.disabledTimer <= 0f) {
            // Drone search cone
            val dAngleRad = d.angle * PI / 180f
            val coneLen = 0.16f * canvasW
            val conePath = Path().apply {
              moveTo(dx, dy)
              val leftRad = dAngleRad - 0.4
              val rightRad = dAngleRad + 0.4
              lineTo((dx + cos(leftRad) * coneLen).toFloat(), (dy + sin(leftRad) * coneLen).toFloat())
              lineTo((dx + cos(rightRad) * coneLen).toFloat(), (dy + sin(rightRad) * coneLen).toFloat())
              close()
            }
            drawPath(conePath, color = Color(0x35EF4444))
          }

          // Drone body
          drawCircle(
            color = if (d.disabledTimer > 0f) Color(0xFF475569) else AlertRed,
            radius = 12f,
            center = Offset(dx, dy)
          )
          drawCircle(
            color = Color.White,
            radius = 12f,
            center = Offset(dx, dy),
            style = Stroke(width = 1.5f)
          )
        }

        // 6. Player Operative
        val px = playerX * canvasW
        val py = playerY * canvasH

        // Noise radius ring (smaller when crouching)
        val noiseRadius = if (isCrouching) 25f else 60f
        drawCircle(
          color = if (isCrouching) RadarGreen.copy(alpha = 0.15f) else DesertGold.copy(alpha = 0.25f),
          radius = noiseRadius,
          center = Offset(px, py),
          style = Stroke(width = 1.5f)
        )

        // Player icon / circle
        drawCircle(
          color = if (isCrouching) Color(0xFF0D9488) else DesertGold,
          radius = 14f,
          center = Offset(px, py)
        )
        // Player direction pointer
        val headRad = playerHeading * PI / 180f
        drawLine(
          color = Color.White,
          start = Offset(px, py),
          end = Offset((px + cos(headRad) * 22f).toFloat(), (py + sin(headRad) * 22f).toFloat()),
          strokeWidth = 3f
        )
      }
    }

    // Top HUD
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
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Radar,
              contentDescription = "Radar",
              tint = if (alertLevel > 50f) AlertRed else RadarGreen,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (alertLevel > 70f) "HOSTILE ALERT!" else if (alertLevel > 20f) "SUSPICIOUS" else "STEALTH SECURE",
              color = if (alertLevel > 70f) AlertRed else if (alertLevel > 20f) DesertGold else RadarGreen,
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
              fontFamily = FontFamily.Monospace
            )
          }

          // Alert meter
          LinearProgressIndicator(
            progress = { (alertLevel / 100f).coerceIn(0f, 1f) },
            modifier = Modifier
              .width(140.dp)
              .height(6.dp)
              .clip(RoundedCornerShape(3.dp)),
            color = if (alertLevel > 70f) AlertRed else if (alertLevel > 30f) DesertGold else RadarGreen,
            trackColor = CarbonBorder
          )
        }

        // Objective indicator
        Surface(
          color = CarbonCard.copy(alpha = 0.85f),
          shape = RoundedCornerShape(8.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = if (isTerminalHacked) Icons.Default.LockOpen else Icons.Default.Lock,
              contentDescription = "Objective",
              tint = if (isTerminalHacked) RadarGreen else DesertGold,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (isTerminalHacked) "EXTRACT AT NORTH GATE" else "HACK SECURITY CONSOLE",
              color = CarbonTextPrimary,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
          }
        }

        // Close / Abort button
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

      // Health bar & timer
      Spacer(modifier = Modifier.height(4.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Shield, contentDescription = "Armor", tint = NitroBlue, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "HP: ${playerHealth.toInt()}%",
            color = NitroBlue,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
        }

        Text(
          text = "TIME: ${"%.1f".format(missionTimeSeconds)}s",
          color = CarbonTextSecondary,
          fontSize = 12.sp,
          fontFamily = FontFamily.Monospace
        )
      }
    }

    // Bottom Tactical Controls (Virtual Touch Joystick + EMP / Stun Gadget buttons)
    Row(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
        .navigationBarsPadding()
        .padding(horizontal = 20.dp, vertical = 14.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.Bottom
    ) {
      // Virtual Directional Thumb Pad
      Box(
        modifier = Modifier
          .size(120.dp)
          .clip(CircleShape)
          .background(CarbonCard.copy(alpha = 0.75f))
          .border(2.dp, CarbonBorder, CircleShape)
          .pointerInput(Unit) {
            detectDragGestures(
              onDragEnd = { joystickOffset = Offset.Zero },
              onDragCancel = { joystickOffset = Offset.Zero },
              onDrag = { change, dragAmount ->
                change.consume()
                val newX = (joystickOffset.x + dragAmount.x).coerceIn(-50f, 50f)
                val newY = (joystickOffset.y + dragAmount.y).coerceIn(-50f, 50f)
                joystickOffset = Offset(newX, newY)
              }
            )
          },
        contentAlignment = Alignment.Center
      ) {
        Canvas(modifier = Modifier.size(120.dp)) {
          drawCircle(color = CarbonBorder.copy(alpha = 0.4f), radius = 50.dp.toPx(), style = Stroke(2f))
          drawCircle(
            color = if (isCrouching) RadarGreen else DesertGold,
            radius = 24.dp.toPx(),
            center = Offset(size.width / 2f + joystickOffset.x, size.height / 2f + joystickOffset.y)
          )
        }
      }

      // Tactical Action Buttons
      Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Stun Dart Button
        Button(
          onClick = {
            if (stunDarts > 0) {
              // Find nearest drone and disable
              val nearestDrone = drones.minByOrNull { hypot(playerX - it.x, playerY - it.y) }
              if (nearestDrone != null && hypot(playerX - nearestDrone.x, playerY - nearestDrone.y) < 0.35f) {
                nearestDrone.disabledTimer = 8f
                stunDarts--
                soundManager.playWireCut()
              }
            }
          },
          enabled = stunDarts > 0,
          colors = ButtonDefaults.buttonColors(containerColor = if (stunDarts > 0) CarbonCard else Color(0xFF1E293B)),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.border(1.dp, if (stunDarts > 0) DesertGold else CarbonBorder, RoundedCornerShape(12.dp))
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.FlashOn, contentDescription = "Stun Dart", tint = DesertGold, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("STUN DART ($stunDarts)", fontSize = 11.sp, color = CarbonTextPrimary, fontWeight = FontWeight.Bold)
          }
        }

        // EMP Jammer Button (Disables closest radar)
        Button(
          onClick = {
            if (empCharges > 0) {
              val closestRadar = radars.minByOrNull { hypot(playerX - it.x, playerY - it.y) }
              if (closestRadar != null) {
                closestRadar.disabledTimer = 8f
                empCharges--
                soundManager.playSound("explosion", 0.6f)
                soundManager.vibrate(80)
              }
            }
          },
          enabled = empCharges > 0,
          colors = ButtonDefaults.buttonColors(containerColor = if (empCharges > 0) CarbonCard else Color(0xFF1E293B)),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.border(1.dp, if (empCharges > 0) NitroBlue else CarbonBorder, RoundedCornerShape(12.dp))
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Bolt, contentDescription = "EMP Jammer", tint = NitroBlue, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("EMP JAMMER ($empCharges)", fontSize = 11.sp, color = CarbonTextPrimary, fontWeight = FontWeight.Bold)
          }
        }

        // Stance Toggle (Crouch / Sneak vs Run)
        Button(
          onClick = {
            isCrouching = !isCrouching
            soundManager.vibrate(20)
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = if (isCrouching) RadarGreen.copy(alpha = 0.25f) else DesertGold.copy(alpha = 0.25f)
          ),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.border(
            1.dp,
            if (isCrouching) RadarGreen else DesertGold,
            RoundedCornerShape(12.dp)
          )
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = if (isCrouching) Icons.Default.DirectionsWalk else Icons.Default.DirectionsRun,
              contentDescription = "Stance",
              tint = if (isCrouching) RadarGreen else DesertGold,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (isCrouching) "SNEAK (SILENT)" else "RUN (SPRINT)",
              fontSize = 11.sp,
              color = if (isCrouching) RadarGreen else DesertGold,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }

    // Terminal Hacking Mini-Modal
    AnimatedVisibility(
      visible = isShowingHackDialog,
      enter = fadeIn(),
      exit = fadeOut(),
      modifier = Modifier.align(Alignment.Center)
    ) {
      Surface(
        color = CarbonCard,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, NitroBlue),
        modifier = Modifier
          .padding(24.dp)
          .fillMaxWidth(0.92f)
      ) {
        Column(
          modifier = Modifier.padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Icon(Icons.Default.Key, contentDescription = "Hack", tint = NitroBlue, modifier = Modifier.size(36.dp))
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "SECURITY CIPHER TERMINAL",
            color = CarbonTextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace
          )
          Text(
            text = "Match the frequency dial to decrypt hangar blast doors.",
            color = CarbonTextSecondary,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
          )

          Spacer(modifier = Modifier.height(16.dp))

          // Frequency Wave Visualization
          Canvas(
            modifier = Modifier
              .fillMaxWidth()
              .height(60.dp)
              .background(CarbonDark, RoundedCornerShape(8.dp))
              .border(1.dp, CarbonBorder, RoundedCornerShape(8.dp))
          ) {
            val cw = size.width
            val ch = size.height
            val midY = ch / 2f

            // Target wave (Cyan)
            val targetPath = Path().apply {
              moveTo(0f, midY)
              for (x in 0..cw.toInt() step 6) {
                val rad = (x / cw) * 4 * PI + (hackTargetFrequency * 10f)
                val y = midY + sin(rad).toFloat() * 18f
                lineTo(x.toFloat(), y)
              }
            }
            drawPath(targetPath, color = NitroBlue.copy(alpha = 0.5f), style = Stroke(2f))

            // Player dialed wave (Gold)
            val playerPath = Path().apply {
              moveTo(0f, midY)
              for (x in 0..cw.toInt() step 6) {
                val rad = (x / cw) * 4 * PI + (hackCurrentDial * 10f)
                val y = midY + sin(rad).toFloat() * 18f
                lineTo(x.toFloat(), y)
              }
            }
            drawPath(playerPath, color = DesertGold, style = Stroke(2.5f))
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Alignment accuracy
          val accuracy = (1f - kotlin.math.abs(hackCurrentDial - hackTargetFrequency) * 2f).coerceIn(0f, 1f)
          Text(
            text = "SIGNAL MATCH: ${(accuracy * 100).toInt()}%",
            color = if (accuracy > 0.85f) RadarGreen else DesertGold,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )

          Spacer(modifier = Modifier.height(10.dp))

          // Dial slider simulation buttons
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
          ) {
            Button(
              onClick = {
                hackCurrentDial = (hackCurrentDial - 0.05f).coerceIn(0f, 1f)
                soundManager.playSound("tick", 0.5f)
              },
              colors = ButtonDefaults.buttonColors(containerColor = CarbonDark)
            ) {
              Text("<< FREQ -", color = CarbonTextPrimary)
            }
            Button(
              onClick = {
                hackCurrentDial = (hackCurrentDial + 0.05f).coerceIn(0f, 1f)
                soundManager.playSound("tick", 0.5f)
              },
              colors = ButtonDefaults.buttonColors(containerColor = CarbonDark)
            ) {
              Text("FREQ + >>", color = CarbonTextPrimary)
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Execute Decrypt Button
          Button(
            onClick = {
              if (accuracy > 0.80f) {
                isTerminalHacked = true
                isShowingHackDialog = false
                soundManager.playSound("victory", 0.8f)
              } else {
                alertLevel = (alertLevel + 25f).coerceAtMost(100f)
                soundManager.playAlert()
              }
            },
            colors = ButtonDefaults.buttonColors(
              containerColor = if (accuracy > 0.80f) RadarGreen else DesertGold
            ),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = if (accuracy > 0.80f) "CIPHER MATCHED - UNLOCK" else "MISALIGNED - RETRY",
              fontWeight = FontWeight.Bold,
              color = CarbonBlack
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
          .background(Color.Black.copy(alpha = 0.88f)),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.padding(24.dp)
        ) {
          Icon(Icons.Default.Warning, contentDescription = "Failed", tint = AlertRed, modifier = Modifier.size(54.dp))
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "MISSION FAILED",
            color = AlertRed,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace
          )
          Text(
            text = "Operative detected and compromised by hangar security.",
            color = CarbonTextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
          )
          Spacer(modifier = Modifier.height(24.dp))
          Button(
            onClick = {
              playerHealth = 100f
              alertLevel = 0f
              playerX = 0.12f
              playerY = 0.88f
              isGameOver = false
            },
            colors = ButtonDefaults.buttonColors(containerColor = DesertGold)
          ) {
            Text("RETRY MISSION", color = CarbonBlack, fontWeight = FontWeight.Bold)
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
