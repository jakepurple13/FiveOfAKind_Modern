package com.programmersbox.fiveofakind

import androidx.compose.runtime.*
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okio.Path.Companion.toPath

private lateinit var dataStore: DataStore<Preferences>

class Settings(
    producePath: () -> String,
) {
    init {
        if (!::dataStore.isInitialized)
            dataStore = PreferenceDataStoreFactory.createWithPath(produceFile = { producePath().toPath() })
    }

    companion object {
        const val DATASTORE_FILE_NAME = "yahtzee.preferences_pb"
    }
}

@Composable
fun <T> rememberPreference(
    key: Preferences.Key<T>,
    defaultValue: T,
): MutableState<T> {
    val coroutineScope = rememberCoroutineScope()
    val state by remember(::dataStore.isInitialized) {
        if (::dataStore.isInitialized) {
            dataStore
                .data
                .mapNotNull { it[key] ?: defaultValue }
                .distinctUntilChanged()
        } else {
            emptyFlow()
        }
    }.collectAsStateWithLifecycle(initialValue = defaultValue)

    return remember(state) {
        object : MutableState<T> {
            override var value: T
                get() = state
                set(value) {
                    coroutineScope.launch {
                        runCatching { dataStore.edit { it[key] = value } }
                    }
                }

            override fun component1() = value
            override fun component2(): (T) -> Unit = { value = it }
        }
    }
}

@Composable
fun <T, R> rememberPreference(
    key: Preferences.Key<T>,
    mapToType: (T) -> R?,
    mapToKey: (R) -> T,
    defaultValue: R,
): MutableState<R> {
    val coroutineScope = rememberCoroutineScope()
    val state by remember(::dataStore.isInitialized) {
        if (::dataStore.isInitialized) {
            dataStore
                .data
                .mapNotNull { it[key]?.let(mapToType) ?: defaultValue }
                .distinctUntilChanged()
        } else {
            flowOf(defaultValue)
        }
    }.collectAsStateWithLifecycle(defaultValue)

    return remember(state) {
        object : MutableState<R> {
            override var value: R
                get() = state
                set(value) {
                    coroutineScope.launch {
                        runCatching { dataStore.edit { it[key] = value.let(mapToKey) } }
                    }
                }

            override fun component1() = value
            override fun component2(): (R) -> Unit = { value = it }
        }
    }
}

@Composable
actual fun rememberShowDotsOnDice() = rememberPreference(
    booleanPreferencesKey("showDiceOnDots"),
    true
)

@Composable
actual fun rememberUse24HourTime() = rememberPreference(
    booleanPreferencesKey("use24HourTime"),
    true
)

@Composable
actual fun rememberThemeColor(): MutableState<ThemeColor> = rememberPreference(
    key = stringPreferencesKey("theme_color"),
    mapToKey = { it.name },
    mapToType = { runCatching { ThemeColor.valueOf(it) }.getOrDefault(ThemeColor.Dynamic) },
    defaultValue = ThemeColor.Dynamic,
)

@Composable
actual fun rememberIsAmoled() = rememberPreference(
    key = booleanPreferencesKey("is_amoled"),
    defaultValue = false,
)

@Composable
actual fun rememberShowInstructions(): MutableState<Boolean> = rememberPreference(
    key = booleanPreferencesKey("show_instructions"),
    defaultValue = true,
)

// Save and load game state
private val SAVED_GAME_KEY = stringPreferencesKey("saved_yahtzee_game")

actual suspend fun saveYahtzeeGame(game: SavedYahtzeeGame) {
    if (::dataStore.isInitialized) {
        dataStore.edit { preferences ->
            preferences[SAVED_GAME_KEY] = Json.encodeToString(game)
        }
    }
}

actual suspend fun loadYahtzeeGame(): SavedYahtzeeGame? {
    if (!::dataStore.isInitialized) return null

    val preferences = dataStore.data.firstOrNull()
    val gameJson = preferences?.get(SAVED_GAME_KEY) ?: return null

    return runCatching {
        Json.decodeFromString<SavedYahtzeeGame>(gameJson)
    }.getOrNull()
}

actual suspend fun hasSavedYahtzeeGame(): Boolean {
    if (!::dataStore.isInitialized) return false

    val preferences = dataStore.data.firstOrNull()
    return preferences?.contains(SAVED_GAME_KEY) == true
}

actual suspend fun deleteSavedYahtzeeGame() {
    if (::dataStore.isInitialized) {
        dataStore.edit { preferences ->
            preferences.remove(SAVED_GAME_KEY)
        }
    }
}
