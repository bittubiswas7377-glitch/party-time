package com.example.model

enum class PartyType {
  PUBLIC,
  PRIVATE
}

data class Party(
  val id: String,
  val name: String,
  val type: PartyType,
  val isLiveNow: Boolean = true,
  val emoji: String = "🎉",
  val vibeTag: String = "Bollywood",
  val hostName: String = "DJ Rohan",
  val hostAvatarEmoji: String = "😎",
  val address: String = "Bandra West, Mumbai",
  val startTime: String = "Tonight @ 9:00 PM",
  val attendeeCount: Int = 24,
  val maxGuests: Int? = 50,
  val description: String = "Dhoom machale! The ultimate rooftop vibe with live music, drinks & food.",
  val mapX: Float = 0.5f, // Relative 0.0 to 1.0 on cartoon map
  val mapY: Float = 0.5f,
  val isJoined: Boolean = false,
  val isRequestPending: Boolean = false,
  val isHostedByMe: Boolean = false
)

data class UserProfile(
  val name: String = "Party Legend Ravi",
  val avatarEmoji: String = "🕺",
  val bio: String = "Always where the bass drops & the snacks are free! 🔥",
  val joinedCount: Int = 12,
  val hostedCount: Int = 3,
  val vibeScore: Int = 98
)
