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

actual class YahtzeeDatabase {

    /*actual fun getHighScores(): Flow<List<ActualYahtzeeScoreItem>> = emptyFlow()
    actual suspend fun addHighScore(scoreItem: ActualYahtzeeScoreItem) {}
    actual suspend fun removeHighScore(scoreItem: ActualYahtzeeScoreItem) {}
    actual fun getHighScoreStats(): Flow<List<ActualYahtzeeScoreStat>> = emptyFlow()*/

    private val statsStuff = storeOf<List<ActualYahtzeeScoreStat>>(
        key = "statsStuff",
        default = emptyList(),
    )

    private val highScoreStuff = storeOf<List<ActualYahtzeeScoreItem>>(
        key = "highScoreStuff",
        default = emptyList(),
    )

    actual fun getHighScores(): Flow<List<ActualYahtzeeScoreItem>> = highScoreStuff
        .updatesOrEmpty
        .filterNotNull()

    actual suspend fun addHighScore(scoreItem: ActualYahtzeeScoreItem) {
        runCatching {
            val current = highScoreStuff.getOrEmpty().toMutableList()
            current.add(scoreItem)
            highScoreStuff.set(current.sortedByDescending { it.totalScore }.take(15))

            val yahtzeeScoreItem = scoreItem

            val list = mapOf(
                HandType.Ones to yahtzeeScoreItem::ones,
                HandType.Twos to yahtzeeScoreItem::twos,
                HandType.Threes to yahtzeeScoreItem::threes,
                HandType.Fours to yahtzeeScoreItem::fours,
                HandType.Fives to yahtzeeScoreItem::fives,
                HandType.Sixes to yahtzeeScoreItem::sixes,
                HandType.ThreeOfAKind to yahtzeeScoreItem::threeKind,
                HandType.FourOfAKind to yahtzeeScoreItem::fourKind,
                HandType.FullHouse to yahtzeeScoreItem::fullHouse,
                HandType.SmallStraight to yahtzeeScoreItem::smallStraight,
                HandType.LargeStraight to yahtzeeScoreItem::largeStraight,
                HandType.Chance to yahtzeeScoreItem::chance,
                HandType.FiveOfAKind to yahtzeeScoreItem::yahtzee,
            )

            val stats = statsStuff.getOrEmpty().toMutableList()

            for (i in list) {
                if (i.value.get() == 0) continue

                val newStat = stats
                    .indexOfFirst { stat -> stat.handType == i.key.name }
                    .takeIf { it != -1 }
                    ?.let { stats.removeAt(it) }
                    ?.let { stat ->
                        stat.copy(
                            numberOfTimes = stat.numberOfTimes + 1,
                            totalPoints = stat.totalPoints + i.value.get(),
                        )
                    } ?: ActualYahtzeeScoreStat(
                    handType = i.key.name,
                    numberOfTimes = 1,
                    totalPoints = i.value.get().toLong(),
                )

                statsStuff.set(
                    stats
                        .apply { add(newStat) }
                        .sortedByDescending { it.totalPoints }
                )
            }
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