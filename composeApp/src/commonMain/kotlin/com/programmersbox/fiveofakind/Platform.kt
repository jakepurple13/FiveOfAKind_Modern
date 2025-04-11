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
import kotlinx.datetime.Clock

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

@Composable
expect fun rememberShowDotsOnDice(): MutableState<Boolean>

@Composable
expect fun rememberUse24HourTime(): MutableState<Boolean>

expect class YahtzeeDatabase {
    fun getHighScores(): Flow<List<ActualYahtzeeScoreItem>>
    suspend fun addHighScore(scoreItem: ActualYahtzeeScoreItem)
    suspend fun removeHighScore(scoreItem: ActualYahtzeeScoreItem)
    fun getHighScoreStats(): Flow<List<ActualYahtzeeScoreStat>>
}

data class ActualYahtzeeScoreItem(
    val time: Long = Clock.System.now().toEpochMilliseconds(),
    val ones: Int = 0,
    val twos: Int = 0,
    val threes: Int = 0,
    val fours: Int = 0,
    val fives: Int = 0,
    val sixes: Int = 0,
    val threeKind: Int = 0,
    val fourKind: Int = 0,
    val fullHouse: Int = 0,
    val smallStraight: Int = 0,
    val largeStraight: Int = 0,
    val yahtzee: Int = 0,
    val chance: Int = 0,
) {
    val smallScore get() = ones + twos + threes + fours + fives + sixes
    val largeScore get() = threeKind + fourKind + fullHouse + smallStraight + largeStraight + yahtzee + chance
    val totalScore get() = largeScore + smallScore + if (smallScore >= 63) 35 else 0
}

data class ActualYahtzeeScoreStat(
    val handType: String,
    val numberOfTimes: Int = 0,
    val totalPoints: Long = 0L,
)

@Composable
expect fun rememberThemeColor(): MutableState<ThemeColor>

@Composable
expect fun rememberIsAmoled(): MutableState<Boolean>

@Composable
expect fun rememberShowInstructions(): MutableState<Boolean>

@Composable
expect fun colorSchemeSetup(isDarkMode: Boolean, dynamicColor: Boolean): ColorScheme

enum class ThemeColor(
    val seedColor: Color,
) {
    Dynamic(Color.Transparent),
    Blue(Color.Blue),
    Red(Color.Red),
    Green(Color.Green),
    Yellow(Color.Yellow),
    Cyan(Color.Cyan),
    Magenta(Color.Magenta),
    Custom(Color.Transparent),
}

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
