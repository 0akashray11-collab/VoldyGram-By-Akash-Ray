@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.CatContact
import com.example.data.CatMessage
import com.example.ui.theme.VoldyGramTheme
import com.example.ui.theme.toColor
import com.example.viewmodel.VoldyGramViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// --- WALLPAPER DRAWERS ---

fun Modifier.drawCatWallpaper(themeWallpaper: String, baseColor: Color): Modifier = this.drawBehind {
    // Draw background solid color first
    drawRect(color = baseColor)

    val scalePx = 180f
    when (themeWallpaper) {
        "paw_prints" -> {
            // Draw repeating small soft pawprints
            val color = Color.Gray.copy(alpha = 0.05f)
            val padding = 160f
            var x = 40f
            while (x < size.width) {
                var y = 40f
                while (y < size.height) {
                    val offsetShift = if (((x / padding).toInt() % 2) == 0) padding / 2 else 0f
                    val py = y + offsetShift
                    // Main pad
                    drawCircle(color, radius = 16f, center = Offset(x, py))
                    // 4 toe pads
                    drawCircle(color, radius = 6f, center = Offset(x - 14f, py - 18f))
                    drawCircle(color, radius = 7f, center = Offset(x - 5f, py - 24f))
                    drawCircle(color, radius = 7f, center = Offset(x + 5f, py - 24f))
                    drawCircle(color, radius = 6f, center = Offset(x + 14f, py - 18f))
                    y += padding
                }
                x += padding
            }
        }
        "fish_bones" -> {
            // Draw repeating fish skeletons
            val color = Color.Gray.copy(alpha = 0.06f)
            val step = 150f
            var x = 50f
            while (x < size.width) {
                var y = 50f
                while (y < size.height) {
                    val py = y + (if (((x / step).toInt() % 2) == 0) step / 2 else 0f)
                    // Spine
                    drawLine(color, start = Offset(x - 25f, py), end = Offset(x + 25f, py), strokeWidth = 3f)
                    // Head (triangle to right)
                    val headPath = Path().apply {
                        moveTo(x + 25f, py)
                        lineTo(x + 13f, py - 8f)
                        lineTo(x + 13f, py + 8f)
                        close()
                    }
                    drawPath(headPath, color)
                    // Ribs (vertical lines)
                    drawLine(color, start = Offset(x - 10f, py - 10f), end = Offset(x - 10f, py + 10f), strokeWidth = 2.5f)
                    drawLine(color, start = Offset(x, py - 12f), end = Offset(x, py + 12f), strokeWidth = 2.5f)
                    drawLine(color, start = Offset(x + 10f, py - 10f), end = Offset(x + 10f, py + 10f), strokeWidth = 2.5f)
                    // Tail (triangle to left)
                    val tailPath = Path().apply {
                        moveTo(x - 25f, py)
                        lineTo(x - 33f, py - 8f)
                        lineTo(x - 33f, py + 8f)
                        close()
                    }
                    drawPath(tailPath, color)
                    
                    y += step
                }
                x += step
            }
        }
        "salmon_coral" -> {
            // Cozy fish ripples + small hearts
            val color = Color.Red.copy(alpha = 0.04f)
            val step = 180f
            var x = 60f
            while (x < size.width) {
                var y = 60f
                while (y < size.height) {
                    val py = y + (if (((x / step).toInt() % 2) == 0) step / 2 else 0f)
                    // Draw a cute fish outline or heart shape
                    drawCircle(color, radius = 10f, center = Offset(x, py))
                    // Small hearts decoration
                    drawLine(color, start = Offset(x - 5f, py - 15f), end = Offset(x + 5f, py - 15f), strokeWidth = 2f)
                    drawLine(color, start = Offset(x - 15f, py - 5f), end = Offset(x - 15f, py + 5f), strokeWidth = 2f)
                    y += step
                }
                x += step
            }
        }
        "cozy_catbed" -> {
            // Simple concentric warm background circles
            val color = Color.Yellow.copy(alpha = 0.05f)
            val step = 200f
            var x = 80f
            while (x < size.width) {
                var y = 80f
                while (y < size.height) {
                    val py = y + (if (((x / step).toInt() % 2) == 0) step / 2 else 0f)
                    drawCircle(color, radius = 25f, center = Offset(x, py), style = androidx.compose.ui.graphics.drawscope.Stroke(2f))
                    drawCircle(color, radius = 5f, center = Offset(x, py))
                    y += step
                }
                x += step
            }
        }
    }
}

