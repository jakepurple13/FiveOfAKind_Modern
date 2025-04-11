package com.programmersbox.fiveofakind

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import com.materialkolor.rememberDynamicColorScheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class WasmPlatform: Platform {
    override val name: String = "Web with Kotlin/Wasm"
}

actual fun getPlatform(): Platform = WasmPlatform()

actual class YahtzeeDatabase {
    actual fun getHighScores(): Flow<List<ActualYahtzeeScoreItem>> = emptyFlow()
    actual suspend fun addHighScore(scoreItem: ActualYahtzeeScoreItem) {}
    actual suspend fun removeHighScore(scoreItem: ActualYahtzeeScoreItem) {}
    actual fun getHighScoreStats(): Flow<List<ActualYahtzeeScoreStat>> = emptyFlow()
}

@Composable
actual fun colorSchemeSetup(isDarkMode: Boolean, dynamicColor: Boolean): ColorScheme {
    return rememberDynamicColorScheme(
        Color.Blue,
        isDark = isDarkMode,
        isAmoled = false
    )
}

private val showDotsOnDice = mutableStateOf(false)

@Composable
actual fun rememberShowDotsOnDice(): MutableState<Boolean> = showDotsOnDice

private val use24HourTime = mutableStateOf(false)

@Composable
actual fun rememberUse24HourTime(): MutableState<Boolean> = use24HourTime

private val isAmoled = mutableStateOf(false)

@Composable
actual fun rememberIsAmoled(): MutableState<Boolean> = isAmoled

private val themeColor = mutableStateOf(ThemeColor.Dynamic)

@Composable
actual fun rememberThemeColor(): MutableState<ThemeColor> = themeColor

private val showInstructions = mutableStateOf(false)

@Composable
actual fun rememberShowInstructions(): MutableState<Boolean> = showInstructions