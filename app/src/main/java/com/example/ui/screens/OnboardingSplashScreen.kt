package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.CartoonBorder
import com.example.ui.theme.PartyCyan
import com.example.ui.theme.PartyGreen
import com.example.ui.theme.PartyOrange
import com.example.ui.theme.PartyPink
import com.example.ui.theme.PartyPinkLight
import com.example.ui.theme.PartyPurple
import com.example.ui.theme.PartyPurpleLight
import com.example.ui.theme.PartyYellow
import com.example.ui.theme.PartyYellowLight

@Composable
fun OnboardingSplashScreen(
  onGetStarted: () -> Unit,
  modifier: Modifier = Modifier
) {
  val locationPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    // Either way proceed to map
    onGetStarted()
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colors = listOf(
            Color(0xFFFBF8FF),
            PartyPurpleLight,
            PartyPinkLight,
            PartyYellowLight
          )
        )
      )
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 24.dp)
        .verticalScroll(rememberScrollState()),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
      ) {
        Spacer(Modifier.height(36.dp))

        // Cute Logo & App Name Badge
        Surface(
          shape = RoundedCornerShape(26.dp),
          color = Color.White,
          border = androidx.compose.foundation.BorderStroke(2.5.dp, PartyPurple),
          shadowElevation = 8.dp,
          modifier = Modifier.padding(bottom = 12.dp)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Text("🗺️", fontSize = 28.sp)
            Text(
              text = "PartyMap",
              fontSize = 26.sp,
              fontWeight = FontWeight.Bold,
              color = PartyPurple
            )
            Text("🎉", fontSize = 28.sp)
          }
        }

        Text(
          text = "Dhoom Machale! 🪩",
          fontSize = 18.sp,
          fontWeight = FontWeight.ExtraBold,
          color = PartyPink
        )

        Text(
          text = "Find live, crazy parties happening right around you on the cartoon map!",
          fontSize = 14.sp,
          textAlign = TextAlign.Center,
          color = Color(0xFF475569),
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )

        Spacer(Modifier.height(14.dp))

        // Hero Illustration Card
        Surface(
          shape = RoundedCornerShape(28.dp),
          border = androidx.compose.foundation.BorderStroke(3.dp, Color.White),
          shadowElevation = 10.dp,
          modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
        ) {
          Image(
            painter = painterResource(id = R.drawable.party_hero_banner_1793799566401),
            contentDescription = "Cartoon Party Hero",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
          )
        }

        Spacer(Modifier.height(18.dp))

        // Feature Highlights in Cartoon Badges
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          FeaturePill("🟢 Instant Join", "No waiting", PartyGreen, Modifier.weight(1f))
          FeaturePill("🔒 Secret Speakeasies", "VIP requests", PartyPurple, Modifier.weight(1f))
          FeaturePill("🔥 Live Vibes", "Pulsing map", PartyOrange, Modifier.weight(1f))
        }
      }

      // Bottom Call to Action
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 36.dp, top = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Button(
          onClick = {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
          },
          shape = RoundedCornerShape(24.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = PartyPink,
            contentColor = Color.White
          ),
          elevation = ButtonDefaults.buttonElevation(8.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .testTag("find_parties_near_me_button")
        ) {
          Text(
            text = "Find Parties Near Me 🚀",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
          )
        }

        Spacer(Modifier.height(10.dp))

        TextButton(
          onClick = onGetStarted,
          modifier = Modifier.testTag("skip_onboarding_button")
        ) {
          Text(
            text = "Skip & Browse All Cities 🗺️",
            color = PartyPurple,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp
          )
        }
      }
    }
  }
}

@Composable
private fun FeaturePill(
  title: String,
  subtitle: String,
  accentColor: Color,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(16.dp),
    color = Color.White.copy(alpha = 0.9f),
    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
    shadowElevation = 2.dp,
    modifier = modifier
  ) {
    Column(
      modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(title, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = accentColor, maxLines = 1)
      Text(subtitle, fontSize = 10.sp, color = Color(0xFF64748B))
    }
  }
}
