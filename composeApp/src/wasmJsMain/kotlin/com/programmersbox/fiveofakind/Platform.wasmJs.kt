package com.programmersbox.fiveofakind

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class WasmPlatform: Platform {
    override val name: String = "Web with Kotlin/Wasm"
}

actual fun getPlatform(): Platform = WasmPlatform()

@Composable
actual fun rememberShowDotsOnDice(): MutableState<Boolean> = mutableStateOf(false)

@Composable
actual fun rememberUse24HourTime(): MutableState<Boolean> = mutableStateOf(false)

actual class YahtzeeDatabase {
    actual fun getHighScores(): Flow<List<ActualYahtzeeScoreItem>> = emptyFlow()
    actual suspend fun addHighScore(scoreItem: ActualYahtzeeScoreItem) {}
    actual suspend fun removeHighScore(scoreItem: ActualYahtzeeScoreItem) {}
    actual fun getHighScoreStats(): Flow<List<ActualYahtzeeScoreStat>> = emptyFlow()
}