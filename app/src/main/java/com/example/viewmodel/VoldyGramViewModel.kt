package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.api.GeminiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class VoldyGramViewModel(application: Application) : AndroidViewModel(application) {

    private val db = VoldyGramDatabase.getDatabase(application)
    private val repository = CatChatRepository(db)
    private val geminiService = GeminiService()

    // --- Core Database Flows ---
    val contacts: StateFlow<List<CatContact>> = repository.allContacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- UI/UX Interactive States ---
    private val _activeChatId = MutableStateFlow<String?>(null)
    val activeChatId: StateFlow<String?> = _activeChatId.asStateFlow()

    private val _activeContact = MutableStateFlow<CatContact?>(null)
    val activeContact: StateFlow<CatContact?> = _activeContact.asStateFlow()

    private val _currentChatMessages = MutableStateFlow<List<CatMessage>>(emptyList())
    val currentChatMessages: StateFlow<List<CatMessage>> = _currentChatMessages.asStateFlow()

    private val _isVoldyTyping = MutableStateFlow(false)
    val isVoldyTyping: StateFlow<Boolean> = _isVoldyTyping.asStateFlow()

    private val _currentOpenedStory = MutableStateFlow<CatContact?>(null)
    val currentOpenedStory: StateFlow<CatContact?> = _currentOpenedStory.asStateFlow()

    // --- Voldy Playpen States ---
    private val _playpenVoldyHappiness = MutableStateFlow(60) // 0 to 100
    val playpenVoldyHappiness: StateFlow<Int> = _playpenVoldyHappiness.asStateFlow()

    private val _bowlFillLevel = MutableStateFlow(0) // 0 to 3
    val bowlFillLevel: StateFlow<Int> = _bowlFillLevel.asStateFlow()

    private val _voldyPlaypenStatus = MutableStateFlow("Voldy is lounging on the cushion.")
    val voldyPlaypenStatus: StateFlow<String> = _voldyPlaypenStatus.asStateFlow()

    // --- Customized Themes & Visual Preferences ---
    private val _chatWallpaper = MutableStateFlow("paw_prints") // paw_prints, salmon_coral, fish_bones, cozy_catbed
    val chatWallpaper: StateFlow<String> = _chatWallpaper.asStateFlow()

    private val _themeColor = MutableStateFlow("classic_meow") // classic_meow (orange), catmint_green (teal), sweet_salmon (pink)
    val themeColor: StateFlow<String> = _themeColor.asStateFlow()

    init {
        // Pre-populate core cats on startup
        viewModelScope.launch {
            repository.prepopulateIfEmpty()
        }

        // Start collecting messages whenever activeChatId changes
        viewModelScope.launch {
            activeChatId.collectLatest { chatId ->
                if (chatId != null) {
                    repository.getMessagesForChat(chatId).collect { msgs ->
                        _currentChatMessages.value = msgs
                    }
                } else {
                    _currentChatMessages.value = emptyList()
                }
            }
        }

        // Keep active contact synced
        viewModelScope.launch {
            activeChatId.collectLatest { chatId ->
                if (chatId != null) {
                    repository.getContactById(chatId).collect { contact ->
                        _activeContact.value = contact
                    }
                } else {
                    _activeContact.value = null
                }
            }
        }
    }

    // --- Chat Actions ---

    fun selectChat(chatId: String) {
        _activeChatId.value = chatId
    }

    fun closeChat() {
        _activeChatId.value = null
    }

    fun sendMessage(text: String, interactiveAction: String? = null) {
        val chatId = activeChatId.value ?: return
        if (text.isBlank() && interactiveAction == null) return

        viewModelScope.launch(Dispatchers.IO) {
            // Write user's message
            val userMsg = CatMessage(
                chatId = chatId,
                senderId = "me",
                senderName = "Me",
                text = text,
                interactiveActionType = interactiveAction
            )
            repository.insertMessage(userMsg)

            // Trigger contact reply based on system type
            if (chatId == "voldy_ai") {
                // Voldy AI Chatbot trigger
                triggerVoldyAiReply(text, interactiveAction)
            } else {
                // Simulated contact normal chat replies
                triggerContactSimulatedReply(chatId, text)
            }
        }
    }

    private suspend fun triggerVoldyAiReply(userText: String, action: String?) {
        _isVoldyTyping.value = true
        // Cat "types" for a cute random duration
        delay((1200..2500).random().toLong())

        val history = _currentChatMessages.value
        val actualPrompt = when (action) {
            "SALMON" -> "I got you a delicious salmon snack!"
            "CATNIP" -> "I brought you some magical herbal catnip!"
            "LASER" -> "Look! I shined a laser dot right here!"
            else -> userText
        }

        // Adjust happiness
        when (action) {
            "SALMON" -> {
                _playpenVoldyHappiness.value = (_playpenVoldyHappiness.value + 15).coerceAtMost(100)
                _voldyPlaypenStatus.value = "Voldy is happily licking salmon crumbs from paws!"
            }
            "CATNIP" -> {
                _playpenVoldyHappiness.value = (_playpenVoldyHappiness.value + 20).coerceAtMost(100)
                _voldyPlaypenStatus.value = "Voldy is in absolute heaven rolling around!"
            }
            "LASER" -> {
                _playpenVoldyHappiness.value = (_playpenVoldyHappiness.value + 10).coerceAtMost(100)
                _voldyPlaypenStatus.value = "Voldy chased the laser furiously!"
            }
        }

        val aiResult = geminiService.chatWithVoldy(actualPrompt, history)

        val voldyMsg = CatMessage(
            chatId = "voldy_ai",
            senderId = "voldy_ai",
            senderName = "Voldy (My Cat 🐾)",
            text = aiResult
        )

        repository.insertMessage(voldyMsg)
        _isVoldyTyping.value = false
    }

    private suspend fun triggerContactSimulatedReply(contactId: String, userText: String) {
        _isVoldyTyping.value = true
        delay((1500..3000).random().toLong())

        val contact = _activeContact.value ?: return
        val replyText = when (contactId) {
            "luna" -> {
                val replies = listOf(
                    "Fascinating. But did you bring food? 🐟",
                    "I am currently watching a fly. Leave a message at the sound of the meow.",
                    "If you give me a back rub, I might listen. Meow. 🐾",
                    "Do you hear that? I think a container is opening!",
                    "*ignores you, continues staring at nothing*"
                )
                replies.random()
            }
            "garfield" -> {
                val replies = listOf(
                    "Yawn. I need another nap. Chat with me when lasagna is baked. 🛌🥞",
                    "I am completely comfortable. Do not expect me to fetch anything.",
                    "Is Voldy awake? We have a synchronized sleeping contest at 3 PM.",
                    "Diet is such an ugly word. Meow."
                )
                replies.random()
            }
            "whiskers" -> {
                val replies = listOf(
                    "Squeak?! Oh, thought it was a mouse. My bad. 🐭",
                    "I knocked a plant off the counter. It was a physics experiment. 🌿🧪",
                    "Let's play catch! Bring the ball of yarn!",
                    "That sounds fun! Wait, is there food involved?"
                )
                replies.random()
            }
            "professor_paws" -> {
                val replies = listOf(
                    "Brilliant observation! Did you know a cat's purr can speed up bone healing? 🧠🐈",
                    "Let me consult my feline textbooks. Ah yes, more naps are recommended.",
                    "Our feline ancestors were worshipped as gods, and we have never forgotten this.",
                    "According to history, cats are the apex predators of cozy sofas. 📚🛋️"
                )
                replies.random()
            }
            else -> {
                "Meow! That's sweet. *swipes at screen*"
            }
        }

        val replyMsg = CatMessage(
            chatId = contactId,
            senderId = contactId,
            senderName = contact.name,
            text = replyText
        )
        repository.insertMessage(replyMsg)
        _isVoldyTyping.value = false
    }

    // --- Contact Addition Support ---

    fun addNewCatContact(name: String, emoji: String, statusText: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val validEmoji = if (emoji.isBlank()) "🐱" else emoji
            val validName = if (name.isBlank()) "Unknown Kitty" else name
            val colors = listOf("#FFE4C4", "#E6E6FA", "#FFD700", "#FFB6C1", "#AFEEEE", "#FFDAB9", "#E0FFFF")
            
            val newContact = CatContact(
                id = "cat_" + System.currentTimeMillis(),
                name = validName,
                avatarEmoji = validEmoji,
                avatarColorHex = colors.random(),
                status = statusText.ifBlank { "Cruising around the alleys." },
                statusTime = "Just now",
                isOnline = true,
                lastSeen = "Online",
                storyText = "Just joined VoldyGram! Meow everyone! 🐾🎊",
                storyTime = "Just now"
            )
            repository.insertContact(newContact)
            
            // Starter greet message
            val starterMsg = CatMessage(
                chatId = newContact.id,
                senderId = newContact.id,
                senderName = newContact.name,
                text = "Meow! Thanks for adding me, mate! Direct message me anytime. 🐾😺"
            )
            repository.insertMessage(starterMsg)
        }
    }

    fun clearChatMessages(chatId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearMessagesForChat(chatId)
        }
    }

    // --- Playpen Interactions ---

    fun fillBowl() {
        viewModelScope.launch {
            if (_bowlFillLevel.value < 3) {
                _bowlFillLevel.value += 1
                _voldyPlaypenStatus.value = "Bowl topped up with premium tuna kibble! Voldy is interested."
                delay(1200)
                if (_bowlFillLevel.value > 0) {
                    _bowlFillLevel.value -= 1
                    _playpenVoldyHappiness.value = (_playpenVoldyHappiness.value + 12).coerceAtMost(100)
                    _voldyPlaypenStatus.value = "Chomp chomp! Voldy ate some kibble and hummed with satisfaction!"
                }
            } else {
                _voldyPlaypenStatus.value = "Bowl is completely full! Voldy is staring at the mountain of kibble."
            }
        }
    }

    fun giveCatnipToy() {
        viewModelScope.launch {
            _playpenVoldyHappiness.value = (_playpenVoldyHappiness.value + 15).coerceAtMost(100)
            _voldyPlaypenStatus.value = "Voldy is sniffing the catnip yarn! Zoomies in 3... 2... 1..."
            delay(1500)
            _voldyPlaypenStatus.value = "Voldy is rolling upside down, batting at imaginary butterflies! 🦋😹"
        }
    }

    fun laserChasings() {
        viewModelScope.launch {
            _playpenVoldyHappiness.value = (_playpenVoldyHappiness.value + 10).coerceAtMost(100)
            _voldyPlaypenStatus.value = "Wiggles butt... POUNCES! Caught the red laser beam!"
            delay(1000)
            _voldyPlaypenStatus.value = "Voldy looks at your finger, wondering how you control the solar dot."
        }
    }

    fun petPlaypenVoldy() {
        viewModelScope.launch {
            _playpenVoldyHappiness.value = (_playpenVoldyHappiness.value + 8).coerceAtMost(100)
            _voldyPlaypenStatus.value = "Purrrrrr... Voldy leans head into your warm stroking gestures."
        }
    }

    // --- App Customizer setters ---

    fun setWallpaper(wp: String) {
        _chatWallpaper.value = wp
    }

    fun setThemeColor(colorName: String) {
        _themeColor.value = colorName
    }

    // --- Stories Handler ---

    fun openStory(contact: CatContact) {
        _currentOpenedStory.value = contact
    }

    fun closeStory() {
        _currentOpenedStory.value = null
    }
}