// --- MASTER UI COMPOSE ENTRY ---

@Composable
fun VoldyGramApp(viewModel: VoldyGramViewModel) {
    val themeColorName by viewModel.themeColor.collectAsStateWithLifecycle()
    val activeChatId by viewModel.activeChatId.collectAsStateWithLifecycle()

    VoldyGramTheme(activeTheme = themeColorName) {
        val currentWallpaper by viewModel.chatWallpaper.collectAsStateWithLifecycle()
        val currentStoryOpen by viewModel.currentOpenedStory.collectAsStateWithLifecycle()

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // If a chat is active, we overlay the full-screen chat detail.
                // This gives a fluid WhatsApp transition!
                AnimatedContent(
                    targetState = activeChatId,
                    transitionSpec = {
                        slideInHorizontally(initialOffsetX = { it }) + fadeIn() togetherWith
                                slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
                    },
                    label = "ChatScreenTransition"
                ) { chatId ->
                    if (chatId != null) {
                        ChatDetailScreen(
                            chatId = chatId,
                            viewModel = viewModel,
                            wallpaperName = currentWallpaper
                        )
                    } else {
                        VoldyGramDashboard(viewModel = viewModel)
                    }
                }

                // Global Full-Screen Cat Story Overlay Modal
                AnimatedVisibility(
                    visible = currentStoryOpen != null,
                    enter = fadeIn() + scaleIn(initialScale = 0.9f),
                    exit = fadeOut() + scaleOut(targetScale = 0.9f)
                ) {
                    currentStoryOpen?.let { contact ->
                        CatStoryDetailsScreen(
                            contact = contact,
                            onClose = { viewModel.closeStory() }
                        )
                    }
                }
            }
        }
    }
}

// --- DASHBOARD CONTAINER (Multi-Tab Dashboard) ---

@Composable
fun VoldyGramDashboard(viewModel: VoldyGramViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "🐾 VoldyGram",
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Black,
                            fontSize = 24.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.testTag("app_title")
                        )
                    }
                },
                navigationIcon = {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "App Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 16.dp).size(26.dp)
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars).testTag("navigation_bar"),
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
                tonalElevation = 6.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Face, contentDescription = "Chats") },
                    label = { Text("Chats", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("chats_tab_item")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Star, contentDescription = "Voldy AI") },
                    label = { Text("Voldy AI", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("voldy_ai_tab_item")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Playpen") },
                    label = { Text("Playpen", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("playpen_tab_item")
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("settings_tab_item")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> ChatsListTab(viewModel)
                1 -> VoldyDirectAiTab(viewModel)
                2 -> VoldyPlaypenTab(viewModel)
                3 -> CatSettingsTab(viewModel)
            }
        }
    }
}

// --- 1. CHATS LIST TAB ---

