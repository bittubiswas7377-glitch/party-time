package com.example

import com.example.model.ChatMessage
import com.example.model.CloudflareConfig
import com.example.model.Party
import com.example.model.PartyType
import com.example.model.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun testPartyCreationAndJoining() {
    val party = Party(
      id = "test_party_1",
      name = "Bollywood Beats Rooftop 🪩",
      type = PartyType.PUBLIC,
      isLiveNow = true,
      emoji = "🪩",
      vibeTag = "Bollywood",
      hostName = "Rohan K.",
      attendeeCount = 42,
      isJoined = false
    )

    assertEquals(PartyType.PUBLIC, party.type)
    assertTrue(party.isLiveNow)
    assertFalse(party.isJoined)
    assertEquals(19.0596, party.latitude, 0.0001)
    assertEquals(72.8295, party.longitude, 0.0001)

    // Simulate join
    val joinedParty = party.copy(
      isJoined = true,
      attendeeCount = party.attendeeCount + 1,
      latitude = 28.6139,
      longitude = 77.2090
    )

    assertTrue(joinedParty.isJoined)
    assertEquals(43, joinedParty.attendeeCount)
    assertEquals(28.6139, joinedParty.latitude, 0.0001)
    assertEquals(77.2090, joinedParty.longitude, 0.0001)
  }

  @Test
  fun testPrivatePartyRequestStatus() {
    val privateParty = Party(
      id = "test_private_1",
      name = "Secret Speakeasy 🔒",
      type = PartyType.PRIVATE,
      isLiveNow = false,
      emoji = "🎷",
      vibeTag = "House Party",
      hostName = "Natasha D.",
      isJoined = false,
      isRequestPending = false
    )

    assertEquals(PartyType.PRIVATE, privateParty.type)
    assertFalse(privateParty.isRequestPending)

    val requestedParty = privateParty.copy(isRequestPending = true)
    assertTrue(requestedParty.isRequestPending)
    assertFalse(requestedParty.isJoined)
  }

  @Test
  fun testRoomChatMessageAndReactions() {
    val chat = ChatMessage(
      id = "msg_001",
      partyId = "party_1",
      senderName = "DJ Rohan",
      senderAvatar = "😎",
      message = "Chaiyya Chaiyya remix starting in 2 mins! 🪩",
      timestamp = "9:05 PM",
      isHost = true,
      reactions = mapOf("🔥" to 5)
    )

    assertEquals("DJ Rohan", chat.senderName)
    assertTrue(chat.isHost)
    assertEquals(5, chat.reactions["🔥"])

    // Add reaction
    val updatedReactions = chat.reactions.toMutableMap()
    updatedReactions["🔥"] = (updatedReactions["🔥"] ?: 0) + 1
    updatedReactions["🍻"] = 3
    val updatedChat = chat.copy(reactions = updatedReactions)

    assertEquals(6, updatedChat.reactions["🔥"])
    assertEquals(3, updatedChat.reactions["🍻"])
  }

  @Test
  fun testCloudflareConfig() {
    val config = CloudflareConfig(
      workerUrl = "https://custom-partymap.workers.dev",
      isConnected = true,
      syncStatus = "Cloudflare Edge Active ⚡"
    )

    assertEquals("https://custom-partymap.workers.dev", config.workerUrl)
    assertTrue(config.isConnected)
    assertTrue(config.syncStatus.contains("Cloudflare"))
  }

  @Test
  fun testUserProfileStats() {
    val profile = UserProfile(
      name = "Party Legend Ravi",
      avatarEmoji = "🕺",
      joinedCount = 12,
      hostedCount = 3,
      vibeScore = 98
    )

    assertEquals("Party Legend Ravi", profile.name)
    assertEquals("🕺", profile.avatarEmoji)
    assertEquals(12, profile.joinedCount)
    assertEquals(3, profile.hostedCount)
    assertEquals(98, profile.vibeScore)
  }
}
