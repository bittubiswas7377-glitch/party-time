package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Party
import com.example.model.PartyType
import com.example.ui.theme.CartoonBorder
import com.example.ui.theme.PartyCyan
import com.example.ui.theme.PartyGreen
import com.example.ui.theme.PartyOrange
import com.example.ui.theme.PartyPink
import com.example.ui.theme.PartyPurple
import com.example.ui.theme.PartyPurpleDark
import com.example.ui.theme.PartyYellow
import kotlin.math.roundToInt

@Composable
fun CartoonMapView(
  parties: List<Party>,
  selectedParty: Party?,
  onPartySelected: (Party) -> Unit,
  modifier: Modifier = Modifier,
  isPickLocationMode: Boolean = false,
  pickedLocation: Pair<Float, Float> = Pair(0.5f, 0.5f),
  onLocationPicked: ((Float, Float) -> Unit)? = null
) {
  // Zoom and pan state
  var scale by remember { mutableFloatStateOf(1.0f) }
  var offset by remember { mutableStateOf(Offset.Zero) }

  // Radar & Pulse transitions
  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 0.8f,
    targetValue = 1.6f,
    animationSpec = infiniteRepeatable(
      animation = tween(1800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "pulseScale"
  )
  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.7f,
    targetValue = 0.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(1800, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "pulseAlpha"
  )

  // Floating bounce animation for pins
  val bounceOffset by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = -6f,
    animationSpec = infiniteRepeatable(
      animation = tween(800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pinBounce"
  )

  val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
    scale = (scale * zoomChange).coerceIn(0.65f, 2.8f)
    offset += offsetChange
  }

  BoxWithConstraints(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFFE8F4F8)) // Cartoon ocean/water light blue base
  ) {
    val canvasWidth = constraints.maxWidth.toFloat()
    val canvasHeight = constraints.maxHeight.toFloat()

    // Interactive gestures
    Box(
      modifier = Modifier
        .fillMaxSize()
        .transformable(state = transformState)
        .pointerInput(isPickLocationMode) {
          detectTapGestures { tapOffset ->
            if (isPickLocationMode && onLocationPicked != null) {
              // Convert tapOffset back to normalized 0..1 coordinates considering pan & zoom
              val centerX = canvasWidth / 2f + offset.x
              val centerY = canvasHeight / 2f + offset.y
              val normX = ((tapOffset.x - centerX) / (canvasWidth * scale) + 0.5f).coerceIn(0.1f, 0.9f)
              val normY = ((tapOffset.y - centerY) / (canvasHeight * scale) + 0.5f).coerceIn(0.1f, 0.9f)
              onLocationPicked(normX, normY)
            }
          }
        }
    ) {
      // 1. Cartoon Map Canvas Background
      Canvas(modifier = Modifier.fillMaxSize()) {
        val centerX = size.width / 2f + offset.x
        val centerY = size.height / 2f + offset.y
        val mapW = size.width * scale
        val mapH = size.height * scale

        // Draw island / land masses
        drawCartoonLandmass(centerX, centerY, mapW, mapH)

        // Draw roads & avenues
        drawCartoonRoads(centerX, centerY, mapW, mapH)

        // Draw city park & lake
        drawCartoonParks(centerX, centerY, mapW, mapH)

        // Draw city buildings / blocks
        drawCartoonBlocks(centerX, centerY, mapW, mapH)

        // Draw user location radar wave
        val userScreenX = centerX + (0.5f - 0.5f) * mapW
        val userScreenY = centerY + (0.52f - 0.5f) * mapH

        // Animated radar waves
        drawCircle(
          color = PartyCyan.copy(alpha = pulseAlpha * 0.5f),
          radius = 55.dp.toPx() * pulseScale,
          center = Offset(userScreenX, userScreenY)
        )
        drawCircle(
          color = PartyCyan.copy(alpha = 0.9f),
          radius = 12.dp.toPx(),
          center = Offset(userScreenX, userScreenY)
        )
        drawCircle(
          color = Color.White,
          radius = 7.dp.toPx(),
          center = Offset(userScreenX, userScreenY)
        )
        drawCircle(
          color = PartyPurple,
          radius = 4.dp.toPx(),
          center = Offset(userScreenX, userScreenY)
        )
      }

      // 2. Overlay Cartoon Markers for Parties
      parties.forEach { party ->
        val centerX = canvasWidth / 2f + offset.x
        val centerY = canvasHeight / 2f + offset.y
        val mapW = canvasWidth * scale
        val mapH = canvasHeight * scale

        val markerX = centerX + (party.mapX - 0.5f) * mapW
        val markerY = centerY + (party.mapY - 0.5f) * mapH

        val isSelected = selectedParty?.id == party.id

        PartyCartoonPin(
          party = party,
          isSelected = isSelected,
          bounceOffset = if (party.isLiveNow) bounceOffset else 0f,
          pulseScale = pulseScale,
          pulseAlpha = pulseAlpha,
          onClick = { onPartySelected(party) },
          modifier = Modifier.offset {
            IntOffset(
              x = (markerX - 36.dp.toPx()).roundToInt(),
              y = (markerY - 60.dp.toPx() + if (isSelected) -10.dp.toPx() else 0f).roundToInt()
            )
          }
        )
      }

      // 3. In Pick Location Mode, show the chosen pin
      if (isPickLocationMode) {
        val centerX = canvasWidth / 2f + offset.x
        val centerY = canvasHeight / 2f + offset.y
        val mapW = canvasWidth * scale
        val mapH = canvasHeight * scale

        val pickedX = centerX + (pickedLocation.first - 0.5f) * mapW
        val pickedY = centerY + (pickedLocation.second - 0.5f) * mapH

        Box(
          modifier = Modifier
            .offset {
              IntOffset((pickedX - 28.dp.toPx()).roundToInt(), (pickedY - 56.dp.toPx()).roundToInt())
            }
            .scale(1.15f),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = PartyPink,
              border = androidx.compose.foundation.BorderStroke(2.dp, Color.White),
              shadowElevation = 6.dp
            ) {
              Text(
                text = "📍 Host Here!",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
            Text("📍", fontSize = 34.sp)
          }
        }
      }
    }

    // Map Controls: Zoom in, Zoom out, Recenter
    Column(
      modifier = Modifier
        .align(Alignment.CenterEnd)
        .padding(end = 16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      FloatingActionButton(
        onClick = { scale = (scale * 1.25f).coerceAtMost(2.8f) },
        shape = CircleShape,
        containerColor = Color.White,
        contentColor = PartyPurple,
        elevation = FloatingActionButtonDefaults.elevation(4.dp),
        modifier = Modifier
          .size(44.dp)
          .testTag("zoom_in_button")
      ) {
        Icon(Icons.Default.Add, contentDescription = "Zoom In")
      }

      FloatingActionButton(
        onClick = { scale = (scale / 1.25f).coerceAtLeast(0.65f) },
        shape = CircleShape,
        containerColor = Color.White,
        contentColor = PartyPurple,
        elevation = FloatingActionButtonDefaults.elevation(4.dp),
        modifier = Modifier
          .size(44.dp)
          .testTag("zoom_out_button")
      ) {
        Icon(Icons.Default.Remove, contentDescription = "Zoom Out")
      }

      FloatingActionButton(
        onClick = {
          scale = 1.0f
          offset = Offset.Zero
        },
        shape = CircleShape,
        containerColor = PartyPurple,
        contentColor = Color.White,
        elevation = FloatingActionButtonDefaults.elevation(6.dp),
        modifier = Modifier
          .size(48.dp)
          .testTag("center_me_button")
      ) {
        Icon(Icons.Default.MyLocation, contentDescription = "Recenter on Me")
      }
    }

    // Cartoon Map Legend Pill at Top
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = Color.White.copy(alpha = 0.95f),
      border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFE2E8F0)),
      shadowElevation = 4.dp,
      modifier = Modifier
        .align(Alignment.TopCenter)
        .padding(top = 10.dp)
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(10.dp)
              .clip(CircleShape)
              .background(PartyGreen)
          )
          Spacer(Modifier.width(4.dp))
          Text("Public (Instant)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CartoonBorder)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(10.dp)
              .clip(CircleShape)
              .background(PartyPurple)
          )
          Spacer(Modifier.width(4.dp))
          Text("Private (Request)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CartoonBorder)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text("🔥", fontSize = 11.sp)
          Spacer(Modifier.width(2.dp))
          Text("Live Now", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PartyPink)
        }
      }
    }
  }
}

