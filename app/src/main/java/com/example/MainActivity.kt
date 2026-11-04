/**
 * =========================================================================================
 * PARTYMAP - LIVE CARTOON PARTY MAP FOR ANDROID
 * =========================================================================================
 * HOW TO ADD GOOGLE MAPS API KEY:
 * 1. Open the Secrets panel in AI Studio (or configure GOOGLE_MAPS_API_KEY in .env).
 * 2. In Google Cloud Console (https://console.cloud.google.com/):
 *    - Enable "Maps SDK for Android" (and "Places API").
 *    - Create an API key under APIs & Services > Credentials.
 *    - Add Android app restrictions with package name "com.aistudio.partymap.kpxqza".
 * 3. Add to Secrets Panel:
 *    GOOGLE_MAPS_API_KEY=AIzaSy...
 *
 * FALLBACK:
 * If the API key is not present or offline, PartyMap runs seamlessly on our vibrant,
 * interactive 2D Cartoon Canvas Map with animated live pulses, custom pins, radar sweeps,
 * bouncy cards, and instant public joins!
 * =========================================================================================
 */
package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.PartyRoomChatBottomSheet
import com.example.ui.screens.MapScreen
import com.example.ui.screens.MyPartiesScreen
import com.example.ui.screens.OnboardingSplashScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.theme.CartoonBorder
import com.example.ui.theme.PartyMapTheme
import com.example.ui.theme.PartyPink
import com.example.ui.theme.PartyPurple
import com.example.ui.theme.PartyPurpleLight
import com.example.ui.theme.PartyYellow
import com.example.viewmodel.PartyViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      PartyMapTheme {
        PartyMapApp()
      }
    }
  }
}

