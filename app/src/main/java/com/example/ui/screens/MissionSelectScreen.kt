package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
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
import com.example.ui.theme.CarbonTextMuted
import com.example.ui.theme.CarbonTextPrimary
import com.example.ui.theme.CarbonTextSecondary
import com.example.ui.theme.DesertGold
import com.example.ui.theme.NitroBlue
import com.example.ui.theme.RadarGreen

data class MissionCardData(
  val id: MissionId,
  val code: String,
  val title: String,
  val location: String,
  val imageRes: Int,
  val brief: String,
  val objectives: List<String>,
  val rewardCredits: Int
)

@Composable
fun MissionSelectScreen(
  missionProgress: Map<MissionId, MissionProgress>,
  soundManager: GameSoundManager,
  onLaunchMission: (MissionId) -> Unit,
  onBack: () -> Unit
) {
  val missionsList = listOf(
    MissionCardData(
      id = MissionId.MISSION_1,
      code = "MISSION 01",
      title = "Hangar Infiltration",
      location = "Sector 7 Military Aircraft Hangar",
      imageRes = R.drawable.img_hangar_stealth,
      brief = "Stealth entry into an enemy aircraft hangar. Avoid sweeping radar cones, bypass security drones, and hack the cipher console to reveal the convoy itinerary.",
      objectives = listOf(
        "Evade 3 sweeping radar detection zones",
        "Silently disable or avoid patrol drones",
        "Crack cipher at security terminal",
        "Extract via blast doors"
      ),
      rewardCredits = 3000
    ),
    MissionCardData(
      id = MissionId.MISSION_2,
      code = "MISSION 02",
      title = "Dune Pursuit",
      location = "Sector 9 Great Dune Corridor",
      imageRes = R.drawable.img_desert_storm_hero,
      brief = "High-speed combat vehicle chase across desert dunes. Engage nitro boosts to intercept the heavy armored convoy, destroy escort gunners, and recover the military data chip.",
      objectives = listOf(
        "Reach speeds over 200 km/h with nitro boosts",
        "Neutralize enemy scout buggies & escort trucks",
        "Ram/destroy the Heavy Boss Carrier Rig",
        "Collect the ejected Data Chip"
      ),
      rewardCredits = 4500
    ),
    MissionCardData(
      id = MissionId.MISSION_3,
      code = "MISSION 03",
      title = "Zero Hour HQ",
      location = "Enemy Command Bunker Core",
      imageRes = R.drawable.img_bomb_defusal,
      brief = "Final showdown at enemy headquarters. The armed Omega dirty bomb is ticking down from 60 seconds. Stabilize frequencies, bleed coolant pressure, and cut the correct wire matrix!",
      objectives = listOf(
        "Harmonize oscillating frequency channels",
        "Purge 3 pressurized coolant chambers",
        "Decipher and snip ignition wires without detonating RED lead",
        "Repel hostile breach drone interference"
      ),
      rewardCredits = 7000
    )
  )

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(CarbonBlack)
      .statusBarsPadding()
      .navigationBarsPadding()
      .padding(horizontal = 16.dp, vertical = 8.dp)
      .testTag("mission_select_screen")
  ) {
    // Top Bar
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
            text = "TACTICAL OPERATIONS",
            color = DesertGold,
            fontSize = 15.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
          )
          Text(
            text = "SELECT DEPLOYMENT THEATRE",
            color = CarbonTextSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // List of Mission Cards
    LazyColumn(
      verticalArrangement = Arrangement.spacedBy(16.dp),
      modifier = Modifier.fillMaxSize()
    ) {
      items(missionsList) { item ->
        val prog = missionProgress[item.id]
        val isUnlocked = prog?.isUnlocked == true
        val isCompleted = prog?.isCompleted == true
        val stars = prog?.stars ?: 0
        val bestTime = prog?.bestTimeSeconds ?: 0f

        Card(
          colors = CardDefaults.cardColors(containerColor = CarbonDark),
          shape = RoundedCornerShape(16.dp),
          border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (isUnlocked) DesertGold.copy(alpha = 0.7f) else CarbonBorder
          ),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column {
            // Card Banner Art
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
            ) {
              Image(
                painter = painterResource(id = item.imageRes),
                contentDescription = item.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
              )
              // Gradient overlay
              Box(
                modifier = Modifier
                  .fillMaxSize()
                  .background(
                    Brush.verticalGradient(
                      colors = listOf(Color.Transparent, CarbonDark.copy(alpha = 0.95f))
                    )
                  )
              )

              // Code & Location Tag
              Surface(
                color = CarbonBlack.copy(alpha = 0.8f),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder),
                modifier = Modifier
                  .align(Alignment.TopStart)
                  .padding(10.dp)
              ) {
                Text(
                  text = "${item.code} // ${item.location.uppercase()}",
                  color = DesertGold,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }

              // Locked / Stars badge
              Surface(
                color = CarbonBlack.copy(alpha = 0.8f),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder),
                modifier = Modifier
                  .align(Alignment.TopEnd)
                  .padding(10.dp)
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  if (!isUnlocked) {
                    Icon(Icons.Default.Lock, contentDescription = "Locked", tint = CarbonTextMuted, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("LOCKED", color = CarbonTextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                  } else {
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
            }

            // Card Body
            Column(modifier = Modifier.padding(14.dp)) {
              Text(
                text = item.title,
                color = CarbonTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = item.brief,
                color = CarbonTextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
              )

              Spacer(modifier = Modifier.height(10.dp))

              // Objectives
              Text(
                text = "PRIMARY OBJECTIVES:",
                color = DesertGold,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
              Spacer(modifier = Modifier.height(4.dp))
              item.objectives.forEach { obj ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text("•", color = NitroBlue, fontSize = 12.sp, modifier = Modifier.padding(end = 6.dp))
                  Text(obj, color = CarbonTextPrimary, fontSize = 11.sp)
                }
              }

              Spacer(modifier = Modifier.height(12.dp))

              // Bottom row: Best time & Deploy Button
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  if (bestTime > 0f) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Icon(Icons.Default.Timer, contentDescription = "Time", tint = NitroBlue, modifier = Modifier.size(14.dp))
                      Spacer(modifier = Modifier.width(4.dp))
                      Text("BEST: ${"%.1f".format(bestTime)}s", color = NitroBlue, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                  }
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ElectricBolt, contentDescription = "Reward", tint = DesertGold, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("BOUNTY: +${item.rewardCredits} CR", color = DesertGold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                  }
                }

                Button(
                  onClick = {
                    soundManager.playNitroBoost()
                    onLaunchMission(item.id)
                  },
                  enabled = isUnlocked,
                  colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCompleted) RadarGreen else DesertGold
                  ),
                  shape = RoundedCornerShape(10.dp)
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Deploy", tint = CarbonBlack, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = if (isCompleted) "REPLAY" else "DEPLOY",
                      color = CarbonBlack,
                      fontWeight = FontWeight.Black,
                      fontSize = 12.sp
                    )
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
