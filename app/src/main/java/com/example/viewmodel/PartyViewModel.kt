package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.PartyRepository
import com.example.model.ChatMessage
import com.example.model.CloudflareConfig
import com.example.model.Party
import com.example.model.UserProfile
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PartyViewModel(application: Application) : AndroidViewModel(application) {
  private val repository = PartyRepository(application.applicationContext)

  val parties: StateFlow<List<Party>> = repository.parties
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val userProfile: StateFlow<UserProfile> = repository.userProfile
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile())

  val partyChats: StateFlow<Map<String, List<ChatMessage>>> = repository.partyChats
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

  val cloudflareConfig: StateFlow<CloudflareConfig> = repository.cloudflareConfig
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.cloudflareService.getConfig())

  private val _currentTab = MutableStateFlow(0) // 0: Map, 1: My Parties, 2: Profile
  val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

  private val _showOnboarding = MutableStateFlow(true)
  val showOnboarding: StateFlow<Boolean> = _showOnboarding.asStateFlow()

  // Active party chat sheet or screen
  private val _activeChatParty = MutableStateFlow<Party?>(null)
  val activeChatParty: StateFlow<Party?> = _activeChatParty.asStateFlow()

  private val _messages = MutableSharedFlow<String>()
  val messages: SharedFlow<String> = _messages.asSharedFlow()

  fun selectTab(tab: Int) {
    _currentTab.value = tab
  }

  fun dismissOnboarding() {
    _showOnboarding.value = false
  }

  fun reopenOnboarding() {
    _showOnboarding.value = true
  }

  fun openPartyChat(party: Party) {
    _activeChatParty.value = party
  }

  fun closePartyChat() {
    _activeChatParty.value = null
  }

  fun sendChatMessage(partyId: String, text: String) {
    val profile = userProfile.value
    val party = parties.value.find { it.id == partyId }
    val isHost = party?.isHostedByMe ?: false
    repository.sendChatMessage(
      partyId = partyId,
      text = text,
      senderName = profile.name,
      senderAvatar = profile.avatarEmoji,
      isHost = isHost
    )
  }

  fun addReaction(partyId: String, messageId: String, emoji: String) {
    repository.addReactionToMessage(partyId, messageId, emoji)
  }

  fun joinParty(partyId: String) {
    repository.joinParty(partyId)
  }

  fun leaveParty(partyId: String) {
    repository.leaveParty(partyId)
  }

  fun requestJoinParty(partyId: String) {
    repository.requestJoinParty(partyId)
  }

  fun cancelRequest(partyId: String) {
    repository.cancelRequest(partyId)
  }

  fun hostParty(party: Party) {
    repository.hostParty(party)
  }

  fun updateProfile(name: String, avatar: String, bio: String) {
    repository.updateProfile(name, avatar, bio)
  }

  fun updateCloudflareUrl(newUrl: String) {
    repository.updateCloudflareUrl(newUrl)
    showMessage("Cloudflare worker endpoint updated! ⚡")
  }

  fun pingCloudflare(callback: (Boolean, String) -> Unit) {
    viewModelScope.launch {
      val (success, detail) = repository.pingCloudflare()
      callback(success, detail)
    }
  }

  fun resetDemoData() {
    repository.resetToDefault()
  }

  fun showMessage(msg: String) {
    viewModelScope.launch {
      _messages.emit(msg)
    }
  }
}
