package com.example.data

import android.content.Context
import android.util.Log
import com.example.model.ChatMessage
import com.example.model.CloudflareConfig
import com.example.model.Party
import com.example.model.PartyType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class CloudflareBackendService(private val context: Context) {
  private val prefs = context.getSharedPreferences("partymap_cloudflare_prefs", Context.MODE_PRIVATE)

  private val client = OkHttpClient.Builder()
    .connectTimeout(5, TimeUnit.SECONDS)
    .readTimeout(5, TimeUnit.SECONDS)
    .build()

  var workerUrl: String
    get() = prefs.getString("cf_worker_url", "https://partymap-api.workers.dev") ?: "https://partymap-api.workers.dev"
    set(value) = prefs.edit().putString("cf_worker_url", value.trim()).apply()

  var isCloudflareEnabled: Boolean
    get() = prefs.getBoolean("cf_enabled", true)
    set(value) = prefs.edit().putBoolean("cf_enabled", value).apply()

  fun getConfig(): CloudflareConfig {
    val enabled = isCloudflareEnabled
    return CloudflareConfig(
      workerUrl = workerUrl,
      isConnected = enabled,
      syncStatus = if (enabled) "Cloudflare Edge Ready ⚡" else "Local Storage Only 📦",
      lastSyncTime = prefs.getString("cf_last_sync", "Never synced") ?: "Ready"
    )
  }

  /**
   * Fetches real parties from Cloudflare Worker
   */
  suspend fun fetchPartiesFromCloudflare(): List<Party>? = withContext(Dispatchers.IO) {
    if (!isCloudflareEnabled) return@withContext null
    try {
      val request = Request.Builder()
        .url("$workerUrl/api/parties")
        .get()
        .addHeader("User-Agent", "PartyMap-Android-Client/1.0")
        .build()

      client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) return@withContext null
        val bodyStr = response.body?.string() ?: return@withContext null
        val jsonArray = JSONArray(bodyStr)
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
              address = obj.optString("address", "City Center"),
              startTime = obj.optString("startTime", "Tonight @ 8 PM"),
              attendeeCount = obj.optInt("attendeeCount", 1),
              maxGuests = if (obj.has("maxGuests") && !obj.isNull("maxGuests")) obj.getInt("maxGuests") else null,
              description = obj.optString("description", ""),
              mapX = obj.optDouble("mapX", 0.5).toFloat(),
              mapY = obj.optDouble("mapY", 0.5).toFloat(),
              isJoined = false,
              isRequestPending = false,
              isHostedByMe = false
            )
          )
        }
        list
      }
    } catch (e: Exception) {
      Log.d("CloudflareService", "No remote parties from Cloudflare: ${e.message}")
      null
    }
  }

  /**
   * Syncs parties with Cloudflare Worker KV / D1
   */
  suspend fun syncPartiesToCloudflare(parties: List<Party>): Boolean = withContext(Dispatchers.IO) {
    if (!isCloudflareEnabled) return@withContext false
    try {
      val jsonArray = JSONArray()
      for (p in parties) {
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
          put("description", p.description)
          put("mapX", p.mapX.toDouble())
          put("mapY", p.mapY.toDouble())
        }
        jsonArray.put(obj)
      }

      val requestBody = jsonArray.toString().toRequestBody("application/json".toMediaType())
      val request = Request.Builder()
        .url("$workerUrl/api/parties/sync")
        .post(requestBody)
        .addHeader("User-Agent", "PartyMap-Android-Client/1.0 (Cloudflare Edge)")
        .addHeader("X-App-Platform", "Android-JetpackCompose")
        .build()

      client.newCall(request).execute().use { response ->
        if (response.isSuccessful) {
          prefs.edit().putString("cf_last_sync", "Synced with Cloudflare Edge").apply()
          true
        } else {
          false
        }
      }
    } catch (e: Exception) {
      Log.d("CloudflareService", "Cloudflare worker sync fallback to local: ${e.message}")
      false
    }
  }

  /**
   * Post message to Cloudflare Worker room chat endpoint
   */
  suspend fun postChatMessage(message: ChatMessage): Boolean = withContext(Dispatchers.IO) {
    if (!isCloudflareEnabled) return@withContext false
    try {
      val json = JSONObject().apply {
        put("id", message.id)
        put("partyId", message.partyId)
        put("senderName", message.senderName)
        put("senderAvatar", message.senderAvatar)
        put("message", message.message)
        put("timestamp", message.timestamp)
        put("isHost", message.isHost)
      }

      val requestBody = json.toString().toRequestBody("application/json".toMediaType())
      val request = Request.Builder()
        .url("$workerUrl/api/parties/${message.partyId}/chat")
        .post(requestBody)
        .addHeader("User-Agent", "PartyMap-Android-Client/1.0")
        .build()

      client.newCall(request).execute().use { response ->
        response.isSuccessful
      }
    } catch (e: Exception) {
      Log.d("CloudflareService", "Cloudflare chat post fallback to local: ${e.message}")
      false
    }
  }

  /**
   * Health ping to Cloudflare Worker
   */
  suspend fun pingWorker(): Pair<Boolean, String> = withContext(Dispatchers.IO) {
    try {
      val request = Request.Builder()
        .url("$workerUrl/api/health")
        .get()
        .build()

      client.newCall(request).execute().use { response ->
        if (response.isSuccessful) {
          Pair(true, "Cloudflare Worker Connected 200 OK! Ray ID: ${response.header("CF-RAY", "edge-active")}")
        } else {
          Pair(false, "Cloudflare returned HTTP ${response.code}")
        }
      }
    } catch (e: Exception) {
      Pair(false, "Offline / Local buffer ready (${e.localizedMessage ?: "Connecting..."})")
    }
  }

  companion object {
    const val CLOUDFLARE_WORKER_SCRIPT_TEMPLATE = """
// Cloudflare Worker for PartyMap App Backend
// Deploy via Wrangler: wrangler deploy
// or paste into Cloudflare Dashboard -> Workers & Pages

export default {
  async fetch(request, env, ctx) {
    const url = new URL(request.url);
    const headers = {
      "Content-Type": "application/json",
      "Access-Control-Allow-Origin": "*",
      "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
      "Access-Control-Allow-Headers": "*"
    };

    if (request.method === "OPTIONS") {
      return new Response(null, { headers });
    }

    // Health check
    if (url.pathname === "/api/health") {
      return new Response(JSON.stringify({
        status: "ok",
        service: "PartyMap Cloudflare Edge",
        region: request.cf?.colo || "EDGE",
        timestamp: new Date().toISOString()
      }), { headers });
    }

    // Fetch parties
    if (url.pathname === "/api/parties" && request.method === "GET") {
      // In production with KV: const data = await env.PARTY_KV.get("parties");
      return new Response(JSON.stringify([]), { headers });
    }

    // Sync or retrieve parties
    if (url.pathname === "/api/parties/sync" && request.method === "POST") {
      const parties = await request.json();
      // Store in Cloudflare KV: await env.PARTY_KV.put("parties", JSON.stringify(parties));
      return new Response(JSON.stringify({ success: true, count: parties.length }), { headers });
    }

    // Room Chat message handler
    if (url.pathname.endsWith("/chat") && request.method === "POST") {
      const msg = await request.json();
      return new Response(JSON.stringify({ success: true, messageId: msg.id }), { headers });
    }

    return new Response(JSON.stringify({ error: "PartyMap Cloudflare API Not Found" }), { status: 404, headers });
  }
};
"""
  }
}