@Composable
private fun PartyCartoonPin(
  party: Party,
  isSelected: Boolean,
  bounceOffset: Float,
  pulseScale: Float,
  pulseAlpha: Float,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val isPublic = party.type == PartyType.PUBLIC
  val primaryPinColor = if (isPublic) PartyGreen else PartyPurple
  val glowColor = if (isPublic) PartyYellow else PartyPink

  Box(
    modifier = modifier
      .clickable { onClick() }
      .testTag("party_pin_${party.id}"),
    contentAlignment = Alignment.TopCenter
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.offset(y = bounceOffset.dp)
    ) {
      // 1. "LIVE NOW" / "PENDING" floating badge
      if (party.isLiveNow) {
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = PartyPink,
          border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.White),
          shadowElevation = 3.dp,
          modifier = Modifier.offset(y = 4.dp)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(PartyYellow)
            )
            Spacer(Modifier.width(3.dp))
            Text(
              text = "LIVE",
              color = Color.White,
              fontWeight = FontWeight.ExtraBold,
              fontSize = 9.sp
            )
          }
        }
      } else if (party.isRequestPending) {
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = PartyOrange,
          shadowElevation = 2.dp
        ) {
          Text(
            text = "⏳ Pending",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
          )
        }
      }

      // 2. Cartoon Pin Head with Glow Ring
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(54.dp)
      ) {
        // Pulsing glow for Live parties
        if (party.isLiveNow) {
          Box(
            modifier = Modifier
              .size(52.dp * pulseScale)
              .clip(CircleShape)
              .background(glowColor.copy(alpha = pulseAlpha * 0.6f))
          )
        }

        // Cartoon Chunky Pin Bubble
        Surface(
          shape = RoundedCornerShape(22.dp),
          color = if (isSelected) PartyPink else Color.White,
          border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 3.5.dp else 2.5.dp,
            color = if (isSelected) PartyYellow else primaryPinColor
          ),
          shadowElevation = if (isSelected) 10.dp else 5.dp,
          modifier = Modifier.size(46.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Text(
              text = party.emoji,
              fontSize = 24.sp
            )

            // Corner indicator for Private lock
            if (!isPublic) {
              Box(
                modifier = Modifier
                  .align(Alignment.BottomEnd)
                  .offset(x = 2.dp, y = 2.dp)
                  .size(16.dp)
                  .clip(CircleShape)
                  .background(PartyPurpleDark)
                  .border(1.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Lock,
                  contentDescription = "Private",
                  tint = Color.White,
                  modifier = Modifier.size(9.dp)
                )
              }
            }
          }
        }
      }

      // 3. Pointer Triangle under pin
      Canvas(modifier = Modifier.size(10.dp, 6.dp)) {
        val path = Path().apply {
          moveTo(0f, 0f)
          lineTo(size.width, 0f)
          lineTo(size.width / 2f, size.height)
          close()
        }
        drawPath(path, color = if (isSelected) PartyYellow else primaryPinColor)
      }

      // 4. Party Name Label Pill
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) Color(0xFF1E1B4B) else Color.White.copy(alpha = 0.95f),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) PartyPink else Color(0xFFCBD5E1)),
        shadowElevation = 3.dp,
        modifier = Modifier.offset(y = 2.dp)
      ) {
        Text(
          text = party.name.take(15) + if (party.name.length > 15) ".." else "",
          color = if (isSelected) Color.White else CartoonBorder,
          fontWeight = FontWeight.Bold,
          fontSize = 10.sp,
          modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
        )
      }
    }
  }
}

