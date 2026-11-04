package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CloudflareBackendService
import com.example.model.CloudflareConfig
import com.example.model.UserProfile
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
  profile: UserProfile,
  cloudflareConfig: CloudflareConfig,
  onUpdateProfile: (name: String, avatar: String, bio: String) -> Unit,
  onUpdateCloudflareUrl: (String) -> Unit,
  onPingCloudflare: ((Boolean, String) -> Unit) -> Unit,
  onResetDemoData: () -> Unit,
  onShowMessage: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  var showEditDialog by remember { mutableStateOf(false) }
  var showResetConfirm by remember { mutableStateOf(false) }
  var showCloudflareUrlDialog by remember { mutableStateOf(false) }
  var showWorkerCodeDialog by remember { mutableStateOf(false) }
  var isPinging by remember { mutableStateOf(false) }
  val context = LocalContext.current

  val badges = listOf(
    Pair("🔥 First on the Floor", "Joined 10+ parties within 10 mins"),
    Pair("🪩 Bollywood King", "Attended 5 rooftop Bollywood jams"),
    Pair("🦉 Night Owl Legend", "Still dancing past 3 AM"),
    Pair("🍕 Pizza Diplomat", "Fed the entire afterparty crowd"),
    Pair("🔒 VIP Clearance", "Approved for 5 secret speakeasies")
  )

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFFFAFAFE))
      .padding(horizontal = 16.dp)
      .verticalScroll(rememberScrollState())
  ) {
    Spacer(Modifier.height(16.dp))

    // Profile Top Card with Gradient & Mascot
    Surface(
      shape = RoundedCornerShape(28.dp),
      border = androidx.compose.foundation.BorderStroke(2.dp, PartyPurple),
      shadowElevation = 6.dp,
      modifier = Modifier.fillMaxWidth()
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(Brush.linearGradient(listOf(PartyPurpleLight, PartyPinkLight, PartyYellowLight)))
          .padding(20.dp)
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.fillMaxWidth()
        ) {
          // Cartoon Avatar
          Box(contentAlignment = Alignment.BottomEnd) {
            Surface(
              shape = CircleShape,
              color = Color.White,
              border = androidx.compose.foundation.BorderStroke(3.dp, PartyPurple),
              shadowElevation = 6.dp,
              modifier = Modifier.size(90.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Text(text = profile.avatarEmoji, fontSize = 50.sp)
              }
            }

            Surface(
              shape = CircleShape,
              color = PartyPink,
              border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.White),
              modifier = Modifier
                .size(28.dp)
                .clickable { showEditDialog = true }
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White, modifier = Modifier.size(14.dp))
              }
            }
          }

          Spacer(Modifier.height(12.dp))

          Text(
            text = profile.name,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = CartoonBorder
          )

          Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.padding(top = 4.dp)
          ) {
            Text(
              text = "👑 Certified Party Legend",
              color = PartyPurple,
              fontWeight = FontWeight.ExtraBold,
              fontSize = 12.sp,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
            )
          }

          Spacer(Modifier.height(8.dp))

          Text(
            text = profile.bio,
            fontSize = 13.sp,
            color = Color(0xFF475569),
            fontWeight = FontWeight.Medium
          )
        }
      }
    }

    Spacer(Modifier.height(18.dp))

    // Stats Grid: Parties Joined, Parties Hosted, Vibe Score
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      StatCard(
        title = "Joined",
        value = "${profile.joinedCount}",
        emoji = "🎪",
        color = PartyPink,
        modifier = Modifier.weight(1f)
      )
      StatCard(
        title = "Hosted",
        value = "${profile.hostedCount}",
        emoji = "🎤",
        color = PartyPurple,
        modifier = Modifier.weight(1f)
      )
      StatCard(
        title = "Vibe Score",
        value = "${profile.vibeScore}%",
        emoji = "🔥",
        color = PartyOrange,
        modifier = Modifier.weight(1.1f)
      )
    }

    Spacer(Modifier.height(20.dp))

    // CLOUDFLARE BACKEND INTEGRATION CARD
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = Color.White,
      border = androidx.compose.foundation.BorderStroke(2.dp, PartyOrange),
      shadowElevation = 4.dp,
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Surface(
              shape = CircleShape,
              color = Color(0xFFFFF7ED),
              modifier = Modifier.size(36.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Cloud, contentDescription = "Cloudflare", tint = PartyOrange, modifier = Modifier.size(20.dp))
              }
            }
            Column {
              Text("Cloudflare Edge Backend", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = CartoonBorder)
              Text("Workers & KV Realtime Sync", fontSize = 11.sp, color = Color(0xFF64748B))
            }
          }

          Surface(
            shape = RoundedCornerShape(12.dp),
            color = PartyGreenLight
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(PartyGreen))
              Spacer(Modifier.width(4.dp))
              Text("Active ⚡", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = PartyGreen)
            }
          }
        }

        Spacer(Modifier.height(12.dp))

        // Worker Endpoint Box
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = Color(0xFFF8FAFC),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text("Worker URL", fontSize = 10.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.SemiBold)
              Text(
                text = cloudflareConfig.workerUrl,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = PartyPurple,
                maxLines = 1
              )
            }
            IconButton(
              onClick = { showCloudflareUrlDialog = true },
              modifier = Modifier.size(28.dp)
            ) {
              Icon(Icons.Default.Edit, contentDescription = "Edit Worker URL", tint = PartyPurple, modifier = Modifier.size(16.dp))
            }
          }
        }

        Spacer(Modifier.height(12.dp))

        // Cloudflare Action Buttons
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedButton(
            onClick = {
              isPinging = true
              onPingCloudflare { success, detail ->
                isPinging = false
                onShowMessage(detail)
              }
            },
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = PartyOrange),
            modifier = Modifier.weight(1f).testTag("ping_cloudflare_button")
          ) {
            Icon(Icons.Default.ElectricBolt, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(if (isPinging) "Pinging..." else "Ping Edge ⚡", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }

          Button(
            onClick = { showWorkerCodeDialog = true },
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PartyPurple, contentColor = Color.White),
            modifier = Modifier.weight(1f).testTag("view_worker_code_button")
          ) {
            Text("Worker Code 📜", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    Spacer(Modifier.height(20.dp))

    // Badges Section
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text(
        text = "Party Badges 🏆",
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        color = CartoonBorder
      )
      Text(
        text = "5 Unlocked",
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = PartyPurple
      )
    }

    Spacer(Modifier.height(10.dp))

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
      badges.forEach { (title, subtitle) ->
        Surface(
          shape = RoundedCornerShape(18.dp),
          color = Color.White,
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
          shadowElevation = 2.dp,
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Surface(
              shape = CircleShape,
              color = PartyYellowLight,
              modifier = Modifier.size(40.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Text(title.take(2), fontSize = 20.sp)
              }
            }
            Spacer(Modifier.width(12.dp))
            Column {
              Text(
                text = title.substring(2).trim(),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = CartoonBorder
              )
              Text(
                text = subtitle,
                fontSize = 12.sp,
                color = Color(0xFF64748B)
              )
            }
          }
        }
      }
    }

    Spacer(Modifier.height(24.dp))

    // Settings & Reset Actions
    Text(
      text = "App Controls & Vibe Reset",
      fontWeight = FontWeight.Bold,
      fontSize = 16.sp,
      color = CartoonBorder
    )

    Spacer(Modifier.height(10.dp))

    OutlinedButton(
      onClick = { showResetConfirm = true },
      shape = RoundedCornerShape(16.dp),
      colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
      modifier = Modifier
        .fillMaxWidth()
        .height(48.dp)
        .testTag("reset_demo_data_button")
    ) {
      Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
      Spacer(Modifier.width(6.dp))
      Text("Clear All App Data & Reset", fontWeight = FontWeight.Bold)
    }

    Spacer(Modifier.height(28.dp))
  }

  // Edit Profile Dialog
  if (showEditDialog) {
    var editName by remember { mutableStateOf(profile.name) }
    var editBio by remember { mutableStateOf(profile.bio) }
    var editAvatar by remember { mutableStateOf(profile.avatarEmoji) }
    val avatarOptions = listOf("🕺", "💃", "😎", "🥳", "🦄", "🎧", "👾", "👑", "🔥", "🍕")

    AlertDialog(
      onDismissRequest = { showEditDialog = false },
      title = { Text("Edit Party Persona 🕺", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("Pick Mascot Avatar", fontSize = 12.sp, color = Color.Gray)
          LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(avatarOptions) { emoji ->
              Box(
                modifier = Modifier
                  .size(42.dp)
                  .clip(CircleShape)
                  .background(if (editAvatar == emoji) PartyPinkLight else Color(0xFFF1F5F9))
                  .border(
                    width = if (editAvatar == emoji) 2.dp else 1.dp,
                    color = if (editAvatar == emoji) PartyPink else Color(0xFFCBD5E1),
                    shape = CircleShape
                  )
                  .clickable { editAvatar = emoji },
                contentAlignment = Alignment.Center
              ) {
                Text(emoji, fontSize = 22.sp)
              }
            }
          }

          OutlinedTextField(
            value = editName,
            onValueChange = { editName = it },
            label = { Text("Display Name") },
            shape = RoundedCornerShape(12.dp)
          )

          OutlinedTextField(
            value = editBio,
            onValueChange = { editBio = it },
            label = { Text("Bio / Party Mantra") },
            shape = RoundedCornerShape(12.dp)
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (editName.isNotBlank()) {
              onUpdateProfile(editName.trim(), editAvatar, editBio.trim())
              showEditDialog = false
              onShowMessage("Persona updated! Looking fresh 🌟")
            }
          },
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = PartyPurple)
        ) {
          Text("Save")
        }
      },
      dismissButton = {
        TextButton(onClick = { showEditDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  // Edit Cloudflare Worker URL Dialog
  if (showCloudflareUrlDialog) {
    var newUrl by remember { mutableStateOf(cloudflareConfig.workerUrl) }
    AlertDialog(
      onDismissRequest = { showCloudflareUrlDialog = false },
      title = { Text("Cloudflare Worker Endpoint ⚡", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            "Enter your Cloudflare Worker URL (e.g., https://my-partymap.workers.dev).",
            fontSize = 12.sp,
            color = Color(0xFF64748B)
          )
          OutlinedTextField(
            value = newUrl,
            onValueChange = { newUrl = it },
            label = { Text("Worker URL") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (newUrl.isNotBlank()) {
              onUpdateCloudflareUrl(newUrl.trim())
              showCloudflareUrlDialog = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = PartyOrange)
        ) {
          Text("Save Endpoint")
        }
      },
      dismissButton = {
        TextButton(onClick = { showCloudflareUrlDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  // View Worker Code Template Dialog
  if (showWorkerCodeDialog) {
    val workerScript = CloudflareBackendService.CLOUDFLARE_WORKER_SCRIPT_TEMPLATE
    AlertDialog(
      onDismissRequest = { showWorkerCodeDialog = false },
      title = { Text("Cloudflare Worker Script 📜", fontWeight = FontWeight.Bold) },
      text = {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
          Text(
            "Deploy this script to Cloudflare Workers to power real-time room chats and party sync on the Cloudflare Edge network:",
            fontSize = 12.sp,
            color = Color(0xFF64748B)
          )
          Spacer(Modifier.height(8.dp))
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF1E1B4B),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = workerScript.trim(),
              color = Color(0xFFE2E8F0),
              fontSize = 10.sp,
              fontFamily = FontFamily.Monospace,
              modifier = Modifier.padding(10.dp)
            )
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("Cloudflare Worker Code", workerScript))
            onShowMessage("Cloudflare Worker script copied to clipboard! 📋")
            showWorkerCodeDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = PartyPurple)
        ) {
          Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
          Spacer(Modifier.width(4.dp))
          Text("Copy Code")
        }
      },
      dismissButton = {
        TextButton(onClick = { showWorkerCodeDialog = false }) {
          Text("Close")
        }
      }
    )
  }

  // Reset Confirmation Dialog
  if (showResetConfirm) {
    AlertDialog(
      onDismissRequest = { showResetConfirm = false },
      title = { Text("Clear All App Data?") },
      text = { Text("This will clear all hosted parties, joined parties, and room chats, resetting PartyMap to a clean state.") },
      confirmButton = {
        Button(
          onClick = {
            onResetDemoData()
            showResetConfirm = false
            onShowMessage("All party data cleared! Ready to host fresh parties 🎉")
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
        ) {
          Text("Clear Everything")
        }
      },
      dismissButton = {
        TextButton(onClick = { showResetConfirm = false }) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
private fun StatCard(
  title: String,
  value: String,
  emoji: String,
  color: Color,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(20.dp),
    color = Color.White,
    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFE2E8F0)),
    shadowElevation = 3.dp,
    modifier = modifier
  ) {
    Column(
      modifier = Modifier.padding(12.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(emoji, fontSize = 22.sp)
      Spacer(Modifier.height(4.dp))
      Text(
        text = value,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 18.sp,
        color = color
      )
      Text(
        text = title,
        fontSize = 11.sp,
        color = Color(0xFF64748B),
        fontWeight = FontWeight.Medium
      )
    }
  }
}
