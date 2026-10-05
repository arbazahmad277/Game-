package com.example.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.audio.GameSoundManager
import com.example.model.MissionId
import com.example.model.MissionProgress
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

@Composable
fun MainMenuScreen(
  credits: Int,
  missionProgress: Map<MissionId, MissionProgress>,
  soundManager: GameSoundManager,
  onStartCampaign: () -> Unit,
  onOpenGarage: () -> Unit,
  onOpenIntel: () -> Unit,
  onToggleSound: () -> Unit,
  onToggleVibration: () -> Unit
) {
  val totalStars = missionProgress.values.sumOf { it.stars }
  val infiniteTransition = rememberInfiniteTransition(label = "heroPulse")
  val glowAlpha by infiniteTransition.animateFloat(
    initialValue = 0.6f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200),
      repeatMode = RepeatMode.Reverse
    ),
    label = "glow"
  )

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(CarbonBlack)
      .testTag("main_menu_container")
  ) {
    // Hero Background Artwork
    Image(
      painter = painterResource(id = R.drawable.img_desert_storm_hero),
      contentDescription = "Desert Storm Key Art",
      modifier = Modifier
        .fillMaxWidth()
        .height(380.dp),
      contentScale = ContentScale.Crop
    )

    // Dark gradient vignette over hero
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(380.dp)
        .background(
          Brush.verticalGradient(
            colors = listOf(Color.Transparent, CarbonBlack.copy(alpha = 0.7f), CarbonBlack)
          )
        )
    )

    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp, vertical = 12.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Top bar: Credits, Stars & Audio Toggles
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Player Tactical Currency
        Surface(
          color = CarbonCard.copy(alpha = 0.85f),
          shape = RoundedCornerShape(20.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, DesertGold.copy(alpha = 0.6f))
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.ElectricBolt, contentDescription = "Credits", tint = DesertGold, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "$credits CR",
              color = DesertGoldLight,
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          // Campaign Stars
          Surface(
            color = CarbonCard.copy(alpha = 0.85f),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Default.Star, contentDescription = "Stars", tint = DesertGold, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "$totalStars / 9",
                color = CarbonTextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
            }
          }

          Spacer(modifier = Modifier.width(8.dp))

          // Sound toggle
          IconButton(
            onClick = {
              onToggleSound()
              soundManager.vibrate(20)
            },
            modifier = Modifier
              .size(36.dp)
              .background(CarbonCard.copy(alpha = 0.85f), CircleShape)
              .border(1.dp, CarbonBorder, CircleShape)
          ) {
            Icon(
              imageVector = if (soundManager.isSoundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
              contentDescription = "Sound",
              tint = if (soundManager.isSoundEnabled) NitroBlue else CarbonTextMuted,
              modifier = Modifier.size(18.dp)
            )
          }

          Spacer(modifier = Modifier.width(6.dp))

          // Vibration toggle
          IconButton(
            onClick = {
              onToggleVibration()
              soundManager.vibrate(30)
            },
            modifier = Modifier
              .size(36.dp)
              .background(CarbonCard.copy(alpha = 0.85f), CircleShape)
              .border(1.dp, CarbonBorder, CircleShape)
          ) {
            Icon(
              imageVector = Icons.Default.Vibration,
              contentDescription = "Haptics",
              tint = if (soundManager.isVibrationEnabled) RadarGreen else CarbonTextMuted,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(130.dp))

      // Game Title Branding
      Text(
        text = "DESERT STORM",
        color = DesertGold,
        fontSize = 34.sp,
        fontWeight = FontWeight.Black,
        fontFamily = FontFamily.Monospace,
        letterSpacing = 3.sp,
        textAlign = TextAlign.Center
      )
      Text(
        text = "LEGACY",
        color = NitroBlue,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        letterSpacing = 8.sp,
        modifier = Modifier.padding(top = 2.dp)
      )

      Text(
        text = "OPEN-WORLD ACTION & HIGH-SPEED VEHICLE CHASE",
        color = CarbonTextSecondary,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.5.sp,
        modifier = Modifier.padding(top = 6.dp)
      )

      Spacer(modifier = Modifier.height(28.dp))

      // MAIN ACTION: LAUNCH CAMPAIGN
      Button(
        onClick = {
          soundManager.playNitroBoost()
          onStartCampaign()
        },
        colors = ButtonDefaults.buttonColors(containerColor = DesertGold),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(60.dp)
          .border(2.dp, DesertGoldLight.copy(alpha = glowAlpha), RoundedCornerShape(16.dp))
          .testTag("launch_campaign_button")
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = CarbonBlack, modifier = Modifier.size(28.dp))
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "DEPLOY MISSIONS",
            color = CarbonBlack,
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // SECONDARY BUTTONS: GARAGE & INTEL
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Button(
          onClick = {
            soundManager.playSound("beep")
            onOpenGarage()
          },
          colors = ButtonDefaults.buttonColors(containerColor = CarbonCard),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .weight(1f)
            .height(52.dp)
            .border(1.5.dp, CarbonBorder, RoundedCornerShape(14.dp))
            .testTag("garage_button")
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.DirectionsCar, contentDescription = "Garage", tint = NitroBlue, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "GARAGE",
              color = CarbonTextPrimary,
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp
            )
          }
        }

        Button(
          onClick = {
            soundManager.playSound("beep")
            onOpenIntel()
          },
          colors = ButtonDefaults.buttonColors(containerColor = CarbonCard),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .weight(1f)
            .height(52.dp)
            .border(1.5.dp, CarbonBorder, RoundedCornerShape(14.dp))
            .testTag("intel_button")
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.FolderSpecial, contentDescription = "Intel", tint = RadarGreen, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "INTEL LOGS",
              color = CarbonTextPrimary,
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Mission Overview Cards Grid
      Text(
        text = "CAMPAIGN THEATRE",
        color = CarbonTextMuted,
        fontSize = 11.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 1.5.sp,
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 8.dp)
      )

      Card(
        colors = CardDefaults.cardColors(containerColor = CarbonDark),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          CampaignOverviewItem(
            num = "01",
            title = "HANGAR INFILTRATION",
            desc = "Stealth entry avoiding radars and security vision sweeps.",
            isUnlocked = missionProgress[MissionId.MISSION_1]?.isUnlocked == true,
            isCompleted = missionProgress[MissionId.MISSION_1]?.isCompleted == true,
            stars = missionProgress[MissionId.MISSION_1]?.stars ?: 0
          )
          Spacer(modifier = Modifier.height(10.dp))
          CampaignOverviewItem(
            num = "02",
            title = "HIGH-SPEED CAR CHASE",
            desc = "Nitro boost chase across dunes to destroy convoy & recover data chip.",
            isUnlocked = missionProgress[MissionId.MISSION_2]?.isUnlocked == true,
            isCompleted = missionProgress[MissionId.MISSION_2]?.isCompleted == true,
            stars = missionProgress[MissionId.MISSION_2]?.stars ?: 0
          )
          Spacer(modifier = Modifier.height(10.dp))
          CampaignOverviewItem(
            num = "03",
            title = "HQ BOMB SHOWDOWN",
            desc = "Final standoff to deactivate tactical warhead under 60 seconds.",
            isUnlocked = missionProgress[MissionId.MISSION_3]?.isUnlocked == true,
            isCompleted = missionProgress[MissionId.MISSION_3]?.isCompleted == true,
            stars = missionProgress[MissionId.MISSION_3]?.stars ?: 0
          )
        }
      }
    }
  }
}

@Composable
fun CampaignOverviewItem(
  num: String,
  title: String,
  desc: String,
  isUnlocked: Boolean,
  isCompleted: Boolean,
  stars: Int
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
      Text(
        text = num,
        color = if (isUnlocked) DesertGold else CarbonTextMuted,
        fontWeight = FontWeight.Black,
        fontSize = 14.sp,
        fontFamily = FontFamily.Monospace
      )
      Spacer(modifier = Modifier.width(10.dp))
      Column {
        Text(
          text = title,
          color = if (isUnlocked) CarbonTextPrimary else CarbonTextMuted,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = desc,
          color = CarbonTextSecondary,
          fontSize = 10.sp,
          maxLines = 1
        )
      }
    }

    Row {
      for (s in 1..3) {
        Icon(
          imageVector = Icons.Default.Star,
          contentDescription = null,
          tint = if (s <= stars) DesertGold else CarbonBorder,
          modifier = Modifier.size(14.dp)
        )
      }
    }
  }
}