@Composable
fun ChatsListTab(viewModel: VoldyGramViewModel) {
    val contacts by viewModel.contacts.collectAsStateWithLifecycle()
    var showAddContactDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredContacts = remember(contacts, searchQuery) {
        if (searchQuery.isBlank()) contacts else {
            contacts.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // Search Input Block
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .testTag("search_chats_input"),
                placeholder = { Text("Search kitty...", fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                )
            )

            // Cat Stories Row block (WhatsApp Status!)
            Text(
                text = "🐾 Cat Stories",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .height(100.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Prepend My Own Story Card
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            // Quick popup about me
                        }
                    ) {
                        Box(
                            contentAlignment = Alignment.BottomEnd,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Text("🙋‍♂️", fontSize = 28.sp)
                            }
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add Story", tint = Color.White, modifier = Modifier.size(12.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("My Story", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                // Contacts with stories
                items(contacts.filter { it.storyText != null }) { contact ->
                    val borderPulse = rememberInfiniteTransition(label = "pulse")
                    val strokeScale by borderPulse.animateFloat(
                        initialValue = 2f,
                        targetValue = 4f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1200, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ), label = "bubblePulse"
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { viewModel.openStory(contact) }
                            .testTag("story_item_${contact.id}")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(58.dp)
                                .clip(CircleShape)
                                .border(
                                    strokeScale.dp,
                                    MaterialTheme.colorScheme.primary,
                                    CircleShape
                                )
                                .padding(4.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(contact.avatarColorHex.toColor())
                            ) {
                                Text(contact.avatarEmoji, fontSize = 26.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = contact.name.take(9),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

            // Contacts List Box
            if (filteredContacts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🙀 No kitties matched...", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "Tap the paw FAB below to recruit another cuddly cat!",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("chats_list"),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredContacts) { contact ->
                        CatContactRow(
                            contact = contact,
                            onClick = { viewModel.selectChat(contact.id) }
                        )
                    }
                }
            }
        }

        // Add Contact Floating Action Button
        LargeFloatingActionButton(
            onClick = { showAddContactDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_contact_fab"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Contact", modifier = Modifier.size(32.dp))
        }

        // Add Contact Dialog Modal
        if (showAddContactDialog) {
            AddCatContactDialog(
                onDismiss = { showAddContactDialog = false },
                onAdd = { name, emoji, status ->
                    viewModel.addNewCatContact(name, emoji, status)
                    showAddContactDialog = false
                }
            )
        }
    }
}

// --- STORY VIEWER OVERLAY ---

@Composable
fun CatStoryDetailsScreen(
    contact: CatContact,
    onClose: () -> Unit
) {
    var progress by remember { mutableStateOf(0f) }

    // Story natural timeout - 5 seconds
    LaunchedEffect(key1 = contact) {
        val duration = 5000f
        val step = 100f
        while (progress < 1.0f) {
            delay((duration / step).toLong())
            progress += 1f / step
        }
        onClose()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.95f))
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClose() }
                )
            }
            .testTag("story_viewer_${contact.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Progress and Exit
            Column {
                // Top Linear Progress Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.weight(1f),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.25f),
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Profile and Timestamp
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(contact.avatarColorHex.toColor())
                        ) {
                            Text(contact.avatarEmoji, fontSize = 24.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(contact.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Posted ${contact.storyTime ?: ""}", color = Color.LightGray, fontSize = 12.sp)
                        }
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("close_story_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close Story", tint = Color.White)
                    }
                }
            }

            // Big Centered Story Card with gorgeous brush styling
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 40.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                contact.avatarColorHex.toColor().copy(alpha = 0.5f),
                                Color.DarkGray.copy(alpha = 0.3f)
                            )
                        )
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(32.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    // Huge cat emoji bouncing
                    Text(
                        text = contact.avatarEmoji,
                        fontSize = 72.sp,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    // Story core text
                    Text(
                        text = contact.storyText ?: "Chilling in my cat bed.",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontStyle = FontStyle.Italic,
                        fontSize = 22.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 32.sp
                    )
                }
            }

            // Simple swipe instructions tip
            Text(
                text = "Tap anywhere to close story 🐾",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// --- REUSABLE CONTACT LIST ROW ---

@Composable
fun CatContactRow(contact: CatContact, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("contact_item_${contact.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Active Story Avatar Style
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(contact.avatarColorHex.toColor())
        ) {
            Text(contact.avatarEmoji, fontSize = 28.sp)
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = contact.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = contact.statusTime,
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = contact.status,
                fontSize = 13.sp,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Unread Indicator Status
        if (contact.isOnline) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF4CAF50)) // Google Green
            )
        }
    }
}

// --- DIALOG: ADD CAT CONTACT ---

