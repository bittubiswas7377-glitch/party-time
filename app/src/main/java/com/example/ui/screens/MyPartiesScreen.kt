package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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

@Composable
fun MyPartiesScreen(
  parties: List<Party>,
  onLeaveParty: (String) -> Unit,
  onCancelRequest: (String) -> Unit,
  onSelectParty: (Party) -> Unit,
  onOpenChat: (Party) -> Unit,
  onNavigateToMap: () -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedTab by remember { mutableIntStateOf(0) }
  val context = LocalContext.current

  val joinedParties = parties.filter { it.isJoined && !it.isHostedByMe }
  val pendingParties = parties.filter { it.isRequestPending }
  val hostedParties = parties.filter { it.isHostedByMe }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFFFAFAFE))
      .padding(horizontal = 16.dp)
  ) {
    Spacer(Modifier.height(16.dp))

    // Header Title
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Column {
        Text(
          text = "My Parties 🎪",
          fontSize = 26.sp,
          fontWeight = FontWeight.Bold,
          color = CartoonBorder
        )
        Text(
          text = "Your party schedule & guest lists",
          fontSize = 13.sp,
          color = Color(0xFF64748B)
        )
      }

      Surface(
        shape = RoundedCornerShape(16.dp),
        color = PartyPinkLight,
        modifier = Modifier.padding(4.dp)
      ) {
        Text(
          text = "${joinedParties.size + hostedParties.size} Active",
          color = PartyPink,
          fontWeight = FontWeight.ExtraBold,
          fontSize = 12.sp,
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
      }
    }

    Spacer(Modifier.height(16.dp))

    // Chunky Cartoon Tabs: "Attending (X)" vs "Hosting (Y)" vs "Pending (Z)"
    TabRow(
      selectedTabIndex = selectedTab,
      containerColor = Color.White,
      contentColor = PartyPurple,
      indicator = { tabPositions ->
        TabRowDefaults.SecondaryIndicator(
          modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
          color = PartyPurple,
          height = 3.dp
        )
      },
      modifier = Modifier
        .clip(RoundedCornerShape(18.dp))
        .border(1.5.dp, Color(0xFFE2E8F0), RoundedCornerShape(18.dp))
    ) {
      Tab(
        selected = selectedTab == 0,
        onClick = { selectedTab = 0 },
        text = {
          Text(
            text = "Joined (${joinedParties.size})",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
          )
        }
      )
      Tab(
        selected = selectedTab == 1,
        onClick = { selectedTab = 1 },
        text = {
          Text(
            text = "Hosting (${hostedParties.size})",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
          )
        }
      )
      Tab(
        selected = selectedTab == 2,
        onClick = { selectedTab = 2 },
        text = {
          Text(
            text = "Pending (${pendingParties.size})",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
          )
        }
      )
    }

    Spacer(Modifier.height(16.dp))

    // List of Parties
    val currentList = when (selectedTab) {
      0 -> joinedParties
      1 -> hostedParties
      else -> pendingParties
    }

    if (currentList.isEmpty()) {
      // Empty State
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.padding(24.dp)
        ) {
          Surface(
            shape = CircleShape,
            color = PartyPurpleLight,
            modifier = Modifier.size(90.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Text(
                text = when (selectedTab) {
                  0 -> "🕺"
                  1 -> "🎪"
                  else -> "⏳"
                },
                fontSize = 44.sp
              )
            }
          }
          Spacer(Modifier.height(16.dp))
          Text(
            text = when (selectedTab) {
              0 -> "No Joined Parties Yet!"
              1 -> "You're Not Hosting Any Parties"
              else -> "No Pending Requests"
            },
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = CartoonBorder
          )
          Spacer(Modifier.height(6.dp))
          Text(
            text = when (selectedTab) {
              0 -> "The night is young! Explore the cartoon map and join open parties nearby."
              1 -> "Become the party legend! Tap '+ Host a Party' on the map to start your own."
              else -> "When you request entry to private parties, track host approval here."
            },
            fontSize = 13.sp,
            color = Color(0xFF64748B),
            modifier = Modifier.padding(horizontal = 24.dp),
            lineHeight = 18.sp
          )
          Spacer(Modifier.height(20.dp))
          Button(
            onClick = onNavigateToMap,
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PartyPurple),
            elevation = ButtonDefaults.buttonElevation(4.dp),
            modifier = Modifier.testTag("explore_parties_button")
          ) {
            Text("Find Parties on Map 🗺️", fontWeight = FontWeight.Bold)
          }
        }
      }
    } else {
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
      ) {
        items(currentList, key = { it.id }) { party ->
          PartyCardItem(
            party = party,
            isHostingTab = selectedTab == 1,
            isPendingTab = selectedTab == 2,
            onCardClick = { onSelectParty(party) },
            onOpenChat = { onOpenChat(party) },
            onLeaveClick = { onLeaveParty(party.id) },
            onCancelRequest = { onCancelRequest(party.id) },
            onShareClick = {
              val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "Party ho rahi hai! Let's go to '${party.name}' at ${party.address}! 🗺️🎉")
                type = "text/plain"
              }
              context.startActivity(Intent.createChooser(sendIntent, "Share with Friends"))
            }
          )
        }
        item {
          Spacer(Modifier.height(24.dp))
        }
      }
    }
  }
}

