package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.GameSoundManager
import com.example.model.VehicleConfig
import com.example.ui.theme.AlertRed
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
import com.example.ui.theme.RadarGreen
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GarageScreen(
  vehicles: List<VehicleConfig>,
  selectedVehicleId: String,
  credits: Int,
  engineLevel: Int,
  nitroLevel: Int,
  armorLevel: Int,
  weaponsLevel: Int,
  soundManager: GameSoundManager,
  onSelectVehicle: (String) -> Unit,
  onUnlockVehicle: (String, Int) -> Unit,
  onUpgradeStat: (String, Int) -> Unit,
  onBack: () -> Unit
) {
  var turntableAngle by remember { mutableFloatStateOf(35f) }
  val currentVehicle = vehicles.firstOrNull { it.id == selectedVehicleId } ?: vehicles.first()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(CarbonBlack)
      .statusBarsPadding()
      .navigationBarsPadding()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 16.dp, vertical = 8.dp)
      .testTag("garage_screen")
  ) {
    // Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
          onClick = onBack,
          modifier = Modifier
            .size(36.dp)
            .background(CarbonCard, CircleShape)
            .border(1.dp, CarbonBorder, CircleShape)
        ) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = CarbonTextPrimary, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          Text(
            text = "MILITARY MOTOR POOL",
            color = DesertGold,
            fontSize = 15.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace
          )
          Text(
            text = "VEHICLE ARSENAL & UPGRADES",
            color = CarbonTextSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }

      Surface(
        color = CarbonCard,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DesertGold)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.ElectricBolt, contentDescription = "Credits", tint = DesertGold, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("$credits CR", color = DesertGoldLight, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Interactive 3D Turntable Vehicle Showcase
    Card(
      colors = CardDefaults.cardColors(containerColor = CarbonDark),
      shape = RoundedCornerShape(16.dp),
      border = androidx.compose.foundation.BorderStroke(1.5.dp, CarbonBorder),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier.padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "ROTATABLE 3D CHASSIS INSPECTION (DRAG TO ROTATE)",
          color = CarbonTextMuted,
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0C131C))
            .pointerInput(Unit) {
              detectDragGestures { change, dragAmount ->
                change.consume()
                turntableAngle = (turntableAngle + dragAmount.x * 0.8f) % 360f
              }
            },
          contentAlignment = Alignment.Center
        ) {
          Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f + 25f
            val rad = turntableAngle * PI / 180f

            // Rotating Turntable Base Platform
            drawOval(
              color = Color(0x3338BDF8),
              topLeft = Offset(cx - 140f, cy - 40f),
              size = Size(280f, 80f),
              style = Stroke(2f)
            )
            drawOval(
              color = Color(0x1138BDF8),
              topLeft = Offset(cx - 120f, cy - 30f),
              size = Size(240f, 60f)
            )

            // 3D Projected Vehicle Model
            val vLen = 140f
            val vWid = 70f
            val vH = 45f

            val cosA = cos(rad).toFloat()
            val sinA = sin(rad).toFloat()

            // 3D projected vehicle center
            val pColor = when (currentVehicle.id) {
              "iron_vulture" -> Color(0xFF451A03)
              "ghost_spectre" -> Color(0xFF0F172A)
              else -> Color(0xFF1E293B)
            }
            val accentColor = when (currentVehicle.id) {
              "iron_vulture" -> AlertRed
              "ghost_spectre" -> NitroBlue
              else -> DesertGold
            }

            // Chassis box projection
            val cornerOffsets = listOf(
              Offset((-vLen / 2) * cosA - (-vWid / 2) * sinA, ((-vLen / 2) * sinA + (-vWid / 2) * cosA) * 0.45f),
              Offset((vLen / 2) * cosA - (-vWid / 2) * sinA, ((vLen / 2) * sinA + (-vWid / 2) * cosA) * 0.45f),
              Offset((vLen / 2) * cosA - (vWid / 2) * sinA, ((vLen / 2) * sinA + (vWid / 2) * cosA) * 0.45f),
              Offset((-vLen / 2) * cosA - (vWid / 2) * sinA, ((-vLen / 2) * sinA + (vWid / 2) * cosA) * 0.45f)
            )

            // Draw base polygon
            val basePath = Path().apply {
              moveTo(cx + cornerOffsets[0].x, cy + cornerOffsets[0].y)
              for (i in 1..3) {
                lineTo(cx + cornerOffsets[i].x, cy + cornerOffsets[i].y)
              }
              close()
            }
            drawPath(basePath, color = pColor)
            drawPath(basePath, color = accentColor, style = Stroke(2.5f))

            // Upper roof cab
            val roofOffsets = cornerOffsets.map { Offset(it.x * 0.65f, it.y * 0.65f - vH) }
            val roofPath = Path().apply {
              moveTo(cx + roofOffsets[0].x, cy + roofOffsets[0].y)
              for (i in 1..3) {
                lineTo(cx + roofOffsets[i].x, cy + roofOffsets[i].y)
              }
              close()
            }
            drawPath(roofPath, color = Color(0x9938BDF8))
            drawPath(roofPath, color = accentColor, style = Stroke(2f))

            // Pillars connecting base to roof
            for (i in 0..3) {
              drawLine(
                color = accentColor,
                start = Offset(cx + cornerOffsets[i].x * 0.7f, cy + cornerOffsets[i].y * 0.7f),
                end = Offset(cx + roofOffsets[i].x, cy + roofOffsets[i].y),
                strokeWidth = 2f
              )
            }

            // Dual Cannons on top
            val cannonX = cx + (20f * cosA)
            val cannonY = cy - vH - 6f + (20f * sinA * 0.45f)
            drawCircle(color = DesertGold, radius = 6f, center = Offset(cannonX, cannonY))
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Vehicle Name & Selection Tag
        Text(
          text = currentVehicle.name,
          color = CarbonTextPrimary,
          fontSize = 18.sp,
          fontWeight = FontWeight.Black
        )
        Text(
          text = currentVehicle.tag,
          color = DesertGold,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold
        )
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Vehicle Selection Switcher Tabs
    Text("SELECT CHASSIS", color = CarbonTextMuted, fontSize = 11.sp, fontWeight = FontWeight.Black)
    Spacer(modifier = Modifier.height(6.dp))
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      vehicles.forEach { v ->
        val isSelected = v.id == selectedVehicleId
        Surface(
          color = if (isSelected) CarbonCard else CarbonDark,
          shape = RoundedCornerShape(10.dp),
          border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (isSelected) DesertGold else CarbonBorder
          ),
          modifier = Modifier
            .weight(1f)
            .clickable {
              if (v.isUnlocked) {
                onSelectVehicle(v.id)
                soundManager.playSound("beep")
              } else {
                onUnlockVehicle(v.id, v.unlockCost)
                soundManager.playSound("beep")
              }
            }
        ) {
          Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = v.name.split(" ").first(),
              color = if (isSelected) DesertGold else CarbonTextPrimary,
              fontSize = 11.sp,
              fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(2.dp))
            if (v.isUnlocked) {
              Text(
                text = if (isSelected) "EQUIPPED" else "SELECT",
                color = if (isSelected) RadarGreen else CarbonTextMuted,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
              )
            } else {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lock, contentDescription = "Locked", tint = AlertRed, modifier = Modifier.size(10.dp))
                Spacer(modifier = Modifier.width(2.dp))
                Text("${v.unlockCost} CR", color = AlertRed, fontSize = 9.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Vehicle Stats & Upgrade Station
    Text("ARSENAL TUNING & UPGRADES (LEVEL 1-5)", color = CarbonTextMuted, fontSize = 11.sp, fontWeight = FontWeight.Black)
    Spacer(modifier = Modifier.height(8.dp))

    Card(
      colors = CardDefaults.cardColors(containerColor = CarbonDark),
      shape = RoundedCornerShape(14.dp),
      border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        // 1. Engine Upgrade
        UpgradeRow(
          title = "TWIN-TURBO ENGINE",
          desc = "Max speed: ${currentVehicle.baseSpeed + engineLevel * 12} KM/H",
          level = engineLevel,
          cost = engineLevel * 800,
          canAfford = credits >= (engineLevel * 800),
          onUpgrade = {
            onUpgradeStat("engine", engineLevel * 800)
            soundManager.playWireCut()
          }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Nitro Supercharger
        UpgradeRow(
          title = "NITRO SUPERCHARGER",
          desc = "Tank capacity: ${currentVehicle.baseNitro + nitroLevel * 15}%",
          level = nitroLevel,
          cost = nitroLevel * 800,
          canAfford = credits >= (nitroLevel * 800),
          onUpgrade = {
            onUpgradeStat("nitro", nitroLevel * 800)
            soundManager.playWireCut()
          }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 3. Armor Plating
        UpgradeRow(
          title = "REACTIVE COMPOSITE ARMOR",
          desc = "Shield points: ${currentVehicle.baseArmor + armorLevel * 20} HP",
          level = armorLevel,
          cost = armorLevel * 800,
          canAfford = credits >= (armorLevel * 800),
          onUpgrade = {
            onUpgradeStat("armor", armorLevel * 800)
            soundManager.playWireCut()
          }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 4. Heavy Gatling Cannons
        UpgradeRow(
          title = "TWIN GATLING CANNONS",
          desc = "Firepower: ${currentVehicle.baseFirepower + weaponsLevel * 15} DMG",
          level = weaponsLevel,
          cost = weaponsLevel * 800,
          canAfford = credits >= (weaponsLevel * 800),
          onUpgrade = {
            onUpgradeStat("weapons", weaponsLevel * 800)
            soundManager.playWireCut()
          }
        )
      }
    }
  }
}

@Composable
fun UpgradeRow(
  title: String,
  desc: String,
  level: Int,
  cost: Int,
  canAfford: Boolean,
  onUpgrade: () -> Unit
) {
  val isMax = level >= 5

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(title, color = CarbonTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
      Text(desc, color = CarbonTextSecondary, fontSize = 10.sp)
      Spacer(modifier = Modifier.height(4.dp))
      Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        for (i in 1..5) {
          Box(
            modifier = Modifier
              .size(width = 24.dp, height = 6.dp)
              .clip(RoundedCornerShape(3.dp))
              .background(if (i <= level) DesertGold else CarbonBorder)
          )
        }
      }
    }

    Spacer(modifier = Modifier.width(12.dp))

    Button(
      onClick = onUpgrade,
      enabled = !isMax && canAfford,
      colors = ButtonDefaults.buttonColors(
        containerColor = if (isMax) CarbonSurface else if (canAfford) NitroBlue else CarbonDark
      ),
      shape = RoundedCornerShape(8.dp)
    ) {
      if (isMax) {
        Text("MAX", color = RadarGreen, fontSize = 11.sp, fontWeight = FontWeight.Black)
      } else {
        Text(
          "$cost CR",
          color = if (canAfford) CarbonBlack else CarbonTextMuted,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold
        )
      }
    }
  }
}
