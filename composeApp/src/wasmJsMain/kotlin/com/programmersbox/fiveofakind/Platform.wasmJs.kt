package com.programmersbox.fiveofakind

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.materialkolor.rememberDynamicColorScheme
import io.github.xxfast.kstore.extensions.getOrEmpty
import io.github.xxfast.kstore.extensions.updatesOrEmpty
import io.github.xxfast.kstore.storage.storeOf
import kotlinx.browser.localStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull

class WasmPlatform : Platform {
    override val name: String = "Web with Kotlin/Wasm"
}

actual fun getPlatform(): Platform = WasmPlatform()

private val highScoreStuff = storeOf<List<ActualYahtzeeScoreItem>>(
    key = "highScoreStuff",
    default = emptyList(),
)

private val statsStuff = storeOf<List<ActualYahtzeeScoreStat>>(
    key = "statsStuff",
    default = emptyList(),
)

actual class YahtzeeDatabase {

    init {
        for (i in 0..localStorage.length) {
            println(localStorage.key(i) + " " + localStorage.getItem(localStorage.key(i)!!))
        }
    }

    /*actual fun getHighScores(): Flow<List<ActualYahtzeeScoreItem>> = emptyFlow()
    actual suspend fun addHighScore(scoreItem: ActualYahtzeeScoreItem) {}
    actual suspend fun removeHighScore(scoreItem: ActualYahtzeeScoreItem) {}
    actual fun getHighScoreStats(): Flow<List<ActualYahtzeeScoreStat>> = emptyFlow()*/

    actual fun getHighScores(): Flow<List<ActualYahtzeeScoreItem>> = highScoreStuff
        .updatesOrEmpty
        .filterNotNull()

    actual suspend fun addHighScore(scoreItem: ActualYahtzeeScoreItem) {
        runCatching {
            val current = highScoreStuff.getOrEmpty().toMutableList()
            current.add(scoreItem)
            highScoreStuff.set(current.sortedByDescending { it.totalScore }.take(15))
        }
    }

    actual suspend fun removeHighScore(scoreItem: ActualYahtzeeScoreItem) {
        runCatching {
            val current = highScoreStuff.getOrEmpty().toMutableList()
            current.remove(scoreItem)
            highScoreStuff.set(current)
        }
    }

    actual fun getHighScoreStats(): Flow<List<ActualYahtzeeScoreStat>> = statsStuff
        .updatesOrEmpty
        .filterNotNull()
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
fun <T> rememberPreference(
    mutableState: MutableState<T>,
    key: String,
    valueToString: (T) -> String,
): MutableState<T> {
    val state by mutableState

    return remember(state) {
        object : MutableState<T> {
            override var value: T
                get() = state
                set(value) {
                    mutableState.value = value
                    localStorage.setItem(key, valueToString(value))
                }

            override fun component1() = value
            override fun component2(): (T) -> Unit = { value = it }
        }
    }
}

private val showDotsOnDice by lazy {
    mutableStateOf(
        localStorage
            .getItem("showDotsOnDice")
            ?.toBoolean()
            ?: true
    )
}

@Composable
actual fun rememberShowDotsOnDice(): MutableState<Boolean> = rememberPreference(
    key = "showDotsOnDice",
    mutableState = showDotsOnDice,
    valueToString = { it.toString() }
)

private val use24HourTime by lazy {
    mutableStateOf(
        localStorage
            .getItem("use24HourTime")
            ?.toBoolean()
            ?: true
    )
}

@Composable
actual fun rememberUse24HourTime(): MutableState<Boolean> = rememberPreference(
    key = "use24HourTime",
    mutableState = use24HourTime,
    valueToString = { it.toString() }
)

private val isAmoled by lazy {
    mutableStateOf(
        localStorage
            .getItem("isAmoled")
            ?.toBoolean()
            ?: false
    )
}

@Composable
actual fun rememberIsAmoled(): MutableState<Boolean> = rememberPreference(
    key = "isAmoled",
    mutableState = isAmoled,
    valueToString = { it.toString() }
)

private val themeColor by lazy {
    mutableStateOf(
        runCatching {
            localStorage
                .getItem("themeColor")
                ?.let { ThemeColor.valueOf(it) }
        }.getOrNull() ?: ThemeColor.Dynamic
    )
}

@Composable
actual fun rememberThemeColor(): MutableState<ThemeColor> = rememberPreference(
    key = "themeColor",
    mutableState = themeColor,
    valueToString = { it.name }
)

private val showInstructions by lazy {
    mutableStateOf(
        localStorage
            .getItem("showInstructions")
            ?.toBoolean()
            ?: true
    )
}

@Composable
actual fun rememberShowInstructions(): MutableState<Boolean> = rememberPreference(
    key = "showInstructions",
    mutableState = showInstructions,
    valueToString = { it.toString() }
)