@Composable
fun PartyMapApp(partyViewModel: PartyViewModel = viewModel()) {
  val parties by partyViewModel.parties.collectAsState()
  val userProfile by partyViewModel.userProfile.collectAsState()
  val currentTab by partyViewModel.currentTab.collectAsState()
  val showOnboarding by partyViewModel.showOnboarding.collectAsState()
  val partyChats by partyViewModel.partyChats.collectAsState()
  val cloudflareConfig by partyViewModel.cloudflareConfig.collectAsState()
  val activeChatParty by partyViewModel.activeChatParty.collectAsState()

  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  // Listen to toast messages from ViewModel
  LaunchedEffect(Unit) {
    partyViewModel.messages.collectLatest { msg ->
      snackbarHostState.showSnackbar(message = msg)
    }
  }

  // Handle system back navigation to return to Map tab
  BackHandler(enabled = !showOnboarding && currentTab != 0) {
    partyViewModel.selectTab(0)
  }

  if (showOnboarding) {
    OnboardingSplashScreen(
      onGetStarted = { partyViewModel.dismissOnboarding() },
      modifier = Modifier
        .fillMaxSize()
        .windowInsetsPadding(WindowInsets.statusBars)
        .windowInsetsPadding(WindowInsets.navigationBars)
    )
  } else {
    Scaffold(
      modifier = Modifier
        .fillMaxSize()
        .windowInsetsPadding(WindowInsets.statusBars),
      snackbarHost = {
        SnackbarHost(
          hostState = snackbarHostState,
          modifier = Modifier
            .padding(16.dp)
            .windowInsetsPadding(WindowInsets.navigationBars)
        ) { data ->
          Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFF1E1B4B),
            border = androidx.compose.foundation.BorderStroke(2.dp, PartyPink),
            shadowElevation = 8.dp
          ) {
            Text(
              text = data.visuals.message,
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
          }
        }
      },
      bottomBar = {
        Surface(
          shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
          color = Color.White,
          shadowElevation = 12.dp,
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
          modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
        ) {
          NavigationBar(
            containerColor = Color.White,
            contentColor = PartyPurple,
            tonalElevation = 0.dp
          ) {
            // Tab 0: Map
            NavigationBarItem(
              selected = currentTab == 0,
              onClick = { partyViewModel.selectTab(0) },
              icon = {
                Text(
                  text = "🗺️",
                  fontSize = if (currentTab == 0) 24.sp else 20.sp
                )
              },
              label = {
                Text(
                  text = "Map",
                  fontWeight = if (currentTab == 0) FontWeight.ExtraBold else FontWeight.Medium,
                  fontSize = 12.sp,
                  color = if (currentTab == 0) PartyPurple else CartoonBorder
                )
              },
              colors = NavigationBarItemDefaults.colors(
                indicatorColor = PartyPurpleLight,
                selectedIconColor = PartyPurple,
                unselectedIconColor = Color.Gray
              ),
              modifier = Modifier.testTag("nav_tab_map")
            )

            // Tab 1: My Parties
            val totalActive = parties.count { it.isJoined || it.isHostedByMe }
            NavigationBarItem(
              selected = currentTab == 1,
              onClick = { partyViewModel.selectTab(1) },
              icon = {
                Box {
                  Text(
                    text = "🎪",
                    fontSize = if (currentTab == 1) 24.sp else 20.sp
                  )
                  if (totalActive > 0) {
                    Box(
                      modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(PartyPink)
                    )
                  }
                }
              },
              label = {
                Text(
                  text = "My Parties",
                  fontWeight = if (currentTab == 1) FontWeight.ExtraBold else FontWeight.Medium,
                  fontSize = 12.sp,
                  color = if (currentTab == 1) PartyPurple else CartoonBorder
                )
              },
              colors = NavigationBarItemDefaults.colors(
                indicatorColor = PartyPurpleLight,
                selectedIconColor = PartyPurple,
                unselectedIconColor = Color.Gray
              ),
              modifier = Modifier.testTag("nav_tab_my_parties")
            )

            // Tab 2: Profile
            NavigationBarItem(
              selected = currentTab == 2,
              onClick = { partyViewModel.selectTab(2) },
              icon = {
                Text(
                  text = userProfile.avatarEmoji,
                  fontSize = if (currentTab == 2) 24.sp else 20.sp
                )
              },
              label = {
                Text(
                  text = "Profile",
                  fontWeight = if (currentTab == 2) FontWeight.ExtraBold else FontWeight.Medium,
                  fontSize = 12.sp,
                  color = if (currentTab == 2) PartyPurple else CartoonBorder
                )
              },
              colors = NavigationBarItemDefaults.colors(
                indicatorColor = PartyPurpleLight,
                selectedIconColor = PartyPurple,
                unselectedIconColor = Color.Gray
              ),
              modifier = Modifier.testTag("nav_tab_profile")
            )
          }
        }
      }
    ) { innerPadding ->
      AnimatedContent(
        targetState = currentTab,
        transitionSpec = {
          if (targetState > initialState) {
            slideInHorizontally { width -> width } + fadeIn() togetherWith
              slideOutHorizontally { width -> -width } + fadeOut()
          } else {
            slideInHorizontally { width -> -width } + fadeIn() togetherWith
              slideOutHorizontally { width -> width } + fadeOut()
          }
        },
        label = "tab_transition",
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
      ) { tabIndex ->
        when (tabIndex) {
          0 -> MapScreen(
            parties = parties,
            onJoinParty = { id -> partyViewModel.joinParty(id) },
            onRequestJoinParty = { id -> partyViewModel.requestJoinParty(id) },
            onLeaveParty = { id -> partyViewModel.leaveParty(id) },
            onHostParty = { party -> partyViewModel.hostParty(party) },
            onOpenChat = { party -> partyViewModel.openPartyChat(party) },
            onShowMessage = { msg -> partyViewModel.showMessage(msg) }
          )
          1 -> MyPartiesScreen(
            parties = parties,
            onLeaveParty = { id ->
              partyViewModel.leaveParty(id)
              partyViewModel.showMessage("Left party 👋")
            },
            onCancelRequest = { id ->
              partyViewModel.cancelRequest(id)
              partyViewModel.showMessage("Join request cancelled ❌")
            },
            onSelectParty = { _ ->
              partyViewModel.selectTab(0)
            },
            onOpenChat = { party ->
              partyViewModel.openPartyChat(party)
            },
            onNavigateToMap = {
              partyViewModel.selectTab(0)
            }
          )
          2 -> ProfileScreen(
            profile = userProfile,
            cloudflareConfig = cloudflareConfig,
            onUpdateProfile = { name, avatar, bio ->
              partyViewModel.updateProfile(name, avatar, bio)
            },
            onUpdateCloudflareUrl = { url ->
              partyViewModel.updateCloudflareUrl(url)
            },
            onPingCloudflare = { cb ->
              partyViewModel.pingCloudflare(cb)
            },
            onResetDemoData = {
              partyViewModel.resetDemoData()
            },
            onShowMessage = { msg ->
              partyViewModel.showMessage(msg)
            }
          )
        }
      }
    }

    // Party Room Chat Bottom Sheet Modal
    activeChatParty?.let { party ->
      val messages = partyChats[party.id] ?: emptyList()
      PartyRoomChatBottomSheet(
        party = party,
        messages = messages,
        cloudflareConfig = cloudflareConfig,
        onSendMessage = { text ->
          partyViewModel.sendChatMessage(party.id, text)
        },
        onAddReaction = { messageId, emoji ->
          partyViewModel.addReaction(party.id, messageId, emoji)
        },
        onDismiss = {
          partyViewModel.closePartyChat()
        }
      )
    }
  }
}
