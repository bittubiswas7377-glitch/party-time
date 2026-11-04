package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.ui.components.CartoonMapView
import com.example.ui.components.HostPartyDialog
import com.example.ui.components.PartyBottomSheet
import com.example.ui.theme.CartoonBorder
import com.example.ui.theme.PartyCyan
import com.example.ui.theme.PartyGreen
import com.example.ui.theme.PartyOrange
import com.example.ui.theme.PartyPink
import com.example.ui.theme.PartyPurple
import com.example.ui.theme.PartyPurpleLight
import com.example.ui.theme.PartyYellow

@Composable
fun MapScreen(
  parties: List<Party>,
  onJoinParty: (String) -> Unit,
  onRequestJoinParty: (String) -> Unit,
  onLeaveParty: (String) -> Unit,
  onHostParty: (Party) -> Unit,
  onOpenChat: (Party) -> Unit,
  onShowMessage: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  var searchQuery by remember { mutableStateOf("") }
  var selectedFilter by remember { mutableStateOf("All") }
  var selectedParty by remember { mutableStateOf<Party?>(null) }
  var showHostDialog by remember { mutableStateOf(false) }
  var isPickLocationMode by remember { mutableStateOf(false) }
  var pickedLocation by remember { mutableStateOf(Pair(0.5f, 0.5f)) }

  // Filter chips list
  val filterChips = listOf("All", "🟢 Public", "🔒 Private", "🔥 Live Now", "Bollywood", "EDM", "Chill", "House Party", "Rooftop")

  // Filter parties based on search & chip
  val filteredParties = remember(parties, searchQuery, selectedFilter) {
    parties.filter { party ->
      val matchesSearch = searchQuery.isBlank() ||
        party.name.contains(searchQuery, ignoreCase = true) ||
        party.vibeTag.contains(searchQuery, ignoreCase = true) ||
        party.address.contains(searchQuery, ignoreCase = true) ||
        party.hostName.contains(searchQuery, ignoreCase = true)

      val matchesChip = when (selectedFilter) {
        "All" -> true
        "🟢 Public" -> party.type == PartyType.PUBLIC
        "🔒 Private" -> party.type == PartyType.PRIVATE
        "🔥 Live Now" -> party.isLiveNow
        else -> party.vibeTag.equals(selectedFilter, ignoreCase = true)
      }

      matchesSearch && matchesChip
    }
  }

  Box(modifier = modifier.fillMaxSize()) {
    // 1. Full-screen Cartoon Map
    CartoonMapView(
      parties = filteredParties,
      selectedParty = selectedParty,
      onPartySelected = { party ->
        selectedParty = party
      },
      isPickLocationMode = isPickLocationMode,
      pickedLocation = pickedLocation,
      onLocationPicked = { x, y ->
        pickedLocation = Pair(x, y)
        isPickLocationMode = false
        showHostDialog = true
        onShowMessage("Pinned spot on map! 📍")
      }
    )

    // 2. Top Bar: Search Bar & Filter Chips
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.TopCenter)
        .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
      // Search Bar
      Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        shadowElevation = 6.dp,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.Search, contentDescription = "Search", tint = PartyPurple)
          Spacer(Modifier.width(8.dp))
          OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search parties, vibes, DJs...", fontSize = 14.sp) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = Color.Transparent,
              unfocusedBorderColor = Color.Transparent
            ),
            modifier = Modifier
              .weight(1f)
              .testTag("search_parties_input")
          )
          if (searchQuery.isNotEmpty()) {
            IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(28.dp)) {
              Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.Gray)
            }
          }
        }
      }

      Spacer(Modifier.height(8.dp))

      // Horizontal Filter Chips
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        filterChips.forEach { chip ->
          val isSelected = selectedFilter == chip
          Surface(
            shape = RoundedCornerShape(18.dp),
            color = if (isSelected) PartyPurple else Color.White.copy(alpha = 0.95f),
            border = androidx.compose.foundation.BorderStroke(
              width = 1.5.dp,
              color = if (isSelected) PartyPurple else Color(0xFFE2E8F0)
            ),
            shadowElevation = 3.dp,
            modifier = Modifier
              .clip(RoundedCornerShape(18.dp))
              .clickable { selectedFilter = chip }
              .testTag("filter_chip_$chip")
          ) {
            Text(
              text = chip,
              color = if (isSelected) Color.White else CartoonBorder,
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
            )
          }
        }
      }
    }

    // Empty state overlay on map
    if (filteredParties.isEmpty() && !isPickLocationMode) {
      Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White.copy(alpha = 0.95f),
        border = androidx.compose.foundation.BorderStroke(2.dp, PartyPurple),
        shadowElevation = 8.dp,
        modifier = Modifier
          .align(Alignment.Center)
          .padding(horizontal = 32.dp)
      ) {
        Column(
          modifier = Modifier.padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text("🗺️", fontSize = 40.sp)
          Spacer(Modifier.height(8.dp))
          Text(
            text = if (searchQuery.isNotBlank() || selectedFilter != "All") "No matching parties found" else "No Live Parties Yet!",
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            color = CartoonBorder
          )
          Spacer(Modifier.height(4.dp))
          Text(
            text = if (searchQuery.isNotBlank() || selectedFilter != "All") "Try clearing your search or filter chips." else "Be the first legend to host a party in your area! Drop your pin on the map.",
            fontSize = 12.sp,
            color = Color(0xFF64748B),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
          )
          Spacer(Modifier.height(14.dp))
          Button(
            onClick = { showHostDialog = true },
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PartyPink)
          ) {
            Text("Host First Party 🎪", fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    // 3. Floating Action Button: "+ Host a Party" at bottom-right
    if (!isPickLocationMode) {
      ExtendedFloatingActionButton(
        onClick = { showHostDialog = true },
        icon = { Icon(Icons.Default.Add, contentDescription = null, tint = Color.White) },
        text = { Text("Host a Party 🎪", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White) },
        shape = RoundedCornerShape(20.dp),
        containerColor = PartyPink,
        elevation = FloatingActionButtonDefaults.elevation(8.dp),
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .padding(end = 20.dp, bottom = 24.dp)
          .testTag("host_party_fab")
      )
    } else {
      // Pick Location Mode Helper Banner
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = PartyPurple,
        shadowElevation = 8.dp,
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .padding(bottom = 24.dp)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("📍 Tap anywhere on map to drop pin!", color = Color.White, fontWeight = FontWeight.Bold)
          Spacer(Modifier.width(12.dp))
          Surface(
            shape = CircleShape,
            color = Color.White,
            modifier = Modifier
              .clickable {
                isPickLocationMode = false
                showHostDialog = true
              }
              .padding(horizontal = 10.dp, vertical = 4.dp)
          ) {
            Text("Done", color = PartyPurple, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
          }
        }
      }
    }

    // 4. Party Details Bottom Sheet
    selectedParty?.let { party ->
      // Refresh party reference from the updated parties list
      val freshParty = parties.find { it.id == party.id } ?: party
      PartyBottomSheet(
        party = freshParty,
        onDismiss = { selectedParty = null },
        onJoinClick = {
          onJoinParty(party.id)
          onShowMessage("Dhoom machale! You joined the party! 🎉🥳")
          selectedParty = freshParty.copy(isJoined = true, attendeeCount = freshParty.attendeeCount + 1)
        },
        onRequestJoinClick = {
          onRequestJoinParty(party.id)
          onShowMessage("Request sent to host! 🔒 Check My Parties for status.")
          selectedParty = freshParty.copy(isRequestPending = true)
        },
        onLeaveClick = {
          onLeaveParty(party.id)
          onShowMessage("Left the party. See you at the next one! 👋")
          selectedParty = freshParty.copy(isJoined = false, attendeeCount = maxOf(0, freshParty.attendeeCount - 1))
        },
        onOpenChat = {
          selectedParty = null
          onOpenChat(freshParty)
        }
      )
    }

    // 5. Host Party Dialog
    if (showHostDialog) {
      HostPartyDialog(
        onDismiss = { showHostDialog = false },
        currentPickedLocation = pickedLocation,
        onPickLocationOnMap = {
          showHostDialog = false
          isPickLocationMode = true
          onShowMessage("Tap anywhere on the cartoon map to place pin 📍")
        },
        onPartyCreated = { newParty ->
          showHostDialog = false
          onHostParty(newParty)
          selectedParty = newParty
          onShowMessage("Party is LIVE on the map! Dhoom machao! 🚀🎉")
        }
      )
    }
  }
}
