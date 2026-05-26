package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// --- COLOR DEFINITIONS ---

// Helper function to decode safe theme HEX strings
fun String.toColor(): Color {
    return try {
        Color(android.graphics.Color.parseColor(this))
    } catch (e: Exception) {
        Color(0xFFFF9800) // Fallback to ginger orange
    }
}

// 1. Classic Meow (Ginger/Cream) Theme
private val ClassicMeowLight = lightColorScheme(
    primary = Color(0xFFE65100), // Rich Ginger Orange
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE0B2), // Soft Creamy Peach
    onPrimaryContainer = Color(0xFF5D1B00),
    secondary = Color(0xFF8D6E63), // Cozy Warm Brown
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEFEBE9),
    background = Color(0xFFFFFBF7), // Cozy feline blanket peach-white
    surface = Color(0xFFFFF5EC), // Soft cat-bed cream
    onBackground = Color(0xFF3E2723),
    onSurface = Color(0xFF3E2723)
)

private val ClassicMeowDark = darkColorScheme(
    primary = Color(0xFFFFB74D), // Soft Ginger-Gold
    onPrimary = Color(0xFF4E2500),
    primaryContainer = Color(0xFF7E3900),
    onPrimaryContainer = Color(0xFFFFD180),
    secondary = Color(0xFFD7CCC8),
    onSecondary = Color(0xFF3E2723),
    background = Color(0xFF1E1715), // Deep Cocoa Chocolate
    surface = Color(0xFF2E221E),
    onBackground = Color(0xFFEFEBE9),
    onSurface = Color(0xFFEFEBE9)
)

// 2. Catmint Green (Teal/Mint) Theme
private val CatmintGreenLight = lightColorScheme(
    primary = Color(0xFF00796B), // Deep Herbal Mint
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2F1), // Sweet Green Mint Tea
    onPrimaryContainer = Color(0xFF003730),
    secondary = Color(0xFF558B2F), // Catnip Green
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF1F8E9),
    background = Color(0xFFF4FAF9),
    surface = Color(0xFFEDF7F6),
    onBackground = Color(0xFF004D40),
    onSurface = Color(0xFF004D40)
)

private val CatmintGreenDark = darkColorScheme(
    primary = Color(0xFF80CBC4),
    onPrimary = Color(0xFF004D40),
    primaryContainer = Color(0xFF00796B),
    onPrimaryContainer = Color(0xFFE0F2F1),
    secondary = Color(0xFFAED581),
    onSecondary = Color(0xFF1B5E20),
    background = Color(0xFF121A19),
    surface = Color(0xFF1D2826),
    onBackground = Color(0xFFE0F2F1),
    onSurface = Color(0xFFE0F2F1)
)

// 3. Sweet Salmon (Coral/Salmon Pink) Theme
private val SweetSalmonLight = lightColorScheme(
    primary = Color(0xFFD84315), // Salmon Paste Orange/Pink
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFEBE7), // Soft Salmon Pink
    onPrimaryContainer = Color(0xFF5D0E00),
    secondary = Color(0xFFFF7043),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFF3E0),
    background = Color(0xFFFFF7F5),
    surface = Color(0xFFFFF1EE),
    onBackground = Color(0xFF4E1D13),
    onSurface = Color(0xFF4E1D13)
)

private val SweetSalmonDark = darkColorScheme(
    primary = Color(0xFFFF8A65),
    onPrimary = Color(0xFF5D1200),
    primaryContainer = Color(0xFFC41C00),
    onPrimaryContainer = Color(0xFFFFE57F),
    secondary = Color(0xFFFFA726),
    onSecondary = Color(0xFF3E2723),
    background = Color(0xFF1D1412),
    surface = Color(0xFF281C19),
    onBackground = Color(0xFFFFEBE7),
    onSurface = Color(0xFFFFEBE7)
)

@Composable
fun VoldyGramTheme(
    activeTheme: String = "classic_meow",
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when (activeTheme) {
        "catmint_green" -> if (darkTheme) CatmintGreenDark else CatmintGreenLight
        "sweet_salmon" -> if (darkTheme) SweetSalmonDark else SweetSalmonLight
        else -> if (darkTheme) ClassicMeowDark else ClassicMeowLight
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Retain MyApplicationTheme for backwards compatibility with tests and preview assets
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Ignored to strictly enforce our adorable custom cat theme
    content: @Composable () -> Unit
) {
    VoldyGramTheme(activeTheme = "classic_meow", darkTheme = darkTheme, content = content)
}