// Draw methods for custom cartoon styled world
private fun DrawScope.drawCartoonLandmass(centerX: Float, centerY: Float, mapW: Float, mapH: Float) {
  // Main Land Polygon (warm pastel green & cream tones)
  val landBrush = Brush.verticalGradient(
    colors = listOf(Color(0xFFE2F9E5), Color(0xFFD4F5DA), Color(0xFFF9FDE8))
  )
  drawRoundRect(
    brush = landBrush,
    topLeft = Offset(centerX - mapW * 0.48f, centerY - mapH * 0.48f),
    size = Size(mapW * 0.96f, mapH * 0.96f),
    cornerRadius = CornerRadius(48.dp.toPx(), 48.dp.toPx())
  )

  // Meandering Cartoon River
  val riverPath = Path().apply {
    moveTo(centerX - mapW * 0.48f, centerY + mapH * 0.15f)
    cubicTo(
      centerX - mapW * 0.15f, centerY + mapH * 0.05f,
      centerX + mapW * 0.05f, centerY + mapH * 0.35f,
      centerX + mapW * 0.48f, centerY + mapH * 0.25f
    )
    lineTo(centerX + mapW * 0.48f, centerY + mapH * 0.38f)
    cubicTo(
      centerX + mapW * 0.05f, centerY + mapH * 0.48f,
      centerX - mapW * 0.15f, centerY + mapH * 0.18f,
      centerX - mapW * 0.48f, centerY + mapH * 0.28f
    )
    close()
  }
  drawPath(riverPath, color = Color(0xFF70D6FF).copy(alpha = 0.85f))
  drawPath(riverPath, color = Color.White.copy(alpha = 0.5f), style = Stroke(width = 3.dp.toPx()))
}

