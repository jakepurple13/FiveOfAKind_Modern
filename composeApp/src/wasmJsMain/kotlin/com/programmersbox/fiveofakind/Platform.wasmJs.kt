package com.programmersbox.fiveofakind

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.materialkolor.rememberDynamicColorScheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class WasmPlatform: Platform {
    override val name: String = "Web with Kotlin/Wasm"
}

actual fun getPlatform(): Platform = WasmPlatform()

@Composable
actual fun rememberShowDotsOnDice(): MutableState<Boolean> = remember {
    mutableStateOf(false)
}

@Composable
actual fun rememberUse24HourTime(): MutableState<Boolean> = remember {
    mutableStateOf(false)
}

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

@Composable
actual fun rememberIsAmoled(): MutableState<Boolean> {
    return remember {
        mutableStateOf(false)
    }
}

@Composable
actual fun rememberThemeColor(): MutableState<ThemeColor> {
    return remember {
        mutableStateOf(ThemeColor.Dynamic)
    }
}