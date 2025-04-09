package com.programmersbox.fiveofakind

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
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