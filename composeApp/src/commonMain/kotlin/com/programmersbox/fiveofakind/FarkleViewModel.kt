package com.programmersbox.fiveofakind

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

// Farkle game states
internal enum class FarkleState {
    Rolling,    // Currently rolling dice
    Selecting,  // Selecting scoring dice
    Banked      // Turn ended, score banked
}

internal class FarkleViewModel : ViewModel() {

    // Game state
    var state by mutableStateOf(FarkleState.Banked)

    // Indicates if dice are currently rolling
    var rolling by mutableStateOf(false)

    // Show game over dialog
    var showGameOverDialog by mutableStateOf(true)

    // Target score to win (typically 10,000)
    var targetScore by mutableStateOf(10000)

    // Scores
    val scores = FarkleScores()

    // The 6 dice for Farkle
    val hand = mutableStateListOf(
        Dice(0, location = "1"),
        Dice(0, location = "2"),
        Dice(0, location = "3"),
        Dice(0, location = "4"),
        Dice(0, location = "5"),
        Dice(0, location = "6")
    )

    // Dice selected for scoring in current turn
    val selectedDice = mutableStateListOf<Dice>()

    // Dice set aside (already scored in this turn)
    val scoredDice = mutableStateListOf<Dice>()

    // Available hand types for the current dice
    private var _availableHandTypes = mutableStateListOf<FarkleScores.HandTypeInfo>()
    val availableHandTypes: List<FarkleScores.HandTypeInfo> get() = _availableHandTypes

    // Currently identified hand type based on selected dice
    private var _currentHandType by mutableStateOf<FarkleScores.HandTypeInfo?>(null)
    val currentHandType: FarkleScores.HandTypeInfo? get() = _currentHandType

    // Roll the available dice
    fun rollDice() {
        // Can't roll if all dice are already scored
        if (hand.all { it in scoredDice }) return

        viewModelScope.launch {
            rolling = true

            // Move selected dice to scored dice
            if (selectedDice.isNotEmpty()) {
                // Add current selection score to turn score
                scores.currentTurnScore += scores.calculateScore(selectedDice)

                // Move selected dice to scored dice
                scoredDice.addAll(selectedDice)
                selectedDice.clear()
                _currentHandType = null
            }

            // Roll remaining dice
            (0 until hand.size).map { i ->
                async(Dispatchers.Unconfined) {
                    if (hand[i] !in scoredDice) {
                        hand[i].roll()
                    }
                }
            }.awaitAll()

            rolling = false

            // Check if the roll is a Farkle (no scoring dice)
            val availableDice = hand.filter { it !in scoredDice }
            if (!scores.hasScoringDice(availableDice)) {
                // Farkle! Player loses turn and accumulated points
                scores.farkle()
                state = FarkleState.Banked
                // Don't automatically reset turn - let player use reset button
            } else {
                // Identify available hand types
                updateAvailableHandTypes(availableDice)
                state = FarkleState.Selecting
            }
        }
    }

    // Update available hand types based on current dice
    private fun updateAvailableHandTypes(availableDice: Collection<Dice>) {
        _availableHandTypes.clear()
        _availableHandTypes.addAll(scores.identifyHandTypes(availableDice))
    }

    // Toggle selection of a die for scoring
    fun toggleDieSelection(die: Dice) {
        if (state != FarkleState.Selecting) return
        if (die in scoredDice) return

        if (die in selectedDice) {
            selectedDice.remove(die)
        } else {
            selectedDice.add(die)
        }

        // Update current hand type based on selected dice
        updateCurrentHandType()
    }

    // Update current hand type based on selected dice
    private fun updateCurrentHandType() {
        _currentHandType = if (selectedDice.isEmpty()) {
            null
        } else {
            // Find the hand type that matches the selected dice
            scores.identifyHandTypes(selectedDice).firstOrNull()
        }
    }

    // Get holdable dice for a specific hand type
    fun getHoldableDiceForHandType(handType: FarkleHandType): List<Dice> {
        return availableHandTypes
            .firstOrNull { it.handType == handType }
            ?.dice ?: emptyList()
    }

    // Check if a die is holdable for any available hand type
    fun isDiceHoldable(die: Dice): Boolean {
        return availableHandTypes.any { handTypeInfo ->
            die in handTypeInfo.dice
        }
    }

    // Bank the current score and end turn
    fun bankScore() {
        if (selectedDice.isNotEmpty()) {
            // Add current selection score to turn score
            scores.currentTurnScore += scores.calculateScore(selectedDice)
            selectedDice.clear()
        }

        // Bank the score
        scores.bankScore()
        state = FarkleState.Banked

        // Check if game is over
        if (scores.totalScore >= targetScore) {
            showGameOverDialog = true
        }
        // Don't automatically reset turn - let player use reset button
    }

    // Reset for a new turn
    fun resetTurn() {
        selectedDice.clear()
        scoredDice.clear()
        hand.forEach { it.value = 0 }
        _availableHandTypes.clear()
        _currentHandType = null
    }

    // Start a new turn
    fun startNewTurn() {
        resetTurn()
        state = FarkleState.Rolling
        rollDice()
    }

    // Reset the game
    fun resetGame() {
        resetTurn()
        scores.resetScores()
        showGameOverDialog = false
        state = FarkleState.Banked
    }

    // Check if the current selection is valid (contains scoring dice)
    fun isValidSelection(): Boolean {
        return scores.calculateScore(selectedDice) > 0
    }
}
