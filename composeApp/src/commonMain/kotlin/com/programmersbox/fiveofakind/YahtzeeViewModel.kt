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
import kotlinx.serialization.Serializable

@Serializable
enum class YahtzeeState { RollOne, RollTwo, RollThree, Stop }

//TODO: THIS SHOULD BE TRUE!!!
const val IS_NOT_DEBUG = true

internal class YahtzeeViewModel : ViewModel() {

    var rolling by mutableStateOf(false)

    var showGameOverDialog by mutableStateOf(true)

    var state by mutableStateOf(YahtzeeState.RollOne)

    val scores = YahtzeeScores()

    val hand = mutableStateListOf(
        Dice(0, location = "1"),
        Dice(0, location = "2"),
        Dice(0, location = "3"),
        Dice(0, location = "4"),
        Dice(0, location = "5")
    )

    val hold = mutableStateListOf<Dice>()

    init {
        viewModelScope.launch {
            if (hasSavedYahtzeeGame()) {
                loadYahtzeeGame()?.let { loadFromSavedGame(it) }
            }
        }
    }

    fun reroll() {
        viewModelScope.launch {
            rolling = true
            hand.filter { it !in hold }
                .map { async(Dispatchers.Unconfined) { it.roll() } }
                .awaitAll()
            rolling = false
            state = when (state) {
                YahtzeeState.RollOne -> YahtzeeState.RollTwo
                YahtzeeState.RollTwo -> YahtzeeState.RollThree
                YahtzeeState.RollThree -> YahtzeeState.Stop
                YahtzeeState.Stop -> YahtzeeState.RollOne
            }
            saveYahtzeeGame(toSavedGame())
        }
    }

    // Consolidated method for placing small scores (ones through sixes)
    private fun placeSmallScore(handType: HandType) {
        if (hand.all { it.value == 0 } && IS_NOT_DEBUG) return
        scores.getSmall(hand, handType)
        reset()
    }

    // Individual methods that call the consolidated method
    fun placeOnes() = placeSmallScore(HandType.Ones)
    fun placeTwos() = placeSmallScore(HandType.Twos)
    fun placeThrees() = placeSmallScore(HandType.Threes)
    fun placeFours() = placeSmallScore(HandType.Fours)
    fun placeFives() = placeSmallScore(HandType.Fives)
    fun placeSixes() = placeSmallScore(HandType.Sixes)

    // Helper method to place a score based on hand type
    private fun placeLargeScore(action: (Collection<Dice>) -> Int) {
        if (hand.all { it.value == 0 } && IS_NOT_DEBUG) return
        action(hand)
        reset()
    }

    fun placeThreeOfKind() = placeLargeScore(scores::getThreeOfAKind)
    fun placeFourOfKind() = placeLargeScore(scores::getFourOfAKind)
    fun placeFullHouse() = placeLargeScore(scores::getFullHouse)
    fun placeSmallStraight() = placeLargeScore(scores::getSmallStraight)
    fun placeLargeStraight() = placeLargeScore(scores::getLargeStraight)
    fun placeYahtzee() = placeLargeScore(scores::getYahtzee)
    fun placeChance() = placeLargeScore(scores::getChance)

    private fun reset() {
        hold.clear()
        hand.forEach { it.value = 0 }
        state = YahtzeeState.RollOne
        viewModelScope.launch { saveYahtzeeGame(toSavedGame()) }
    }

    fun resetGame() {
        reset()
        scores.resetScores()
        showGameOverDialog = true
        viewModelScope.launch { deleteSavedYahtzeeGame() }
    }

    fun toggleDiceHold(index: Int): Boolean {
        hand
            .getOrNull(index)
            ?.also { if (it.value == 0) return false }
            ?.let { dice -> if (dice in hold) hold.remove(dice) else hold.add(dice) }

        return true
    }

    init {
        setup()
    }
}
