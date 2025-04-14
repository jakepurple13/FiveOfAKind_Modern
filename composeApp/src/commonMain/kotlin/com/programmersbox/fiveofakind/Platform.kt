package com.programmersbox.fiveofakind

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.graphics.Color
import com.materialkolor.rememberDynamicColorScheme
import kotlinx.coroutines.flow.Flow

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect class YahtzeeDatabase {
    fun getHighScores(): Flow<List<ActualYahtzeeScoreItem>>
    suspend fun addHighScore(scoreItem: ActualYahtzeeScoreItem)
    suspend fun removeHighScore(scoreItem: ActualYahtzeeScoreItem)
    fun getHighScoreStats(): Flow<List<ActualYahtzeeScoreStat>>
}

@Composable
expect fun rememberShowDotsOnDice(): MutableState<Boolean>

@Composable
expect fun rememberUse24HourTime(): MutableState<Boolean>

@Composable
expect fun rememberThemeColor(): MutableState<ThemeColor>

@Composable
expect fun rememberIsAmoled(): MutableState<Boolean>

@Composable
expect fun rememberShowInstructions(): MutableState<Boolean>

@Composable
expect fun colorSchemeSetup(isDarkMode: Boolean, dynamicColor: Boolean): ColorScheme

@Composable
fun buildColorScheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
): ColorScheme {
    val themeColor by rememberThemeColor()
    val isAmoled by rememberIsAmoled()
    return key(themeColor) {
        when (themeColor) {
            ThemeColor.Dynamic -> colorSchemeSetup(darkTheme, dynamicColor).let {
                if (isAmoled && darkTheme) {
                    it.copy(
                        surface = Color.Black,
                        onSurface = Color.White,
                        background = Color.Black,
                        onBackground = Color.White
                    )
                } else {
                    it
                }
            }

            else -> rememberDynamicColorScheme(
                seedColor = themeColor.seedColor,
                isDark = darkTheme,
                isAmoled = isAmoled
            )
        }
    }
}

expect suspend fun saveYahtzeeGame(game: SavedYahtzeeGame)
expect suspend fun loadYahtzeeGame(): SavedYahtzeeGame?
expect suspend fun hasSavedYahtzeeGame(): Boolean
expect suspend fun deleteSavedYahtzeeGame()
