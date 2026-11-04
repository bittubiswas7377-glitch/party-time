package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.ChatMessage
import com.example.model.CloudflareConfig
import com.example.model.Party
import com.example.model.PartyType
import com.example.model.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class PartyRepository(context: Context) {
  private val prefs: SharedPreferences =
    context.getSharedPreferences("partymap_prefs", Context.MODE_PRIVATE)

  val cloudflareService = CloudflareBackendService(context)

  private val _parties = MutableStateFlow<List<Party>>(emptyList())
  val parties: StateFlow<List<Party>> = _parties.asStateFlow()

  private val _userProfile = MutableStateFlow(UserProfile(
    name = "Party Explorer",
    avatarEmoji = "🕺",
    bio = "Ready to discover live parties and afterparties! 🚀",
    joinedCount = 0,
    hostedCount = 0,
    vibeScore = 100
  ))
  val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

  // Map of partyId -> List of ChatMessage
  private val _partyChats = MutableStateFlow<Map<String, List<ChatMessage>>>(emptyMap())
  val partyChats: StateFlow<Map<String, List<ChatMessage>>> = _partyChats.asStateFlow()

  private val _cloudflareConfig = MutableStateFlow(cloudflareService.getConfig())
  val cloudflareConfig: StateFlow<CloudflareConfig> = _cloudflareConfig.asStateFlow()

  private val repositoryScope = CoroutineScope(Dispatchers.IO)

  init {
    loadProfile()
    loadParties()
    loadChats()

    // Sync with Cloudflare edge backend on startup
    repositoryScope.launch {
      val remoteParties = cloudflareService.fetchPartiesFromCloudflare()
      if (!remoteParties.isNullOrEmpty()) {
        val merged = (remoteParties + _parties.value).distinctBy { it.id }
        _parties.value = merged
        saveParties(merged)
      } else if (_parties.value.isNotEmpty()) {
        cloudflareService.syncPartiesToCloudflare(_parties.value)
      }
      _cloudflareConfig.value = cloudflareService.getConfig()
    }
  }

  private fun loadParties() {
    val jsonString = prefs.getString("saved_parties", null)
    if (jsonString.isNullOrEmpty()) {
      _parties.value = emptyList()
      return
    }

    try {
      val jsonArray = JSONArray(jsonString)
      val list = mutableListOf<Party>()
      for (i in 0 until jsonArray.length()) {
        val obj = jsonArray.getJSONObject(i)
        list.add(
          Party(
            id = obj.getString("id"),
            name = obj.getString("name"),
            type = PartyType.valueOf(obj.optString("type", "PUBLIC")),
            isLiveNow = obj.optBoolean("isLiveNow", true),
            emoji = obj.optString("emoji", "🎉"),
            vibeTag = obj.optString("vibeTag", "Bollywood"),
            hostName = obj.optString("hostName", "Host"),
            hostAvatarEmoji = obj.optString("hostAvatarEmoji", "😎"),
            address = obj.optString("address", "Venue"),
            startTime = obj.optString("startTime", "Tonight"),
            attendeeCount = obj.optInt("attendeeCount", 1),
            maxGuests = if (obj.has("maxGuests") && !obj.isNull("maxGuests")) obj.getInt("maxGuests") else null,
            description = obj.optString("description", ""),
            latitude = obj.optDouble("latitude", 19.0596),
            longitude = obj.optDouble("longitude", 72.8295),
            mapX = obj.optDouble("mapX", 0.5).toFloat(),
            mapY = obj.optDouble("mapY", 0.5).toFloat(),
            isJoined = obj.optBoolean("isJoined", false),
            isRequestPending = obj.optBoolean("isRequestPending", false),
            isHostedByMe = obj.optBoolean("isHostedByMe", false)
          )
        )
      }
      _parties.value = list
    } catch (e: Exception) {
      _parties.value = emptyList()
    }
  }

  private fun saveParties(list: List<Party>) {
    try {
      val array = JSONArray()
      for (p in list) {
        val obj = JSONObject().apply {
          put("id", p.id)
          put("name", p.name)
          put("type", p.type.name)
          put("isLiveNow", p.isLiveNow)
          put("emoji", p.emoji)
          put("vibeTag", p.vibeTag)
          put("hostName", p.hostName)
          put("hostAvatarEmoji", p.hostAvatarEmoji)
          put("address", p.address)
          put("startTime", p.startTime)
          put("attendeeCount", p.attendeeCount)
          if (p.maxGuests != null) put("maxGuests", p.maxGuests)
          put("description", p.description)
          put("latitude", p.latitude)
          put("longitude", p.longitude)
          put("mapX", p.mapX.toDouble())
          put("mapY", p.mapY.toDouble())
          put("isJoined", p.isJoined)
          put("isRequestPending", p.isRequestPending)
          put("isHostedByMe", p.isHostedByMe)
        }
        array.put(obj)
      }
      prefs.edit().putString("saved_parties", array.toString()).apply()

      // Async sync to Cloudflare Worker
      repositoryScope.launch {
        cloudflareService.syncPartiesToCloudflare(list)
        _cloudflareConfig.value = cloudflareService.getConfig()
      }
    } catch (e: Exception) {
      // Ignore
    }
  }

  private fun loadChats() {
    val jsonString = prefs.getString("saved_party_chats", null)
    if (jsonString.isNullOrEmpty()) {
      _partyChats.value = emptyMap()
      return
    }

    try {
      val root = JSONObject(jsonString)
      val map = mutableMapOf<String, List<ChatMessage>>()
      val keys = root.keys()
      while (keys.hasNext()) {
        val partyId = keys.next()
        val array = root.getJSONArray(partyId)
        val msgList = mutableListOf<ChatMessage>()
        for (i in 0 until array.length()) {
          val obj = array.getJSONObject(i)
          val reactionsMap = mutableMapOf<String, Int>()
          if (obj.has("reactions")) {
            val rObj = obj.getJSONObject("reactions")
            val rKeys = rObj.keys()
            while (rKeys.hasNext()) {
              val rk = rKeys.next()
              reactionsMap[rk] = rObj.getInt(rk)
            }
          }
          msgList.add(
            ChatMessage(
              id = obj.getString("id"),
              partyId = obj.getString("partyId"),
              senderName = obj.getString("senderName"),
              senderAvatar = obj.getString("senderAvatar"),
              message = obj.getString("message"),
              timestamp = obj.getString("timestamp"),
              isHost = obj.optBoolean("isHost", false),
              isMe = obj.optBoolean("isMe", false),
              reactions = reactionsMap
            )
          )
        }
        map[partyId] = msgList
      }
      _partyChats.value = map
    } catch (e: Exception) {
      _partyChats.value = emptyMap()
    }
  }

  private fun saveChats(chats: Map<String, List<ChatMessage>>) {
    try {
      val root = JSONObject()
      for ((partyId, list) in chats) {
        val array = JSONArray()
        for (msg in list) {
          val mObj = JSONObject().apply {
            put("id", msg.id)
            put("partyId", msg.partyId)
            put("senderName", msg.senderName)
            put("senderAvatar", msg.senderAvatar)
            put("message", msg.message)
            put("timestamp", msg.timestamp)
            put("isHost", msg.isHost)
            put("isMe", msg.isMe)
            val rObj = JSONObject()
            msg.reactions.forEach { (emoji, count) -> rObj.put(emoji, count) }
            put("reactions", rObj)
          }
          array.put(mObj)
        }
        root.put(partyId, array)
      }
      prefs.edit().putString("saved_party_chats", root.toString()).apply()
    } catch (e: Exception) {
      // Ignore
    }
  }

  private fun loadProfile() {
    val name = prefs.getString("user_name", "Party Explorer") ?: "Party Explorer"
    val avatar = prefs.getString("user_avatar", "🕺") ?: "🕺"
    val bio = prefs.getString("user_bio", "Ready to discover live parties and afterparties! 🚀") ?: ""
    val joined = prefs.getInt("user_joined_count", 0)
    val hosted = prefs.getInt("user_hosted_count", 0)
    val vibe = prefs.getInt("user_vibe_score", 100)

    _userProfile.value = UserProfile(
      name = name,
      avatarEmoji = avatar,
      bio = bio,
      joinedCount = joined,
      hostedCount = hosted,
      vibeScore = vibe
    )
  }

  fun updateProfile(name: String, avatar: String, bio: String) {
    val current = _userProfile.value
    val updated = current.copy(name = name, avatarEmoji = avatar, bio = bio)
    _userProfile.value = updated
    prefs.edit()
      .putString("user_name", name)
      .putString("user_avatar", avatar)
      .putString("user_bio", bio)
      .apply()
  }

  fun sendChatMessage(
    partyId: String,
    text: String,
    senderName: String,
    senderAvatar: String,
    isHost: Boolean
  ) {
    if (text.isBlank()) return
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    val timestamp = sdf.format(Date())

    val newMsg = ChatMessage(
      id = "msg_" + UUID.randomUUID().toString().take(8),
      partyId = partyId,
      senderName = senderName,
      senderAvatar = senderAvatar,
      message = text.trim(),
      timestamp = timestamp,
      isHost = isHost,
      isMe = true,
      reactions = emptyMap()
    )

    val currentMap = _partyChats.value.toMutableMap()
    val list = (currentMap[partyId] ?: emptyList()).toMutableList()
    list.add(newMsg)
    currentMap[partyId] = list
    _partyChats.value = currentMap
    saveChats(currentMap)

    // Send to Cloudflare Worker backend
    repositoryScope.launch {
      cloudflareService.postChatMessage(newMsg)
      _cloudflareConfig.value = cloudflareService.getConfig()
    }
  }

  fun addReactionToMessage(partyId: String, messageId: String, reactionEmoji: String) {
    val currentMap = _partyChats.value.toMutableMap()
    val list = (currentMap[partyId] ?: emptyList()).map { msg ->
      if (msg.id == messageId) {
        val currentReactions = msg.reactions.toMutableMap()
        val count = currentReactions[reactionEmoji] ?: 0
        currentReactions[reactionEmoji] = count + 1
        msg.copy(reactions = currentReactions)
      } else {
        msg
      }
    }
    currentMap[partyId] = list
    _partyChats.value = currentMap
    saveChats(currentMap)
  }

  fun joinParty(partyId: String): Boolean {
    val current = _parties.value
    val updated = current.map { party ->
      if (party.id == partyId && !party.isJoined) {
        party.copy(
          isJoined = true,
          attendeeCount = party.attendeeCount + 1,
          isRequestPending = false
        )
      } else {
        party
      }
    }
    _parties.value = updated
    saveParties(updated)

    val newJoinedCount = updated.count { it.isJoined }
    _userProfile.value = _userProfile.value.copy(joinedCount = newJoinedCount)
    prefs.edit().putInt("user_joined_count", newJoinedCount).apply()

    // Add a welcome system message to the party room chat
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    val welcomeMsg = ChatMessage(
      id = "sys_" + UUID.randomUUID().toString().take(6),
      partyId = partyId,
      senderName = "Party Bot 🤖",
      senderAvatar = "🎉",
      message = "${_userProfile.value.name} joined the party! Vibe check passed! 🥳",
      timestamp = sdf.format(Date()),
      isHost = false,
      isMe = false,
      reactions = mapOf("🎉" to 1)
    )
    val chats = _partyChats.value.toMutableMap()
    val pList = (chats[partyId] ?: emptyList()).toMutableList()
    pList.add(welcomeMsg)
    chats[partyId] = pList
    _partyChats.value = chats
    saveChats(chats)

    return true
  }

  fun leaveParty(partyId: String) {
    val current = _parties.value
    val updated = current.map { party ->
      if (party.id == partyId && party.isJoined) {
        party.copy(
          isJoined = false,
          attendeeCount = maxOf(0, party.attendeeCount - 1)
        )
      } else {
        party
      }
    }
    _parties.value = updated
    saveParties(updated)

    val newJoinedCount = updated.count { it.isJoined }
    _userProfile.value = _userProfile.value.copy(joinedCount = newJoinedCount)
    prefs.edit().putInt("user_joined_count", newJoinedCount).apply()
  }

  fun requestJoinParty(partyId: String) {
    val current = _parties.value
    val updated = current.map { party ->
      if (party.id == partyId) {
        party.copy(isRequestPending = true)
      } else {
        party
      }
    }
    _parties.value = updated
    saveParties(updated)
  }

  fun cancelRequest(partyId: String) {
    val current = _parties.value
    val updated = current.map { party ->
      if (party.id == partyId) {
        party.copy(isRequestPending = false)
      } else {
        party
      }
    }
    _parties.value = updated
    saveParties(updated)
  }

  fun hostParty(newParty: Party) {
    val current = _parties.value
    val partyWithFlags = newParty.copy(
      isHostedByMe = true,
      isJoined = true
    )
    val updated = listOf(partyWithFlags) + current
    _parties.value = updated
    saveParties(updated)

    // Seed initial host greeting message in the room chat
    val initialMsg = ChatMessage(
      id = "host_init_" + UUID.randomUUID().toString().take(6),
      partyId = newParty.id,
      senderName = _userProfile.value.name,
      senderAvatar = newParty.hostAvatarEmoji,
      message = "Party room is officially open! Welcome everyone. Dhoom machao! 🪩✨",
      timestamp = "Just now",
      isHost = true,
      isMe = true,
      reactions = mapOf("🔥" to 1)
    )
    val chats = _partyChats.value.toMutableMap()
    chats[newParty.id] = listOf(initialMsg)
    _partyChats.value = chats
    saveChats(chats)

    val hostedCount = updated.count { it.isHostedByMe }
    val joinedCount = updated.count { it.isJoined }
    _userProfile.value = _userProfile.value.copy(
      hostedCount = hostedCount,
      joinedCount = joinedCount
    )
    prefs.edit()
      .putInt("user_hosted_count", hostedCount)
      .putInt("user_joined_count", joinedCount)
      .apply()
  }

  fun updateCloudflareUrl(newUrl: String) {
    cloudflareService.workerUrl = newUrl
    _cloudflareConfig.value = cloudflareService.getConfig()
  }

  suspend fun pingCloudflare(): Pair<Boolean, String> {
    val res = cloudflareService.pingWorker()
    _cloudflareConfig.value = cloudflareService.getConfig()
    return res
  }

  fun resetToDefault() {
    _parties.value = emptyList()
    _partyChats.value = emptyMap()
    _userProfile.value = UserProfile(
      name = "Party Explorer",
      avatarEmoji = "🕺",
      bio = "Ready to discover live parties and afterparties! 🚀",
      joinedCount = 0,
      hostedCount = 0,
      vibeScore = 100
    )
    prefs.edit().clear().apply()
  }
}
