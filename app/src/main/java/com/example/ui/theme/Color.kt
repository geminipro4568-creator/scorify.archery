package com.example.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

// Global theme state for dynamic toggle
var isDarkThemeGlobal by mutableStateOf(true)

// Cyber-sport "Scorify" Color Palette (Dynamic getters for light/dark support)
val CharcoalBg: Color
    get() = if (isDarkThemeGlobal) Color(0xFF121212) else Color(0xFFF5F5F7)

val CharcoalSurface: Color
    get() = if (isDarkThemeGlobal) Color(0xFF1A1A1A) else Color(0xFFFFFFFF)

val CharcoalCard: Color
    get() = if (isDarkThemeGlobal) Color(0xFF242424) else Color(0xFFEFEFF4)

val CharcoalDark: Color
    get() = if (isDarkThemeGlobal) Color(0xFF0A0A0A) else Color(0xFFE5E5EA)

val CrispWhite: Color
    get() = if (isDarkThemeGlobal) Color(0xFFFFFFFF) else Color(0xFF121212)

val CoolGray: Color
    get() = if (isDarkThemeGlobal) Color(0xFF8E8E93) else Color(0xFF5E5E62)

// Electric Archery Colors
val ArcheryGold = Color(0xFFFFD700)
val ArcheryRed = Color(0xFFFF3333)
val ArcheryBlue = Color(0xFF3366FF)
val ArcheryBlack = Color(0xFF2C2C2C)
val ArcheryWhite = Color(0xFFEEEEEE)

// Theme semantics
val CyberGreen = Color(0xFF00FF66)
val AlertOrange = Color(0xFFFF9500)
