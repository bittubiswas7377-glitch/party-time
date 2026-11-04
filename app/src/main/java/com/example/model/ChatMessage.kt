package com.example.model

data class ChatMessage(
  val id: String,
  val partyId: String,
  val senderName: String,
  val senderAvatar: String,
  val message: String,
  val timestamp: String,
  val isHost: Boolean = false,
  val isMe: Boolean = false,
  val reactions: Map<String, Int> = emptyMap()
)

data class CloudflareConfig(
  val workerUrl: String = "https://partymap.workers.dev",
  val isConnected: Boolean = true,
  val syncStatus: String = "Cloudflare Edge Ready ⚡",
  val lastSyncTime: String = "Just now"
)
