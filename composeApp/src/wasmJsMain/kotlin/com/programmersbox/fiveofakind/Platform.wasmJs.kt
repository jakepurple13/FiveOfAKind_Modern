package com.programmersbox.fiveofakind

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEvent
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.materialkolor.rememberDynamicColorScheme
import io.github.xxfast.kstore.extensions.getOrEmpty
import io.github.xxfast.kstore.extensions.updatesOrEmpty
import io.github.xxfast.kstore.storage.storeOf
import kotlinx.browser.localStorage
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

class WasmPlatform : Platform {
    override val name: String = "Web with Kotlin/Wasm"
}

actual fun getPlatform(): Platform = WasmPlatform()

actual class YahtzeeDatabase {

    private val statsStuff by lazy {
        storeOf<List<ActualYahtzeeScoreStat>>(
            key = "statsStuff",
            default = emptyList()
        )
    }

    private val highScoreStuff by lazy {
        storeOf<List<ActualYahtzeeScoreItem>>(
            key = "yahtzeeScoreStuff",
            default = emptyList()
        )
    }

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
        .map {
            it.sortedBy {
                runCatching { HandType.valueOf(it.handType) }
                    .getOrNull()
                    ?.ordinal
                    ?: Int.MAX_VALUE
            }
        }
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
                    runCatching {
                        localStorage.setItem(key, valueToString(value))
                    }
                }

            override fun component1() = value
            override fun component2(): (T) -> Unit = { value = it }
        }
    }
}

@Composable
actual fun rememberShowInstructions(): MutableState<Boolean> = rememberSettingsPreferences(
    onGet = { it.showInstructions },
    onSet = { s, v -> s.copy(showInstructions = v) }
)

@Composable
actual fun rememberThemeColor(): MutableState<ThemeColor> = rememberSettingsPreferences(
    onGet = { it.themeColor },
    onSet = { s, v -> s.copy(themeColor = v) }
)

@Composable
actual fun rememberIsAmoled(): MutableState<Boolean> = rememberSettingsPreferences(
    onGet = { it.isAmoled },
    onSet = { s, v -> s.copy(isAmoled = v) }
)

@Composable
actual fun rememberShowDotsOnDice(): MutableState<Boolean> = rememberSettingsPreferences(
    onGet = { it.showDotsOnDice },
    onSet = { s, v -> s.copy(showDotsOnDice = v) }
)

@Composable
actual fun rememberUse24HourTime(): MutableState<Boolean> = rememberSettingsPreferences(
    onGet = { it.use24HourTime },
    onSet = { s, v -> s.copy(use24HourTime = v) }
)

private val settingsStuff by lazy {
    storeOf<YahtzeeSettings>(
        key = "settingsStuff",
        default = YahtzeeSettings()
    )
}

@Serializable
data class YahtzeeSettings(
    val showDotsOnDice: Boolean = false,
    val use24HourTime: Boolean = true,
    val themeColor: ThemeColor = ThemeColor.Dynamic,
    val isAmoled: Boolean = false,
    val showInstructions: Boolean = true,
)

@Composable
fun <T> rememberSettingsPreferences(
    onGet: (YahtzeeSettings) -> T,
    onSet: (YahtzeeSettings, T) -> YahtzeeSettings,
): MutableState<T> {
    val coroutineScope = rememberCoroutineScope()
    val state by remember {
        settingsStuff
            .updates
            .filterNotNull()
    }.collectAsStateWithLifecycle(YahtzeeSettings())

    return remember(state) {
        object : MutableState<T> {
            override var value: T
                get() = onGet(state)
                set(value) {
                    coroutineScope.launch {
                        runCatching { settingsStuff.set(onSet(state, value)) }
                    }
                }

            override fun component1() = value
            override fun component2(): (T) -> Unit = { value = it }
        }
    }
}

private val savedGameStuff by lazy {
    storeOf<SavedYahtzeeGame>(
        key = "savedGameStuff",
        default = null
    )
}

actual suspend fun deleteSavedYahtzeeGame() {
    savedGameStuff.delete()
}

actual suspend fun hasSavedYahtzeeGame(): Boolean = savedGameStuff.get() != null

actual suspend fun loadYahtzeeGame(): SavedYahtzeeGame? = savedGameStuff.get()

actual suspend fun saveYahtzeeGame(game: SavedYahtzeeGame) {
    savedGameStuff.set(game)
}

val keyEventFlow = MutableSharedFlow<KeyEvent>(
    replay = 1,
    onBufferOverflow = BufferOverflow.DROP_OLDEST
)

internal actual fun YahtzeeViewModel.setup() {
    keyEventFlow
        .onEach { keyEvent(it) }
        .launchIn(viewModelScope)
}

@Composable
actual fun DrawerHandler(
    enabled: Boolean,
    onBack: () -> Unit,
) {
}