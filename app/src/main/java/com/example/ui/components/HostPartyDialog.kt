package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Party
import com.example.model.PartyType
import com.example.ui.theme.CartoonBorder
import com.example.ui.theme.PartyCyan
import com.example.ui.theme.PartyGreen
import com.example.ui.theme.PartyGreenLight
import com.example.ui.theme.PartyOrange
import com.example.ui.theme.PartyPink
import com.example.ui.theme.PartyPinkLight
import com.example.ui.theme.PartyPurple
import com.example.ui.theme.PartyPurpleLight
import com.example.ui.theme.PartyYellow
import com.example.ui.theme.PartyYellowLight
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HostPartyDialog(
  onDismiss: () -> Unit,
  onPartyCreated: (Party) -> Unit,
  currentPickedLocation: Pair<Float, Float>,
  onPickLocationOnMap: () -> Unit,
  modifier: Modifier = Modifier
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  var name by remember { mutableStateOf("") }
  var partyType by remember { mutableStateOf(PartyType.PUBLIC) }
  var selectedEmoji by remember { mutableStateOf("🎉") }
  var selectedVibe by remember { mutableStateOf("Bollywood") }
  var address by remember { mutableStateOf("Bandra West, Mumbai") }
  var startTime by remember { mutableStateOf("Tonight @ 9:00 PM") }
  var isLiveNow by remember { mutableStateOf(true) }
  var maxGuestsText by remember { mutableStateOf("50") }
  var description by remember { mutableStateOf("") }
  var showTypeTooltip by remember { mutableStateOf(false) }

  val emojiOptions = listOf("🎉", "🪩", "🔥", "🍕", "🍹", "🕺", "🎧", "🎸", "🌮", "🍸", "⚡", "👾")
  val vibeOptions = listOf("Bollywood", "EDM", "Chill", "House Party", "Rooftop", "Techno", "Hip-hop", "Karaoke", "Retro")

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = Color.White,
    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
    modifier = modifier.testTag("host_party_dialog")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp)
        .padding(bottom = 36.dp)
        .verticalScroll(rememberScrollState())
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column {
          Text(
            text = "Host a Party 🎪",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = CartoonBorder
          )
          Text(
            text = "Spread the vibe and get the party started!",
            fontSize = 13.sp,
            color = Color(0xFF64748B)
          )
        }
        IconButton(
          onClick = onDismiss,
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Color(0xFFF1F5F9))
        ) {
          Icon(Icons.Default.Close, contentDescription = "Close", tint = CartoonBorder)
        }
      }

      Spacer(Modifier.height(16.dp))

      // 1. Party Name
      Text("Party Name *", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CartoonBorder)
      Spacer(Modifier.height(6.dp))
      OutlinedTextField(
        value = name,
        onValueChange = { name = it },
        placeholder = { Text("e.g. Bandra Sunset Jam 🪩") },
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = PartyPurple,
          unfocusedBorderColor = Color(0xFFCBD5E1)
        ),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("party_name_input")
      )

      Spacer(Modifier.height(16.dp))

      // 2. Party Type Toggle: Public vs Private
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text("Party Type *", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CartoonBorder)
          IconButton(
            onClick = { showTypeTooltip = !showTypeTooltip },
            modifier = Modifier.size(24.dp)
          ) {
            Icon(Icons.Default.Info, contentDescription = "Info", tint = PartyPurple, modifier = Modifier.size(16.dp))
          }
        }
      }

      // Tooltip Card
      AnimatedVisibility(visible = showTypeTooltip) {
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = PartyPurpleLight,
          border = androidx.compose.foundation.BorderStroke(1.dp, PartyPurple),
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Text("🟢 PUBLIC: Anyone on the map can join instantly without approval.", fontSize = 12.sp, color = CartoonBorder, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(4.dp))
            Text("🔒 PRIVATE: Guests must send a join request. You approve who gets the address.", fontSize = 12.sp, color = CartoonBorder, fontWeight = FontWeight.Medium)
          }
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Public Option Card
        Surface(
          shape = RoundedCornerShape(18.dp),
          color = if (partyType == PartyType.PUBLIC) PartyGreenLight else Color(0xFFF8FAFC),
          border = androidx.compose.foundation.BorderStroke(
            width = if (partyType == PartyType.PUBLIC) 2.5.dp else 1.dp,
            color = if (partyType == PartyType.PUBLIC) PartyGreen else Color(0xFFE2E8F0)
          ),
          modifier = Modifier
            .weight(1f)
            .clickable { partyType = PartyType.PUBLIC }
            .testTag("select_public_type")
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(Icons.Default.Public, contentDescription = null, tint = PartyGreen)
            Column {
              Text("Public 🎉", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CartoonBorder)
              Text("Open for all", fontSize = 11.sp, color = Color(0xFF64748B))
            }
          }
        }

        // Private Option Card
        Surface(
          shape = RoundedCornerShape(18.dp),
          color = if (partyType == PartyType.PRIVATE) PartyPurpleLight else Color(0xFFF8FAFC),
          border = androidx.compose.foundation.BorderStroke(
            width = if (partyType == PartyType.PRIVATE) 2.5.dp else 1.dp,
            color = if (partyType == PartyType.PRIVATE) PartyPurple else Color(0xFFE2E8F0)
          ),
          modifier = Modifier
            .weight(1f)
            .clickable { partyType = PartyType.PRIVATE }
            .testTag("select_private_type")
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(Icons.Default.Lock, contentDescription = null, tint = PartyPurple)
            Column {
              Text("Private 🔒", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CartoonBorder)
              Text("Request to join", fontSize = 11.sp, color = Color(0xFF64748B))
            }
          }
        }
      }

      Spacer(Modifier.height(16.dp))

      // 3. Emoji Mascot Picker
      Text("Pick Cartoon Emoji Mascot", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CartoonBorder)
      Spacer(Modifier.height(8.dp))
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        items(emojiOptions) { emoji ->
          val isSelected = selectedEmoji == emoji
          Box(
            modifier = Modifier
              .size(46.dp)
              .clip(CircleShape)
              .background(if (isSelected) PartyPinkLight else Color(0xFFF1F5F9))
              .border(
                width = if (isSelected) 2.5.dp else 1.dp,
                color = if (isSelected) PartyPink else Color(0xFFCBD5E1),
                shape = CircleShape
              )
              .clickable { selectedEmoji = emoji },
            contentAlignment = Alignment.Center
          ) {
            Text(emoji, fontSize = 24.sp)
          }
        }
      }

      Spacer(Modifier.height(16.dp))

      // 4. Vibe Tag Picker
      Text("Music / Vibe Tag *", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CartoonBorder)
      Spacer(Modifier.height(8.dp))
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        vibeOptions.forEach { vibe ->
          val isSelected = selectedVibe == vibe
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = if (isSelected) PartyPurple else Color(0xFFF1F5F9),
            modifier = Modifier.clickable { selectedVibe = vibe }
          ) {
            Text(
              text = vibe,
              color = if (isSelected) Color.White else CartoonBorder,
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
          }
        }
      }

      Spacer(Modifier.height(16.dp))

      // 5. Map Location Card & Pick on Map Button
      Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFFF8FAFC),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text("Party Location", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CartoonBorder)
            Text(
              text = "Pinned on Cartoon Map (${String.format("%.2f", currentPickedLocation.first)}, ${String.format("%.2f", currentPickedLocation.second)})",
              fontSize = 11.sp,
              color = Color(0xFF64748B)
            )
          }

          Button(
            onClick = onPickLocationOnMap,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PartyPurpleLight, contentColor = PartyPurple),
            modifier = Modifier.testTag("pick_location_button")
          ) {
            Text("📍 Reposition Pin", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
        }
      }

      Spacer(Modifier.height(12.dp))

      // Address text
      OutlinedTextField(
        value = address,
        onValueChange = { address = it },
        label = { Text("Venue / Landmark Address") },
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = PartyPurple,
          unfocusedBorderColor = Color(0xFFCBD5E1)
        ),
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(Modifier.height(14.dp))

      // 6. Date & Time + Live Now Switch
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        OutlinedTextField(
          value = startTime,
          onValueChange = { startTime = it },
          label = { Text("Date & Time") },
          shape = RoundedCornerShape(16.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PartyPurple,
            unfocusedBorderColor = Color(0xFFCBD5E1)
          ),
          modifier = Modifier.weight(1f)
        )

        Surface(
          shape = RoundedCornerShape(16.dp),
          color = if (isLiveNow) PartyPinkLight else Color(0xFFF1F5F9),
          modifier = Modifier.padding(top = 8.dp)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("🔥 Live Now", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isLiveNow) PartyPink else CartoonBorder)
            Spacer(Modifier.width(4.dp))
            Switch(
              checked = isLiveNow,
              onCheckedChange = { isLiveNow = it },
              colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = PartyPink
              )
            )
          }
        }
      }

      Spacer(Modifier.height(14.dp))

      // 7. Max Guests & Description
      OutlinedTextField(
        value = maxGuestsText,
        onValueChange = { maxGuestsText = it.filter { ch -> ch.isDigit() } },
        label = { Text("Max Guests (optional)") },
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = PartyPurple,
          unfocusedBorderColor = Color(0xFFCBD5E1)
        ),
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(Modifier.height(14.dp))

      OutlinedTextField(
        value = description,
        onValueChange = { description = it },
        label = { Text("Party Description / Entry Rules") },
        placeholder = { Text("Dhoom machale! BYOB, rooftop pool, and live DJ sets...") },
        shape = RoundedCornerShape(16.dp),
        minLines = 3,
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = PartyPurple,
          unfocusedBorderColor = Color(0xFFCBD5E1)
        ),
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(Modifier.height(24.dp))

      // 8. Submit Button
      Button(
        onClick = {
          if (name.isBlank()) return@Button
          val newParty = Party(
            id = "party_" + UUID.randomUUID().toString().take(8),
            name = name.trim(),
            type = partyType,
            isLiveNow = isLiveNow,
            emoji = selectedEmoji,
            vibeTag = selectedVibe,
            hostName = "You (Host)",
            hostAvatarEmoji = selectedEmoji,
            address = address.ifBlank { "Bandra West, Mumbai" },
            startTime = startTime.ifBlank { "Tonight @ 9:00 PM" },
            attendeeCount = 1,
            maxGuests = maxGuestsText.toIntOrNull() ?: 50,
            description = description.ifBlank { "Party ho rahi hai! Join the vibe and have a blast with us." },
            mapX = currentPickedLocation.first,
            mapY = currentPickedLocation.second,
            isJoined = true,
            isHostedByMe = true
          )
          onPartyCreated(newParty)
        },
        enabled = name.isNotBlank(),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = if (partyType == PartyType.PUBLIC) PartyGreen else PartyPurple,
          contentColor = Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(6.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(56.dp)
          .testTag("submit_party_button")
      ) {
        Text("🚀 Launch Party On Map!", fontWeight = FontWeight.Bold, fontSize = 16.sp)
      }
    }
  }
}
