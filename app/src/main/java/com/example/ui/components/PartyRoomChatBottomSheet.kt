package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChatMessage
import com.example.model.CloudflareConfig
import com.example.model.Party
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartyRoomChatBottomSheet(
  party: Party,
  messages: List<ChatMessage>,
  cloudflareConfig: CloudflareConfig,
  onSendMessage: (String) -> Unit,
  onAddReaction: (messageId: String, emoji: String) -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  var textInput by remember { mutableStateOf("") }
  val listState = rememberLazyListState()
  val scope = rememberCoroutineScope()

  // Floating animation for live vibe
  val infiniteTransition = rememberInfiniteTransition(label = "chatPulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 0.95f,
    targetValue = 1.05f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "vibePulse"
  )

  val quickReplies = listOf(
    "Dhoom machale! 🪩",
    "Party ho rahi hai! 🎉",
    "Where is the DJ? 🎧",
    "Drinks on terrace! 🍸",
    "Vibe check 10/10 🔥",
    "Bass is crazy 🔊",
    "Just walked in! 🕺"
  )

  val vibeReactions = listOf("🔥", "🍻", "💃", "🪩", "🎉", "🍕", "🚀", "❤️")

  // Auto scroll to bottom when new message arrives
  LaunchedEffect(messages.size) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.size - 1)
    }
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = Color(0xFFFBF8FF),
    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
    modifier = modifier
      .fillMaxHeight(0.92f)
      .testTag("party_room_chat_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(bottom = 16.dp)
    ) {
      // 1. Header: Party info + Cloudflare status + Close button
      Surface(
        color = Color.White,
        shadowElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Surface(
                shape = CircleShape,
                color = PartyPurpleLight,
                border = androidx.compose.foundation.BorderStroke(2.dp, PartyPurple),
                modifier = Modifier.size(46.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Text(party.emoji, fontSize = 24.sp)
                }
              }

              Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = party.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = CartoonBorder,
                    maxLines = 1
                  )
                }
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Box(
                    modifier = Modifier
                      .size(8.dp)
                      .clip(CircleShape)
                      .background(PartyGreen)
                  )
                  Text(
                    text = "${party.attendeeCount} in room chat",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.SemiBold
                  )
                }
              }
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

          Spacer(Modifier.height(8.dp))

          // Cloudflare Sync Banner
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFF3E8FF),
            border = androidx.compose.foundation.BorderStroke(1.dp, PartyPurple.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.ElectricBolt,
                  contentDescription = "Cloudflare",
                  tint = PartyOrange,
                  modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                  text = "Cloudflare Worker Backend",
                  fontWeight = FontWeight.Bold,
                  fontSize = 11.sp,
                  color = PartyPurple
                )
              }
              Text(
                text = "⚡ Edge Realtime",
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                color = PartyGreen
              )
            }
          }
        }
      }

      // 2. Quick Vibe Reaction Tapper
      Surface(
        color = Color(0xFFFAF5FF),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("Vibe Blast:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PartyPurple)
          vibeReactions.forEach { emoji ->
            Surface(
              shape = CircleShape,
              color = Color.White,
              border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE9D5FF)),
              shadowElevation = 1.dp,
              modifier = Modifier
                .size(36.dp)
                .clickable {
                  // React to latest message or send quick vibe message
                  if (messages.isNotEmpty()) {
                    onAddReaction(messages.last().id, emoji)
                  } else {
                    onSendMessage(emoji)
                  }
                }
            ) {
              Box(contentAlignment = Alignment.Center) {
                Text(emoji, fontSize = 18.sp)
              }
            }
          }
        }
      }

      // 3. Messages List
      LazyColumn(
        state = listState,
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
          .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        item {
          Spacer(Modifier.height(10.dp))
          // Friendly cartoon notice
          Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
          ) {
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = Color.White.copy(alpha = 0.9f),
              border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
              Text(
                text = "🔒 Live Chat Room for ${party.name} • Be nice & party hard! 🥳",
                fontSize = 11.sp,
                color = Color(0xFF64748B),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
              )
            }
          }
        }

        if (messages.isEmpty()) {
          item {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, bottom = 20.dp),
              contentAlignment = Alignment.Center
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                  shape = CircleShape,
                  color = PartyPurpleLight,
                  modifier = Modifier.size(64.dp)
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Text("💬", fontSize = 30.sp)
                  }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                  text = "No messages in this party room yet!",
                  fontWeight = FontWeight.Bold,
                  fontSize = 15.sp,
                  color = CartoonBorder
                )
                Spacer(Modifier.height(4.dp))
                Text(
                  text = "Break the ice! Drop a quick reply or tap an emoji vibe above. 🎉",
                  fontSize = 12.sp,
                  color = Color(0xFF64748B),
                  textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
              }
            }
          }
        }

        items(messages, key = { it.id }) { msg ->
          ChatBubbleItem(
            message = msg,
            onAddReaction = { emoji -> onAddReaction(msg.id, emoji) }
          )
        }

        item {
          Spacer(Modifier.height(8.dp))
        }
      }

      // 4. Quick Replies Row
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState())
          .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        quickReplies.forEach { reply ->
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
            modifier = Modifier.clickable {
              onSendMessage(reply)
            }
          ) {
            Text(
              text = reply,
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              color = CartoonBorder,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
            )
          }
        }
      }

      // 5. Message Input Bar
      Surface(
        color = Color.White,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = textInput,
            onValueChange = { textInput = it },
            placeholder = { Text("Say something in the party room... 🎉", fontSize = 13.sp) },
            shape = RoundedCornerShape(22.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = PartyPurple,
              unfocusedBorderColor = Color(0xFFCBD5E1)
            ),
            singleLine = true,
            modifier = Modifier
              .weight(1f)
              .testTag("chat_input_field")
          )

          Surface(
            shape = CircleShape,
            color = if (textInput.isNotBlank()) PartyPurple else Color(0xFFE2E8F0),
            shadowElevation = if (textInput.isNotBlank()) 4.dp else 0.dp,
            modifier = Modifier
              .size(46.dp)
              .clickable(enabled = textInput.isNotBlank()) {
                val toSend = textInput.trim()
                if (toSend.isNotBlank()) {
                  textInput = ""
                  onSendMessage(toSend)
                }
              }
              .testTag("send_chat_message_button")
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Send",
                tint = if (textInput.isNotBlank()) Color.White else Color.Gray,
                modifier = Modifier.size(20.dp)
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun ChatBubbleItem(
  message: ChatMessage,
  onAddReaction: (String) -> Unit
) {
  val isMe = message.isMe
  val isHost = message.isHost

  val bubbleColor = when {
    isMe -> PartyPurple
    isHost -> Color(0xFFFFF7ED) // Warm peach for host
    else -> Color.White
  }

  val textColor = when {
    isMe -> Color.White
    else -> CartoonBorder
  }

  val alignment = if (isMe) Alignment.End else Alignment.Start

  Column(
    modifier = Modifier.fillMaxWidth(),
    horizontalAlignment = alignment
  ) {
    Row(
      verticalAlignment = Alignment.Top,
      horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
      modifier = Modifier.widthIn(max = 320.dp)
    ) {
      if (!isMe) {
        Surface(
          shape = CircleShape,
          color = if (isHost) PartyYellowLight else PartyPurpleLight,
          border = androidx.compose.foundation.BorderStroke(1.5.dp, if (isHost) PartyOrange else PartyPurple),
          modifier = Modifier.size(34.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Text(message.senderAvatar, fontSize = 18.sp)
          }
        }
        Spacer(Modifier.width(8.dp))
      }

      Column(horizontalAlignment = alignment) {
        // Name & Host Pill
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          if (!isMe) {
            Text(
              text = message.senderName,
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              color = CartoonBorder
            )
          }
          if (isHost) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = PartyOrange
            ) {
              Text(
                text = "👑 HOST",
                color = Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
              )
            }
          }
          Text(
            text = message.timestamp,
            fontSize = 10.sp,
            color = Color(0xFF94A3B8)
          )
        }

        Spacer(Modifier.height(2.dp))

        // Bubble
        Surface(
          shape = RoundedCornerShape(
            topStart = 18.dp,
            topEnd = 18.dp,
            bottomStart = if (isMe) 18.dp else 4.dp,
            bottomEnd = if (isMe) 4.dp else 18.dp
          ),
          color = bubbleColor,
          border = if (!isMe) androidx.compose.foundation.BorderStroke(1.5.dp, if (isHost) PartyOrange else Color(0xFFE2E8F0)) else null,
          shadowElevation = 2.dp,
          modifier = Modifier.widthIn(min = 60.dp, max = 280.dp)
        ) {
          Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(
              text = message.message,
              color = textColor,
              fontSize = 14.sp,
              lineHeight = 19.sp,
              fontWeight = FontWeight.Medium
            )
          }
        }

        // Reactions row
        if (message.reactions.isNotEmpty()) {
          Row(
            modifier = Modifier.padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            message.reactions.forEach { (emoji, count) ->
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.clickable { onAddReaction(emoji) }
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(emoji, fontSize = 11.sp)
                  Spacer(Modifier.width(2.dp))
                  Text(
                    text = "$count",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = CartoonBorder
                  )
                }
              }
            }
          }
        }
      }

      if (isMe) {
        Spacer(Modifier.width(8.dp))
        Surface(
          shape = CircleShape,
          color = PartyPurpleLight,
          border = androidx.compose.foundation.BorderStroke(1.5.dp, PartyPurple),
          modifier = Modifier.size(34.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Text(message.senderAvatar, fontSize = 18.sp)
          }
        }
      }
    }
  }
}