private fun DrawScope.drawCartoonRoads(centerX: Float, centerY: Float, mapW: Float, mapH: Float) {
  val roadColor = Color(0xFFFFF7ED)
  val roadBorder = Color(0xFFF5E6D3)
  val roadWidth = 26.dp.toPx()

  // Major Horizontal Avenue ("Bollywood Blvd")
  val y1 = centerY - mapH * 0.18f
  drawRoundRect(
    color = roadBorder,
    topLeft = Offset(centerX - mapW * 0.46f, y1 - 2.dp.toPx()),
    size = Size(mapW * 0.92f, roadWidth + 4.dp.toPx()),
    cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
  )
  drawRoundRect(
    color = roadColor,
    topLeft = Offset(centerX - mapW * 0.46f, y1),
    size = Size(mapW * 0.92f, roadWidth),
    cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
  )

  // Major Vertical Avenue ("Techno Way")
  val x1 = centerX
  drawRoundRect(
    color = roadBorder,
    topLeft = Offset(x1 - roadWidth / 2f - 2.dp.toPx(), centerY - mapH * 0.46f),
    size = Size(roadWidth + 4.dp.toPx(), mapH * 0.92f),
    cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
  )
  drawRoundRect(
    color = roadColor,
    topLeft = Offset(x1 - roadWidth / 2f, centerY - mapH * 0.46f),
    size = Size(roadWidth, mapH * 0.92f),
    cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
  )

  // Secondary Diagonal Road ("Bass Lane")
  val x2 = centerX - mapW * 0.25f
  drawRoundRect(
    color = roadColor,
    topLeft = Offset(x2, centerY - mapH * 0.42f),
    size = Size(18.dp.toPx(), mapH * 0.84f),
    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
  )

  val x3 = centerX + mapW * 0.26f
  drawRoundRect(
    color = roadColor,
    topLeft = Offset(x3, centerY - mapH * 0.42f),
    size = Size(18.dp.toPx(), mapH * 0.84f),
    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
  )
}

private fun DrawScope.drawCartoonParks(centerX: Float, centerY: Float, mapW: Float, mapH: Float) {
  // Vibrant Park ("Juhu Party Grounds")
  drawRoundRect(
    color = Color(0xFFB8E994),
    topLeft = Offset(centerX + mapW * 0.08f, centerY - mapH * 0.42f),
    size = Size(mapW * 0.15f, mapH * 0.18f),
    cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
  )
  // Little lake inside park
  drawCircle(
    color = Color(0xFF67E6DC),
    radius = 16.dp.toPx(),
    center = Offset(centerX + mapW * 0.15f, centerY - mapH * 0.33f)
  )

  // Another Park ("Sunset Lawn")
  drawRoundRect(
    color = Color(0xFFC7F9CC),
    topLeft = Offset(centerX - mapW * 0.42f, centerY + mapH * 0.22f),
    size = Size(mapW * 0.14f, mapH * 0.16f),
    cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
  )
}

private fun DrawScope.drawCartoonBlocks(centerX: Float, centerY: Float, mapW: Float, mapH: Float) {
  val blockColors = listOf(
    Color(0xFFFFDFD3), Color(0xFFE0BBE4), Color(0xFFD0F4DE), Color(0xFFFFC6FF)
  )
  // Draw cute colorful city building blocks
  val offsets = listOf(
    Pair(-0.20f, -0.38f),
    Pair(-0.12f, -0.35f),
    Pair(0.12f, -0.12f),
    Pair(0.20f, -0.08f),
    Pair(-0.20f, 0.02f),
    Pair(-0.12f, 0.06f),
    Pair(0.08f, 0.42f),
    Pair(0.18f, 0.38f)
  )

  offsets.forEachIndexed { index, (relX, relY) ->
    val color = blockColors[index % blockColors.size]
    drawRoundRect(
      color = color,
      topLeft = Offset(centerX + mapW * relX, centerY + mapH * relY),
      size = Size(36.dp.toPx(), 30.dp.toPx()),
      cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
    )
    // Little roof stripe
    drawRoundRect(
      color = Color.White.copy(alpha = 0.5f),
      topLeft = Offset(centerX + mapW * relX + 4.dp.toPx(), centerY + mapH * relY + 4.dp.toPx()),
      size = Size(28.dp.toPx(), 8.dp.toPx()),
      cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
    )
  }
}