@Composable
private fun PartyCardItem(
  party: Party,
  isHostingTab: Boolean,
  isPendingTab: Boolean,
  onCardClick: () -> Unit,
  onOpenChat: () -> Unit,
  onLeaveClick: () -> Unit,
  onCancelRequest: () -> Unit,
  onShareClick: () -> Unit
) {
  val isPublic = party.type == PartyType.PUBLIC

  Surface(
    shape = RoundedCornerShape(22.dp),
    color = Color.White,
    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFE2E8F0)),
    shadowElevation = 4.dp,
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onCardClick() }
      .testTag("my_party_card_${party.id}")
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      // Top row: Mascot + Name + Status Badges
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          modifier = Modifier.weight(1f)
        ) {
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (isPublic) PartyGreenLight else PartyPurpleLight,
            modifier = Modifier.size(50.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Text(text = party.emoji, fontSize = 28.sp)
            }
          }

          Column {
            Text(
              text = party.name,
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp,
              color = CartoonBorder,
              maxLines = 1
            )
            Text(
              text = "🎵 ${party.vibeTag} • Hosted by ${party.hostName}",
              fontSize = 12.sp,
              color = Color(0xFF64748B)
            )
          }
        }

        // Live / Type Badge
        if (party.isLiveNow) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = PartyPink
          ) {
            Text(
              text = "🔥 LIVE",
              color = Color.White,
              fontWeight = FontWeight.ExtraBold,
              fontSize = 10.sp,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
          }
        }
      }

      Spacer(Modifier.height(12.dp))

      // Time & Address Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Schedule, contentDescription = null, tint = PartyPurple, modifier = Modifier.size(15.dp))
          Spacer(Modifier.width(4.dp))
          Text(party.startTime, fontSize = 12.sp, color = CartoonBorder, fontWeight = FontWeight.Medium)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Place, contentDescription = null, tint = PartyPink, modifier = Modifier.size(15.dp))
          Spacer(Modifier.width(4.dp))
          Text(party.address, fontSize = 12.sp, color = CartoonBorder, fontWeight = FontWeight.Medium, maxLines = 1)
        }
      }

      Spacer(Modifier.height(14.dp))

      // Action Buttons Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "${party.attendeeCount} going",
          fontWeight = FontWeight.Bold,
          fontSize = 12.sp,
          color = PartyGreen
        )

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          // Room Chat button
          Button(
            onClick = onOpenChat,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = PartyPurpleLight,
              contentColor = PartyPurple
            ),
            elevation = ButtonDefaults.buttonElevation(1.dp),
            modifier = Modifier.testTag("my_party_chat_btn_${party.id}")
          ) {
            Text("💬 Chat", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }

          OutlinedButton(
            onClick = onShareClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = PartyPurple)
          ) {
            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(3.dp))
            Text("Invite", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }

          if (isPendingTab) {
            OutlinedButton(
              onClick = onCancelRequest,
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
            ) {
              Text("Cancel Request", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          } else if (!isHostingTab) {
            OutlinedButton(
              onClick = onLeaveClick,
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
              modifier = Modifier.testTag("leave_button_${party.id}")
            ) {
              Text("Leave 🏃‍♂️", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          } else {
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = PartyPurpleLight
            ) {
              Text(
                text = "👑 Host Status: Active",
                color = PartyPurple,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
              )
            }
          }
        }
      }
    }
  }
}
