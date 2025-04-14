package com.programmersbox.fiveofakind

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

@Serializable
data class SavedYahtzeeGame(
    val id: Long = Clock.System.now().toEpochMilliseconds(),
    val name: String = "Saved Game",
    val state: YahtzeeState = YahtzeeState.RollOne,
    val scores: Map<HandType, Int> = emptyMap(),
    val hand: List<SavedDice> = emptyList(),
    val timestamp: Long = Clock.System.now().toEpochMilliseconds(),
)

@Serializable
data class SavedDice(
    val value: Int,
    val location: String,
)

// Extension functions to convert between model classes and saved game classes
internal fun YahtzeeViewModel.toSavedGame(name: String = "Saved Game"): SavedYahtzeeGame {
    return SavedYahtzeeGame(
        name = name,
        state = state,
        scores = scores.scoreList,
        hand = hand.map { it.toSavedDice() },
    )
}

internal fun Dice.toSavedDice(): SavedDice {
    return SavedDice(
        value = value,
        location = location
    )
}

internal fun SavedDice.toDice(): Dice {
    return Dice(
        value = value,
        location = location
    )
}

internal fun YahtzeeViewModel.loadFromSavedGame(savedGame: SavedYahtzeeGame) {
    // Clear current state
    resetGame()

    // Set the game state
    state = savedGame.state

    // Load scores
    savedGame.scores.forEach { (handTypeName, score) ->
        scores.scoreList[handTypeName] = score
    }

    // Load dice
    hand.clear()
    hand.addAll(savedGame.hand.map { it.toDice() })

    // Load held dice
    hold.clear()
}
