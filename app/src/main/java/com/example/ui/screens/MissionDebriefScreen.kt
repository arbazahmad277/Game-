package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.GameSoundManager
import com.example.model.MissionId
import com.example.model.MissionResult
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

@Composable
fun MissionDebriefScreen(
  result: MissionResult,
  soundManager: GameSoundManager,
  onNextMission: (MissionId) -> Unit,
  onReturnToMenu: () -> Unit
) {
  val starScale1 = remember { Animatable(0f) }
  val starScale2 = remember { Animatable(0f) }
  val starScale3 = remember { Animatable(0f) }

  LaunchedEffect(Unit) {
    if (result.stars >= 1) {
      starScale1.animateTo(1f, tween(300))
      soundManager.playSound("beep")
    }
    if (result.stars >= 2) {
      starScale2.animateTo(1f, tween(300))
      soundManager.playSound("beep")
    }
    if (result.stars >= 3) {
      starScale3.animateTo(1f, tween(300))
      soundManager.playSound("victory")
    }
  }

  val nextMissionId = when (result.missionId) {
    MissionId.MISSION_1 -> MissionId.MISSION_2
    MissionId.MISSION_2 -> MissionId.MISSION_3
    MissionId.MISSION_3 -> null
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(CarbonBlack)
      .statusBarsPadding()
      .navigationBarsPadding()
      .testTag("mission_debrief_screen"),
    contentAlignment = Alignment.Center
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(20.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Victory Medal Icon
      Icon(
        imageVector = Icons.Default.EmojiEvents,
        contentDescription = "Victory",
        tint = DesertGold,
        modifier = Modifier.size(56.dp)
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "MISSION ACCOMPLISHED",
        color = DesertGold,
        fontSize = 24.sp,
        fontWeight = FontWeight.Black,
        fontFamily = FontFamily.Monospace,
        letterSpacing = 2.sp
      )

      Text(
        text = "${result.missionId.code} // ${result.missionId.title.uppercase()}",
        color = NitroBlue,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Animated Stars Row
      Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Icon(
          imageVector = Icons.Default.Star,
          contentDescription = null,
          tint = if (result.stars >= 1) DesertGold else CarbonBorder,
          modifier = Modifier
            .size(44.dp)
            .scale(if (result.stars >= 1) starScale1.value else 1f)
        )
        Icon(
          imageVector = Icons.Default.Star,
          contentDescription = null,
          tint = if (result.stars >= 2) DesertGold else CarbonBorder,
          modifier = Modifier
            .size(52.dp)
            .scale(if (result.stars >= 2) starScale2.value else 1f)
        )
        Icon(
          imageVector = Icons.Default.Star,
          contentDescription = null,
          tint = if (result.stars >= 3) DesertGold else CarbonBorder,
          modifier = Modifier
            .size(44.dp)
            .scale(if (result.stars >= 3) starScale3.value else 1f)
        )
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Performance Stats Card
      Card(
        colors = CardDefaults.cardColors(containerColor = CarbonDark),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          // Stat rows
          StatRow(
            label = "MISSION TIME",
            value = "${"%.1f".format(result.timeElapsedSeconds)}s",
            color = NitroBlue
          )
          Spacer(modifier = Modifier.height(8.dp))
          StatRow(
            label = "COMBAT RATING",
            value = "${result.score} PTS",
            color = RadarGreen
          )
          Spacer(modifier = Modifier.height(8.dp))
          StatRow(
            label = "CREDITS REWARD",
            value = "+${result.creditsEarned} CR",
            color = DesertGoldLight
          )

          Spacer(modifier = Modifier.height(14.dp))

          // Debrief notes
          Text(
            text = "TACTICAL AFTER-ACTION REPORT:",
            color = CarbonTextMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
          Spacer(modifier = Modifier.height(6.dp))
          result.debriefNotes.forEach { note ->
            Row(
              modifier = Modifier.padding(vertical = 2.dp),
              verticalAlignment = Alignment.Top
            ) {
              Text("▶", color = DesertGold, fontSize = 10.sp, modifier = Modifier.padding(end = 6.dp, top = 2.dp))
              Text(note, color = CarbonTextSecondary, fontSize = 11.sp, lineHeight = 15.sp)
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Navigation Buttons
      if (nextMissionId != null) {
        Button(
          onClick = {
            soundManager.playNitroBoost()
            onNextMission(nextMissionId)
          },
          colors = ButtonDefaults.buttonColors(containerColor = DesertGold),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.PlayArrow, contentDescription = "Next", tint = CarbonBlack)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "PROCEED TO ${nextMissionId.code}",
              color = CarbonBlack,
              fontWeight = FontWeight.Black,
              fontSize = 14.sp
            )
          }
        }
        Spacer(modifier = Modifier.height(10.dp))
      }

      Button(
        onClick = onReturnToMenu,
        colors = ButtonDefaults.buttonColors(containerColor = CarbonCard),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
          .border(1.5.dp, CarbonBorder, RoundedCornerShape(14.dp))
      ) {
        Text("RETURN TO CAMPAIGN HQ", color = CarbonTextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
      }
    }
  }
}

@Composable
fun StatRow(label: String, value: String, color: Color) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(label, color = CarbonTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    Text(value, color = color, fontSize = 14.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
  }
}
