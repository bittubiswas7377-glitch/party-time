package com.example.ui.components

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartyBottomSheet(
  party: Party,
  onDismiss: () -> Unit,
  onJoinClick: () -> Unit,
  onRequestJoinClick: () -> Unit,
  onLeaveClick: () -> Unit,
  onOpenChat: () -> Unit,
  modifier: Modifier = Modifier
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val context = LocalContext.current
  val isPublic = party.type == PartyType.PUBLIC

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = Color.White,
    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
    dragHandle = {
      Box(
        modifier = Modifier
          .padding(top = 12.dp, bottom = 4.dp)
          .size(width = 48.dp, height = 5.dp)
          .clip(CircleShape)
          .background(Color(0xFFCBD5E1))
      )
    },
    modifier = modifier.testTag("party_bottom_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp)
        .padding(bottom = 32.dp)
    ) {
      // 1. Colorful Banner Header with Emoji Mascot & Close Button
      val bannerGradient = if (isPublic) {
        listOf(PartyYellowLight, PartyPinkLight)
      } else {
        listOf(PartyPurpleLight, PartyPinkLight)
      }

      Surface(
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, if (isPublic) PartyYellow else PartyPurple),
        modifier = Modifier.fillMaxWidth()
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(Brush.horizontalGradient(bannerGradient))
            .padding(16.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
              // Big cartoon emoji bubble
              Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(2.dp, if (isPublic) PartyGreen else PartyPurple),
                shadowElevation = 4.dp,
                modifier = Modifier.size(62.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Text(text = party.emoji, fontSize = 34.sp)
                }
              }

              Column {
                // Public / Private Badge + Live pill
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isPublic) PartyGreen else PartyPurple,
                    shadowElevation = 2.dp
                  ) {
                    Row(
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                      if (!isPublic) {
                        Icon(
                          imageVector = Icons.Default.Lock,
                          contentDescription = null,
                          tint = Color.White,
                          modifier = Modifier.size(11.dp)
                        )
                      }
                      Text(
                        text = if (isPublic) "PUBLIC PARTY 🎉" else "PRIVATE PARTY 🔒",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp
                      )
                    }
                  }

                  if (party.isLiveNow) {
                    Surface(
                      shape = RoundedCornerShape(12.dp),
                      color = PartyPink,
                      shadowElevation = 2.dp
                    ) {
                      Text(
                        text = "🔥 LIVE NOW",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                      )
                    }
                  }
                }

                Spacer(Modifier.height(4.dp))
                // Vibe Tag
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = Color.White.copy(alpha = 0.9f)
                ) {
                  Text(
                    text = "🎵 ${party.vibeTag} Vibe",
                    color = CartoonBorder,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                  )
                }
              }
            }

            IconButton(
              onClick = onDismiss,
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.8f))
            ) {
              Icon(Icons.Default.Close, contentDescription = "Close", tint = CartoonBorder)
            }
          }
        }
      }

      Spacer(Modifier.height(16.dp))

      // 2. Party Title
      Text(
        text = party.name,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        color = CartoonBorder
      )

      Spacer(Modifier.height(10.dp))

      // 3. Host info pill
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(16.dp))
          .background(Color(0xFFF8FAFC))
          .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Surface(
            shape = CircleShape,
            color = PartyPurpleLight,
            modifier = Modifier.size(36.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Text(text = party.hostAvatarEmoji, fontSize = 20.sp)
            }
          }
          Column {
            Text(
              text = "Hosted by ${party.hostName}",
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
              color = CartoonBorder
            )
            Text(
              text = if (party.isHostedByMe) "⭐ You are the host!" else "Top Verified Host 🏆",
              fontSize = 11.sp,
              color = if (party.isHostedByMe) PartyPink else Color(0xFF64748B)
            )
          }
        }

        // Share button
        OutlinedButton(
          onClick = {
            val sendIntent = Intent().apply {
              action = Intent.ACTION_SEND
              putExtra(Intent.EXTRA_TEXT, "Party ho rahi hai! Join me at '${party.name}' on PartyMap! 🗺️🎉")
              type = "text/plain"
            }
            context.startActivity(Intent.createChooser(sendIntent, "Share Party Vibe"))
          },
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = PartyPurple),
          modifier = Modifier.testTag("share_party_button")
        ) {
          Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
          Spacer(Modifier.width(4.dp))
          Text("Invite", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
      }

      Spacer(Modifier.height(14.dp))

      // 4. Meta row: Location, Time & Attendees
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Location card
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = Color(0xFFF1F5F9),
          modifier = Modifier.weight(1f)
        ) {
          Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.Place, contentDescription = null, tint = PartyPink, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Column {
              Text("Where", fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
              Text(party.address, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CartoonBorder, maxLines = 1)
            }
          }
        }

        // Time card
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = Color(0xFFF1F5F9),
          modifier = Modifier.weight(1f)
        ) {
          Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.Schedule, contentDescription = null, tint = PartyPurple, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Column {
              Text("When", fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
              Text(party.startTime, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CartoonBorder, maxLines = 1)
            }
          }
        }
      }

      Spacer(Modifier.height(12.dp))

      // 5. Live Attendees row with animated avatar heads
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isPublic) PartyGreenLight else PartyPurpleLight,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.People, contentDescription = null, tint = if (isPublic) PartyGreen else PartyPurple)
            Spacer(Modifier.width(8.dp))
            Text(
              text = "${party.attendeeCount} Party Animals Going!",
              fontWeight = FontWeight.ExtraBold,
              fontSize = 13.sp,
              color = CartoonBorder
            )
          }

          // Cute mini avatar bubbles
          Row(horizontalArrangement = Arrangement.spacedBy((-6).dp)) {
            listOf("🕺", "💃", "😎", "🥳").forEach { emoji ->
              Box(
                modifier = Modifier
                  .size(24.dp)
                  .clip(CircleShape)
                  .background(Color.White)
                  .border(1.5.dp, if (isPublic) PartyGreen else PartyPurple, CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Text(emoji, fontSize = 12.sp)
              }
            }
          }
        }
      }

      Spacer(Modifier.height(14.dp))

      // 6. Description with fun cartoon quote
      Text(
        text = "Vibe Check 👀",
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        color = Color(0xFF64748B)
      )
      Spacer(Modifier.height(4.dp))
      Text(
        text = party.description,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = CartoonBorder
      )

      Spacer(Modifier.height(16.dp))

      // Room Chat Quick Access Button
      Button(
        onClick = onOpenChat,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = PartyPurpleLight,
          contentColor = PartyPurple
        ),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, PartyPurple),
        elevation = ButtonDefaults.buttonElevation(2.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
          .testTag("open_party_room_chat_button")
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          Text("💬 Open Room Chat (Live Discussion)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
      }

      Spacer(Modifier.height(12.dp))

      // 7. Action Button
      when {
        party.isJoined -> {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(
              shape = RoundedCornerShape(16.dp),
              color = PartyGreenLight,
              border = androidx.compose.foundation.BorderStroke(1.5.dp, PartyGreen),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
              ) {
                Text("🥳 You're on the Guest List! Enjoy the Vibe!", fontWeight = FontWeight.Bold, color = PartyGreen, fontSize = 14.sp)
              }
            }

            OutlinedButton(
              onClick = onLeaveClick,
              shape = RoundedCornerShape(16.dp),
              colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE53E3E)),
              modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .testTag("leave_party_button")
            ) {
              Text("Leave Party 🏃‍♂️", fontWeight = FontWeight.Bold)
            }
          }
        }

        party.isRequestPending -> {
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = PartyYellowLight,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, PartyOrange),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center
            ) {
              Text("⏳ Request Pending! Host will approve soon.", fontWeight = FontWeight.Bold, color = Color(0xFFD97706), fontSize = 14.sp)
            }
          }
        }

        isPublic -> {
          Button(
            onClick = onJoinClick,
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = PartyGreen,
              contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(6.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(56.dp)
              .testTag("join_party_button")
          ) {
            Text("Join Party Instant 🎉 (Dhoom Machale!)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
          }
        }

        else -> {
          // Private party
          Button(
            onClick = onRequestJoinClick,
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = PartyPurple,
              contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(6.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(56.dp)
              .testTag("request_join_party_button")
          ) {
            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Request to Join 🔒 (Private Access)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
          }
        }
      }
    }
  }
}
