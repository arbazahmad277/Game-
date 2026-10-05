package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.GameSoundManager
import com.example.model.IntelEntry
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

@Composable
fun IntelArchiveScreen(
  soundManager: GameSoundManager,
  onBack: () -> Unit
) {
  val intelEntries = remember {
    listOf(
      IntelEntry(
        id = "intel_01",
        title = "OPERATION BLACKOUT: RADAR SWEEP DOSSIER",
        classification = "TOP SECRET // EYES ONLY",
        summary = "Surveillance mechanics and vulnerability specs of Sector 7 Hangar rotating radar arrays.",
        fullText = "Sector 7 Hangar is fortified with rotating pulse radars operating on a 360-degree sweep cycle. Field operatives are equipped with an experimental EMP Jammer capable of temporary microwave disruption. Radar sweeps can be blocked by standard military storage crates when crouching in stealth stance."
      ),
      IntelEntry(
        id = "intel_02",
        title = "COLOSSUS CONVOY & NITRO TELEMETRY",
        classification = "CONFIDENTIAL // TACTICAL",
        summary = "Technical schematics of the armored convoy transporting the military data chip through Dune Corridor 9.",
        fullText = "The enemy heavy carrier rig is escorted by high-speed scout buggies and support trucks. Analysis indicates the carrier's reinforced rear bumper is vulnerable to high-velocity kinetic ramming when nitro boost is activated. Machine gun fire can strip outer plating to force early chip ejection."
      ),
      IntelEntry(
        id = "intel_03",
        title = "OMEGA WARHEAD DEFUSAL SCHEMATIC",
        classification = "CRITICAL // IMMEDIATE THREAT",
        summary = "Decryption protocol and safe wire severing matrix for the HQ bunker dirty bomb.",
        fullText = "Warning: The Omega Warhead utilizes a live trigger lead. Protocol confirms cutting the RED wire causes instantaneous detonation. The GREEN auxiliary capacitor must be severed first to dump secondary charge, followed by the YELLOW ignition line. Keep pressure chambers under 40 PSI to prevent thermal runaway."
      )
    )
  }

  var selectedEntry by remember { mutableStateOf<IntelEntry?>(intelEntries.first()) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(CarbonBlack)
      .statusBarsPadding()
      .navigationBarsPadding()
      .padding(horizontal = 16.dp, vertical = 8.dp)
      .testTag("intel_archive_screen")
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
            text = "INTELLIGENCE ARCHIVE",
            color = RadarGreen,
            fontSize = 15.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace
          )
          Text(
            text = "DECRYPTED MISSION LOGS",
            color = CarbonTextSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // List of intel files
    LazyColumn(
      verticalArrangement = Arrangement.spacedBy(10.dp),
      modifier = Modifier.weight(1f)
    ) {
      items(intelEntries) { entry ->
        val isSelected = entry.id == selectedEntry?.id
        Card(
          colors = CardDefaults.cardColors(containerColor = if (isSelected) CarbonCard else CarbonDark),
          shape = RoundedCornerShape(12.dp),
          border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (isSelected) RadarGreen else CarbonBorder
          ),
          modifier = Modifier
            .fillMaxWidth()
            .clickable {
              selectedEntry = entry
              soundManager.playSound("beep")
            }
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = entry.classification,
                color = if (entry.classification.contains("CRITICAL")) AlertRed else DesertGold,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
              Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                tint = if (isSelected) RadarGreen else CarbonTextMuted,
                modifier = Modifier.size(16.dp)
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = entry.title,
              color = CarbonTextPrimary,
              fontSize = 13.sp,
              fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = entry.summary,
              color = CarbonTextSecondary,
              fontSize = 11.sp,
              lineHeight = 15.sp
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Selected dossier details card
    selectedEntry?.let { entry ->
      Card(
        colors = CardDefaults.cardColors(containerColor = CarbonDark),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, DesertGold),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Description, contentDescription = null, tint = DesertGold, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("FIELD DOSSIER REPORT", color = DesertGold, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
          }
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = entry.fullText,
            color = CarbonTextPrimary,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            fontFamily = FontFamily.Monospace
          )
          Spacer(modifier = Modifier.height(10.dp))
          Button(
            onClick = {
              soundManager.playSound("radar", 0.7f)
              soundManager.vibrate(40)
            },
            colors = ButtonDefaults.buttonColors(containerColor = CarbonCard),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(Icons.Default.VolumeUp, contentDescription = "Play Audio", tint = NitroBlue, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("TRANSMIT TACTICAL BRIEF AUDIO", color = NitroBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