@Composable
fun AddCatContactDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, emoji: String, status: String) -> Unit
) {
    var kittyName by remember { mutableStateOf("") }
    var kittyStatus by remember { mutableStateOf("") }
    var selectedEmoji by remember { mutableStateOf("🐱") }
    
    val emojiOptions = listOf("🐱", "🐈", "🦁", "🐯", "😺", "😸", "😻", "😽", "😾")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "😻 Add New Kitty",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Selected Emoji Head
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(8.dp)
                ) {
                    Text(selectedEmoji, fontSize = 36.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Emoji selectors
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                ) {
                    items(emojiOptions) { emoji ->
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .border(
                                    width = if (selectedEmoji == emoji) 2.dp else 0.dp,
                                    color = if (selectedEmoji == emoji) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedEmoji = emoji }
                        ) {
                            Text(emoji, fontSize = 20.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = kittyName,
                    onValueChange = { kittyName = it },
                    label = { Text("Kitty Name") },
                    placeholder = { Text("e.g. Whiskers") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_contact_name_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = kittyStatus,
                    onValueChange = { kittyStatus = it },
                    label = { Text("Current Status") },
                    placeholder = { Text("e.g. Eating lasagna") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_contact_status_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.testTag("add_contact_cancel")) {
                        Text("Cancel", color = Color.Gray, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { onAdd(kittyName, selectedEmoji, kittyStatus) },
                        modifier = Modifier.testTag("add_contact_submit"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Add Kitty", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// --- 2. VOLDY DIRECT AI TAB ---

@Composable
fun VoldyDirectAiTab(viewModel: VoldyGramViewModel) {
    // Redirect to direct messaging page seamlessly for Voldy AI Bot
    VoldyDirectAiLayout(viewModel)
}

@Composable
fun VoldyDirectAiLayout(viewModel: VoldyGramViewModel) {
    var inputText by remember { mutableStateOf("") }
    val currentChatId = "voldy_ai"
    
    // Automatically trigger activeChatId for loading messages
    LaunchedEffect(key1 = currentChatId) {
        viewModel.selectChat(currentChatId)
    }

    val messages by viewModel.currentChatMessages.collectAsStateWithLifecycle()
    val isTyping by viewModel.isVoldyTyping.collectAsStateWithLifecycle()
    val wallpaperName by viewModel.chatWallpaper.collectAsStateWithLifecycle()

    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Automatically scrolls to last message
    LaunchedEffect(messages.size, isTyping) {
        if (messages.isNotEmpty()) {
            lazyListState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .drawCatWallpaper(wallpaperName, MaterialTheme.colorScheme.background)
    ) {
        // Mini Header Card for Voldy's mood inside Chat
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Interactive Bouncing Avatar
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFE4C4))
                ) {
                    Text("🐈", fontSize = 24.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text("Voldy AI Companion 🐾", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Active, purring quietly.", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
                
                // Refresh Chat button
                IconButton(onClick = { viewModel.clearChatMessages(currentChatId) }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Clear Chat", tint = Color.Gray)
                }
            }
        }

        // Messages Box Scrolling Thread
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 8.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(messages) { message ->
                ChatBubble(message = message)
            }

            if (isTyping) {
                item {
                    FelineTypingIndicator()
                }
            }
        }

        // Special quick Cat Gifts Pill box row!
        Text(
            text = "🎁 Feed or Play with Voldy:",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GiftPillButton(
                label = "🐟 Salmon Pate",
                color = Color(0xFFFFCCBC),
                onClick = { viewModel.sendMessage("", "SALMON") }
            )
            GiftPillButton(
                label = "🌿 Catnip Toy",
                color = Color(0xFFC8E6C9),
                onClick = { viewModel.sendMessage("", "CATNIP") }
            )
            GiftPillButton(
                label = "🔴 Laser pointer",
                color = Color(0xFFFFCDD2),
                onClick = { viewModel.sendMessage("", "LASER") }
            )
            GiftPillButton(
                label = "🧶 Roll of Yarn",
                onClick = { viewModel.sendMessage("Here is a roll of colorful yarn! 🧶") }
            )
        }

        // Send chat layout
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("direct_chat_input"),
                placeholder = { Text("Meow a message to Voldy...") },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.25f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                trailingIcon = {
                    Row(modifier = Modifier.padding(end = 4.dp)) {
                        IconButton(onClick = { inputText += "🐾" }) {
                            Text("🐾", fontSize = 18.sp)
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendMessage(inputText)
                        inputText = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .testTag("direct_chat_send"),
                colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send Direct Msg"
                )
            }
        }
    }
}

@Composable
fun GiftPillButton(
    label: String,
    color: Color = MaterialTheme.colorScheme.secondaryContainer,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(color)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// --- 3. VOLDY PLAYPEN TAB ---

@Composable
fun VoldyPlaypenTab(viewModel: VoldyGramViewModel) {
    val happiness by viewModel.playpenVoldyHappiness.collectAsStateWithLifecycle()
    val bowlLevel by viewModel.bowlFillLevel.collectAsStateWithLifecycle()
    val playpenStatus by viewModel.voldyPlaypenStatus.collectAsStateWithLifecycle()
    
    // Laser canvas interactive touch states
    var touchX by remember { mutableStateOf(-1f) }
    var touchY by remember { mutableStateOf(-1f) }
    var pawX by remember { mutableFloatStateOf(400f) }
    var pawY by remember { mutableFloatStateOf(400f) }

    // Paw follows laser pointer (with spring transition!)
    LaunchedEffect(key1 = touchX, key2 = touchY) {
        if (touchX > 0 && touchY > 0) {
            // Animate paw towards coordinates smoothly
            val duration = 800
            val steps = 20
            val diffX = (touchX - pawX) / steps
            val diffY = (touchY - pawY) / steps
            for (i in 1..steps) {
                delay((duration / steps).toLong())
                pawX += diffX
                pawY += diffY
            }
            // Trigger feedback in ViewModel!
            viewModel.laserChasings()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Applet Banner Hero Banner!
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🦁 Voldy's Playpen", fontSize = 20.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                Text(
                    "Keep your cozy cat happy! Track food bowls and play with laser beans.",
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Happiness Widget Panel
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Happiness State:", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(
                        text = "$happiness% ${if (happiness >= 80) "🥰 Ecstatic" else if (happiness >= 50) "😸 Content" else "😾 Sassy"}",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Beautiful custom slider-style visual bar
                LinearProgressIndicator(
                    progress = { happiness / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .testTag("happiness_progress"),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primaryContainer
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "💬 Status: $playpenStatus",
                    fontSize = 12.sp,
                    fontStyle = FontStyle.Italic,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }

        // LASER INTERACTIVE CANVAS
        Text(
            text = "🔴 Interactive Laser Chase Canvas",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                .background(Color(0xFF333333)) // Dark slate playground
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            touchX = offset.x
                            touchY = offset.y
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            touchX = change.position.x
                            touchY = change.position.y
                        },
                        onDragEnd = {
                            touchX = -1f
                            touchY = -1f
                        }
                    )
                    detectTapGestures(
                        onPress = { offset ->
                            touchX = offset.x
                            touchY = offset.y
                            tryAwaitRelease()
                            touchX = -1f
                            touchY = -1f
                        }
                    )
                }
                .testTag("laser_playground_canvas")
        ) {
            // Draw interactive overlays
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Draw grid lines
                val gridColor = Color.White.copy(alpha = 0.05f)
                var x = 0f
                while (x < size.width) {
                    drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
                    x += 40f
                }
                var y = 0f
                while (y < size.height) {
                    drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                    y += 40f
                }

                // Draw laser outer glow
                if (touchX > 0 && touchY > 0) {
                    drawCircle(Color.Red.copy(alpha = 0.3f), radius = 25f, center = Offset(touchX, touchY))
                    drawCircle(Color.Red, radius = 8f, center = Offset(touchX, touchY))
                }
            }

            // Clumsy Paws Swiping Overlay!
            Box(
                modifier = Modifier
                    .offset(x = (pawX / 3).dp, y = (pawY / 3).dp) // Adjust scaling fit
                    .size(54.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("🐾", fontSize = 42.sp)
            }

            if (touchX < 0) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Drag your finger inside to shine a laser! 🔴\nVoldy's paw will chase it!",
                        textAlign = TextAlign.Center,
                        color = Color.LightGray.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // FEEDING APPLET CONTROLS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Kibble Bowl Applet Widget
            Card(
                modifier = Modifier.weight(1f),
                onClick = { viewModel.fillBowl() },
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🍜 Food Bowl", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Render custom bowl fill status
                    val kibbles = when (bowlLevel) {
                        3 -> "🍖🍖🍖"
                        2 -> "🍖🍖"
                        1 -> "🍖"
                        else -> "🥣 (Empty)"
                    }
                    Text(kibbles, fontSize = 28.sp)
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = if (bowlLevel == 0) "Tap to fill" else "Voldy is eating!",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Stroking petting Button Panel
            Card(
                modifier = Modifier.weight(1f),
                onClick = { viewModel.petPlaypenVoldy() },
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🧶 Give Petting", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text("👋😻", fontSize = 28.sp)
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Leans and purrs",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// --- 4. CAT SETTINGS TAB ---

@Composable
fun CatSettingsTab(viewModel: VoldyGramViewModel) {
    val wallpaper by viewModel.chatWallpaper.collectAsStateWithLifecycle()
    val activeThemeName by viewModel.themeColor.collectAsStateWithLifecycle()
    var showResetDatabaseNotice by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "🎨 Theme Style Selectors",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Theme Options Cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ThemeCard(
                label = "Classic Meow",
                colorsEmoji = "🐈",
                bgColor = Color(0xFFFFE3D3),
                isSelected = activeThemeName == "classic_meow",
                onClick = { viewModel.setThemeColor("classic_meow") }
            )
            ThemeCard(
                label = "Catmint Green",
                colorsEmoji = "🌿",
                bgColor = Color(0xFFD0F8F1),
                isSelected = activeThemeName == "catmint_green",
                onClick = { viewModel.setThemeColor("catmint_green") }
            )
            ThemeCard(
                label = "Sweet Salmon",
                colorsEmoji = "🐟",
                bgColor = Color(0xFFFFECE9),
                isSelected = activeThemeName == "sweet_salmon",
                onClick = { viewModel.setThemeColor("sweet_salmon") }
            )
        }

        Text(
            text = "🖼️ Cat Wallpaper Background (Chat)",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Wallpaper selector grids
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                WallpaperOptionCard(
                    title = "🐾 Paw Meadow",
                    wallpaperValue = "paw_prints",
                    isSelected = wallpaper == "paw_prints",
                    onClick = { viewModel.setWallpaper("paw_prints") },
                    modifier = Modifier.weight(1f)
                )
                WallpaperOptionCard(
                    title = "🐟 Salmon Ripples",
                    wallpaperValue = "salmon_coral",
                    isSelected = wallpaper == "salmon_coral",
                    onClick = { viewModel.setWallpaper("salmon_coral") },
                    modifier = Modifier.weight(1f)
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                WallpaperOptionCard(
                    title = "🦴 Fish skeletons",
                    wallpaperValue = "fish_bones",
                    isSelected = wallpaper == "fish_bones",
                    onClick = { viewModel.setWallpaper("fish_bones") },
                    modifier = Modifier.weight(1f)
                )
                WallpaperOptionCard(
                    title = "🛋️ Cute Cushion",
                    wallpaperValue = "cozy_catbed",
                    isSelected = wallpaper == "cozy_catbed",
                    onClick = { viewModel.setWallpaper("cozy_catbed") },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
        Spacer(modifier = Modifier.height(16.dp))

        // Actions
        Text(
            text = "⚙️ Maintenance Controls",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showResetDatabaseNotice = true },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Reset", tint = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Reset Database", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
                    Text("Wipes all custom cats and recreates starter kitty contacts.", fontSize = 11.sp, color = Color.Gray)
                }
            }
        }

        // Reset dialog confirmation
        if (showResetDatabaseNotice) {
            AlertDialog(
                onDismissRequest = { showResetDatabaseNotice = false },
                title = { Text("Reset VoldyGram database? 🙀") },
                text = { Text("This deletes all customized chat history, new kitty contacts, and resets Voldy AI to initial greeting.") },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        onClick = {
                            viewModel.clearChatMessages("voldy_ai")
                            viewModel.clearChatMessages("luna")
                            viewModel.clearChatMessages("garfield")
                            viewModel.clearChatMessages("whiskers")
                            showResetDatabaseNotice = false
                        }
                    ) {
                        Text("Reset ALL", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDatabaseNotice = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun RowScope.ThemeCard(
    label: String,
    colorsEmoji: String,
    bgColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .weight(1f)
            .clickable { onClick() }
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(colorsEmoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color.DarkGray)
        }
    }
}

@Composable
fun WallpaperOptionCard(
    title: String,
    wallpaperValue: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onClick() }
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// --- ACTIVE CHAT DETAIL SCREEN OVERLAY ---

@Composable
fun ChatDetailScreen(
    chatId: String,
    viewModel: VoldyGramViewModel,
    wallpaperName: String
) {
    val activeContactState by viewModel.activeContact.collectAsStateWithLifecycle()
    val messages by viewModel.currentChatMessages.collectAsStateWithLifecycle()
    val isTyping by viewModel.isVoldyTyping.collectAsStateWithLifecycle()

    val lazyListState = rememberLazyListState()
    var userText by remember { mutableStateOf("") }

    LaunchedEffect(messages.size, isTyping) {
        if (messages.isNotEmpty()) {
            lazyListState.animateScrollToItem(messages.size - 1)
        }
    }

    activeContactState?.let { contact ->
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,
                            modifier = Modifier.fillMaxWidth().padding(end = 16.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(contact.avatarColorHex.toColor())
                            ) {
                                Text(contact.avatarEmoji, fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = contact.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isTyping) "feline typing..." else contact.lastSeen,
                                    fontSize = 11.sp,
                                    color = if (isTyping) MaterialTheme.colorScheme.primary else Color.Gray,
                                    fontWeight = if (isTyping) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { viewModel.closeChat() },
                            modifier = Modifier.testTag("chat_back_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.clearChatMessages(chatId) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Clear Session", tint = Color.Gray)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                    )
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .drawCatWallpaper(wallpaperName, MaterialTheme.colorScheme.background)
            ) {
                // Thread lists
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    items(messages) { message ->
                        ChatBubble(message = message)
                    }

                    if (isTyping) {
                        item {
                            FelineTypingIndicator()
                        }
                    }
                }

                // Short cat reaction suggestion pills
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    GiftPillButton(label = "🧶 Roll yarn", onClick = { userText += "🧶 Here, play with some yarn!" })
                    GiftPillButton(label = "🐟 Gimme fish", onClick = { userText += "🐟 I got you yummy salmon fish!" })
                    GiftPillButton(label = "🐈 Love you!", onClick = { userText += "😻 You are the cutest kitty ever!" })
                    GiftPillButton(label = "🔴 Run dot!", onClick = { userText += "🔴 Watch the red dot!" })
                    GiftPillButton(label = "🌿 Sniff herb", onClick = { userText += "🌿 Here is some sweet catnip herb!" })
                }

                // Send Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = userText,
                        onValueChange = { userText = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_text"),
                        placeholder = { Text("Meow a reply...", fontSize = 14.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.25f),
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (userText.isNotBlank()) {
                                viewModel.sendMessage(userText)
                                userText = ""
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .testTag("chat_send_button"),
                        colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send Message"
                        )
                    }
                }
            }
        }
    }
}

// --- CHAT BUBBLE RENDERER ---

@Composable
fun ChatBubble(message: CatMessage) {
    val isMe = message.senderId == "me"
    
    val alignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
    val bubbleColor = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    val radiusCorner = if (isMe) RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp) else RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = alignment
    ) {
        Column(
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            // Optional special gift card wrapper
            if (message.interactiveActionType != null) {
                Card(
                    modifier = Modifier.padding(bottom = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🎁", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sent Gift Action: ${message.interactiveActionType}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Surface(
                color = bubbleColor,
                shape = radiusCorner,
                tonalElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Sender label for group-chats style
                    if (!isMe) {
                        Text(
                            text = message.senderName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                    
                    Text(
                        text = message.text,
                        color = textColor,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

// --- TYPING INDICATOR MOVEMENT ---

@Composable
fun FelineTypingIndicator() {
    val transition = rememberInfiniteTransition(label = "typing")
    val translationY by transition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ), label = "typingDot"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "🐾",
                    fontSize = 14.sp,
                    modifier = Modifier.offset(y = translationY.dp)
                )
                Text(
                    "kitty is typing...",
                    fontSize = 12.sp,
                    fontStyle = FontStyle.Italic,
                    color = Color.Gray
                )
            }
        }
    }
}
