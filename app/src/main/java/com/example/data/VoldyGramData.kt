package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Entity(tableName = "contacts")
data class CatContact(
    @PrimaryKey val id: String, // e.g. "voldy_ai", "luna", "garfield"
    val name: String,
    val avatarEmoji: String, // Adorable emoji representation for local fallback
    val avatarColorHex: String, // Pastel color for avatar circle background
    val status: String, // Status text (WhatsApp "About" or recent story link)
    val statusTime: String,
    val isOnline: Boolean,
    val lastSeen: String,
    val isSystemBot: Boolean = false,
    val storyText: String? = null, // Recent story update
    val storyTime: String? = null // Timestamp for story update
)

@Entity(tableName = "messages")
data class CatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val chatId: String, // Belongs to which contact
    val senderId: String, // "me" or the contact's id
    val senderName: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val interactiveActionType: String? = null // "SALMON", "CATNIP", "LASER" if it was a gift interaction
)

@Dao
interface VoldyGramDao {
    @Query("SELECT * FROM contacts ORDER BY isSystemBot DESC, name ASC")
    fun getAllContactsFlow(): Flow<List<CatContact>>

    @Query("SELECT * FROM contacts WHERE id = :id LIMIT 1")
    fun getContactByIdFlow(id: String): Flow<CatContact?>

    @Query("SELECT * FROM contacts WHERE id = :id LIMIT 1")
    suspend fun getContactById(id: String): CatContact?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: CatContact)

    @Update
    suspend fun updateContact(contact: CatContact)

    @Delete
    suspend fun deleteContact(contact: CatContact)

    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    fun getMessagesForChatFlow(chatId: String): Flow<List<CatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: CatMessage)

    @Query("DELETE FROM messages WHERE chatId = :chatId")
    suspend fun clearMessagesForChat(chatId: String)

    @Query("SELECT * FROM messages ORDER BY timestamp DESC")
    suspend fun getAllMessages(): List<CatMessage>
}

@Database(entities = [CatContact::class, CatMessage::class], version = 1, exportSchema = false)
abstract class VoldyGramDatabase : RoomDatabase() {
    abstract fun voldyGramDao(): VoldyGramDao

    companion object {
        @Volatile
        private var INSTANCE: VoldyGramDatabase? = null

        fun getDatabase(context: Context): VoldyGramDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VoldyGramDatabase::class.java,
                    "voldygram_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class CatChatRepository(private val db: VoldyGramDatabase) {
    private val dao = db.voldyGramDao()

    val allContacts: Flow<List<CatContact>> = dao.getAllContactsFlow()

    fun getContactById(id: String): Flow<CatContact?> = dao.getContactByIdFlow(id)

    fun getMessagesForChat(chatId: String): Flow<List<CatMessage>> = dao.getMessagesForChatFlow(chatId)

    suspend fun insertContact(contact: CatContact) {
        dao.insertContact(contact)
    }

    suspend fun insertMessage(message: CatMessage) {
        dao.insertMessage(message)
    }

    suspend fun deleteContact(contact: CatContact) {
        dao.deleteContact(contact)
    }

    suspend fun updateContact(contact: CatContact) {
        dao.updateContact(contact)
    }

    suspend fun clearMessagesForChat(chatId: String) {
        dao.clearMessagesForChat(chatId)
    }

    // Prepopulate database with default cats if it is empty
    suspend fun prepopulateIfEmpty() {
        withContext(Dispatchers.IO) {
            val contacts = dao.getAllContactsFlow().first()
            if (contacts.isEmpty()) {
                // Insert contacts
                val defaultContacts = listOf(
                    CatContact(
                        id = "voldy_ai",
                        name = "Voldy (My Cat 🐾)",
                        avatarEmoji = "🐈",
                        avatarColorHex = "#FFE4C4", // Bisque
                        status = "Sleeping on the keyboard. meow.",
                        statusTime = "10 mins ago",
                        isOnline = true,
                        lastSeen = "Online",
                        isSystemBot = true,
                        storyText = "Purring very loudly in your keyboard right now. Go send me a treat! 🥐",
                        storyTime = "10m ago"
                    ),
                    CatContact(
                        id = "luna",
                        name = "Luna",
                        avatarEmoji = "🐱",
                        avatarColorHex = "#E6E6FA", // Lavender
                        status = "I saw a red dot today. It got away... for now. 🔴",
                        statusTime = "2 hours ago",
                        isOnline = true,
                        lastSeen = "Online",
                        storyText = "Contemplating the mouse trap... is it delicious? 🐭",
                        storyTime = "1h ago"
                    ),
                    CatContact(
                        id = "garfield",
                        name = "Garfield",
                        avatarEmoji = "🐯",
                        avatarColorHex = "#FFD700", // Gold
                        status = "I hate Mondays. Feed me lasagna.",
                        statusTime = "Yesterday",
                        isOnline = false,
                        lastSeen = "Last seen 3h ago",
                        storyText = "Just woke up from my fourth nap of the morning. 😴🛌",
                        storyTime = "2h ago"
                    ),
                    CatContact(
                        id = "whiskers",
                        name = "Whiskers",
                        avatarEmoji = "🦁",
                        avatarColorHex = "#FFB6C1", // LightPink
                        status = "Catching bugs is my business.",
                        statusTime = "3 days ago",
                        isOnline = true,
                        lastSeen = "Online",
                        storyText = "I successfully caught a fly! Feeling like a tiger today. 🦟✨",
                        storyTime = "4h ago"
                    ),
                    CatContact(
                        id = "professor_paws",
                        name = "Professor Paws",
                        avatarEmoji = "😺",
                        avatarColorHex = "#AFEEEE", // PaleTurquoise
                        status = "Did you know cats rule the internet?",
                        statusTime = "Active now",
                        isOnline = true,
                        lastSeen = "Online",
                        storyText = "Lesson of the day: Cats have 230 bones, human only 206! We are superior. 🧠📚",
                        storyTime = "30m ago"
                    )
                )

                for (contact in defaultContacts) {
                    dao.insertContact(contact)
                }

                // Insert some cute default messages to kick off the chat screen
                dao.insertMessage(CatMessage(chatId = "luna", senderId = "luna", senderName = "Luna", text = "Meow there! Is my dinner ready? I demand salmon pate. 🐟"))
                dao.insertMessage(CatMessage(chatId = "luna", senderId = "me", senderName = "Me", text = "You just ate 30 minutes ago, Luna!"))
                dao.insertMessage(CatMessage(chatId = "luna", senderId = "luna", senderName = "Luna", text = "That was a snack. I need real food now! 😾"))

                dao.insertMessage(CatMessage(chatId = "garfield", senderId = "garfield", senderName = "Garfield", text = "Don't buy normal food. Cook some lasagna instead. Send me a slice!"))

                dao.insertMessage(CatMessage(chatId = "whiskers", senderId = "whiskers", senderName = "Whiskers", text = "Pst... I heard there is catnip in the cabinet. Help me get it? 🌿🐾"))

                dao.insertMessage(
                    CatMessage(
                        chatId = "voldy_ai",
                        senderId = "voldy_ai",
                        senderName = "Voldy (My Cat 🐾)",
                        text = "Meow! Purrrrr. This is Voldy, your loyal companion! Send a message or use the interactive gifts below to keep me happy! 🧶"
                    )
                )
            }
        }
    }
}
