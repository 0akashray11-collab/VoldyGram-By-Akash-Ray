package com.example.api

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// --- Gemini REST API Models ---

@JsonClass(generateAdapter = true)
data class Part(
    @Json(name = "text") val text: String
)

@JsonClass(generateAdapter = true)
data class Content(
    @Json(name = "parts") val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    @Json(name = "contents") val contents: List<Content>,
    @Json(name = "systemInstruction") val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    @Json(name = "content") val content: Content
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    @Json(name = "candidates") val candidates: List<Candidate>?
)

// --- Retrofit API Interface ---

interface GeminiApi {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

class GeminiService {

    private val api: GeminiApi

    init {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()

        api = retrofit.create(GeminiApi::class.java)
    }

    /**
     * Talks to Voldy AI (The Cat). Evaluates input, hits Gemini if API key is present,
     * or invokes high-fidelity feline rule-based response if offline or key is missing.
     */
    suspend fun chatWithVoldy(userMessage: String, chatHistory: List<com.example.data.CatMessage>): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.d("GeminiService", "No functional API Key found, using cat fallback logic.")
            return getCatFallbackResponse(userMessage)
        }

        // Prepare conversation format
        val systemMessagePart = Part(
            "You are Voldy, the user's adorable, slightly sassy, and playful pet cat. " +
            "Your speech is feline-themed: sprinkle meows, purrs, claw scratching sounds, and adorable headbutts in your speech! " +
            "Keep your responses relatively short, fun, and extremely cute (mostly 2-3 sentences). " +
            "Use paw emojis (🐾, 🐈, 🐱, 😻) heavily. You LOVE salmon, catnip, and playing with laser pointers."
        )

        val contentsList = mutableListOf<Content>()
        // Feed the last 12 messages from history for contextual memory
        chatHistory.takeLast(12).forEach { msg ->
            if (msg.interactiveActionType == null) {
                val roleName = if (msg.senderId == "me") "user" else "model"
                // Although gemini uses role-based, we pass everything in simple format
                contentsList.add(Content(listOf(Part("${if (roleName == "user") "Owner" else "Voldy"}: ${msg.text}"))))
            }
        }
        
        // Add current user message
        contentsList.add(Content(listOf(Part("Owner: $userMessage"))))

        try {
            val request = GenerateContentRequest(
                contents = contentsList,
                systemInstruction = Content(listOf(systemMessagePart))
            )
            val response = api.generateContent(apiKey, request)
            val reply = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!reply.isNullOrEmpty()) {
                return reply.trim()
            }
        } catch (e: Exception) {
            Log.e("GeminiService", "Error calling Gemini, falling back.", e)
        }
        
        return getCatFallbackResponse(userMessage)
    }

    /**
     * Highly responsive cat simulation fallback engine.
     */
    private fun getCatFallbackResponse(msg: String): String {
        val cleanMsg = msg.lowercase().trim()
        
        return when {
            cleanMsg.contains("salmon") || cleanMsg.contains("fish") || cleanMsg.contains("food") || cleanMsg.contains("feed") -> {
                "Prrrr! *eyes widen like saucers* Salmon?! Lick lick lick! 🐟 That's incredibly delicious, human! My tummy is purring! Meow! 😸"
            }
            cleanMsg.contains("catnip") || cleanMsg.contains("weed") || cleanMsg.contains("herb") -> {
                "Meow-WOW! *rolls around, rubbing head against phone screen* 🌿 Oh, this catnip is magnificent! I can see glowing mice! *soft clumsy paw swat*"
            }
            cleanMsg.contains("laser") || cleanMsg.contains("red dot") || cleanMsg.contains("dot") || cleanMsg.contains("play") -> {
                "*ears perk up, pupils dilate* RED DOT?! Where?! Did you see it? *wiggles butt, pounces on your text bubble* 🔴 Scratch scratch! Meow!"
            }
            cleanMsg.contains("sleep") || cleanMsg.contains("tired") || cleanMsg.contains("nap") -> {
                "Yaaawww-meow... *stretches all four paws* Time for a 16-hour nap under the hot keyboard. Cuddle me? *curls into a soft bread loaf next to you* 🛌🛋️"
            }
            cleanMsg.contains("love") || cleanMsg.contains("like") || cleanMsg.contains("sweet") || cleanMsg.contains("good boy") -> {
                "*softly purrs, closing eyes* Prrrrrr... *gently headbutts your thumb* Meow. You're not so bad for a butler, human. Love you! ❤️🐾"
            }
            cleanMsg.contains("hello") || cleanMsg.contains("hi") || cleanMsg.contains("hey") || cleanMsg.contains("voldy") -> {
                "Meow! *chirps happily and rubs cheeks against your messages* I'm awake! What are we doing today, butler? 🐾"
            }
            cleanMsg.contains("joke") || cleanMsg.contains("funny") -> {
                "Meow! What do you call a pile of kittens? ...A meow-ntain! 😹 *giggles in paws*"
            }
            else -> {
                val fallbacks = listOf(
                    "*glares at you with supreme majesty for 5 seconds, then licks own paw* Meow.",
                    "Purr... *sits directly on your keyboard to stop you typing* Chat with me, not others! 🐈",
                    "Meow! *chirps* I saw a bird outside! It was huge, probably size of a dinosaur!",
                    "Prrrr-meow? *tilts head, watching your finger swipe on the screen* Play laser with me! 🔴",
                    "Scratch! *carefully inspects your fingers* Did you bring snacks, or am I singing meows for free? 🥐🐾",
                    "Purrrrrrrrrrr... *vibrates gently on your lap* You are mine now, human. 🛋️"
                )
                fallbacks.random()
            }
        }
    }
}
